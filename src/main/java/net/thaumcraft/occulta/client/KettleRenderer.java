package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.kettle.KettleBlock;
import net.thaumcraft.occulta.kettle.KettleBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Caldeirão de Pote no mundo: o {@code RenderKettle} e o {@code ModelKettle} do Witchery.
 *
 * <p>Quatro paredes e um fundo, a barra de cima com as quatro correntes de onde ele pende, e a <b>tampa de
 * líquido</b> — que é uma chapa lisa pintada da cor do que estiver cozinhando, com quatro figuras que se revezam
 * de segundo em segundo. Os frascos de vidro que estiverem no pote aparecem na borda, até dois.
 *
 * <p>A barra de cima só se desenha quando <b>não há bloco por cima</b>, como no original: sem isso ela atravessa
 * o chão de quem puser o pote debaixo de alguma coisa.
 */
public class KettleRenderer implements BlockEntityRenderer<KettleBlockEntity, KettleRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/witches_kettle.png");

    /** As quatro paredes e o fundo. */
    private static final float[] SIDE = BoxMesh.box(0, 0, 0, 9, 6, 1, 0, 0, 64, 64);
    private static final float[] BOTTOM = BoxMesh.box(0, 0, 0, 8, 1, 8, 13, 0, 64, 64);

    /** A barra de onde ele pende. */
    private static final float[] CROSSBAR = BoxMesh.box(-4, 0, 0, 24, 2, 2, 0, 10, 64, 64);

    /** Uma corrente: uma chapa sem espessura. */
    private static final float[] CHAIN = BoxMesh.box(0, -0.5f, 0, 11, 1, 0, 0, 15, 64, 64);

    /** As quatro figuras do líquido, que se revezam. */
    private static final float[][] LIQUID = {
            BoxMesh.box(0, 0, 0, 8, 0, 8, -8, 16, 64, 64),
            BoxMesh.box(0, 0, 0, 8, 0, 8, 8, 16, 64, 64),
            BoxMesh.box(0, 0, 0, 8, 0, 8, 24, 16, 64, 64),
            BoxMesh.box(0, 0, 0, 8, 0, 8, 40, 16, 64, 64)};

    /** E o frasco da borda, de três caixas. */
    private static final float[] BOTTLE = BoxMesh.join(
            BoxMesh.box(0, 2, 0, 3, 3, 3, 52, 5, 64, 64),
            BoxMesh.box(1, 1, 1, 1, 1, 1, 60, 3, 64, 64),
            BoxMesh.box(0.5f, 0, 0.5f, 2, 1, 2, 56, 0, 64, 64));

    /** Onde cada corrente pende, e como ela se inclina: os quatro do original, em graus. */
    private static final float[][] CHAINS = {
            {0.0f, -0.4f, 1.1f}, {0.0f, 0.4f, 1.1f}, {0.0f, 0.4f, 2.05f}, {0.0f, -2.75f, -1.1f}};

    /** Quantas batidas cada figura do líquido dura, e quantas a volta toda. */
    public static final int FRAME = 20;
    public static final int CYCLE = 80;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean water;
        public boolean ruined;
        public boolean ready;
        public boolean brewing;
        public boolean roofed;
        public int color;
        public int bottles;
        public long ticks;
        /** Na mão não se desenha a barra nem as correntes: o pote na mão não pende de nada. */
        public boolean inHand;
    }

    public KettleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(KettleBlockEntity pote, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(pote, state, partial, camera, crumbling);
        var feitio = pote.getBlockState();
        state.facing = feitio.hasProperty(KettleBlock.FACING)
                ? feitio.getValue(KettleBlock.FACING) : Direction.NORTH;
        state.water = pote.filled();
        state.ruined = pote.ruined();
        state.ready = pote.ready();
        state.brewing = pote.brewing();
        state.color = pote.color();
        state.bottles = pote.bottles();
        state.ticks = pote.getLevel() == null ? 0L : pote.getLevel().getGameTime();
        state.roofed = pote.getLevel() != null
                && pote.getLevel().getBlockState(pote.getBlockPos().above()).isSolidRender();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state);
    }

    /** O pote inteiro, do jeito que aquele estado diz. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, State state) {
        pose.pushPose();
        // o caminho do original: meio do bloco, de cabeça para baixo, um bloco abaixo, e o giro da marca
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(angle(state.facing)));

        part(pose, collector, SIDE, -5.0f, 18.0f, -5.0f, 0.0f, state.lightCoords, 0xFFFFFFFF);
        part(pose, collector, SIDE, -4.0f, 18.0f, 4.0f, 0.0f, state.lightCoords, 0xFFFFFFFF);
        part(pose, collector, SIDE, -5.0f, 18.0f, 5.0f, 90.0f, state.lightCoords, 0xFFFFFFFF);
        part(pose, collector, SIDE, 4.0f, 18.0f, 4.0f, 90.0f, state.lightCoords, 0xFFFFFFFF);
        part(pose, collector, BOTTOM, -4.0f, 23.0f, -4.0f, 0.0f, state.lightCoords, 0xFFFFFFFF);

        if (!state.inHand) {
            if (!state.roofed) {
                part(pose, collector, CROSSBAR, -8.0f, 8.05f, -1.0f, 0.0f, state.lightCoords, 0xFFFFFFFF);
            }
            for (float[] corrente : CHAINS) {
                chain(pose, collector, corrente, state.lightCoords);
            }
        }

        for (int i = 0; i < Math.min(state.bottles > 0 ? (state.bottles > 1 ? 2 : 1) : 0, 2); i++) {
            part(pose, collector, BOTTLE, i == 0 ? -4.0f : 0.0f, 13.0f, -6.0f, 0.0f,
                    state.lightCoords, 0xFFFFFFFF);
        }

        if (state.water) {
            int quadro = (int) ((state.ticks % CYCLE) / FRAME);
            part(pose, collector, LIQUID[quadro], -4.0f, 20.0f, -4.0f, 0.0f, state.lightCoords, tint(state));
        }
        pose.popPose();
    }

    /** A cor do líquido: a da receita, meia-luz enquanto cozinha, e a do pote estragado por cima de tudo. */
    private static int tint(State state) {
        if (state.ruined) return 0xFF7F9C00 | 0xFF000000;
        int cor = state.color;
        float fator = 1.0f;
        if (cor == 0) {
            cor = KettleBlockEntity.PLAIN & 0xFFFFFF;
        } else if (state.brewing && !state.ready) {
            fator = 0.5f;
        }
        int r = (int) ((cor >> 16 & 0xFF) * fator);
        int g = (int) ((cor >> 8 & 0xFF) * fator);
        int b = (int) ((cor & 0xFF) * fator);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** O giro de cada marca: os quatro do original, pela ordem dele. */
    private static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh,
                             float px, float py, float pz, float giroY, int light, int cor) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (giroY != 0.0f) pose.mulPose(Axis.YP.rotationDegrees(giroY));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, cor));
        pose.popPose();
    }

    /** Uma corrente, que pende do mesmo ponto com a inclinação dela. */
    private static void chain(PoseStack pose, SubmitNodeCollector collector, float[] giro, int light) {
        pose.pushPose();
        pose.translate(0.0f, 9.0f / 16.0f, 0.0f);
        // a ordem é a do ModelRenderer de 2014: primeiro o Z, depois o Y, e só então o X
        pose.mulPose(Axis.ZP.rotation(giro[2]));
        pose.mulPose(Axis.YP.rotation(giro[1]));
        if (giro[0] != 0.0f) pose.mulPose(Axis.XP.rotation(giro[0]));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(CHAIN, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
