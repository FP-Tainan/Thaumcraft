package net.thaumcraft.mortuorum.client;

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
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mortuorum.TearEntity;

/**
 * A lágrima no ar: o {@code RenderTear} e o {@code RenderTearBlood} do Necromancy — a figura do item, chata, sempre
 * virada para quem olha, a meio tamanho.
 */
public class TearRenderer extends EntityRenderer<TearEntity, TearRenderer.State> {
    private static final Identifier TEAR = Thaumcraft.id("textures/item/tear.png");
    private static final Identifier TEAR_BLOOD = Thaumcraft.id("textures/item/tear_blood.png");

    public static class State extends EntityRenderState {
        boolean blood;
    }

    public TearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TearEntity lágrima, State state, float partial) {
        super.extractRenderState(lágrima, state, partial);
        state.blood = lágrima.isBlood();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.scale(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - camera.orientation.angle()));
        pose.mulPose(camera.orientation);
        Identifier folha = state.blood ? TEAR_BLOOD : TEAR;
        int luz = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha), (m, v) -> {
            float[][] cantos = {
                    {-0.5f, -0.25f, 0.0f, 0.0f, 1.0f},
                    {0.5f, -0.25f, 0.0f, 1.0f, 1.0f},
                    {0.5f, 0.75f, 0.0f, 1.0f, 0.0f},
                    {-0.5f, 0.75f, 0.0f, 0.0f, 0.0f},
            };
            for (float[] canto : cantos) {
                v.addVertex(m, canto[0], canto[1], canto[2]).setColor(-1).setUv(canto[3], canto[4])
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(luz).setNormal(m, 0.0f, 1.0f, 0.0f);
            }
        });
        pose.popPose();
    }
}
