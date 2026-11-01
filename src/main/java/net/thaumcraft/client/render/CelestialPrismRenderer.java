package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.machine.CelestialPrismBlockEntity;

/**
 * O <b>Prisma Celeste</b>: o {@code renderCelestial} do {@code EssenceGeneratorRenderer} da 1.4.0.009.
 *
 * <p>Uma peça de vidro de duas vezes o tamanho do modelo, <b>sem sombra e sem costas</b>: o original
 * desliga a iluminação e o recorte de faces antes de a desenhar, e mistura-a como vidro. É isso que a faz
 * parecer um cristal e não uma pedra.
 */
public class CelestialPrismRenderer implements BlockEntityRenderer<CelestialPrismBlockEntity, CelestialPrismRenderer.State> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/celestial_prism.png");

    /** O {@code glScalef(2, 2, 2)} do original. */
    private static final float ESCALA = 2.0f;

    /** Ele desenha-se sem sombra: o original chama {@code disableStandardItemLighting}. */
    private static final int SEM_SOMBRA = 0xF000F0;

    public static class State extends BlockEntityRenderState {
    }

    public CelestialPrismRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** Ele sobe dois blocos: não pode sumir quando o pé sai da tela. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        float[] malha = ObjModel.part("celestial_prism", null);
        if (malha.length == 0) return;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.scale(ESCALA, ESCALA, ESCALA);
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PELE),
                (m, v) -> ObjMesh.draw(malha, m, v, SEM_SOMBRA, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
