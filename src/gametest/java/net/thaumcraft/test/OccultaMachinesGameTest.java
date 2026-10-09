package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.BloodCrucibleBlock;
import net.thaumcraft.occulta.BloodCrucibleBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brazier.BrazierBlockEntity;
import net.thaumcraft.occulta.brazier.BrazierRecipes;
import net.thaumcraft.occulta.spinning.SpinningRecipes;
import net.thaumcraft.occulta.spinning.SpinningWheelBlockEntity;

import java.util.List;

/**
 * As três máquinas da fatia: a Roca, o Braseiro e o Crisol de Sangue.
 */
public class OccultaMachinesGameTest {
    // ------------------------------------------------------------------ a roca

    /** A tabela da roca casa fibra com temperos, e não casa sem a quantidade certa de fibra. */
    @GameTest
    public void theWheelNeedsEnoughFibre(GameTestHelper helper) {
        var teia = SpinningRecipes.of(Items.COBWEB);
        if (teia == null) {
            helper.fail("a teia devia estar na tabela da roca");
            return;
        }
        if (teia.fibreCount() != 8) helper.fail("ela pede oito de linha, e pede " + teia.fibreCount());

        List<ItemStack> nenhum = List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        if (teia.matches(new ItemStack(Items.STRING, 7), nenhum)) helper.fail("com sete não dá");
        if (!teia.matches(new ItemStack(Items.STRING, 8), nenhum)) helper.fail("com oito dá");
        if (teia.matches(new ItemStack(Items.STRING, 8),
                List.of(new ItemStack(Items.DIAMOND), ItemStack.EMPTY, ItemStack.EMPTY))) {
            helper.fail("e com tempero que ela não pede, não dá");
        }
        helper.succeed();
    }

    /** O Fio Dourado pede o Sopro de Magia, e a roca acha a receita dele. */
    @GameTest
    public void theWheelFindsTheGoldenThread(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.SPINNING_WHEEL.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof SpinningWheelBlockEntity roca)) {
            helper.fail("a roca devia ter alma");
            return;
        }

        roca.setItem(SpinningWheelBlockEntity.FIBRE, new ItemStack(Items.HAY_BLOCK));
        if (roca.recipe() != null) helper.fail("só com feno ela não fia nada");

        roca.setItem(SpinningWheelBlockEntity.MOD_A, new ItemStack(OccultaItems.WHIFF_OF_MAGIC));
        var achou = roca.recipe();
        if (achou == null || achou.result() != OccultaItems.GOLDEN_THREAD) {
            helper.fail("com o Sopro de Magia sai o Fio Dourado");
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    // ------------------------------------------------------------------ o braseiro

    /** O braseiro só acende com três coisas dentro, e o que arde é uma receita. */
    @GameTest
    public void theBrazierLightsOnlyWithAFullSet(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.BRAZIER.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof BrazierBlockEntity braseiro)) {
            helper.fail("o braseiro devia ter alma");
            return;
        }

        braseiro.light();
        if (braseiro.burning()) helper.fail("vazio, ele não acende");

        var fumaça = BrazierRecipes.of("tc.brazier.smoke");
        if (fumaça == null) {
            helper.fail("o Sinal de Fumaça devia estar na tabela");
            return;
        }
        for (var coisa : fumaça.inputs()) braseiro.add(new ItemStack(coisa));
        if (braseiro.burning()) helper.fail("posto, mas não aceso, ele ainda não arde");
        if (braseiro.recipe() == null) helper.fail("e a receita já se acha");

        braseiro.light();
        if (!braseiro.burning()) helper.fail("com o isqueiro, ele arde");
        braseiro.douse();
        if (braseiro.burning()) helper.fail("e com água, apaga");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** As oito receitas do braseiro são de três coisas, e nenhuma casa com as de outra. */
    @GameTest
    public void everyBrazierRecipeIsThreeThings(GameTestHelper helper) {
        var todas = BrazierRecipes.all();
        if (todas.size() != 8) helper.fail("as oito do original, e tem " + todas.size());
        for (var receita : todas) {
            if (receita.inputs().size() != 3) helper.fail(receita.key() + " devia ser de três coisas");
            List<ItemStack> postas = receita.inputs().stream().map(ItemStack::new).toList();
            if (!receita.matches(postas)) helper.fail(receita.key() + " devia casar consigo mesma");
            for (var outra : todas) {
                if (outra == receita) continue;
                if (outra.matches(postas)) helper.fail(outra.key() + " não devia casar com " + receita.key());
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o crisol

    /** O crisol enche de cinco em cinco até vinte, e esvazia de uma vez. */
    @GameTest
    public void theCrucibleFillsFiveAtATime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 4));
        level.setBlockAndUpdate(onde, OccultaBlocks.BLOOD_CRUCIBLE.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof BloodCrucibleBlockEntity crisol)) {
            helper.fail("o crisol devia ter alma");
            return;
        }

        if (crisol.blood() != 0) helper.fail("um crisol novo está vazio");
        for (int i = 0; i < 4; i++) crisol.feed();
        if (crisol.blood() != BloodCrucibleBlockEntity.MAX) helper.fail("quatro goles enchem-no");
        if (!crisol.full()) helper.fail("e cheio ele sabe que está cheio");
        crisol.feed();
        if (crisol.blood() != BloodCrucibleBlockEntity.MAX) helper.fail("e não transborda");
        if (Math.abs(crisol.filled() - 1.0f) > 1.0e-6) helper.fail("e se diz cheio de um a um");

        crisol.drain();
        if (crisol.blood() != 0) helper.fail("e esvazia de uma vez");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Os três dons do crisol são os do original, e nenhuma outra coisa escolhe algum. */
    @GameTest
    public void theCrucibleKnowsItsThreeGifts(GameTestHelper helper) {
        var sup = net.thaumcraft.occulta.vampire.VampirePowers.Supremo.class;
        if (BloodCrucibleBlock.gift(new ItemStack(OccultaItems.WATER_ARTICHOKE_GLOBE))
                != net.thaumcraft.occulta.vampire.VampirePowers.Supremo.TEMPESTADE) {
            helper.fail("a alcachofra escolhe a Tempestade");
        }
        if (BloodCrucibleBlock.gift(new ItemStack(OccultaItems.BAT_WOOL))
                != net.thaumcraft.occulta.vampire.VampirePowers.Supremo.ENXAME) {
            helper.fail("a lã de morcego escolhe o Enxame");
        }
        if (BloodCrucibleBlock.gift(new ItemStack(Items.BONE))
                != net.thaumcraft.occulta.vampire.VampirePowers.Supremo.CASA) {
            helper.fail("o osso escolhe o caminho de casa");
        }
        if (BloodCrucibleBlock.gift(new ItemStack(Items.DIAMOND)) != null) {
            helper.fail("e um diamante não escolhe nada");
        }
        if (sup.getEnumConstants().length != 4) helper.fail("são três dons e o nenhum");

        // e o grau que ele lê é o grau do vampiro, que num mortal é zero
        var quem = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        if (BloodCrucibleBlock.level(quem) != 0) helper.fail("num mortal, o grau é zero");
        helper.succeed();
    }
}
