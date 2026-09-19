package net.thaumcraft.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.world.outer.Cell;
import net.thaumcraft.world.outer.Labyrinth;

/** As Terras de Fora: a dimensão do labirinto eldritch ({@code Config.dimensionOuterId} no original). */
public final class OuterLands {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, Thaumcraft.id("outer"));

    private OuterLands() {
    }

    /** O {@code isDangerousLocation}: nas Terras de Fora, a sala da chave e as bibliotecas (as casas 6 e 8). */
    public static boolean dangerous(Level level, net.minecraft.core.BlockPos pos) {
        if (!is(level) || level.getServer() == null) return false;
        Labyrinth maze = Labyrinth.get(level.getServer());
        Cell c = maze == null ? null : maze.cell(pos.getX() >> 4, pos.getZ() >> 4);
        return c != null && (c.feature == 6 || c.feature == 8);
    }

    /** Este mundo é as Terras de Fora? */
    public static boolean is(Level level) {
        return level.dimension().equals(KEY);
    }
}
