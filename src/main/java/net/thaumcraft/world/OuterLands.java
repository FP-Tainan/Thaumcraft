package net.thaumcraft.world;

import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;

/** As Terras de Fora: a dimensão do labirinto eldritch ({@code Config.dimensionOuterId} no original). */
public final class OuterLands {
    private OuterLands() {
    }

    /**
     * O {@code isDangerousLocation}: nas Terras de Fora, as salas de chefe e os ninhos do labirinto (as casas 6 e 8). Chega
     * com o labirinto.
     */
    public static boolean dangerous(Level level, net.minecraft.core.BlockPos pos) {
        return false;
    }

    /** Este mundo é as Terras de Fora? */
    public static boolean is(Level level) {
        return level.dimension().identifier().equals(Thaumcraft.id("outer"));
    }
}
