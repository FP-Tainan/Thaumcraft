package net.thaumcraft.occulta.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;
import net.thaumcraft.occulta.OccultaEntities;

import java.util.List;

/**
 * A muralha da aldeia: o {@code WorldHandlerVillageDistrict$Wall.placeWalls} do Witchery.
 *
 * <p><b>Ela não é um prédio, é um contorno</b> — e é por isso que não pode ser um molde como as outras peças.
 * Uma muralha tem de saber onde a aldeia acaba, e isso só se sabe depois de a aldeia estar desenhada.
 *
 * <p><b>Como o original a acha.</b> Pega só as <b>ruas</b>, engorda cada uma — sete além de cada ponta e vinte
 * para cada lado —, une tudo num mapa de duas dimensões, fecha os vãos de até sete que tenham sobrado entre
 * elas, e depois <b>apaga o miolo</b>: toda célula cujas oito vizinhas estejam ocupadas deixa de contar. O que
 * sobra é a borda, e é nela que a muralha se levanta.
 *
 * <p><b>E os portões saem de graça.</b> As três células do meio da <b>ponta</b> de cada rua engordada ficam
 * marcadas à parte; é por ali que a rua sai da aldeia, e é ali que a muralha se abre.
 *
 * <p>A altura de cada pedaço é sondada no terreno e <b>suavizada contra a vizinha já feita</b>, um degrau de
 * cada vez — é o que faz a muralha acompanhar o relevo em vez de flutuar ou se enterrar.
 */
public final class VillageWall {
    /** O quanto cada rua engorda: sete além das pontas, vinte para os lados. */
    public static final int GORDURA_COMPRIMENTO = 7;
    public static final int GORDURA_LADO = 20;

    /** O vão máximo que se fecha entre duas ruas. */
    private static final int ALCANCE = 7;

    /** A altura da muralha acima do chão achado. */
    private static final int ALTURA = 9;

    /** De quantas em quantas células de muralha nasce um guarda. */
    private static final int GUARDA_A_CADA = 200;

    /** O que cada célula do mapa é. */
    private static final byte VAZIO = 0, MIOLO = 1, BORDA = 2, PORTAO = 3;

    private VillageWall() {
    }

    /** Uma rua engordada: a caixa e se ela corre no sentido leste-oeste. */
    public record Faixa(int x1, int z1, int x2, int z2, boolean lesteOeste) {
        /** O {@code StructureBounds} do original: engorda conforme o sentido da rua. */
        public static Faixa de(BoundingBox caixa) {
            boolean lo = caixa.maxX() - caixa.minX() > caixa.maxZ() - caixa.minZ();
            int gx = lo ? GORDURA_COMPRIMENTO : GORDURA_LADO;
            int gz = lo ? GORDURA_LADO : GORDURA_COMPRIMENTO;
            return new Faixa(caixa.minX() - gx, caixa.minZ() - gz,
                             caixa.maxX() + gx, caixa.maxZ() + gz, lo);
        }
    }

    /**
     * Desenha a muralha em volta das ruas dadas.
     *
     * @param doAlto a altura de onde se começa a sondar o chão: a do bloco gerador
     */
    public static void desenha(ServerLevel level, List<Faixa> ruas, int doAlto,
                               Block base, Block cerca, Block escada) {
        Planta planta = planta(ruas);
        if (planta == null) return;
        short[][] alturas = new short[planta.mapa().length][planta.mapa()[0].length];
        levanta(level, planta.mapa(), alturas, planta.minX(), planta.minZ(), doAlto, base, cerca, escada);
    }

    /** O mapa da aldeia e o canto de onde ele conta. */
    public record Planta(byte[][] mapa, int minX, int minZ) {
    }

    /**
     * O mapa: ruas engordadas, vãos fechados, miolo apagado. É a parte que decide a forma da muralha, e está
     * à parte do desenhar para se poder provar sem escrever um bloco no mundo.
     */
    public static @Nullable Planta planta(List<Faixa> ruas) {
        if (ruas.isEmpty()) return null;

        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Faixa r : ruas) {
            minX = Math.min(r.x1(), minX);
            minZ = Math.min(r.z1(), minZ);
            maxX = Math.max(r.x2(), maxX);
            maxZ = Math.max(r.z2(), maxZ);
        }

        byte[][] mapa = new byte[maxX - minX + 3][maxZ - minZ + 3];

        // cada rua engordada entra no mapa, com as três do meio da ponta marcadas como portão
        for (Faixa r : ruas) {
            int meioX = (r.x2() - r.x1() + 1) / 2 + r.x1() - 1;
            int meioZ = (r.z2() - r.z1() + 1) / 2 + r.z1() - 1;
            for (int x = r.x1(); x <= r.x2(); x++) {
                for (int z = r.z1(); z <= r.z2(); z++) {
                    int mx = x - minX + 1, mz = z - minZ + 1;
                    boolean pontaZ = !r.lesteOeste() && (z == r.z1() || z == r.z2())
                            && x >= meioX - 1 && x <= meioX + 1;
                    boolean pontaX = r.lesteOeste() && (x == r.x1() || x == r.x2())
                            && z >= meioZ - 1 && z <= meioZ + 1;
                    mapa[mx][mz] = (pontaZ || pontaX) ? PORTAO : BORDA;
                }
            }
        }

        fechaVaos(mapa);
        apagaMiolo(mapa);
        return new Planta(mapa, minX, minZ);
    }

    /** Para as provas: o que uma célula do mapa é. */
    public static final byte CELULA_VAZIA = VAZIO, CELULA_MIOLO = MIOLO,
            CELULA_BORDA = BORDA, CELULA_PORTAO = PORTAO;

    /** Fecha os vãos de até sete entre duas ruas, para a aldeia virar uma mancha só. */
    private static void fechaVaos(byte[][] mapa) {
        for (int x = 1; x < mapa.length - ALCANCE; x++) {
            for (int z = 1; z < mapa[x].length - ALCANCE; z++) {
                if (mapa[x][z] != BORDA) continue;
                for (int p = 1; p < ALCANCE; p++) {
                    if (mapa[x + p][z] == BORDA && mapa[x + p - 1][z] == VAZIO) {
                        for (int q = p; q > 0; q--) mapa[x + q][z] = BORDA;
                    }
                    if (mapa[x][z + p] == BORDA && mapa[x][z + p - 1] == VAZIO) {
                        for (int q = p; q > 0; q--) mapa[x][z + q] = BORDA;
                    }
                }
            }
        }
    }

    /** E apaga o miolo: quem tem as oito vizinhas ocupadas deixa de ser borda. */
    private static void apagaMiolo(byte[][] mapa) {
        for (int x = 1; x < mapa.length - 1; x++) {
            for (int z = 1; z < mapa[x].length - 1; z++) {
                boolean cercado = true;
                for (int dx = -1; dx <= 1 && cercado; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        if (mapa[x + dx][z + dz] == VAZIO) { cercado = false; break; }
                    }
                }
                if (cercado) mapa[x][z] = MIOLO;
            }
        }
    }

    private static void levanta(ServerLevel level, byte[][] mapa, short[][] alturas,
                                int minX, int minZ, int doAlto,
                                Block base, Block cerca, Block escada) {
        int desdeOGuarda = 0;

        for (int x = 1; x < mapa.length - 1; x++) {
            for (int z = 1; z < mapa[x].length - 1; z++) {
                if (mapa[x][z] < BORDA) continue;

                boolean n = mapa[x][z - 1] >= BORDA, s = mapa[x][z + 1] >= BORDA;
                boolean l = mapa[x + 1][z] >= BORDA, o = mapa[x - 1][z] >= BORDA;
                boolean nl = mapa[x + 1][z - 1] >= BORDA, so = mapa[x - 1][z + 1] >= BORDA;
                boolean sl = mapa[x + 1][z + 1] >= BORDA, no = mapa[x - 1][z - 1] >= BORDA;

                int dx = minX + x, dz = minZ + z;
                int chão = sondaOChao(level, dx, dz, doAlto);
                int deCima = chão + ALTURA;

                // suaviza contra a vizinha já feita: um degrau de cada vez
                int vizinha = Math.max(Math.max(alturas[x - 1][z], alturas[x + 1][z]),
                        Math.max(alturas[x][z + 1], alturas[x][z - 1]));
                if (vizinha > 0) {
                    if (vizinha > deCima) deCima = vizinha - 1;
                    else if (vizinha < deCima) deCima = vizinha + 1;
                }
                if (deCima - chão > 0) alturas[x][z] = (short) Math.clamp(deCima, 0, Short.MAX_VALUE);

                for (int y = deCima; y > chão; y--) {
                    if (y == deCima) {
                        coroa(level, dx, y, dz, base, escada, n, s, l, o, nl, so, sl, no);
                        if (++desdeOGuarda > GUARDA_A_CADA) {
                            poeGuarda(level, dx, y, dz);
                            desdeOGuarda = 0;
                        }
                    } else {
                        corpo(level, mapa, x, z, dx, y, dz, deCima, base, cerca, escada);
                    }
                }
            }
        }
    }

    /**
     * Onde é o chão: desce contando blocos sólidos à volta até achar nove.
     *
     * <p>É o que o original faz, e é o que impede a muralha de nascer em cima de uma copa de árvore.
     */
    private static int sondaOChao(ServerLevel level, int dx, int dz, int doAlto) {
        int y = doAlto;
        int sólidos = 0;
        for (; y > level.getMinY() + 1 && sólidos < 9; y--) {
            sólidos = 0;
            for (int ddx = dx - 1; ddx <= dx + 1; ddx++) {
                for (int ddz = dz - 1; ddz <= dz + 1; ddz++) {
                    BlockState feitio = level.getBlockState(new BlockPos(ddx, y, ddz));
                    if (!podeSerTrocado(feitio) && feitio.isSolidRender()) sólidos++;
                }
            }
        }
        return y;
    }

    /** A fiada de cima: os cantos e as ameias de escada viradas para fora. */
    private static void coroa(ServerLevel level, int dx, int y, int dz, Block base, Block escada,
                              boolean n, boolean s, boolean l, boolean o,
                              boolean nl, boolean so, boolean sl, boolean no) {
        if (!nl && !n && !l) canto(level, base, dx, y, dz, 2, -2);
        if (!no && !n && !o) canto(level, base, dx, y, dz, -2, -2);
        if (!sl && !s && !l) canto(level, base, dx, y, dz, 2, 2);
        if (!so && !s && !o) canto(level, base, dx, y, dz, -2, 2);

        if (!n && !nl && !no) {
            poe(level, dx, y, dz - 2, base.defaultBlockState());
            poe(level, dx, y + 1, dz - 2, escadaVirada(escada, Direction.NORTH));
        }
        if (!l && !sl && !nl) {
            poe(level, dx + 2, y, dz, base.defaultBlockState());
            poe(level, dx + 2, y + 1, dz, escadaVirada(escada, Direction.SOUTH));
        }
        if (!s && !sl && !so) {
            poe(level, dx, y, dz + 2, base.defaultBlockState());
            poe(level, dx, y + 1, dz + 2, escadaVirada(escada, Direction.NORTH));
        }
        if (!o && !no && !so) {
            poe(level, dx - 2, y, dz, base.defaultBlockState());
            poe(level, dx - 2, y + 1, dz, escadaVirada(escada, Direction.SOUTH));
        }
    }

    private static void canto(ServerLevel level, Block base, int dx, int y, int dz, int sx, int sz) {
        BlockState feitio = base.defaultBlockState();
        poe(level, dx + sx, y, dz + sz, feitio);
        poe(level, dx + sx / 2, y, dz + sz, feitio);
        poe(level, dx + sx, y, dz + sz / 2, feitio);
        poe(level, dx + sx, y + 1, dz + sz, feitio);
        poe(level, dx + sx / 2, y + 1, dz + sz, feitio);
        poe(level, dx + sx, y + 1, dz + sz / 2, feitio);
    }

    /** E o corpo da muralha, com o portão aberto onde a rua passa. */
    private static void corpo(ServerLevel level, byte[][] mapa, int x, int z,
                              int dx, int y, int dz, int deCima,
                              Block base, Block cerca, Block escada) {
        final int LONGE = 4;
        boolean portão = mapa[x][z] == PORTAO
                && (x > LONGE && x < mapa.length - LONGE
                        && mapa[x - LONGE][z] == BORDA && mapa[x + LONGE][z] == BORDA
                    || z > LONGE && z < mapa[x].length - LONGE
                        && mapa[x][z - LONGE] == BORDA && mapa[x][z + LONGE] == BORDA);

        if (portão && y == deCima - 3) {
            level.setBlock(new BlockPos(dx, y, dz), cerca.defaultBlockState(), 2);
        }

        if (portão && y <= deCima - 3) return;

        BlockState feitio = base.defaultBlockState();
        boolean pn = mapa[x][z - 1] == PORTAO, ps = mapa[x][z + 1] == PORTAO;
        boolean pl = mapa[x + 1][z] == PORTAO, po = mapa[x - 1][z] == PORTAO;

        poe(level, dx, y, dz, feitio);
        if (!pn) poe(level, dx, y, dz - 1, feitio);
        if (!pn && !pl) poe(level, dx + 1, y, dz - 1, feitio);
        if (!pn && !po) poe(level, dx - 1, y, dz - 1, feitio);
        if (!pl) poe(level, dx + 1, y, dz, feitio);
        if (!ps) poe(level, dx, y, dz + 1, feitio);
        if (!ps && !pl) poe(level, dx + 1, y, dz + 1, feitio);
        if (!ps && !po) poe(level, dx - 1, y, dz + 1, feitio);
        if (!po) poe(level, dx - 1, y, dz, feitio);
    }

    private static BlockState escadaVirada(Block escada, Direction para) {
        BlockState feitio = escada.defaultBlockState();
        if (feitio.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            feitio = feitio.setValue(BlockStateProperties.HORIZONTAL_FACING, para);
        }
        if (feitio.hasProperty(StairBlock.HALF)) {
            feitio = feitio.setValue(StairBlock.HALF, Half.BOTTOM);
        }
        return feitio;
    }

    /**
     * Põe um bloco, mas só onde há o que trocar.
     *
     * <p>O original troca ar, folha, planta e <b>madeira</b>. Hoje não há "material", e madeira ali queria dizer
     * árvore: ficam folha e tronco. <b>Tábua não entra</b> — uma muralha que come a parede de uma casa é um
     * defeito, não uma fidelidade.
     */
    private static void poe(ServerLevel level, int x, int y, int z, BlockState feitio) {
        BlockPos onde = new BlockPos(x, y, z);
        if (!podeSerTrocado(level.getBlockState(onde))) return;
        level.setBlock(onde, feitio, 2);
    }

    private static boolean podeSerTrocado(BlockState feitio) {
        return feitio.isAir() || feitio.canBeReplaced()
                || feitio.is(BlockTags.LEAVES) || feitio.is(BlockTags.LOGS);
    }

    /**
     * Põe um guarda na muralha — se ainda não houver um ali.
     *
     * <p>A conferência existe porque o salto-de-encaixe <b>não sabe limitar quantidade</b>: pode sair mais de um
     * marcador de muralha na mesma aldeia. Desenhar duas vezes é quase de graça — o {@code poe} só troca o que é
     * trocável, e tijolo não é —, mas a guarnição dobraria. Com isto, não dobra.
     */
    private static void poeGuarda(ServerLevel level, int x, int y, int z) {
        var roda = new net.minecraft.world.phys.AABB(x - 8, y - 8, z - 8, x + 8, y + 8, z + 8);
        if (!level.getEntitiesOfClass(VillageGuardEntity.class, roda).isEmpty()) return;

        var guarda = OccultaEntities.VILLAGE_GUARD.create(level, EntitySpawnReason.STRUCTURE);
        if (guarda == null) return;
        guarda.snapTo(x + 0.5, y, z + 0.5, 0.0f, 0.0f);
        guarda.setPersistenceRequired();
        guarda.finalizeSpawn(level, level.getCurrentDifficultyAt(guarda.blockPosition()),
                EntitySpawnReason.STRUCTURE, null);
        level.addFreshEntity(guarda);
    }

    /** O que a muralha é feita, por variante de aldeia: tijolo de pedra, e arenito no deserto. */
    public static Block base(boolean deserto) {
        return deserto ? Blocks.SANDSTONE : Blocks.STONE_BRICKS;
    }

    public static Block escada(boolean deserto) {
        return deserto ? Blocks.SANDSTONE_STAIRS : Blocks.STONE_BRICK_STAIRS;
    }

    public static Block cerca(boolean deserto) {
        return Blocks.OAK_FENCE;
    }
}
