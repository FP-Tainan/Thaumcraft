package net.thaumcraft.world;

import net.minecraft.world.level.WorldGenLevel;

/**
 * O que o anel eldritch combina com as Terras de Fora: o {@code MazeHandler.mazesInRange} (não fazer anel onde já há
 * labirinto reservado) e o {@code MazeThread} que reserva o labirinto do anel novo. Chegam com as Terras de Fora.
 */
public final class EldritchRings {
    private EldritchRings() {
    }

    static boolean mazesInRange(WorldGenLevel level, int chunkX, int chunkZ, int w, int h) {
        return false;
    }

    static void reserveMaze(WorldGenLevel level, int chunkX, int chunkZ, int w, int h, long seed) {
    }
}
