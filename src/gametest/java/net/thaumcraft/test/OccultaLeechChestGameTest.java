package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.LeechChestBlockEntity;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * O <b>Baú de Sanguessugas</b>: o baú que anota o nome de quem o abre.
 *
 * <p>A prova que carrega a fatia é a dos <b>três nomes</b>: ele guarda os três mais recentes, não guarda
 * ninguém duas vezes, e mostra <b>um saco de sangue por nome</b>. Os sacos são a parte honesta da armadilha
 * — ficam à vista, e quem souber lê neles quantas pessoas já caíram.
 */
public class OccultaLeechChestGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (LeechChestBlockEntity.NOMES != 3) helper.fail("ele guarda três nomes");
        if (LeechChestBlockEntity.LUGARES != 27) helper.fail("e tem os vinte e sete lugares de um baú");
        helper.succeed();
    }

    /**
     * <b>Ele anota quem o abre, e não anota ninguém duas vezes.</b>
     *
     * <p>Um baú aberto dez vezes pela mesma pessoa tem um saco, não dez — e o quarto nome empurra o primeiro
     * para fora.
     */
    @GameTest(maxTicks = 40)
    public void itWritesDownWhoOpensItOnlyOnce(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(casa, OccultaBlocks.LEECH_CHEST.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof LeechChestBlockEntity baú)) {
            helper.fail("o baú tem alma");
            return;
        }

        var quem = helper.makeMockServerPlayerInLevel();
        baú.anota(quem);
        if (baú.quantosNomes() != 1) helper.fail("quem o abre deixa o nome");
        baú.anota(quem);
        if (baú.quantosNomes() != 1) {
            helper.fail("e deixa uma vez só, e deu " + baú.quantosNomes());
        }

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>E devolve um nome que não seja o de quem pergunta.</b>
     *
     * <p>Quem abriu o próprio baú não se prende a si mesmo; e um nome de alguém que saiu do mundo fica
     * guardado para quando ele voltar, em vez de dar um frasco que não prende ninguém.
     */
    @GameTest(maxTicks = 40)
    public void itNeverGivesYouYourOwnName(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(casa, OccultaBlocks.LEECH_CHEST.defaultBlockState());
        if (!(level.getBlockEntity(casa) instanceof LeechChestBlockEntity baú)) {
            helper.fail("o baú tem alma");
            return;
        }

        var quem = helper.makeMockServerPlayerInLevel();
        baú.anota(quem);
        if (baú.tiraUmNome(quem) != null) helper.fail("ele não dá a quem pergunta o nome dele mesmo");
        if (baú.quantosNomes() != 1) helper.fail("e não gasta o nome por tentar");

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>De onde ele vem</b>: um baú <b>armadilhado</b> vazio, com quatro trepadeiras à volta e água nas
     * quinas de baixo.
     *
     * <p>O original pede o armadilhado e não o comum, e a escolha é dele: o que vai nascer dali é uma
     * armadilha, e ela começa numa armadilha.
     */
    @GameTest(maxTicks = 40)
    public void aTrappedChestInVinesBecomesLeechChests(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos baú = helper.absolutePos(new BlockPos(4, 2, 4));

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado).below(), Blocks.WATER.defaultBlockState());
        }
        level.setBlockAndUpdate(baú, Blocks.CHEST.defaultBlockState());
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado), Blocks.VINE.defaultBlockState());
        }
        if (MutandisItem.éBaúArmadilhado(level, baú)) {
            helper.fail("um baú comum não serve: o original pede o armadilhado");
        }

        level.setBlockAndUpdate(baú, Blocks.TRAPPED_CHEST.defaultBlockState());
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado), Blocks.VINE.defaultBlockState());
        }
        if (!MutandisItem.éBaúArmadilhado(level, baú)) helper.fail("e o armadilhado serve");

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(baú.relative(lado).below(), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(baú, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * E a <b>Rosa de Sangue</b> volta a pedir o que o original pedia.
     *
     * <p>Até esta fatia, ela saía de um <b>baú comum</b>, porque o Baú de Sanguessugas não existia e o elo
     * do meio da corrente estava declarado como buraco no {@code PORTE.md}. Agora o elo está lá: baú
     * armadilhado vira Baú de Sanguessugas, e Baú de Sanguessugas vira Rosas de Sangue.
     */
    @GameTest(maxTicks = 40)
    public void theBloodRoseNowComesFromTheLeechChest(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 6));
        level.setBlockAndUpdate(onde.below(), Blocks.WATER.defaultBlockState());

        // um baú comum já não serve
        level.setBlockAndUpdate(onde, Blocks.CHEST.defaultBlockState());
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.POPPY.defaultBlockState());
        }
        if (MutandisItem.éBaúDeRosas(level, onde)) {
            helper.fail("o baú comum já não dá rosas: o elo do meio voltou");
        }

        level.setBlockAndUpdate(onde, OccultaBlocks.LEECH_CHEST.defaultBlockState());
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.POPPY.defaultBlockState());
        }
        if (!MutandisItem.éBaúDeRosas(level, onde)) {
            helper.fail("e o Baú de Sanguessugas dá");
        }

        // e um baú com coisa dentro não serve
        if (level.getBlockEntity(onde) instanceof LeechChestBlockEntity caixa) {
            caixa.setItem(0, new ItemStack(Items.DIAMOND));
            if (MutandisItem.éBaúDeRosas(level, onde)) helper.fail("nem um baú com coisa dentro");
            caixa.setItem(0, ItemStack.EMPTY);
        }

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }
}
