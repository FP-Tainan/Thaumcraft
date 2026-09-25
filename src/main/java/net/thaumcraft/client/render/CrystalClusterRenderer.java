package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.CrystalClusterBlock;
import net.thaumcraft.block.entity.CrystalClusterBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * O aglomerado de cristal: o {@code TileCrystalRenderer} e o {@code ModelCrystal} da 4.2.3.5.
 *
 * <p>Uma lasca grande e cinco menores em volta, cada uma o cubo do {@code ModelCrystal} girado de 45 graus e esticado,
 * na cor do aspecto (o misto, uma de cada cor), com um brilho próprio que pulsa. As voltas e os tamanhos saem de um
 * sorteio com a mesma semente do original ({@code tipo + x + y * z}), então cada aglomerado tem o mesmo formato que
 * teria lá.
 */
public class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlockEntity, CrystalClusterRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/crystal.png");
    /** O cubo do {@code ModelCrystal} numa folha de 64 por 32. */
    private static final float[] CRYSTAL = BoxMesh.box(-16, -16, 0, 16, 16, 16, 0, 0, 64, 32);

    public static class State extends BlockEntityRenderState {
        int kind;
        Direction facing = Direction.UP;
        BlockPos pos = BlockPos.ZERO;
        float ticks;
    }

    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrystalClusterBlockEntity crystal, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(crystal, state, crumbling);
        state.kind = crystal.getBlockState().getBlock() instanceof CrystalClusterBlock block ? block.kind() : 6;
        state.facing = crystal.getBlockState().getValue(CrystalClusterBlock.FACING);
        state.pos = crystal.getBlockPos();
        var player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0 : player.tickCount;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.kind, state.facing, state.pos, state.ticks);
    }

    /** O {@code renderTileEntityAt}: a lasca grande e as cinco em volta. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, int kind, Direction facing, BlockPos pos, float ticks) {
        int colour = kind != 6 ? CrystalClusterBlock.COLOURS[kind + 1] : CrystalClusterBlock.COLOURS[5];
        Random rand = new Random(kind + pos.getX() + pos.getY() * pos.getZ());
        float a1 = (rand.nextFloat() - rand.nextFloat()) * 5.0f;
        float a2 = (rand.nextFloat() - rand.nextFloat()) * 5.0f;
        shard(pose, collector, facing, a1, a2, rand, colour, 1.1f, ticks);
        for (int a = 1; a < 6; a++) {
            if (kind == 6) colour = CrystalClusterBlock.COLOURS[a == 5 ? 6 : a];
            int angle1 = rand.nextInt(36) + 72 * a;
            int angle2 = 15 + rand.nextInt(15);
            shard(pose, collector, facing, angle1, angle2, rand, colour, 0.8f, ticks);
        }
    }

    /** O {@code drawCrystal}. */
    private static void shard(PoseStack pose, SubmitNodeCollector collector, Direction facing, float a1, float a2, Random rand,
                              int colour, float size, float ticks) {
        float shade = Mth.sin((ticks + rand.nextInt(10)) / (5.0f + rand.nextFloat())) * 0.075f + 0.925f;
        int r = Math.min(255, (colour >> 16 & 255) * 255 / 220), g = Math.min(255, (colour >> 8 & 255) * 255 / 220),
                b = Math.min(255, (colour & 255) * 255 / 220);
        int tint = 0xFF000000 | r << 16 | g << 8 | b;
        pose.pushPose();
        orient(pose, facing);
        pose.mulPose(Axis.YP.rotationDegrees(a1));
        pose.mulPose(Axis.XP.rotationDegrees(a2));
        pose.scale((0.15f + rand.nextFloat() * 0.075f) * size, (0.5f + rand.nextFloat() * 0.1f) * size,
                (0.15f + rand.nextFloat() * 0.05f) * size);
        int light = (int) (210.0f * shade);
        // o ModelCrystal: o ponto de giro em (0, 32, 0), girado de 0,7071 radiano em z e em x, em dezesseis avos
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        pose.translate(0.0f, 32.0f, 0.0f);
        pose.mulPose(Axis.ZP.rotation(0.7071f));
        pose.mulPose(Axis.XP.rotation(0.7071f));
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE),
                (m, c) -> MeshDrawer.draw(CRYSTAL, m, c, light, OverlayTexture.NO_OVERLAY, tint));
        pose.popPose();
    }

    /** O {@code translateFromOrientation}. */
    private static void orient(PoseStack pose, Direction facing) {
        switch (facing) {
            case DOWN -> {
                pose.translate(0.5f, 1.3f, 0.5f);
                pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            }
            case UP -> pose.translate(0.5f, -0.3f, 0.5f);
            case NORTH -> {
                pose.translate(0.5f, 0.5f, 1.3f);
                pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
            }
            case SOUTH -> {
                pose.translate(0.5f, 0.5f, -0.3f);
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case WEST -> {
                pose.translate(1.3f, 0.5f, 0.5f);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
            case EAST -> {
                pose.translate(-0.3f, 0.5f, 0.5f);
                pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            }
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /** O aglomerado na mão e no inventário: o {@code BlockCrystalRenderer}, virado de noventa graus. */
    public static class Item implements SpecialModelRenderer<Integer> {
        private static final List<String> KINDS = List.of("air", "fire", "water", "earth", "order", "entropy", "balanced");
        private final int kind;

        Item(String aspect) {
            this.kind = Math.max(0, KINDS.indexOf(aspect));
        }

        @Override
        public void submit(@Nullable Integer ignored, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil,
                           int tint) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            pose.translate(-0.5f, -0.5f, -0.5f);
            draw(pose, collector, this.kind, Direction.UP, BlockPos.ZERO, 0);
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public Integer extractArgument(ItemStack stack) {
            return this.kind;
        }
    }

    public record Unbaked(String aspect) implements SpecialModelRenderer.Unbaked<Integer> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("aspect").forGetter(Unbaked::aspect)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Integer> bake(SpecialModelRenderer.BakingContext context) {
            return new Item(this.aspect);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
