package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Placa de Marcação: o {@code BlockMarkingPlate} das Portas Dimensionais.
 *
 * <p>Um poste alto e fino, de quase dois blocos, que serve para marcar um lugar. No original ele não faz mais
 * nada — é enfeite —, e é assim que vem para cá.
 */
public class MarkingPlateBlock extends Block {
    public static final MapCodec<MarkingPlateBlock> CODEC = simpleCodec(MarkingPlateBlock::new);

    /** O tamanho que ele ocupa: o {@code FULL_BLOCK_AABB} do original, que é mais alto que o bloco. */
    private static final VoxelShape SHAPE = box(4.0, 0.0, 0.0, 12.0, 30.0, 16.0);

    public MarkingPlateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
