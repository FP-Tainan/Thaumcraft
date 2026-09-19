package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.TCBiomes;
import org.jetbrains.annotations.Nullable;

/**
 * As fibras da mácula: o {@code BlockTaintFibres} da 4.2.3.5, em cinco formas (o {@link #KIND}): 0 a película que
 * forra as faces dos blocos em volta, 1 e 2 o capim maculado (o 2 brilha), 3 o talo de esporos e 4 o talo com o esporo
 * solto em cima (que brilha mais).
 *
 * <p>Só vivem no bioma maculado: fora dele, ou (a película) cercadas só de mácula, somem no tique ao acaso. A cada tique
 * elas também maculam o bioma em volta, e sorteiam um bloco perto — dentro do bioma, ali nasce fibra; se não dá, com
 * dois vizinhos maculados, tronco, abóbora e cacto viram crosta, e com três, terra, areia e argila viram solo maculado.
 */
public class TaintFibreBlock extends Block {
    public static final MapCodec<TaintFibreBlock> CODEC = simpleCodec(TaintFibreBlock::new);
    public static final IntegerProperty KIND = IntegerProperty.create("kind", 0, 4);
    private static final VoxelShape PLANT = Block.box(3.2, 0, 3.2, 12.8, 12.8, 12.8);
    private static final VoxelShape[] FILM = {
            Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 15, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 1), Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 0, 1, 16, 16), Block.box(15, 0, 0, 16, 16, 16)};
    /** A ordem das direções do Forge ({@code ForgeDirection} 0 a 5). */
    public static final Direction[] FORGE = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public TaintFibreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(KIND, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    /** A luz: 8 no capim que brilha, 10 no talo com esporo. */
    public static int light(BlockState state) {
        int kind = state.getValue(KIND);
        return kind == 2 ? 8 : kind == 4 ? 10 : 0;
    }

    /** O {@code canPlaceBlockAt}: só se põe dentro do bioma maculado. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.getLevel().getBiome(context.getClickedPos()).is(TCBiomes.TAINTED_LAND)) return null;
        return this.defaultBlockState();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(KIND) != 0) return PLANT;
        for (int a = 0; a < 6; a++) {
            BlockPos at = pos.relative(FORGE[a]);
            if (level.getBlockState(at).isFaceSturdy(level, at, FORGE[a].getOpposite())) return FILM[a];
        }
        return FILM[0];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** O {@code onNeighborBlockChange}: cercada só de mácula (ou de ar), ela some. */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (onlyAdjacentToTaint(level, pos)) return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int md = state.getValue(KIND);
        taintBiomeSpread(level, pos, random, this);
        if (md == 0 && onlyAdjacentToTaint(level, pos) || !level.getBiome(pos).is(TCBiomes.TAINTED_LAND)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        BlockPos at = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        if (!level.getBiome(at).is(TCBiomes.TAINTED_LAND)) return;
        BlockState there = level.getBlockState(at);
        if (spreadFibres(level, at)) return;
        int adjacent = TaintBlock.adjacentTaint(level, at);
        if (adjacent >= 2 && (there.is(BlockTags.LOGS) || gourd(there) || there.is(Blocks.CACTUS))) {
            level.setBlockAndUpdate(at, TCBlocks.TAINT_CRUST.defaultBlockState());
            level.blockEvent(at, TCBlocks.TAINT_CRUST, 1, 0);
        }
        if (adjacent >= 3 && !there.isAir() && soil(there)) {
            level.setBlockAndUpdate(at, TCBlocks.TAINT_SOIL.defaultBlockState());
            level.blockEvent(at, TCBlocks.TAINT_SOIL, 1, 0);
        }
        if (md == 3 && random.nextInt(10) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlockAndUpdate(pos, state.setValue(KIND, 4));
            var spore = new net.thaumcraft.entity.taint.TaintSporeEntity(net.thaumcraft.registry.TCEntities.TAINT_SPORE, level);
            spore.snapTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(spore);
        } else if (md == 4 && level.getEntitiesOfClass(net.thaumcraft.entity.taint.TaintSporeEntity.class,
                new net.minecraft.world.phys.AABB(pos.above())).isEmpty()) {
            level.setBlockAndUpdate(pos, state.setValue(KIND, 3));
        }
    }

    /** O material {@code gourd} de então: abóbora e melancia. */
    private static boolean gourd(BlockState state) {
        return state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN) || state.is(Blocks.MELON);
    }

    /** Os materiais {@code sand}, {@code ground}, {@code grass} e {@code clay} de então. */
    private static boolean soil(BlockState state) {
        if (state.getBlock() instanceof TaintBlock) return false;
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY)
                || state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL);
    }

    /**
     * O {@code spreadFibres}: põe fibra aqui se o lugar está colado a um bloco firme, não está cercado só de mácula e é
     * ar, coisa que se substitui, flor ou folha. Nove em dez vezes (ou sem chão firme e ar em cima) é a película; na
     * outra, capim, capim que brilha ou talo de esporos.
     */
    public static boolean spreadFibres(ServerLevel level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        if (!adjacentToSolid(level, pos) || onlyAdjacentToTaint(level, pos) || !there.getFluidState().isEmpty()) return false;
        if (!(there.isAir() || there.canBeReplaced() || there.is(BlockTags.SMALL_FLOWERS) || there.is(BlockTags.LEAVES))) return false;
        if (there.is(TCBlocks.TAINT_FIBRES)) return false;
        RandomSource random = level.getRandom();
        int kind;
        if (random.nextInt(10) != 0 || !level.isEmptyBlock(pos.above()) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            kind = 0;
        } else if (random.nextInt(10) < 9) {
            kind = 1;
        } else if (random.nextInt(12) < 10) {
            kind = 2;
        } else {
            kind = 3;
        }
        level.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRES.defaultBlockState().setValue(KIND, kind));
        level.blockEvent(pos, TCBlocks.TAINT_FIBRES, 1, 0);
        return true;
    }

    /**
     * O {@code taintBiomeSpread}: uma coluna vizinha sorteada fora do bioma maculado vira bioma maculado, uma vez em mil
     * e só se este bloco tem dois vizinhos maculados.
     */
    public static void taintBiomeSpread(ServerLevel level, BlockPos pos, RandomSource rand, Block block) {
        int xx = rand.nextInt(3) - 1;
        int zz = rand.nextInt(3) - 1;
        BlockPos column = pos.offset(xx, 0, zz);
        if (!level.getBiome(column).is(TCBiomes.TAINTED_LAND) && rand.nextInt(TaintBlock.SPREAD_RATE * 5) == 0
                && TaintBlock.adjacentTaint(level, pos) >= 2) {
            BiomePainter.paint(level, column, TCBiomes.TAINTED_LAND);
            level.blockEvent(pos, block, 1, 0);
        }
    }

    /** O {@code isAdjacentToSolidBlock}: alguma das seis faces em volta é firme para cá. */
    public static boolean adjacentToSolid(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos at = pos.relative(dir);
            if (level.getBlockState(at).isFaceSturdy(level, at, dir.getOpposite())) return true;
        }
        return false;
    }

    /** O {@code isOnlyAdjacentToTaint}: tudo em volta é ar ou mácula. */
    public static boolean onlyAdjacentToTaint(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(dir));
            if (!state.isAir() && !TaintBlock.isTaint(state)) return false;
        }
        return true;
    }

    /** O {@code onEntityCollidedWithBlock}: o fluxo da mácula, muito raramente (1 em 1000 no jogador, 1 em 500 nos bichos). */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
            if (entity instanceof Player && level.getRandom().nextInt(1000) == 0) {
                living.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 80, 0));
            } else if (!(entity instanceof Player) && level.getRandom().nextInt(500) == 0) {
                living.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 160, 0));
            }
        }
    }

    /** O evento 1: o som de raízes. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (level.isClientSide()) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.ROOTS.value(), SoundSource.BLOCKS, 0.1f,
                        0.9f + level.getRandom().nextFloat() * 0.2f, false);
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }
}
