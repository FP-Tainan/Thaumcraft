package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCBlocks;

/**
 * A barreira da pedra de proteção: o tipo quatro do {@code BlockAiry} da 4.2.3.5, com o
 * {@code TileWardingStoneFence}.
 *
 * <p>Não se vê nem se mira. Para bicho, é parede — e o caminho dos bichos a conta como parede —, a não ser que a
 * pedra embaixo esteja com sinal de redstone; para gente, é ar. A cada cem tiques ela confere se ainda há uma
 * pedra de proteção um ou dois blocos abaixo, e some se não houver.
 */
public class WardingBarrierBlock extends Block {
    public static final MapCodec<WardingBarrierBlock> CODEC = simpleCodec(WardingBarrierBlock::new);

    public WardingBarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entity && entity.getEntity() instanceof LivingEntity living
                && !(living instanceof Player) && level instanceof Level world && !powered(world, pos)) {
            return Shapes.block();
        }
        return Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // o getBlocksMovement do original: só passa quando a pedra está com redstone; sem saber a pedra aqui, parede
        return false;
    }

    /** A pedra embaixo (um ou dois blocos) está com sinal de redstone? */
    private static boolean powered(Level level, BlockPos pos) {
        int down = level.getBlockState(pos.below()).is(TCBlocks.BUILDING.get("paving_stone_warding")) ? 1 : 2;
        return level.hasNeighborSignal(pos.below(down));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, 1 + level.getRandom().nextInt(100));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var stone = TCBlocks.BUILDING.get("paving_stone_warding");
        if (!level.getBlockState(pos.below()).is(stone) && !level.getBlockState(pos.below(2)).is(stone)) {
            level.removeBlock(pos, false);
            return;
        }
        level.scheduleTick(pos, this, 100);
    }
}
