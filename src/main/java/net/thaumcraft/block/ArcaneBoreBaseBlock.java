package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.thaumcraft.block.entity.ArcaneBoreBaseBlockEntity;

/**
 * A base da broca arcana: o número 4 do {@code BlockWoodenDevice} da 4.2.3.5, com o {@code TileArcaneBoreBase}. Um
 * pedestal de madeira com um bico de lado, por onde sai o que a broca cava; a broca vai em cima (ou embaixo) dela.
 * O bico nasce virado conforme o jogador olha e a varinha o gira para a face batida.
 */
public class ArcaneBoreBaseBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneBoreBaseBlock> CODEC = simpleCodec(ArcaneBoreBaseBlock::new);
    /** O {@code orientation}: para onde o bico aponta (e onde os itens saem). */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public ArcaneBoreBaseBlock(Properties properties) {
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

    /** O {@code placeBlockAt}: o giro do jogador em quartos — olhando para o sul, o bico fica ao norte. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBaseBlockEntity(pos, state);
    }
}
