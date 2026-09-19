package net.thaumcraft.world.outer;

import net.minecraft.core.Direction;

/**
 * As quatro partes da sala do chefe, dois chunks por dois: o {@code Gen2x2} da 4.2.3.5. Cada parte faz as duas paredes
 * de fora do seu canto (rocha-mãe, o nada, a rocha antiga), o chão e o teto, e as escadas-rodapé; a sala inteira se
 * fecha quando as quatro existem.
 */
final class Gen2x2 extends GenCommon {
    private Gen2x2() {
    }

    /**
     * Uma parte: {@code east}/{@code south} dizem em que lado da sala ela fica (a de cima à esquerda tem as paredes no oeste
     * e no norte). As contas são as do original, espelhadas.
     */
    private static void generate(MazeWorld w, int cx, int cz, int y, Cell cell, boolean east, boolean south) {
        int x = cx * 16;
        int z = cz * 16;
        // a parede de fora: a do lado (oeste 1 ou leste 15) e a de cima ou de baixo (norte 1 ou sul 15)
        int wallA = east ? 15 : 1, wallB = south ? 15 : 1;
        int a0 = east ? 0 : 1, a1 = 15, b0 = south ? 0 : 1, b1 = 15;
        for (int a = a0; a <= a1; a++) {
            for (int b = b0; b <= b1; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == wallA || b == wallB) placeBlock(w, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        int voidA = east ? 14 : 2, voidB = south ? 14 : 2;
        int va0 = east ? 0 : 2, va1 = east ? 14 : 15, vb0 = south ? 0 : 2, vb1 = south ? 14 : 15;
        boolean sideExit = east ? cell.east : cell.west;
        boolean endExit = south ? cell.south : cell.north;
        for (int a = va0; a <= va1; a++) {
            for (int b = vb0; b <= vb1; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == voidA || b == voidB) && (a != voidA || b <= 4 || b >= 12 || !sideExit || c >= 10)
                            && (b != voidB || a <= 4 || a >= 12 || !endExit || c >= 10)) {
                        placeBlock(w, x + a, y + c, z + b, 8, cell);
                    }
                }
            }
        }
        int rockA = east ? 13 : 3, rockB = south ? 13 : 3;
        int ra0 = east ? 0 : 3, ra1 = east ? 13 : 15, rb0 = south ? 0 : 3, rb1 = south ? 13 : 15;
        for (int a = ra0; a <= ra1; a++) {
            for (int b = rb0; b <= rb1; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == rockA || b == rockB) placeBlock(w, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (int a = va0; a <= va1; a++) {
            for (int b = vb0; b <= vb1; b++) {
                placeBlock(w, x + a, y - 1, z + b, 1, cell);
                placeBlock(w, x + a, y, z + b, 8, cell);
                placeBlock(w, x + a, y + 1, z + b, 19, cell);
                placeBlock(w, x + a, y + 13, z + b, 1, cell);
                placeBlock(w, x + a, y + 12, z + b, 8, cell);
                placeBlock(w, x + a, y + 11, z + b, 2, cell);
            }
        }
        // as escadas-rodapé ao longo das duas paredes
        int stairZ = south ? 12 : 4, stairX = east ? 12 : 4;
        Direction endDir = south ? Direction.SOUTH : Direction.NORTH, sideDir = east ? Direction.EAST : Direction.WEST;
        int g0 = east ? 0 : 4, g1 = east ? 11 : 15;
        for (int g = g0; g <= g1; g++) {
            placeBlock(w, x + g, y + 2, z + stairZ, 10, endDir, cell);
            placeBlock(w, x + g, y + 10, z + stairZ, 11, endDir, cell);
        }
        int h0 = south ? 0 : 4, h1 = south ? (east ? 12 : 11) : 15;
        for (int g = h0; g <= h1; g++) {
            placeBlock(w, x + stairX, y + 2, z + g, 10, sideDir, cell);
            placeBlock(w, x + stairX, y + 10, z + g, 11, sideDir, cell);
        }
        generateConnections(w, cx, cz, y, cell, 3, true);
    }

    static void generateUpperLeft(MazeWorld w, int cx, int cz, int y, Cell cell) {
        generate(w, cx, cz, y, cell, false, false);
    }

    static void generateUpperRight(MazeWorld w, int cx, int cz, int y, Cell cell) {
        generate(w, cx, cz, y, cell, true, false);
    }

    static void generateLowerLeft(MazeWorld w, int cx, int cz, int y, Cell cell) {
        generate(w, cx, cz, y, cell, false, true);
    }

    static void generateLowerRight(MazeWorld w, int cx, int cz, int y, Cell cell) {
        generate(w, cx, cz, y, cell, true, true);
    }
}
