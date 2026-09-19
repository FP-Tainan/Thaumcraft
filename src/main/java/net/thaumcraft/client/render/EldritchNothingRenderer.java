package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.EldritchNothingBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O nada: o {@code TileEldritchNothingRenderer} da 4.2.3.5. Em cada face que dá para um lugar aberto, o mesmo céu de
 * estrelas em camadas do buraco (o túnel e o campo de partículas); de longe (mais de uns vinte e dois blocos), só a folha
 * parada {@code particlefield32.png}, acesa a 180.
 */
public class EldritchNothingRenderer implements BlockEntityRenderer<EldritchNothingBlockEntity, EldritchNothingRenderer.State> {
    private static final Identifier FAR = Thaumcraft.id("textures/misc/particlefield32.png");

    public static class State extends BlockEntityRenderState {
        final boolean[] open = new boolean[6];
        boolean near;
    }

    public EldritchNothingRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchNothingBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(te, state, crumbling);
        Level level = te.getLevel();
        for (Direction dir : Direction.values()) {
            state.open[dir.get3DDataValue()] = level != null && !level.getBlockState(te.getBlockPos().relative(dir)).isSolidRender();
        }
        state.near = camera.distanceToSqr(te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ() + 0.5) < 512.0;
    }

    private static float[][] face(Direction dir) {
        return switch (dir) {
            case UP -> new float[][]{{0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}};
            case DOWN -> new float[][]{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
            case NORTH -> new float[][]{{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}};
            case SOUTH -> new float[][]{{1, 0, 1}, {1, 1, 1}, {0, 1, 1}, {0, 0, 1}};
            case WEST -> new float[][]{{0, 0, 1}, {0, 1, 1}, {0, 1, 0}, {0, 0, 0}};
            case EAST -> new float[][]{{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}};
        };
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean[] open = state.open.clone();
        if (state.near) {
            collector.submitCustomGeometry(pose, HoleRenderer.HOLE, (matrix, consumer) -> {
                for (Direction dir : Direction.values()) {
                    if (!open[dir.get3DDataValue()]) continue;
                    float[][] q = face(dir);
                    // dos dois lados, como o buraco: a ordem dos cantos não precisa bater com a do jogo
                    for (int i = 0; i < 4; i++) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
                    for (int i = 3; i >= 0; i--) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
                }
            });
        } else {
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(FAR), (matrix, consumer) -> {
                for (Direction dir : Direction.values()) {
                    if (!open[dir.get3DDataValue()]) continue;
                    float[][] q = face(dir);
                    float[][] uv = {{1, 1}, {1, 0}, {0, 0}, {0, 1}};
                    for (int k = 0; k < 8; k++) {
                        int i = k < 4 ? k : 7 - k;
                        consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]).setColor(0xFFFFFFFF).setUv(uv[i][0], uv[i][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(180).setNormal(matrix, dir.getStepX(), dir.getStepY(), dir.getStepZ());
                    }
                }
            });
        }
    }
}
