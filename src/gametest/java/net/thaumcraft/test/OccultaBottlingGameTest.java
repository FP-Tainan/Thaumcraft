package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Bottling;
import net.thaumcraft.occulta.brew.Brew;

import java.util.List;

/**
 * A <b>perícia de engarrafar</b>: a coisa mais calada do ofício, e a maior.
 *
 * <p>Um caldeirão tem três mil de caldo. Quem nunca engarrafou tira dele <b>um frasco</b>; quem já
 * engarrafou muito, e tiver o chapéu, o manto, o sapo e a Perícia de Cozimento a correr, tira
 * <b>trinta</b>. Trinta vezes mais, e o jogo nunca o diz.
 *
 * <p>A prova que carrega a fatia é a da <b>árvore de rendimento</b>: nove ramos de condições que o
 * original escreve à mão, e cada um deles é a diferença entre dois frascos e quinze. Um ramo trocado não
 * acusa nada — dá frascos a menos, e quem joga pensa que é assim.
 */
public class OccultaBottlingGameTest {
    /**
     * Um cozimento <b>que cabe</b>, para as contas que não dependem do que está na panela.
     *
     * <p>A raiz sozinha abre um grau, e o veneno gasta dois: faltava-lhe espaço. A verruga abre mais
     * dois, e aí ele cabe — que é a regra do caldeirão e não uma esquisitice da prova.
     */
    private static final List<Item> SIMPLES = List.of(OccultaItems.MANDRAKE_ROOT,
            Items.NETHER_WART, Items.SPIDER_EYE);

    /** <b>A tabela dos dez degraus</b>, que é a do original. */
    @GameTest(maxTicks = 20)
    public void theTenStepsAreTheOriginals(GameTestHelper helper) {
        int[] frascos = {1, 2, 3, 4, 5, 6, 8, 10, 15, 30};
        int[] dreno = {3000, 1500, 1000, 750, 600, 500, 375, 300, 200, 100};
        if (!java.util.Arrays.equals(Bottling.FRASCOS, frascos)
                || !java.util.Arrays.equals(Bottling.DRENO, dreno)) {
            helper.fail("os dez degraus são os do original");
            return;
        }
        for (int i = 0; i < frascos.length; i++) {
            if (frascos[i] * dreno[i] < Bottling.TANQUE) {
                helper.fail("cada degrau tem de encher o tanque: " + frascos[i] + " x " + dreno[i]);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Quem nunca engarrafou leva o caldeirão inteiro num frasco.</b>
     *
     * <p>É onde o porte estava antes desta fatia, e tinha de continuar a ser: ninguém que já jogava há de
     * acordar com o caldeirão diferente.
     */
    @GameTest(maxTicks = 20)
    public void withoutSkillTheWholeCauldronIsOneFlask(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        if (Bottling.perícia(quem) != 0) {
            helper.fail("ninguém nasce sabendo engarrafar");
            return;
        }
        if (Bottling.dreno(quem, SIMPLES) != Bottling.TANQUE) {
            helper.fail("sem perícia, um frasco leva os três mil: leva "
                    + Bottling.dreno(quem, SIMPLES));
            return;
        }
        if (Bottling.frascos(quem, SIMPLES) != 1) {
            helper.fail("e dá um frasco só");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E aprende-se engarrafando</b> — mas só com cozimento à altura.
     *
     * <p>Abaixo de trinta basta um cozimento que gaste mais do que um grau; dos trinta aos sessenta
     * pede-se quatro. Quem só fizer cozimentos simples empaca nos trinta.
     */
    @GameTest(maxTicks = 20)
    public void oneLearnsOnlyFromRealBrews(GameTestHelper helper) {
        var fraco = new net.thaumcraft.occulta.brew.BrewCapacity();
        fraco.openIf(1, 1);
        fraco.consume(1);
        if (Bottling.ensina(0, fraco)) {
            helper.fail("um cozimento de um grau não ensina nada a ninguém");
            return;
        }

        var bom = new net.thaumcraft.occulta.brew.BrewCapacity();
        bom.openIf(4, 10);
        bom.consume(4);
        if (!Bottling.ensina(0, bom)) {
            helper.fail("um de quatro graus ensina quem não sabe nada");
            return;
        }
        if (Bottling.ensina(60, bom)) {
            helper.fail("mas já não ensina quem está nos sessenta: esse precisa de seis");
            return;
        }

        var cheio = new net.thaumcraft.occulta.brew.BrewCapacity();
        cheio.openIf(9, 10);
        cheio.consume(8);
        if (!Bottling.ensina(90, cheio)) {
            helper.fail("e um de oito ensina até aos cem");
            return;
        }
        if (Bottling.ensina(Bottling.TETO, cheio)) {
            helper.fail("nos cem não se aprende mais nada");
            return;
        }

        /*
         * E o caldeirão vazio não ensina: teto zero é o primeiro ramo da guarda do original.
         *
         * <p>O outro ramo dela — <b>gastar mais do que o teto</b> — não se consegue alcançar neste porte,
         * porque o {@code consume} recusa o que não cabe em vez de o deixar passar. Fica a guarda, que é
         * do original e não custa nada.
         */
        if (Bottling.ensina(0, new net.thaumcraft.occulta.brew.BrewCapacity())) {
            helper.fail("um caldeirão de água não ensina nada");
            return;
        }
        helper.succeed();
    }

    /** <b>E o ponto entra mesmo</b>, com aviso nos três degraus. */
    @GameTest(maxTicks = 20)
    public void thePointGoesIn(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        var bom = new net.thaumcraft.occulta.brew.BrewCapacity();
        bom.openIf(4, 10);
        bom.consume(4);

        Bottling.aprende(quem, bom);
        if (Bottling.perícia(quem) != 1) {
            helper.fail("um frasco, um ponto: tem " + Bottling.perícia(quem));
            return;
        }
        // um cozimento de quatro graus ensina até aos sessenta, e aí empaca: dali pede-se seis
        for (int n = 0; n < 200; n++) Bottling.aprende(quem, bom);
        if (Bottling.perícia(quem) != 60) {
            helper.fail("um cozimento de quatro graus leva até aos sessenta: parou em "
                    + Bottling.perícia(quem));
            return;
        }

        // e um de oito leva daí até aos cem, e não passa
        var cheio = new net.thaumcraft.occulta.brew.BrewCapacity();
        cheio.openIf(9, 10);
        cheio.consume(8);
        for (int n = 0; n < 200; n++) Bottling.aprende(quem, cheio);
        if (Bottling.perícia(quem) != Bottling.TETO) {
            helper.fail("e um de oito leva até aos cem: parou em " + Bottling.perícia(quem));
            return;
        }
        helper.succeed();
    }

    /**
     * <b>A árvore do rendimento</b>, ramo a ramo.
     *
     * <p>Nove ramos, e a árvore não é linear: só a partir da perícia sessenta é que o equipamento e a
     * poção começam a valer de verdade. Aqui prova-se o degrau de cada combinação sem vestir nada —
     * chamando a conta por dentro com os números que ela leria.
     */
    @GameTest(maxTicks = 20)
    public void theYieldTreeHasNineBranches(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();

        quem.setAttached(Bottling.PERÍCIA, 0);
        if (Bottling.degrau(quem, SIMPLES) != 0) {
            helper.fail("perícia zero é o primeiro degrau");
            return;
        }
        quem.setAttached(Bottling.PERÍCIA, 30);
        if (Bottling.degrau(quem, SIMPLES) != 1) {
            helper.fail("perícia trinta, sem nada vestido, é o segundo");
            return;
        }
        quem.setAttached(Bottling.PERÍCIA, 60);
        if (Bottling.degrau(quem, SIMPLES) != 2) {
            helper.fail("e perícia sessenta, sem nada vestido, é o terceiro");
            return;
        }
        quem.setAttached(Bottling.PERÍCIA, 99);
        if (Bottling.degrau(quem, SIMPLES) != 2) {
            helper.fail("e noventa e nove é o mesmo: a perícia sozinha para no terceiro");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E a penalidade de rendimento desce um degrau por grau.</b>
     *
     * <p>O diamante abre espaço no caldeirão e cobra <b>dois</b> degraus por isso. É a única coisa da
     * tabela que cobra, e o bónus de rendimento só serve para apagar a penalidade — nunca para passar
     * dela, que é a conta esquisita do {@code ModifierYield}.
     */
    @GameTest(maxTicks = 20)
    public void theDiamondCostsTwoSteps(GameTestHelper helper) {
        if (Bottling.penalidade(SIMPLES) != 0) {
            helper.fail("um cozimento comum não tem penalidade nenhuma");
            return;
        }
        var comDiamante = new java.util.ArrayList<>(SIMPLES);
        comDiamante.add(Items.DIAMOND);
        if (Bottling.penalidade(List.copyOf(comDiamante)) != 2) {
            helper.fail("o diamante cobra dois degraus: cobra "
                    + Bottling.penalidade(List.copyOf(comDiamante)));
            return;
        }

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setAttached(Bottling.PERÍCIA, 60);
        if (Bottling.degrau(quem, List.copyOf(comDiamante)) != 0) {
            helper.fail("e com ele na panela, quem está no terceiro degrau cai para o primeiro");
            return;
        }
        helper.succeed();
    }

    /** E a conta do espaço continua a ser a de sempre, que é de onde a perícia aprende. */
    @GameTest(maxTicks = 20)
    public void theCapacityIsWhatTeaches(GameTestHelper helper) {
        var espaço = Brew.capacity(SIMPLES);
        if (espaço.max() <= 0) {
            helper.fail("a raiz de mandrágora abre espaço");
            return;
        }
        if (espaço.used() <= 0) {
            helper.fail("e o olho de aranha gasta-o");
            return;
        }
        helper.succeed();
    }
}
