package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;

/**
 * O taumatório: o {@code TileThaumatoriumRenderer} da 4.2.3.5. O {@code thaumatorium.obj} (as duas metades) virado para
 * a frente, e na frente, no alto, o resultado de uma das receitas marcadas — uma de cada vez, trocando a cada dois
 * segundos, chapado como num quadro.
 */
public class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity, ThaumatoriumRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/thaumatorium.png");
    private final ItemModelResolver models;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean showItem;
    }

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(ThaumatoriumBlockEntity tile, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tile, state, crumbling);
        state.facing = tile.facing();
        state.showItem = false;
        if (tile.getLevel() != null && !tile.recipeHash.isEmpty()) {
            var viewer = net.minecraft.client.Minecraft.getInstance().getCameraEntity();
            int ticks = viewer == null ? 0 : viewer.tickCount;
            CrucibleRecipe recipe = CrucibleRecipe.byHash(tile.recipeHash.get(ticks / 40 % tile.recipeHash.size()));
            if (recipe != null) {
                ItemStack is = recipe.result().copyWithCount(1);
                this.models.updateForTopItem(state.item, is, ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
                state.showItem = true;
            }
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        switch (state.facing) {
            case NORTH -> pose.mulPose(Axis.ZP.rotationDegrees(270.0f));
            case SOUTH -> pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            case EAST -> pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
            default -> {
            }
        }
        float[] body = ObjModel.part("thaumatorium", null);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> ObjMesh.draw(body, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
        if (state.showItem) {
            pose.pushPose();
            pose.translate(0.5f + state.facing.getStepX() / 1.99f, 1.325f, 0.5f + state.facing.getStepZ() / 1.99f);
            switch (state.facing) {
                case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
                case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                default -> {
                }
            }
            // o item de quadro do original: 0,75 do tamanho, chapado
            pose.scale(0.375f, 0.375f, 0.375f);
            state.item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
