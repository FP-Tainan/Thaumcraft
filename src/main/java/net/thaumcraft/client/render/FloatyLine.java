package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * A linha ondulante do Thaumcraft: o {@code UtilsFX.drawFloatyLine} da 4.2.3.5, descompilado.
 *
 * <p>É o que se vê saindo de um nó até a ponta da varinha que bebe dele. São duas fitas cruzadas — uma de
 * pé e uma deitada, cada uma com três décimos de largura —, cortadas em oito pedaços por bloco. O meio da
 * linha ondula em três senos, um por eixo, enquanto as pontas ficam presas; o brilho é zero nas pontas e
 * cheio no meio; e a textura corre ao longo dela, que é o que dá a impressão de a essência estar
 * escorrendo do nó para a varinha. A luz soma, como toda fumaça mágica do mod.
 *
 * <p>Ela também cresce: com {@code grow} abaixo de um, só o trecho perto do nó é desenhado. A varinha
 * leva dez tiques para a linha chegar até ela.
 */
public final class FloatyLine {
    public static final Identifier WISPY = Thaumcraft.id("textures/misc/wispy.png");
    /** O {@code golemLinkQuality} do original, dezesseis: a linha tem metade disso em pedaços por bloco. */
    private static final float QUALITY = 16.0f;

    private FloatyLine() {
    }

    /**
     * Desenha a linha com a pose na ponta de chegada — o nó, no caso da drenagem.
     *
     * @param from  a ponta de partida, em coordenadas de mundo (a da varinha)
     * @param to    a ponta de chegada, em coordenadas de mundo (o meio do nó)
     * @param grow  quanto da linha já saiu, de zero a um
     * @param speed o quanto a textura corre por passo de tempo; negativo corre para a partida
     * @param width a meia largura de cada fita
     */
    public static void submit(PoseStack pose, SubmitNodeCollector collector, Vec3 from, Vec3 to, int colour,
                              float grow, float speed, float width) {
        submit(pose, collector, from, to, colour, grow, speed, width, AdditiveGlow.of(WISPY), -1.0f);
    }

    /**
     * A mesma linha com outro jeito de pintar: o fio do ritual dos clérigos ({@code RenderCultist.drawFloatyLine}) mistura
     * por transparência em vez de somar luz e tem o brilho fixo em oito décimos.
     *
     * @param fixedAlpha o brilho de toda a linha; negativo, o de sempre (zero nas pontas, cheio no meio)
     */
    public static void submit(PoseStack pose, SubmitNodeCollector collector, Vec3 from, Vec3 to, int colour,
                              float grow, float speed, float width, net.minecraft.client.renderer.rendertype.RenderType type, float fixedAlpha) {
        double cx = from.x - to.x, cy = from.y - to.y, cz = from.z - to.z;
        float dist = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
        float length = Math.round(dist) * (QUALITY / 2.0f);
        if (length <= 0.0f) return;
        int steps = (int) (length * grow);
        if (steps < 1) return;
        // o relógio do original: nanossegundos em passos de trinta milésimos
        float time = (float) (net.minecraft.util.Util.getNanos() / 30_000_000L);

        int count = steps + 1;
        double[] px = new double[count], py = new double[count], pz = new double[count];
        float[] alpha = new float[count], u = new float[count];
        for (int i = 0; i < count; i++) {
            float f2 = i / length;
            float f3 = 1.0f - Math.abs(i - length / 2.0f) / (length / 2.0f);
            double along = dist * (1.0f - f2) * QUALITY / 2.0f - time % 32767.0f / 5.0f;
            double dx = cx + Math.sin((from.z % 16.0 + along) / 4.0) * 0.5f * f3;
            double dy = cy + Math.sin((from.x % 16.0 + along) / 3.0) * 0.5f * f3;
            double dz = cz + Math.sin((from.y % 16.0 + along) / 2.0) * 0.5f * f3;
            px[i] = dx * f2;
            py[i] = dy * f2;
            pz[i] = dz * f2;
            alpha[i] = fixedAlpha >= 0.0f ? fixedAlpha : Math.max(0.0f, f3);
            u[i] = (1.0f - f2) * dist - time * speed;
        }

        int rgb = colour & 0xFFFFFF;
        collector.submitCustomGeometry(pose, type, (matrix, consumer) -> {
            for (int i = 0; i < steps; i++) {
                // a fita de pé
                ribbon(matrix, consumer, rgb, px, py, pz, alpha, u, i, 0.0f, width);
                // e a deitada
                ribbon(matrix, consumer, rgb, px, py, pz, alpha, u, i, width, 0.0f);
            }
        });
    }

    /** Um pedaço de fita entre o ponto {@code i} e o seguinte, dos dois lados, porque ela não tem costas. */
    private static void ribbon(PoseStack.Pose matrix, VertexConsumer consumer, int rgb, double[] px, double[] py,
                               double[] pz, float[] alpha, float[] u, int i, float wx, float wy) {
        int j = i + 1;
        corner(matrix, consumer, px[i] - wx, py[i] - wy, pz[i], u[i], 1.0f, rgb, alpha[i]);
        corner(matrix, consumer, px[i] + wx, py[i] + wy, pz[i], u[i], 0.0f, rgb, alpha[i]);
        corner(matrix, consumer, px[j] + wx, py[j] + wy, pz[j], u[j], 0.0f, rgb, alpha[j]);
        corner(matrix, consumer, px[j] - wx, py[j] - wy, pz[j], u[j], 1.0f, rgb, alpha[j]);

        corner(matrix, consumer, px[j] - wx, py[j] - wy, pz[j], u[j], 1.0f, rgb, alpha[j]);
        corner(matrix, consumer, px[j] + wx, py[j] + wy, pz[j], u[j], 0.0f, rgb, alpha[j]);
        corner(matrix, consumer, px[i] + wx, py[i] + wy, pz[i], u[i], 0.0f, rgb, alpha[i]);
        corner(matrix, consumer, px[i] - wx, py[i] - wy, pz[i], u[i], 1.0f, rgb, alpha[i]);
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer consumer, double x, double y, double z,
                               float u, float v, int rgb, float alpha) {
        consumer.addVertex(matrix, (float) x, (float) y, (float) z)
                .setColor(((int) (alpha * 255.0f) << 24) | rgb)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
