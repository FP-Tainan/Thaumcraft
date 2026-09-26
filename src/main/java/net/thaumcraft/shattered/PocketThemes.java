package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O que há dentro de um bolso aberto por uma fenda do mundo.
 *
 * <p><b>Isto é do porte, e não do original</b> — ou, para ser justo, é o original por outro caminho. Lá as salas
 * vêm de esquemas {@code .schem} guardados no jar: ruínas, prisões, bibliotecas. O porte ainda não lê esquemas, e
 * quem manda pediu o que os esquemas davam: <i>uma é uma biblioteca, na próxima porta um pedaço de deserto, a
 * outra o Nether, a outra um pedaço de um reino antigo</i>. Então as salas são feitas em código, uma por tema, e
 * quando o leitor de esquemas chegar troca-se o que as enche e não o resto.
 *
 * <p>O bolso liso de tecido preto continua a ser o que uma porta comum abre. Estes são os bolsos <b>bravos</b>: os
 * que se ganham estabilizando uma fenda que já estava no mundo, e que se ligam uns aos outros pelas portas que
 * cada sala tem nas outras paredes.
 */
public enum PocketThemes {
    /** A biblioteca: estantes até ao teto, tapetes e lanternas. */
    LIBRARY(Blocks.STONE_BRICKS, Blocks.OAK_PLANKS, Blocks.DARK_OAK_PLANKS) {
        @Override
        public void fill(ServerLevel level, BlockPos canto, RandomSource sorte) {
            // duas filas de estantes, encostadas às paredes de leste e oeste
            for (int z = 2; z < Pockets.ROOM - 2; z++) {
                for (int y = 0; y < 4; y++) {
                    if (sorte.nextInt(9) == 0) continue;
                    põe(level, canto.offset(1, y, z), Blocks.BOOKSHELF);
                    põe(level, canto.offset(Pockets.ROOM - 2, y, z), Blocks.BOOKSHELF);
                }
            }
            // uma ilha de estantes no meio, e mesas de leitura em volta
            for (int x = 6; x <= 8; x++) {
                for (int y = 0; y < 3; y++) põe(level, canto.offset(x, y, Pockets.ROOM / 2), Blocks.BOOKSHELF);
            }
            põe(level, canto.offset(5, 0, Pockets.ROOM / 2), Blocks.LECTERN);
            põe(level, canto.offset(9, 0, Pockets.ROOM / 2), Blocks.LECTERN);
            // tapete pelo chão, e lanternas penduradas
            for (int x = 3; x < Pockets.ROOM - 3; x++) {
                for (int z = 3; z < Pockets.ROOM - 3; z++) {
                    if (level.getBlockState(canto.offset(x, 0, z)).isAir() && sorte.nextInt(3) != 0) {
                        põe(level, canto.offset(x, 0, z), Blocks.CARPET.pick(DyeColor.RED));
                    }
                }
            }
            lanternas(level, canto, sorte, Blocks.LANTERN, 12);
        }
    },

    /**
     * Um pedaço de deserto: arenito, areia, cactos e mato seco.
     *
     * <p><b>O chão é de arenito, e não de areia</b>, de propósito: um bolso não tem nada por baixo dele, e a
     * areia cai. Um chão de areia esvaziava-se para o vazio à primeira sacudidela. As dunas por cima já são de
     * areia — essas têm o arenito a segurá-las.
     */
    DESERT(Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE) {
        @Override
        public void fill(ServerLevel level, BlockPos canto, RandomSource sorte) {
            // dunas: umas quantas manchas de areia mais alta
            for (int volta = 0; volta < 7; volta++) {
                int cx = 2 + sorte.nextInt(Pockets.ROOM - 4), cz = 2 + sorte.nextInt(Pockets.ROOM - 4);
                int alto = 1 + sorte.nextInt(2);
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        int longe = Math.abs(x) + Math.abs(z);
                        for (int y = 0; y < alto - longe / 2; y++) {
                            põe(level, canto.offset(cx + x, y, cz + z), Blocks.SAND);
                        }
                    }
                }
            }
            // e o que cresce nela
            for (int volta = 0; volta < 14; volta++) {
                int x = 2 + sorte.nextInt(Pockets.ROOM - 4), z = 2 + sorte.nextInt(Pockets.ROOM - 4);
                int y = chão(level, canto, x, z);
                if (y < 0) continue;
                if (sorte.nextInt(3) == 0) {
                    int altura = 1 + sorte.nextInt(3);
                    for (int i = 0; i < altura; i++) põe(level, canto.offset(x, y + i, z), Blocks.CACTUS);
                } else {
                    põe(level, canto.offset(x, y, z), Blocks.DEAD_BUSH);
                }
            }
            // uns tijolos meio enterrados, que é o que sobra de quem lá esteve
            for (int volta = 0; volta < 5; volta++) {
                int x = 2 + sorte.nextInt(Pockets.ROOM - 4), z = 2 + sorte.nextInt(Pockets.ROOM - 4);
                põe(level, canto.offset(x, 0, z), Blocks.CHISELED_SANDSTONE);
            }
            // e a luz vem do teto, em manchas largas: é o mais perto de um sol que um bolso tem
            teto(level, canto, sorte, Blocks.GLOWSTONE, 10);
        }
    },

    /** Um pedaço do Nether: pedra-do-inferno, tijolos, areia-das-almas e fogo. */
    NETHER(Blocks.NETHER_BRICKS, Blocks.NETHERRACK, Blocks.NETHER_BRICKS) {
        @Override
        public void fill(ServerLevel level, BlockPos canto, RandomSource sorte) {
            for (int volta = 0; volta < 9; volta++) {
                int cx = 2 + sorte.nextInt(Pockets.ROOM - 4), cz = 2 + sorte.nextInt(Pockets.ROOM - 4);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        põe(level, canto.offset(cx + x, 0, cz + z),
                                sorte.nextBoolean() ? Blocks.SOUL_SAND : Blocks.NETHERRACK);
                    }
                }
            }
            // colunas de tijolo do chão ao teto, e um fogo-das-almas ou outro
            for (int volta = 0; volta < 4; volta++) {
                int x = 3 + sorte.nextInt(Pockets.ROOM - 6), z = 3 + sorte.nextInt(Pockets.ROOM - 6);
                for (int y = 0; y < Pockets.HEIGHT; y++) põe(level, canto.offset(x, y, z), Blocks.NETHER_BRICKS);
            }
            for (int volta = 0; volta < 5; volta++) {
                int x = 2 + sorte.nextInt(Pockets.ROOM - 4), z = 2 + sorte.nextInt(Pockets.ROOM - 4);
                if (level.getBlockState(canto.offset(x, 0, z)).is(Blocks.SOUL_SAND)) {
                    põe(level, canto.offset(x, 1, z), Blocks.SOUL_FIRE);
                }
            }
            teto(level, canto, sorte, Blocks.GLOWSTONE, 12);
        }
    },

    /** Um pedaço de um reino antigo: pedra lascada, colunas, correntes e teias. */
    ANCIENT_KINGDOM(Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.CHISELED_DEEPSLATE) {
        @Override
        public void fill(ServerLevel level, BlockPos canto, RandomSource sorte) {
            // quatro colunas, com a base e o capitel lavrados
            int[][] pés = {{3, 3}, {3, Pockets.ROOM - 4}, {Pockets.ROOM - 4, 3}, {Pockets.ROOM - 4, Pockets.ROOM - 4}};
            for (int[] pé : pés) {
                for (int y = 0; y < Pockets.HEIGHT; y++) {
                    boolean ponta = y == 0 || y == Pockets.HEIGHT - 1;
                    põe(level, canto.offset(pé[0], y, pé[1]),
                            ponta ? Blocks.CHISELED_DEEPSLATE : Blocks.DEEPSLATE_BRICKS);
                }
                // uma corrente pendurada ao lado de cada coluna
                for (int y = Pockets.HEIGHT - 2; y > Pockets.HEIGHT - 5; y--) {
                    põe(level, canto.offset(pé[0] + 1, y, pé[1]), Blocks.IRON_CHAIN);
                }
            }
            // o chão gasto, e o que o tempo deixou por cima
            for (int x = 1; x < Pockets.ROOM - 1; x++) {
                for (int z = 1; z < Pockets.ROOM - 1; z++) {
                    if (sorte.nextInt(6) == 0) põe(level, canto.offset(x, -1, z), Blocks.CRACKED_DEEPSLATE_BRICKS);
                    if (sorte.nextInt(24) == 0 && level.getBlockState(canto.offset(x, 0, z)).isAir()) {
                        põe(level, canto.offset(x, 0, z), Blocks.COBWEB);
                    }
                }
            }
            lanternas(level, canto, sorte, Blocks.SOUL_LANTERN, 22);
            // e uns braseiros no chão, junto aos pés das colunas
            for (int[] pé : pés) {
                põe(level, canto.offset(pé[0] - 1, 0, pé[1]), Blocks.SOUL_LANTERN);
            }
        }
    };

    private final net.minecraft.world.level.block.Block parede;
    private final net.minecraft.world.level.block.Block chão;
    private final net.minecraft.world.level.block.Block teto;

    PocketThemes(net.minecraft.world.level.block.Block parede,
                 net.minecraft.world.level.block.Block chão,
                 net.minecraft.world.level.block.Block teto) {
        this.parede = parede;
        this.chão = chão;
        this.teto = teto;
    }

    public BlockState wall() {
        return this.parede.defaultBlockState();
    }

    public BlockState floor() {
        return this.chão.defaultBlockState();
    }

    public BlockState ceiling() {
        return this.teto.defaultBlockState();
    }

    /** O que enche a sala depois de ela estar cavada. */
    public abstract void fill(ServerLevel level, BlockPos canto, RandomSource sorte);

    /** Um tema qualquer, que não seja aquele de onde se veio — para duas salas seguidas não serem a mesma. */
    public static PocketThemes roll(RandomSource sorte, PocketThemes anterior) {
        PocketThemes[] todos = values();
        if (anterior == null || todos.length < 2) return todos[sorte.nextInt(todos.length)];
        PocketThemes escolhido;
        do {
            escolhido = todos[sorte.nextInt(todos.length)];
        } while (escolhido == anterior);
        return escolhido;
    }

    /** O tema de nome tal, ou nada se o nome não for de nenhum. */
    public static PocketThemes byName(String nome) {
        for (PocketThemes tema : values()) {
            if (tema.name().equals(nome)) return tema;
        }
        return null;
    }

    private static void põe(ServerLevel level, BlockPos onde, net.minecraft.world.level.block.Block bloco) {
        level.setBlock(onde, bloco.defaultBlockState(), 2);
    }

    /** A primeira casa vazia acima do chão daquela coluna, ou -1 se não houver nenhuma. */
    private static int chão(ServerLevel level, BlockPos canto, int x, int z) {
        for (int y = 0; y < Pockets.HEIGHT; y++) {
            if (level.getBlockState(canto.offset(x, y, z)).isAir()) return y;
        }
        return -1;
    }

    /** Umas quantas manchas de luz no próprio teto, para a sala não ficar às escuras. */
    private static void teto(ServerLevel level, BlockPos canto, RandomSource sorte,
                             net.minecraft.world.level.block.Block luz, int quantas) {
        for (int volta = 0; volta < quantas; volta++) {
            int cx = 1 + sorte.nextInt(Pockets.ROOM - 2), cz = 1 + sorte.nextInt(Pockets.ROOM - 2);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) > 1) continue;
                    int px = cx + x, pz = cz + z;
                    if (px < 0 || px >= Pockets.ROOM || pz < 0 || pz >= Pockets.ROOM) continue;
                    põe(level, canto.offset(px, Pockets.HEIGHT, pz), luz);
                }
            }
        }
    }

    /** Umas quantas luzes penduradas no teto, para a sala não ficar às escuras. */
    private static void lanternas(ServerLevel level, BlockPos canto, RandomSource sorte,
                                  net.minecraft.world.level.block.Block luz, int quantas) {
        for (int volta = 0; volta < quantas; volta++) {
            int x = 2 + sorte.nextInt(Pockets.ROOM - 4), z = 2 + sorte.nextInt(Pockets.ROOM - 4);
            BlockPos onde = canto.offset(x, Pockets.HEIGHT - 1, z);
            if (!level.getBlockState(onde).isAir()) continue;
            var estado = luz.defaultBlockState();
            if (estado.hasProperty(net.minecraft.world.level.block.LanternBlock.HANGING)) {
                estado = estado.setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true);
            }
            level.setBlock(onde, estado, 2);
        }
    }

    /** O tecido preto do bolso comum, que é o que uma porta sem fenda abre. */
    public static BlockState plainWall() {
        return FabricBlocks.ANCIENT.get(DyeColor.BLACK).defaultBlockState();
    }

    public static BlockState plainFloor() {
        return FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState();
    }
}
