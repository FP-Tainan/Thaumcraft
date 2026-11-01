package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Despeja no desenhista do jogo uma malha vinda de um modelo do mod original.
 *
 * <p>É irmã do {@link MeshDrawer}, com uma diferença: aquele monta caixas e calcula a normal de cada face
 * na hora, porque caixa tem face plana e óbvia. Aqui a malha vem de um programa de modelagem, com normal
 * própria em cada canto — é ela que faz a barriga do alambique parecer curva em vez de facetada, e por
 * isso ela vem junto no arquivo em vez de ser recalculada.
 *
 * <p>Cada canto são oito números seguidos: posição, textura e normal. Quatro cantos fazem um
 * quadrilátero, que é a figura que o desenhista do jogo espera; triângulo vem com o último canto
 * repetido.
 */
public final class ObjMesh {
    /** Quantos números tem cada canto. */
    public static final int STRIDE = 8;

    private ObjMesh() {
    }

    public static void draw(float[] mesh, PoseStack.Pose matrix, VertexConsumer consumer,
                            int light, int overlay, int colour) {
        draw(mesh, matrix, consumer, light, overlay, colour, 0.0f, 0.0f);
    }

    /**
     * A mesma malha com a textura <b>corrida</b>.
     *
     * <p>É o que o Ars Magica 2 faz com as runas do Obelisco: ele mexe na matriz de textura do OpenGL para
     * a figura escorregar devagar por cima do modelo. Hoje não há matriz de textura, e o mesmo se consegue
     * somando o deslocamento a cada canto.
     */
    public static void draw(float[] mesh, PoseStack.Pose matrix, VertexConsumer consumer,
                            int light, int overlay, int colour, float u, float v) {
        for (int at = 0; at < mesh.length; at += STRIDE) {
            consumer.addVertex(matrix, mesh[at], mesh[at + 1], mesh[at + 2])
                    .setColor(colour)
                    .setUv(mesh[at + 3] + u, mesh[at + 4] + v)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(matrix, mesh[at + 5], mesh[at + 6], mesh[at + 7]);
        }
    }
}
