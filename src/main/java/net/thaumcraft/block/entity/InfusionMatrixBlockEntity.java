package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaSources;
import net.thaumcraft.crafting.InfusionEnchantmentRecipe;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Warp;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A matriz rúnica: o {@code TileInfusionMatrix} da 4.2.3.5, onde a infusão acontece.
 *
 * <p>A construção é a do diagrama do altar do original: a matriz no ar, um pedestal dois blocos abaixo dela e um pilar
 * de infusão em cada canto desse pedestal. Ao toque da varinha, a matriz junta o que está no pedestal do meio com o que
 * está nos pedestais em volta e procura uma receita (ou um encantamento) que o jogador conheça.
 *
 * <p>Achando, ela trabalha de dez em dez tiques ({@code craftCycle}): primeiro cobra a experiência (se for encantamento),
 * depois a essência, uma unidade por vez, dos jarros a até doze blocos — e quando falta, a instabilidade pode subir —,
 * depois os ingredientes, um por um: cinco ciclos puxando as migalhas de cada pedestal até ele se esvaziar. Terminado
 * tudo, a coisa nova aparece no pedestal do meio.
 *
 * <p>A <strong>instabilidade</strong> é a da receita somada à falta de simetria da construção (pedestais sem par do
 * outro lado, estabilizadores sem par). A cada ciclo há {@code instabilidade} chances em quinhentas de um dos vinte e um
 * azares do original: cuspir ou destruir um ingrediente (às vezes com gosma ou gás de fluxo, ou uma explosão), raios,
 * mácula e cansaço de vis em quem estiver perto, uma explosão na matriz ou distorção num jogador. Tirar a coisa do meio
 * no meio do serviço também sorteia um azar, e a infusão para.
 */
public class InfusionMatrixBlockEntity extends BlockEntity {
    /** O que o cliente desenha: as runas, as migalhas dos pedestais, a experiência e os raios da instabilidade. */
    public interface ClientEffects {
        void runes(Level level, BlockPos pedestal, float r, float g, float b);

        void pedestal(Level level, BlockPos pedestal, BlockPos matrix, ItemStack stack);

        void experience(Level level, Entity from, BlockPos matrix);

        void bolt(Vec3 from, Vec3 to);
    }

    public static ClientEffects clientEffects;

    /** Um fio do {@code sourceFX}: de onde a matriz puxa, por quantos tiques ainda, e a criatura (na experiência). */
    private static final class SourceFx {
        final BlockPos loc;
        int ticks;
        final int entity;

        SourceFx(BlockPos loc, int ticks, int entity) {
            this.loc = loc;
            this.ticks = ticks;
            this.entity = entity;
        }
    }

    private final List<BlockPos> pedestals = new ArrayList<>();
    private boolean active;
    private boolean crafting;
    private boolean checkSurroundings = true;
    private int symmetry;
    private int instability;
    private AspectList recipeEssentia = new AspectList();
    private final List<Ingredient> recipeIngredients = new ArrayList<>();
    /** A saída da receita comum; na de encantamento, o encantamento que sobe um nível. */
    private ItemStack recipeOutput = ItemStack.EMPTY;
    @Nullable
    private ResourceKey<Enchantment> recipeEnchantment;
    @Nullable
    private String recipePlayer;
    private ItemStack recipeInput = ItemStack.EMPTY;
    private int recipeInstability;
    private int recipeXP;
    private int recipeType;
    private int count;
    private int countDelay = 10;
    private int itemCount;
    /** Só de quem vê. */
    private final Map<String, SourceFx> sourceFX = new HashMap<>();
    public int craftCount;
    public float startUp;

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFUSION_MATRIX, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        matrix.count++;
        if (matrix.checkSurroundings) {
            matrix.checkSurroundings = false;
            matrix.getSurroundings();
        }
        if (level.isClientSide()) {
            matrix.doEffects(level, pos);
            return;
        }
        if (matrix.count % (matrix.crafting ? 20 : 100) == 0 && !validLocation(level, pos)) {
            matrix.active = false;
            matrix.sync();
            return;
        }
        if (matrix.active && matrix.crafting && matrix.count % matrix.countDelay == 0) {
            matrix.craftCycle((ServerLevel) level, pos);
            matrix.setChanged();
        }
    }

    /** O {@code validLocation}: pedestal dois blocos abaixo e um pilar de infusão em cada canto dele. */
    public static boolean validLocation(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity)) return false;
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                if (!(level.getBlockEntity(pos.offset(dx, -2, dz)) instanceof InfusionPillarBlockEntity)) return false;
            }
        }
        return true;
    }

    /**
     * O toque da varinha: o {@code onWandRightClick} da matriz e, se ela não quiser, o gatilho três da varinha (o
     * {@code createInfusionAltar}). Ligada e parada, começa a infusão; desligada com os pilares de pé, liga; desligada sem
     * eles, tenta erguer o altar.
     */
    public boolean poke(Level level, BlockPos pos, Player player, ItemStack wand) {
        if (level.isClientSide()) return true;
        if (this.active && !this.crafting) {
            this.craftingStart(player);
            return true;
        }
        if (!this.active && validLocation(level, pos)) {
            this.active = true;
            this.sync();
            return true;
        }
        if (this.active) return true;
        return raiseAltar(level, pos, wand, player);
    }

    /** O {@code fitInfusionAltar}: a construção de pedra, antes de virar altar. */
    public static boolean fitsAltar(Level level, BlockPos pos) {
        BlockPos floor = pos.below(2);
        if (!(level.getBlockEntity(floor) instanceof PedestalBlockEntity)) return false;
        net.minecraft.world.level.block.Block stone = TCBlocks.BUILDING.get("arcane_stone");
        net.minecraft.world.level.block.Block bricks = TCBlocks.BUILDING.get("arcane_stone_bricks");
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean corner = dx != 0 && dz != 0;
                // no andar do pedestal: tijolos nos cantos, pedestal no meio, o resto vazio
                BlockPos low = floor.offset(dx, 0, dz);
                if (corner ? !level.getBlockState(low).is(bricks) : (dx != 0 || dz != 0) && !level.isEmptyBlock(low)) return false;
                // no andar do meio: pedra arcana nos cantos, o resto vazio
                BlockPos mid = floor.offset(dx, 1, dz);
                if (corner ? !level.getBlockState(mid).is(stone) : !level.isEmptyBlock(mid)) return false;
                // no andar da matriz: só ela
                BlockPos high = pos.offset(dx, 0, dz);
                if ((dx != 0 || dz != 0) && !level.isEmptyBlock(high)) return false;
            }
        }
        return true;
    }

    /** O {@code replaceInfusionAltar}: a varinha paga vinte e cinco de cada primário e os cantos viram pilares. */
    private boolean raiseAltar(Level level, BlockPos pos, ItemStack wand, Player player) {
        if (!fitsAltar(level, pos)) return false;
        BlockPos floor = pos.below(2);
        AspectList cost = new AspectList();
        for (Aspect primal : net.thaumcraft.api.aspects.Aspects.primals()) cost.add(primal, 25);
        if (!(wand.getItem() instanceof net.thaumcraft.item.WandItem) || !net.thaumcraft.item.WandItem.consume(wand, cost, true, player)) {
            return false;
        }
        // a orientação de cada pilar, pelo canto: a do original
        int[][] corners = {{-1, -1, 2}, {-1, 1, 3}, {1, -1, 4}, {1, 1, 5}};
        for (int[] corner : corners) {
            BlockPos base = floor.offset(corner[0], 0, corner[1]);
            level.setBlock(base.above(), TCBlocks.INFUSION_PILLAR_TOP.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
            level.setBlock(base, TCBlocks.INFUSION_PILLAR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
            if (level.getBlockEntity(base) instanceof InfusionPillarBlockEntity pillar) {
                pillar.setOrientation((byte) corner[2]);
                level.sendBlockUpdated(base, pillar.getBlockState(), pillar.getBlockState(), 3);
            }
            // o evento de bloco um do original: brilho roxo e o pó dos tijolos, nas duas metades
            if (level instanceof ServerLevel server) {
                for (BlockPos at : new BlockPos[]{base, base.above()}) {
                    TCNetwork.blockSparkle(server, at, 0xB680FF);
                    server.levelEvent(2001, at, net.minecraft.world.level.block.Block.getId(
                            net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState()));
                }
            }
        }
        this.active = true;
        this.sync();
        level.playSound(null, pos, TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    /** O {@code craftingStart}: junta o que está nos pedestais e procura a receita, em silêncio se não achar. */
    private void craftingStart(Player player) {
        Level level = this.level;
        BlockPos pos = this.worldPosition;
        if (!validLocation(level, pos)) {
            this.active = false;
            this.sync();
            return;
        }
        this.getSurroundings();
        this.recipeInput = ItemStack.EMPTY;
        if (level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity centre && !centre.held().isEmpty()) {
            this.recipeInput = centre.held().copy();
        }
        if (this.recipeInput.isEmpty()) return;
        List<ItemStack> components = new ArrayList<>();
        for (BlockPos at : this.pedestals) {
            if (level.getBlockEntity(at) instanceof PedestalBlockEntity ped && !ped.held().isEmpty()) components.add(ped.held().copy());
        }
        if (components.isEmpty()) return;
        InfusionRecipe recipe = InfusionRecipes.find(this.recipeInput, components, player);
        if (recipe != null) {
            this.recipeType = 0;
            this.recipeIngredients.clear();
            this.recipeIngredients.addAll(recipe.components());
            this.recipeOutput = recipe.resultFor(this.recipeInput);
            this.recipeEnchantment = null;
            this.recipeInstability = recipe.instability();
            this.recipeEssentia = recipe.essentia().copy();
            this.recipePlayer = player.getName().getString();
        } else {
            InfusionEnchantmentRecipe recipe2 = InfusionEnchantmentRecipe.find(components, this.recipeInput, level, player);
            if (recipe2 == null) return;
            this.recipeType = 1;
            this.recipeIngredients.clear();
            this.recipeIngredients.addAll(recipe2.components());
            this.recipeOutput = ItemStack.EMPTY;
            this.recipeEnchantment = recipe2.enchantment();
            this.recipeInstability = recipe2.instabilityFor(this.recipeInput);
            this.recipeEssentia = recipe2.essentiaFor(this.recipeInput, level);
            this.recipeXP = recipe2.xp(this.recipeInput, level);
        }
        this.instability = this.symmetry + this.recipeInstability;
        this.crafting = true;
        level.playSound(null, pos, TCSounds.CRAFT_START.value(), SoundSource.BLOCKS, 0.5f, 1.0f);
        this.sync();
    }

    /** A coisa do meio ainda é a mesma do começo? O {@code areItemStacksEqualForCrafting}, sem olhar o desgaste. */
    private boolean sameInput(ItemStack held) {
        if (held.isEmpty() || this.recipeInput.isEmpty()) return false;
        ItemStack a = held.copyWithCount(1), b = this.recipeInput.copyWithCount(1);
        a.remove(DataComponents.DAMAGE);
        b.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(a, b);
    }

    /** O {@code craftCycle}, passo a passo como no original. */
    private void craftCycle(ServerLevel level, BlockPos pos) {
        var random = level.getRandom();
        boolean valid = level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity ped && this.sameInput(ped.held());
        if (!valid || this.instability > 0 && random.nextInt(500) <= this.instability) {
            switch (random.nextInt(21)) {
                case 0, 2, 10, 13 -> this.inEvEjectItem(level, 0);
                case 1, 11 -> this.inEvEjectItem(level, 2);
                case 3, 8, 14 -> this.inEvZap(level, false);
                case 4, 15 -> this.inEvEjectItem(level, 5);
                case 5, 16 -> this.inEvHarm(level, false);
                case 6, 17 -> this.inEvEjectItem(level, 1);
                case 7 -> this.inEvEjectItem(level, 4);
                case 9 -> level.explode(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        1.5f + random.nextFloat(), Level.ExplosionInteraction.NONE);
                case 12 -> this.inEvZap(level, true);
                case 18 -> this.inEvHarm(level, true);
                case 19 -> this.inEvEjectItem(level, 3);
                default -> this.inEvWarp(level);
            }
            if (valid) return;
        }
        if (!valid) {
            this.instability = 0;
            this.crafting = false;
            this.recipeEssentia = new AspectList();
            this.sync();
            level.playSound(null, pos, TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
            return;
        }
        if (this.recipeType == 1 && this.recipeXP > 0) {
            List<Player> targets = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(10.0));
            if (!targets.isEmpty()) {
                for (Player target : targets) {
                    if (target.experienceLevel <= 0) continue;
                    target.giveExperienceLevels(-1);
                    this.recipeXP--;
                    target.hurtServer(level, level.damageSources().magic(), random.nextInt(2));
                    TCNetwork.infusionSource(level, pos, 0, 0, 0, target.getId());
                    level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH,
                            target.getSoundSource(), 1.0f, 2.0f + random.nextFloat() * 0.4f);
                    this.countDelay = 20;
                    return;
                }
                List<Aspect> ingEss = this.recipeEssentia.getAspects();
                if (!ingEss.isEmpty() && random.nextInt(3) == 0) {
                    this.recipeEssentia.add(ingEss.get(random.nextInt(ingEss.size())), 1);
                    if (random.nextInt(Math.max(1, 50 - this.recipeInstability * 2)) == 0) this.instability++;
                    if (this.instability > 25) this.instability = 25;
                    this.sync();
                }
            }
            return;
        }
        if (this.recipeType == 1 && this.recipeXP == 0) this.countDelay = 10;
        if (this.recipeEssentia.visSize() > 0) {
            for (Aspect aspect : this.recipeEssentia.getAspects()) {
                if (this.recipeEssentia.getAmount(aspect) <= 0) continue;
                if (EssentiaSources.drain(this, aspect, null, 12)) {
                    this.recipeEssentia.reduce(aspect, 1);
                    this.sync();
                    return;
                }
                if (random.nextInt(Math.max(1, 100 - this.recipeInstability * 3)) == 0) this.instability++;
                if (this.instability > 25) this.instability = 25;
                this.sync();
            }
            this.checkSurroundings = true;
        } else if (this.recipeIngredients.isEmpty()) {
            this.instability = 0;
            this.crafting = false;
            this.craftingFinish(level, pos);
            this.recipeOutput = ItemStack.EMPTY;
            this.recipeEnchantment = null;
            this.sync();
        } else {
            for (int a = 0; a < this.recipeIngredients.size(); a++) {
                Ingredient wanted = this.recipeIngredients.get(a);
                for (BlockPos cc : this.pedestals) {
                    if (!(level.getBlockEntity(cc) instanceof PedestalBlockEntity ped) || ped.held().isEmpty() || !wanted.test(ped.held())) continue;
                    if (this.itemCount == 0) {
                        this.itemCount = 5;
                        TCNetwork.infusionSource(level, pos, pos.getX() - cc.getX(), pos.getY() - cc.getY(), pos.getZ() - cc.getZ(), 0);
                    } else if (this.itemCount-- <= 1) {
                        var remainder = ped.held().getItem().getCraftingRemainder();
                        ped.hold(remainder == null ? ItemStack.EMPTY : remainder.create());
                        this.recipeIngredients.remove(a);
                    }
                    return;
                }
                List<Aspect> ingEss = this.recipeEssentia.getAspects();
                if (!ingEss.isEmpty() && random.nextInt(1 + a) == 0) {
                    this.recipeEssentia.add(ingEss.get(random.nextInt(ingEss.size())), 1);
                    if (random.nextInt(Math.max(1, 50 - this.recipeInstability * 2)) == 0) this.instability++;
                    if (this.instability > 25) this.instability = 25;
                    this.sync();
                }
            }
        }
    }

    /** Os raios: dano mágico de quatro a sete em uma criatura a até dez blocos (ou em todas). */
    private void inEvZap(ServerLevel level, boolean all) {
        BlockPos pos = this.worldPosition;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(10.0))) {
            TCNetwork.blockZap(level, pos, Vec3.atCenterOf(pos),
                    new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2.0f, target.getZ()));
            target.hurtServer(level, level.damageSources().magic(), 4 + level.getRandom().nextInt(4));
            if (!all) break;
        }
    }

    /** A mácula do fluxo (seis segundos) ou o cansaço de vis (dois minutos), em uma criatura perto ou em todas. */
    private void inEvHarm(ServerLevel level, boolean all) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.worldPosition).inflate(10.0))) {
            if (level.getRandom().nextBoolean()) {
                target.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 120, 0, false, true));
            } else {
                net.thaumcraft.research.Incurable.add(target, new MobEffectInstance(TCEffects.VIS_EXHAUST, 2400, 0, true, true));
            }
            if (!all) break;
        }
    }

    /** A distorção: num jogador perto, um em quatro de um ponto que gruda; senão, de um a cinco temporários. */
    private void inEvWarp(ServerLevel level) {
        List<Player> targets = level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(10.0));
        if (targets.isEmpty()) return;
        Player target = targets.get(level.getRandom().nextInt(targets.size()));
        if (level.getRandom().nextFloat() < 0.25f) Warp.addSticky(target, 1);
        else Warp.add(target, 1 + level.getRandom().nextInt(5), true);
    }

    /**
     * Um ingrediente perdido, de um pedestal qualquer que tenha: 0 cai no chão; 1 cai e deixa gosma de fluxo; 2 cai e
     * deixa gás de fluxo; 3 some com gosma; 4 some com gás; 5 cai com uma explosão.
     */
    private void inEvEjectItem(ServerLevel level, int type) {
        for (int q = 0; q < 50 && !this.pedestals.isEmpty(); q++) {
            BlockPos cc = this.pedestals.get(level.getRandom().nextInt(this.pedestals.size()));
            if (!(level.getBlockEntity(cc) instanceof PedestalBlockEntity ped) || ped.held().isEmpty()) continue;
            if (type >= 3 && type != 5) {
                ped.hold(ItemStack.EMPTY);
            } else {
                Containers.dropContents(level, cc, ped);
                ped.hold(ItemStack.EMPTY);
            }
            if (type == 1 || type == 3) {
                level.setBlock(cc.above(), TCBlocks.FLUX_GOO.defaultBlockState(), 3);
                level.playSound(null, cc, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.3f, 1.0f);
            } else if (type == 2 || type == 4) {
                level.setBlock(cc.above(), TCBlocks.FLUX_GAS.defaultBlockState(), 3);
                level.playSound(null, cc, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.0f);
            } else if (type == 5) {
                level.explode(null, cc.getX() + 0.5f, cc.getY() + 0.5f, cc.getZ() + 0.5f, 1.0f, Level.ExplosionInteraction.NONE);
            }
            level.blockEvent(cc, level.getBlockState(cc).getBlock(), 11, 0);
            TCNetwork.blockZap(level, this.worldPosition, Vec3.atCenterOf(this.worldPosition),
                    new Vec3(cc.getX() + 0.5f, cc.getY() + 1.5f, cc.getZ() + 0.5f));
            return;
        }
    }

    /** O {@code craftingFinish}: a coisa nova no pedestal do meio (ou o encantamento um nível acima), e o brilho. */
    private void craftingFinish(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity ped)) return;
        if (this.recipeType == 1 && this.recipeEnchantment != null) {
            Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(this.recipeEnchantment).orElse(null);
            ItemStack temp = ped.held().copy();
            if (holder != null) EnchantmentHelper.updateEnchantments(temp, e -> e.set(holder, e.getLevel(holder) + 1));
            ped.hold(temp);
        } else if (!this.recipeOutput.isEmpty()) {
            ped.hold(this.recipeOutput.copy());
        }
        if (this.recipePlayer != null) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayerByName(this.recipePlayer);
            if (p != null) ped.held().onCraftedBy(p, ped.held().getCount());
        }
        this.recipeEssentia = new AspectList();
        this.sync();
        level.blockEvent(pos.below(2), level.getBlockState(pos.below(2)).getBlock(), 12, 0);
    }

    /**
     * O {@code getSurroundings}: os pedestais (a até oito blocos de lado e até dez abaixo, o primeiro de cada coluna) e
     * os estabilizadores (cabeças e o que implementa o {@code IInfusionStabiliser}) a até doze, e a conta da simetria.
     */
    private void getSurroundings() {
        Level level = this.level;
        if (level == null) return;
        BlockPos pos = this.worldPosition;
        List<BlockPos> stuff = new ArrayList<>();
        this.pedestals.clear();
        for (int xx = -12; xx <= 12; xx++) {
            for (int zz = -12; zz <= 12; zz++) {
                boolean skip = false;
                for (int yy = -5; yy <= 10; yy++) {
                    if (xx == 0 && zz == 0) continue;
                    BlockPos at = new BlockPos(pos.getX() + xx, pos.getY() - yy, pos.getZ() + zz);
                    if (!level.isLoaded(at)) continue;
                    if (!skip && yy > 0 && Math.abs(xx) <= 8 && Math.abs(zz) <= 8 && level.getBlockEntity(at) instanceof PedestalBlockEntity) {
                        this.pedestals.add(at);
                        skip = true;
                    } else if (level.getBlockState(at).is(INFUSION_STABILIZERS)) {
                        stuff.add(at);
                    }
                }
            }
        }
        this.symmetry = 0;
        for (BlockPos cc : this.pedestals) {
            boolean items = false;
            if (level.getBlockEntity(cc) instanceof PedestalBlockEntity ped) {
                this.symmetry += 2;
                if (!ped.held().isEmpty()) {
                    this.symmetry++;
                    items = true;
                }
            }
            BlockPos mirror = new BlockPos(pos.getX() * 2 - cc.getX(), cc.getY(), pos.getZ() * 2 - cc.getZ());
            if (level.getBlockEntity(mirror) instanceof PedestalBlockEntity twin) {
                this.symmetry -= 2;
                if (!twin.held().isEmpty() && items) this.symmetry--;
            }
        }
        float sym = 0.0f;
        for (BlockPos cc : stuff) {
            if (level.getBlockState(cc).is(INFUSION_STABILIZERS)) sym += 0.1f;
            BlockPos mirror = new BlockPos(pos.getX() * 2 - cc.getX(), cc.getY(), pos.getZ() * 2 - cc.getZ());
            if (level.getBlockState(mirror).is(INFUSION_STABILIZERS)) sym -= 0.2f;
        }
        this.symmetry = (int) (this.symmetry + sym);
    }

    /** O que estabiliza a infusão: as cabeças (o {@code Blocks.skull}) e o que implementa o {@code IInfusionStabiliser}. */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> INFUSION_STABILIZERS =
            net.minecraft.tags.TagKey.create(Registries.BLOCK, net.thaumcraft.Thaumcraft.id("infusion_stabilizers"));

    /** O {@code PacketFXInfusionSource} chegando: o pedestal (ou a criatura) de que se puxa, por 60 ou 15 tiques. */
    public void addSourceFx(int dx, int dy, int dz, int entity) {
        if (this.level == null) return;
        BlockPos loc = this.worldPosition.offset(-dx, -dy, -dz);
        String key = loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + entity;
        int ticks = this.level.getBlockEntity(loc) instanceof PedestalBlockEntity ? 60 : 15;
        SourceFx fx = this.sourceFX.get(key);
        if (fx != null) fx.ticks = ticks;
        else this.sourceFX.put(key, new SourceFx(loc, ticks, entity));
    }

    /**
     * O {@code doEffects}, do lado de quem vê: os sons, as runas subindo do pedestal, o {@code startUp} (a matriz se
     * erguendo quando liga), as migalhas de cada fonte e, com instabilidade, os raios em volta.
     */
    private void doEffects(Level level, BlockPos pos) {
        var random = level.getRandom();
        if (this.crafting) {
            if (this.craftCount == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.INFUSER_START.value(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            } else if (this.craftCount % 65 == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.INFUSER.value(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            this.craftCount++;
            if (clientEffects != null) {
                clientEffects.runes(level, pos.below(2), 0.5f + random.nextFloat() * 0.2f, 0.1f, 0.7f + random.nextFloat() * 0.3f);
            }
        } else if (this.craftCount > 0) {
            this.craftCount = Math.clamp(this.craftCount - 2, 0, 50);
        }
        if (this.active && this.startUp != 1.0f) {
            if (this.startUp < 1.0f) this.startUp += Math.max(this.startUp / 10.0f, 0.001f);
            if (this.startUp > 0.999) this.startUp = 1.0f;
        }
        if (!this.active && this.startUp > 0.0f) {
            this.startUp -= this.startUp / 10.0f;
            if (this.startUp < 0.001) this.startUp = 0.0f;
        }
        for (String key : this.sourceFX.keySet().toArray(new String[0])) {
            SourceFx fx = this.sourceFX.get(key);
            if (fx.ticks <= 0) {
                this.sourceFX.remove(key);
                continue;
            }
            if (fx.loc.equals(pos)) {
                Entity from = level.getEntity(fx.entity);
                if (from != null && clientEffects != null) clientEffects.experience(level, from, pos);
            } else if (level.getBlockEntity(fx.loc) instanceof PedestalBlockEntity ped) {
                if (!ped.held().isEmpty() && clientEffects != null) clientEffects.pedestal(level, fx.loc, pos, ped.held());
            } else {
                fx.ticks = 0;
            }
            fx.ticks--;
        }
        if (this.crafting && this.instability > 0 && random.nextInt(200) <= this.instability && clientEffects != null) {
            clientEffects.bolt(new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f),
                    new Vec3(pos.getX() + 0.5f + (random.nextFloat() - random.nextFloat()) * 2.0f,
                            pos.getY() + 0.5f + (random.nextFloat() - random.nextFloat()) * 2.0f,
                            pos.getZ() + 0.5f + (random.nextFloat() - random.nextFloat()) * 2.0f));
        }
    }

    // ---- o que se mostra ----

    public boolean isActive() {
        return this.active;
    }

    public boolean isCrafting() {
        return this.crafting;
    }

    public int instability() {
        return this.instability;
    }

    public int symmetry() {
        return this.symmetry;
    }

    /** O {@code getAspects}: a essência que a infusão ainda cobra (é o que o thaumômetro e os óculos mostram). */
    public AspectList getAspects() {
        return this.recipeEssentia;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.active = input.getBooleanOr("active", false);
        this.crafting = input.getBooleanOr("crafting", false);
        this.instability = input.getIntOr("instability", 0);
        this.recipeEssentia = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.recipeIngredients.clear();
        this.recipeIngredients.addAll(input.read("recipein", Ingredient.CODEC.listOf()).orElseGet(List::of));
        this.recipeOutput = input.read("recipeout", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.recipeEnchantment = input.read("recipeenchant", ResourceKey.codec(Registries.ENCHANTMENT)).orElse(null);
        this.recipeInput = input.read("recipeinput", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.recipeInstability = input.getIntOr("recipeinst", 0);
        this.recipeType = input.getIntOr("recipetype", 0);
        this.recipeXP = input.getIntOr("recipexp", 0);
        String player = input.getStringOr("recipeplayer", "");
        this.recipePlayer = player.isEmpty() ? null : player;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("active", this.active);
        output.putBoolean("crafting", this.crafting);
        output.putInt("instability", this.instability);
        output.store("aspects", AspectList.CODEC, this.recipeEssentia);
        if (!this.recipeIngredients.isEmpty()) output.store("recipein", Ingredient.CODEC.listOf(), List.copyOf(this.recipeIngredients));
        if (!this.recipeOutput.isEmpty()) output.store("recipeout", ItemStack.CODEC, this.recipeOutput);
        if (this.recipeEnchantment != null) output.store("recipeenchant", ResourceKey.codec(Registries.ENCHANTMENT), this.recipeEnchantment);
        if (!this.recipeInput.isEmpty()) output.store("recipeinput", ItemStack.CODEC, this.recipeInput);
        output.putInt("recipeinst", this.recipeInstability);
        output.putInt("recipetype", this.recipeType);
        output.putInt("recipexp", this.recipeXP);
        output.putString("recipeplayer", this.recipePlayer == null ? "" : this.recipePlayer);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
