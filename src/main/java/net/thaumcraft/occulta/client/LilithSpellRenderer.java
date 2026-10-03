package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.vampire.LilithSpellEntity;

/**
 * O feitiço de Lilith no ar: o {@code RenderSpellEffect} do Witchery.
 *
 * <p>O original desenha uma <b>chapa virada para quem olha</b>, com a figura da bola de neve do jogo, tingida
 * da cor do símbolo e a pouco mais de metade de opaca. Não é um modelo, é uma <b>mancha de luz</b> — e cada um
 * dos cinco tem a sua cor e o seu tamanho, de modo que se aprende a reconhecê-los de longe.
 *
 * <p>É o que aqui se faz, com a mesma figura e as mesmas cinco cores.
 */
public class LilithSpellRenderer extends EntityRenderer<LilithSpellEntity, LilithSpellRenderer.State> {
    /** A figura do original: a bola de neve do jogo. */
    private static final Identifier FIGURA =
            Identifier.fromNamespaceAndPath("minecraft", "textures/item/snowball.png");

    /** A chapa: um quadrado de dezesseis, sem fundo. */
    private static final float[] CHAPA = BoxMesh.box(-8f, -8f, 0f, 16, 16, 0, 0, 0, 16, 16);

    /** Quanto o tamanho do símbolo vale, e quão opaca a mancha é. */
    public static final float TAMANHO = 0.65f;
    public static final int OPACA = 140;

    /** As cinco cores do original, uma por símbolo. */
    public static final int[] CORES = {
            0xFFE260, // Ignianima
            0xFFF4D9, // Flipendo
            0x5E7FFF, // Impedimenta
            0xFFE400, // Confundus
            0x513366, // Attraho
    };

    /** E os cinco tamanhos. */
    public static final float[] TAMANHOS = {3.0f, 3.0f, 1.5f, 1.5f, 1.0f};

    public static class State extends EntityRenderState {
        int qual;
    }

    public LilithSpellRenderer(EntityRendererProvider.Context contexto) {
        super(contexto);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LilithSpellEntity feitiço, State estado, float parcial) {
        super.extractRenderState(feitiço, estado, parcial);
        estado.qual = feitiço.qual().ordinal();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        int qual = Math.clamp(estado.qual, 0, CORES.length - 1);
        pose.pushPose();
        // virada para quem olha, que é o que o original faz com a matriz da câmara
        pose.mulPose(câmara.orientation);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        float quão = TAMANHOS[qual] * TAMANHO / 16.0f;
        pose.scale(quão, quão, quão);

        int cor = OPACA << 24 | CORES[qual];
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(FIGURA),
                (matriz, vértices) -> MeshDrawer.draw(CHAPA, matriz, vértices,
                        0xF000F0, OverlayTexture.NO_OVERLAY, cor));
        pose.popPose();
        super.submit(estado, pose, coletor, câmara);
    }
}
