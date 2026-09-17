package net.thaumcraft.block;

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
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O bloco de um nó de aura.
 *
 * <p>Ele não é uma pedra: é uma bolha de magia parada no ar. Não tem face nenhuma para desenhar — quem o
 * desenha é o {@code NodeRenderer} — e não segura ninguém que passe por ele. O que ele tem é um miolo de
 * meia casa, para que a mira do thaumômetro e a da varinha possam pegá-lo.
 */
public class NodeBlock extends BaseEntityBlock {
    public static final MapCodec<NodeBlock> CODEC = simpleCodec(NodeBlock::new);
    /** O miolo que a mira pega: meia casa no meio do bloco. */
    private static final VoxelShape CORE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    public NodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // nada de faces: o nó é desenhado à parte
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CORE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // atravessa-se um nó como se atravessa o ar
        return Shapes.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.NODE, NodeBlockEntity::tick);
    }
}
