package net.thaumcraft.occulta.ice;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * <b>A esfera</b>: o {@code BlockActionSphere} do Witchery.
 *
 * <p>É a conta que o mod usa sempre que uma coisa tem de <b>cobrir uma bola</b> do mundo — o Rito da Expansão
 * Gelada e o Cozimento da Casca de Gelo são os dois que a usam aqui.
 *
 * <p>Ela é o <b>método de Bresenham em três dimensões</b>: risca-se um círculo e, para cada ponto dele,
 * risca-se outro círculo perpendicular, espelhando os oito octantes nos três eixos. Dá trinta e dois pontos
 * por volta, sem uma única raiz quadrada — que era como se faziam estas coisas quando a máquina não dava para
 * mais.
 *
 * <p>E ela é <b>oca</b>: o que ela risca é a casca. Quem a usa risca a casca com o bloco que quer e depois
 * {@linkplain #enche enche} o de dentro.
 */
public final class IceSphere {
    private IceSphere() {
    }

    /**
     * A <b>casca</b> de uma esfera deste raio.
     *
     * <p><b>Fiel ao original:</b> o raio <b>desconta um</b> antes de começar. Uma esfera de raio oito tem
     * casca de sete, e é por isso que as esferas do mod são sempre um pouco menores do que o número diz.
     */
    public static void casca(BlockPos meio, int raio, Consumer<BlockPos> oquê) {
        if (raio == 1) {
            oquê.accept(meio);
            return;
        }
        int x = raio - 1;
        int y = 0;
        int erro = 1 - x;
        while (x >= y) {
            anel(meio, y, x, erro, oquê);
            y++;
            if (erro < 0) {
                erro += 2 * y + 1;
            } else {
                x--;
                erro += 2 * (y - x + 1);
            }
        }
    }

    /** Um anel da casca, com os trinta e dois pontos que os três eixos dão. */
    private static void anel(BlockPos meio, int altura, int raio, int erro0, Consumer<BlockPos> oquê) {
        int x = raio;
        int z = 0;
        int erro = erro0;
        while (x >= z) {
            for (int sinalAltura = 1; sinalAltura >= -1; sinalAltura -= 2) {
                int h = altura * sinalAltura;
                põe(meio, x, h, z, oquê);
                põe(meio, -x, h, z, oquê);
                põe(meio, x, h, -z, oquê);
                põe(meio, -x, h, -z, oquê);
                põe(meio, z, h, x, oquê);
                põe(meio, -z, h, x, oquê);
                põe(meio, z, h, -x, oquê);
                põe(meio, -z, h, -x, oquê);
            }
            for (int sinalX = 1; sinalX >= -1; sinalX -= 2) {
                int a = x * sinalX;
                põe(meio, altura, a, z, oquê);
                põe(meio, -altura, a, z, oquê);
                põe(meio, altura, a, -z, oquê);
                põe(meio, -altura, a, -z, oquê);
                põe(meio, z, a, altura, oquê);
                põe(meio, -z, a, altura, oquê);
                põe(meio, z, a, -altura, oquê);
                põe(meio, -z, a, -altura, oquê);
            }
            z++;
            if (erro < 0) {
                erro += 2 * z + 1;
            } else {
                x--;
                erro += 2 * (z - x + 1);
            }
        }
    }

    private static void põe(BlockPos meio, int dx, int dy, int dz, Consumer<BlockPos> oquê) {
        oquê.accept(meio.offset(dx, dy, dz));
    }

    /**
     * E o <b>recheio</b>: o {@code fillWith} do original.
     *
     * <p>Do meio para fora, nos seis sentidos, até bater na casca — e o que estiver pelo caminho e for
     * <b>água, água correndo ou vapor de cozimento</b> vira o bloco de encher. O resto fica.
     *
     * <p>É o que faz uma casca de gelo no meio de um lago ficar com <b>ar</b> dentro em vez de água: a bolha
     * é a parte útil, e sem este passo a esfera seria só uma casca à volta de um afogamento.
     *
     * <p><b>Engano do original que fica:</b> na varredura em X ele pergunta pelo bloco em
     * {@code (realX, x, posZ)} — o contador do laço no lugar da altura. A parada por casca, nesse sentido, é
     * lida na altura errada, e por isso o recheio às vezes atravessa a casca de lado. Fica como está: é o que
     * o jogo de 2014 faz, e a bolha sai com o mesmo feitio torto que ele dá.
     */
    public static void enche(ServerLevel level, BlockPos meio, int raio, Block comQuê, Block borda) {
        emY(level, meio, 1, raio, comQuê, borda);
        emY(level, meio.below(), -1, raio, comQuê, borda);
    }

    private static void emY(ServerLevel level, BlockPos de, int passo, int raio, Block comQuê, Block borda) {
        for (int y = 0; y <= raio; y++) {
            BlockPos aqui = de.offset(0, y * passo, 0);
            if (level.getBlockState(aqui).is(borda)) return;
            emX(level, aqui, 1, raio, comQuê, borda);
            emX(level, aqui.west(), -1, raio, comQuê, borda);
        }
    }

    private static void emX(ServerLevel level, BlockPos de, int passo, int raio, Block comQuê, Block borda) {
        for (int x = 0; x <= raio; x++) {
            BlockPos aqui = de.offset(x * passo, 0, 0);
            // o engano do original: o contador do laço entra no lugar da altura
            if (level.getBlockState(new BlockPos(aqui.getX(), x, aqui.getZ())).is(borda)) return;
            emZ(level, aqui, 1, raio, comQuê, borda);
            emZ(level, aqui.north(), -1, raio, comQuê, borda);
        }
    }

    private static void emZ(ServerLevel level, BlockPos de, int passo, int raio, Block comQuê, Block borda) {
        for (int z = 0; z <= raio; z++) {
            BlockPos aqui = de.offset(0, 0, z * passo);
            var achou = level.getBlockState(aqui);
            if (achou.is(borda)) return;
            if (achou.is(comQuê)) continue;
            if (achou.is(Blocks.WATER) || achou.is(net.thaumcraft.occulta.OccultaBlocks.BREW_GAS)) {
                level.setBlockAndUpdate(aqui, comQuê.defaultBlockState());
            }
        }
    }
}
