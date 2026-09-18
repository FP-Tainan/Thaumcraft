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
     * O {@code validLocation} do original: pedestal dois blocos abaixo e um pilar de infusão em cada canto dele.
     */
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
     * O toque da varinha: o {@code onWandRightClick} da {@code TileInfusionMatrix} e, se ela não quiser, o gatilho
     * três da varinha, o {@code createInfusionAltar}. Ligada e parada, começa a infusão; desligada com os pilares
     * de pé, liga; desligada sem eles, tenta erguer o altar.
     */
    public boolean poke(Level level, BlockPos pos, Player player, ItemStack wand) {
        if (level.isClientSide()) return true;
        if (this.active && !this.crafting) return this.start(level, pos, player);
        if (!this.active && validLocation(level, pos)) {
            this.active = true;
            this.sync();
            return true;
        }
        if (this.active) return true;
        if (raiseAltar(level, pos, wand)) return true;
        player.sendOverlayMessage(Component.translatable("tc.infusion.badplace"));
        return false;
    }

    /**
     * O {@code fitInfusionAltar} e o {@code replaceInfusionAltar} do {@code WandManager}: com o pedestal no
     * chão, tijolos de pedra arcana nos quatro cantos dele, pedra arcana em cima dos tijolos e o resto vazio, a
     * varinha paga vinte e cinco de cada primário e os cantos viram pilares.
     */
    private boolean raiseAltar(Level level, BlockPos pos, ItemStack wand) {
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
        net.thaumcraft.api.aspects.AspectList cost = new net.thaumcraft.api.aspects.AspectList();
        for (net.thaumcraft.api.aspects.Aspect primal : net.thaumcraft.api.aspects.Aspects.primals()) cost.add(primal, 25);
        if (!(wand.getItem() instanceof net.thaumcraft.item.WandItem) || !net.thaumcraft.item.WandItem.consume(wand, cost, true)) {
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
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                for (BlockPos at : new BlockPos[]{base, base.above()}) {
                    net.thaumcraft.net.TCNetwork.blockSparkle(server, at, 0xB680FF);
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
     * <p>O original sorteia entre vinte e um azares; aqui são cinco: cuspir um ingrediente, um raio, um
     * susto em quem estiver perto, a explosão, e a mácula brotando no chão em volta. Os que faltam —
     * criaturas do vazio, distorção da mente — chegam com as peças que faltam.
     */
    private void misfire(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return;
        switch (level.getRandom().nextInt(10)) {
            case 0, 1, 2 -> this.spit(server, pos);
            case 3, 4 -> this.zap(server, pos);
            case 5, 6 -> this.scare(server, pos);
            case 7, 8 -> this.taint(server, pos);
            default -> {
                server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1.5f + level.getRandom().nextFloat(), Level.ExplosionInteraction.NONE);
            }
        }
    }

    /**
     * A mácula brota no chão em volta.
     *
     * <p>É o azar mais feio da infusão no original, e o mais demorado de consertar: a magia que escapa
     * apodrece a terra, e dali ela se alastra sozinha. Só a Flor Etérea desfaz.
     */
    private void taint(ServerLevel level, BlockPos pos) {
        // longe o bastante para não comer o próprio altar, e perto o bastante para dar trabalho
        BlockPos seed = null;
        for (int tries = 0; tries < 16 && seed == null; tries++) {
            int away = 6 + level.getRandom().nextInt(5);
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            BlockPos at = pos.offset(
                    (int) Math.round(Math.cos(angle) * away),
                    -2 - level.getRandom().nextInt(3),
                    (int) Math.round(Math.sin(angle) * away));
            if (canRot(level, at)) seed = at;
        }
        if (seed == null) return;

        // a mácula chega em punhado, e não em bloco solto: sozinha ela nunca pegaria, porque a regra do
        // original pede vizinhos já maculados para ela avançar
        int planted = 0;
        for (int tries = 0; tries < 24 && planted < 5; tries++) {
            BlockPos at = seed.offset(
                    level.getRandom().nextInt(5) - 2, level.getRandom().nextInt(3) - 1,
                    level.getRandom().nextInt(5) - 2);
            if (!canRot(level, at)) continue;
            level.setBlockAndUpdate(at, TCBlocks.TAINT_SOIL.defaultBlockState());
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                    at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 8, 0.4, 0.2, 0.4, 0.02);
            planted++;
        }
        if (planted > 0) {
            level.playSound(null, seed, TCSounds.SPILL.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
        }
    }

    /** Aquele chão dá para apodrecer? Tem de ser bloco firme, com céu por cima, e fora do altar. */
    private boolean canRot(ServerLevel level, BlockPos at) {
        var there = level.getBlockState(at);
        if (there.isAir() || !there.isSolidRender()) return false;
        if (!level.getBlockState(at.above()).isAir()) return false;
        // nada do altar vira mácula: a construção não pode se desmanchar sozinha
        BlockPos matrix = this.getBlockPos();
        return Math.abs(at.getX() - matrix.getX()) > 2 || Math.abs(at.getZ() - matrix.getZ()) > 2;
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
