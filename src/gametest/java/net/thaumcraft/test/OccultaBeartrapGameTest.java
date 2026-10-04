package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.trap.BeartrapBlock;
import net.thaumcraft.occulta.trap.BeartrapBlockEntity;
import net.thaumcraft.occulta.wolf.WolfmanEntity;

/**
 * As duas armadilhas: a de <b>ferro</b>, que apanha quem passa, e a de <b>prata</b>, que apanha <b>um</b>.
 *
 * <p>A prova que carrega a fatia é a da <b>mordida que pega</b>. Até aqui, qualquer lobisomem deste porte
 * passava a licantropia a quem mordesse, e isso estava errado: no original <b>só um</b> lobisomem é
 * contagioso, e a única coisa que o torna contagioso é a <b>Armadilha de Lobo</b>.
 *
 * <p>Que é dizer: ninguém apanha a doença por azar. Quem a quer <b>tem de a preparar</b> — uma ovelha na
 * corda, um Altar do Lobo e uma lua cheia —, e quem não a quer pode andar por um mato cheio de lobisomens
 * sem nenhum risco de a apanhar.
 */
public class OccultaBeartrapGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (BeartrapBlock.DANO != 4.0f) helper.fail("ela tira quatro, de dano de bigorna");
        if (BeartrapBlock.PRESO != 600) helper.fail("e prende trinta segundos, que são seiscentas batidas");
        if (BeartrapBlock.GRAU != 2) helper.fail("no terceiro grau da paralisia, que é o número dois");
        if (BeartrapBlockEntity.ACALMA != 20) helper.fail("e leva vinte batidas a ficar sensível");
        if (BeartrapBlockEntity.ISCA != 8.0) helper.fail("a isca dela fica a oito blocos");
        if (BeartrapBlockEntity.PERTO != 16 || BeartrapBlockEntity.LONGE != 32) {
            helper.fail("e o lobisomem nasce entre dezesseis e trinta e dois");
        }
        helper.succeed();
    }

    /**
     * <b>Ela nasce disparada</b>, e um clique a arma.
     *
     * <p>É do original, e faz sentido: uma armadilha não se arma sozinha ao cair da mão de ninguém. Quem a
     * põe no chão tem de se abaixar e armá-la — e outro clique volta a desarmá-la.
     */
    @GameTest(maxTicks = 40)
    public void itIsBornSprungAndAClickSetsIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(casa, OccultaBlocks.BEARTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }

        if (!armadilha.disparada()) helper.fail("ela nasce disparada");
        armadilha.vira(level);
        if (armadilha.disparada()) helper.fail("e um clique a arma");
        armadilha.vira(level);
        if (!armadilha.disparada()) helper.fail("e outro clique a desarma");

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * E ela <b>espera vinte batidas</b> antes de ficar sensível.
     *
     * <p>É o tempo de quem a armou tirar o pé de cima dela. Sem isso, armar uma armadilha seria o mesmo que
     * pisar nela.
     */
    @GameTest(maxTicks = 60)
    public void itWaitsTwentyTicksBeforeItBites(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(5, 2, 2));
        level.setBlockAndUpdate(casa, OccultaBlocks.BEARTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }

        armadilha.vira(level);
        if (armadilha.sensível(level)) helper.fail("recém-armada, ela ainda não morde");

        helper.runAfterDelay(BeartrapBlockEntity.ACALMA + 2, () -> {
            if (!armadilha.sensível(level)) helper.fail("passadas as vinte batidas, ela morde");
            level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /**
     * <b>Ela apanha quem pisa</b>: quatro de dano e trinta segundos no lugar.
     *
     * <p>A prova usa uma ovelha, que é o que o original usa de isca e serve bem de vítima. Depois de ferrar,
     * a armadilha fica <b>disparada</b> — ela morde uma vez só, e quem a quiser outra vez tem de a armar.
     */
    @GameTest(maxTicks = 80)
    public void itCatchesWhateverStepsOnIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 5);
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, OccultaBlocks.BEARTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }
        armadilha.vira(level);

        helper.runAfterDelay(BeartrapBlockEntity.ACALMA + 2, () -> {
            var ovelha = helper.spawn(EntityTypes.SHEEP, onde);
            float antes = ovelha.getHealth();

            helper.runAfterDelay(10, () -> {
                if (!armadilha.disparada()) helper.fail("ela dispara em quem pisa");
                if (ovelha.getHealth() >= antes) helper.fail("e fere quem pisa");
                if (!ovelha.hasEffect(OccultaEffects.PARALYSIS)) {
                    helper.fail("e prende quem pisa no lugar");
                }
                ovelha.discard();
                level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
                helper.succeed();
            });
        });
    }

    /**
     * <b>A de prata não apanha quem não é lobisomem.</b>
     *
     * <p>Uma ovelha pode dormir em cima dela. É de propósito: ela não é para pegar gente nem bicho, é para
     * pegar <b>um</b>.
     */
    @GameTest(maxTicks = 80)
    public void theWolftrapIgnoresEveryoneElse(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(5, 2, 5);
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, OccultaBlocks.WOLFTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }
        if (!armadilha.dePrata()) helper.fail("esta é a de prata");
        armadilha.vira(level);

        helper.runAfterDelay(BeartrapBlockEntity.ACALMA + 2, () -> {
            var ovelha = helper.spawn(EntityTypes.SHEEP, onde);
            float antes = ovelha.getHealth();

            helper.runAfterDelay(10, () -> {
                if (armadilha.disparada()) helper.fail("a de prata não dispara numa ovelha");
                if (ovelha.getHealth() < antes) helper.fail("nem a fere");
                ovelha.discard();
                level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
                helper.succeed();
            });
        });
    }

    /**
     * <b>E nem um lobisomem qualquer.</b>
     *
     * <p>Só o que ela mesma chamou. Um lobisomem que apareça por outro caminho passa por cima dela e nada
     * lhe acontece — porque a Armadilha de Lobo não é uma armadilha, é o fim de uma receita.
     */
    @GameTest(maxTicks = 80)
    public void andNotEvenAnyOldWerewolf(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 2);
        BlockPos casa = helper.absolutePos(onde);
        level.setBlockAndUpdate(casa, OccultaBlocks.WOLFTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }
        armadilha.vira(level);

        helper.runAfterDelay(BeartrapBlockEntity.ACALMA + 2, () -> {
            WolfmanEntity lobo = helper.spawn(OccultaEntities.WOLFMAN, onde);
            if (armadilha.apanhaOLobo(lobo)) helper.fail("não é este o lobisomem dela");
            if (lobo.contagioso()) helper.fail("e por isso a mordida dele não pega");
            lobo.discard();
            level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
            helper.succeed();
        });
    }

    /**
     * <b>A mordida de um lobisomem não pega.</b>
     *
     * <p>Esta é a prova que carrega a fatia, e ela é o contrário do que este porte fazia antes: um lobisomem
     * nasce <b>sem contágio</b>, e sem contágio a mordida dele não passa licantropia a ninguém.
     *
     * <p>O caminho para um contagioso existe, e é um só: a Armadilha de Lobo o chama, ele pisa nela, e é ela
     * que vira a chave. Fora disso, nenhum.
     */
    @GameTest(maxTicks = 40)
    public void aWerewolfsBiteDoesNotCatchByDefault(GameTestHelper helper) {
        piso(helper);
        WolfmanEntity lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(6, 2, 6));
        if (lobo.contagioso()) helper.fail("um lobisomem nasce sem contágio");

        lobo.contagioso(true);
        if (!lobo.contagioso()) helper.fail("e a armadilha é quem vira a chave");

        lobo.discard();
        helper.succeed();
    }

    /**
     * <b>Ela se esconde de quem não a pôs</b>, e deixa de se esconder ao disparar.
     *
     * <p>Uma armadilha disparada já não serve de nada escondida; uma sem dono — posta por comando ou por
     * geração — também não se esconde; e a de prata nunca se esconde, que não é para pegar gente.
     */
    @GameTest(maxTicks = 40)
    public void itHidesFromEveryoneButWhoeverSetIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(6, 2, 2));
        level.setBlockAndUpdate(casa, OccultaBlocks.BEARTRAP.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof BeartrapBlockEntity armadilha)) {
            helper.fail("a armadilha tem alma");
            return;
        }

        // disparada, está à vista de todos
        if (!armadilha.àVistaDe(null)) helper.fail("disparada, ela está à vista");
        armadilha.vira(level);
        // armada e sem dono, também: é o caso de quem a põe por comando
        if (!armadilha.àVistaDe(null)) helper.fail("sem dono, ela está à vista");

        var quem = helper.makeMockServerPlayerInLevel();
        armadilha.dono(quem);
        if (!armadilha.àVistaDe(quem)) helper.fail("quem a pôs a vê");
        if (armadilha.àVistaDe(null)) helper.fail("e mais ninguém");

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
