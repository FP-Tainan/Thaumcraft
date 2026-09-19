package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.eldritch.EldritchPortalBlockEntity;
import net.thaumcraft.world.OuterLands;
import org.jetbrains.annotations.Nullable;

/**
 * A passagem entre os mundos: o {@code TeleporterThaumcraft} da 4.2.3.5. Nas mesmas coordenadas do outro lado, procura o
 * portal eldritch mais perto a até cento e vinte e oito blocos e põe o viajante numa quina ao lado dele; sem portal, fica
 * onde estava, só que no outro mundo.
 *
 * <p>O original varria cada bloco do quadrado; aqui, nas Terras de Fora, o portal é achado pelo labirinto (é a casa do
 * meio), e no mundo de cima pelas entidades de bloco dos chunks, do mais perto para o mais longe.
 */
public final class OuterTeleporter {
    private static final int RANGE = 128;

    private OuterTeleporter() {
    }

    public static void send(ServerPlayer player, ServerLevel target) {
        BlockPos portal = findPortal(target, player.getX(), player.getY(), player.getZ());
        Vec3 to;
        if (portal != null) {
            var random = target.getRandom();
            to = new Vec3(portal.getX() + 0.5 + (random.nextBoolean() ? 1 : -1), portal.getY(), portal.getZ() + 0.5 + (random.nextBoolean() ? 1 : -1));
        } else {
            to = player.position();
        }
        player.teleport(new TeleportTransition(target, to, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
    }

    @Nullable
    static BlockPos findPortal(ServerLevel level, double px, double py, double pz) {
        if (OuterLands.is(level)) return findInMaze(level, px, py, pz);
        int ccx = (int) Math.floor(px) >> 4, ccz = (int) Math.floor(pz) >> 4;
        BlockPos best = null;
        double bestDist = -1.0;
        int rings = RANGE / 16;
        for (int r = 0; r <= rings; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    ChunkAccess chunk = level.getChunk(ccx + dx, ccz + dz, ChunkStatus.FULL, true);
                    if (chunk == null) continue;
                    for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                        if (!(chunk.getBlockEntity(pos) instanceof EldritchPortalBlockEntity)) continue;
                        if (Math.abs(pos.getX() + 0.5 - px) > RANGE || Math.abs(pos.getZ() + 0.5 - pz) > RANGE) continue;
                        double d = sq(pos.getX() + 0.5 - px) + sq(pos.getY() + 0.5 - py) + sq(pos.getZ() + 0.5 - pz);
                        if (bestDist < 0.0 || d < bestDist) {
                            bestDist = d;
                            best = pos.immutable();
                        }
                    }
                }
            }
            // o anel seguinte já está mais longe que o melhor achado
            if (best != null && sq(r * 16.0) > bestDist) break;
        }
        return best;
    }

    /** Nas Terras de Fora, os portais estão nas casas 1 do labirinto, no capitel do meio da sala. */
    @Nullable
    private static BlockPos findInMaze(ServerLevel level, double px, double py, double pz) {
        Labyrinth maze = Labyrinth.get(level.getServer());
        if (maze == null) return null;
        int ccx = (int) Math.floor(px) >> 4, ccz = (int) Math.floor(pz) >> 4;
        BlockPos best = null;
        double bestDist = -1.0;
        for (int dx = -RANGE / 16 - 1; dx <= RANGE / 16 + 1; dx++) {
            for (int dz = -RANGE / 16 - 1; dz <= RANGE / 16 + 1; dz++) {
                Cell cell = maze.cell(ccx + dx, ccz + dz);
                if (cell == null || cell.feature != 1) continue;
                BlockPos pos = new BlockPos((ccx + dx) * 16 + 8, MazeFeature.FLOOR + 3, (ccz + dz) * 16 + 8);
                if (Math.abs(pos.getX() + 0.5 - px) > RANGE || Math.abs(pos.getZ() + 0.5 - pz) > RANGE) continue;
                double d = sq(pos.getX() + 0.5 - px) + sq(pos.getY() + 0.5 - py) + sq(pos.getZ() + 0.5 - pz);
                if (bestDist < 0.0 || d < bestDist) {
                    bestDist = d;
                    best = pos;
                }
            }
        }
        if (best != null) level.getChunk(best);
        return best;
    }

    private static double sq(double v) {
        return v * v;
    }
}
