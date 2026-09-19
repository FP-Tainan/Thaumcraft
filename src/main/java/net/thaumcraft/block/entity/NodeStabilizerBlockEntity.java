package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * O {@code TileNodeStabilizer} da 4.2.3.5: só o desenho usa. Com um nó em cima e sem sinal de redstone, os pistões
 * vão se abrindo até 37; sem, voltam.
 */
public class NodeStabilizerBlockEntity extends BlockEntity {
    public int count;

    public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_STABILIZER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity stabilizer) {
        BlockState above = level.getBlockState(pos.above());
        boolean node = above.is(TCBlocks.NODE) || above.is(TCBlocks.ENERGIZED_NODE);
        if (node && !level.hasNeighborSignal(pos)) {
            if (stabilizer.count < 37) stabilizer.count++;
        } else if (stabilizer.count > 0) {
            stabilizer.count--;
        }
    }
}
