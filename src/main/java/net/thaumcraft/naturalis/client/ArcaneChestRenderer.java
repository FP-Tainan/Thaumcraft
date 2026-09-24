package net.thaumcraft.naturalis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.ArcaneChestBlock;
import net.thaumcraft.naturalis.ArcaneChestBlockEntity;

/**
 * O Baú Arcano no mundo: o {@code TileArcaneChestRenderer} do Magia Naturalis 0.5.0 — o baú de sempre, com a
 * folha de madeira-grande ou de prateada, e a tampa que abre quando alguém mexe nele.
 */
public class ArcaneChestRenderer implements BlockEntityRenderer<ArcaneChestBlockEntity, ArcaneChestRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("arcane_chest"), "main");
    /** As duas folhas ficam no atlas dos baús, como as do jogo. */
    public static final SpriteId GREATWOOD = Sheets.CHEST_MAPPER.apply(Thaumcraft.id("chest_greatwood"));
    public static final SpriteId SILVERWOOD = Sheets.CHEST_MAPPER.apply(Thaumcraft.id("chest_silverwood"));

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        SpriteId sheet = GREATWOOD;
        float open;
    }

    private final ChestModel model;
    private final SpriteGetter sprites;

    public ArcaneChestRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ChestModel(context.bakeLayer(LAYER));
        this.sprites = context.sprites();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArcaneChestBlockEntity chest, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(chest, state, crumbling);
        var blockState = chest.getBlockState();
        state.facing = blockState.hasProperty(HorizontalDirectionalBlock.FACING)
                ? blockState.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        state.sheet = chest.kind() == ArcaneChestBlock.Kind.SILVERWOOD ? SILVERWOOD : GREATWOOD;
        state.open = chest.getOpenNess(partial);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.mulPose(ChestRenderer.modelTransformation(state.facing));
        // a mesma curva da tampa do baú comum
        float open = 1.0f - state.open;
        open = 1.0f - open * open * open;
        collector.submitModel(this.model, open, pose, state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                state.sheet, this.sprites, 0, state.breakProgress);
        pose.popPose();
    }
}
