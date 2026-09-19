package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemBobberEntity;
import net.thaumcraft.entity.GolemEntity;

/**
 * A boia do golem pescador: o {@code RenderGolemBobber} da 4.2.3.5 — o desenhinho da boia virado para quem olha, e a
 * linha preta caindo em curva até a mão direita do golem.
 */
public class GolemBobberRenderer extends EntityRenderer<GolemBobberEntity, GolemBobberRenderer.State> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png");

    public static class State extends EntityRenderState {
        public Vec3 line;
    }

    public GolemBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GolemBobberEntity bobber, State s, float partial) {
        super.extractRenderState(bobber, s, partial);
        GolemEntity fisher = bobber.fisher();
        s.line = null;
        if (fisher == null) return;
        // a mão do golem, como o original a calcula: um pouco à frente e à direita, abaixo da altura dos olhos
        float f11 = Mth.lerp(partial, fisher.yBodyRotO, fisher.yBodyRot) * (float) (Math.PI / 180.0);
        double d7 = Mth.sin(f11), d9 = Mth.cos(f11);
        double d3 = Mth.lerp(partial, fisher.xo, fisher.getX()) - d9 * 0.25 - d7 * 0.7;
        double d4 = Mth.lerp(partial, fisher.yo, fisher.getY()) + fisher.getEyeHeight() - 0.4;
        double d5 = Mth.lerp(partial, fisher.zo, fisher.getZ()) - d7 * 0.25 + d9 * 0.7;
        double d14 = Mth.lerp(partial, bobber.xo, bobber.getX());
        double d8 = Mth.lerp(partial, bobber.yo, bobber.getY()) + 0.25;
        double d10 = Mth.lerp(partial, bobber.zo, bobber.getZ());
        s.line = new Vec3((float) (d3 - d14), (float) (d4 - d8), (float) (d5 - d10));
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.pushPose();
        pose.scale(0.5f, 0.5f, 0.5f);
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> {
            vertex(c, m, s.lightCoords, 0.0f, 0, 0, 1);
            vertex(c, m, s.lightCoords, 1.0f, 0, 1, 1);
            vertex(c, m, s.lightCoords, 1.0f, 1, 1, 0);
            vertex(c, m, s.lightCoords, 0.0f, 1, 0, 0);
        });
        pose.popPose();
        if (s.line != null) {
            float xa = (float) s.line.x, ya = (float) s.line.y, za = (float) s.line.z;
            float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            collector.submitCustomGeometry(pose, RenderTypes.lines(), (m, c) -> {
                for (int i = 0; i < 16; i++) {
                    float a0 = i / 16.0f, a1 = (i + 1) / 16.0f;
                    line(xa, ya, za, c, m, a0, a1, width);
                    line(xa, ya, za, c, m, a1, a0, width);
                }
            });
        }
        pose.popPose();
        super.submit(s, pose, collector, camera);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose m, int light, float x, int y, int u, int v) {
        c.addVertex(m, x - 0.5f, y - 0.5f, 0.0f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0.0f, 1.0f, 0.0f);
    }

    /** Um pedaço da linha: a curva {@code y × (f² + f) / 2} do original, a um quarto de bloco acima da boia. */
    private static void line(float xa, float ya, float za, VertexConsumer c, PoseStack.Pose m, float a, float next, float width) {
        float x = xa * a, y = ya * (a * a + a) * 0.5f + 0.25f, z = za * a;
        float nx = xa * next - x, ny = ya * (next * next + next) * 0.5f + 0.25f - y, nz = za * next - z;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        c.addVertex(m, x, y, z).setColor(0xFF000000).setNormal(m, nx / len, ny / len, nz / len).setLineWidth(width);
    }
}
