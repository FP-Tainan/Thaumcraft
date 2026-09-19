package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.WandPedestalBlockEntity;

/**
 * O pedestal de recarga: o {@code TileWandPedestalRenderer} da 4.2.3.5. A varinha (ou o amuleto) gira sobre a coluna,
 * subindo e descendo devagar, e enquanto bebe de um nó a linha ondulante ({@code wispy.png}) vai do topo até ele, na
 * cor do aspecto.
 */
public class WandPedestalRenderer implements BlockEntityRenderer<WandPedestalBlockEntity, WandPedestalRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean empty = true;
        float ticks, h;
        boolean draining;
        Vec3 from = Vec3.ZERO, to = Vec3.ZERO;
        BlockPos pos = BlockPos.ZERO;
        int colour;
    }

    private final ItemModelResolver models;

    public WandPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WandPedestalBlockEntity ped, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(ped, state, crumbling);
        ItemStack held = ped.held();
        state.empty = held.isEmpty() || ped.getLevel() == null;
        if (state.empty) return;
        this.models.updateForTopItem(state.item, held.copyWithCount(1), ItemDisplayContext.GROUND, ped.getLevel(), null, 0);
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partial;
        state.h = Mth.sin(state.ticks % 32767.0f / 16.0f) * 0.05f;
        state.draining = ped.draining;
        BlockPos pos = ped.getBlockPos();
        state.pos = pos;
        state.from = new Vec3(pos.getX() + 0.5, pos.getY() + 1.65 - state.h * 2.0f, pos.getZ() + 0.5);
        state.to = Vec3.atCenterOf(ped.drain);
        state.colour = ped.drainColor;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.empty) return;
        pose.pushPose();
        pose.translate(0.5f, 1.15f + state.h, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0f));
        // o item solto do jogo antigo, parado no começo do sobe-e-desce
        pose.translate(0.0f, 0.1f, 0.0f);
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
        if (state.draining) {
            pose.pushPose();
            pose.translate(state.to.x - state.pos.getX(), state.to.y - state.pos.getY(), state.to.z - state.pos.getZ());
            FloatyLine.submit(pose, collector, state.from, state.to, state.colour, Math.min(state.ticks, 10.0f) / 10.0f, -0.02f, 0.15f);
            pose.popPose();
        }
    }

    /** O {@code getRenderBoundingBox} do original é dois blocos maior: a linha até o nó sai do bloco. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
