package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.world.TreeLeaves;

import java.util.Random;

/**
 * O amieiro e o espinheiro-alvar: o {@code WorldGenLargeWitchTree} do Witchery.
 *
 * <p>É o carvalho grande do jogo antigo — o mesmo desenho de galhos que a grande-madeira do Thaumcraft usa —,
 * com tronco de um e com os números que cada árvore pede:
 *
 * <ul>
 *   <li><b>amieiro</b>: galhos mais caídos (meio), copa estreita e rala, e até sete de altura;</li>
 *   <li><b>espinheiro-alvar</b>: galhos como os do original, copa larga e cheia, e até nove.</li>
 * </ul>
 *
 * <p>Os números saem do {@code setScale} do jogo antigo, que faz três coisas de uma vez: o teto da altura é doze
 * vezes o primeiro número, a copa ganha uma fileira a mais quando ele passa de meio, e os outros dois são a
 * largura e o cheio da folhagem.
 */
public final class LargeWitchTree {
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

    private final Random rand = new Random();
    private final LevelAccessor level;
    private final int flags;
    private final TreeLeaves leaves = new TreeLeaves();
    private final int[] basePos = {0, 0, 0};

    private final BlockState log;
    private final BlockState leaf;
    private final double heightAttenuation;
    private final double branchSlope;
    private final double scaleWidth;
    private final double leafDensity;
    private final int heightLimitLimit;
    private final int leafDistanceLimit;

    private int heightLimit;
    private int height;
    private int[][] leafNodes;

    private LargeWitchTree(LevelAccessor level, boolean worldgen, Kind kind) {
        this.level = level;
        this.flags = worldgen ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
        this.log = kind.log().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        this.leaf = kind.leaves().defaultBlockState();
        this.heightAttenuation = kind.heightAttenuation();
        this.branchSlope = kind.branchSlope();
        this.scaleWidth = kind.scaleWidth();
        this.leafDensity = kind.leafDensity();
        this.heightLimitLimit = kind.heightLimitLimit();
        this.leafDistanceLimit = kind.leafDistanceLimit();
    }

    /** O jeito de cada uma das duas árvores grandes. */
    public record Kind(Block log, Block leaves, double heightAttenuation, double branchSlope,
                       double scaleWidth, double leafDensity, int heightLimitLimit, int leafDistanceLimit) {
    }

    /** O amieiro: {@code new WorldGenLargeWitchTree(true, 1, 1, 0.5)} com {@code setScale(0.6, 0.5, 0.5)}. */
    public static Kind alder() {
        return new Kind(OccultaBlocks.ALDER_LOG, OccultaBlocks.ALDER_LEAVES, 0.618, 0.5, 0.5, 0.5, 7, 5);
    }

    /** O espinheiro-alvar: {@code new WorldGenLargeWitchTree(true, 2, 2)} com {@code setScale(0.8, 1.2, 1.0)}. */
    public static Kind hawthorn() {
        return new Kind(OccultaBlocks.HAWTHORN_LOG, OccultaBlocks.HAWTHORN_LEAVES, 0.618, 0.381, 1.2, 1.0, 9, 5);
    }

    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos pos, boolean worldgen,
                                   Kind kind) {
        return new LargeWitchTree(level, worldgen, kind).run(random, pos.getX(), pos.getY(), pos.getZ());
    }

    private boolean run(RandomSource random, int x0, int y0, int z0) {
        this.rand.setSeed(random.nextLong());
        this.basePos[0] = x0;
        this.basePos[1] = y0;
        this.basePos[2] = z0;
        this.heightLimit = this.heightLimitLimit + this.rand.nextInt(this.heightLimitLimit);

        if (!this.validTreeLocation(0, 0)) return false;

        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.generateTrunk();
        this.leaves.settle(this.level, this.flags);
        return true;
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
                continue;
            }
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
                    base[1] = node[1] - drop > trunkTop ? trunkTop : (int) (node[1] - drop);
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
                if (state.isAir() || state.is(BlockTags.LEAVES)) {
                    this.leaves.place(this.level, pos, this.leaf, this.flags);
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
            this.level.setBlock(pos, this.log.setValue(RotatedPillarBlock.AXIS, axis), this.flags);
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
        this.placeBlockLine(new int[]{x, y0, z}, new int[]{x, y1, z});
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
            if (!state.isAir() && !state.is(BlockTags.LEAVES)) break;
        }
        return i == end ? -1 : Math.abs(i);
    }

    private boolean validTreeLocation(int x, int z) {
        int[] from = {this.basePos[0] + x, this.basePos[1], this.basePos[2] + z};
        int[] to = {this.basePos[0] + x, this.basePos[1] + this.heightLimit - 1, this.basePos[2] + z};
        BlockState soil = this.level.getBlockState(
                new BlockPos(this.basePos[0] + x, this.basePos[1] - 1, this.basePos[2] + z));
        if (!TreeLeaves.isSoil(soil)) return false;
        int free = this.checkBlockLine(from, to);
        if (free == -1) return true;
        if (free < 6) return false;
        this.heightLimit = free;
        return true;
    }
}
