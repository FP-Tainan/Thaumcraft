package net.thaumcraft.entity.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.TCBiomes;

/**
 * O estouro que macula o chão (o do creeper maculado e o da garrafa de mácula): sorteios em volta de onde se estourou,
 * metade deles pintando de Terra Maculada uma coluna que ainda não é e pondo fibra em cima do chão firme, se ali cabe.
 */
public final class TaintSplosion {
    private TaintSplosion() {
    }

    /** O {@code taintsplosionFX} (o cliente liga isto). */
    public static java.util.function.Consumer<Entity> effect = e -> {
    };

    public static void taintAround(ServerLevel level, Entity source, int tries, float spread) {
        int x = (int) source.getX(), y = (int) source.getY(), z = (int) source.getZ();
        var random = source.getRandom();
        for (int a = 0; a < tries; a++) {
            int xx = x + (int) ((random.nextFloat() - random.nextFloat()) * spread);
            int zz = z + (int) ((random.nextFloat() - random.nextFloat()) * spread);
            BlockPos at = new BlockPos(xx, y, zz);
            if (!level.getRandom().nextBoolean() || level.getBiome(at).is(TCBiomes.TAINTED_LAND)) continue;
            BiomePainter.paint(level, at, TCBiomes.TAINTED_LAND);
            BlockState below = level.getBlockState(at.below());
            if (below.isFaceSturdy(level, at.below(), Direction.UP) && below.isCollisionShapeFullBlock(level, at.below())
                    && level.getBlockState(at).canBeReplaced()) {
                level.setBlockAndUpdate(at, TCBlocks.TAINT_FIBRES.defaultBlockState());
            }
        }
    }
}
