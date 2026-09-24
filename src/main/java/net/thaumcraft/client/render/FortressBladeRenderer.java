package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.maleficium.FortressBladeItem;

import java.util.function.Consumer;

/**
 * As Lâminas de Fortaleza na mão e no inventário: o {@code RenderItemKatana}, o {@code ModelKatana} e o
 * {@code ModelSaya} do Tainted Magic 8.1.1.
 *
 * <p>Na mão e na casa do inventário vai só a lâmina, como uma espada; a bainha fica na cintura de quem a carrega
 * ({@code HipSheathLayer}). Quando a lâmina tem inscrição, as runas do {@code script.png} correm pelo fio, piscando
 * na cor que o original lhes dá.
 */
public class FortressBladeRenderer {
    private static final float UNIT = 1.0f / 16.0f;

    // as caixas do ModelKatana e do ModelSaya, já com o ponto de giro (0, −40, 0) somado
    private static final float[] BLADE = BoxMesh.join(
            // a lâmina: a caixa do original, que é o que faz a textura cair no lugar
            BoxMesh.box(-0.5f, -40.0f, -2.0f, 1, 48, 4, 0, 0, 32, 64),
            // a guarda e o punho
            BoxMesh.box(-2.5f, -40.0f, -3.5f, 5, 1, 7, 0, 52, 32, 64),
            BoxMesh.box(-1.0f, -52.0f, -1.5f, 2, 12, 3, 22, 0, 32, 64));
    private static final float[] SHEATH = BoxMesh.box(-1.0f, -39.5f, -2.0f, 2, 48, 4, 10, 0, 32, 64);

    /**
     * O corpo da lâmina: o modelo do original é fino e comprido demais para o tamanho que a lâmina tem na mão de
     * hoje — parecia um espeto. Ela engrossa quase o dobro e encurta um quinto, que é a silhueta de katana.
     */
    private static final float THICK_X = 1.8f, LONG_Y = 0.8f, THICK_Z = 1.6f;

    /** O desenho das runas que correm pelo fio da lâmina inscrita. */
    private static final Identifier SCRIPT = Identifier.withDefaultNamespace("textures/misc/script.png");

    private FortressBladeRenderer() {
    }

    /** A folha de cada lâmina, pelo nome do metal. */
    private static Identifier texture(String metal) {
        return Thaumcraft.id("textures/models/katana_" + metal + ".png");
    }

    /**
     * A bainha na cintura de quem carrega a lâmina, com a lâmina dentro quando ela não está na mão: o
     * {@code render} do {@code IRenderInventoryItem}.
     */
    public static void sheath(PoseStack pose, SubmitNodeCollector collector, net.minecraft.world.item.ItemStack stack,
                              int light, boolean bladeInHand) {
        Identifier folha = texture(metalOf(stack));
        pose.pushPose();
        // de lado a bainha é só o fio dela: mostra-se a face larga, como a lâmina
        pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        pose.scale(0.45f, 0.45f, 0.45f);
        pose.translate(0.0f, 22.0f * UNIT, 0.0f);
        pose.scale(THICK_X, LONG_Y, THICK_Z);
        Blade.box(pose, collector, folha, SHEATH, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        if (!bladeInHand) Blade.box(pose, collector, folha, BLADE, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** De que metal é a lâmina, pelo nome do item. */
    private static String metalOf(net.minecraft.world.item.ItemStack stack) {
        String nome = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int corte = nome.indexOf('_');
        return corte < 0 ? "thaumium" : nome.substring(0, corte);
    }

    /** O que o arquivo do item diz: de que metal a lâmina é. */
    public record Unbaked(String metal) implements SpecialModelRenderer.Unbaked<Boolean> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.STRING.fieldOf("metal").forGetter(Unbaked::metal)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Boolean> bake(SpecialModelRenderer.BakingContext context) {
            return new Blade(texture(this.metal));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }

    /** A lâmina desenhada: o que passa é se ela tem inscrição, que é o que acende as runas. */
    public record Blade(Identifier texture) implements SpecialModelRenderer<Boolean> {
        @Override
        public void submit(@org.jetbrains.annotations.Nullable Boolean inscribed, PoseStack pose,
                           SubmitNodeCollector collector, int light, int overlay, boolean foil, int tint) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            // de lado a lâmina é só o fio: quem a segura vê a face larga
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            pose.scale(0.45f, 0.45f, 0.45f);
            pose.translate(0.0f, 22.0f * UNIT, 0.0f);
            // a lâmina do modelo é fina demais para o tamanho que ela tem na mão: engrossa sem esticar
            pose.scale(THICK_X, LONG_Y, THICK_Z);
            box(pose, collector, this.texture, BLADE, light, overlay);
            if (Boolean.TRUE.equals(inscribed)) runes(pose, collector, light, overlay);
            pose.popPose();
        }

        /** As vinte e oito runas do {@code drawRune}, catorze de cada lado do fio. */
        private static void runes(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
            int time = (int) (System.currentTimeMillis() / 50L % 24000L);
            for (int side = 0; side < 2; side++) {
                for (int a = 0; a < 14; a++) {
                    int rune = side == 0 ? a * 3 % 16 : (a + 3) % 16;
                    float red = Mth.sin((time + rune * 5) / 5.0f) * 0.1f + 0.88f;
                    float green = Mth.sin((time + rune * 5) / 7.0f) * 0.1f + 0.63f;
                    float alpha = Mth.sin((time + rune * 5) / 10.0f) * 0.3f + 0.6f;
                    int colour = (int) (alpha * 255) << 24 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | 51;
                    pose.pushPose();
                    pose.translate(0.0f, (-36.0f + a * 2.25f) * UNIT, side == 0 ? -1.4f * UNIT : 1.4f * UNIT);
                    pose.scale(2.0f * UNIT, 2.0f * UNIT, 2.0f * UNIT);
                    final int rgba = colour;
                    final int quadro = rune;
                    collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(SCRIPT),
                            (matrix, consumer) -> rune(matrix, consumer, quadro, light, overlay, rgba));
                    pose.popPose();
                }
            }
        }

        /** Uma runa: um quadro dos dezesseis da folha, de frente e de costas. */
        private static void rune(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer, int rune,
                                 int light, int overlay, int colour) {
            float u0 = 0.0625f * rune;
            float u1 = u0 + 0.0625f;
            float[][] corners = {{-0.5f, 0.5f, u1, 0.0f}, {0.5f, 0.5f, u0, 0.0f},
                    {0.5f, -0.5f, u0, 1.0f}, {-0.5f, -0.5f, u1, 1.0f}};
            for (int face = 0; face < 2; face++) {
                for (int i = 0; i < 4; i++) {
                    float[] c = corners[face == 0 ? i : 3 - i];
                    consumer.addVertex(matrix, c[0], c[1], 0.0f).setColor(colour).setUv(c[2], c[3])
                            .setOverlay(overlay).setLight(light)
                            .setNormal(matrix, 0.0f, 0.0f, face == 0 ? -1.0f : 1.0f);
                }
            }
        }

        static void box(PoseStack pose, SubmitNodeCollector collector, Identifier texture, float[] mesh,
                                int light, int overlay) {
            pose.pushPose();
            pose.scale(UNIT, UNIT, UNIT);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(texture),
                    (matrix, consumer) -> MeshDrawer.draw(mesh, matrix, consumer, light, overlay, 0xFFFFFFFF));
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<org.joml.Vector3fc> extents) {
            extents.accept(new org.joml.Vector3f(-0.5f, -0.5f, -0.5f));
            extents.accept(new org.joml.Vector3f(1.5f, 1.5f, 1.5f));
        }

        @Override
        public Boolean extractArgument(ItemStack stack) {
            return FortressBladeItem.inscription(stack) >= 0;
        }
    }
}
