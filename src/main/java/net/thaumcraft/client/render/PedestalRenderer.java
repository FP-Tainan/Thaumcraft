package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.PedestalBlockEntity;

/**
 * O que está em cima do pedestal: pairando um dedo acima do prato e girando devagar, como no original.
 */
public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.State> {
    /** Quantos tiques uma volta inteira leva. */
    private static final float SPIN = 60.0f;

    /** O que o desenhista precisa saber do pedestal neste quadro. */
    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean empty = true;
        public float ticks;
    }

    private final net.minecraft.client.renderer.item.ItemModelResolver models;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PedestalBlockEntity pedestal, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pedestal, state, crumbling);
        ItemStack held = pedestal.held();
        state.empty = held.isEmpty();
        state.ticks = pedestal.getLevel() == null ? 0.0f : pedestal.getLevel().getGameTime() + partial;
        if (!state.empty) {
            this.models.updateForTopItem(state.item, held, ItemDisplayContext.GROUND,
                    pedestal.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.empty) return;
        pose.pushPose();
        pose.translate(0.5f, 0.95f, 0.5f);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.ticks / SPIN * 360.0f % 360.0f));
        pose.scale(0.5f, 0.5f, 0.5f);
        state.item.submit(pose, collector, state.lightCoords,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
