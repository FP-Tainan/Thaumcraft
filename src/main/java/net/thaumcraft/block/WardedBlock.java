package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.WardedBlockEntity;

/**
 * O bloco protegido pelo foco de Proteção: o {@code BlockWarded} da 4.2.3.5, descompilado.
 *
 * <p>Ele guarda o bloco que estava ali e se faz passar por ele — mesma cara, mesma forma, mesma luz —, mas
 * não se quebra, não explode e não deixa bicho nascer em cima. Quem o desenha é o desenhista, com o bloco
 * guardado; com a varinha de Proteção na mão, aparecem por cima as runas douradas (as do dono) ou vermelhas
 * (as dos outros).
 */
public class WardedBlock extends BaseEntityBlock {
    public static final MapCodec<WardedBlock> CODEC = simpleCodec(WardedBlock::new);
    /** A luz do bloco guardado: o {@code light} do {@code TileWarded}, que aqui vai no estado. */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    public WardedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // quem desenha é o desenhista, com o bloco guardado
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState stored = stored(level, pos);
        return stored == null ? Shapes.block() : stored.getShape(level, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState stored = stored(level, pos);
        return stored == null ? Shapes.block() : stored.getCollisionShape(level, pos, context);
    }

    private static BlockState stored(BlockGetter level, BlockPos pos) {
        // o original desiste depois de umas voltas e fica com pedra; aqui basta não se chamar a si mesmo
        if (level.getBlockEntity(pos) instanceof WardedBlockEntity warded && !(warded.stored().getBlock() instanceof WardedBlock)) {
            return warded.stored();
        }
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardedBlockEntity(pos, state);
    }
}
