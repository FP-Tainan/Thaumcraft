package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.WardedBlockEntity;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;

/**
 * O bloco protegido: o {@code BlockWardedRenderer} e o {@code TileWardedRenderer} da 4.2.3.5.
 *
 * <p>Por baixo, o bloco guardado, desenhado como se ainda estivesse ali. Por cima, só para quem segura uma
 * varinha com o foco de Proteção, as runas do vidro protegido em cada face que não encosta noutro bloco
 * protegido — com as 47 texturas conectadas do original, escolhidas pelos oito vizinhos de cada face que têm
 * o mesmo dono. As runas do dono pulsam douradas e meio fortes; as dos outros, avermelhadas e fracas.
 */
public class WardedRenderer implements BlockEntityRenderer<WardedBlockEntity, WardedRenderer.State> {
    /** As 47 texturas {@code warded_glass_1} a {@code 47} do original, em oito colunas por seis linhas. */
    private static final Identifier SHEET = Thaumcraft.id("textures/misc/warded_glass.png");
    /** O {@code UtilsFX.connectedTextureRefByID}: dos oito vizinhos (um bit cada) para a textura conectada. */
    private static final int[] CONNECTED = {0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13,
            13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13,
            2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28,
            28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16,
            28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1,
            13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13,
            13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10,
            10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10,
            10, 8, 8, 36, 35, 8, 8, 34, 11};
    /** O original desenha a um milésimo por fora do bloco. */
    private static final float OUT = 0.002f;

    public static class State extends BlockEntityRenderState {
        MovingBlockRenderState block;
        boolean runes;
        int colour;
        /** A textura de cada face, na ordem do original (baixo, cima, norte, sul, oeste, leste), ou −1. */
        final int[] faces = new int[6];
    }

    public WardedRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WardedBlockEntity warded, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(warded, state, crumbling);
        state.block = null;
        state.runes = false;
        if (!(warded.getLevel() instanceof ClientLevel level)) return;
        BlockPos pos = warded.getBlockPos();
        if (!warded.stored().isAir() && !warded.stored().is(TCBlocks.WARDED)) {
            MovingBlockRenderState block = new MovingBlockRenderState();
            block.randomSeedPos = pos;
            block.blockPos = pos;
            block.blockState = warded.stored();
            block.biome = level.getBiome(pos);
            block.cardinalLighting = level.cardinalLighting();
            block.lightEngine = level.getLightEngine();
            state.block = block;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof WandItem)
                || !"warding".equals(player.getMainHandItem().get(TCComponents.WAND_FOCUS))) {
            return;
        }
        state.runes = true;
        int ticks = player.tickCount;
        boolean mine = warded.owner() == Focuses.wardOwner(player);
        float r = Mth.sin(ticks / 2.0f + pos.getX()) * 0.2f + 0.8f;
        float g = Mth.sin(ticks / 3.0f + pos.getY()) * 0.2f + (mine ? 0.7f : 0.28f);
        float b = Mth.sin(ticks / 4.0f + pos.getZ()) * 0.2f + 0.28f;
        float a = mine ? 0.5f : 0.25f;
        state.colour = (int) (a * 255) << 24 | (int) (Mth.clamp(r, 0, 1) * 255) << 16
                | (int) (Mth.clamp(g, 0, 1) * 255) << 8 | (int) (Mth.clamp(b, 0, 1) * 255);
        for (int side = 0; side < 6; side++) {
            Direction facing = FACES[side];
            state.faces[side] = level.getBlockState(pos.relative(facing)).is(TCBlocks.WARDED)
                    ? -1 : CONNECTED[mask(level, pos, side, warded.owner())];
        }
    }

    /** A ordem das faces do original: baixo, cima, norte, sul, oeste, leste. */
    private static final Direction[] FACES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
            Direction.WEST, Direction.EAST};

    /** O {@code getIconOnSide}: um bit para cada um dos oito vizinhos da face que é protegido pelo mesmo dono. */
    private static int mask(Level level, BlockPos p, int side, int owner) {
        int x = p.getX(), y = p.getY(), z = p.getZ();
        int[][] n = new int[8][];
        if (side == 0 || side == 1) {
            n = new int[][]{{x - 1, y, z - 1}, {x, y, z - 1}, {x + 1, y, z - 1}, {x - 1, y, z}, {x + 1, y, z},
                    {x - 1, y, z + 1}, {x, y, z + 1}, {x + 1, y, z + 1}};
        } else if (side == 2 || side == 3) {
            int a = side == 2 ? 1 : -1, c = side == 3 ? 1 : -1;
            n = new int[][]{{x + a, y + 1, z}, {x, y + 1, z}, {x + c, y + 1, z}, {x + a, y, z}, {x + c, y, z},
                    {x + a, y - 1, z}, {x, y - 1, z}, {x + c, y - 1, z}};
        } else {
            int a = side == 5 ? 1 : -1, c = side == 4 ? 1 : -1;
            n = new int[][]{{x, y + 1, z + a}, {x, y + 1, z}, {x, y + 1, z + c}, {x, y, z + a}, {x, y, z + c},
                    {x, y - 1, z + a}, {x, y - 1, z}, {x, y - 1, z + c}};
        }
        int id = 0;
        for (int i = 0; i < 8; i++) {
            BlockPos at = new BlockPos(n[i][0], n[i][1], n[i][2]);
            if (level.getBlockState(at).is(TCBlocks.WARDED) && level.getBlockEntity(at) instanceof WardedBlockEntity other
                    && other.owner() == owner) {
                id |= 1 << i;
            }
        }
        return id;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.block != null) collector.submitMovingBlock(pose, state.block, 0);
        if (!state.runes) return;
        int[] faces = state.faces.clone();
        int colour = state.colour;
        collector.submitCustomGeometry(pose, AdditiveGlow.of(SHEET), (matrix, consumer) -> {
            for (int side = 0; side < 6; side++) {
                if (faces[side] >= 0) face(matrix, consumer, side, faces[side], colour);
            }
        });
    }

    /** Uma face com a textura conectada, com as coordenadas de textura do {@code RenderBlocks} da época. */
    private static void face(PoseStack.Pose m, VertexConsumer c, int side, int tile, int colour) {
        float u0 = (tile % 8) / 8.0f, v0 = (tile / 8) / 6.0f, du = 1.0f / 8.0f, dv = 1.0f / 6.0f;
        float lo = -OUT, hi = 1.0f + OUT;
        // os quatro cantos (x, y, z) e, para cada um, a fração (u, v) na textura
        float[][] corners = switch (side) {
            case 0 -> new float[][]{{0, lo, 0, 0, 0}, {1, lo, 0, 1, 0}, {1, lo, 1, 1, 1}, {0, lo, 1, 0, 1}};
            case 1 -> new float[][]{{0, hi, 0, 0, 0}, {0, hi, 1, 0, 1}, {1, hi, 1, 1, 1}, {1, hi, 0, 1, 0}};
            case 2 -> new float[][]{{0, 1, lo, 1, 0}, {1, 1, lo, 0, 0}, {1, 0, lo, 0, 1}, {0, 0, lo, 1, 1}};
            case 3 -> new float[][]{{0, 1, hi, 0, 0}, {0, 0, hi, 0, 1}, {1, 0, hi, 1, 1}, {1, 1, hi, 1, 0}};
            case 4 -> new float[][]{{lo, 1, 1, 1, 0}, {lo, 1, 0, 0, 0}, {lo, 0, 0, 0, 1}, {lo, 0, 1, 1, 1}};
            default -> new float[][]{{hi, 0, 1, 0, 1}, {hi, 0, 0, 1, 1}, {hi, 1, 0, 1, 0}, {hi, 1, 1, 0, 0}};
        };
        // as duas faces, para que a runa apareça de dentro e de fora como no original, sem descarte
        for (int i = 0; i < 4; i++) vertex(m, c, corners[i], u0, v0, du, dv, colour);
        for (int i = 3; i >= 0; i--) vertex(m, c, corners[i], u0, v0, du, dv, colour);
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float[] k, float u0, float v0, float du, float dv,
                               int colour) {
        c.addVertex(m, k[0], k[1], k[2]).setColor(colour).setUv(u0 + k[3] * du, v0 + k[4] * dv)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(m, 0.0f, 1.0f, 0.0f);
    }
}
