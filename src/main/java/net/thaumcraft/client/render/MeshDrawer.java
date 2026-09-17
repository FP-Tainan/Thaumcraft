package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Desenha as malhas que vieram dos arquivos de modelo do Thaumcraft 4.2.3.5.
 *
 * <p>Cada linha da tabela é uma face de quatro cantos. A face aponta para onde mandam dois lados dela — sem
 * essa conta a luz deixa a peça chapada.
 */
public final class MeshDrawer {
    private MeshDrawer() {
    }

    public static void draw(float[] quads, PoseStack.Pose matrix, VertexConsumer consumer,
                            int light, int overlay, int color) {
        for (int face = 0; face < quads.length; face += 20) {
            float ax = quads[face + 5] - quads[face];
            float ay = quads[face + 6] - quads[face + 1];
            float az = quads[face + 7] - quads[face + 2];
            float bx = quads[face + 15] - quads[face];
            float by = quads[face + 16] - quads[face + 1];
            float bz = quads[face + 17] - quads[face + 2];
            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;
            float size = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (size > 1.0e-5f) {
                nx /= size;
                ny /= size;
                nz /= size;
            }
            for (int corner = 0; corner < 4; corner++) {
                int at = face + corner * 5;
                consumer.addVertex(matrix, quads[at], quads[at + 1], quads[at + 2])
                        .setColor(color)
                        .setUv(quads[at + 3], quads[at + 4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(matrix, nx, ny, nz);
            }
        }
    }
}
