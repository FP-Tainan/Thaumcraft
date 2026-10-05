package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.AlluringSkullBlock;
import net.thaumcraft.occulta.AlluringSkullBlockEntity;
import net.thaumcraft.occulta.InfinityEggBlock;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * A <b>Caveira do Chamado</b> e o <b>Ovo do Infinito</b>: as duas coisas que se põem no chão e puxam o mundo
 * para elas — uma puxa os mortos, a outra puxa o poder.
 *
 * <p>A prova que carrega a fatia é a do <b>chamado</b>: a caveira acordada manda um zumbi andar na direção
 * dela de sessenta e quatro blocos de distância.
 */
public class OccultaAlluringSkullGameTest {
    /**
     * Um chão de pedra na arena inteira.
     *
     * <p>Sem ele o bicho não tem por onde andar, e um chamado que ninguém pode atender parece um chamado
     * que não funciona.
     */
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Posta, ela está dormindo e não faz nada. */
    @GameTest
    public void itSleepsUntilWoken(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.ALLURING_SKULL.defaultBlockState());

        if (level.getBlockState(onde).getValue(AlluringSkullBlock.ACORDADA)) {
            helper.fail("uma caveira posta está dormindo");
        }
        if (level.getBlockState(onde).getLightEmission() != 7) {
            helper.fail("e dá sete de luz; deu " + level.getBlockState(onde).getLightEmission());
        }
        if (AlluringSkullBlock.QUADRANTES != 8 || AlluringSkullBlock.VOLTA != 100) {
            helper.fail("oito quadrantes, um de cinco em cinco segundos");
        }
        helper.succeed();
    }

    /** Ela cai com aquilo a que está pregada. */
    @GameTest(maxTicks = 40)
    public void itFallsWithWhatHoldsIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.ALLURING_SKULL.defaultBlockState());

        level.setBlockAndUpdate(onde.below(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(onde).is(OccultaBlocks.ALLURING_SKULL)) {
            helper.fail("sem chão por baixo ela cai");
        }
        helper.succeed();
    }

    /**
     * <b>Acordada, ela puxa os mortos-vivos.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Um zumbi parado a alguns blocos dela, e o chamado do quadrante
     * em que ele está o faz andar.
     */
    @GameTest(maxTicks = 60)
    public void awakeItPullsTheUndead(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));

        Zombie zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 5));

        /*
         * <b>Vinte batidas antes de chamar.</b> Um bicho acabado de nascer ainda não tocou o chão, e o jogo
         * não traça caminho nenhum para quem está no ar. Sem esta espera, o chamado acha o zumbi e não
         * consegue mandá-lo a lado nenhum — o que parece um chamado estragado e não é.
         */
        helper.runAfterDelay(20, () -> {
            zumbi.getNavigation().stop();
            if (!zumbi.getNavigation().isDone()) helper.fail("o zumbi devia estar parado");

            /*
             * As oito voltas, que é o que a caveira faz em quarenta segundos. Qual delas o apanha depende de
             * que lado da caveira a arena o pôs — e a arena de uma prova pode estar <b>girada</b> —, mas ao
             * fim das oito ele tem de estar andando: é isso que quer dizer dar a volta ao mundo.
             */
            int ouviram = 0;
            for (int quadrante = 0; quadrante < AlluringSkullBlock.QUADRANTES; quadrante++) {
                ouviram += AlluringSkullBlockEntity.chama(level, onde, quadrante);
            }
            if (ouviram == 0) helper.fail("algum dos oito quadrantes devia achar o zumbi");
            if (zumbi.getNavigation().isDone()) {
                helper.fail("o chamado devia pô-lo a andar para a caveira; ouviram " + ouviram);
            }
            zumbi.discard();
            helper.succeed();
        });
    }

    /** E ela não chama o que não é morto-vivo. */
    @GameTest(maxTicks = 60)
    public void itDoesNotCallTheLiving(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(5, 2, 5));
        helper.runAfterDelay(20, () -> {
            ovelha.getNavigation().stop();
            /*
             * E a pergunta é feita <b>à ovelha</b>, e não ao número que o chamado devolve: ele tem sessenta
             * e quatro blocos de alcance, e as arenas das provas são vizinhas — um zumbi da prova do lado
             * entra na conta e não diz nada sobre esta.
             */
            for (int quadrante = 0; quadrante < AlluringSkullBlock.QUADRANTES; quadrante++) {
                AlluringSkullBlockEntity.chama(level, onde, quadrante);
            }
            if (!ovelha.getNavigation().isDone()) {
                helper.fail("uma ovelha não ouve o chamado dos mortos");
            }
            ovelha.discard();
            helper.succeed();
        });
    }

    /**
     * <b>E o Ovo do Infinito multiplica o altar por dez.</b>
     *
     * <p>Não é um enfeite como a caveira ou o candelabro: é o fim da escala.
     */
    @GameTest
    public void theEggMultipliesTheAltarByTen(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        for (int volta = 0; volta < net.thaumcraft.occulta.AltarBlock.PIECES; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 3, 0, volta / 3),
                    OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        if (!(level.getBlockEntity(canto) instanceof net.thaumcraft.occulta.AltarBlockEntity pedra)) {
            helper.fail("as seis pedras deviam fazer um altar");
            return;
        }
        var manda = pedra.core();
        if (manda == null) {
            helper.fail("e uma delas devia mandar");
            return;
        }

        manda.refresh();
        if (manda.powerScale() != 1 || manda.rechargeScale() != 1) {
            helper.fail("um altar pelado vale um de cada");
        }

        level.setBlockAndUpdate(canto.above(), OccultaBlocks.INFINITY_EGG.defaultBlockState());
        manda.refresh();
        if (manda.powerScale() != InfinityEggBlock.VEZES) {
            helper.fail("o ovo multiplica o teto por dez; ficou " + manda.powerScale());
        }
        if (manda.rechargeScale() != InfinityEggBlock.VEZES) {
            helper.fail("e a velocidade também; ficou " + manda.rechargeScale());
        }
        helper.succeed();
    }

    /** O ovo não foge quando lhe batem, que é todo o ponto dele. */
    @GameTest(maxTicks = 40)
    public void theEggDoesNotRunAway(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(onde, OccultaBlocks.INFINITY_EGG.defaultBlockState());

        level.getBlockState(onde).attack(level, onde,
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
        if (!level.getBlockState(onde).is(OccultaBlocks.INFINITY_EGG)) {
            helper.fail("o Ovo do Infinito não foge de quem lhe bate");
        }
        helper.succeed();
    }
}
