package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.vampire.AttackBatEntity;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampirePowers;
import net.thaumcraft.occulta.vampire.VampirePowers.Poder;
import net.thaumcraft.occulta.vampire.VampirePowers.Supremo;
import net.thaumcraft.occulta.vampire.VampireStats;
import net.thaumcraft.occulta.vampire.VampireTick;
import net.thaumcraft.occulta.wolf.Werewolf;

/**
 * O que um vampiro <b>faz</b>: prender, correr, virar morcego e os três Supremos.
 *
 * <p>A prova que carrega a fatia é a da <b>forma de morcego</b>, e não pelo voo: é porque ela custa
 * <b>menos seis de dano</b>. Um vampiro de décimo grau em forma de morcego bate menos do que gente, e isso
 * diz o que os cinco poderes dele são — nenhum deles serve para ganhar uma briga. Ele ganha por
 * <b>chegar antes</b>.
 */
public class OccultaVampirePowersGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um vampiro do grau pedido, com o sangue cheio e a quem a vampirice custe alguma coisa. */
    private static Player vampiro(GameTestHelper helper, int grau) {
        Player quem = helper.makeMockServerPlayerInLevel();
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        }
        quem.getAbilities().instabuild = false;
        quem.getAbilities().invulnerable = false;
        quem.onUpdateAbilities();

        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, grau);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        return quem;
    }

    /**
     * Tira a espera de meio segundo, para que uma prova possa usar o poder duas vezes na mesma batida.
     *
     * <p>No jogo ela existe e é do original; aqui ela só estorvaria a conta.
     */
    private static void jáPode(Player quem) {
        quem.setAttached(VampirePowers.ÚLTIMO, quem.tickCount - VampirePowers.ESPERA);
    }

    // ------------------------------------------------------------------ os números

    /** Os cinco poderes e a escada deles são os do original. */
    @GameTest(maxTicks = 20)
    public void theFivePowersAreTheOriginals(GameTestHelper helper) {
        if (Poder.values().length != 6) helper.fail("são cinco poderes e o nenhum");
        if (Poder.BEBER.custa != 0 || Poder.BEBER.grau != 1) helper.fail("beber não custa, e abre ao primeiro");
        if (Poder.PRENDER.custa != 50 || Poder.PRENDER.grau != 2) helper.fail("prender custa cinquenta, ao segundo");
        if (Poder.VELOCIDADE.custa != 10 || Poder.VELOCIDADE.grau != 4) helper.fail("correr custa dez, ao quarto");
        if (Poder.MORCEGO.custa != 50 || Poder.MORCEGO.grau != 7) helper.fail("o morcego custa cinquenta, ao sétimo");
        if (Poder.MORCEGO.mantém != 1) helper.fail("e um por volta do relógio para ficar nele");
        if (Poder.SUPREMO.grau != 10) helper.fail("e o Supremo só ao décimo");

        int[] escada = {0, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5};
        for (int grau = 0; grau <= 10; grau++) {
            if (VampirePowers.QUANTOS[grau] != escada[grau]) {
                helper.fail("a escada de quantos poderes ele tem é a do original, e falhou no grau " + grau);
            }
        }
        if (VampirePowers.USOS != 5) helper.fail("um Supremo vem com cinco usos");
        if (VampirePowers.ESPERA != 10) helper.fail("e a espera é de meio segundo");
        helper.succeed();
    }

    /**
     * <b>A escada não salta o que falta: ela não chega lá.</b>
     *
     * <p>Passar ao poder seguinte para no último que o grau dele dá, e a volta seguinte devolve-o a
     * <i>nenhum</i>. Um vampiro de segundo grau nunca vê a palavra "morcego" na tela — e é assim que o mod
     * conta a escada sem escrever uma linha.
     */
    @GameTest(maxTicks = 20)
    public void theLadderOfPowersStopsWhereTheGradeStops(GameTestHelper helper) {
        Player quem = vampiro(helper, 2);
        if (VampirePowers.quantosPode(quem) != 2) helper.fail("ao segundo grau ele tem dois poderes");

        VampirePowers.escolhe(quem, Poder.NENHUM);
        VampirePowers.seguinte(quem);
        if (VampirePowers.escolhido(quem) != Poder.BEBER) helper.fail("o primeiro é beber");
        VampirePowers.seguinte(quem);
        if (VampirePowers.escolhido(quem) != Poder.PRENDER) helper.fail("o segundo é prender");
        VampirePowers.seguinte(quem);
        if (VampirePowers.escolhido(quem) != Poder.NENHUM) {
            helper.fail("e dali ele volta a nenhum, porque a velocidade ainda não é dele");
        }

        // e ao décimo grau a volta inteira passa pelos cinco
        Vampire.grau(quem, 10);
        if (VampirePowers.quantosPode(quem) != 5) helper.fail("ao décimo grau ele tem os cinco");
        for (Poder qual : new Poder[]{Poder.BEBER, Poder.PRENDER, Poder.VELOCIDADE, Poder.MORCEGO,
                Poder.SUPREMO, Poder.NENHUM}) {
            VampirePowers.seguinte(quem);
            if (VampirePowers.escolhido(quem) != qual) helper.fail("a volta devia passar por " + qual);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ prender

    /** <b>Só gente se prende olhando</b>: aldeão, jogador e guarda, e mais nada. */
    @GameTest(maxTicks = 40)
    public void onlyPeopleCanBeTransfixed(GameTestHelper helper) {
        piso(helper);
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        var galinha = helper.spawn(EntityTypes.CHICKEN, new BlockPos(5, 2, 3));
        var vira = helper.spawn(net.thaumcraft.occulta.OccultaEntities.WERE_VILLAGER,
                new BlockPos(3, 2, 5));

        if (!VampirePowers.prendível(aldeão)) helper.fail("um aldeão se prende");
        if (VampirePowers.prendível(galinha)) helper.fail("uma galinha não");
        if (VampirePowers.prendível(vira)) {
            helper.fail("e um aldeão que vira também não: o que corre nele já é outra maldição");
        }

        aldeão.discard();
        galinha.discard();
        vira.discard();
        helper.succeed();
    }

    /**
     * <b>Prender custa cinquenta e paralisa</b>, e o grau diz quanto e quão fundo.
     *
     * <p>Do oitavo grau em diante a paralisia sobe ao <b>quinto</b> grau da poção — e é esse o número que
     * importa, porque do quarto para cima a {@linkplain net.thaumcraft.occulta.vampire.Blood presa conta
     * como desacordada} e dá <b>todo</b> o sangue que se lhe pede. Prender e beber é o laço inteiro de um
     * vampiro.
     */
    @GameTest(maxTicks = 40)
    public void transfixingCostsFiftyAndHolds(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 2);
        VampirePowers.escolhe(quem, Poder.PRENDER);

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        int tinha = Vampire.sangueDe(quem);
        if (!VampirePowers.prende(level, quem, aldeão)) helper.fail("o toque era dele");
        if (Vampire.sangueDe(quem) != tinha - 50) helper.fail("e custou cinquenta");

        var preso = aldeão.getEffect(OccultaEffects.PARALYSIS);
        if (preso == null) helper.fail("o aldeão ficou preso");
        if (preso != null && preso.getAmplifier() != VampirePowers.PRENDE_GRAU) {
            helper.fail("ao segundo grau a paralisia é do quarto grau da poção");
        }
        if (preso != null && preso.getDuration() != (5 + 2 / 2) * 20) {
            helper.fail("e dura cinco segundos mais meio grau, e durou " + preso.getDuration());
        }

        // e preso já, não se prende outra vez: o sangue não se gasta duas vezes
        int agora = Vampire.sangueDe(quem);
        jáPode(quem);
        VampirePowers.prende(level, quem, aldeão);
        if (Vampire.sangueDe(quem) != agora) helper.fail("quem já está preso não custa mais nada");

        // do oitavo grau a poção sobe um grau
        aldeão.removeEffect(OccultaEffects.PARALYSIS);
        Vampire.grau(quem, 8);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        VampirePowers.prende(level, quem, aldeão);
        var fundo = aldeão.getEffect(OccultaEffects.PARALYSIS);
        if (fundo == null || fundo.getAmplifier() != VampirePowers.PRENDE_GRAU_ALTO) {
            helper.fail("do oitavo grau em diante a paralisia é do quinto grau da poção");
        }

        aldeão.discard();
        helper.succeed();
    }

    /** E <b>abaixo do segundo grau não se prende nada</b>, nem com o sangue cheio. */
    @GameTest(maxTicks = 40)
    public void transfixingNeedsTheSecondGrade(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 1);
        VampirePowers.escolhe(quem, Poder.PRENDER);

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        int tinha = Vampire.sangueDe(quem);
        if (!VampirePowers.prende(level, quem, aldeão)) helper.fail("o toque ainda era dele");
        if (aldeão.hasEffect(OccultaEffects.PARALYSIS)) helper.fail("mas não prendeu");
        if (Vampire.sangueDe(quem) != tinha) helper.fail("e não custou nada");

        aldeão.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ a velocidade

    /**
     * <b>A velocidade dobra, e o grau diz até onde.</b>
     *
     * <p>Cada dose dobra a Rapidez que ele já tem — dois, quatro, oito — e soma três segundos ao que estava
     * correndo em vez de recomeçar. Um vampiro de quarto grau corre <b>uma vez</b>; um de décimo, quatro.
     * Quem quiser a velocidade cheia tem de a construir dose a dose <b>antes</b> de precisar dela.
     */
    @GameTest(maxTicks = 40)
    public void speedDoublesAndTheGradeSaysHowFar(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 4);
        VampirePowers.escolhe(quem, Poder.VELOCIDADE);

        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        var corre = quem.getEffect(MobEffects.SPEED);
        if (corre == null || corre.getAmplifier() != 1) helper.fail("a primeira dose dá Rapidez II");
        if (quem.getEffect(MobEffects.JUMP_BOOST) == null) helper.fail("e Salto ao lado dela");
        if (Vampire.sangueDe(quem) != tinha - 10) helper.fail("e custou dez");
        int durava = corre == null ? 0 : corre.getDuration();

        // ao quarto grau ele corre uma vez só: a segunda não passa
        jáPode(quem);
        int agora = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        var ainda = quem.getEffect(MobEffects.SPEED);
        if (ainda != null && ainda.getAmplifier() != 1) helper.fail("ao quarto grau não há segunda dose");
        if (Vampire.sangueDe(quem) != agora) helper.fail("e a que não passa não custa");

        // ao décimo, passa — e soma três segundos ao que já estava correndo
        Vampire.grau(quem, 10);
        jáPode(quem);
        VampirePowers.usa(level, quem);
        var mais = quem.getEffect(MobEffects.SPEED);
        if (mais == null || mais.getAmplifier() != 3) helper.fail("a segunda dose dá Rapidez IV");
        if (mais != null && mais.getDuration() <= durava) {
            helper.fail("e soma ao que já durava em vez de recomeçar");
        }
        helper.succeed();
    }

    /** E de <b>morcego</b> não há velocidade: ele já voa. */
    @GameTest(maxTicks = 40)
    public void aBatDoesNotRun(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);

        VampirePowers.escolhe(quem, Poder.MORCEGO);
        VampirePowers.usa(level, quem);
        if (!VampirePowers.emMorcego(quem)) helper.fail("ele devia estar de morcego");

        VampirePowers.escolhe(quem, Poder.VELOCIDADE);
        jáPode(quem);
        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        if (quem.hasEffect(MobEffects.SPEED)) helper.fail("um morcego não corre");
        if (Vampire.sangueDe(quem) != tinha) helper.fail("e não paga por não correr");

        VampirePowers.tiraOMorcego(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o morcego

    /**
     * <b>A forma de morcego custa cinquenta, e um por volta do relógio.</b>
     *
     * <p>Acabando o sangue, ele deixa de ser morcego <b>onde estiver</b> — e se estiver no ar, cai. Não há
     * aviso nenhum: a barra de sangue é o aviso.
     */
    @GameTest(maxTicks = 40)
    public void theBatCostsFiftyAndOnePerClock(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 7);
        VampirePowers.escolhe(quem, Poder.MORCEGO);

        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        if (!VampirePowers.emMorcego(quem)) helper.fail("ao sétimo grau ele vira morcego");
        if (Vampire.sangueDe(quem) != tinha - 50) helper.fail("e custou cinquenta");
        if (!quem.getAbilities().mayfly) helper.fail("e ganhou as asas");

        /*
         * E com as asas, a queda não lhe pega. É o jogo que o faz, e não o mod: quem pode voar não se magoa
         * a cair, e é por isso que o original não precisa de escrever uma linha para isso.
         */
        if (quem.causeFallDamage(20.0, 1.0f, level.damageSources().fall())) {
            helper.fail("um morcego não se magoa a cair");
        }

        // a volta do relógio cobra um
        int voando = Vampire.sangueDe(quem);
        VampireTick.cobra(level, quem);
        if (Vampire.sangueDe(quem) >= voando) helper.fail("cada volta do relógio lhe cobra sangue");
        if (!VampirePowers.emMorcego(quem)) helper.fail("e com sangue ele continua morcego");

        // sem sangue, as asas somem
        Vampire.sangue(quem, 0);
        VampireTick.cobra(level, quem);
        if (VampirePowers.emMorcego(quem)) helper.fail("sem sangue não há morcego");
        if (quem.getAbilities().mayfly) helper.fail("e as asas somem com ele");
        helper.succeed();
    }

    /** E <b>abaixo do sétimo grau</b> não há morcego nenhum. */
    @GameTest(maxTicks = 40)
    public void theBatNeedsTheSeventhGrade(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 6);
        VampirePowers.escolhe(quem, Poder.MORCEGO);

        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        if (VampirePowers.emMorcego(quem)) helper.fail("ao sexto grau ainda não há voo");
        if (Vampire.sangueDe(quem) != tinha) helper.fail("e o que não dá não custa");
        helper.succeed();
    }

    /**
     * <b>E é esta a prova que carrega a fatia: um morcego bate menos do que gente.</b>
     *
     * <p>Menos seis de dano, e o gole de sangue de dez para dois. No original são duas tabelas diferentes, e
     * a do morcego <b>substitui</b> a do vampiro em vez de se somar a ela — de modo que um vampiro de décimo
     * grau, que bate três a mais, em forma de morcego bate <b>seis a menos</b>.
     *
     * <p>É o poder que mais muda o jogo e o que menos serve para brigar, e é de propósito.
     */
    @GameTest(maxTicks = 40)
    public void theBatIsNotForFighting(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);

        if (VampireStats.de(quem) != 3.0f) helper.fail("de gente, o décimo grau bate três a mais");

        VampirePowers.escolhe(quem, Poder.MORCEGO);
        VampirePowers.usa(level, quem);
        if (VampireStats.de(quem) != VampirePowers.MORCEGO_DANO) {
            helper.fail("de morcego ele bate menos seis, e deu " + VampireStats.de(quem));
        }
        if (VampirePowers.MORCEGO_DANO >= 0.0f) helper.fail("e menos seis é menos do que nada");

        var dano = quem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (dano != null && dano.getValue() >= 1.0) {
            helper.fail("e a pancada dele não vale nada, e valeu " + dano.getValue());
        }

        // e o gole passa de dez a dois
        if (VampirePowers.GOLE_DE_MORCEGO != 2 || VampirePowers.GOLE != 10) {
            helper.fail("um morcego não tem boca para mais do que dois");
        }

        VampirePowers.tiraOMorcego(quem);
        if (VampireStats.de(quem) != 3.0f) helper.fail("e saindo dela, o grau volta a contar");
        helper.succeed();
    }

    /**
     * <b>Não se é lobo e morcego ao mesmo tempo</b>, e a <b>lua ganha</b>.
     *
     * <p>No original as duas maldições partilham um único contador de forma, e a mistura é impossível por
     * construção. Aqui são dois apegos separados, e por isso a regra é escrita à mão: de lobo não se vira
     * morcego, e virando lobo, o morcego <b>cai</b>.
     */
    @GameTest(maxTicks = 40)
    public void theBatAndTheWolfDoNotMix(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        Werewolf.grau(quem, 10);
        VampirePowers.escolhe(quem, Poder.MORCEGO);

        // de lobo, não há morcego
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        if (VampirePowers.emMorcego(quem)) helper.fail("um lobo não vira morcego");
        if (Vampire.sangueDe(quem) != tinha) helper.fail("e não paga por não virar");

        // de gente, há
        Werewolf.forma(quem, Werewolf.Forma.GENTE);
        jáPode(quem);
        VampirePowers.usa(level, quem);
        if (!VampirePowers.emMorcego(quem)) helper.fail("de gente ele vira");

        // e a lua, chegando, derruba-o
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        if (VampirePowers.emMorcego(quem)) helper.fail("virando lobo, o morcego cai");

        Werewolf.forma(quem, Werewolf.Forma.GENTE);
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    // ------------------------------------------------------------------ os Supremos

    /** Os três <b>Supremos</b> vêm do Crisol com cinco usos, e cada uso gasta um. */
    @GameTest(maxTicks = 40)
    public void theUltimateComesWithFiveCharges(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        VampirePowers.escolhe(quem, Poder.SUPREMO);

        if (VampirePowers.supremo(quem) != Supremo.NENHUM) helper.fail("ele nasce sem Supremo nenhum");
        if (VampirePowers.cargas(quem) != 0) helper.fail("e sem cargas");

        // sem Supremo escolhido, apertar não faz nada
        VampirePowers.usa(level, quem);
        if (VampirePowers.cargas(quem) != 0) helper.fail("e o que não há não se gasta");

        VampirePowers.dáOSupremo(quem, Supremo.TEMPESTADE);
        if (VampirePowers.cargas(quem) != 5) helper.fail("o Crisol dá cinco usos");

        // e escolher outro troca, em vez de somar
        VampirePowers.dáOSupremo(quem, Supremo.ENXAME);
        if (VampirePowers.supremo(quem) != Supremo.ENXAME) helper.fail("o novo troca o velho");
        if (VampirePowers.cargas(quem) != 5) helper.fail("e volta aos cinco, sem somar");
        helper.succeed();
    }

    /**
     * <b>A tempestade apaga o sol</b>, e não se chama o que já veio.
     *
     * <p>É o Supremo mais calado dos três e o mais útil: um temporal tira o sol, e sem sol um vampiro anda de
     * dia. Ele não ataca ninguém — muda o mundo para caber nele.
     */
    @GameTest(maxTicks = 40)
    public void theStormOnlyComesWhenItIsNotRaining(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        VampirePowers.escolhe(quem, Poder.SUPREMO);
        VampirePowers.dáOSupremo(quem, Supremo.TEMPESTADE);

        var tempo = level.getWeatherData();
        boolean chovia = tempo.isRaining();
        int chuvaAntes = tempo.getRainTime();
        tempo.setRaining(false);

        VampirePowers.usa(level, quem);
        if (!tempo.isRaining()) helper.fail("ela devia ter chamado a chuva");
        if (!tempo.isThundering()) helper.fail("e o trovão com ela");
        if (tempo.getRainTime() < VampirePowers.TEMPESTADE_BASE * 20) {
            helper.fail("e ela dura de cinco a quinze minutos");
        }
        if (VampirePowers.cargas(quem) != 4) helper.fail("e gastou um uso");

        // chovendo já, não faz nada — e não gasta
        jáPode(quem);
        VampirePowers.usa(level, quem);
        if (VampirePowers.cargas(quem) != 4) helper.fail("não se chama o que já veio");

        tempo.setRaining(chovia);
        tempo.setThundering(false);
        tempo.setRainTime(chuvaAntes);
        helper.succeed();
    }

    /**
     * <b>O enxame</b>: morcegos que vão ao que o dono está olhando, e de que nada cai.
     *
     * <p>Quinze deles valem sessenta de dor <b>se todos acertarem</b>, e eles raramente acertam todos: param
     * em paredes, perdem o alvo quando o dono vira a cara, e morrem no primeiro corpo que encontram pelo
     * caminho. E nascem onde cabem — num buraco apertado, nascem menos.
     */
    @GameTest(maxTicks = 60)
    public void theSwarmLeavesNothingBehind(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        VampirePowers.escolhe(quem, Poder.SUPREMO);
        VampirePowers.dáOSupremo(quem, Supremo.ENXAME);

        if (VampirePowers.ENXAME != 15) helper.fail("são quinze morcegos");
        VampirePowers.usa(level, quem);
        if (VampirePowers.cargas(quem) != 4) helper.fail("e o enxame gasta um uso sempre");

        var roda = quem.getBoundingBox().inflate(16.0);
        var bichos = level.getEntitiesOfClass(AttackBatEntity.class, roda);
        if (bichos.isEmpty()) helper.fail("algum morcego devia ter nascido");
        for (var morcego : bichos) {
            if (morcego.dono() != quem) helper.fail("cada um sabe quem o chamou");
            if (!net.thaumcraft.occulta.NoDrops.marcado(morcego)) {
                helper.fail("e de nenhum deles cai nada: quinze por uso seriam uma fábrica");
            }
            morcego.discard();
        }
        if (AttackBatEntity.DANO != 4.0f) helper.fail("cada um dói quatro");
        if (AttackBatEntity.VIDA != 300) helper.fail("e dura trezentas batidas");
        helper.succeed();
    }

    /**
     * <b>O morcego do enxame se gasta no primeiro corpo que apanha</b>, e a dor é do <b>dono</b>.
     *
     * <p>Quem mordeu um aldeão com um enxame mordeu-o com as próprias mãos, para efeito de quem vem atrás.
     */
    @GameTest(maxTicks = 80)
    public void theSwarmBatSpendsItselfOnTheFirstBody(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        float tinha = aldeão.getHealth();

        var morcego = helper.spawn(net.thaumcraft.occulta.OccultaEntities.ATTACK_BAT,
                new BlockPos(3, 2, 3));
        morcego.dono(quem);
        morcego.setResting(false);

        helper.startSequence()
                .thenExecuteAfter(20, () -> {
                    if (aldeão.getHealth() >= tinha) {
                        helper.fail("o morcego devia ter doído ao aldeão");
                    }
                    if (!morcego.isRemoved()) helper.fail("e se gastado nele");
                    aldeão.discard();
                })
                .thenSucceed();
    }

    /**
     * <b>O caminho de casa</b>: ele o leva à cama dele.
     *
     * <p>E estando <b>em casa</b> — a seis blocos dela —, leva-o à aldeia mais perto, que é onde há gente: o
     * Supremo da colheita leva o vampiro ao rebanho dele. Nenhum dos três é um golpe, e este é o que diz
     * melhor o que eles são: um vampiro não precisa de ganhar uma briga, precisa de <b>estar noutro lugar
     * </b>.
     */
    @GameTest(maxTicks = 60)
    public void theRoadHomeGoesToTheBed(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        if (!(vampiro(helper, 10) instanceof net.minecraft.server.level.ServerPlayer quem)) {
            helper.fail("esta prova pede um jogador de verdade");
            return;
        }
        VampirePowers.escolhe(quem, Poder.SUPREMO);
        VampirePowers.dáOSupremo(quem, Supremo.CASA);

        if (VampirePowers.EM_CASA != 6.0) helper.fail("seis blocos da cama é estar em casa");

        BlockPos cama = helper.absolutePos(new BlockPos(1, 2, 1));
        quem.teleportTo(level, cama.getX() + 30.0, cama.getY(), cama.getZ() + 30.0,
                java.util.Set.of(), 0.0f, 0.0f, false);
        quem.setRespawnPosition(new net.minecraft.server.level.ServerPlayer.RespawnConfig(
                net.minecraft.world.level.storage.LevelData.RespawnData.of(
                        level.dimension(), cama, 0.0f, 0.0f), true), false);

        VampirePowers.usa(level, quem);
        if (quem.distanceToSqr(cama.getX() + 0.5, quem.getY(), cama.getZ() + 0.5) > 4.0) {
            helper.fail("ele devia ter ido para a cama dele");
        }
        if (VampirePowers.cargas(quem) != 4) helper.fail("e gastou um uso");
        helper.succeed();
    }

    /** E no <b>criativo</b> nada se gasta: nem sangue, nem cargas. */
    @GameTest(maxTicks = 40)
    public void creativeNeverPays(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        quem.getAbilities().instabuild = true;
        quem.onUpdateAbilities();

        VampirePowers.escolhe(quem, Poder.SUPREMO);
        VampirePowers.dáOSupremo(quem, Supremo.ENXAME);
        int tinha = Vampire.sangueDe(quem);
        VampirePowers.usa(level, quem);
        if (VampirePowers.cargas(quem) != 5) helper.fail("no criativo as cargas não se gastam");
        if (Vampire.sangueDe(quem) != tinha) helper.fail("nem o sangue");

        for (var morcego : level.getEntitiesOfClass(AttackBatEntity.class,
                quem.getBoundingBox().inflate(16.0))) {
            morcego.discard();
        }
        helper.succeed();
    }

    /** E a <b>visão noturna</b> é um interruptor, não um poder a usar: agachado, o prender liga-a. */
    @GameTest(maxTicks = 40)
    public void theVisionIsASwitch(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 2);
        VampirePowers.escolhe(quem, Poder.PRENDER);

        if (VampirePowers.vêNoEscuro(quem)) helper.fail("ela nasce desligada");
        quem.setShiftKeyDown(true);
        VampirePowers.usa(level, quem);
        if (!VampirePowers.vêNoEscuro(quem)) helper.fail("agachado, o prender liga a visão");

        // e o relógio dá-lhe a visão enquanto ela estiver ligada
        VampireTick.cobra(level, quem);
        if (!quem.hasEffect(MobEffects.NIGHT_VISION)) helper.fail("e o relógio mantém-na");

        jáPode(quem);
        VampirePowers.usa(level, quem);
        if (VampirePowers.vêNoEscuro(quem)) helper.fail("e o mesmo gesto desliga-a");
        if (quem.hasEffect(MobEffects.NIGHT_VISION)) helper.fail("e a visão vai-se com ela");

        quem.setShiftKeyDown(false);
        helper.succeed();
    }

    /** A <b>espera de meio segundo</b> vale para todos, e vale mesmo quando o poder falha. */
    @GameTest(maxTicks = 40)
    public void theWaitCountsEvenWhenItFails(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 10);
        VampirePowers.escolhe(quem, Poder.VELOCIDADE);

        VampirePowers.usa(level, quem);
        var corre = quem.getEffect(MobEffects.SPEED);
        if (corre == null) helper.fail("a primeira passou");
        int amp = corre == null ? -1 : corre.getAmplifier();

        // sem tirar a espera, a segunda não passa
        VampirePowers.usa(level, quem);
        var ainda = quem.getEffect(MobEffects.SPEED);
        if (ainda != null && ainda.getAmplifier() != amp) {
            helper.fail("a segunda devia ter batido na espera");
        }

        // e um vivo também apanha o que a paralisia dele dá, para a conta não mentir
        var preso = new MobEffectInstance(OccultaEffects.PARALYSIS, 20, VampirePowers.PRENDE_GRAU);
        if (preso.getAmplifier() != 4) helper.fail("o quarto grau da poção é quatro");
        helper.succeed();
    }
}
