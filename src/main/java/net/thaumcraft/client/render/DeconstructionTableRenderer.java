package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.registry.TCItems;

/**
 * A mesa de desconstrução: o {@code TileDeconstructionTableRenderer} da 4.2.3.5.
 *
 * <p>A mesa do {@code ModelArcaneWorkbench} com a textura dela e um thaumômetro em pé no meio do tampo; o que
 * está sendo desfeito gira devagar um palmo acima, subindo e descendo; e o primário que sobrou gira deitado,
 * rente ao tampo.
 */
public class DeconstructionTableRenderer implements BlockEntityRenderer<DeconstructionTableBlockEntity, DeconstructionTableRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/decontable.png");

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState thaumometer = new ItemStackRenderState();
        final ItemStackRenderState held = new ItemStackRenderState();
        boolean hasHeld;
        Aspect aspect;
        float ticks;
    }

    private final ItemModelResolver models;

    public DeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DeconstructionTableBlockEntity table, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(table, state, crumbling);
        this.models.updateForTopItem(state.thaumometer, new ItemStack(TCItems.THAUMOMETER), ItemDisplayContext.GROUND,
                table.getLevel(), null, 0);
        ItemStack held = table.getItem(0);
        state.hasHeld = table.getLevel() != null && !held.isEmpty();
        if (state.hasHeld) {
            this.models.updateForTopItem(state.held, held.copyWithCount(1), ItemDisplayContext.GROUND, table.getLevel(), null, 0);
        }
        state.aspect = table.aspect();
        state.ticks = table.getLevel() == null ? 0.0f : table.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        WorkbenchModel.draw(pose, collector, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY);

        pose.pushPose();
        pose.translate(0.5f, 0.92f, 0.5f);
        pose.scale(0.8f, 0.8f, 0.8f);
        state.thaumometer.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();

        if (state.hasHeld) {
            pose.pushPose();
            pose.translate(0.5f, 1.15f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0f));
            // o sobe-e-desce do item solto do jogo antigo, com o começo que o original lhe dá
            pose.translate(0.0f, Mth.sin(state.ticks / 10.0f + Mth.sin(state.ticks / 14.0f) * 0.2f + 0.2f) * 0.1f + 0.1f, 0.0f);
            state.held.submit(pose, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }

        if (state.aspect != null) {
            Aspect aspect = state.aspect;
            pose.pushPose();
            pose.translate(0.5f, 1.081f, 0.5f);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(state.ticks % 360.0f));
            pose.scale(0.024f, 0.024f, 0.024f);
            int colour = 0xCC000000 | aspect.color();
            int light = state.lightCoords;
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(aspect.image()), (m, c) -> {
                float[][] k = {{-8, 8, 0, 1}, {8, 8, 1, 1}, {8, -8, 1, 0}, {-8, -8, 0, 0}};
                for (int i = 0; i < 4; i++) vertex(m, c, k[i], colour, light);
                for (int i = 3; i >= 0; i--) vertex(m, c, k[i], colour, light);
            });
            pose.popPose();
        }
    }

    private static void vertex(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float[] k, int colour, int light) {
        c.addVertex(m, k[0], k[1], 0.0f).setColor(colour).setUv(k[2], k[3]).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
