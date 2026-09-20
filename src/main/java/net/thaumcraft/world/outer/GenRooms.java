package net.thaumcraft.world.outer;

import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.thaumcraft.block.eldritch.AncientLockBlock;
import net.thaumcraft.block.eldritch.StrangeCrystalBlock;
import net.thaumcraft.entity.PermanentItemEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * As salas dos becos e a da porta do chefe: o {@code GenBossRoom}, o {@code GenKeyRoom}, o {@code GenNestRoom} e o
 * {@code GenLibraryRoom} da 4.2.3.5.
 */
final class GenRooms extends GenCommon {
    private GenRooms() {
    }

    static final int[][] PAT_DOORWAY = {
            {0, 2, 2, 2, 2, 2, 0},
            {2, 2, 9, 9, 9, 2, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 9, 9, 1, 9, 9, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 2, 9, 9, 9, 2, 2},
            {0, 2, 2, 2, 2, 2, 0}
    };

    /** A parte da sala do chefe, e, na que tem a saída, a porta antiga com a fechadura no meio e o intransponível em volta. */
    static void bossRoom(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        switch (cell.feature) {
            case 2 -> Gen2x2.generateUpperLeft(w, cx, cz, 50, cell);
            case 3 -> Gen2x2.generateUpperRight(w, cx, cz, 50, cell);
            case 4 -> Gen2x2.generateLowerLeft(w, cx, cz, 50, cell);
            case 5 -> Gen2x2.generateLowerRight(w, cx, cz, 50, cell);
            default -> {
            }
        }
        for (int a = 0; a < 7; a++) {
            for (int b = 0; b < 7; b++) {
                int xx = 0, zz = 0;
                Direction dir = null;
                if (cell.north) {
                    xx = x + 5 + a;
                    zz = z + 3;
                    dir = Direction.NORTH;
                }
                if (cell.south) {
                    xx = x + 5 + a;
                    zz = z + 13;
                    dir = Direction.SOUTH;
                }
                if (cell.east) {
                    xx = x + 13;
                    zz = z + 5 + a;
                    dir = Direction.EAST;
                }
                if (cell.west) {
                    xx = x + 3;
                    zz = z + 5 + a;
                    dir = Direction.WEST;
                }
                // a parte da sala que não tem saída não tem portal nenhum: o original escrevia na origem do mundo
                if (dir == null) continue;
                switch (PAT_DOORWAY[a][b]) {
                    case 1 -> {
                        placeBlock(w, xx, y + 2 + b, zz, 16, cell);
                        var lock = w.get(xx, y + 2 + b, zz);
                        if (dir != null && lock.is(TCBlocks.ANCIENT_LOCK)) w.set(xx, y + 2 + b, zz, lock.setValue(AncientLockBlock.FACING, dir));
                    }
                    case 2 -> placeBlock(w, xx, y + 2 + b, zz, 15, cell);
                    case 9 -> placeBlock(w, xx, y + 2 + b, zz, 17, cell);
                    default -> {
                    }
                }
            }
        }
    }

    /** A sala da chave: o capitel com a tábua rúnica boiando em cima, guardada por dois a quatro guardiões. */
    static void keyRoom(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        box(w, x, z, y, cell, 13);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        if (c > 3 && c < 9 && (a == 8 || b == 8) || c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
                            if (a != 8 && b != 8 || c != 6) placeBlock(w, x + a, y + c, z + b, 19, cell);
                        } else {
                            placeBlock(w, x + a, y + c, z + b, 18, cell);
                        }
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                placeBlock(w, x + a, y - 1, z + b, 1, cell);
                placeBlock(w, x + a, y, z + b, 8, cell);
                placeBlock(w, x + a, y + 1, z + b, 2, cell);
                placeBlock(w, x + a, y + 13, z + b, 1, cell);
                placeBlock(w, x + a, y + 12, z + b, 8, cell);
                placeBlock(w, x + a, y + 11, z + b, 2, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) placeBlock(w, x + a, y + 1 + g, z + b, 2, cell);
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) placeBlock(w, x + a, y + 11 - g, z + b, 2, cell);
                }
            }
        }
        skirting(w, x, z, y, cell, false);
        generateConnections(w, cx, cz, y, cell, 3, true);
        w.set(x + 8, y + 2, z + 8, MazeBlocks.eldritch(3));
        var tablet = new PermanentItemEntity(w.level.getLevel(), x + 8.5, y + 3.5, z + 8.5, new ItemStack(TCItems.RUNED_TABLET));
        tablet.setDeltaMovement(0.0, 0.0, 0.0);
        w.level.addFreshEntity(tablet);
        Difficulty difficulty = w.level.getLevel().getDifficulty();
        int zz = 2 + (difficulty == Difficulty.HARD ? 2 : difficulty == Difficulty.NORMAL ? 1 : 0);
        for (int qq = 0; qq < zz; qq++) {
            var eg = TCEntities.ELDRITCH_GUARDIAN.create(w.level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (eg == null) continue;
            double i1 = x + 8.5 + Mth.nextInt(w.rand, 1, 3) * Mth.nextInt(w.rand, -1, 1);
            double k1 = z + 8.5 + Mth.nextInt(w.rand, 1, 3) * Mth.nextInt(w.rand, -1, 1);
            eg.setPos(i1, y + 2, k1);
            eg.finalizeSpawn(w.level, w.level.getCurrentDifficultyAt(eg.blockPosition()), EntitySpawnReason.STRUCTURE, null);
            eg.setHomeTo(new net.minecraft.core.BlockPos(x + 8, y + 2, z + 8), 16);
            if (qq == 0 && zz >= 4) Champions.makeChampion(eg, true);
            w.level.addFreshEntity(eg);
        }
    }

    /** O ninho: tudo incrustado, cristais estranhos pendurados, dois montes no meio, urnas e caixotes pelo chão. */
    static void nestRoom(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        var random = w.rand;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 11; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) placeBlock(w, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 10; c++) {
                    if (ring(a, b, c, cell)) placeBlock(w, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 9; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) placeBlock(w, x + a, y + c, z + b, 21, cell);
                    if ((a == 4 && !cell.west || a == 12 && !cell.east || b == 4 && !cell.north || b == 12 && !cell.south) && random.nextBoolean()) {
                        placeBlock(w, x + a, y + c, z + b, 21, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                placeBlock(w, x + a, y - 1, z + b, 1, cell);
                placeBlock(w, x + a, y, z + b, 8, cell);
                placeBlock(w, x + a, y + 1, z + b, 21, cell);
                placeBlock(w, x + a, y + 11, z + b, 1, cell);
                placeBlock(w, x + a, y + 10, z + b, 8, cell);
                placeBlock(w, x + a, y + 9, z + b, 21, cell);
                if (random.nextBoolean()) {
                    placeBlock(w, x + a, y + 8, z + b, 21, cell);
                } else if (random.nextBoolean() && w.isAir(x + a, y + 8, z + b)) {
                    w.set(x + a, y + 8, z + b, TCBlocks.STRANGE_CRYSTALS.defaultBlockState().setValue(StrangeCrystalBlock.FACING, Direction.DOWN));
                }
            }
        }
        placeBlock(w, x + 8, y + 2, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 3, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 4, z + 8, 21, cell);
        placeBlock(w, x + 7, y + 2, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 2, z + 7, 21, cell);
        placeBlock(w, x + 9, y + 2, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 2, z + 9, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 7, y + 3, z + 8, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 8, y + 3, z + 7, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 9, y + 3, z + 8, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 8, y + 3, z + 9, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 8, y + 5, z + 8, 7, cell);
        placeBlock(w, x + 8, y + 8, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 7, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 6, z + 8, 21, cell);
        placeBlock(w, x + 7, y + 8, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 8, z + 7, 21, cell);
        placeBlock(w, x + 9, y + 8, z + 8, 21, cell);
        placeBlock(w, x + 8, y + 8, z + 9, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 7, y + 7, z + 8, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 8, y + 7, z + 7, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 9, y + 7, z + 8, 21, cell);
        if (random.nextBoolean()) placeBlock(w, x + 8, y + 7, z + 9, 21, cell);
        generateConnections(w, cx, cz, y, cell, 3, true);
        for (int a = -5; a <= 5; a++) {
            for (int b = -5; b <= 5; b++) {
                if (random.nextFloat() < 0.15f && w.isAir(x + 8 + a, y + 2, z + 8 + b)) {
                    float rr = random.nextFloat();
                    int md = rr < 0.15f ? 2 : rr < 0.4f ? 1 : 0;
                    w.set(x + 8 + a, y + 2, z + 8 + b, MazeBlocks.loot(random.nextFloat() < 0.2f, md));
                }
            }
        }
    }

    /** A biblioteca: pedra antiga, pedestais nos cantos com pedras de glifos entre lajes, e uma coluna delas no meio. */
    static void libraryRoom(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        box(w, x, z, y, cell, 13);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) placeBlock(w, x + a, y + c, z + b, 2, cell);
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                placeBlock(w, x + a, y - 1, z + b, 1, cell);
                placeBlock(w, x + a, y, z + b, 8, cell);
                placeBlock(w, x + a, y + 1, z + b, 2, cell);
                placeBlock(w, x + a, y + 12, z + b, 1, cell);
                placeBlock(w, x + a, y + 11, z + b, 8, cell);
                placeBlock(w, x + a, y + 10, z + b, 2, cell);
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    if (a <= 5 && b <= 5 || a <= 5 && b >= 11 || a >= 11 && b <= 5 || a >= 11 && b >= 11) {
                        placeBlock(w, x + a, y + 2, z + b, 2, cell);
                        placeBlock(w, x + a, y + 9, z + b, 2, cell);
                    }
                    if (a == 5 && b == 5 || a == 5 && b == 11 || a == 11 && b == 5 || a == 11 && b == 11) {
                        w.set(x + a, y + 3, z + b, MazeBlocks.cosmetic(15));
                        w.set(x + a, y + 8, z + b, MazeBlocks.cosmetic(15));
                    }
                }
            }
        }
        skirting(w, x, z, y, cell, true);
        for (int[] c : new int[][]{{5, 5}, {5, 11}, {11, 5}, {11, 11}}) {
            w.set(x + c[0], y + 4, z + c[1], MazeBlocks.eldritch(5));
            w.set(x + c[0], y + 5, z + c[1], MazeBlocks.slab(1));
        }
        for (int[] c : new int[][]{{5, 5}, {5, 11}, {11, 5}, {11, 11}}) {
            w.set(x + c[0], y + 7, z + c[1], MazeBlocks.eldritch(5));
            w.set(x + c[0], y + 6, z + c[1], MazeBlocks.slab(9));
        }
        w.set(x + 8, y + 2, z + 8, MazeBlocks.cosmetic(15));
        w.set(x + 8, y + 3, z + 8, MazeBlocks.eldritch(5));
        w.set(x + 8, y + 4, z + 8, MazeBlocks.slab(1));
        w.set(x + 8, y + 9, z + 8, MazeBlocks.cosmetic(15));
        w.set(x + 8, y + 8, z + 8, MazeBlocks.eldritch(5));
        w.set(x + 8, y + 7, z + 8, MazeBlocks.slab(9));
        generateConnections(w, cx, cz, y, cell, 3, true);
    }

    /** A parede do nada entre a casca de rocha-mãe e a sala, aberta nas saídas até a altura dez. */
    private static boolean ring(int a, int b, int c, Cell cell) {
        return (a == 2 || a == 14 || b == 2 || b == 14)
                && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10)
                && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10)
                && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10);
    }

    /** A casca de rocha-mãe (altura treze) e a parede do nada (altura doze) das salas de treze por treze. */
    private static void box(MazeWorld w, int x, int z, int y, Cell cell, int height) {
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < height; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) placeBlock(w, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < height - 1; c++) {
                    if (ring(a, b, c, cell)) placeBlock(w, x + a, y + c, z + b, 8, cell);
                }
            }
        }
    }

    /** As escadas-rodapé das quatro paredes (e, na biblioteca, as de cima também, na mesma volta). */
    private static void skirting(MazeWorld w, int x, int z, int y, Cell cell, boolean top) {
        for (int g = 0; g < 5; g++) {
            placeBlock(w, x + 6 + g, y + 2, z + 4, 10, Direction.NORTH, cell);
            placeBlock(w, x + 6 + g, y + 2, z + 12, 10, Direction.SOUTH, cell);
            placeBlock(w, x + 12, y + 2, z + 6 + g, 10, Direction.EAST, cell);
            placeBlock(w, x + 4, y + 2, z + 6 + g, 10, Direction.WEST, cell);
            if (top) {
                placeBlock(w, x + 6 + g, y + 9, z + 4, 11, Direction.NORTH, cell);
                placeBlock(w, x + 6 + g, y + 9, z + 12, 11, Direction.SOUTH, cell);
                placeBlock(w, x + 12, y + 9, z + 6 + g, 11, Direction.EAST, cell);
                placeBlock(w, x + 4, y + 9, z + 6 + g, 11, Direction.WEST, cell);
            }
        }
    }
}
