package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.AlembicBlock;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

/**
 * O alambique arcano, desenhado peça por peça.
 *
 * <p>Ele saiu do formato de modelo do jogo e veio para cá por um motivo só: <strong>os pés</strong>. No
 * original eles se abrem em diagonal, e um arquivo de modelo do Minecraft só deixa girar uma peça em
 * torno de <em>um</em> eixo — um pé de canto precisa de dois. Aqui cada pé é girado em torno do eixo
 * perpendicular à diagonal dele, que é o que os abre para fora de verdade.
 *
 * <p>A forma é a da captura do mod rodando: largo em cima e afinando para baixo, como um abajur, com o
 * alçapão no alto, o colarinho de latão embaixo e o bico saindo de um lado. Empilhado sobre outro
 * alambique ele troca os pés pelo encaixe — é assim que a coluna do original se monta.
 */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
    private static final Identifier IRON = Thaumcraft.id("textures/block/metalbase.png");
    private static final Identifier BRASS = Thaumcraft.id("textures/block/goldbase.png");

    /** Do tamanho do modelo, em dezesseis avos, para o tamanho do bloco. */
    private static final float UNIT = 1.0f / 16.0f;

    /**
     * O corpo, de baixo para cima: {@code largura, de, até}.
     *
     * <p>Ele abre aos poucos até quase o topo e depois fecha no chanfro do telhado — é esse perfil que
     * faz a silhueta ler como abajur e não como colmeia.
     */
    private static final float[][] BODY = {
            {5.5f, 4.0f, 5.5f},
            {7.5f, 5.5f, 7.0f},
            {9.5f, 7.0f, 8.5f},
            {11.5f, 8.5f, 10.0f},
            {13.0f, 10.0f, 11.5f},
            {13.5f, 11.5f, 13.0f},
            {11.5f, 13.0f, 14.0f},
            {8.0f, 14.0f, 15.0f},
            {4.5f, 15.0f, 16.0f},
    };

    /** O quanto cada pé se abre para fora. */
    private static final float LEG_SPLAY = 26.0f;

    /** O que o desenhista precisa saber do alambique neste quadro. */
    public static class State extends BlockEntityRenderState {
        @Nullable
        public Aspect aspect;
        public int amount;
        public boolean labelled;
        public boolean stacked;
    }

    private final Font font;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AlembicBlockEntity alembic, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(alembic, state, crumbling);
        state.aspect = alembic.aspect();
        state.amount = alembic.amount();
        state.labelled = EssentiaLabel.visible(alembic);
        state.stacked = alembic.getBlockState().hasProperty(AlembicBlock.STACKED)
                && alembic.getBlockState().getValue(AlembicBlock.STACKED);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;

        // o corpo de ferro escuro
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(IRON), (matrix, consumer) -> {
            for (float[] slice : BODY) {
                box(matrix, consumer, slice[0], slice[1], slice[2], light);
            }
            // o bico de lado, na barriga
            quadBox(matrix, consumer, 14.5f, 10.0f, 6.5f, 16.5f, 12.0f, 9.5f, light);
        });

        // o latão: o colarinho e, conforme o caso, o encaixe ou a aba dos pés
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRASS), (matrix, consumer) -> {
            box(matrix, consumer, 6.5f, 2.0f, 4.0f, light);
            if (state.stacked) {
                box(matrix, consumer, 4.0f, 0.0f, 2.0f, light);
            } else {
                box(matrix, consumer, 12.0f, 2.8f, 4.0f, light);
            }
        });

        // e os pés, cada um aberto para a diagonal dele
        if (!state.stacked) this.submitLegs(pose, collector, light);

        if (state.labelled && state.aspect != null && state.amount > 0) {
            // na altura da barriga e para a frente: numa coluna de alambiques não há ar livre em cima
            EssentiaLabel.submit(pose, collector, camera, this.font,
                    state.aspect, state.amount, 0.6f, 0.62f, light);
        }
    }

    /**
     * Os quatro pés, girados para fora.
     *
     * <p>O eixo do giro é o perpendicular à diagonal do pé no plano do chão: assim o pé do canto abre
     * para o canto, e não para o lado. É esta conta que o arquivo de modelo do jogo não sabe fazer.
     */
    private void submitLegs(PoseStack pose, SubmitNodeCollector collector, int light) {
        for (int corner = 0; corner < 4; corner++) {
            float dx = (corner & 1) == 0 ? -1.0f : 1.0f;
            float dz = (corner & 2) == 0 ? -1.0f : 1.0f;

            pose.pushPose();
            // o pé nasce na beirada da aba de latão e abre a partir dali
            pose.translate(0.5f, 3.2f * UNIT, 0.5f);
            pose.mulPose(new Quaternionf().rotateAxis(
                    (float) Math.toRadians(LEG_SPLAY), -dz, 0.0f, dx));
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRASS), (matrix, consumer) -> {
                float half = 1.15f * UNIT;
                float reach = 2.9f * UNIT;
                // ele desce um fio abaixo do bloco de propósito, para o pé encostar no que está embaixo em
                // vez de terminar no ar por causa da inclinação
                float drop = 4.2f * UNIT;
                quad(matrix, consumer,
                        dx * reach - half, -drop, dz * reach - half,
                        dx * reach + half, 0.0f, dz * reach + half, light);
            });
            pose.popPose();
        }
    }

    /** Uma fatia quadrada do corpo, centrada no bloco. */
    private static void box(PoseStack.Pose matrix, VertexConsumer consumer,
                            float width, float from, float to, int light) {
        float half = width / 2.0f;
        quadBox(matrix, consumer, 8.0f - half, from, 8.0f - half, 8.0f + half, to, 8.0f + half, light);
    }

    /** Uma caixa em unidades de modelo. */
    private static void quadBox(PoseStack.Pose matrix, VertexConsumer consumer,
                                float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        quad(matrix, consumer, x0 * UNIT, y0 * UNIT, z0 * UNIT,
                x1 * UNIT, y1 * UNIT, z1 * UNIT, light);
    }

    /**
     * Uma caixa já em blocos, com cada face pegando a textura inteira.
     *
     * <p>As duas texturas do alambique são chapadas, sem desenho a casar; por isso cada face pode tomar a
     * folha toda em vez de um recorte — e assim não há desenrolado de textura para errar.
     */
    private static void quad(PoseStack.Pose matrix, VertexConsumer consumer,
                             float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        // baixo e cima
        face(matrix, consumer, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, -1, 0, light);
        face(matrix, consumer, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0, light);
        // norte e sul
        face(matrix, consumer, x1, y1, z0, x0, y1, z0, x0, y0, z0, x1, y0, z0, 0, 0, -1, light);
        face(matrix, consumer, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1, 0, 0, 1, light);
        // oeste e leste
        face(matrix, consumer, x0, y1, z0, x0, y1, z1, x0, y0, z1, x0, y0, z0, -1, 0, 0, light);
        face(matrix, consumer, x1, y1, z1, x1, y1, z0, x1, y0, z0, x1, y0, z1, 1, 0, 0, light);
    }

    private static void face(PoseStack.Pose matrix, VertexConsumer consumer,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, int light) {
        corner(matrix, consumer, ax, ay, az, 0.0f, 0.0f, nx, ny, nz, light);
        corner(matrix, consumer, bx, by, bz, 1.0f, 0.0f, nx, ny, nz, light);
        corner(matrix, consumer, cx, cy, cz, 1.0f, 1.0f, nx, ny, nz, light);
        corner(matrix, consumer, dx, dy, dz, 0.0f, 1.0f, nx, ny, nz, light);
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(matrix, nx, ny, nz);
    }
}
