package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.thaumcraft.block.entity.EtherealBloomBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCBlocks;

/**
 * A Flor Etérea: a única coisa que faz a mácula recuar.
 *
 * <p>É a flor do original, e a resposta dele para quem deixou a mácula crescer demais: ela devolve aos poucos o
 * bioma natural das colunas em volta (ver {@link EtherealBloomBlockEntity}), e fora da Terra Maculada a crosta vira
 * gosma, o solo volta a ser terra e as fibras somem.
 *
 * <p>No mundo o bloco não tem desenho — o original devolve {@code blank} para a face de onde a cruz tira a
 * textura —, e quem desenha a flor é o {@link net.thaumcraft.client.render.EtherealBloomRenderer}. Como
 * planta de caverna do Forge, pega em qualquer chão firme.
 */
public class EtherealBloomBlock extends VegetationBlock implements EntityBlock {
    public static final MapCodec<EtherealBloomBlock> CODEC = simpleCodec(EtherealBloomBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public EtherealBloomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EtherealBloomBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,
                                                                            BlockState state, BlockEntityType<T> type) {
        return type == TCBlockEntities.ETHEREAL_BLOOM ? (BlockEntityTicker<T>) (BlockEntityTicker<EtherealBloomBlockEntity>) EtherealBloomBlockEntity::tick : null;
    }
}
