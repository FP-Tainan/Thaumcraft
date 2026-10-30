package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * Onde as <b>plantas do ofício</b> nascem: o lugar que o cozimento acha e o jeito que ele tem de as pôr lá.
 *
 * <p>São o {@code BlockUtil.isReplaceableBlock}, o {@code getClosestPlantableBlock} e o
 * {@code setBlockIfReplaceable} do Witchery, mais os três plantios que os três cozimentos de planta fazem
 * com eles: a <b>teia</b>, o <b>cipó</b> e o <b>cato</b>.
 *
 * <h2>O que é «se deixa empurrar»</h2>
 *
 * <p>O original não pergunta se um bloco é <b>sólido</b> — pergunta se ele se <b>deixa empurrar</b>. Ar,
 * água, lava, fogo, neve rasa, cipó e mato se deixam; pedra, madeira e vidro não. É uma pergunta diferente da
 * do choque, e é por isso que estas plantas crescem dentro da água e engolem o capim sem pedir licença.
 */
public final class WitchPlants {
    private WitchPlants() {
    }

    /** O {@code isReplaceableBlock}. */
    public static boolean substituível(LevelReader level, BlockPos onde) {
        return level.getBlockState(onde).canBeReplaced();
    }

    /** O {@code setBlockIfReplaceable}: põe, se houver lugar, e diz se pôs. */
    public static boolean põeSePuder(ServerLevel level, BlockPos onde, BlockState oquê) {
        if (!substituível(level, onde)) return false;
        level.setBlock(onde, oquê, Block.UPDATE_ALL);
        return true;
    }

    /**
     * O <b>{@code getClosestPlantableBlock}</b>: a casa em que a planta cabe, a partir de onde o frasco bateu.
     *
     * <p>São três caminhos, e o original escolhe entre eles nesta ordem. Se o lugar do estouro se deixa
     * empurrar, ele <b>cai por ele abaixo</b> até achar chão, e planta em cima desse chão. Se não, e se o
     * frasco veio <b>de cima</b> ou de ninguém — o que é o caso de quem leva o frasco no corpo —, planta
     * onde está. E se não, e se não veio de baixo, ele <b>dá um passo para fora da parede</b> e tenta plantar
     * no pé dela.
     *
     * <p><b>Guarda do porte:</b> a queda para o lugar tem fundo. No original ela não tem — ela conta com a
     * pedra do fundo do mundo para parar, e num mundo sem fundo ela nunca pararia.
     *
     * @param exigeBloco o {@code allowAir} do original, que apesar do nome quer dizer o contrário: ligado, o
     *                   estouro <b>no ar puro</b> não cai, ele vai pelo segundo caminho
     */
    @Nullable
    public static BlockPos chãoMaisPerto(ServerLevel level, BlockPos onde, @Nullable Direction lado,
                                         boolean exigeBloco) {
        int x = onde.getX();
        int y = onde.getY();
        int z = onde.getZ();
        boolean achou = false;

        BlockPos aqui = new BlockPos(x, y, z);
        if (substituível(level, aqui) && (!exigeBloco || !level.getBlockState(aqui).isAir())) {
            while (y > level.getMinY() && substituível(level, new BlockPos(x, y - 1, z))) y--;
            y--;
            achou = true;
        } else if (lado == null || lado == Direction.UP) {
            achou = true;
        } else if (lado != Direction.DOWN) {
            x += lado.getStepX();
            z += lado.getStepZ();
            if (substituível(level, new BlockPos(x, y, z))) {
                y--;
                achou = !substituível(level, new BlockPos(x, y, z));
            }
        }

        return achou ? new BlockPos(x, y + 1, z) : null;
    }

    // ------------------------------------------------------------------ a teia

    /**
     * A <b>teia</b>: uma casa, uma cruz, ou uma bola.
     *
     * <p>Sem força ela é <b>uma casa só</b>. No primeiro grau ela enche as seis vizinhas de face — a cruz de
     * sete. No segundo, enche também as doze de aresta, e o que fica é uma <b>bola de dezenove casas</b>:
     * tudo o que toca o meio por face ou por aresta, e nada pelas quinas.
     */
    public static void teia(ServerLevel level, BlockPos onde, @Nullable Direction lado, int força) {
        BlockPos lugar = chãoMaisPerto(level, onde, lado, true);
        if (lugar == null) return;
        BlockState teia = OccultaBlocks.WITCH_WEB.defaultBlockState();

        põeSePuder(level, lugar, teia);
        if (força > 0) {
            for (Direction rumo : Direction.values()) põeSePuder(level, lugar.relative(rumo), teia);
        }
        if (força > 1) {
            for (Direction rumo : Direction.Plane.HORIZONTAL) {
                põeSePuder(level, lugar.relative(rumo).relative(rumo.getClockWise()), teia);
                põeSePuder(level, lugar.above().relative(rumo), teia);
                põeSePuder(level, lugar.below().relative(rumo), teia);
            }
        }
    }

    // ------------------------------------------------------------------ o cipó

    /**
     * O <b>cipó</b>: ele corre a parede de cima a baixo, pela face em que o frasco bateu.
     *
     * <p>Ele só pega em <b>parede</b>: acertando o chão ou o teto não acontece nada. E tem duas metades que
     * não são iguais.
     *
     * <p><b>Para baixo</b>, sem força, ele só desce enquanto houver parede ao lado — acaba a parede, acaba o
     * cipó. <b>Com força</b> ele continua descendo no vazio, pendurado, até esbarrar em alguma coisa. É a
     * diferença toda que a força faz, e ela se vê de longe: um cipó com força <b>cai da beira do penhasco</b>.
     *
     * <p><b>Para cima</b> ele sempre precisa de parede, com força ou sem — mas ele <b>atravessa folhas</b>,
     * que é como ele sobe por dentro da copa de uma árvore.
     */
    public static void cipó(ServerLevel level, BlockPos onde, @Nullable Direction lado, int força) {
        if (lado == null || lado.getAxis().isVertical()) return;
        BlockState cipó = OccultaBlocks.WITCH_VINE.defaultBlockState()
                .setValue(WitchVineBlock.FACING, lado.getOpposite());
        BlockPos coluna = onde.relative(lado);

        for (int dy = 0; ; dy--) {
            BlockPos lugar = coluna.offset(0, dy, 0);
            if (lugar.getY() <= level.getMinY() || !substituível(level, lugar)) break;
            if (força <= 0 && !level.getBlockState(onde.offset(0, dy, 0)).isSolidRender()) break;
            level.setBlock(lugar, cipó, Block.UPDATE_ALL);
        }

        for (int dy = 1; ; dy++) {
            BlockPos lugar = coluna.offset(0, dy, 0);
            if (lugar.getY() >= level.getMaxY()) break;
            BlockState tem = level.getBlockState(lugar);
            if (!tem.canBeReplaced() && !tem.is(BlockTags.LEAVES)) break;
            if (!level.getBlockState(onde.offset(0, dy, 0)).isSolidRender()) break;
            level.setBlock(lugar, cipó, Block.UPDATE_ALL);
        }
    }

    // ------------------------------------------------------------------ o cato

    /**
     * O <b>cato</b>: três casas de altura, mais uma por grau.
     *
     * <p>Acertando um cato que já está lá, ele <b>sobe até o topo da coluna</b> e cresce dali. É por isso que
     * acertar duas vezes no mesmo lugar faz uma coluna mais alta, e não duas colunas ao lado uma da outra.
     *
     * @return se pegou em alguma coisa
     */
    public static boolean cato(ServerLevel level, BlockPos onde, @Nullable Direction lado, int força) {
        BlockPos lugar;
        if (level.getBlockState(onde).is(OccultaBlocks.WITCH_CACTUS)) {
            BlockPos topo = onde.above();
            while (level.getBlockState(topo).is(OccultaBlocks.WITCH_CACTUS)) topo = topo.above();
            lugar = substituível(level, topo) ? topo : null;
        } else {
            lugar = chãoMaisPerto(level, onde, lado, false);
        }
        if (lugar == null) return false;

        BlockState cato = OccultaBlocks.WITCH_CACTUS.defaultBlockState();
        int alto = 3 + força;
        for (int i = 0; i < alto; i++) {
            BlockPos aqui = lugar.above(i);
            if (aqui.getY() >= level.getMaxY() || !substituível(level, aqui)) break;
            level.setBlock(aqui, cato, Block.UPDATE_ALL);
        }
        return true;
    }
}
