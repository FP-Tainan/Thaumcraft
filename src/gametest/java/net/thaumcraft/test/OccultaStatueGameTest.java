package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.StatueOfWorshipBlockEntity;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.goblin.GoblinWorshipGoal;
import net.thaumcraft.occulta.infusion.Infusions;

/**
 * A <b>Estátua de Adoração</b>: a única coisa do mod que enche uma infusão.
 *
 * <p>A prova que carrega a fatia é a dos <b>três degraus</b>: cinco goblins enchem, dez dão Adoração,
 * quinze dão Adoração II — e é essa última que destrava o terceiro grau dos símbolos. Sem a estátua, a
 * infusão é um cantil que se enche uma vez; com ela, é uma barra que volta.
 *
 * <p>E a segunda prova é a do <b>dono</b>: uma estátua de bancada não é de ninguém e não faz nada. Para
 * ela servir é preciso o Rito de Prender a Estátua, que a devolve com a cara de quem o fez.
 */
public class OccultaStatueGameTest {
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
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (StatueOfWorshipBlockEntity.PULSO != 100) helper.fail("ela conta de cinco em cinco segundos");
        if (StatueOfWorshipBlockEntity.CUBO != 8.0) helper.fail("num cubo de oito blocos");
        if (StatueOfWorshipBlockEntity.ENCHE != 5 || StatueOfWorshipBlockEntity.ADORA != 10
                || StatueOfWorshipBlockEntity.ADORA_MAIS != 15) {
            helper.fail("e os degraus são cinco, dez e quinze");
        }
        if (StatueOfWorshipBlockEntity.CARGA != 30) helper.fail("trinta de carga por pulso");
        if (StatueOfWorshipBlockEntity.ALCANCE != 64.0) helper.fail("a sessenta e quatro blocos");
        if (StatueOfWorshipBlockEntity.DURA != 1200) helper.fail("e a Adoração dura um minuto");
        if (GoblinWorshipGoal.FICA != 600 || GoblinWorshipGoal.DE_TRÊS != 3) {
            helper.fail("o goblin fica meio minuto, e atende duas vezes em três");
        }
        helper.succeed();
    }

    /**
     * <b>Uma estátua de bancada não é de ninguém.</b>
     *
     * <p>Ela sai da bancada <b>pelada</b>, sem dono, e assim não faz nada. Só o Rito de Prender a Estátua
     * lhe dá uma cara — e é por isso que ela é um ídolo e não uma máquina.
     */
    @GameTest
    public void aCraftedStatueBelongsToNobody(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.STATUE_OF_WORSHIP.defaultBlockState());

        if (!(level.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua)) {
            helper.fail("a estátua devia ter alma");
            return;
        }
        if (estátua.dono() != null) helper.fail("e uma estátua posta à mão não é de ninguém");

        // e batendo nela não acontece nada, que é o que uma pedra faz
        StatueOfWorshipBlockEntity.bate(level, onde, estátua);
        if (estátua.quantos() != 0) helper.fail("sem dono ela nem conta");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E o rito lhe dá uma: o que entra pelado sai com a cara de quem o fez. */
    @GameTest
    public void theRiteGivesHerAFace(GameTestHelper helper) {
        var rito = net.thaumcraft.occulta.rite.RiteRegistry.get("tc.rite.bindstatuetoplayer");
        if (rito == null) {
            helper.fail("o Rito de Prender a Estátua devia estar na lista");
            return;
        }
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ItemStack estátua = new ItemStack(OccultaBlocks.STATUE_OF_WORSHIP);
        if (TaglockItem.isBound(estátua)) helper.fail("ela não nasce presa a ninguém");

        TaglockItem.bind(estátua, quem);
        var dono = TaglockItem.bound(estátua);
        if (dono == null || !dono.owner().equals(quem.getUUID())) {
            helper.fail("e depois de presa, é dele");
        }
        helper.succeed();
    }

    /**
     * <b>Os três degraus.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Cinco adoradores enchem a infusão; dez dão <b>Adoração</b>;
     * quinze dão <b>Adoração II</b>, que é o que o terceiro grau dos símbolos pede.
     *
     * <p>Repare na conta que isso faz: quinze goblins num cubo de oito blocos é <b>uma aldeia inteira</b>
     * junta à volta de um ídolo com a sua cara. O ofício não é discreto nesta ponta.
     */
    @GameTest
    public void theThreeStepsOfWorship(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        /*
         * O jogador de mentira tem de ser o <b>do mundo</b>: o outro não tem ligação de rede, e dar-lhe
         * uma poção rebenta. Ele nasce criativo, e isso aqui não estorva — a estátua não cobra nada.
         */
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        Infusions.infunde(quem, Infusions.daquele(1), Infusions.CARGAS);
        Infusions.põeEnergia(quem, 0);
        quem.removeEffect(OccultaEffects.WORSHIP);

        // quatro adoradores não bastam
        StatueOfWorshipBlockEntity.paga(level, onde, quem, StatueOfWorshipBlockEntity.ENCHE - 1);
        if (Infusions.energia(quem) != 0) {
            helper.fail("quatro adoradores não enchem nada; encheram "
                    + Infusions.energia(quem));
        }

        // cinco enchem trinta
        StatueOfWorshipBlockEntity.paga(level, onde, quem, StatueOfWorshipBlockEntity.ENCHE);
        if (Infusions.energia(quem) != StatueOfWorshipBlockEntity.CARGA) {
            helper.fail("cinco dão trinta; deram " + Infusions.energia(quem));
        }
        if (quem.hasEffect(OccultaEffects.WORSHIP)) helper.fail("mas cinco não dão Adoração");

        // dez dão Adoração
        StatueOfWorshipBlockEntity.paga(level, onde, quem, StatueOfWorshipBlockEntity.ADORA);
        var adoração = quem.getEffect(OccultaEffects.WORSHIP);
        if (adoração == null || adoração.getAmplifier() != 0) {
            helper.fail("dez dão Adoração do primeiro nível; deu " + adoração);
        }

        // e quinze dão o segundo nível, que é o do terceiro grau dos símbolos
        StatueOfWorshipBlockEntity.paga(level, onde, quem, StatueOfWorshipBlockEntity.ADORA_MAIS);
        adoração = quem.getEffect(OccultaEffects.WORSHIP);
        if (adoração == null || adoração.getAmplifier() != 1) {
            helper.fail("quinze dão Adoração II; deu " + adoração);
        }
        if (net.thaumcraft.occulta.symbol.Spells.grauQueVale(quem, 3) != 3) {
            helper.fail("e com ela o terceiro grau dos símbolos vale");
        }
        quem.removeEffect(OccultaEffects.WORSHIP);
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }

    /**
     * <b>E ela nunca enche acima do teto.</b>
     *
     * <p>É a mesma regra do cantil: o que o rito deu é o teto, e a estátua só repõe o que se gastou.
     */
    @GameTest
    public void sheNeverFillsAboveTheBrim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        Infusions.infunde(quem, Infusions.daquele(1), 100);
        Infusions.põeEnergia(quem, 90);

        StatueOfWorshipBlockEntity.paga(level, onde, quem, StatueOfWorshipBlockEntity.ENCHE);
        if (Infusions.energia(quem) != 100) {
            helper.fail("noventa mais trinta dá cem, que é o teto; deu " + Infusions.energia(quem));
        }
        quem.removeAttached(Infusions.CARGA);
        helper.succeed();
    }

    /**
     * <b>O dono atravessa o item.</b>
     *
     * <p>Partida, a estátua leva o dono consigo; posta outra vez, ela volta a ser dele. Sem isso,
     * arrumar a casa apagaria o rito que a prendeu — e o rito custa quatro mil de altar.
     */
    @GameTest
    public void theOwnerSurvivesBeingBroken(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.STATUE_OF_WORSHIP.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua)) {
            helper.fail("a estátua devia ter alma");
            return;
        }

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        estátua.dono(new TaglockItem.Taglock(quem.getUUID(), quem.getName().getString()));

        // o que cai dela leva o dono
        var caiu = net.minecraft.world.level.block.Block.getDrops(level.getBlockState(onde), level,
                onde, estátua, null, ItemStack.EMPTY);
        if (caiu.isEmpty()) {
            helper.fail("ela devia cair em item");
        } else {
            var preso = TaglockItem.bound(caiu.getFirst());
            if (preso == null || !preso.owner().equals(quem.getUUID())) {
                helper.fail("e o item devia levar o dono; levou " + preso);
            }
        }

        // e o que se tira dela com o botão do meio também
        var tirado = level.getBlockState(onde).getCloneItemStack(level, onde, true);
        if (!TaglockItem.isFor(tirado, quem)) {
            helper.fail("e o item tirado com o botão do meio também");
        }

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Ela conta os goblins que a adoram — e manda adorar os que não adoram.</b>
     *
     * <p>E conta <b>antes</b> de mandar: quem acabou de ser chamado só entra na conta do pulso seguinte.
     * É por isso que uma estátua recém-posta leva uns segundos a pagar.
     */
    @GameTest(maxTicks = 60)
    public void sheCountsBeforeSheCalls(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.STATUE_OF_WORSHIP.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua)) {
            helper.fail("a estátua devia ter alma");
            return;
        }

        var um = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(4, 2, 4));
        var dois = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(5, 2, 4));
        if (estátua.conta(level) != 0) helper.fail("no primeiro pulso ninguém adora ainda");

        um.adorando(true);
        dois.adorando(true);
        if (estátua.conta(level) != 2) {
            helper.fail("e depois de adorarem, são dois; foram " + estátua.conta(level));
        }

        um.discard();
        dois.discard();
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
