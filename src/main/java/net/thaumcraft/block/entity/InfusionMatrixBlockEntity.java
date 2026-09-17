package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaSources;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A matriz rúnica: onde a infusão acontece.
 *
 * <p>A construção é a do diagrama do altar do próprio original — o {@code InfusionAltar} do
 * {@code ConfigRecipes}. A matriz fica no ar; dois blocos abaixo dela vai um pedestal arcano, e nos
 * quatro cantos desse pedestal vão quatro blocos de pedra arcana. Sem isso, a matriz não liga.
 *
 * <p>Ligada, ela recolhe os pedestais num raio de oito blocos e, ao toque da varinha, procura uma receita
 * que case com o que está no pedestal do meio e com o que está nos de fora. Achando, ela começa: de dez
 * em dez tiques puxa um ponto de essência de algum jarro ao alcance, e quando a essência acaba, consome
 * um ingrediente de cada pedestal. Terminado tudo, a coisa nova aparece no pedestal do meio.
 *
 * <p>O que torna a infusão perigosa é a <strong>instabilidade</strong>. Ela é a soma de duas coisas: a
 * instabilidade natural da receita e a falta de <strong>simetria</strong> da construção. Cada pedestal
 * conta dois pontos, e mais um se tiver coisa em cima; o pedestal espelhado do outro lado da matriz
 * desconta a mesma coisa. Uma construção perfeitamente simétrica zera a conta — e é por isso que, no
 * Thaumcraft, as salas de infusão são desenhadas como mandalas.
 *
 * <p>A cada dez tiques, com um em quinhentos de chance por ponto de instabilidade, alguma coisa dá
 * errado: um raio, um susto, um ingrediente cuspido para longe, ou uma explosão.
 */
public class InfusionMatrixBlockEntity extends BlockEntity {
    /** Até onde a matriz enxerga pedestal. */
    private static final int PEDESTAL_REACH = 8;
    /** Até onde ela bebe essência, os doze blocos do original. */
    private static final int ESSENTIA_REACH = 12;
    /** De quantos em quantos tiques ela dá um passo. */
    private static final int STEP = 10;
    /** O teto da instabilidade, como no original. */
    private static final int MAX_INSTABILITY = 25;

    private boolean active;
    private boolean crafting;
    private int symmetry;
    private int instability;
    private int recipeInstability;
    private int count;
    private AspectList owed = new AspectList();
    private final List<ItemStack> owedItems = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private ItemStack middle = ItemStack.EMPTY;

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFUSION_MATRIX, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        matrix.count++;
        if (level.isClientSide()) {
            if (matrix.crafting) matrix.sparkle(level, pos);
            return;
        }
        // de tempos em tempos ela confere se a construção continua de pé
        if (matrix.count % (matrix.crafting ? 20 : 100) == 0 && !validLocation(level, pos)) {
            matrix.stop();
            return;
        }
        if (matrix.active && matrix.crafting && matrix.count % STEP == 0) {
            matrix.step(level, pos);
        }
    }

    /**
     * A construção está de pé?
     *
     * <p>Pedestal dois blocos abaixo e pedra arcana nos quatro cantos dele, que é o que o diagrama do
     * altar do original desenha.
     */
    public static boolean validLocation(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity)) return false;
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                if (!level.getBlockState(pos.offset(dx, -2, dz)).is(TCBlocks.BUILDING.get("arcane_stone"))) return false;
            }
        }
        return true;
    }

    /** O toque da varinha: liga a matriz, ou começa a infusão se ela já estiver ligada. */
    public boolean poke(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return true;
        if (!validLocation(level, pos)) {
            player.sendOverlayMessage(Component.translatable("tc.infusion.badplace"));
            this.stop();
            return false;
        }
        if (!this.active) {
            this.active = true;
            level.playSound(null, pos, TCSounds.WAND.value(), SoundSource.BLOCKS, 0.6f, 1.2f);
            this.sync();
            return true;
        }
        if (!this.crafting) return this.start(level, pos, player);
        return true;
    }

    /** Procura a receita e começa. */
    private boolean start(Level level, BlockPos pos, Player player) {
        this.measure(level, pos);

        PedestalBlockEntity centre = level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity found
                ? found : null;
        if (centre == null || centre.held().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("tc.infusion.nocentre"));
            return false;
        }

        List<BlockPos> around = this.pedestals(level, pos);
        List<ItemStack> parts = new ArrayList<>();
        for (BlockPos at : around) {
            if (level.getBlockEntity(at) instanceof PedestalBlockEntity pedestal && !pedestal.held().isEmpty()) {
                parts.add(pedestal.held().copy());
            }
        }

        InfusionRecipe recipe = InfusionRecipes.find(centre.held(), parts);
        if (recipe == null) {
            player.sendOverlayMessage(Component.translatable("tc.infusion.norecipe"));
            return false;
        }
        if (!net.thaumcraft.research.ResearchManager.knows(player, recipe.research())) {
            player.sendOverlayMessage(Component.translatable("tc.infusion.unknown"));
            return false;
        }

        this.middle = centre.held().copy();
        this.result = recipe.result().copy();
        this.recipeInstability = recipe.instability();
        this.instability = Math.max(0, this.symmetry) + this.recipeInstability;
        this.owed = recipe.essentia().copy();
        this.owedItems.clear();
        this.owedItems.addAll(parts);
        this.crafting = true;
        level.playSound(null, pos, TCSounds.CRAFT_START.value(), SoundSource.BLOCKS, 0.5f, 1.0f);
        this.sync();
        return true;
    }

    /**
     * Um passo da infusão.
     *
     * <p>Na ordem do original: primeiro o azar, depois a essência, depois os ingredientes, e por fim o
     * resultado.
     */
    private void step(Level level, BlockPos pos) {
        PedestalBlockEntity centre = level.getBlockEntity(pos.below(2)) instanceof PedestalBlockEntity found
                ? found : null;
        // tiraram a coisa do meio no meio do serviço: a infusão desanda
        if (centre == null || !ItemStack.isSameItemSameComponents(centre.held(), this.middle)) {
            this.misfire(level, pos);
            this.stop();
            return;
        }

        if (this.instability > 0 && level.getRandom().nextInt(500) <= this.instability) {
            this.misfire(level, pos);
        }

        if (!this.owed.isEmpty()) {
            for (Aspect aspect : this.owed.getAspects()) {
                if (this.owed.getAmount(aspect) <= 0) continue;
                BlockPos from = EssentiaSources.drain(level, pos, aspect, ESSENTIA_REACH);
                if (from != null) {
                    this.owed.remove(aspect, 1);
                    this.thread(level, pos, from, aspect.color());
                    this.sync();
                    return;
                }
                // faltou essência: a magia escapa e a instabilidade sobe
                this.rattle(level, Math.max(1, 100 - this.recipeInstability * 3));
                this.sync();
                return;
            }
        }

        if (!this.owedItems.isEmpty()) {
            for (BlockPos at : this.pedestals(level, pos)) {
                if (!(level.getBlockEntity(at) instanceof PedestalBlockEntity pedestal)) continue;
                ItemStack held = pedestal.held();
                if (held.isEmpty()) continue;
                int slot = this.indexOf(held);
                if (slot < 0) continue;
                this.owedItems.remove(slot);
                pedestal.hold(ItemStack.EMPTY);
                this.thread(level, pos, at, 0xB09CD9);
                level.playSound(null, pos, TCSounds.CRAFT_START.value(), SoundSource.BLOCKS, 0.3f, 1.6f);
                this.sync();
                return;
            }
            // os pedestais não têm mais o que a receita pedia
            this.rattle(level, Math.max(1, 50 - this.recipeInstability * 2));
            this.sync();
            return;
        }

        // acabou: a coisa nova toma o lugar da velha no pedestal do meio
        centre.hold(this.result.copy());
        level.playSound(null, pos, TCSounds.LEARN.value(), SoundSource.BLOCKS, 0.8f, 1.0f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    60, 0.5, 0.5, 0.5, 0.15);
        }
        this.crafting = false;
        this.instability = 0;
        this.result = ItemStack.EMPTY;
        this.middle = ItemStack.EMPTY;
        this.owedItems.clear();
        this.sync();
    }

    /** Onde nesta lista está uma coisa igual a esta? */
    private int indexOf(ItemStack wanted) {
        for (int slot = 0; slot < this.owedItems.size(); slot++) {
            if (ItemStack.isSameItem(this.owedItems.get(slot), wanted)) return slot;
        }
        return -1;
    }

    /** Um em {@code bound} de subir a instabilidade em um. */
    private void rattle(Level level, int bound) {
        if (level.getRandom().nextInt(bound) != 0) return;
        this.instability = Math.min(MAX_INSTABILITY, this.instability + 1);
    }

    /**
     * Alguma coisa deu errado.
     *
     * <p>O original sorteia entre vinte e um azares; aqui são os quatro que dá para fazer sem as peças das
     * fatias seguintes: cuspir um ingrediente, um raio, um susto em quem estiver perto, e a explosão. Os
     * outros — mácula, criaturas do vazio, distorção — chegam com a fatia oito.
     */
    private void misfire(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return;
        switch (level.getRandom().nextInt(8)) {
            case 0, 1, 2 -> this.spit(server, pos);
            case 3, 4 -> this.zap(server, pos);
            case 5, 6 -> this.scare(server, pos);
            default -> {
                server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1.5f + level.getRandom().nextFloat(), Level.ExplosionInteraction.NONE);
            }
        }
    }

    /** Um pedestal perde o que tinha: a coisa sai voando. */
    private void spit(ServerLevel level, BlockPos pos) {
        List<BlockPos> around = this.pedestals(level, pos);
        if (around.isEmpty()) return;
        BlockPos at = around.get(level.getRandom().nextInt(around.size()));
        if (!(level.getBlockEntity(at) instanceof PedestalBlockEntity pedestal)) return;
        ItemStack held = pedestal.held();
        if (held.isEmpty()) return;
        pedestal.hold(ItemStack.EMPTY);
        ItemEntity thrown = new ItemEntity(level, at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 0.5, held);
        thrown.setDeltaMovement(
                (level.getRandom().nextDouble() - 0.5) * 0.6,
                0.35,
                (level.getRandom().nextDouble() - 0.5) * 0.6);
        level.addFreshEntity(thrown);
        level.playSound(null, pos, TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 0.7f, 1.0f);
    }

    /** Um raio cai perto da matriz. */
    private void zap(ServerLevel level, BlockPos pos) {
        BlockPos at = pos.offset(level.getRandom().nextInt(7) - 3, -2, level.getRandom().nextInt(7) - 3);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 30, 0.3, 0.8, 0.3, 0.3);
        level.playSound(null, at, TCSounds.ZAP.value(), SoundSource.BLOCKS, 0.8f, 1.0f);
        for (var living : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                new net.minecraft.world.phys.AABB(at).inflate(2.0))) {
            living.hurtServer(level, level.damageSources().magic(), 3.0f);
        }
    }

    /** Quem estiver por perto leva um susto e uma dor de cabeça. */
    private void scare(ServerLevel level, BlockPos pos) {
        for (Player nearby : level.players()) {
            if (nearby.distanceToSqr(Vec3.atCenterOf(pos)) > 100.0) continue;
            nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.NAUSEA, 200, 0));
            nearby.hurtServer(level, level.damageSources().magic(), 2.0f);
        }
        level.playSound(null, pos, TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 1.0f, 0.7f);
    }

    /** O fio de luz que liga a matriz a de onde a coisa veio. */
    private void thread(Level level, BlockPos pos, BlockPos from, int colour) {
        if (!(level instanceof ServerLevel server)) return;
        Vec3 here = Vec3.atCenterOf(pos);
        Vec3 there = Vec3.atCenterOf(from);
        int steps = (int) Math.max(4, here.distanceTo(there) * 3);
        for (int step = 0; step <= steps; step++) {
            Vec3 at = there.lerp(here, step / (double) steps);
            server.sendParticles(
                    new net.minecraft.core.particles.DustParticleOptions(0xFF000000 | colour, 0.8f),
                    at.x, at.y, at.z, 1, 0.04, 0.04, 0.04, 0.0);
        }
    }

    /** As faíscas que a matriz solta enquanto trabalha. */
    private void sparkle(Level level, BlockPos pos) {
        var random = level.getRandom();
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0,
                    pos.getY() + 1.0 + random.nextDouble(),
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0,
                    0.0, -0.4, 0.0);
        }
    }

    /**
     * A conta da simetria.
     *
     * <p>Cada pedestal vale dois, e mais um se tiver coisa em cima; o pedestal espelhado do outro lado da
     * matriz desconta o mesmo. Construção simétrica dá zero, que é o sonho de todo taumaturgo.
     */
    private void measure(Level level, BlockPos pos) {
        this.symmetry = 0;
        for (BlockPos at : this.pedestals(level, pos)) {
            boolean loaded = level.getBlockEntity(at) instanceof PedestalBlockEntity pedestal
                    && !pedestal.held().isEmpty();
            this.symmetry += 2;
            if (loaded) this.symmetry++;

            BlockPos mirror = new BlockPos(
                    pos.getX() * 2 - at.getX(), at.getY(), pos.getZ() * 2 - at.getZ());
            if (level.getBlockEntity(mirror) instanceof PedestalBlockEntity twin) {
                this.symmetry -= 2;
                if (loaded && !twin.held().isEmpty()) this.symmetry--;
            }
        }
    }

    /** Os pedestais de fora que a matriz enxerga — o do meio não conta. */
    private List<BlockPos> pedestals(Level level, BlockPos pos) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos centre = pos.below(2);
        for (int x = -PEDESTAL_REACH; x <= PEDESTAL_REACH; x++) {
            for (int z = -PEDESTAL_REACH; z <= PEDESTAL_REACH; z++) {
                for (int y = -5; y <= 1; y++) {
                    BlockPos at = pos.offset(x, y, z);
                    if (at.equals(centre)) continue;
                    if (!level.isLoaded(at)) continue;
                    if (level.getBlockEntity(at) instanceof PedestalBlockEntity) found.add(at);
                }
            }
        }
        return found;
    }

    private void stop() {
        this.active = false;
        this.crafting = false;
        this.instability = 0;
        this.owed = new AspectList();
        this.owedItems.clear();
        this.result = ItemStack.EMPTY;
        this.middle = ItemStack.EMPTY;
        this.sync();
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

    @Nullable
    public Aspect nextAspect() {
        for (Aspect aspect : this.owed.getAspects()) {
            if (this.owed.getAmount(aspect) > 0) return aspect;
        }
        return null;
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
        this.symmetry = input.getIntOr("symmetry", 0);
        this.instability = input.getIntOr("instability", 0);
        this.recipeInstability = input.getIntOr("recipe_instability", 0);
        this.owed = input.read("owed", AspectList.CODEC).orElseGet(AspectList::new);
        this.result = input.read("result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.middle = input.read("middle", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.owedItems.clear();
        this.owedItems.addAll(input.read("owed_items", ItemStack.CODEC.listOf()).orElseGet(List::of));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("active", this.active);
        output.putBoolean("crafting", this.crafting);
        output.putInt("symmetry", this.symmetry);
        output.putInt("instability", this.instability);
        output.putInt("recipe_instability", this.recipeInstability);
        output.store("owed", AspectList.CODEC, this.owed);
        if (!this.result.isEmpty()) output.store("result", ItemStack.CODEC, this.result);
        if (!this.middle.isEmpty()) output.store("middle", ItemStack.CODEC, this.middle);
        output.store("owed_items", ItemStack.CODEC.listOf(), List.copyOf(this.owedItems));
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
