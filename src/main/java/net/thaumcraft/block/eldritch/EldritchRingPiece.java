package net.thaumcraft.block.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Uma peça do anel eldritch: quebrada, leva as vizinhas junto e estoura, como no {@code breakBlock} do original. */
public interface EldritchRingPiece {
    static void breakRing(ServerLevel level, BlockPos pos) {
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
            if (level.getBlockState(at).getBlock() instanceof EldritchRingPiece) level.removeBlock(at, false);
        }
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, false, Level.ExplosionInteraction.NONE);
    }
}
