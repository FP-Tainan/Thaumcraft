package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.thaumcraft.block.MagicalLeavesBlock;
import net.thaumcraft.registry.TCBlocks;

import java.util.Random;

/**
 * A grande-madeira: o {@code WorldGenGreatwoodTrees} da 4.2.3.5, descompilado e traduzido conta por conta.
 *
 * <p>É o gerador do carvalho grande do jogo antigo com tronco de dois por dois, feito duas vezes: uma copa
 * de baixo e, em cima dela, outra mais larga. Uma em oito nasce com uma toca de aranha-das-cavernas debaixo:
 * um gerador de monstros, teias pela copa e um baú de masmorra mais embaixo.
 */
public final class GreatwoodTree {
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

    private final Random rand = new Random();
    private final LevelAccessor level;
    private final int flags;
    private final TreeLeaves leaves = new TreeLeaves();
    private final int[] basePos = {0, 0, 0};
    private int heightLimit;
    private int height;
    private final double heightAttenuation = 0.618;
    private final double branchSlope = 0.38;
    private double scaleWidth = 1.2;
    private final double leafDensity = 0.9;
    private final int trunkSize = 2;
    private final int heightLimitLimit = 11;
    private final int leafDistanceLimit = 4;
    private int[][] leafNodes;

    private GreatwoodTree(LevelAccessor level, boolean worldgen) {
        this.level = level;
        this.flags = worldgen ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
    }

    /**
     * O {@code generate} do original.
     *
     * @param worldgen se é o mundo nascendo (sem avisar vizinhos) ou uma muda crescendo
     * @param spiders  se vem com a toca de aranhas
     */
    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos pos, boolean worldgen,
                                   boolean spiders) {
        return new GreatwoodTree(level, worldgen).run(random, pos.getX(), pos.getY(), pos.getZ(), spiders);
    }

    private boolean run(RandomSource random, int x0, int y0, int z0, boolean spiders) {
        this.rand.setSeed(random.nextLong());
        this.basePos[0] = x0;
        this.basePos[1] = y0;
        this.basePos[2] = z0;
        if (this.heightLimit == 0) this.heightLimit = this.heightLimitLimit + this.rand.nextInt(this.heightLimitLimit);

        boolean valid = false;
        search:
        for (int a = -1; a < 2; a++) {
            next:
            for (int b = -1; b < 2; b++) {
                for (int i = 0; i < this.trunkSize; i++) {
                    for (int j = 0; j < this.trunkSize; j++) {
                        if (!this.validTreeLocation(i + a, j + b)) continue next;
                    }
                }
                valid = true;
                this.basePos[0] += a;
                this.basePos[2] += b;
                break search;
            }
        }
        if (!valid) return false;

        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.generateTrunk();
        // a segunda copa, mais larga, começa onde a primeira acaba — e volta à posição pedida, como no original
        this.scaleWidth = 1.66;
        this.basePos[0] = x0;
        this.basePos[1] = y0 + this.height;
        this.basePos[2] = z0;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.generateTrunk();
        this.leaves.settle(this.level, this.flags);

        if (spiders) this.spiderDen(random, x0, y0, z0);
        return true;
    }

    private void spiderDen(RandomSource random, int x, int y, int z) {
        BlockPos spawner = new BlockPos(x, y - 1, z);
        this.level.setBlock(spawner, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
        if (this.level.getBlockEntity(spawner) instanceof SpawnerBlockEntity den) {
            den.setEntityId(net.minecraft.world.entity.EntityTypes.CAVE_SPIDER, random);
            for (int a = 0; a < 50; a++) {
                BlockPos web = new BlockPos(x - 7 + random.nextInt(14), y + random.nextInt(10), z - 7 + random.nextInt(14));
                if (this.level.isEmptyBlock(web) && this.touchesTree(web)) {
                    this.level.setBlock(web, Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            BlockPos chest = new BlockPos(x, y - 2, z);
            this.level.setBlock(chest, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(this.level, random, chest,
                    BuiltInLootTables.SIMPLE_DUNGEON);
        }
    }

    /** O {@code BlockUtils.isBlockTouching} com folha ou tora mágica. */
    private boolean touchesTree(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState state = this.level.getBlockState(pos.relative(dir));
            if (state.getBlock() instanceof MagicalLeavesBlock || state.is(TCBlocks.GREATWOOD_LOG)
                    || state.is(TCBlocks.SILVERWOOD_LOG) || state.is(TCBlocks.SILVERWOOD_KNOT)) {
                return true;
            }
        }
        return false;
    }

    private void generateLeafNodeList() {
        this.height = (int) (this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) this.height = this.heightLimit - 1;

        int perLayer = (int) (1.382 + Math.pow(this.leafDensity * this.heightLimit / 13.0, 2.0));
        if (perLayer < 1) perLayer = 1;

        int[][] nodes = new int[perLayer * this.heightLimit][4];
        int y = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
        int count = 1;
        int trunkTop = this.basePos[1] + this.height;
        int layer = y - this.basePos[1];
        nodes[0][0] = this.basePos[0];
        nodes[0][1] = y;
        nodes[0][2] = this.basePos[2];
        nodes[0][3] = trunkTop;
        y--;

        while (layer >= 0) {
            int made = 0;
            float size = this.layerSize(layer);
            if (size < 0.0f) {
                y--;
                layer--;
            } else {
                double half = 0.5;
                while (made < perLayer) {
                    double reach = this.scaleWidth * size * (this.rand.nextFloat() + 0.328);
                    double angle = this.rand.nextFloat() * 2.0 * Math.PI;
                    int nx = Mth.floor(reach * Math.sin(angle) + this.basePos[0] + half);
                    int nz = Mth.floor(reach * Math.cos(angle) + this.basePos[2] + half);
                    int[] node = {nx, y, nz};
                    int[] above = {nx, y + this.leafDistanceLimit, nz};
                    if (this.checkBlockLine(node, above) == -1) {
                        int[] base = {this.basePos[0], this.basePos[1], this.basePos[2]};
                        double dist = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - node[0]), 2.0)
                                + Math.pow(Math.abs(this.basePos[2] - node[2]), 2.0));
                        double drop = dist * this.branchSlope;
                        if (node[1] - drop > trunkTop) {
                            base[1] = trunkTop;
                        } else {
                            base[1] = (int) (node[1] - drop);
                        }
                        if (this.checkBlockLine(base, node) == -1) {
                            nodes[count][0] = nx;
                            nodes[count][1] = y;
                            nodes[count][2] = nz;
                            nodes[count][3] = base[1];
                            count++;
                        }
                    }
                    made++;
                }
                y--;
                layer--;
            }
        }

        this.leafNodes = new int[count][4];
        System.arraycopy(nodes, 0, this.leafNodes, 0, count);
    }

    private void genTreeLayer(int x, int y, int z, float radius, byte axis) {
        int r = (int) (radius + 0.618);
        byte a1 = OTHER_COORD_PAIRS[axis];
        byte a2 = OTHER_COORD_PAIRS[axis + 3];
        int[] center = {x, y, z};
        int[] at = {0, 0, 0};
        at[axis] = center[axis];
        for (int i = -r; i <= r; i++) {
            at[a1] = center[a1] + i;
            for (int j = -r; j <= r; j++) {
                double d = Math.pow(Math.abs(i) + 0.5, 2.0) + Math.pow(Math.abs(j) + 0.5, 2.0);
                if (d > radius * radius) continue;
                at[a2] = center[a2] + j;
                BlockPos pos = new BlockPos(at[0], at[1], at[2]);
                BlockState state = this.level.getBlockState(pos);
                if (state.isAir() || state.getBlock() instanceof MagicalLeavesBlock) {
                    this.leaves.place(this.level, pos, TCBlocks.GREATWOOD_LEAVES.defaultBlockState(), this.flags);
                }
            }
        }
    }

    private float layerSize(int layer) {
        if (layer < this.heightLimit * 0.3) return -1.618f;
        float half = this.heightLimit / 2.0f;
        float off = this.heightLimit / 2.0f - layer;
        float size;
        if (off == 0.0f) {
            size = half;
        } else if (Math.abs(off) >= half) {
            size = 0.0f;
        } else {
            size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0) - Math.pow(Math.abs(off), 2.0));
        }
        return size * 0.5f;
    }

    private float leafSize(int layer) {
        if (layer < 0 || layer >= this.leafDistanceLimit) return -1.0f;
        return layer != 0 && layer != this.leafDistanceLimit - 1 ? 3.0f : 2.0f;
    }

    private void generateLeafNode(int x, int y, int z) {
        for (int yy = y; yy < y + this.leafDistanceLimit; yy++) {
            this.genTreeLayer(x, yy, z, this.leafSize(yy - y), (byte) 1);
        }
    }

    private void placeBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        byte major = 0;
        for (byte i = 0; i < 3; i++) {
            delta[i] = to[i] - from[i];
            if (Math.abs(delta[i]) > Math.abs(delta[major])) major = i;
        }
        if (delta[major] == 0) return;
        byte a1 = OTHER_COORD_PAIRS[major];
        byte a2 = OTHER_COORD_PAIRS[major + 3];
        byte step = (byte) (delta[major] > 0 ? 1 : -1);
        double s1 = (double) delta[a1] / delta[major];
        double s2 = (double) delta[a2] / delta[major];
        int[] at = {0, 0, 0};
        for (int i = 0, end = delta[major] + step; i != end; i += step) {
            at[major] = Mth.floor(from[major] + i + 0.5);
            at[a1] = Mth.floor(from[a1] + i * s1 + 0.5);
            at[a2] = Mth.floor(from[a2] + i * s2 + 0.5);
            // o deitado do tronco: de pé, ou ao longo do eixo em que o galho mais anda
            Direction.Axis axis = Direction.Axis.Y;
            int dx = Math.abs(at[0] - from[0]);
            int dz = Math.abs(at[2] - from[2]);
            int most = Math.max(dx, dz);
            if (most > 0) {
                if (dx == most) {
                    axis = Direction.Axis.X;
                } else if (dz == most) {
                    axis = Direction.Axis.Z;
                }
            }
            BlockPos pos = new BlockPos(at[0], at[1], at[2]);
            this.level.setBlock(pos, TCBlocks.GREATWOOD_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                    this.flags);
            this.leaves.forget(pos);
        }
    }

    private void generateLeaves() {
        for (int[] node : this.leafNodes) this.generateLeafNode(node[0], node[1], node[2]);
    }

    private boolean leafNodeNeedsBase(int y) {
        return y >= this.heightLimit * 0.2;
    }

    private void generateTrunk() {
        int x = this.basePos[0];
        int y0 = this.basePos[1];
        int y1 = this.basePos[1] + this.height;
        int z = this.basePos[2];
        int[] from = {x, y0, z};
        int[] to = {x, y1, z};
        this.placeBlockLine(from, to);
        if (this.trunkSize == 2) {
            from[0]++;
            to[0]++;
            this.placeBlockLine(from, to);
            from[2]++;
            to[2]++;
            this.placeBlockLine(from, to);
            from[0]--;
            to[0]--;
            this.placeBlockLine(from, to);
        }
    }

    private void generateLeafNodeBases() {
        int[] base = {this.basePos[0], this.basePos[1], this.basePos[2]};
        for (int[] node : this.leafNodes) {
            int[] tip = {node[0], node[1], node[2]};
            base[1] = node[3];
            if (this.leafNodeNeedsBase(base[1] - this.basePos[1])) this.placeBlockLine(base, tip);
        }
    }

    private int checkBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        byte major = 0;
        for (byte i = 0; i < 3; i++) {
            delta[i] = to[i] - from[i];
            if (Math.abs(delta[i]) > Math.abs(delta[major])) major = i;
        }
        if (delta[major] == 0) return -1;
        byte a1 = OTHER_COORD_PAIRS[major];
        byte a2 = OTHER_COORD_PAIRS[major + 3];
        byte step = (byte) (delta[major] > 0 ? 1 : -1);
        double s1 = (double) delta[a1] / delta[major];
        double s2 = (double) delta[a2] / delta[major];
        int[] at = {0, 0, 0};
        int i = 0;
        int end = delta[major] + step;
        for (; i != end; i += step) {
            at[major] = from[major] + i;
            at[a1] = Mth.floor(from[a1] + i * s1);
            at[a2] = Mth.floor(from[a2] + i * s2);
            BlockState state = this.level.getBlockState(new BlockPos(at[0], at[1], at[2]));
            if (!state.isAir() && !(state.getBlock() instanceof MagicalLeavesBlock)) break;
        }
        return i == end ? -1 : Math.abs(i);
    }

    private boolean validTreeLocation(int x, int z) {
        int[] from = {this.basePos[0] + x, this.basePos[1], this.basePos[2] + z};
        int[] to = {this.basePos[0] + x, this.basePos[1] + this.heightLimit - 1, this.basePos[2] + z};
        BlockState soil = this.level.getBlockState(new BlockPos(this.basePos[0] + x, this.basePos[1] - 1, this.basePos[2] + z));
        if (!TreeLeaves.isSoil(soil)) return false;
        int free = this.checkBlockLine(from, to);
        if (free == -1) return true;
        if (free < 6) return false;
        this.heightLimit = free;
        return true;
    }
}
