package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.client.render.model.AlembicModel;

/**
 * A cerca da malha do alambique.
 *
 * <p>O {@link AlembicModel} é gerado a partir do {@code alembic.obj} que veio dentro do Thaumcraft
 * 4.2.3.5, e é a coisa mais fácil do mod de se estragar sem ninguém notar: são milhares de números
 * soltos, e um deles fora do lugar torce uma peça sem quebrar nada que o compilador veja.
 *
 * <p>Então aqui estão as contagens e as caixas de cada peça, medidas no modelo original. Se alguém
 * editar a malha à mão, ou se o gerador for corrigido de um jeito que mude a geometria, o build para
 * aqui em vez de o alambique sair torto no mundo.
 *
 * <p>Os números estão em blocos, já com o giro de noventa graus em X que o modelo precisa — o autor o
 * desenhou com o Z para cima.
 */
public class AlembicModelGameTest {
    /** Cada peça e quantos cantos ela tem: são quatro por triângulo, com o último repetido. */
    @GameTest
    public void partsHaveTheOriginalTriangleCount(GameTestHelper helper) {
        corners(helper, "Pot", AlembicModel.POT, 64);
        corners(helper, "Legs", AlembicModel.LEGS, 60);
        corners(helper, "TubeMain", AlembicModel.TUBEMAIN, 12);
        corners(helper, "TubeSmall", AlembicModel.TUBESMALL, 8);
        corners(helper, "Panel", AlembicModel.PANEL, 12);
        helper.succeed();
    }

    /**
     * E cada peça continua ocupando o lugar que ocupava no modelo do autor.
     *
     * <p>É esta prova que pega um número trocado: contagem certa com vértice errado passa pela primeira e
     * para aqui.
     */
    @GameTest
    public void partsSitWhereTheOriginalPutThem(GameTestHelper helper) {
        // o corpo: quase a largura toda do bloco, do dedo acima do chão até quase o teto
        box(helper, "Pot", AlembicModel.POT, -0.440f, 0.100f, -0.381f, 0.440f, 0.900f, 0.381f);
        // os pés: rentes ao chão, e abrindo um pouco mais que o corpo
        box(helper, "Legs", AlembicModel.LEGS, -0.433f, 0.003f, -0.490f, 0.433f, 0.250f, 0.479f);
        // o bico, que atravessa o piso do bloco para entrar no que está embaixo
        box(helper, "TubeMain", AlembicModel.TUBEMAIN, -0.151f, -0.100f, -0.130f, 0.149f, 0.100f, 0.130f);
        // o encaixe fino, de lado
        box(helper, "TubeSmall", AlembicModel.TUBESMALL, -0.040f, -0.250f, -0.290f, 0.040f, 0.250f, -0.210f);
        // e o painel do rótulo, na face oeste — é dele que sai o giro conforme a face do bloco
        box(helper, "Panel", AlembicModel.PANEL, -0.406f, 0.347f, -0.125f, -0.326f, 0.597f, 0.125f);
        helper.succeed();
    }

    private static void corners(GameTestHelper helper, String name, float[] mesh, int triangles) {
        int expected = triangles * 4 * 8;
        if (mesh.length != expected) {
            helper.fail(name + ": " + (mesh.length / 8) + " cantos, esperado " + (expected / 8)
                    + " (" + triangles + " triângulos do modelo original)");
        }
    }

    private static void box(GameTestHelper helper, String name, float[] mesh,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        float[] low = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] high = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        for (int at = 0; at < mesh.length; at += 8) {
            for (int axis = 0; axis < 3; axis++) {
                low[axis] = Math.min(low[axis], mesh[at + axis]);
                high[axis] = Math.max(high[axis], mesh[at + axis]);
            }
        }
        float[] wanted = {x0, y0, z0, x1, y1, z1};
        float[] got = {low[0], low[1], low[2], high[0], high[1], high[2]};
        for (int i = 0; i < 6; i++) {
            if (Math.abs(got[i] - wanted[i]) > 0.001f) {
                helper.fail(name + ": a caixa está em " + list(got) + ", e no modelo original é " + list(wanted));
                return;
            }
        }
    }

    private static String list(float[] box) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < box.length; i++) {
            if (i > 0) out.append(", ");
            out.append(String.format(java.util.Locale.ROOT, "%.3f", box[i]));
        }
        return out.append(']').toString();
    }
}
