package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.AncientLockBlockEntity;
import net.thaumcraft.registry.TCItems;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

/**
 * A fechadura antiga: o {@code TileEldritchLockRenderer} da 4.2.3.5. Uma cruz de quatro braços de cubos
 * ({@code eldritch_cube.png}) no plano da porta, respirando, que se recolhem um a um enquanto a fechadura bombeia; a tábua
 * rúnica encaixada na face; e, na abertura de cinco por cinco em volta, o céu de estrelas do nada.
 */
public class AncientLockRenderer implements BlockEntityRenderer<AncientLockBlockEntity, AncientLockRenderer.State> {
    public static final ModelLayerLocation CUBE = new ModelLayerLocation(Thaumcraft.id("eldritch_cube"), "main");
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/eldritch_cube.png");
    private static final Identifier FAR = Thaumcraft.id("textures/misc/particlefield32.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        int count;
        float ticks;
        boolean near;
        final ItemStackRenderState tablet = new ItemStackRenderState();
    }

    private final ModelPart cube;
    private final ItemModelResolver items;

    public AncientLockRenderer(BlockEntityRendererProvider.Context context) {
        this.cube = context.bakeLayer(CUBE);
        this.items = context.itemModelResolver();
    }

    /**
     * O {@code ModelCube(0)}: um cubo de dezesseis numa folha de 64 por 64.
     *
     * <p>O original liga o espelho, mas depois do {@code addBox} — em 1.7.10 o sinalizador é lido dentro do
     * {@code addBox}, logo aquilo não faz nada. É a marca do exportador do Techne, que o punha sempre no fim.
     */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16, 16, 16),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(AncientLockBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.facing = te.getFacing();
        state.count = te.count;
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partial;
        state.near = camera.distanceToSqr(te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ()) < 512.0;
        if (te.count >= 0) {
            this.items.updateForTopItem(state.tablet, new ItemStack(TCItems.RUNED_TABLET), ItemDisplayContext.FIXED, te.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Direction dir = state.facing;
        int light = state.lightCoords;
        // os braços
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        for (int u = 0; u < 4; u++) {
            pose.pushPose();
            pose.mulPose(new Quaternionf().rotationAxis(u * Mth.HALF_PI, new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ())));
            for (int a = 1; a < 5 - (state.count + u * 5) / 20; a++) {
                pose.pushPose();
                pose.translate(0.0f, 0.25f + 0.5f * a, 0.0f);
                float w = Mth.sin((state.ticks + a * 10 + u * 20) / 20.0f) * 0.1f;
                if (a == 1 || a == 4) w = w / 2.0f + 0.2f;
                pose.scale(0.5f + w, 0.5f, 0.5f + w);
                collector.submitModelPart(this.cube, pose, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null, -1, null);
                pose.popPose();
            }
            pose.popPose();
        }
        pose.popPose();
        // a tábua rúnica encaixada
        if (state.count >= 0) {
            pose.pushPose();
            pose.translate(0.5f + dir.getStepX() * 0.525f, 0.285f, 0.5f + dir.getStepZ() * 0.525f);
            switch (dir) {
                case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
                case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                default -> {
                }
            }
            pose.translate(0.0f, 0.25f, 0.0f);
            pose.scale(0.5f, 0.5f, 0.5f);
            state.tablet.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        // o véu da porta: cinco por cinco, no plano do meio da fechadura
        if (dir.getAxis() == Direction.Axis.Y) return;
        float[][] q = dir.getAxis() == Direction.Axis.Z
                ? new float[][]{{-2, 3, 0.5f}, {-2, -2, 0.5f}, {3, -2, 0.5f}, {3, 3, 0.5f}}
                : new float[][]{{0.5f, 3, -2}, {0.5f, -2, -2}, {0.5f, -2, 3}, {0.5f, 3, 3}};
        if (state.near) {
            collector.submitCustomGeometry(pose, HoleRenderer.HOLE, (matrix, consumer) -> {
                for (int i = 0; i < 4; i++) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
                for (int i = 3; i >= 0; i--) consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]);
            });
        } else {
            float[][] uv = {{1, 1}, {1, 0}, {0, 0}, {0, 1}};
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(FAR), (matrix, consumer) -> {
                for (int i = 0; i < 4; i++) {
                    consumer.addVertex(matrix, q[i][0], q[i][1], q[i][2]).setColor(0xFF7F7F7F).setUv(uv[i][0], uv[i][1])
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(180).setNormal(matrix, 0.0f, 1.0f, 0.0f);
                }
            });
        }
    }
}
