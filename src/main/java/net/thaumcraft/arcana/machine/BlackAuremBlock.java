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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.arcana.ArcanaBlocks;

/**
 * O <b>Aurem Negro</b>: o {@code BlockEssenceGenerator} de tipo um, da 1.4.0.009.
 *
 * <p>Ele <b>não tem corpo</b>: atravessa-se. É de propósito, e faz sentido — o que ele faz é puxar o que
 * está à volta para dentro de si, e um bicho que esbarrasse nele nunca lá chegaria. A forma que se mira
 * começa a meio bloco do chão e sobe dois.
 */
public class BlackAuremBlock extends BaseEntityBlock {
    public static final MapCodec<BlackAuremBlock> CODEC = simpleCodec(BlackAuremBlock::new);

    /** O {@code setBlockBounds(0, 0.5, 0, 1, 2, 1)} do original. */
    private static final VoxelShape FORMA = Block.box(0.0, 8.0, 0.0, 16.0, 32.0, 16.0);

    public BlackAuremBlock(Properties properties) {
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

    /** Sem corpo: o {@code getCollisionBoundingBoxFromPool} do original devolve nada para este. */
    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter nível, BlockPos onde, CollisionContext quem) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new BlackAuremBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nível, BlockState feitio, BlockEntityType<T> tipo) {
        if (nível.isClientSide()) return null;
        return createTickerHelper(tipo, ArcanaBlocks.BLACK_AUREM_ENTITY,
                (mundo, onde, estado, aurem) -> aurem.serverTick((net.minecraft.server.level.ServerLevel) mundo));
    }
}
