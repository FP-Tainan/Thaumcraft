package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

/**
 * Uma das três mudas do ofício: o {@code BlockWitchSapling} do Witchery.
 *
 * <p>Como a muda do jogo antigo, ela não cresce na primeira vez que o acaso bate nela: a primeira batida
 * <b>marca</b> a muda, e é a segunda que faz a árvore. Aqui a marca é o {@code stage}, como nas mudas de hoje.
 *
 * <p>A farinha de osso adianta uma batida, três vezes em quatro — é o que o {@code func_149852_a} do original diz.
 */
public class WitchSaplingBlock extends VegetationBlock implements BonemealableBlock {
    /** A caixa dela: o {@code setBlockBounds(0.1, 0, 0.1, 0.9, 0.8, 0.9)} do original. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    /** A marca de que o acaso já bateu uma vez, que é o {@code metadata & 8} do original. */
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty STAGE =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.STAGE;

    /** Três vezes em quatro a farinha de osso pega, como no original. */
    public static final float BONEMEAL_CHANCE = 0.75f;

    private final BiFunction<ServerLevel, BlockPos, Boolean> tree;

    public WitchSaplingBlock(Properties properties, BiFunction<ServerLevel, BlockPos, Boolean> tree) {
        super(properties);
        this.tree = tree;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        throw new UnsupportedOperationException("as mudas do ofício não vão em estrutura nem em comando");
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** O {@code updateTick}: com luz de nove para cima, uma vez em sete o acaso mexe com ela. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) return;
        if (random.nextInt(7) != 0) return;
        this.advance(level, pos, state, random);
    }

    /** O {@code markOrGrowMarked}: a primeira batida marca, a segunda faz a árvore. */
    public void advance(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_INVISIBLE);
            return;
        }
        this.grow(level, pos, state, random);
    }

    /** Tira a muda e tenta a árvore; se não couber, devolve a muda. */
    public boolean grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.removeBlock(pos, false);
        boolean nasceu = this.tree.apply(level, pos);
        if (!nasceu) level.setBlock(pos, state, Block.UPDATE_NONE);
        return nasceu;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < BONEMEAL_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.advance(level, pos, state, random);
    }
}
