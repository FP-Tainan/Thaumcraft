package net.thaumcraft.occulta.spirit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O Apanhador de Sonhos: a {@code BlockDreamCatcher} do Witchery.
 *
 * <p>Pendura-se numa parede e prega-se-lhe uma <b>Teia de Sonho</b>. Quem dormir a cinco dele leva o que a teia
 * tem para dar; e é ele, com a teia dos pesadelos, que faz o <b>quarto de sonho</b> valer a pena montar — sem um
 * por perto, o outro lado é quase sempre pesadelo.
 */
public class DreamCatcherBlock extends BaseEntityBlock {
    public static final MapCodec<DreamCatcherBlock> CODEC = simpleCodec(DreamCatcherBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final double PANE = 0.2;

    public DreamCatcherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction lado = context.getClickedFace();
        if (lado.getAxis().isVertical()) return null;
        return this.defaultBlockState().setValue(FACING, lado);
    }

    /** Ele precisa de parede atrás: sem ela, cai. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction olha = state.getValue(FACING);
        return level.getBlockState(pos.relative(olha.getOpposite())).isFaceSturdy(level,
                pos.relative(olha.getOpposite()), olha);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction olha = state.getValue(FACING);
        double fundo = (1.0 - PANE) * 16.0;
        double frente = PANE * 16.0;
        return switch (olha) {
            case NORTH -> Block.box(0.0, 0.0, fundo, 16.0, 16.0, 16.0);
            case SOUTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, frente);
            case WEST -> Block.box(fundo, 0.0, 0.0, 16.0, 16.0, 16.0);
            default -> Block.box(0.0, 0.0, 0.0, frente, 16.0, 16.0);
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DreamCatcherBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, net.thaumcraft.occulta.OccultaBlocks.DREAM_CATCHER_ENTITY,
                (mundo, onde, feitio, alma) -> alma.tick());
    }

    /** Pregar-lhe uma teia, ou tirar a que lá está. */
    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos pos,
                                          Player quem, InteractionHand mão, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!(server.getBlockEntity(pos) instanceof DreamCatcherBlockEntity alma)) return InteractionResult.PASS;

        var teia = DreamWeaveItem.of(naMão);
        if (teia == null) return InteractionResult.PASS;
        if (alma.weave() == teia) return InteractionResult.PASS;

        ItemStack velha = alma.drop();
        alma.setWeave(teia);
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
        if (!velha.isEmpty() && !quem.getInventory().add(velha)) quem.drop(velha, false);
        server.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moving) {
        if (level.getBlockEntity(pos) instanceof DreamCatcherBlockEntity alma) {
            ItemStack teia = alma.drop();
            if (!teia.isEmpty()) Block.popResource(level, pos, teia);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
}
