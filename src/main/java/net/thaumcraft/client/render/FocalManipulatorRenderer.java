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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.item.FocusItem;

/**
 * O manipulador focal: o {@code TileFocalManipulatorRenderer} da 4.2.3.5 — a mesa do {@code ModelArcaneWorkbench} com a
 * {@code wandtable.png} e, em cima, o foco posto girando devagar, subindo e descendo.
 */
public class FocalManipulatorRenderer implements BlockEntityRenderer<FocalManipulatorBlockEntity, FocalManipulatorRenderer.State> {
    public static final Identifier TEXTURE = Thaumcraft.id("textures/models/wandtable.png");

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState held = new ItemStackRenderState();
        boolean hasHeld;
        float ticks;
    }

    private final ItemModelResolver models;

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FocalManipulatorBlockEntity table, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(table, state, crumbling);
        ItemStack held = table.getItem(0);
        state.hasHeld = table.getLevel() != null && held.getItem() instanceof FocusItem;
        if (state.hasHeld) {
            this.models.updateForTopItem(state.held, held.copyWithCount(1), ItemDisplayContext.GROUND, table.getLevel(), null, 0);
        }
        state.ticks = table.getLevel() == null ? 0.0f : table.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        WorkbenchModel.draw(pose, collector, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY);
        if (state.hasHeld) {
            pose.pushPose();
            pose.translate(0.5f, 1.0f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0f));
            // o sobe-e-desce do item solto do jogo antigo, com o começo que o original lhe dá
            pose.translate(0.0f, Mth.sin(state.ticks / 10.0f + Mth.sin(state.ticks / 14.0f) * 0.2f + 0.2f) * 0.1f + 0.1f, 0.0f);
            state.held.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
