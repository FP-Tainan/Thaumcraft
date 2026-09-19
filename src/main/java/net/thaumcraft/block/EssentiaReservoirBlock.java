package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.world.Flux;
import org.jetbrains.annotations.Nullable;

/**
 * O reservatório de essência: o {@code BlockEssentiaReservoir} da 4.2.3.5. Uma caixa de vidro de 12/16 que guarda até
 * 256 de essência misturada, puxando do cano do lado para onde o bocal está virado. Posto, o bocal olha para o bloco
 * em que se clicou; a varinha o vira para a face batida (ou para a oposta, agachado). Quebrado cheio, estoura.
 */
public class EssentiaReservoirBlock extends BaseEntityBlock {
    public static final MapCodec<EssentiaReservoirBlock> CODEC = simpleCodec(EssentiaReservoirBlock::new);
    /** O {@code facing} do original: o lado do bocal. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public EssentiaReservoirBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** O {@code placeBlockAt} do item: o bocal vira para o bloco em que se clicou. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaReservoirBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ESSENTIA_RESERVOIR, EssentiaReservoirBlockEntity::tick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaReservoirBlockEntity te)) return 0;
        float r = (float) te.essentia.visSize() / te.maxAmount;
        return Mth.floor(r * 14.0f) + (te.essentia.visSize() > 0 ? 1 : 0);
    }

    /**
     * O {@code breakBlock}: com essência dentro, um estouro (que não quebra blocos) e o fluxo espalhado, um bloco a cada
     * 16 de essência. Chamado pela entidade antes de sair ({@code preRemoveSideEffects}), que é quem sabe quanto havia.
     */
    public static void burst(Level level, BlockPos pos, int essentia) {
        int sz = essentia / 16;
        if (sz <= 0 || !(level instanceof ServerLevel server)) return;
        server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, Level.ExplosionInteraction.NONE);
        Flux.spill(server, pos, sz);
    }
}
