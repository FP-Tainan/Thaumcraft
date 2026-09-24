package net.thaumcraft.naturalis.client;

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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.thaumcraft.client.render.ExtrudedSprite;
import net.thaumcraft.client.render.WorkbenchModel;
import net.thaumcraft.naturalis.ResearchLogItem;
import net.thaumcraft.naturalis.TranscribingTableBlockEntity;

/**
 * A Mesa de Transcrição no mundo: o {@code TileTranscribingTableRenderer} do Magia Naturalis 0.5.0 — a mesa
 * arcana com a pintura dela, o diário deitado num canto do tampo e a pena girando por cima.
 */
public class TranscribingTableRenderer
        implements BlockEntityRenderer<TranscribingTableBlockEntity, TranscribingTableRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/transcribing_table.png");
    private static final Identifier QUILL = Thaumcraft.id("textures/block/tablequill.png");

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState log = new ItemStackRenderState();
        boolean hasLog;
        float ticks;
    }

    private final ItemModelResolver models;

    public TranscribingTableRenderer(BlockEntityRendererProvider.Context context) {
        this.models = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TranscribingTableBlockEntity table, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(table, state, crumbling);
        ItemStack log = table.getItem(0);
        state.hasLog = table.getLevel() != null && log.getItem() instanceof ResearchLogItem;
        if (state.hasLog) {
            this.models.updateForTopItem(state.log, log.copyWithCount(1), ItemDisplayContext.FIXED,
                    table.getLevel(), null, 0);
        }
        state.ticks = table.getLevel() == null ? 0.0f : table.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        WorkbenchModel.draw(pose, collector, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY);
        if (!state.hasLog) return;

        // o diário deitado no tampo, um pouco torto, como no original
        pose.pushPose();
        pose.translate(0.59f, 1.02f, 0.29f);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(20.0f));
        state.log.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();

        // e a pena, girando deitada acima do tampo
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0f));
        pose.translate(-0.25f, 0.0f, -0.25f);
        pose.scale(0.5f, 0.5f, 0.5f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(QUILL), (matrix, consumer) ->
                ExtrudedSprite.draw(matrix, consumer, 16, 0.025f, state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
