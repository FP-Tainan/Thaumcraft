package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.HungryChestBlock;
import net.thaumcraft.block.entity.HungryChestBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O baú faminto: o {@code TileChestHungryRenderer} da 4.2.3.5, com o {@code ModelChest} do jogo de 2014 — o baú
 * mudou de desenho (e de folha de textura) no 1.15, então as três caixas vêm do modelo antigo, medida por medida.
 */
public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity, HungryChestRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/chesthungry.png");
    /** A tampa (ponto de giro 1, 7, 15), o fecho (8, 7, 15) e a base (1, 6, 1) do {@code ModelChest}. */
    private static final float[] LID = BoxMesh.box(0, -5, -14, 14, 5, 14, 0, 0, 64, 64);
    private static final float[] KNOB = BoxMesh.box(-1, -2, -15, 2, 4, 1, 0, 0, 64, 64);
    private static final float[] BASE = BoxMesh.box(0, 0, 0, 14, 10, 14, 0, 19, 64, 64);

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.SOUTH;
        float lid;
    }

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HungryChestBlockEntity chest, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(chest, state, crumbling);
        state.facing = chest.getBlockState().hasProperty(HungryChestBlock.FACING)
                ? chest.getBlockState().getValue(HungryChestBlock.FACING) : Direction.SOUTH;
        state.lid = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.facing, state.lid, state.lightCoords);
    }

    static void draw(PoseStack pose, SubmitNodeCollector collector, Direction facing, float open, int light) {
        pose.pushPose();
        pose.translate(0.0f, 1.0f, 1.0f);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.translate(0.5f, 0.5f, 0.5f);
        float angle = switch (facing) {
            case NORTH -> 180.0f;
            case WEST -> 90.0f;
            case EAST -> -90.0f;
            default -> 0.0f;
        };
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-0.5f, -0.5f, -0.5f);
        float lid = 1.0f - open;
        lid = 1.0f - lid * lid * lid;
        float rot = -(lid * (float) Math.PI / 2.0f);

        part(pose, collector, LID, 1, 7, 15, rot, light);
        part(pose, collector, KNOB, 8, 7, 15, rot, light);
        part(pose, collector, BASE, 1, 6, 1, 0.0f, light);
        pose.popPose();
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh, float px, float py, float pz,
                             float rotX, int light) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rotX != 0.0f) pose.mulPose(Axis.XP.rotation(rotX));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** O baú na mão e no inventário, fechado e de frente. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            draw(pose, collector, Direction.SOUTH, 0.0f, light);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new Item();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
