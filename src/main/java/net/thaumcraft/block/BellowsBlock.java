package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.BellowsBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O fole: o tipo zero do {@code BlockWoodenDevice} da 4.2.3.5, com o {@code TileBellows}.
 *
 * <p>O bico aponta para o bloco em que se clicou ao pô-lo. Soprando num forno alquímico, cada fole corta um
 * oitavo do tempo de fogo; soprando num forno comum, empurra o cozimento um tique a cada dois. Com redstone, para.
 */
public class BellowsBlock extends BaseEntityBlock {
    public static final MapCodec<BellowsBlock> CODEC = simpleCodec(BellowsBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    /** A caixa do original: um décimo de folga dos lados. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 16.0, 14.4);

    public BellowsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** O {@code placeBlockAt} do {@code BlockWoodenDeviceItem}: o bico olha para dentro da face clicada. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BellowsBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.BELLOWS,
                level.isClientSide() ? BellowsBlockEntity::clientTick : BellowsBlockEntity::serverTick);
    }

    /** O {@code getBellows} do {@code TileBellows}: quantos foles sopram neste bloco, sem redstone. */
    public static int blowingInto(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            BlockPos at = pos.relative(dir);
            BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof BellowsBlock && state.getValue(FACING) == dir.getOpposite()
                    && !level.hasNeighborSignal(at)) {
                count++;
            }
        }
        return count;
    }
}
