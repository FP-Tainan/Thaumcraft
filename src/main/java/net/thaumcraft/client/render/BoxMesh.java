package net.thaumcraft.client.render;

/**
 * Uma caixa de modelo com o desenrolado de textura que o Minecraft sempre usou.
 *
 * <p>É o mesmo mapeamento do {@code ModelRenderer.addBox} do jogo antigo, que é como os modelos do
 * Thaumcraft 4.2.3.5 foram desenhados: a folha traz o fundo e o topo lado a lado em cima, e as quatro
 * paredes numa fileira embaixo. Sem isto as texturas do mod original sairiam embaralhadas.
 */
public final class BoxMesh {
    private BoxMesh() {
    }

    /**
     * Os quatro cantos de cada uma das seis faces, na ordem em que o desenhista espera.
     *
     * <p>Cada canto são cinco números: x, y, z, u, v. A caixa vai de {@code (x,y,z)} e cresce
     * {@code (dx,dy,dz)}; a textura começa em {@code (u,v)} numa folha de {@code tw} por {@code th}.
     */
    public static float[] box(float x, float y, float z, float dx, float dy, float dz,
                              float u, float v, float tw, float th) {
        float x1 = x + dx;
        float y1 = y + dy;
        float z1 = z + dz;

        // as quatro paredes ficam numa fileira, depois do fundo e do topo
        float uz = dz;
        float ux = dx;
        float[][] faces = {
                // fundo
                face(x, y, z1, x1, y, z1, x1, y, z, x, y, z,
                        u + uz, v, u + uz + ux, v + uz, tw, th),
                // topo
                face(x, y1, z, x1, y1, z, x1, y1, z1, x, y1, z1,
                        u + uz + ux, v + uz, u + uz + ux * 2, v, tw, th),
                // norte
                face(x1, y1, z, x, y1, z, x, y, z, x1, y, z,
                        u + uz, v + uz, u + uz + ux, v + uz + dy, tw, th),
                // sul
                face(x, y1, z1, x1, y1, z1, x1, y, z1, x, y, z1,
                        u + uz * 2 + ux, v + uz, u + uz * 2 + ux * 2, v + uz + dy, tw, th),
                // oeste
                face(x, y1, z, x, y1, z1, x, y, z1, x, y, z,
                        u, v + uz, u + uz, v + uz + dy, tw, th),
                // leste
                face(x1, y1, z1, x1, y1, z, x1, y, z, x1, y, z1,
                        u + uz + ux, v + uz, u + uz * 2 + ux, v + uz + dy, tw, th),
        };
        float[] flat = new float[faces.length * 20];
        for (int i = 0; i < faces.length; i++) System.arraycopy(faces[i], 0, flat, i * 20, 20);
        return flat;
    }

    /** Junta várias caixas numa malha só. */
    public static float[] join(float[]... parts) {
        return joinAll(parts);
    }

    /**
     * A caixa espelhada, o {@code mirror} do {@code ModelRenderer} antigo: as faces de oeste e de leste trocam
     * de desenho, e cada face mostra o seu desenho de trás para a frente.
     */
    public static float[] mirror(float[] box) {
        float[] out = box.clone();
        // oeste e leste trocam de textura, cada canto com o seu par
        for (int corner = 0; corner < 4; corner++) {
            int west = 4 * 20 + corner * 5, east = 5 * 20 + corner * 5;
            out[west + 3] = box[east + 3];
            out[west + 4] = box[east + 4];
            out[east + 3] = box[west + 3];
            out[east + 4] = box[west + 4];
        }
        // e cada face vira no sentido do comprimento
        for (int face = 0; face < 6; face++) {
            int at = face * 20;
            float u0 = out[at + 3], u1 = out[at + 5 + 3];
            for (int corner = 0; corner < 4; corner++) {
                int u = at + corner * 5 + 3;
                out[u] = u0 + u1 - out[u];
            }
        }
        return out;
    }

    private static float[] joinAll(float[]... parts) {
        int size = 0;
        for (float[] part : parts) size += part.length;
        float[] all = new float[size];
        int at = 0;
        for (float[] part : parts) {
            System.arraycopy(part, 0, all, at, part.length);
            at += part.length;
        }
        return all;
    }

    private static float[] face(float ax, float ay, float az, float bx, float by, float bz,
                                float cx, float cy, float cz, float dx2, float dy2, float dz2,
                                float u0, float v0, float u1, float v1, float tw, float th) {
        float a = u0 / tw;
        float b = v0 / th;
        float c = u1 / tw;
        float d = v1 / th;
        return new float[]{
                ax, ay, az, a, b,
                bx, by, bz, c, b,
                cx, cy, cz, c, d,
                dx2, dy2, dz2, a, d,
        };
    }
}
