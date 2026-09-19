package net.thaumcraft.world.outer;

import net.minecraft.core.Direction;
import net.thaumcraft.registry.TCBlocks;

/**
 * A sala do portal, no meio do labirinto: o {@code GenPortal} da 4.2.3.5. Uma sala de treze por treze de pedra antiga
 * lisa, com o chão e o teto em pirâmide, os quatro cantos abertos em pilares de escada, e no centro o capitel, o portal de
 * volta e o obelisco em cima.
 */
final class GenPortal extends GenCommon {
    private GenPortal() {
    }

    static void generatePortal(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) placeBlock(w, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14)
                            && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10)
                            && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10)
                            && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        placeBlock(w, x + a, y + c, z + b, 8, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if ((a == 3 || a == 13 || b == 3 || b == 13) && (a > 4 || b > 4) && (a > 4 || b < 12) && (a < 12 || b > 4) && (a < 12 || b < 12)) {
                        placeBlock(w, x + a, y + c, z + b, 2, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                placeBlock(w, x + a, y - 1, z + b, 1, cell);
                placeBlock(w, x + a, y, z + b, 8, cell);
                placeBlock(w, x + a, y + 1, z + b, 19, cell);
                placeBlock(w, x + a, y + 13, z + b, 1, cell);
                placeBlock(w, x + a, y + 12, z + b, 8, cell);
                placeBlock(w, x + a, y + 11, z + b, 2, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) placeBlock(w, x + a, y + 1 + g, z + b, 19, cell);
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) placeBlock(w, x + a, y + 11 - g, z + b, 19, cell);
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            placeBlock(w, x + 6 + g, y + 2, z + 4, 10, Direction.NORTH, cell);
            placeBlock(w, x + 6 + g, y + 2, z + 12, 10, Direction.SOUTH, cell);
            placeBlock(w, x + 12, y + 2, z + 6 + g, 10, Direction.EAST, cell);
            placeBlock(w, x + 4, y + 2, z + 6 + g, 10, Direction.WEST, cell);
        }
        generateConnections(w, cx, cz, y, cell, 3, true);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 1; c < 12; c++) {
                    if (a <= 4 && b <= 4 || a <= 4 && b >= 12 || a >= 12 && b <= 4 || a >= 12 && b >= 12) {
                        placeBlock(w, x + a, y + c, z + b, 9, cell);
                        w.set(x + a, y + c, z + b, MazeWorld.AIR);
                    }
                }
            }
        }
        int[][] pillars = {{5, 5, 4, 5, 5, 4}, {12, 5, 11, 5, 11, 4}, {5, 11, 4, 11, 5, 12}, {12, 11, 11, 11, 11, 12}};
        Direction[][] faces = {{Direction.NORTH, Direction.WEST}, {Direction.NORTH, Direction.EAST}, {Direction.SOUTH, Direction.WEST},
                {Direction.SOUTH, Direction.EAST}};
        for (int i = 0; i < 4; i++) {
            int[] p = pillars[i];
            for (int[] level : new int[][]{{3, 10}, {8, 11}}) {
                placeBlock(w, x + p[0], y + level[0], z + p[1], level[1], faces[i][0], cell);
                placeBlock(w, x + p[2], y + level[0], z + p[3], level[1], faces[i][0], cell);
                placeBlock(w, x + p[4], y + level[0], z + p[5], level[1], faces[i][1], cell);
            }
        }
        w.set(x + 8, y + 2, z + 8, MazeBlocks.eldritch(3));
        w.set(x + 8, y + 3, z + 8, TCBlocks.ELDRITCH_PORTAL.defaultBlockState());
        genObelisk(w, x + 8, y + 4, z + 8);
    }
}
