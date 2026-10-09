package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.imp.ImpEntity;
import net.thaumcraft.occulta.imp.Imps;
import net.thaumcraft.occulta.imp.SoulBrewItem;
import net.thaumcraft.occulta.symbol.SymbolKnowledge;
import net.thaumcraft.occulta.symbol.Symbols;

import java.util.Arrays;

/**
 * O <b>Diabrete</b>: o bicho que negocia.
 *
 * <p>A prova que carrega a fatia é a dos <b>segredos</b>. Ele é a única porta para três dos trinta e um
 * feitiços — o Carnosa Diem, o Morsmordre e o Ignianima — e não os dá: <b>troca-os</b>, por coisas
 * brilhantes, pela ordem, e só quando lhe apetece.
 */
public class OccultaImpGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------ os números

    /** Os números dele são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (ImpEntity.VIDA != 50.0 || ImpEntity.VELOCIDADE != 0.3) {
            helper.fail("cinquenta de vida e três décimos de passo");
        }
        if (ImpEntity.MURRO != 4.0f || ImpEntity.MURRO_LIGADO != 8.0f) {
            helper.fail("bate por quatro, e por oito ligado");
        }
        if (ImpEntity.TETO != 15.0f || ImpEntity.TETO_LIGADO != 5.0f) {
            helper.fail("e nenhum golpe lhe tira mais de quinze, ou de cinco ligado");
        }
        if (ImpEntity.LIGADO_POR != 20 * 60 * 60) helper.fail("o coração o liga por uma hora");
        if (ImpEntity.GOSTA != 20) helper.fail("e ele retribui de vinte de afeição para cima");
        if (ImpEntity.ENTRE_PRESENTES != 20 * 60 * 3) helper.fail("com três minutos entre presentes");
        if (ImpEntity.CUSTA_NÍVEIS != 25) helper.fail("e o contrato custa vinte e cinco níveis");
        if (ImpEntity.CASA != 16.0) helper.fail("e a casa dele é de dezesseis");
        helper.succeed();
    }

    /**
     * <b>As coisas brilhantes, e o que elas valem.</b>
     *
     * <p>Repare na conta: um bloco de diamante vale setenta e dois, que são nove diamantes a oito. Ele
     * não conta valor — conta <b>brilho</b>, e um bloco brilha tanto quanto o que o faz.
     */
    @GameTest
    public void theShiniesAreTheOriginals(GameTestHelper helper) {
        if (Imps.brilho(Items.DIAMOND) != 8) helper.fail("um diamante vale oito");
        if (Imps.brilho(Blocks.DIAMOND_BLOCK.asItem()) != 72) helper.fail("e um bloco deles setenta e dois");
        if (Imps.brilho(Items.EMERALD) != 3) helper.fail("uma esmeralda vale três");
        if (Imps.brilho(Blocks.EMERALD_BLOCK.asItem()) != 27) helper.fail("e um bloco delas vinte e sete");
        if (Imps.brilho(Items.DIAMOND_PICKAXE) != 24) helper.fail("e a picareta paga o feitio: vinte e quatro");
        if (Imps.brilho(Items.NETHER_STAR) != 16) helper.fail("a estrela do nether vale dezesseis");
        if (Imps.brilho(Items.DIRT) != null) helper.fail("e terra não vale nada");
        if (Imps.quantosNomes() != 100) helper.fail("e os nomes dele são cem");
        helper.succeed();
    }

    /** E ele dá os presentes nas contas do original: cinco de lã, mas só um galho. */
    @GameTest
    public void theGiftsComeInTheRightAmounts(GameTestHelper helper) {
        if (Imps.quantos(OccultaItems.BAT_WOOL) != 5) helper.fail("cinco de lã de morcego");
        if (Imps.quantos(OccultaItems.ENT_BRANCH) != 1) helper.fail("mas um galho de ent só");
        if (Imps.quantos(OccultaItems.CREEPER_HEART) != 2) helper.fail("e dois corações de creeper");
        if (Imps.SEGREDOS.length != 4) helper.fail("e os segredos são quatro");
        if (Imps.SEGREDOS[0] != OccultaItems.BREW_SOUL_HUNGER) helper.fail("a Fome vem primeiro");
        if (Imps.SEGREDOS[3] != OccultaItems.CONTRACT_TORMENT) helper.fail("e o Tormento por último");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o contrato

    /**
     * <b>Ele é de quem assinar — e cobra vinte e cinco níveis.</b>
     *
     * <p>Um contrato em branco não serve, um assinado por outro não serve, e sem experiência ele não
     * fecha. É a única coisa do mod que se compra com níveis.
     */
    @GameTest
    public void heIsBoughtWithASignedContract(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.IMP, new BlockPos(3, 2, 3));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        // em branco: não fecha
        ItemStack branco = new ItemStack(OccultaItems.CONTRACT);
        quem.setItemInHand(InteractionHand.MAIN_HAND, branco);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (ele.isTame()) helper.fail("um contrato em branco não o compra");

        // assinado por outro: também não
        ItemStack doOutro = new ItemStack(OccultaItems.CONTRACT);
        doOutro.set(net.thaumcraft.occulta.OccultaComponents.TAGLOCK,
                new TaglockItem.Taglock(java.util.UUID.randomUUID(), "Outro"));
        quem.setItemInHand(InteractionHand.MAIN_HAND, doOutro);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (ele.isTame()) helper.fail("nem um assinado por outra pessoa");

        // assinado por ele, e com níveis: fecha
        ItemStack dele = new ItemStack(OccultaItems.CONTRACT);
        TaglockItem.bind(dele, quem);
        quem.setItemInHand(InteractionHand.MAIN_HAND, dele);
        if (ele.mobInteract(quem, InteractionHand.MAIN_HAND) != InteractionResult.SUCCESS) {
            helper.fail("e o dele fecha o negócio");
        }
        if (!ele.isTame()) helper.fail("e ele passa a ser de quem assinou");
        if (!ele.isOwnedBy(quem)) helper.fail("e sabe de quem");
        if (ele.getCustomName() == null) helper.fail("e ganha um nome de demônio");
        if (!ele.home().equals(ele.blockPosition())) helper.fail("e o lugar vira a casa dele");

        ele.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ as coisas brilhantes

    /**
     * <b>Os segredos, pela ordem.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Ele dá os quatro na ordem do original — Fome, Medo,
     * Angústia, Tormento — e só depois começa a dar ingredientes. Não há outra maneira de aprender os
     * três feitiços que os três primeiros ensinam.
     */
    @GameTest(maxTicks = 100)
    public void heTradesHisSecretsInOrder(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        var ele = helper.spawn(OccultaEntities.IMP, new BlockPos(3, 2, 3));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        TaglockItem.bind(new ItemStack(OccultaItems.CONTRACT), quem);
        ele.tame(quem);

        /*
         * Com o jogador em criativo, o relógio dos três minutos não conta e o sorteio é o mesmo — de modo
         * que basta ir dando blocos de diamante até ele retribuir quatro vezes.
         */
        var quaisSaíram = new java.util.ArrayList<net.minecraft.world.item.Item>();
        for (int volta = 0; volta < 400 && quaisSaíram.size() < 4; volta++) {
            quem.setItemInHand(InteractionHand.MAIN_HAND,
                    new ItemStack(Blocks.DIAMOND_BLOCK.asItem(), 64));
            ele.mobInteract(quem, InteractionHand.MAIN_HAND);
            for (var caiu : level.getEntitiesOfClass(ItemEntity.class, new AABB(onde).inflate(3.0))) {
                quaisSaíram.add(caiu.getItem().getItem());
                caiu.discard();
            }
        }

        if (quaisSaíram.size() < 4) {
            helper.fail("ele devia ter contado os quatro segredos; contou " + quaisSaíram.size());
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (quaisSaíram.get(i) == Imps.SEGREDOS[i]) continue;
            helper.fail("o segredo " + (i + 1) + " devia ser " + Imps.SEGREDOS[i]
                    + "; foi " + quaisSaíram.get(i));
            return;
        }
        if (ele.segredos() != 4) helper.fail("e ele se lembra de quantos contou");

        ele.discard();
        helper.succeed();
    }

    /** <b>E o que não brilha ele recusa.</b> */
    @GameTest
    public void heRefusesWhatDoesNotShine(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.IMP, new BlockPos(3, 2, 3));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ele.tame(quem);

        ItemStack terra = new ItemStack(Blocks.DIRT.asItem(), 4);
        quem.setItemInHand(InteractionHand.MAIN_HAND, terra);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (ele.afeição() != 0) helper.fail("terra não lhe agrada");
        if (terra.getCount() != 4) helper.fail("e ele nem a come");

        ele.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ ligar e desligar

    /** <b>O Coração de Demônio o liga, e a Agulha de Gelo o apaga.</b> */
    @GameTest
    public void theHeartPowersHimAndTheNeedleDoesNot(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ele = helper.spawn(OccultaEntities.IMP, new BlockPos(3, 2, 3));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ele.tame(quem);
        if (ele.ligado()) helper.fail("ele nasce desligado");

        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.DEMON_HEART));
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (!ele.ligado()) helper.fail("o coração o liga");

        float tinha = ele.getHealth();
        ele.hurtServer(level, level.damageSources().magic(), 100.0f);
        float perdeu = tinha - ele.getHealth();
        if (perdeu > ImpEntity.TETO_LIGADO) {
            helper.fail("e ligado nenhum golpe lhe tira mais de cinco; tirou " + perdeu);
        }

        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.ICY_NEEDLE));
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (ele.ligado()) helper.fail("e a agulha o apaga");

        ele.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ o gole

    /**
     * <b>O gole ensina o feitiço, e nada mais ensina.</b>
     *
     * <p>É a correção que esta fatia traz: o Carnosa Diem e o Ignianima não eram trancados por um livro —
     * são trancados por um <b>Cozimento de Alma</b>, e este é o único caminho até eles.
     */
    @GameTest
    public void theSoulBrewTeachesTheSpell(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

        if (SymbolKnowledge.knows(quem, "carnosadiem")) helper.fail("ninguém nasce sabendo");

        ItemStack frasco = new ItemStack(OccultaItems.BREW_SOUL_HUNGER);
        if (!(frasco.getItem() instanceof SoulBrewItem gole)) {
            helper.fail("o frasco é um Cozimento de Alma");
            return;
        }
        if (!gole.chave().equals("carnosadiem")) helper.fail("e o da Fome ensina o Carnosa Diem");
        frasco.getItem().finishUsingItem(frasco, level, quem);
        if (!SymbolKnowledge.knows(quem, "carnosadiem")) helper.fail("bebido, fica sabido");
        if (SymbolKnowledge.knows(quem, "ignianima")) helper.fail("mas só esse");

        quem.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        helper.succeed();
    }

    /** <b>E os quatro feitiços que pedem chave são esses quatro.</b> */
    @GameTest
    public void fourSpellsAskForAKey(GameTestHelper helper) {
        var comChave = new java.util.ArrayList<String>();
        for (var qual : Symbols.todos()) {
            if (qual.chave != null) comChave.add(qual.chave);
        }
        // dos quatro, três estão portados — o Morsmordre é que ainda não existe
        if (!comChave.contains("carnosadiem")) helper.fail("o Carnosa Diem pede chave");
        if (!comChave.contains("ignianima")) helper.fail("e o Ignianima também");
        if (!comChave.contains("tormentum")) helper.fail("e o Tormentum também");

        // e os imperdoáveis não pedem nenhuma: é isso que os torna imperdoáveis
        for (var qual : Symbols.todos()) {
            if (!qual.imperdoável) continue;
            if (qual.chave == null) continue;
            helper.fail("um imperdoável não se aprende: " + qual.nome);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ as três receitas

    /** As três receitas: a carne estranha, a volta dela e o contrato. */
    @GameTest
    public void theThreeRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        confere(helper, level, "a Carne Estranha", OccultaItems.ODD_PORK, Arrays.asList(
                new ItemStack(OccultaItems.MUTANDIS), new ItemStack(Items.ROTTEN_FLESH)), 1, 2);

        confere(helper, level, "a volta dela", Items.PORKCHOP, Arrays.asList(
                new ItemStack(OccultaItems.MUTANDIS), new ItemStack(OccultaItems.ODD_PORK)), 1, 2);

        ItemStack papel = new ItemStack(Items.PAPER);
        confere(helper, level, "o Contrato", OccultaItems.CONTRACT, Arrays.asList(
                papel, papel, papel,
                papel, new ItemStack(OccultaItems.ODD_PORK), papel,
                papel, papel, new ItemStack(Items.STRING)), 3, 3);

        helper.succeed();
    }

    private static void confere(GameTestHelper helper, ServerLevel level, String nome,
                                net.minecraft.world.item.Item sai,
                                java.util.List<ItemStack> posto, int largura, int altura) {
        var mesa = CraftingInput.of(largura, altura, posto);
        var achou = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, mesa, level);
        if (achou.isEmpty()) {
            helper.fail(nome + " devia fechar receita");
            return;
        }
        ItemStack saiu = achou.get().value().assemble(mesa);
        if (!saiu.is(sai)) helper.fail("de " + nome + " devia sair o certo; saiu " + saiu);
    }
}
