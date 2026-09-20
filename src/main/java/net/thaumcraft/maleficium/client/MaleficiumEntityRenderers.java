package net.thaumcraft.maleficium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.client.render.EldritchOrbRenderer;
import net.thaumcraft.maleficium.entity.DarkMatterEntity;
import net.thaumcraft.maleficium.entity.DiffusionEntity;
import net.thaumcraft.maleficium.entity.HomingShardEntity;

/**
 * As três criaturas do Maleficium desenhadas: o {@code ClientProxy} do Tainted Magic 8.1.1 dava ao orbe de matéria
 * escura o mesmo desenho do orbe eldritch do Thaumcraft, e tinha um desenhista próprio para a lasca de vis
 * ({@code RenderEntityHomingShard}) e para a névoa ({@code RenderEntityDiffusion}) — um quadro da folha de
 * partículas virado para quem vê.
 */
public final class MaleficiumEntityRenderers {
    private MaleficiumEntityRenderers() {
    }

    public static void init() {
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.maleficium.entity.MaleficiumEntities.DARK_MATTER, DarkMatter::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.maleficium.entity.MaleficiumEntities.HOMING_SHARD, Shard::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.maleficium.entity.MaleficiumEntities.DIFFUSION, Diffusion::new);
    }

    /** O que passa do mundo para o desenho: a idade, e o tamanho ou a transparência que vêm dela. */
    public static class State extends EntityRenderState {
        int id;
        int ticks;
        float scale = 1.0f;
        int colour = 0xFFFFFFFF;
    }

    /** A esfera de matéria escura: o desenho do orbe eldritch, como no original. */
    public static class DarkMatter extends EntityRenderer<DarkMatterEntity, State> {
        public DarkMatter(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(DarkMatterEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.id = entity.getId();
            state.ticks = entity.tickCount;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            EldritchOrbRenderer.draw(pose, collector, camera, state.id, state.ticks);
            super.submit(state, pose, collector, camera);
        }
    }

    /** A lasca de vis: o quadro roxo da quarta linha da folha, crescendo com a potência. */
    public static class Shard extends EntityRenderer<HomingShardEntity, State> {
        public Shard(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(HomingShardEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.ticks = entity.tickCount;
            state.scale = 0.4f + 0.1f * entity.strength();
            state.colour = 0xFF000000 | (int) (0.405f * 255) << 16 | (int) (0.075f * 255) << 8 | (int) (0.525f * 255);
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float u0 = (8 + state.ticks % 8) / 16.0f;
            quad(pose, collector, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(Sparkle.PARTICLES),
                    camera, u0, u0 + 0.0625f, 0.25f, 0.3125f, state.scale, state.colour);
            super.submit(state, pose, collector, camera);
        }
    }

    /** A névoa de matéria escura: o quadro cinzento da terceira linha, sumindo em vinte tiques. */
    public static class Diffusion extends EntityRenderer<DiffusionEntity, State> {
        public Diffusion(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.0f;
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(DiffusionEntity entity, State state, float partial) {
            super.extractRenderState(entity, state, partial);
            state.ticks = entity.tickCount;
            float alpha = Math.max(0.0f, (20.0f - entity.tickCount) / 40.0f);
            state.colour = (int) (alpha * 255.0f) << 24 | 0x1A1A1A;
            state.scale = 0.5f;
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            float u0 = state.ticks % 13 / 16.0f;
            quad(pose, collector, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(Sparkle.PARTICLES),
                    camera, u0, u0 + 0.0625f, 0.1875f, 0.25f, state.scale, state.colour);
            super.submit(state, pose, collector, camera);
        }
    }

    /** Um quadrado virado para quem vê, com um quadro da folha de partículas. */
    private static void quad(PoseStack pose, SubmitNodeCollector collector, RenderType type, CameraRenderState camera,
                             float u0, float u1, float v0, float v1, float scale, int colour) {
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
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(m, 0.0f, 1.0f, 0.0f);
    }
}
