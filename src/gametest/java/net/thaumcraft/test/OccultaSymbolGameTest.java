package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.AltarBlock;
import net.thaumcraft.occulta.AltarBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PlacedItemBlock;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.symbol.Spells;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;

/**
 * Os <b>símbolos</b> e a <b>Vara Mística</b>: o ramo em que o <b>gesto é a interface</b>.
 *
 * <p>Não há menu, não há lista, não há botão: há um desenho que se sabe ou não se sabe fazer. A prova que
 * carrega a fatia é a da <b>tabela de desenhos</b> — cada sequência de traços tem de dar o símbolo certo e
 * o grau certo.
 */
public class OccultaSymbolGameTest {
    /** A tabela de desenhos é a do original, traço por traço. */
    @GameTest
    public void theStrokeTableIsTheOriginals(GameTestHelper helper) {
        if (Symbols.quantos() != 25) {
            helper.fail("são vinte e cinco símbolos; há " + Symbols.quantos());
        }

        // Accio: esquerda, cima, direita, direita, baixo — e de grau um
        byte[] accio = {Symbols.ESQUERDA, Symbols.CIMA, Symbols.DIREITA, Symbols.DIREITA, Symbols.BAIXO};
        Symbol qual = Symbols.doDesenho(accio);
        if (qual == null || qual.id != 1) helper.fail("esse desenho é o Accio; deu " + qual);
        if (Symbols.grauDoDesenho(accio) != 1) helper.fail("e de grau um");

        // e o mesmo desenho alongado é o mesmo símbolo, de grau três
        byte[] maior = {Symbols.ESQUERDA, Symbols.CIMA, Symbols.CIMA, Symbols.CIMA,
            Symbols.DIREITA, Symbols.DIREITA, Symbols.DIREITA, Symbols.BAIXO, Symbols.BAIXO,
            Symbols.BAIXO};
        if (Symbols.doDesenho(maior) != qual) helper.fail("o desenho comprido é o mesmo símbolo");
        if (Symbols.grauDoDesenho(maior) != 3) helper.fail("e de grau três");

        // e um desenho que não é nada não é nada
        if (Symbols.doDesenho(new byte[] {Symbols.CIMA, Symbols.CIMA, Symbols.CIMA}) != null) {
            helper.fail("e três para cima não são símbolo nenhum");
        }
        helper.succeed();
    }

    /**
     * <b>E os catorze são catorze desenhos diferentes.</b>
     *
     * <p>Nenhum deles pode levar ao mesmo lugar que outro: a tabela é a interface, e duas entradas iguais
     * seriam um feitiço que ninguém consegue lançar.
     */
    @GameTest
    public void noTwoSymbolsShareADrawing(GameTestHelper helper) {
        for (int id = 1; id <= 40; id++) {
            Symbol qual = Symbols.daquele(id);
            if (qual == null) continue;
            if (qual.nome.isEmpty()) helper.fail("o de número " + id + " não tem nome");
        }
        /*
         * O Protego é o desenho mais curto que há — <b>dois traços</b> —, e isso faz dele o único que se
         * pode acertar por acidente. É assim no original.
         */
        byte[] protego = {Symbols.BAIXO, Symbols.CIMA};
        Symbol escudo = Symbols.doDesenho(protego);
        if (escudo == null || escudo.id != 31) helper.fail("baixo e cima são o Protego; deu " + escudo);
        helper.succeed();
    }

    /**
     * <b>As três imperdoáveis só se lançam com a Infusão Infernal.</b>
     *
     * <p>É a única porta trancada da tabela, e vale uma prova própria: matar, doer e escravizar de longe
     * não se emprestam a quem tomou a luz ou o mundo para dentro de si.
     */
    @GameTest
    public void theThreeUnforgivablesNeedTheInfernalInfusion(GameTestHelper helper) {
        /*
         * O jogador de mentira tem de ser o <b>da sobrevivência</b>: no criativo tudo serve, e a porta
         * trancada não se vê.
         */
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(
                net.minecraft.world.level.GameType.SURVIVAL);
        for (int qual : new int[]{4, 9, 20}) {
            Symbol maldito = Symbols.daquele(qual);
            if (maldito == null) {
                helper.fail("o de número " + qual + " devia estar na tabela");
                return;
            }
            if (!maldito.maldição || !maldito.imperdoável) {
                helper.fail(maldito.nome + " é maldição e é imperdoável");
            }
            if (maldito.serveAInfusão(quem, Infusions.daquele(1).id)) {
                helper.fail("e não serve à Infusão da Luz");
            }
            if (!maldito.serveAInfusão(quem, Symbols.A_INFERNAL)) {
                helper.fail("mas serve à Infernal");
            }
        }

        // e as duas maldições que não são imperdoáveis servem a qualquer uma
        for (int qual : new int[]{39, 40}) {
            Symbol maldito = Symbols.daquele(qual);
            if (maldito == null || !maldito.maldição || maldito.imperdoável) {
                helper.fail("o de número " + qual + " é maldição, mas não imperdoável");
            } else if (!maldito.serveAInfusão(quem, Infusions.daquele(1).id)) {
                helper.fail("e serve a qualquer infusão");
            }
        }
        helper.succeed();
    }

    /**
     * <b>O Ignianima dói mais quanto pior estiver quem o lança.</b>
     *
     * <p>É a única conta do mod inteiro que premeia estar quase morto: com a vida cheia são dois, e com
     * a vida no fundo são seis mais metade do que falta. Quem o lança sabendo disso lança-o sangrando.
     */
    @GameTest
    public void theIgnianimaRewardsBleeding(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setHealth(quem.getMaxHealth());
        float cheio = net.thaumcraft.occulta.symbol.Symbols.ignianima(quem);
        if (cheio != 2.0f) helper.fail("com a vida cheia, dois; deu " + cheio);

        // com doze de vida ainda está na terceira banda da escada, e vale cinco
        quem.setHealth(quem.getMaxHealth() * 0.6f);
        float meio = net.thaumcraft.occulta.symbol.Symbols.ignianima(quem);
        if (meio != 5.0f) helper.fail("com doze de vida, cinco; deu " + meio);

        // e a dez já cai na conta de baixo, que é seis mais metade do que falta
        quem.setHealth(quem.getMaxHealth() * 0.5f);
        float metade = net.thaumcraft.occulta.symbol.Symbols.ignianima(quem);
        if (metade != 7.0f) helper.fail("com dez de vida, sete; deu " + metade);

        quem.setHealth(1.0f);
        float quase = net.thaumcraft.occulta.symbol.Symbols.ignianima(quem);
        if (quase <= metade) helper.fail("e quase morto, mais ainda; deu " + quase);
        quem.setHealth(quem.getMaxHealth());
        helper.succeed();
    }

    /**
     * <b>E os desenhos dos dez novos são os do original, traço por traço.</b>
     */
    @GameTest
    public void theTenNewDrawingsAreTheOriginals(GameTestHelper helper) {
        // Imperio: direita, baixo, baixo, baixo — e de grau dois
        confere(helper, new byte[]{Symbols.BAIXO, Symbols.BAIXO, Symbols.BAIXO, Symbols.BAIXO}, 20, 2);
        // Flagrate: cima, direita, esquerda, cima, direita — grau dois
        confere(helper, new byte[]{Symbols.CIMA, Symbols.DIREITA, Symbols.ESQUERDA, Symbols.CIMA,
                Symbols.DIREITA}, 16, 2);
        // Attraho: três cimas, duas direitas, baixo, esquerda — grau um
        confere(helper, new byte[]{Symbols.CIMA, Symbols.CIMA, Symbols.CIMA, Symbols.DIREITA,
                Symbols.DIREITA, Symbols.BAIXO, Symbols.ESQUERDA}, 47, 1);
        // Crucio: baixo, esquerda, baixo, baixo, direita — grau um
        confere(helper, new byte[]{Symbols.BAIXO, Symbols.ESQUERDA, Symbols.BAIXO, Symbols.BAIXO,
                Symbols.DIREITA}, 9, 1);
        helper.succeed();
    }

    /** Confere que aquele desenho é aquele símbolo, daquele grau. */
    private static void confere(GameTestHelper helper, byte[] desenho, int id, int grau) {
        Symbol qual = Symbols.doDesenho(desenho);
        if (qual == null || qual.id != id) {
            helper.fail("esse desenho é o de número " + id + "; deu " + qual);
            return;
        }
        if (Symbols.grauDoDesenho(desenho) != grau) {
            helper.fail("e de grau " + grau + "; deu " + Symbols.grauDoDesenho(desenho));
        }
    }

    /** O custo dobra por grau, como no original. */
    @GameTest
    public void theCostDoublesPerDegree(GameTestHelper helper) {
        Symbol accio = Symbols.daquele(1);
        if (accio.custo(1) != 1 || accio.custo(2) != 2 || accio.custo(3) != 4) {
            helper.fail("um, dois e quatro");
        }

        // e o Nox custa cinquenta, com o grau zero do original valendo metade
        Symbol nox = Symbols.daquele(26);
        if (nox.custo(1) != 50) helper.fail("o Nox custa cinquenta; custa " + nox.custo(1));
        if (nox.custo(0) != 25) helper.fail("e no grau zero, vinte e cinco; custa " + nox.custo(0));
        helper.succeed();
    }

    /**
     * <b>O feitiço prepara-se e só depois se lança.</b>
     *
     * <p>Desenhar não lança: guarda. É o que torna o gesto confiável com o atraso da rede.
     */
    @GameTest
    public void theSpellIsPreparedBeforeItIsCast(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeAttached(Spells.PREPARADO);
        if (Spells.preparado(quem) != null) helper.fail("ninguém nasce com um feitiço preparado");

        Spells.prepara(quem, 1, 2);
        var tem = Spells.preparado(quem);
        if (tem == null || tem.id() != 1 || tem.grau() != 2) {
            helper.fail("e depois de o preparar, tem; veio " + tem);
        }

        Spells.esquece(quem);
        if (Spells.preparado(quem) != null) helper.fail("e esquecê-lo apaga-o");
        helper.succeed();
    }

    /**
     * <b>E o grau comprido só vale a quem tem Adoração.</b>
     *
     * <p>Sem ela, um desenho de grau três é lançado como grau um e o esforço foi para nada.
     */
    @GameTest
    public void theLongDrawingNeedsWorship(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.removeEffect(net.thaumcraft.occulta.OccultaEffects.WORSHIP);

        if (Spells.grauQueVale(quem, 1) != 1) helper.fail("o grau um vale sempre");
        if (Spells.grauQueVale(quem, 3) != 1) helper.fail("e o três, sem Adoração, vale um");

        quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.thaumcraft.occulta.OccultaEffects.WORSHIP, 200, 1));
        if (Spells.grauQueVale(quem, 3) != 3) {
            helper.fail("e com Adoração II vale três; valeu " + Spells.grauQueVale(quem, 3));
        }
        quem.removeEffect(net.thaumcraft.occulta.OccultaEffects.WORSHIP);
        helper.succeed();
    }

    /**
     * <b>O Incendio põe fogo onde a bola bate — e acende o Homem de Vime.</b>
     *
     * <p>É a única coisa deste ramo que mexe com outro: o feitiço é o segundo jeito de acender a figura,
     * depois do isqueiro.
     */
    @GameTest(maxTicks = 60)
    public void incendioSetsFire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());

        var incendio = (net.thaumcraft.occulta.symbol.ProjectileSymbol) Symbols.daquele(21);
        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(chão), net.minecraft.core.Direction.UP,
                chão, false);
        incendio.aoBater(level, null, bateu, 1);

        if (!level.getBlockState(chão.above()).is(Blocks.FIRE)) {
            helper.fail("o Incendio põe fogo em cima do que acertou");
        }
        level.setBlockAndUpdate(chão.above(), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E a Vara Mística deitada no altar soma ao poder de encanto dele. */
    @GameTest
    public void theBranchOnTheAltarEnhances(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        for (int volta = 0; volta < AltarBlock.PIECES; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 3, 0, volta / 3),
                    OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        if (!(level.getBlockEntity(canto) instanceof AltarBlockEntity pedra)) {
            helper.fail("as seis pedras deviam fazer um altar");
            return;
        }
        AltarBlockEntity manda = pedra.core();
        if (manda == null) {
            helper.fail("e uma delas devia mandar");
            return;
        }

        manda.refresh();
        if (manda.enhancement() != 0) helper.fail("um altar pelado não encanta nada");

        PlacedItemBlock.põe(level, canto.above(), new ItemStack(OccultaItems.MYSTIC_BRANCH), null);
        manda.refresh();
        if (manda.enhancement() != 1) {
            helper.fail("a vara deitada soma um ao encanto; ficou " + manda.enhancement());
        }
        helper.succeed();
    }
}
