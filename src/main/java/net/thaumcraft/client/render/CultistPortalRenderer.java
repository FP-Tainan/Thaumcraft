package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.CultistPortalEntity;

/**
 * O portal carmesim: o {@code RenderCultistPortal} da 4.2.3.5. Um quadro da folha animada ({@code cultist_portal.png},
 * dezesseis quadros), sempre virado para quem vê, abrindo nos primeiros dois segundos e meio. Estremece quando apanha e
 * pulsa quando solta alguém; quanto mais ferido, mais ele treme e mais transparente fica.
 */
public class CultistPortalRenderer extends EntityRenderer<CultistPortalEntity, CultistPortalRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/misc/cultist_portal.png");

    public static class State extends EntityRenderState {
        float scale;
        float scaley;
        float alpha;
        float height;
    }

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1f;
        this.shadowStrength = 0.5f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CultistPortalEntity portal, State state, float partial) {
        super.extractRenderState(portal, state, partial);
        float scaley = 1.5f;
        int e = (int) Math.min(50.0f, portal.tickCount + partial);
        if (portal.hurtTime > 0) {
            double d = Math.sin(portal.hurtTime * 72 * Math.PI / 180.0);
            scaley = (float) (scaley - d / 4.0);
            e = (int) (e + 6.0 * d);
        }
        if (portal.pulse > 0) {
            double d = Math.sin(portal.pulse * 36 * Math.PI / 180.0);
            scaley = (float) (scaley + d / 4.0);
            e = (int) (e + 12.0 * d);
        }
        float scale = e / 50.0f * 1.3f;
        float m = (1.0f - portal.getHealth() / portal.getMaxHealth()) / 3.0f;
        float bob = Mth.sin(portal.tickCount / (5.0f - 12.0f * m)) * m + m;
        float bob2 = Mth.sin(portal.tickCount / (6.0f - 15.0f * m)) * m + m;
        state.alpha = 1.0f - bob;
        state.scaley = scaley - bob / 4.0f;
        state.scale = scale - bob2 / 3.0f;
        state.height = portal.getBbHeight();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        long time = System.nanoTime() / 50000000L;
        int frame = 15 - (int) time % 16;
        float u0 = frame / 16.0f, u1 = u0 + 0.0625f;
        int colour = (int) (Mth.clamp(state.alpha, 0.0f, 1.0f) * 255.0f) << 24 | 0xFFFFFF;
        pose.pushPose();
        pose.translate(0.0f, state.height / 2.0f, 0.0f);
        pose.mulPose(camera.orientation);
        pose.scale(state.scale, state.scaley, state.scale);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE), (m, c) -> {
            corner(m, c, -1.0f, -1.0f, u1, 0.0f, colour);
            corner(m, c, -1.0f, 1.0f, u1, 1.0f, colour);
            corner(m, c, 1.0f, 1.0f, u0, 1.0f, colour);
            corner(m, c, 1.0f, -1.0f, u0, 0.0f, colour);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void corner(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220)
                .setNormal(m, 0.0f, 0.0f, -1.0f);
    }
}
