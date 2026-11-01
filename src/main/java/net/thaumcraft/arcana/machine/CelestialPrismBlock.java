package net.thaumcraft.arcana.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.arcana.ArcanaBlocks;

/**
 * O <b>Prisma Celeste</b>: o {@code BlockEssenceGenerator} de tipo dois, da 1.4.0.009.
 *
 * <p>Dois blocos de altura num lugar só, como o Obelisco, e com o mesmo brilho. Não tem tela: o que ele
 * faz não depende de ninguém lhe pôr nada dentro.
 */
public class CelestialPrismBlock extends BaseEntityBlock {
    public static final MapCodec<CelestialPrismBlock> CODEC = simpleCodec(CelestialPrismBlock::new);

    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0);

    public CelestialPrismBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new CelestialPrismBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nível, BlockState feitio, BlockEntityType<T> tipo) {
        if (nível.isClientSide()) return null;
        return createTickerHelper(tipo, ArcanaBlocks.CELESTIAL_PRISM_ENTITY,
                (mundo, onde, estado, prisma) -> prisma.serverTick((net.minecraft.server.level.ServerLevel) mundo));
    }
}
