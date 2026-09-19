package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.thaumcraft.entity.FallingTaintEntity;

/** A crosta caindo: o {@code RenderFallingTaint}, que desenha o bloco inteiro onde a entidade está, como a areia. */
public class FallingTaintRenderer extends EntityRenderer<FallingTaintEntity, FallingBlockRenderState> {
    public FallingTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }

    @Override
    public FallingBlockRenderState createRenderState() {
        return new FallingBlockRenderState();
    }

    @Override
    public void extractRenderState(FallingTaintEntity entity, FallingBlockRenderState state, float partial) {
        super.extractRenderState(entity, state, partial);
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        state.movingBlockRenderState.randomSeedPos = pos;
        state.movingBlockRenderState.blockPos = pos;
        state.movingBlockRenderState.blockState = entity.block();
        if (entity.level() instanceof ClientLevel level) {
            state.movingBlockRenderState.biome = level.getBiome(pos);
            state.movingBlockRenderState.cardinalLighting = level.cardinalLighting();
            state.movingBlockRenderState.lightEngine = level.getLightEngine();
        }
    }

    @Override
    public void submit(FallingBlockRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.movingBlockRenderState.blockState.getRenderShape() != RenderShape.MODEL) return;
        pose.pushPose();
        pose.translate(-0.5, 0.0, -0.5);
        collector.submitMovingBlock(pose, state.movingBlockRenderState, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
