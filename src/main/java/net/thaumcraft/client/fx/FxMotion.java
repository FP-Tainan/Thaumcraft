package net.thaumcraft.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

/**
 * O movimento de um {@code EntityFX} do 1.7.10 para os efeitos que vivem no {@link ThaumFx}: a caixa de 0,2 em volta do
 * ponto, o {@code moveEntity} que bate nos blocos (e zera o movimento no eixo que bateu) e o {@code pushOutOfBlocks}.
 */
final class FxMotion {
    /** Posição e movimento; o {@link #move} mexe nos dois. */
    double x, y, z, mx, my, mz;

    FxMotion(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** O {@code moveEntity} com a caixa de 0,2. */
    void move(Level level) {
        AABB box = new AABB(this.x - 0.1, this.y - 0.1, this.z - 0.1, this.x + 0.1, this.y + 0.1, this.z + 0.1);
        Vec3 wanted = new Vec3(this.mx, this.my, this.mz);
        Vec3 done = Entity.collideBoundingBox(CollisionContext.empty(), wanted, box, level, List.of());
        this.x += done.x;
        this.y += done.y;
        this.z += done.z;
        if (done.x != wanted.x) this.mx = 0.0;
        if (done.y != wanted.y) this.my = 0.0;
        if (done.z != wanted.z) this.mz = 0.0;
    }

    /**
     * O {@code pushOutOfBlocks} das partículas: dentro de um bloco inteiro (e fora de líquido), empurra para o lado livre
     * mais perto, com um tremor nos outros dois eixos.
     */
    void pushOutOfBlocks(Level level, RandomSource random) {
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        var state = level.getBlockState(pos);
        if (state.isAir() || !state.isCollisionShapeFullBlock(level, pos) || !state.getFluidState().isEmpty()) return;
        double fx = this.x - pos.getX(), fy = this.y - pos.getY(), fz = this.z - pos.getZ();
        double[] dist = {fx, 1.0 - fx, fy, 1.0 - fy, fz, 1.0 - fz};
        BlockPos[] next = {pos.west(), pos.east(), pos.below(), pos.above(), pos.north(), pos.south()};
        double best = 9999.0;
        int side = -1;
        for (int i = 0; i < 6; i++) {
            if (!level.getBlockState(next[i]).isCollisionShapeFullBlock(level, next[i]) && dist[i] < best) {
                best = dist[i];
                side = i;
            }
        }
        float push = random.nextFloat() * 0.05f + 0.025f;
        float jitter = (random.nextFloat() - random.nextFloat()) * 0.1f;
        switch (side) {
            case 0 -> { this.mx = -push; this.my = this.mz = jitter; }
            case 1 -> { this.mx = push; this.my = this.mz = jitter; }
            case 2 -> { this.my = -push; this.mx = this.mz = jitter; }
            case 3 -> { this.my = push; this.mx = this.mz = jitter; }
            case 4 -> { this.mz = -push; this.my = this.mx = jitter; }
            case 5 -> { this.mz = push; this.my = this.mx = jitter; }
            default -> { }
        }
    }
}
