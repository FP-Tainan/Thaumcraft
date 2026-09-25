package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A foice na mão e no inventário: o modelo de Blender que o Necromancy carrega por fora do jar.
 *
 * <p>São triângulos, e o jogo só sabe desenhar quadrados, então cada triângulo vai com o último canto repetido —
 * é o que se faz sempre que uma malha de fora entra aqui. A parte de ferro sai com uma folha e a de pano com
 * outra, como no {@code .mtl} do original.
 */
public record ScytheItemRenderer() implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // a malha vem centrada na origem; o jogo desenha o item na casa de zero a um
        pose.translate(0.5f, 0.5f, 0.5f);
        part(pose, collector, ScytheMesh.blade(), ScytheMesh.BLADE_TEXTURE, light, overlay);
        part(pose, collector, ScytheMesh.cloth(), ScytheMesh.CLOTH_TEXTURE, light, overlay);
        pose.popPose();
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] malha, Identifier folha,
                             int light, int overlay) {
        if (malha.length == 0) return;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha), (m, v) -> {
            for (int canto = 0; canto + ScytheMesh.STRIDE * 3 <= malha.length; canto += ScytheMesh.STRIDE * 3) {
                for (int i = 0; i < 4; i++) {
                    // o quarto canto é o terceiro outra vez: é o triângulo posto num quadrado
                    vertex(m, v, malha, canto + ScytheMesh.STRIDE * Math.min(i, 2), light, overlay);
                }
            }
        });
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer v, float[] malha, int at, int light, int overlay) {
        v.addVertex(m, malha[at], malha[at + 1], malha[at + 2])
                .setColor(-1)
                .setUv(malha[at + 3], malha[at + 4])
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(m, malha[at + 5], malha[at + 6], malha[at + 7]);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, -0.5f, -0.5f));
        extents.accept(new Vector3f(1.5f, 1.5f, 1.5f));
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
            return new ScytheItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
