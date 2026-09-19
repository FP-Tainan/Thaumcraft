package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.world.OuterLands;
import org.jetbrains.annotations.Nullable;

/**
 * O capstone e o altar do anel eldritch: o {@code TileEldritchCapRenderer} da 4.2.3.5. A peça {@code Cap} do
 * {@code obelisk_cap.obj} deitada no bloco, com a textura do capstone ou a do altar (nas Terras de Fora, a segunda
 * textura, mais escura). No altar, os olhos eldritch já postos aparecem em volta, um por lado, inclinados para fora.
 */
public class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, EldritchCapRenderer.State> {
    private static final Identifier CAP_2 = Thaumcraft.id("textures/models/obelisk_cap_2.png");

    public static class State extends BlockEntityRenderState {
        boolean outer;
        int eyes;
        final ItemStackRenderState eye = new ItemStackRenderState();
    }

    private final Identifier texture;
    private final net.minecraft.client.renderer.item.ItemModelResolver models;

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context, Identifier texture) {
        this.texture = texture;
        this.models = context.itemModelResolver();
    }

    public static <T extends BlockEntity> BlockEntityRendererProvider<T, State> altar() {
        return context -> new EldritchCapRenderer<>(context, Thaumcraft.id("textures/models/obelisk_cap_altar.png"));
    }

    public static <T extends BlockEntity> BlockEntityRendererProvider<T, State> cap() {
        return context -> new EldritchCapRenderer<>(context, Thaumcraft.id("textures/models/obelisk_cap.png"));
    }

    /** O {@code getMaxRenderDistanceSquared} de 9216: noventa e seis blocos. */
    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.outer = te.getLevel() != null && OuterLands.is(te.getLevel());
        state.eyes = te instanceof EldritchAltarBlockEntity altar ? altar.getEyes() : 0;
        if (state.eyes > 0) {
            this.models.updateForTopItem(state.eye, new ItemStack(TCItems.ELDRITCH_EYE), ItemDisplayContext.GROUND, te.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        Identifier tex = state.outer ? CAP_2 : this.texture;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        float[] cap = ObjModel.part("obelisk_cap", "Cap");
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(tex),
                (m, v) -> ObjMesh.draw(cap, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
        if (state.eyes <= 0) return;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        for (int a = 0; a < state.eyes; a++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(a * 90.0f));
            pose.translate(0.46f, 0.2f, 0.0f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            pose.mulPose(Axis.XP.rotationDegrees(-18.0f));
            state.eye.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        pose.popPose();
    }
}
