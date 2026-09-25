package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O Altar de Invocação na mão e no inventário: o {@code IItemRenderer} do {@code TileEntityAltarRenderer}, que
 * desenha o altar inteiro encolhido para caber numa casa — a mesa dele tem três blocos de comprimento.
 */
public record SummoningAltarItemRenderer() implements SpecialModelRenderer<Unit> {
    /** O altar tem três blocos de ponta a ponta; encolhido a um terço, cabe no lugar de um. */
    private static final float SCALE = 1.0f / 3.0f;

    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.scale(SCALE, SCALE, SCALE);
        // o altar vai de -0,5 a 2,5 de comprimento e de 0 a 1,5 de altura: o meio dele é este ponto
        pose.translate(-1.0f, -0.75f, 0.0f);
        pose.translate(0.5f, 1.5f, 0.5f);
        pose.scale(1.0f, -1.0f, -1.0f);
        SummoningAltarRenderer.boxes(pose, collector, light);
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public Unit extractArgument(ItemStack stack) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new SummoningAltarItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
