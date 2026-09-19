package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.NodeJarBlockEntity;

/**
 * O nó no jarro: o número 2 do {@code BlockJar} da 4.2.3.5 — o vidro do jarro com um nó de aura preso dentro, que
 * ilumina com força 11. O nó não se refaz nem faz nada enquanto estiver preso.
 */
public class NodeJarBlock extends BaseEntityBlock {
    public static final MapCodec<NodeJarBlock> CODEC = simpleCodec(NodeJarBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public NodeJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** O evento 9 do {@code TileJarNode}: estrelinhas de todas as cores em volta do jarro recém-feito. */
    @Override
    protected boolean triggerEvent(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, int id, int param) {
        if (id != 9) return super.triggerEvent(state, level, pos, id, param);
        if (level.isClientSide()) clientEffects.accept(pos);
        return true;
    }

    public static java.util.function.Consumer<BlockPos> clientEffects = pos -> {
    };

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeJarBlockEntity(pos, state);
    }
}
