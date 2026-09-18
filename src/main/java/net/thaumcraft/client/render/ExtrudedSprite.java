package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Um desenho chato engrossado, como o Minecraft desenha os itens na mão.
 *
 * <p>É o {@code ItemRenderer.renderItemIn2D} de 1.7.10: a figura na frente, a mesma figura atrás, e entre
 * as duas uma tira fina para cada coluna e cada linha de pontos, pintada com aquela coluna ou linha da
 * própria figura. Com o recorte pelo alfa, as tiras só aparecem onde a figura tem ponto — e é isso que
 * dá aos itens a borda em degrauzinho, com a espessura de um ponto.
 *
 * <p>O Thaumcraft usa isto para peças que são uma figura com corpo, como a roda da válvula.
 */
public final class ExtrudedSprite {
    private ExtrudedSprite() {
    }

    /**
     * Desenha a figura inteira ocupando o quadrado de zero a um em X e Y, com a espessura para trás
     * (de zero a {@code -thickness} em Z).
     *
     * @param pixels quantos pontos a figura tem de lado
     */
    public static void draw(PoseStack.Pose matrix, VertexConsumer consumer, int pixels, float thickness,
                            int light, int overlay, int colour) {
        float back = -thickness;
        // a frente e o verso
        quad(matrix, consumer, light, overlay, colour, 0.0f, 0.0f, 1.0f,
                0, 0, 0, 1, 1, 0,
                1, 0, 0, 0, 1, 0,
                1, 1, 0, 0, 0, 0,
                0, 1, 0, 1, 0, 0);
        quad(matrix, consumer, light, overlay, colour, 0.0f, 0.0f, -1.0f,
                0, 1, back, 1, 0, 0,
                1, 1, back, 0, 0, 0,
                1, 0, back, 0, 1, 0,
                0, 0, back, 1, 1, 0);

        float step = 1.0f / pixels;
        // um quarto de ponto para dentro, para a tira pegar a coluna certa e não a vizinha
        float inset = step * 0.5f;
        for (int i = 0; i < pixels; i++) {
            float at = i * step;
            float u = 1.0f - (at + inset);
            // a tira que olha para a esquerda e a que olha para a direita, na coluna i
            quad(matrix, consumer, light, overlay, colour, -1.0f, 0.0f, 0.0f,
                    at, 0, back, u, 1, 0,
                    at, 0, 0, u, 1, 0,
                    at, 1, 0, u, 0, 0,
                    at, 1, back, u, 0, 0);
            quad(matrix, consumer, light, overlay, colour, 1.0f, 0.0f, 0.0f,
                    at + step, 1, back, u, 0, 0,
                    at + step, 1, 0, u, 0, 0,
                    at + step, 0, 0, u, 1, 0,
                    at + step, 0, back, u, 1, 0);

            float v = 1.0f - (at + inset);
            // e as tiras de cima e de baixo, na linha i
            quad(matrix, consumer, light, overlay, colour, 0.0f, 1.0f, 0.0f,
                    0, at + step, 0, 1, v, 0,
                    1, at + step, 0, 0, v, 0,
                    1, at + step, back, 0, v, 0,
                    0, at + step, back, 1, v, 0);
            quad(matrix, consumer, light, overlay, colour, 0.0f, -1.0f, 0.0f,
                    1, at, 0, 0, v, 0,
                    0, at, 0, 1, v, 0,
                    0, at, back, 1, v, 0,
                    1, at, back, 0, v, 0);
        }
    }

    /**
     * Um quadrilátero: quatro cantos de {@code x, y, z, u, v} com um zero sobrando no fim de cada um
     * só para os números alinharem na leitura.
     */
    private static void quad(PoseStack.Pose matrix, VertexConsumer consumer, int light, int overlay, int colour,
                             float nx, float ny, float nz, float... corners) {
        for (int c = 0; c < 4; c++) {
            int at = c * 6;
            consumer.addVertex(matrix, corners[at], corners[at + 1], corners[at + 2])
                    .setColor(colour)
                    .setUv(corners[at + 3], corners[at + 4])
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(matrix, nx, ny, nz);
        }
    }
}
