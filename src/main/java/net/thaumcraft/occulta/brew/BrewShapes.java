package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

/**
 * As formas que os efeitos de cozimento desenham no chão: o {@code BlockActionCircle} do Witchery.
 *
 * <p>O original risca o círculo pelo método de Bresenham — o mesmo que uma tela usa para desenhar uma
 * circunferência com números inteiros —, e não por conta de distância. A diferença aparece na borda: um bloco
 * aqui ou ali cai dentro ou fora. Está portado como lá para o desenho ser o mesmo.
 */
public final class BrewShapes {
    private BrewShapes() {
    }

    /** O {@code processFilledCircle}: o disco cheio, num andar só. */
    public static void filledCircle(BlockPos meio, int raio, Consumer<BlockPos> oQueFazer) {
        if (raio <= 1) {
            oQueFazer.accept(meio);
            return;
        }
        int x = raio - 1;
        int z = 0;
        int erro = 1 - x;
        while (x >= z) {
            line(meio, -x, x, z, oQueFazer);
            line(meio, -z, z, x, oQueFazer);
            line(meio, -x, x, -z, oQueFazer);
            line(meio, -z, z, -x, oQueFazer);
            z++;
            if (erro < 0) {
                erro += 2 * z + 1;
            } else {
                x--;
                erro += 2 * (z - x + 1);
            }
        }
    }

    private static void line(BlockPos meio, int de, int até, int dz, Consumer<BlockPos> oQueFazer) {
        for (int dx = de; dx <= até; dx++) {
            oQueFazer.accept(meio.offset(dx, 0, dz));
        }
    }

    /**
     * A bola cheia, por conta de distância: é o que os efeitos que não riscam círculo usam — a derrubada, a poda
     * e a pulverização percorrem o cubo e olham a distância, como no original.
     */
    public static void ball(BlockPos meio, int raio, Consumer<BlockPos> oQueFazer) {
        int raioQuadrado = raio * raio;
        for (int y = -raio; y <= raio; y++) {
            for (int x = -raio; x <= raio; x++) {
                for (int z = -raio; z <= raio; z++) {
                    if (x * x + y * y + z * z > raioQuadrado) continue;
                    oQueFazer.accept(meio.offset(x, y, z));
                }
            }
        }
    }
}
