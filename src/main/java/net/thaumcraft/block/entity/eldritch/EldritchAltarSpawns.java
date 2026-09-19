package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Quem o altar eldritch chama e o labirinto que ele reserva: as partes do {@code TileEldritchAltar} que dependem dos
 * cultistas, do guardião e das Terras de Fora. Ficam aqui, à parte, até as fatias que trazem cada um.
 */
public final class EldritchAltarSpawns {
    private EldritchAltarSpawns() {
    }

    /** O {@code spawnClerics}: os quatro clérigos do ritual nas quinas do altar. Chega com os cultistas. */
    static void clerics(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
    }

    /** O {@code spawnGuards}: cavaleiros carmesins enquanto houver clérigo por perto. Chega com os cultistas. */
    static void guards(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
    }

    /** O {@code spawnGuardian}: um guardião eldritch. Chega com o guardião. */
    static void guardian(ServerLevel level, BlockPos pos, EldritchAltarBlockEntity altar) {
    }

    /** O {@code checkForMaze}. Chega com as Terras de Fora. */
    static boolean checkForMaze(Level level, BlockPos pos, int w, int h) {
        return false;
    }
}
