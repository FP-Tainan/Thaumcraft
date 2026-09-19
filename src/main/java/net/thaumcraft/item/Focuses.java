package net.thaumcraft.item;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.EmberEntity;
import net.thaumcraft.entity.FrostShardEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O que cada foco faz quando a varinha aponta: os {@code ItemFocusFire}, {@code ItemFocusFrost},
 * {@code ItemFocusShock} e {@code ItemFocusExcavation} da 4.2.3.5, descompilados, sem as melhorias.
 *
 * <ul>
 * <li>fogo: enquanto o botão está apertado, cobra ignis 10 (centésimos) por tique e solta duas brasas, que
 * voam espalhadas, queimam dois de vida de quem acertam e deixam a criatura em chamas por três segundos. Sem a
 * melhoria do fogo alquímico elas não acendem bloco nenhum;</li>
 * <li>gelo: tiro único — aqua 5, ignis 2 e perditio 2 — de uma esfera de gelo que machuca três e quica até
 * três vezes antes de se partir;</li>
 * <li>raio: aer 25 por tique; do lado de quem vê, um raio da mão até a mira, e do servidor, quatro de dano por
 * tique na criatura apontada a até vinte blocos;</li>
 * <li>escavação: o feixe rói o bloco na mira aos poucos, cinco centésimos da dureza por tique — vinte e cinco
 * em pedra, terra e areia, e o triplo disso em obsidiana — e só cobra terra 15 quando o bloco cai.</li>
 * </ul>
 */
public final class Focuses {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void tick(Level level, Player player, ItemStack wand, FocusItem focus);
    }

    public static ClientEffects clientEffects = (level, player, wand, focus) -> {
    };

    /** O alcance do {@code BlockUtils.getTargetBlock} do original. */
    private static final double TARGET_REACH = 10.0;
    /** O dano do raio sem potência, por tique. */
    private static final float SHOCK_DAMAGE = 4.0f;

    private static final Map<UUID, Dig> DIGS = new HashMap<>();
    private static final Map<UUID, Long> FIRE_SOUND = new HashMap<>();
    private static final Map<UUID, Long> RUMBLE_SOUND = new HashMap<>();

    private Focuses() {
    }

    /** O item de foco daquele tipo, para devolver à mão de quem trocou. */
    public static Item byType(String type) {
        return TCItems.FOCI.get(type);
    }

    /** O foco preso nesta varinha, se houver. */
    public static FocusItem on(ItemStack wand) {
        String type = wand.get(TCComponents.WAND_FOCUS);
        if (type == null) return null;
        Item found = byType(type);
        return found instanceof FocusItem focus ? focus : null;
    }

    /**
     * Um tique de foco em uso, dos dois lados.
     *
     * @return se a varinha deu conta de pagar e o foco agiu
     */
    public static boolean tick(Level level, Player player, ItemStack wand, FocusItem focus) {
        // o original sempre confere antes, sem gastar; quem gasta é cada foco, na hora dele
        if (!WandItem.consumeRaw(wand, focus.cost(), false, player)) return false;
        if (level.isClientSide()) {
            clientEffects.tick(level, player, wand, focus);
            return true;
        }
        return switch (focus.type()) {
            case "fire" -> breatheFire(level, player, wand, focus);
            case "excavation" -> excavate(level, player, wand, focus);
            case "frost" -> shootFrost(level, player, wand, focus);
            case "shock" -> shock(level, player, wand, focus);
            case "portable_hole" -> portableHole(level, player, wand, focus);
            case "trade" -> trade(level, player, wand, focus);
            case "primal" -> primal(level, player, wand);
            case "warding" -> warding(level, player, wand, focus);
            case "hellbat" -> hellbat(level, player, wand, focus);
            case "pech" -> pechBlast(level, player, wand, focus);
            default -> false;
        };
    }

    /** Quando o botão solta, a escavação esquece o bloco que estava roendo. */
    public static void stop(Player player) {
        DIGS.remove(player.getUUID());
    }

    // ----------------------------------------------------------------- mira

    /** O {@code BlockUtils.getTargetBlock}: o bloco na mira a até dez blocos, sem pegar líquido. */
    public static HitResult targetBlock(Level level, Player player) {
        Vec3 eyes = player.getEyePosition();
        Vec3 far = eyes.add(player.getViewVector(1.0f).scale(TARGET_REACH));
        return level.clip(new ClipContext(eyes, far, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    /** O {@code EntityUtils.getPointedEntity}: a primeira criatura na mira, com a caixa folgada em 1,1. */
    public static Entity pointedEntity(Level level, Player player, double range) {
        return pointedEntity(level, player, range, null);
    }

    /** O mesmo, pulando as criaturas de uma classe (o foco dos morcegos não mira os próprios morcegos). */
    public static Entity pointedEntity(Level level, Player player, double range, Class<? extends Entity> skip) {
        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 far = eyes.add(look.scale(range));
        HitResult wall = level.clip(new ClipContext(eyes, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                player));
        if (wall.getType() != HitResult.Type.MISS) far = wall.getLocation();
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eyes, far, box,
                entity -> !entity.isSpectator() && entity.isPickable() && (skip == null || !skip.isInstance(entity)), eyes.distanceToSqr(far));
        return hit == null ? null : hit.getEntity();
    }

    // ----------------------------------------------------------------- fogo

    private static boolean breatheFire(Level level, Player player, ItemStack wand, FocusItem focus) {
        long now = System.currentTimeMillis();
        if (FIRE_SOUND.getOrDefault(player.getUUID(), 0L) < now) {
            level.playSound(null, player, TCSounds.FIRELOOP.value(), SoundSource.PLAYERS, 0.33f, 2.0f);
            FIRE_SOUND.put(player.getUUID(), now + 500L);
        }
        if (!WandItem.consumeRaw(wand, focus.cost(), true, player)) return false;
        for (int a = 0; a < 2; a++) {
            EmberEntity ember = new EmberEntity(level, player, 15.0f);
            ember.setPos(ember.position().add(ember.getDeltaMovement()));
            level.addFreshEntity(ember);
        }
        return true;
    }

    // ----------------------------------------------------------------- gelo

    private static boolean shootFrost(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeRaw(wand, focus.cost(), true, player)) return false;
        FrostShardEntity shard = new FrostShardEntity(level, player, 1.0f);
        shard.setDamage(3.0f);
        level.addFreshEntity(shard);
        level.playSound(null, shard, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.4f,
                1.0f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    // ----------------------------------------------------------------- raio

    private static boolean shock(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeRaw(wand, focus.cost(), true, player)) return false;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.SHOCK.value(),
                SoundSource.PLAYERS, 0.25f, 1.0f);
        Entity pointed = pointedEntity(level, player, 20.0);
        boolean pvp = !(pointed instanceof Player) || (level instanceof ServerLevel server && server.isPvpAllowed());
        if (pointed instanceof LivingEntity && pvp) {
            pointed.hurt(level.damageSources().playerAttack(player), SHOCK_DAMAGE);
        }
        return true;
    }

    // ----------------------------------------------------------------- primordial

    private static final Map<UUID, Long> PRIMAL_COOLDOWN = new HashMap<>();

    /**
     * O {@code getVisCost} do {@code ItemFocusPrimal}: de 50 a 250 centésimos de cada primário, sorteados por
     * um {@code Random} semeado com o relógio em fatias de 200 milissegundos.
     */
    public static net.thaumcraft.api.aspects.AspectList primalCost(long millis) {
        java.util.Random rand = new java.util.Random(millis / 200L);
        net.thaumcraft.api.aspects.AspectList cost = new net.thaumcraft.api.aspects.AspectList()
                .add(net.thaumcraft.api.aspects.Aspects.WATER, 50 + rand.nextInt(5) * 50)
                .add(net.thaumcraft.api.aspects.Aspects.AIR, 50 + rand.nextInt(5) * 50)
                .add(net.thaumcraft.api.aspects.Aspects.EARTH, 50 + rand.nextInt(5) * 50)
                .add(net.thaumcraft.api.aspects.Aspects.FIRE, 50 + rand.nextInt(5) * 50)
                .add(net.thaumcraft.api.aspects.Aspects.ORDER, 50 + rand.nextInt(5) * 50)
                .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 50 + rand.nextInt(5) * 50);
        // no relógio zero (o piso do registro) todos saem 50
        if (millis == 0L) {
            cost = new net.thaumcraft.api.aspects.AspectList();
            for (net.thaumcraft.api.aspects.Aspect aspect : net.thaumcraft.api.aspects.Aspects.primals()) cost.add(aspect, 50);
        }
        return cost;
    }

    /** O {@code ItemFocusPrimal}: meio segundo de espera entre tiros, e a esfera sai com o som do gelo. */
    private static boolean primal(Level level, Player player, ItemStack wand) {
        long now = System.currentTimeMillis();
        if (PRIMAL_COOLDOWN.getOrDefault(player.getUUID(), 0L) > now) return false;
        if (!WandItem.consumeRaw(wand, primalCost(now), true, player)) return false;
        PRIMAL_COOLDOWN.put(player.getUUID(), now + 500L);
        net.thaumcraft.entity.PrimalOrbEntity orb = new net.thaumcraft.entity.PrimalOrbEntity(level, player);
        level.addFreshEntity(orb);
        level.playSound(null, orb, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.3f,
                0.8f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    // ----------------------------------------------------------------- pechs

    private static final Map<UUID, Long> PECH_COOLDOWN = new HashMap<>();

    /** O {@code ItemFocusPech}: a rajada do pech, de quem segura a varinha; quatro por segundo. */
    private static boolean pechBlast(Level level, Player player, ItemStack wand, FocusItem focus) {
        long now = System.currentTimeMillis();
        if (PECH_COOLDOWN.getOrDefault(player.getUUID(), 0L) > now) return false;
        if (!WandItem.consumeRaw(wand, focus.cost(), true, player)) return false;
        PECH_COOLDOWN.put(player.getUUID(), now + 250L);
        net.thaumcraft.entity.PechBlastEntity blast = new net.thaumcraft.entity.PechBlastEntity(level, player, 0, 0, false);
        level.addFreshEntity(blast);
        level.playSound(null, blast, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.4f, 1.0f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    // ----------------------------------------------------------------- nove infernos

    private static final Map<UUID, Long> HELLBAT_COOLDOWN = new HashMap<>();

    /**
     * O {@code ItemFocusHellbat}: um morcego de fogo invocado sai da mão e vai atrás da criatura na mira, a até 32
     * blocos. Sem criatura na mira, nada acontece; um por segundo.
     */
    private static boolean hellbat(Level level, Player player, ItemStack wand, FocusItem focus) {
        long now = System.currentTimeMillis();
        if (HELLBAT_COOLDOWN.getOrDefault(player.getUUID(), 0L) > now) return false;
        Entity pointed = pointedEntity(level, player, 32.0, net.thaumcraft.entity.FireBatEntity.class);
        if (!(pointed instanceof LivingEntity target)) return false;
        if (target instanceof Player && !(level instanceof ServerLevel server && server.isPvpAllowed())) return false;
        HELLBAT_COOLDOWN.put(player.getUUID(), now + 1000L);
        double yaw = player.getYRot() / 180.0f * (float) Math.PI;
        Vec3 look = player.getViewVector(1.0f);
        double px = player.getX() - Math.cos(yaw) * 0.16f + look.x * 0.5;
        double py = player.getBoundingBox().minY + player.getBbHeight() / 2.0f + 0.25 - 0.05000000014901161 + look.y * 0.5;
        double pz = player.getZ() - Math.sin(yaw) * 0.16f + look.z * 0.5;
        net.thaumcraft.entity.FireBatEntity bat = net.thaumcraft.registry.TCEntities.FIREBAT.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (bat == null) return false;
        bat.snapTo(px, py + bat.getBbHeight(), pz, player.getYRot(), 0.0f);
        bat.setTarget(target);
        bat.owner = player;
        bat.setIsSummoned(true);
        bat.setIsBatHanging(false);
        if (WandItem.consumeRaw(wand, focus.cost(), true, player) && level.addFreshEntity(bat)) {
            level.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_MOBBLOCK_SPAWN, BlockPos.containing(px, py, pz), 0);
            level.playSound(null, bat, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.2f, 0.95f + level.getRandom().nextFloat() * 0.1f);
        } else {
            level.playSound(null, player, TCSounds.WAND_FAIL.value(), SoundSource.PLAYERS, 0.1f, 0.8f + level.getRandom().nextFloat() * 0.1f);
        }
        return true;
    }

    // ----------------------------------------------------------------- proteção

    /** O {@code delay} do original: meio segundo por bloco, para um clique não proteger e desfazer de uma vez. */
    private static final Map<String, Long> WARD_DELAY = new HashMap<>();

    /** O dono de uma proteção, como o original conta: o número do nome do jogador. */
    public static int wardOwner(Player player) {
        return player.getName().getString().hashCode();
    }

    /**
     * O {@code ItemFocusWarding}: num bloco maciço sem miolo, protege; num bloco já protegido pelo mesmo
     * jogador, desfaz. Sem as melhorias de arquiteto, é um bloco por vez.
     */
    private static boolean warding(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0f, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = block.getBlockPos();
        String key = pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + level.dimension();
        long now = System.currentTimeMillis();
        if (WARD_DELAY.getOrDefault(key, 0L) > now) return false;
        WARD_DELAY.put(key, now + 500L);

        BlockState state = level.getBlockState(pos);
        net.minecraft.world.level.block.entity.BlockEntity tile = level.getBlockEntity(pos);
        int owner = wardOwner(player);
        boolean changed = false;
        if (tile == null && state.isSolidRender()) {
            if (WandItem.consumeRaw(wand, focus.cost(), true, player)) {
                int light = state.getLightEmission();
                level.setBlock(pos, net.thaumcraft.registry.TCBlocks.WARDED.defaultBlockState()
                        .setValue(net.thaumcraft.block.WardedBlock.LIGHT, light), Block.UPDATE_ALL);
                if (level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.WardedBlockEntity warded) {
                    warded.ward(state, owner);
                    level.sendBlockUpdated(pos, state, level.getBlockState(pos), Block.UPDATE_ALL);
                }
                if (level instanceof ServerLevel server) net.thaumcraft.net.TCNetwork.blockSparkle(server, pos, 0xFCA000);
                changed = true;
            }
        } else if (tile instanceof net.thaumcraft.block.entity.WardedBlockEntity warded && warded.owner() == owner) {
            level.setBlock(pos, warded.stored(), Block.UPDATE_ALL);
            if (level instanceof ServerLevel server) net.thaumcraft.net.TCNetwork.blockSparkle(server, pos, 0xFCA000);
            changed = true;
        }
        if (changed) {
            level.playSound(null, pos, TCSounds.ZAP.value(), SoundSource.PLAYERS, 0.25f, 1.0f);
        }
        return changed;
    }

    // ----------------------------------------------------------------- buraco portátil

    /**
     * O {@code ItemFocusPortableHole}: conta quantos blocos maciços há em linha, a partir do que se mirou, até
     * trinta e três; cobra o custo vezes essa profundidade e abre o túnel, que dura seis segundos.
     */
    private static boolean portableHole(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0f, true);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos start = block.getBlockPos();
        Direction face = block.getDirection();
        int distance = 0;
        BlockPos at = start;
        for (; distance < 33; distance++) {
            BlockState state = level.getBlockState(at);
            if (state.is(Blocks.BEDROCK) || state.is(net.thaumcraft.registry.TCBlocks.HOLE) || state.isAir()
                    || state.getDestroySpeed(level, at) < 0.0f) {
                break;
            }
            at = at.relative(face.getOpposite());
        }
        // o custo é o de um bloco vezes a profundidade (o merge do original fica com o maior)
        net.thaumcraft.api.aspects.AspectList cost = focus.cost();
        for (net.thaumcraft.api.aspects.Aspect aspect : cost.getAspects()) {
            cost.merge(aspect, cost.getAmount(aspect) * distance);
        }
        if (WandItem.consumeRaw(wand, cost, true, player)) {
            net.thaumcraft.block.entity.HoleBlockEntity.createHole(level, start, face.get3DDataValue(), distance + 1,
                    net.thaumcraft.block.entity.HoleBlockEntity.DURATION);
        }
        level.playSound(null, start, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    // ----------------------------------------------------------------- troca equivalente

    /**
     * O {@code ItemFocusTrade}: agachado, a varinha guarda o bloco da mira; de pé, troca o bloco da mira pelo
     * guardado, e a troca se espalha por três vizinhos iguais à mostra.
     */
    private static boolean trade(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0f, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return true;
        BlockPos pos = block.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) == null && state.getBlock().asItem() != net.minecraft.world.item.Items.AIR) {
                wand.set(TCComponents.WAND_PICKED,
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(state.getBlock().asItem()).toString());
            }
            return true;
        }
        Item picked = picked(wand);
        if (picked != null && level.getBlockEntity(pos) == null && level instanceof ServerLevel server) {
            Swapper.add(server, pos, state, picked, 3, player, player.getInventory().getSelectedSlot());
        }
        return true;
    }

    /** O bloco guardado na varinha, se houver. */
    public static Item picked(ItemStack wand) {
        String id = wand.get(TCComponents.WAND_PICKED);
        if (id == null) return null;
        var found = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.Identifier.parse(id));
        return found.orElse(null);
    }

    /**
     * O golpe com a varinha de troca, o {@code onEntitySwing} do original: troca só o bloco batido, sem se
     * espalhar — e a varinha com esse foco não quebra bloco nenhum.
     */
    public static net.minecraft.world.InteractionResult tradeSwing(Player player, Level level,
            net.minecraft.world.InteractionHand hand, BlockPos pos, Direction direction) {
        ItemStack wand = player.getItemInHand(hand);
        if (!(wand.getItem() instanceof WandItem)) return net.minecraft.world.InteractionResult.PASS;
        FocusItem focus = on(wand);
        if (focus == null || !focus.type().equals("trade")) return net.minecraft.world.InteractionResult.PASS;
        Item picked = picked(wand);
        if (picked != null && level instanceof ServerLevel server && level.getBlockEntity(pos) == null) {
            Swapper.add(server, pos, level.getBlockState(pos), picked, 0, player, player.getInventory().getSelectedSlot());
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    // ----------------------------------------------------------------- escavação

    private static boolean excavate(Level level, Player player, ItemStack wand, FocusItem focus) {
        HitResult mop = targetBlock(level, player);
        long now = System.currentTimeMillis();
        if (mop.getType() != HitResult.Type.MISS) {
            if (RUMBLE_SOUND.getOrDefault(player.getUUID(), 0L) < now) {
                Vec3 at = mop.getLocation();
                level.playSound(null, at.x, at.y, at.z, TCSounds.RUMBLE.value(), SoundSource.PLAYERS, 0.3f, 1.0f);
                RUMBLE_SOUND.put(player.getUUID(), now + 1200L);
            }
        } else {
            RUMBLE_SOUND.put(player.getUUID(), 0L);
        }

        BlockHitResult block = mop instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK
                && level.mayInteract(player, b.getBlockPos()) ? b : null;
        Dig dig = DIGS.computeIfAbsent(player.getUUID(), id -> new Dig());
        Dig.Step step = dig.advance(level, block, false);
        if (step.breakNow() && WandItem.consumeRaw(wand, focus.cost(), true, player)) {
            breakBlock((ServerLevel) level, player, step.pos());
            dig.reset();
        }
        return true;
    }

    /** O {@code excavate} do original: quebra com o que o bloco daria, e a experiência dele. */
    private static void breakBlock(ServerLevel level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        var entity = level.getBlockEntity(pos);
        if (!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, entity)) return;
        state.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
        level.destroyBlock(pos, true, player);
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, entity);
    }

    /**
     * O quanto o feixe já roeu do bloco da mira. Os dois lados levam a mesma conta: o servidor para saber
     * quando quebrar, e quem vê para desenhar a rachadura.
     */
    public static final class Dig {
        public BlockPos pos;
        private float count;

        /**
         * @param pos      o bloco sendo roído
         * @param progress o quadro da rachadura, de zero a nove, ou −1 sem rachadura
         * @param breakNow se o bloco já foi roído inteiro
         */
        public record Step(BlockPos pos, int progress, boolean breakNow) {
        }

        public void reset() {
            this.pos = null;
            this.count = 0.0f;
        }

        public Step advance(Level level, BlockHitResult hit, boolean client) {
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
                this.reset();
                return new Step(null, -1, false);
            }
            BlockPos at = hit.getBlockPos();
            BlockState state = level.getBlockState(at);
            float hardness = state.getDestroySpeed(level, at);
            if (hardness < 0.0f) return new Step(null, -1, false);
            float speed = 0.05f;
            if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) speed = 0.25f;
            if (state.is(Blocks.OBSIDIAN)) speed *= 3.0f;

            if (!at.equals(this.pos)) {
                this.pos = at;
                this.count = 0.0f;
                return new Step(at, -1, false);
            }
            float bc = this.count;
            int progress = client && bc > 0.0f && !state.isAir() ? (int) (bc / hardness * 9.0f) : -1;
            if (bc >= hardness) {
                if (client) this.count = 0.0f;
                return new Step(at, progress, !client);
            }
            this.count = bc + speed;
            return new Step(at, progress, false);
        }
    }
}
