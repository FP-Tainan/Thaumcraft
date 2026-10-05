package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.WickerBundleBlock;
import net.thaumcraft.occulta.wolf.HornedHuntsmanEntity;

/**
 * O <b>Homem de Vime</b>: a figura de oito blocos que, acesa, larga o <b>Caçador Cornudo</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>sangue</b>: a mesma figura, feita de feixes <b>simples</b>, não
 * acende. Só a de feixes <b>ensanguentados</b> é uma oferenda — a outra é só madeira empilhada com jeito.
 *
 * <p>É o segundo caminho para o Caçador, e o mais antigo. O Chifre da Caça chama-o de qualquer lugar; o
 * Homem de Vime pede que alguém o construa, o encha de sangue e lhe ponha fogo.
 */
public class OccultaWickerManGameTest {
    /** Onde os pés da figura ficam na arena. */
    private static final BlockPos PÉS = new BlockPos(2, 2, 2);

    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Monta a figura inteira, com ou sem sangue, ao longo do leste. */
    private static void monta(GameTestHelper helper, boolean sangue) {
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(PÉS);
        var feixe = OccultaBlocks.WICKER_BUNDLE.defaultBlockState()
                .setValue(WickerBundleBlock.SANGUE, sangue);
        int[][] figura = {
            {0, 6}, {1, 6}, {0, 5}, {1, 5},
            {-1, 4}, {0, 4}, {1, 4}, {2, 4},
            {-1, 3}, {0, 3}, {1, 3}, {2, 3},
            {-1, 2}, {0, 2}, {1, 2}, {2, 2},
            {0, 1}, {0, 0}, {1, 0},
        };
        for (int[] onde : figura) {
            level.setBlockAndUpdate(
                    pés.relative(Direction.EAST, onde[0]).above(onde[1]), feixe);
        }
    }

    /** E tira tudo o que ficou. */
    private static void desmonta(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(PÉS);
        for (int a = -3; a <= 4; a++) {
            for (int y = -1; y <= 9; y++) {
                BlockPos ali = pés.relative(Direction.EAST, a).above(y);
                if (!level.getBlockState(ali).isAir()) {
                    level.setBlockAndUpdate(ali, Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (var bicho : level.getEntitiesOfClass(HornedHuntsmanEntity.class,
                new AABB(pés).inflate(12.0))) {
            bicho.discard();
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (WickerBundleBlock.PÓS != 120) helper.fail("a chegada dele levanta cento e vinte pós");
        helper.succeed();
    }

    /**
     * <b>A figura de feixes ensanguentados é o Homem de Vime.</b>
     *
     * <p>Montada e reconhecida, ela aceita o fogo.
     */
    @GameTest(maxTicks = 40)
    public void theBloodiedFigureIsTheWickerMan(GameTestHelper helper) {
        piso(helper);
        monta(helper, true);
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(PÉS);

        if (!WickerBundleBlock.éAFigura(level, pés, Direction.EAST)) {
            helper.fail("a figura montada é a figura");
        }
        desmonta(helper);
        helper.succeed();
    }

    /**
     * <b>E a de feixes simples não é.</b>
     *
     * <p>Esta é a prova que carrega a fatia. A mesma figura, pedra por pedra, com feixes que nunca viram
     * sangue — e o molde recusa-a. O que a faz é o sangue, não a forma.
     */
    @GameTest(maxTicks = 40)
    public void andThePlainOneIsNot(GameTestHelper helper) {
        piso(helper);
        monta(helper, false);
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(PÉS);

        if (WickerBundleBlock.éAFigura(level, pés, Direction.EAST)) {
            helper.fail("sem sangue ela não é o Homem de Vime");
        }
        desmonta(helper);
        helper.succeed();
    }

    /**
     * <b>Acesa, ela arde e larga o Caçador.</b>
     *
     * <p>Doze fogos nos lugares que o original escolheu — o peito, a barriga, as pernas e as duas pontas dos
     * braços — e o Caçador de pé no meio dela, com a entrada que estoura.
     *
     * <p>E ficam <b>dez</b>, não doze, porque dois deles — o (0,+1) e o (+1,+1) — ficam <b>cercados de
     * fogo</b> depois de todos postos: sem chão por baixo e sem vime ao lado que pegue, o jogo apaga-os no
     * mesmo instante. O original perde-os pela mesma razão, e não faz diferença nenhuma: o vime arde a vinte
     * de espalhar e os outros dez voltam a acendê-los antes de a figura cair.
     */
    @GameTest(maxTicks = 60)
    public void litItBurnsAndTheHuntsmanWalksOut(GameTestHelper helper) {
        piso(helper);
        monta(helper, true);
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(PÉS);

        if (!WickerBundleBlock.acende(level, pés.above(2), 0.0f)) {
            helper.fail("o isqueiro acha a figura a partir de qualquer feixe dela");
        }

        int fogos = 0;
        for (int a = -1; a <= 2; a++) {
            for (int y = 0; y <= 7; y++) {
                if (level.getBlockState(pés.relative(Direction.EAST, a).above(y)).is(Blocks.FIRE)) {
                    fogos++;
                }
            }
        }
        if (fogos < 10) helper.fail("ela pega fogo em dez lugares ou mais, e foram " + fogos);

        var caçadores = level.getEntitiesOfClass(HornedHuntsmanEntity.class,
                new AABB(pés).inflate(8.0));
        if (caçadores.isEmpty()) helper.fail("e de dentro dela sai o Caçador Cornudo");

        desmonta(helper);
        helper.succeed();
    }

    /**
     * <b>Um feixe solto não é figura nenhuma.</b>
     *
     * <p>O isqueiro num feixe sem vizinhos não faz nada — nem acha a figura, nem põe fogo.
     */
    @GameTest(maxTicks = 40)
    public void aloneItIsJustABundle(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos sozinho = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(sozinho, OccultaBlocks.WICKER_BUNDLE.defaultBlockState()
                .setValue(WickerBundleBlock.SANGUE, true));

        if (WickerBundleBlock.acende(level, sozinho, 0.0f)) {
            helper.fail("um feixe sozinho não é figura nenhuma");
        }
        level.setBlockAndUpdate(sozinho, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
