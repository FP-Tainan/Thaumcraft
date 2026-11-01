package net.thaumcraft.arcana.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.arcana.ArcanaBlocks;

/**
 * O <b>Conduto de Essência</b>: o {@code BlockEssenceConduit} da 1.4.0.009.
 *
 * <p>Um pedestal estreito com um cristal a girar em cima. Ocupa o bloco inteiro de altura mas só o meio em
 * largura, e <b>vira-se para a face em que foi posto</b>.
 *
 * <p>Desenha-se inteiro no desenhista do bloco-entidade, como no original — não é uma caixa de texturas.
 */
public class EssenceConduitBlock extends BaseEntityBlock {
    public static final MapCodec<EssenceConduitBlock> CODEC = simpleCodec(EssenceConduitBlock::new);

    /** A face em que ele está colado. */
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public EssenceConduitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
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
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return this.defaultBlockState().setValue(FACING, contexto.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    /** O corpo dele é do desenhista, e não do modelo de bloco. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new EssenceConduitBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nível, BlockState feitio, BlockEntityType<T> tipo) {
        return createTickerHelper(tipo, ArcanaBlocks.ESSENCE_CONDUIT_ENTITY,
                (mundo, onde, estado, conduto) -> {
                    if (mundo instanceof net.minecraft.server.level.ServerLevel servidor) conduto.serverTick(servidor);
                    else conduto.clientTick();
                });
    }
}
