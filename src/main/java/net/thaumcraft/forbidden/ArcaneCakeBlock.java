package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Bolo Arcano: o {@code BlockArcaneCake} do Forbidden Magic 0.575.
 *
 * <p>Um bolo de doze fatias que <b>volta a crescer sozinho</b>: cada vez que o acaso bate nele, uma fatia
 * torna. Cada garfada enche dois de fome com um de saturação, como a do bolo comum.
 */
public class ArcaneCakeBlock extends Block {
    public static final MapCodec<ArcaneCakeBlock> CODEC = simpleCodec(ArcaneCakeBlock::new);

    /** Quantas fatias já saíram: de zero (inteiro) a onze (a última). */
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 11);

    private static final VoxelShape[] SHAPES = new VoxelShape[12];

    static {
        for (int mordidas = 0; mordidas < SHAPES.length; mordidas++) {
            // o (1 + l) / 12 do original, que na última fatia passaria do bloco: fica na beirada
            double corte = Math.min((1 + mordidas) / 12.0 * 16.0, 15.0);
            SHAPES[mordidas] = Block.box(corte, 0.0, 1.0, 15.0, 8.0, 15.0);
        }
    }

    public ArcaneCakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BITES)];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        return eat(level, pos, state, player);
    }

    /** O {@code eatCakeSlice}: dois de fome e um de saturação, e uma fatia a menos. */
    private static InteractionResult eat(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) return InteractionResult.PASS;
        player.getFoodData().eat(2, 1.0f);
        int mordidas = state.getValue(BITES) + 1;
        if (mordidas >= 12) level.removeBlock(pos, false);
        else level.setBlock(pos, state.setValue(BITES, mordidas), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    /** O {@code updateTick}: ele se refaz, uma fatia de cada vez. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int mordidas = state.getValue(BITES);
        if (mordidas > 0) level.setBlock(pos, state.setValue(BITES, mordidas - 1), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks,
                                     BlockPos pos, net.minecraft.core.Direction direction, BlockPos neighbour,
                                     BlockState neighbourState, RandomSource random) {
        return !state.canSurvive(level, pos) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighbour, neighbourState, random);
    }

    /** Quebrado, ele não deixa nada: quem quer bolo faz outro. */
    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        return java.util.List.of();
    }
}
