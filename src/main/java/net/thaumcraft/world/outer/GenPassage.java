package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;

/**
 * O corredor comum do labirinto: o {@code GenPassage.generateDefaultPassage} da 4.2.3.5. Uma sala de sete por sete com
 * as passagens das saídas e as escadas-rodapé; na encruzilhada, às vezes, a pedra incrustada luminosa no chão e no teto.
 * Os enfeites: pedras rúnicas (11), a sala tomada pela crosta (12), a mácula (13) e as teias com o gerador de aranhas da
 * mente (14).
 */
final class GenPassage extends GenCommon {
    private GenPassage() {
    }

    private static int wall(MazeWorld w, Cell cell) {
        return cell.feature == 11 && w.rand.nextInt(3) == 0 ? 20 : 2;
    }

    static void generateDefaultPassage(MazeWorld w, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        var random = w.rand;
        generateConnections(w, cx, cz, y, cell, 4, false);
        int mod = 0;
        if (cell.north && cell.south && cell.west && cell.east && random.nextBoolean()) mod = 1;
        for (int a = 1; a < 8; a++) {
            for (int h = 1; h < 8; h++) {
                if (a == 4 && h == 4 && mod == 1) {
                    placeBlock(w, x + 4 + a, y + 2, z + 4 + h, 7, cell);
                    placeBlock(w, x + 4 + a, y + 8, z + 4 + h, 7, cell);
                } else {
                    placeBlock(w, x + 4 + a, y + 2, z + 4 + h, wall(w, cell), cell);
                    placeBlock(w, x + 4 + a, y + 8, z + 4 + h, wall(w, cell), cell);
                }
                placeBlock(w, x + 4 + a, y, z + 4 + h, 1, cell);
                placeBlock(w, x + 4 + a, y + 10, z + 4 + h, 1, cell);
                placeBlock(w, x + 4 + a, y + 1, z + 4 + h, 8, cell);
                placeBlock(w, x + 4 + a, y + 9, z + 4 + h, 8, cell);
            }
        }
        if (cell.north) {
            for (int a = 2 + mod; a < 9 - mod; a++) {
                for (int h = 2 + mod; h < 9 - mod; h++) placeBlock(w, x + 3 + a, y + 10 - h, z + 5, PAT_CONNECT[h][a], Direction.NORTH, cell);
            }
            if (mod == 0) {
                if (cell.west) {
                    placeBlock(w, x + 6, y + 3, z + 6, 3, Direction.EAST, cell);
                    placeBlock(w, x + 6, y + 7, z + 6, 5, Direction.EAST, cell);
                }
                if (cell.east) {
                    placeBlock(w, x + 10, y + 3, z + 6, 3, Direction.EAST, cell);
                    placeBlock(w, x + 10, y + 7, z + 6, 5, Direction.EAST, cell);
                }
            }
        } else {
            for (int a = 1; a < 8; a++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 5, wall(w, cell), cell);
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 4, 8, cell);
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 3, 1, cell);
                    if (h == 7) {
                        placeBlock(w, x + 4 + a, y + 1, z + 4, 1, cell);
                        placeBlock(w, x + 4 + a, y + 9, z + 4, 1, cell);
                    }
                    if (a == 7) {
                        placeBlock(w, x + 4, y + 9 - h, z + 4, 1, cell);
                        placeBlock(w, x + 12, y + 9 - h, z + 4, 1, cell);
                    }
                }
            }
            for (int a = 2; a < 7; a++) {
                placeBlock(w, x + 4 + a, y + 3, z + 6, 3, Direction.EAST, cell);
                placeBlock(w, x + 4 + a, y + 7, z + 6, 5, Direction.EAST, cell);
            }
        }
        if (cell.south) {
            for (int a = 2 + mod; a < 9 - mod; a++) {
                for (int h = 2 + mod; h < 9 - mod; h++) placeBlock(w, x + 3 + a, y + 10 - h, z + 11, PAT_CONNECT[h][a], Direction.SOUTH, cell);
            }
            if (mod == 0) {
                if (cell.west) {
                    placeBlock(w, x + 6, y + 3, z + 10, 4, Direction.EAST, cell);
                    placeBlock(w, x + 6, y + 7, z + 10, 6, Direction.EAST, cell);
                }
                if (cell.east) {
                    placeBlock(w, x + 10, y + 3, z + 10, 4, Direction.EAST, cell);
                    placeBlock(w, x + 10, y + 7, z + 10, 6, Direction.EAST, cell);
                }
            }
        } else {
            for (int a = 1; a < 8; a++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 11, wall(w, cell), cell);
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 12, 8, cell);
                    placeBlock(w, x + 4 + a, y + 9 - h, z + 13, 1, cell);
                    if (h == 7) {
                        placeBlock(w, x + 4 + a, y + 1, z + 12, 1, cell);
                        placeBlock(w, x + 4 + a, y + 9, z + 12, 1, cell);
                    }
                    if (a == 7) {
                        placeBlock(w, x + 4, y + 9 - h, z + 12, 1, cell);
                        placeBlock(w, x + 12, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }
            for (int a = 2; a < 7; a++) {
                placeBlock(w, x + 4 + a, y + 3, z + 10, 4, Direction.EAST, cell);
                placeBlock(w, x + 4 + a, y + 7, z + 10, 6, Direction.EAST, cell);
            }
        }
        if (cell.east) {
            for (int a = 2 + mod; a < 9 - mod; a++) {
                for (int h = 2 + mod; h < 9 - mod; h++) placeBlock(w, x + 11, y + 10 - h, z + 3 + a, PAT_CONNECT[h][a], Direction.EAST, cell);
            }
            if (mod == 0) {
                if (cell.north) {
                    placeBlock(w, x + 10, y + 3, z + 6, 4, Direction.NORTH, cell);
                    placeBlock(w, x + 10, y + 7, z + 6, 6, Direction.NORTH, cell);
                }
                if (cell.south) {
                    placeBlock(w, x + 10, y + 3, z + 10, 4, Direction.NORTH, cell);
                    placeBlock(w, x + 10, y + 7, z + 10, 6, Direction.NORTH, cell);
                }
            }
        } else {
            for (int a = 1; a < 8; a++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(w, x + 11, y + 9 - h, z + 4 + a, wall(w, cell), cell);
                    placeBlock(w, x + 12, y + 9 - h, z + 4 + a, 8, cell);
                    placeBlock(w, x + 13, y + 9 - h, z + 4 + a, 1, cell);
                    if (h == 7) {
                        placeBlock(w, x + 12, y + 1, z + 4 + a, 1, cell);
                        placeBlock(w, x + 12, y + 9, z + 4 + a, 1, cell);
                    }
                    if (a == 7) {
                        placeBlock(w, x + 12, y + 9 - h, z + 4, 1, cell);
                        placeBlock(w, x + 12, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }
            for (int a = 2; a < 7; a++) {
                placeBlock(w, x + 10, y + 3, z + 4 + a, 4, Direction.NORTH, cell);
                placeBlock(w, x + 10, y + 7, z + 4 + a, 6, Direction.NORTH, cell);
            }
        }
        if (cell.west) {
            for (int a = 2 + mod; a < 9 - mod; a++) {
                for (int h = 2 + mod; h < 9 - mod; h++) placeBlock(w, x + 5, y + 10 - h, z + 3 + a, PAT_CONNECT[h][a], Direction.WEST, cell);
            }
            if (mod == 0) {
                if (cell.north) {
                    placeBlock(w, x + 6, y + 3, z + 6, 3, Direction.NORTH, cell);
                    placeBlock(w, x + 6, y + 7, z + 6, 5, Direction.NORTH, cell);
                }
                if (cell.south) {
                    placeBlock(w, x + 6, y + 3, z + 10, 3, Direction.NORTH, cell);
                    placeBlock(w, x + 6, y + 7, z + 10, 5, Direction.NORTH, cell);
                }
            }
        } else {
            for (int a = 1; a < 8; a++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(w, x + 5, y + 9 - h, z + 4 + a, wall(w, cell), cell);
                    placeBlock(w, x + 4, y + 9 - h, z + 4 + a, 8, cell);
                    placeBlock(w, x + 3, y + 9 - h, z + 4 + a, 1, cell);
                    if (h == 7) {
                        placeBlock(w, x + 4, y + 1, z + 4 + a, 1, cell);
                        placeBlock(w, x + 4, y + 9, z + 4 + a, 1, cell);
                    }
                    if (a == 7) {
                        placeBlock(w, x + 4, y + 9 - h, z + 4, 1, cell);
                        placeBlock(w, x + 4, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }
            for (int a = 2; a < 7; a++) {
                placeBlock(w, x + 6, y + 3, z + 4 + a, 3, Direction.NORTH, cell);
                placeBlock(w, x + 6, y + 7, z + 4 + a, 5, Direction.NORTH, cell);
            }
        }
        if (mod == 1) {
            placeBlock(w, x + 5, y + 3, z + 5, 3, Direction.EAST, cell);
            placeBlock(w, x + 5, y + 7, z + 5, 5, Direction.EAST, cell);
            placeBlock(w, x + 5, y + 3, z + 6, 3, Direction.NORTH, cell);
            placeBlock(w, x + 5, y + 7, z + 6, 5, Direction.NORTH, cell);
            placeBlock(w, x + 11, y + 3, z + 5, 3, Direction.EAST, cell);
            placeBlock(w, x + 11, y + 7, z + 5, 5, Direction.EAST, cell);
            placeBlock(w, x + 11, y + 3, z + 6, 4, Direction.NORTH, cell);
            placeBlock(w, x + 11, y + 7, z + 6, 6, Direction.NORTH, cell);
            placeBlock(w, x + 5, y + 3, z + 11, 3, Direction.NORTH, cell);
            placeBlock(w, x + 5, y + 7, z + 11, 5, Direction.NORTH, cell);
            placeBlock(w, x + 6, y + 3, z + 11, 4, Direction.EAST, cell);
            placeBlock(w, x + 6, y + 7, z + 11, 6, Direction.EAST, cell);
            placeBlock(w, x + 11, y + 3, z + 11, 4, Direction.NORTH, cell);
            placeBlock(w, x + 11, y + 7, z + 11, 6, Direction.NORTH, cell);
            placeBlock(w, x + 10, y + 3, z + 11, 4, Direction.EAST, cell);
            placeBlock(w, x + 10, y + 7, z + 11, 6, Direction.EAST, cell);
        }
        if (cell.feature == 12) {
            for (int a = -4; a <= 4; a++) {
                for (int h = -4; h < 5; h++) {
                    for (int j = -4; j <= 4; j++) {
                        int px = x + 8 + a, py = y + 4 + h, pz = z + 8 + j;
                        var here = w.get(px, py, pz);
                        if ((here.isAir() || isCosmetic(here) || here.is(TCBlocks.ANCIENT_STONE_STAIRS)) && random.nextBoolean()) {
                            placeBlock(w, px, py, pz, 21, cell);
                        }
                    }
                }
            }
        }
        if (cell.feature == 13) {
            var taint = w.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getOrThrow(net.thaumcraft.world.TCBiomes.TAINTED_LAND);
            for (int a = -4; a <= 4; a++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -4; j <= 4; j++) {
                        int px = x + 8 + a, py = y + 4 + h, pz = z + 8 + j;
                        if (w.isAir(px, py, pz) && w.nextToSolid(px, py, pz)) {
                            if (random.nextInt(3) != 0) {
                                w.set(px, py, pz, TCBlocks.TAINT_FIBRES.defaultBlockState()
                                        .setValue(net.thaumcraft.block.TaintFibreBlock.KIND, random.nextInt(4) == 0 ? 1 : 0));
                            }
                            w.biome(px, pz, taint);
                        }
                    }
                }
            }
        }
        if (cell.feature == 14) {
            for (int a = -3; a <= 3; a++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -3; j <= 3; j++) {
                        if (w.isAir(x + 8 + a, y + 4 + h, z + 8 + j) && random.nextFloat() < 0.35f) {
                            w.set(x + 8 + a, y + 4 + h, z + 8 + j, Blocks.COBWEB.defaultBlockState());
                        }
                    }
                }
            }
            w.set(x + 8, y + 4, z + 8, Blocks.SPAWNER.defaultBlockState());
            if (w.blockEntity(x + 8, y + 4, z + 8) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(TCEntities.MIND_SPIDER, random);
            }
        }
    }

    /** O {@code blockCosmeticSolid} de então, qualquer número. */
    static boolean isCosmetic(net.minecraft.world.level.block.state.BlockState s) {
        return s.is(TCBlocks.ANCIENT_STONE) || s.is(TCBlocks.ANCIENT_ROCK) || s.is(TCBlocks.ANCIENT_STONE_NOSPAWN)
                || s.is(TCBlocks.CRUSTED_STONE) || s.is(TCBlocks.ANCIENT_STONE_PEDESTAL) || s.is(TCBlocks.OBSIDIAN_TOTEM)
                || s.is(TCBlocks.OBSIDIAN_TILE);
    }
}
