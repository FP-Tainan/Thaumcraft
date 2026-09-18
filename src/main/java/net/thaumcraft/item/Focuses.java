package net.thaumcraft.item;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
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
        if (!WandItem.consumeRaw(wand, focus.cost(), false)) return false;
        if (level.isClientSide()) {
            clientEffects.tick(level, player, wand, focus);
            return true;
        }
        return switch (focus.type()) {
            case "fire" -> breatheFire(level, player, wand, focus);
            case "excavation" -> excavate(level, player, wand, focus);
            case "frost" -> shootFrost(level, player, wand, focus);
            case "shock" -> shock(level, player, wand, focus);
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
        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 far = eyes.add(look.scale(range));
        HitResult wall = level.clip(new ClipContext(eyes, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                player));
        if (wall.getType() != HitResult.Type.MISS) far = wall.getLocation();
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eyes, far, box,
                entity -> !entity.isSpectator() && entity.isPickable(), eyes.distanceToSqr(far));
        return hit == null ? null : hit.getEntity();
    }

    // ----------------------------------------------------------------- fogo

    private static boolean breatheFire(Level level, Player player, ItemStack wand, FocusItem focus) {
        long now = System.currentTimeMillis();
        if (FIRE_SOUND.getOrDefault(player.getUUID(), 0L) < now) {
            level.playSound(null, player, TCSounds.FIRELOOP.value(), SoundSource.PLAYERS, 0.33f, 2.0f);
            FIRE_SOUND.put(player.getUUID(), now + 500L);
        }
        if (!WandItem.consumeRaw(wand, focus.cost(), true)) return false;
        for (int a = 0; a < 2; a++) {
            EmberEntity ember = new EmberEntity(level, player, 15.0f);
            ember.setPos(ember.position().add(ember.getDeltaMovement()));
            level.addFreshEntity(ember);
        }
        return true;
    }

    // ----------------------------------------------------------------- gelo

    private static boolean shootFrost(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeRaw(wand, focus.cost(), true)) return false;
        FrostShardEntity shard = new FrostShardEntity(level, player, 1.0f);
        shard.setDamage(3.0f);
        level.addFreshEntity(shard);
        level.playSound(null, shard, TCSounds.ICE.value(), SoundSource.PLAYERS, 0.4f,
                1.0f + level.getRandom().nextFloat() * 0.1f);
        return true;
    }

    // ----------------------------------------------------------------- raio

    private static boolean shock(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (!WandItem.consumeRaw(wand, focus.cost(), true)) return false;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.SHOCK.value(),
                SoundSource.PLAYERS, 0.25f, 1.0f);
        Entity pointed = pointedEntity(level, player, 20.0);
        boolean pvp = !(pointed instanceof Player) || (level instanceof ServerLevel server && server.isPvpAllowed());
        if (pointed instanceof LivingEntity && pvp) {
            pointed.hurt(level.damageSources().playerAttack(player), SHOCK_DAMAGE);
        }
        return true;
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
        if (step.breakNow() && WandItem.consumeRaw(wand, focus.cost(), true)) {
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
