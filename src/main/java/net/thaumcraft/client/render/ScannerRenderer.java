package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O thaumômetro como ele é na 4.2.3.5: um aro hexagonal de latão com runas e seis pedras, e um vidro
 * azul-violeta por dentro. Na versão original ele nunca foi um desenho chapado — sempre foi peça de três
 * dimensões, e é assim que ele aparece aqui.
 */
public class ScannerRenderer implements SpecialModelRenderer<Void> {
    private static final Identifier BRASS = Thaumcraft.id("textures/item/scanner.png");
    private static final Identifier LENS = Thaumcraft.id("textures/item/scanscreen.png");
    /** A malha vem nas medidas do arquivo do mod; isto a traz para o tamanho de um item na mão. */
    private static final float SCALE = 0.33f;
    /** O vidro vai um pouco além do vazio do aro, para encostar no latão. */
    private static final float LENS_REACH = 1.20f;

    @Override
    public void submit(@Nullable Void argument, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // o modelo do original foi feito deitado no chão: aqui ele fica de pé, virado para quem olha
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.scale(SCALE, SCALE, SCALE);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRASS), (matrix, consumer) ->
                MeshDrawer.draw(ScannerMesh.QUADS, matrix, consumer, light, overlay, 0xFFFFFFFF));
        // o vidro é um hexágono como o aro, mas desenhado de lado: um sexto de volta o encaixa
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(30.0f));
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(LENS), (matrix, consumer) -> {
            lens(matrix, consumer, overlay, -0.01f);
            lens(matrix, consumer, overlay, 0.01f);
        });
        pose.popPose();
        pose.popPose();
    }

    /** O vidro, no mesmo plano do aro e dos dois lados, para se ver de frente e de costas. */
    private static void lens(PoseStack.Pose matrix, VertexConsumer consumer, int overlay, float depth) {
        vertex(matrix, consumer, -LENS_REACH, depth, -LENS_REACH, 0.0f, 1.0f, overlay);
        vertex(matrix, consumer, LENS_REACH, depth, -LENS_REACH, 1.0f, 1.0f, overlay);
        vertex(matrix, consumer, LENS_REACH, depth, LENS_REACH, 1.0f, 0.0f, overlay);
        vertex(matrix, consumer, -LENS_REACH, depth, LENS_REACH, 0.0f, 0.0f, overlay);
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int overlay) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(0x99FFFFFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, -0.1f, -0.5f));
        extents.accept(new Vector3f(0.5f, 0.1f, 0.5f));
    }

    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new ScannerRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
