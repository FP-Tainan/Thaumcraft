package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.EnergizedNodeBlockEntity;

/**
 * O nó energizado: o {@code TileNodeEnergizedRenderer} da 4.2.3.5. As bolhas do nó de que ele era feito — sempre à
 * vista, com ou sem óculos — e, por cima, o anel de raios animado ({@code lightningringv.png}) somando luz.
 */
public class EnergizedNodeRenderer implements BlockEntityRenderer<EnergizedNodeBlockEntity, EnergizedNodeRenderer.State> {
    private static final Identifier RING = Thaumcraft.id("textures/misc/lightningringv.png");
    /** A {@code lightningringv.png} tem os quadros lado a lado: tantos quanto a largura cabe a altura. */
    private static final int RING_FRAMES = 16;

    public static class State extends NodeRenderState {
        int x;
    }

    public EnergizedNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnergizedNodeBlockEntity node, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(node, state, crumbling);
        state.wisps.clear();
        for (Aspect aspect : node.auraBase().getAspects()) {
            int amount = node.auraBase().getAmount(aspect);
            if (amount > 0) state.wisps.add(new NodeRenderState.Wisp(aspect.color(), amount, aspect.blend() != 1));
        }
        state.type = node.type();
        state.modifier = node.modifier();
        state.seed = Math.abs(node.getBlockPos().hashCode()) % 32;
        var player = Minecraft.getInstance().player;
        state.ticks = player == null ? partial : player.tickCount + partial;
        state.x = node.getBlockPos().getX();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        NodeRenderer.drawWisps(state, pose, collector, camera);
        int frame = (int) ((System.nanoTime() / 40000000L + state.x) % RING_FRAMES);
        FacingQuad.draw(pose, collector, camera, AdditiveGlow.of(RING), new Vec3(0.5, 0.5, 0.5), 0.33f, 0.9f, RING_FRAMES, frame, 0xFFFFFF);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
