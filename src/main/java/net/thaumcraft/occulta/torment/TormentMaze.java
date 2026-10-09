package net.thaumcraft.occulta.torment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O labirinto do Tormento, casa por casa: a {@code GenerateMaze} e o {@code populate} do
 * {@code WorldChunkManagerTorment} do Witchery, juntos.
 *
 * <p>O original monta o labirinto no instante em que o pedaço (0,0) nasce e o <b>escreve no mundo</b> com
 * um {@code setBlock} de cada vez, atravessando pedaços que ainda não existem — coisa que o jogo de 2014
 * deixava e o de hoje não. Aqui ele é <b>desenhado primeiro</b>, inteiro, numa planta de papel, e o gerador
 * só copia dela o pedaço que lhe toca. O desenho é o mesmo e a ordem dos sorteios é a mesma; o que muda é
 * que ele passa a ser uma <b>conta</b> em vez de uma escrita.
 *
 * <h2>Como ele é</h2>
 *
 * <p>São <b>seis</b> labirintos, um por andar, de quinze em quinze de altura a partir do décimo. Cada um é
 * uma grelha de <b>trinta e uma por trinta e uma</b> casas desenhada a <b>duas casas por casa</b>, o que dá
 * sessenta e três por sessenta e três blocos, e o caminho entre elas sai de uma <b>busca em profundidade
 * embaralhada</b> — a mesma que o original usa, com os quatro bits por casa.
 *
 * <p>As paredes são de <b>Força</b>, que não se vê, e o chão é de <b>Pedra do Tormento</b>, que tem a cara
 * do micélio. De cem casas de chão, <b>uma</b> é micélio de verdade. O teto também é de Força, de modo que
 * tudo o que se vê, lá dentro, é o chão se perdendo no escuro.
 *
 * <p>Depois do labirinto vêm as salas, e <b>por esta ordem</b>, porque os dois desvios das salas do meio
 * saem do mesmo sorteio em fila:
 *
 * <ol>
 *   <li>a <b>câmara de entrada</b>, de sete por sete, onde quem chega cai;</li>
 *   <li>a <b>câmara de saída</b>, de sete por nove, que avança uma fila para fora do labirinto;</li>
 *   <li>o <b>portal</b>, com a moldura de três de Pedra do Tormento dos dois lados;</li>
 *   <li>a <b>sala da esquerda</b>, de cinco por cinco, desviada ao acaso em até cinco para o norte ou para
 *       o sul, com um <b>Baú de Reabastecimento</b> no meio;</li>
 *   <li>a <b>sala da direita</b>, igual, com outro desvio e outro baú;</li>
 *   <li>e a <b>sala do meio</b>, de sete por sete, com o terceiro baú — e é nela que o <b>Senhor do
 *       Tormento</b> espera, quando é ele que arrasta alguém para cá.</li>
 * </ol>
 *
 * <p>Os baús ficam <b>no topo do chão</b> e não sobre ele: eles estão <b>enterrados</b>, com a tampa ao
 * nível dos pés de quem passa. É o que o {@code drawChest} do original faz, e fica.
 */
public final class TormentMaze {
    /** Quantos andares: os seis do {@code NUM_LEVELS}. */
    public static final int LEVELS = 6;

    /** Onde o primeiro assenta: o {@code BASE_LEVEL}. */
    public static final int BASE = 10;

    /** E de quanto em quanto sobem: o {@code LEVEL_HEIGHT}. */
    public static final int STEP = 15;

    /** O lado da grelha, em casas: o {@code MAZE_SIZE}. */
    public static final int SIZE = 31;

    /** O quanto uma parede tem de alto: o {@code WALL_HEIGHT}. */
    public static final int WALL = 6;

    /** O canto do desenho, que sai do {@code 0 * 16 + 8 - 31} e do {@code 0 * 16 + 8 - 2} do original. */
    public static final int ORIGIN_X = -SIZE + 8;
    public static final int ORIGIN_Z = 6;

    /** O lado das câmaras grandes: as de entrada, de saída e do meio. */
    public static final int CHAMBER = 7;

    /** E o das duas salas de baú. */
    public static final int ROOM = 5;

    /** O quanto uma sala de baú se pode desviar, para cada lado. */
    public static final int SHIFT = 5;

    /** De quantas em quantas casas de chão há micélio de verdade: uma em cem. */
    public static final int MYCELIUM = 100;

    /** Onde quem chega cai, em x e em z, e a que altura acima do chão do andar. */
    public static final int DOOR_X = 8;
    public static final int DOOR_Z = 8;
    public static final int DOOR_UP = 2;

    /** E onde o Senhor do Tormento espera, quando é ele que arrasta alguém: o {@code (9, yPos-1, 36)}. */
    public static final int LORD_X = 9;
    public static final int LORD_Z = 36;

    /** O que cada casa da planta é. */
    public static final byte NADA = 0;
    public static final byte FORÇA = 1;
    public static final byte PEDRA = 2;
    public static final byte MICÉLIO = 3;
    public static final byte PORTAL = 4;
    public static final byte BAÚ = 5;
    /** E o ar que uma passagem abre, que não é o mesmo que «nada»: ele <b>apaga</b> o que estava. */
    public static final byte AR = 6;

    /** O que a planta de um andar abrange, em x e em z. A fila a mais em z é a da câmara de saída. */
    public static final int SPAN_X = 2 * SIZE + 1;
    public static final int SPAN_Z = 2 * SIZE + CHAMBER + 1;

    /** E em y, de {@code origY - 1} a {@code origY + 5}. */
    public static final int SPAN_Y = WALL + 1;
    public static final int FLOOR_DOWN = 1;

    /** A planta pronta dos seis andares, uma por andar. */
    private final byte[][] plantas = new byte[LEVELS][SPAN_X * SPAN_Z * SPAN_Y];

    /**
     * Desenha os seis andares a partir daquela semente.
     *
     * <p>Cada andar tem o seu sorteio, tirado da semente do mundo e do número do andar, para que a planta
     * seja sempre a mesma no mesmo mundo — que é o que o gerador de hoje precisa e o de 2014 não precisava.
     */
    public TormentMaze(long semente) {
        for (int andar = 0; andar < LEVELS; andar++) {
            RandomSource sorte = RandomSource.create(semente + andar * 0x9E3779B97F4A7C15L);
            desenha(andar, new Grelha(SIZE, SIZE, sorte), sorte);
        }
    }

    /** A que altura o chão do andar fica. */
    public static int floorOf(int andar) {
        return BASE + andar * STEP;
    }

    /** E em que andar aquela altura cai, ou menos um se cair no vão entre dois. */
    public static int levelAt(int y) {
        int andar = (y - BASE + FLOOR_DOWN) / STEP;
        if (andar < 0 || andar >= LEVELS) return -1;
        int dentro = y - floorOf(andar);
        return dentro >= -FLOOR_DOWN && dentro < WALL ? andar : -1;
    }

    /**
     * O que a planta manda pôr naquela casa do mundo, ou {@link #NADA} se aquela casa não for de nenhum
     * andar.
     */
    public byte at(int x, int y, int z) {
        int andar = levelAt(y);
        if (andar < 0) return NADA;
        int px = x - ORIGIN_X;
        int pz = z - ORIGIN_Z;
        if (px < 0 || px >= SPAN_X || pz < 0 || pz >= SPAN_Z) return NADA;
        int py = y - floorOf(andar) + FLOOR_DOWN;
        return this.plantas[andar][índice(px, py, pz)];
    }

    /** E o bloco que cada marca é. */
    public static BlockState blockOf(byte marca) {
        return switch (marca) {
            case FORÇA -> OccultaBlocks.FORCE.defaultBlockState();
            case PEDRA -> OccultaBlocks.TORMENT_STONE.defaultBlockState();
            case MICÉLIO -> Blocks.MYCELIUM.defaultBlockState();
            case PORTAL -> OccultaBlocks.TORMENT_PORTAL.defaultBlockState();
            case BAÚ -> OccultaBlocks.REFILLING_CHEST.defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }

    private static int índice(int px, int py, int pz) {
        return (py * SPAN_Z + pz) * SPAN_X + px;
    }

    private void põe(int andar, int x, int y, int z, byte oquê) {
        int px = x - ORIGIN_X;
        int pz = z - ORIGIN_Z;
        int py = y + FLOOR_DOWN;
        if (px < 0 || px >= SPAN_X || pz < 0 || pz >= SPAN_Z || py < 0 || py >= SPAN_Y) return;
        this.plantas[andar][índice(px, py, pz)] = oquê;
    }

    // ------------------------------------------------------------------ o desenho

    /**
     * O {@code display} do original, linha por linha.
     *
     * <p>As coordenadas aqui são <b>relativas ao chão do andar</b>: o {@code origY} do original é o zero
     * desta conta, e é por isso que a planta vai de menos um a cinco.
     */
    private void desenha(int andar, Grelha grelha, RandomSource sorte) {
        int i;
        for (i = 0; i < SIZE; i++) {
            int j;
            for (j = 0; j < SIZE; j++) {
                if ((grelha.casa(j, i) & Rumo.NORTE.bit) == 0) {
                    parede(andar, 2 * j, 2 * i);
                    parede(andar, 2 * j + 1, 2 * i);
                } else {
                    parede(andar, 2 * j, 2 * i);
                    passagem(andar, 2 * j + 1, 2 * i, sorte);
                }
            }
            parede(andar, 2 * j, 2 * i);

            for (j = 0; j < SIZE; j++) {
                if ((grelha.casa(j, i) & Rumo.OESTE.bit) == 0) {
                    parede(andar, 2 * j, 2 * i + 1);
                    passagem(andar, 2 * j + 1, 2 * i + 1, sorte);
                } else {
                    passagem(andar, 2 * j, 2 * i + 1, sorte);
                    passagem(andar, 2 * j + 1, 2 * i + 1, sorte);
                }
            }
            parede(andar, 2 * j, 2 * i + 1);
        }

        int j;
        for (j = 0; j < SIZE; j++) {
            parede(andar, 2 * j, 2 * i);
            parede(andar, 2 * j + 1, 2 * i);
        }
        parede(andar, 2 * j, 2 * i);

        // a câmara de entrada
        int meio = CHAMBER / 2;
        for (int x = 0; x < CHAMBER; x++) {
            for (int y = 0; y < CHAMBER; y++) {
                passagem(andar, SIZE + x - meio, y + 1, sorte);
            }
        }

        // a de saída, que avança sete filas para fora
        for (int x = 0; x < CHAMBER; x++) {
            for (int y = 0; y < CHAMBER + 2; y++) {
                passagem(andar, SIZE + x - meio, 2 * SIZE + y - CHAMBER, sorte);
            }
        }

        portal(andar, SIZE, 2 * SIZE);

        // e as duas salas de baú, com um desvio cada uma, tirados em fila do mesmo sorteio
        int metade = ROOM / 2;
        int desvio = sorte.nextInt(2 * SHIFT + 1) - SHIFT;
        for (int x = 0; x < ROOM; x++) {
            for (int y = 0; y < ROOM; y++) {
                passagem(andar, x + 1, y + SIZE - metade + desvio, sorte);
            }
        }
        põe(andar, ORIGIN_X + metade + 1, 0, ORIGIN_Z + SIZE + desvio, BAÚ);

        desvio = sorte.nextInt(2 * SHIFT + 1) - SHIFT;
        for (int x = 0; x < ROOM; x++) {
            for (int y = 0; y < ROOM; y++) {
                passagem(andar, 2 * SIZE + x - ROOM, y + SIZE - metade + desvio, sorte);
            }
        }
        põe(andar, ORIGIN_X + 2 * SIZE - metade - 1, 0, ORIGIN_Z + SIZE + desvio, BAÚ);

        // a sala do meio
        for (int x = 0; x < CHAMBER; x++) {
            for (int y = 0; y < CHAMBER; y++) {
                passagem(andar, SIZE + x - meio, SIZE + y - meio, sorte);
            }
        }
        põe(andar, ORIGIN_X + SIZE, 0, ORIGIN_Z + SIZE, BAÚ);
    }

    /** O {@code drawWall}: Força do chão ao teto, seis de altura. */
    private void parede(int andar, int dx, int dz) {
        for (int h = 0; h < WALL; h++) {
            põe(andar, ORIGIN_X + dx, h, ORIGIN_Z + dz, FORÇA);
        }
    }

    /** O {@code drawPassage}: duas de chão, quatro de ar e o teto. */
    private void passagem(int andar, int dx, int dz, RandomSource sorte) {
        int x = ORIGIN_X + dx;
        int z = ORIGIN_Z + dz;
        põe(andar, x, -1, z, PEDRA);
        põe(andar, x, 0, z, sorte.nextInt(MYCELIUM) == 0 ? MICÉLIO : PEDRA);
        for (int h = 1; h < WALL - 1; h++) põe(andar, x, h, z, AR);
        põe(andar, x, WALL - 1, z, FORÇA);
    }

    /** O {@code drawPortal}: duas casas de portal e a moldura de três dos dois lados. */
    private void portal(int andar, int dx, int dz) {
        int x = ORIGIN_X + dx;
        int z = ORIGIN_Z + dz;
        põe(andar, x, 1, z, PORTAL);
        põe(andar, x, 2, z, PORTAL);
        põe(andar, x, 3, z, PEDRA);
        for (int lado = -1; lado <= 1; lado += 2) {
            for (int h = 1; h <= 3; h++) põe(andar, x + lado, h, z, PEDRA);
        }
    }

    // ------------------------------------------------------------------ a grelha

    /** Os quatro rumos do original, com o bit de cada um e o que lhe é oposto. */
    private enum Rumo {
        NORTE(1, 0, -1),
        SUL(2, 0, 1),
        LESTE(4, 1, 0),
        OESTE(8, -1, 0);

        final int bit;
        final int dx;
        final int dz;

        Rumo(int bit, int dx, int dz) {
            this.bit = bit;
            this.dx = dx;
            this.dz = dz;
        }

        Rumo oposto() {
            return switch (this) {
                case NORTE -> SUL;
                case SUL -> NORTE;
                case LESTE -> OESTE;
                case OESTE -> LESTE;
            };
        }
    }

    /**
     * A grelha do labirinto: a <b>busca em profundidade embaralhada</b> do {@code generateMaze}.
     *
     * <p>O original é recursivo, e numa grelha de trinta e uma por trinta e uma a pilha dele chega a ter
     * novecentas e sessenta e uma chamadas de fundura. Aqui a recursão é uma <b>pilha à mão</b>, que faz a
     * mesma caminhada sem arriscar o fundo da pilha de verdade: em cada casa, embaralham-se os quatro rumos
     * e segue-se o primeiro que der para casa limpa, deixando os outros para quando se voltar.
     */
    private static final class Grelha {
        private final int largura;
        private final int fundura;
        private final int[] casas;

        Grelha(int largura, int fundura, RandomSource sorte) {
            this.largura = largura;
            this.fundura = fundura;
            this.casas = new int[largura * fundura];
            cava(0, 0, sorte);
        }

        int casa(int x, int z) {
            return this.casas[z * this.largura + x];
        }

        private void liga(int x, int z, Rumo rumo) {
            this.casas[z * this.largura + x] |= rumo.bit;
        }

        private boolean dentro(int x, int z) {
            return x >= 0 && x < this.largura && z >= 0 && z < this.fundura;
        }

        /** Uma casa da pilha: onde se está, a fila embaralhada de rumos e por qual se vai. */
        private static final class Passo {
            final int x;
            final int z;
            final List<Rumo> rumos;
            int qual;

            Passo(int x, int z, List<Rumo> rumos) {
                this.x = x;
                this.z = z;
                this.rumos = rumos;
            }
        }

        private void cava(int x0, int z0, RandomSource sorte) {
            List<Passo> pilha = new ArrayList<>();
            pilha.add(new Passo(x0, z0, embaralhados(sorte)));

            while (!pilha.isEmpty()) {
                Passo onde = pilha.get(pilha.size() - 1);
                if (onde.qual >= onde.rumos.size()) {
                    pilha.remove(pilha.size() - 1);
                    continue;
                }
                Rumo rumo = onde.rumos.get(onde.qual++);
                int nx = onde.x + rumo.dx;
                int nz = onde.z + rumo.dz;
                if (!dentro(nx, nz) || casa(nx, nz) != 0) continue;
                liga(onde.x, onde.z, rumo);
                liga(nx, nz, rumo.oposto());
                pilha.add(new Passo(nx, nz, embaralhados(sorte)));
            }
        }

        private static List<Rumo> embaralhados(RandomSource sorte) {
            List<Rumo> rumos = new ArrayList<>(List.of(Rumo.values()));
            embaralha(rumos, sorte);
            return rumos;
        }

        /** O {@code Collections.shuffle} com o sorteio do mundo, que é o que o original faz. */
        private static void embaralha(List<Rumo> rumos, RandomSource sorte) {
            for (int k = rumos.size(); k > 1; k--) {
                Collections.swap(rumos, k - 1, sorte.nextInt(k));
            }
        }
    }
}
