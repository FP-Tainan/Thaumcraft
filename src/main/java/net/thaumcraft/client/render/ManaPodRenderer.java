package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.block.entity.ManaPodBlockEntity;

/**
 * A vagem de mana: o {@code TileManaPodRenderer} e o {@code ModelManaPod} da 4.2.3.5. Do tamanho dois em diante, a casca
 * ({@code manapod_2.png}) pendurada, crescendo com o tamanho, verde no começo e tomando a cor do aspecto até o sete; do
 * três em diante, por dentro, o miolo ({@code manapod_0.png}) que pulsa e brilha sozinho.
 */
public class ManaPodRenderer implements BlockEntityRenderer<ManaPodBlockEntity, ManaPodRenderer.State> {
    private static final Identifier INNER = Thaumcraft.id("textures/models/manapod_0.png");
    private static final Identifier SHELL = Thaumcraft.id("textures/models/manapod_2.png");
    /** O {@code pod0}, de quatro por cinco por quatro, e o {@code pod2}, de sete por nove por sete, numa folha de 32. */
    private static final float[] POD0 = BoxMesh.box(-2, 0, -2, 4, 5, 4, 0, 0, 32, 32);
    private static final float[] POD2 = BoxMesh.box(-3.5f, 0, -3.5f, 7, 9, 7, 0, 0, 32, 32);

    public static class State extends BlockEntityRenderState {
        int age;
        int colour;
        float ticks;
        int hash;
    }

    public ManaPodRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ManaPodBlockEntity pod, State state, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pod, state, crumbling);
        state.age = pod.getBlockState().getValue(ManaPodBlock.AGE);
        // a casca vai do verde do original (0,145; 0,616; 0,459) até a cor do aspecto no sete
        float br = 0.14509805f, bg = 0.6156863f, bb = 0.45882353f;
        float fr = br, fg = bg, fb = bb;
        Aspect aspect = pod.aspect == null ? Aspects.PLANT : pod.aspect;
        if (pod.aspect != null) {
            float ar = (aspect.color() >> 16 & 255) / 255.0f, ag = (aspect.color() >> 8 & 255) / 255.0f, ab = (aspect.color() & 255) / 255.0f;
            if (state.age == 7) {
                fr = ar;
                fg = ag;
                fb = ab;
            } else {
                float m = state.age - 2;
                fr = (br + ar * m) / (m + 1.0f);
                fg = (bg + ag * m) / (m + 1.0f);
                fb = (bb + ab * m) / (m + 1.0f);
            }
        }
        state.colour = (int) (0.9f * 255) << 24 | (int) (fr * 255) << 16 | (int) (fg * 255) << 8 | (int) (fb * 255);
        var player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0 : player.tickCount;
        state.hash = pod.getBlockPos().hashCode();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.age <= 1) return;
        pose.pushPose();
        pose.translate(0.5f, 0.75f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (state.age > 2) {
            float scale = Mth.sin((state.ticks + state.hash % 100) / 8.0f) * 0.1f + 0.9f;
            int bright = state.age * 10 + (int) (150.0f * scale);
            // o original acende só a luz de bloco, com o céu no zero
            int light = Math.min(240, bright);
            pose.pushPose();
            pose.translate(0.0f, 0.1f, 0.0f);
            float s = 0.125f * state.age * scale / 16.0f;
            pose.scale(s, s, s);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(INNER),
                    (m, c) -> MeshDrawer.draw(POD0, m, c, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
            pose.popPose();
        }
        float s = 0.15f * state.age / 16.0f;
        pose.scale(s, s, s);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(SHELL),
                (m, c) -> MeshDrawer.draw(POD2, m, c, state.lightCoords, OverlayTexture.NO_OVERLAY, state.colour));
        pose.popPose();
    }
}
