package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.entity.ExplosiveOrbEntity;
import net.thaumcraft.entity.ShockOrbEntity;

/**
 * Os orbes das melhorias de foco: o {@code RenderExplosiveOrb} (a bola de fogo, um quadro da {@code particles2.png},
 * mistura comum) e o {@code RenderElectricOrb} (o orbe do choque de terra, um quadro da {@code particles.png}, somando
 * luz e pulsando). Um quadrado virado para quem vê, aceso.
 */
public final class FocusOrbRenderers {
    private static final Identifier PARTICLES2 = Thaumcraft.id("textures/misc/particles2.png");

    private FocusOrbRenderers() {
    }

    public static class State extends EntityRenderState {
        int ticks;
        float partial;
        boolean red;
    }

    private static void quad(PoseStack pose, SubmitNodeCollector collector, RenderType type, float u0, float u1, float v0, float v1,
                             float scale, int colour, CameraRenderState camera) {
        pose.pushPose();
        pose.mulPose(camera.orientation);
        pose.scale(scale, scale, scale);
        collector.submitCustomGeometry(pose, type, (m, c) -> {
            corner(m, c, -0.5f, -0.5f, u0, v1, colour);
            corner(m, c, 0.5f, -0.5f, u1, v1, colour);
            corner(m, c, 0.5f, 0.5f, u1, v0, colour);
            corner(m, c, -0.5f, 0.5f, u0, v0, colour);
        });
        pose.popPose();
    }

    private static void corner(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220).setNormal(m, 0.0f, 1.0f, 0.0f);
    }

    /** A bola de fogo: quatro quadros da linha 13 da {@code particles2.png}, a 0,7. */
    public static class Explosive extends EntityRenderer<ExplosiveOrbEntity, State> {
        public Explosive(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(ExplosiveOrbEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.ticks = entity.tickCount;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float u0 = state.ticks % 4 / 16.0f, u1 = u0 + 0.0625f;
            float v0 = 0.8125f, v1 = v0 + 0.0625f;
            quad(pose, collector, AdditiveGlow.blended(PARTICLES2), u0, u1, v0, v1, 0.7f, 0xCCFFFFFF, camera);
            super.submit(state, pose, collector, camera);
        }
    }

    /**
     * O orbe que persegue o alvo ({@code EntityGolemOrb}), com o mesmo {@code RenderElectricOrb} do choque: o azul na última
     * linha da folha, o vermelho na de cima dela.
     */
    public static class GolemOrb extends EntityRenderer<net.thaumcraft.entity.GolemOrbEntity, State> {
        public GolemOrb(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(net.thaumcraft.entity.GolemOrbEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.ticks = entity.tickCount;
            state.red = entity.isRed();
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float u0 = (1 + state.ticks % 6) / 8.0f, u1 = u0 + 0.125f;
            float v0 = state.red ? 0.75f : 0.875f, v1 = v0 + 0.125f;
            float bob = Mth.sin(state.ticks / 5.0f) * 0.2f + 0.2f;
            quad(pose, collector, AdditiveGlow.of(Sparkle.PARTICLES), u0, u1, v0, v1, 1.0f + bob, 0xCCFFFFFF, camera);
            super.submit(state, pose, collector, camera);
        }
    }

    /** O orbe do choque: seis quadros da última linha da {@code particles.png} (em oitavos), pulsando. */
    public static class Electric extends EntityRenderer<ShockOrbEntity, State> {
        public Electric(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(ShockOrbEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.ticks = entity.tickCount;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float u0 = (1 + state.ticks % 6) / 8.0f, u1 = u0 + 0.125f;
            float v0 = 0.875f, v1 = v0 + 0.125f;
            float bob = Mth.sin(state.ticks / 5.0f) * 0.2f + 0.2f;
            quad(pose, collector, AdditiveGlow.of(Sparkle.PARTICLES), u0, u1, v0, v1, 1.0f + bob, 0xCCFFFFFF, camera);
            super.submit(state, pose, collector, camera);
        }
    }
}
