package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O Aurem Negro na mão e no inventário: a mesma figura chapada, parada.
 *
 * <p>O original tem para isto o outro ramo do {@code renderArsMagicaEffect} — o das coordenadas
 * {@code 0,0,0}: em vez de virar a figura para quem olha, gira-a <b>35 graus</b> e baixa-a três quartos.
 */
public class BlackAuremItemRenderer implements SpecialModelRenderer<net.minecraft.util.Unit> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/black_aurem.png");
    private static final float ESCALA = 2.0f;
    private static final float MEIO = 0.25f;
    /** O {@code glRotatef(35, 0, 1, 0)} do original. */
    private static final float VIRA = 35.0f;
    /** E o {@code glTranslatef(0, -0.75, 0)}. */
    private static final float DESCE = -0.75f;

    @Override
    public void submit(@Nullable net.minecraft.util.Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(VIRA));
        pose.translate(0.0f, DESCE, 0.0f);
        pose.translate(0.0f, MEIO, 0.0f);
        pose.scale(ESCALA, ESCALA, ESCALA);
        pose.translate(0.0f, -MEIO, 0.0f);
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(PELE), (m, v) -> {
            canto(m, v, -0.5f, -0.25f, 0.0f, 1.0f, overlay);
            canto(m, v, 0.5f, -0.25f, 1.0f, 1.0f, overlay);
            canto(m, v, 0.5f, 0.75f, 1.0f, 0.0f, overlay);
            canto(m, v, -0.5f, 0.75f, 0.0f, 0.0f, overlay);
        });
        pose.popPose();
    }

    private static void canto(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer v,
                              float x, float y, float u, float w, int overlay) {
        v.addVertex(m, x, y, 0.0f).setColor(0xFFFFFFFF).setUv(u, w)
                .setOverlay(overlay).setLight(0xF000F0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, -0.5f, -0.5f));
        extents.accept(new Vector3f(0.5f, 0.5f, 0.5f));
    }

    @Override
    public net.minecraft.util.Unit extractArgument(ItemStack pilha) {
        return net.minecraft.util.Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext contexto) {
            return new BlackAuremItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
