package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.entity.TubeBufferBlockEntity;
import net.thaumcraft.block.entity.TubeOnewayBlockEntity;

/** Os desenhistas do tubo de mão única e do tampão. */
public final class TubeVariantRenderers {
    private TubeVariantRenderers() {
    }

    /** O tubo de mão única: o cano de sempre e, se há cano atrás, os três anéis mostrando o sentido. */
    public static class Oneway implements BlockEntityRenderer<TubeOnewayBlockEntity, Oneway.State> {
        private final TubeRenderer base;

        public static class State extends TubeRenderer.State {
            Direction facing = Direction.NORTH;
            boolean rings;
        }

        public Oneway(BlockEntityRendererProvider.Context context) {
            this.base = new TubeRenderer(context);
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(TubeOnewayBlockEntity tube, State state, float partial, Vec3 camera,
                                       net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
            this.base.extractRenderState(tube, state, partial, camera, crumbling);
            state.facing = tube.facing();
            var level = tube.getLevel();
            Direction back = state.facing.getOpposite();
            state.rings = level == null || level.getBlockEntity(tube.getBlockPos().relative(back)) instanceof EssentiaTransport
                    next && next.isConnectable(back.getOpposite());
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            this.base.submit(state, pose, collector, camera);
            if (state.rings) TubeRingsRenderer.oneway(pose, collector, state.facing, state.lightCoords);
        }
    }

    /** O tampão: os anéis dos lados estrangulados. */
    public static class Buffer implements BlockEntityRenderer<TubeBufferBlockEntity, Buffer.State> {
        public static class State extends BlockEntityRenderState {
            final int[] chokes = new int[6];
        }

        public Buffer(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(TubeBufferBlockEntity buffer, State state, float partial, Vec3 camera,
                                       net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
            BlockEntityRenderState.extractBase(buffer, state, crumbling);
            var level = buffer.getLevel();
            for (Direction dir : Direction.values()) {
                int choke = buffer.choke(dir);
                boolean joined = level != null && buffer.isOpen(dir)
                        && level.getBlockEntity(buffer.getBlockPos().relative(dir)) instanceof EssentiaTransport next
                        && next.isConnectable(dir.getOpposite());
                state.chokes[dir.get3DDataValue()] = joined ? choke : 0;
            }
        }

        @Override
        public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            for (Direction dir : Direction.values()) {
                int choke = state.chokes[dir.get3DDataValue()];
                if (choke != 0) TubeRingsRenderer.choke(pose, collector, dir, choke, state.lightCoords);
            }
        }
    }
}
