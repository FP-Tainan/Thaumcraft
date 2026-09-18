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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.item.WandItem;

/**
 * A bancada arcana: o {@code TileArcaneWorkbenchRenderer} da 4.2.3.5 — a mesa do {@code ModelArcaneWorkbench} e,
 * se houver varinha na casa dela, a varinha deitada sobre o tampo, um pouco torta.
 */
public class ArcaneWorkbenchRenderer implements BlockEntityRenderer<ArcaneWorkbenchBlockEntity, ArcaneWorkbenchRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/worktable.png");

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState wand = new ItemStackRenderState();
        boolean hasWand;
    }

    private final ItemModelResolver models;

    public ArcaneWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArcaneWorkbenchBlockEntity bench, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(bench, state, crumbling);
        ItemStack wand = bench.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
        state.hasWand = bench.getLevel() != null && wand.getItem() instanceof WandItem;
        if (state.hasWand) {
            ItemStack one = wand.copyWithCount(1);
            this.models.updateForTopItem(state.wand, one, ItemDisplayContext.FIXED, bench.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        WorkbenchModel.draw(pose, collector, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY);
        if (!state.hasWand) return;
        pose.pushPose();
        pose.translate(0.65f, 1.0625f, 0.25f);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(20.0f));

        state.wand.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
