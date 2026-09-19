package net.thaumcraft.world.outer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.eldritch.CrabSpawnerBlockEntity;
import net.thaumcraft.registry.TCBlocks;


/**
 * O que as salas do labirinto têm em comum: o {@code GenCommon} da 4.2.3.5. O {@code placeBlock} traduz os números das
 * plantas (1 rocha-mãe, 2 pedra antiga, 8 o nada, 9 ar, 10/11 escadas, 15 porta antiga, 16 fechadura, 17 o intransponível,
 * 18 rocha antiga, 19 pedra lisa, 20 pedra rúnica, 21 incrustada) e anota onde podem ir enfeites, aberturas de caranguejo e
 * urnas; o {@code processDecorations} os põe no fim, onde couberem; e o {@code generateConnections} abre as passagens
 * para as casas vizinhas.
 */
public class GenCommon {

    static final int[][] PAT_CONNECT = {
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
    };

    static void placeBlock(MazeWorld w, int x, int y, int z, int b, Cell cell) {
        placeBlock(w, x, y, z, b, null, cell);
    }

    static void placeBlock(MazeWorld w, int x, int y, int z, int b, Direction dir, Cell cell) {
        BlockState block = null;
        boolean crust = false;
        var rand = w.rand;
        switch (b) {
            case 1 -> {
                if (w.isAir(x, y, z)) block = MazeWorld.BEDROCK;
            }
            case 2 -> {
                if (cell.feature == 7 && rand.nextInt(3) == 0) {
                    crust = true;
                } else if (!w.is(x, y, z, TCBlocks.ELDRITCH_NOTHING)) {
                    if (rand.nextInt(25) == 0) {
                        boolean crab = cell.feature == 7 || rand.nextInt(50) == 0;
                        if ((!crab || cell.feature != 0) && (!crab || cell.feature != 7)) w.decoCommon.add(new BlockPos(x, y, z));
                        else w.crabSpawner.add(new BlockPos(x, y, z));
                    }
                    block = MazeBlocks.cosmetic(11);
                }
            }
            case 3 -> {
                if (rand.nextFloat() < 0.005) w.decoUrn.add(new BlockPos(x, y, z));
                block = MazeBlocks.stairs(dir == Direction.NORTH || dir == Direction.SOUTH ? 1 : dir == Direction.WEST || dir == Direction.EAST ? 3 : 0);
            }
            case 4 -> {
                if (rand.nextFloat() < 0.005) w.decoUrn.add(new BlockPos(x, y, z));
                block = MazeBlocks.stairs(dir == Direction.NORTH || dir == Direction.SOUTH ? 0 : dir == Direction.WEST || dir == Direction.EAST ? 2 : 0);
            }
            case 5 -> block = MazeBlocks.stairs(dir == Direction.NORTH || dir == Direction.SOUTH ? 5 : dir == Direction.WEST || dir == Direction.EAST ? 7 : 0);
            case 6 -> block = MazeBlocks.stairs(dir == Direction.NORTH || dir == Direction.SOUTH ? 4 : dir == Direction.WEST || dir == Direction.EAST ? 6 : 0);
            case 7 -> block = MazeBlocks.eldritch(4);
            case 8 -> block = TCBlocks.ELDRITCH_NOTHING.defaultBlockState();
            case 9 -> {
                block = MazeWorld.AIR;
                BlockPos p = new BlockPos(x, y, z);
                w.decoCommon.remove(p);
                w.crabSpawner.remove(p);
                w.decoUrn.remove(p);
            }
            case 10 -> block = MazeBlocks.stairs(switch (dir) {
                case NORTH -> 3;
                case SOUTH -> 2;
                case EAST -> 0;
                case WEST -> 1;
                default -> 0;
            });
            case 11 -> block = MazeBlocks.stairs(switch (dir) {
                case NORTH -> 7;
                case SOUTH -> 6;
                case EAST -> 4;
                case WEST -> 5;
                default -> 0;
            });
            case 15, 16 -> {
                block = MazeBlocks.eldritch(b == 15 ? 7 : 8);
                BlockPos p = new BlockPos(x, y, z);
                w.decoCommon.remove(p);
                w.crabSpawner.remove(p);
                w.decoUrn.remove(p);
            }
            case 17 -> block = TCBlocks.IMPASSABLE.defaultBlockState();
            case 18 -> {
                if (!w.is(x, y, z, TCBlocks.ELDRITCH_NOTHING)) block = MazeBlocks.cosmetic(12);
            }
            case 19 -> {
                if (!w.is(x, y, z, TCBlocks.ELDRITCH_NOTHING)) block = MazeBlocks.cosmetic(13);
            }
            case 20 -> {
                if (!w.is(x, y, z, TCBlocks.ELDRITCH_NOTHING)) block = MazeBlocks.eldritch(10);
            }
            case 21 -> crust = true;
            case 99 -> block = MazeWorld.BEDROCK;
            default -> {
            }
        }
        // a pedra incrustada (o 21, e o 2 do ninho uma vez em três)
        if (crust && !w.is(x, y, z, TCBlocks.ELDRITCH_NOTHING)) {
            block = MazeBlocks.cosmetic(14);
            if (rand.nextInt(25) == 0) {
                block = MazeBlocks.eldritch(4);
            } else if (rand.nextInt(25) == 0) {
                boolean crab = cell.feature == 7 || (cell.feature == 12 && rand.nextBoolean()) || rand.nextInt(25) == 0;
                if (crab && (cell.feature == 0 || cell.feature == 7 || cell.feature == 12)) w.crabSpawner.add(new BlockPos(x, y, z));
            }
        }
        if (block != null) w.set(x, y, z, block);
    }

    public static void genObelisk(MazeWorld w, int x, int y, int z) {
        w.set(x, y, z, MazeBlocks.eldritch(1));
        for (int i = 1; i <= 4; i++) w.set(x, y + i, z, MazeBlocks.eldritch(2));
    }

    static void processDecorations(MazeWorld w) {
        var rand = w.rand;
        for (BlockPos cc : w.decoUrn) {
            if (w.isAir(cc.getX(), cc.getY() + 1, cc.getZ())) {
                w.set(cc.getX(), cc.getY(), cc.getZ(), MazeBlocks.cosmetic(15));
                float rr = rand.nextFloat();
                w.set(cc.getX(), cc.getY() + 1, cc.getZ(), MazeBlocks.loot(false, rr < 0.025f ? 2 : rr < 0.1f ? 1 : 0));
            }
        }
        for (BlockPos cc : w.decoCommon) {
            int x = cc.getX(), y = cc.getY(), z = cc.getZ();
            int exp = w.exposedSides(x, y, z);
            if (exp > 0 && (exp == 1 || !bedrockShowing(w, x, y, z)) && !w.nextToEldritch(x, y, z)) {
                int meta = rand.nextInt(3) != 0 ? 4 : rand.nextInt(8) != 0 ? 5 : 10;
                w.set(x, y, z, MazeBlocks.eldritch(meta));
                if (meta == 4 && rand.nextInt(12) == 0) {
                    for (Direction dir : Direction.values()) {
                        if (w.isAir(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ())) {
                            w.set(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ(),
                                    TCBlocks.STRANGE_CRYSTALS.defaultBlockState().setValue(net.thaumcraft.block.eldritch.StrangeCrystalBlock.FACING, dir));
                            break;
                        }
                    }
                }
            }
        }
        for (BlockPos cc : w.crabSpawner) {
            int x = cc.getX(), y = cc.getY(), z = cc.getZ();
            if (w.exposedSides(x, y, z) == 1 && !w.nextToEldritch(x, y, z)) {
                w.set(x, y, z, MazeBlocks.eldritch(9));
                if (w.blockEntity(x, y, z) instanceof CrabSpawnerBlockEntity te) {
                    for (Direction dir : Direction.values()) {
                        if (w.isAir(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ())) {
                            te.setFacing(dir);
                            break;
                        }
                    }
                }
            }
        }
        w.decoCommon.clear();
        w.crabSpawner.clear();
        w.decoUrn.clear();
    }

    static boolean bedrockShowing(MazeWorld w, int x, int y, int z) {
        for (Direction dir : Direction.values()) {
            Direction op = dir.getOpposite();
            if (!w.opaque(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ())) {
                BlockState back = w.get(x + op.getStepX(), y + op.getStepY(), z + op.getStepZ());
                if (back.is(net.minecraft.world.level.block.Blocks.BEDROCK) || back.is(TCBlocks.ELDRITCH_NOTHING)) return true;
            }
        }
        return false;
    }

    private static int low(int d, int depth, boolean tip) {
        return d == depth && tip ? 2 : d == depth - 1 && tip ? 1 : 0;
    }

    private static int high(int d, int depth, boolean tip) {
        return d == depth && tip ? 9 : d == depth - 1 && tip ? 10 : 11;
    }

    static void generateConnections(MazeWorld world, int cx, int cz, int y, Cell cell, int depth, boolean justthetip) {
        int x = cx * 16;
        int z = cz * 16;
        for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            boolean open = switch (side) {
                case NORTH -> cell.north;
                case SOUTH -> cell.south;
                case EAST -> cell.east;
                default -> cell.west;
            };
            if (!open) continue;
            for (int d = 0; d <= depth; d++) {
                for (int w = low(d, depth, justthetip); w < high(d, depth, justthetip); w++) {
                    for (int h = low(d, depth, justthetip); h < high(d, depth, justthetip); h++) {
                        if (d == depth && justthetip && PAT_CONNECT[h][w] == 8) continue;
                        switch (side) {
                            case NORTH -> placeBlock(world, x + 3 + w, y + 10 - h, z + d, PAT_CONNECT[h][w], side, cell);
                            case SOUTH -> placeBlock(world, x + 3 + w, y + 10 - h, z + 16 - d, PAT_CONNECT[h][w], side, cell);
                            case EAST -> placeBlock(world, x + 16 - d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], side, cell);
                            default -> placeBlock(world, x + d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], side, cell);
                        }
                    }
                }
            }
        }
    }
}
