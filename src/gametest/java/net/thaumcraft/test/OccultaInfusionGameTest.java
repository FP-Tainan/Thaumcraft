package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.infusion.InfernalInfusion;
import net.thaumcraft.occulta.infusion.LightInfusion;
import net.thaumcraft.occulta.infusion.OtherwhereInfusion;

/**
 * A <b>Infusão</b>, e a primeira delas: a do <b>Outro Lugar</b>.
 *
 * <p>É o maior passo que o ofício dá: até aqui tudo o que a bruxa faz está fora dela — o caldeirão, o
 * círculo, o altar, o boneco. A infusão é a primeira coisa que ela faz <b>a si própria</b>.
 *
 * <p>A prova que carrega a fatia é a do <b>cantil</b>: a carga que o rito dá é tudo o que há, cada poder
 * gasta, e quem tenta um poder caro com pouco fica com <b>zero</b>.
 */
public class OccultaInfusionGameTest {
    /** Um chão de pedra na arena inteira, para o bicho ter por onde andar. */
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Ninguém nasce infundido, e quem não é não faz nada. */
    @GameTest
    public void nobodyIsBornInfused(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeAttached(Infusions.CARGA);

        if (Infusions.de(quem) != Infusions.NENHUMA) helper.fail("ninguém nasce infundido");
        if (Infusions.energia(quem) != 0 || Infusions.teto(quem) != 0) {
            helper.fail("e sem infusão não há carga nenhuma");
        }
        if (Infusions.quantas() != 5) {
            helper.fail("há a de ninguém, a da Luz, a do Mundo, a do Outro Lugar e a Infernal; há "
                    + Infusions.quantas());
        }
        helper.succeed();
    }

    /** O rito dá duzentas cargas e põe o teto nelas. */
    @GameTest
    public void theRiteFillsTheFlask(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        Infusion outroLugar = Infusions.daquele(3);
        if (!(outroLugar instanceof OtherwhereInfusion)) {
            helper.fail("a de número três é a do Outro Lugar");
        }

        Infusions.infunde(quem, outroLugar, Infusions.CARGAS);
        if (Infusions.de(quem) != outroLugar) helper.fail("e passa a ser a dele");
        if (Infusions.energia(quem) != Infusions.CARGAS) {
            helper.fail("com duzentas cargas; tem " + Infusions.energia(quem));
        }
        if (Infusions.teto(quem) != Infusions.CARGAS) helper.fail("e o teto nelas");
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }

    /**
     * <b>O cantil, e o que acontece a quem o esvazia mal.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Tirar carga com o {@code tira} — que é o que o equipamento de
     * fora faz — só <b>recusa</b> quando não há bastante. Mas o salto da infusão, que gasta por dentro,
     * <b>apaga o que sobrava</b>: quem tenta um poder caro com pouco fica com zero.
     */
    @GameTest
    public void theFlaskEmptiesAndPunishes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Infusions.infunde(quem, Infusions.daquele(3), 10);

        if (!Infusions.tira(level, quem, 4, false)) helper.fail("dez dá para quatro");
        if (Infusions.energia(quem) != 6) {
            helper.fail("e sobram seis; sobraram " + Infusions.energia(quem));
        }
        if (Infusions.tira(level, quem, 20, false)) helper.fail("mas não dá para vinte");
        if (Infusions.energia(quem) != 6) {
            helper.fail("e recusar não tira nada; ficaram " + Infusions.energia(quem));
        }

        // e o poder que gasta por dentro apaga o que sobrava
        Infusions.de(quem).largou(level, quem, new ItemStack(OccultaItems.WITCH_HAND), 0);
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }

    /** Encher não passa do teto: a Estátua de Adoração não dá mais do que o rito deu. */
    @GameTest
    public void fillingNeverPassesTheBrim(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        Infusions.infunde(quem, Infusions.daquele(3), 100);
        Infusions.põeEnergia(quem, 80);

        Infusions.enche(quem, 30);
        if (Infusions.energia(quem) != 100) {
            helper.fail("oitenta mais trinta dá cem, que é o teto; deu " + Infusions.energia(quem));
        }
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }

    /**
     * <b>E o lugar de voltar fica guardado, com o mundo em que estava.</b>
     *
     * <p>É o que faz a Infusão do Outro Lugar valer a pena: ela não leva só para onde se vê, leva para onde
     * se esteve.
     */
    @GameTest
    public void theRecallPointRemembersTheWorldToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeAttached(Infusions.VOLTA);
        if (Infusions.volta(quem) != null) helper.fail("ninguém nasce com um lugar de voltar");

        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        quem.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        Infusions.guardaVolta(quem);

        var volta = Infusions.volta(quem);
        if (volta == null) helper.fail("e depois de o guardar, tem");
        else {
            if (volta.onde() != level.dimension()) helper.fail("com o mundo em que ele estava");
            if (volta.lugar().getY() != onde.getY()) {
                helper.fail("e o lugar; veio " + volta.lugar());
            }
        }
        quem.removeAttached(Infusions.VOLTA);
        helper.succeed();
    }

    /**
     * <b>A Infusão da Luz esconde quem a tem — e faz quem o perseguia esquecer.</b>
     *
     * <p>A metade boa do poder é a segunda: a invisibilidade de trinta batidas só esconde, mas o esquecer
     * <b>desfaz a perseguição</b>.
     *
     * <p>Esta prova usa um jogador de mentira <b>da sobrevivência</b>, e tem de ser assim: o
     * {@code setTarget} de qualquer bicho passa o alvo por um filtro que recusa quem for criativo, e o
     * jogador de mentira que entra no mundo nasce criativo sem jeito de sair. Este outro não tem ligação de
     * rede, e por isso não se lhe podem dar poções — o que esconder se prova noutra.
     */
    @GameTest(maxTicks = 60)
    public void theHuntersForgetTheHunted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(
                net.minecraft.world.level.GameType.SURVIVAL);
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);

        var zumbi = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        zumbi.setTarget(quem);
        if (zumbi.getTargetUnchecked() != quem) helper.fail("o zumbi devia estar perseguindo");

        LightInfusion.esquecem(level, quem);
        if (zumbi.getTargetUnchecked() == quem) {
            helper.fail("e a luz dobrada faz com que ele perca o alvo");
        }
        zumbi.discard();
        helper.succeed();
    }

    /**
     * <b>E quem a dobra fica invisível — e aparece no instante em que larga a Mão.</b>
     *
     * <p>A invisibilidade é <b>tirada</b> ao largar, e não deixada a acabar sozinha: quem se esconde com
     * luz emprestada fica visível quando o empréstimo acaba.
     */
    @GameTest(maxTicks = 40)
    public void theLightHidesAndStopsHiding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        Infusions.infunde(quem, Infusions.daquele(1), Infusions.CARGAS);
        quem.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);

        var mão = new ItemStack(OccultaItems.WITCH_HAND);
        Infusions.de(quem).segurando(level, quem, mão, Infusions.SEGURA - LightInfusion.DOBRA);
        if (!quem.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY)) {
            helper.fail("a luz dobrada esconde quem a dobra");
        }

        Infusions.de(quem).largou(level, quem, mão, Infusions.SEGURA);
        if (quem.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY)) {
            helper.fail("e largar a Mão a tira na hora");
        }
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }
    /**
     * <b>A Infusão Infernal toma bichos para si, e depois os aponta.</b>
     *
     * <p>Um soco agachado escraviza; um soco sem agachar manda <b>todos os seus</b> irem atrás do que
     * levou o soco. É a diferença entre lutar e <b>apontar</b>.
     */
    @GameTest(maxTicks = 60)
    public void theInfernalTakesAndThenPoints(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(
                net.minecraft.world.level.GameType.SURVIVAL);
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        Infusions.infunde(quem, Infusions.daquele(4), Infusions.CARGAS);

        var zumbi = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        var ovelha = helper.spawn(net.minecraft.world.entity.EntityTypes.SHEEP, new BlockPos(6, 2, 6));
        var mão = new ItemStack(OccultaItems.WITCH_HAND);

        // agachado, o soco toma-o para si
        quem.setShiftKeyDown(true);
        Infusions.de(quem).soca(level, quem, mão, zumbi);
        if (!net.thaumcraft.occulta.enslave.Enslavement.escravoDe(zumbi, quem)) {
            helper.fail("o soco agachado devia tomá-lo para si");
        }
        if (Infusions.energia(quem) != Infusions.CARGAS - InfernalInfusion.CUSTO_ESCRAVIZAR) {
            helper.fail("e custa cinco; custou "
                    + (Infusions.CARGAS - Infusions.energia(quem)));
        }

        // e sem agachar, o soco noutro manda-o atrás dele
        quem.setShiftKeyDown(false);
        Infusions.de(quem).soca(level, quem, mão, ovelha);
        if (zumbi.getTargetUnchecked() != ovelha) {
            helper.fail("e o soco sem agachar manda os seus atrás de quem o levou");
        }
        zumbi.discard();
        ovelha.discard();
        helper.succeed();
    }

    /** Os números da Infusão do Outro Lugar são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (OtherwhereInfusion.ALCANCE != 40 || OtherwhereInfusion.POR_SEGUNDO != 20) {
            helper.fail("quarenta de partida, e mais vinte por segundo segurado");
        }
        if (OtherwhereInfusion.CUSTO_SALTO != 1 || OtherwhereInfusion.CUSTO_VOLTA != 2
                || OtherwhereInfusion.CUSTO_ACIMA != 2 || OtherwhereInfusion.CUSTO_LEVAR != 4) {
            helper.fail("um, dois, dois e quatro");
        }
        if (OtherwhereInfusion.GUARDA != 60) helper.fail("e três segundos agachado para guardar o lugar");
        if (Infusions.DANO != 100.0f) helper.fail("e o rito faz cem de dano a quem se infunde");
        helper.succeed();
    }
}
