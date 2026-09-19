package net.thaumcraft.world;

import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;

/** As Terras de Fora: a dimensão do labirinto eldritch ({@code Config.dimensionOuterId} no original). */
public final class OuterLands {
    private OuterLands() {
    }

    /** Este mundo é as Terras de Fora? */
    public static boolean is(Level level) {
        return level.dimension().identifier().equals(Thaumcraft.id("outer"));
    }
}
