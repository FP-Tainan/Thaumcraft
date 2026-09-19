package net.thaumcraft.world;

import net.minecraft.world.level.WorldGenLevel;
import net.thaumcraft.world.outer.Labyrinth;

/**
 * O que o anel eldritch combina com as Terras de Fora: o {@code MazeHandler.mazesInRange} (não fazer anel onde já há
 * labirinto reservado) e o {@code MazeThread} que reserva o labirinto do anel novo.
 */
public final class EldritchRings {
    private EldritchRings() {
    }

    static boolean mazesInRange(WorldGenLevel level, int chunkX, int chunkZ, int w, int h) {
        Labyrinth maze = Labyrinth.get(level.getServer());
        return maze != null && maze.mazesInRange(chunkX, chunkZ, w, h);
    }

    static void reserveMaze(WorldGenLevel level, int chunkX, int chunkZ, int w, int h, long seed) {
        Labyrinth maze = Labyrinth.get(level.getServer());
        if (maze != null) maze.reserve(chunkX, chunkZ, w, h, seed);
    }
}
