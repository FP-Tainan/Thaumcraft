package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.naturalis.NaturalisItems;
import net.thaumcraft.naturalis.SickleItem;
import net.thaumcraft.research.Researches;
import net.thaumcraft.research.WarpEvents;

import java.util.Arrays;

/**
 * O ramo do Magia Naturalis: a aba, as foices e o que elas ceifam.
 */
public class NaturalisGameTest {
    /** A aba do ramo existe no livro, com as pesquisas do original. */
    @GameTest
    public void theBranchHasItsOwnTab(GameTestHelper helper) {
        if (Researches.get("MN_INTRO") == null) helper.fail("a pesquisa de entrada do ramo devia existir");
        var sickles = Researches.get("MN_SICKLES");
        if (sickles == null) helper.fail("a pesquisa das foices devia existir");
        else if (!sickles.category().equals(net.thaumcraft.naturalis.Naturalis.CATEGORY)) {
            helper.fail("as foices deviam estar na aba do ramo; estão em " + sickles.category());
        }
        helper.succeed();
    }

    /** A foice de táumio sai da receita do original: três lingotes e um graveto. */
    @GameTest
    public void theThaumiumSickleHasItsRecipe(GameTestHelper helper) {
        ItemStack ingot = new ItemStack(net.thaumcraft.registry.TCResources.get("thaumium_ingot"));
        ItemStack stick = new ItemStack(net.minecraft.world.item.Items.STICK);
        ItemStack none = ItemStack.EMPTY;
        var input = CraftingInput.of(3, 3, Arrays.asList(
                none, ingot, none,
                none, none, ingot,
                stick, ingot, none));
        var found = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (found.isEmpty()) helper.fail("a foice de táumio devia fechar receita");
        else if (!found.get().value().assemble(input).is(NaturalisItems.THAUMIUM_SICKLE)) {
            helper.fail("devia sair a foice de táumio");
        }
        helper.succeed();
    }

    /** A foice leva junto o mato encostado; a de táumio alcança dois blocos além do primeiro. */
    @GameTest
    public void theSickleReapsWhatIsBesideIt(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(NaturalisItems.THAUMIUM_SICKLE));
        BlockPos base = new BlockPos(1, 2, 1);
        for (int i = 0; i < 4; i++) helper.setBlock(base.offset(i, 0, 0), Blocks.SHORT_GRASS);
        ItemStack sickle = player.getMainHandItem();
        BlockPos absolute = helper.absolutePos(base);
        sickle.getItem().mineBlock(sickle, helper.getLevel(), helper.getLevel().getBlockState(absolute), absolute, player);
        helper.getLevel().destroyBlock(absolute, false);
        int left = 0;
        for (int i = 0; i < 4; i++) {
            if (helper.getLevel().getBlockState(helper.absolutePos(base.offset(i, 0, 0))).is(Blocks.SHORT_GRASS)) left++;
        }
        if (left > 1) helper.fail("a foice devia ter levado o mato do lado; sobraram " + left);
        helper.succeed();
    }

    /** E ela só ceifa o que é de ceifar: pedra não é da conta dela. */
    @GameTest
    public void theSickleOnlyCutsPlants(GameTestHelper helper) {
        if (SickleItem.cuts(Blocks.STONE.defaultBlockState())) helper.fail("a foice não corta pedra");
        if (!SickleItem.cuts(Blocks.OAK_LEAVES.defaultBlockState())) helper.fail("a foice corta folha");
        if (!SickleItem.cuts(Blocks.WHEAT.defaultBlockState())) helper.fail("a foice corta plantação");
        if (!SickleItem.cuts(Blocks.COBWEB.defaultBlockState())) helper.fail("a foice corta teia");
        helper.succeed();
    }

    /** As sete madeiras arcanas existem, contam como tábua e caem inteiras. */
    @GameTest
    public void theArcaneWoodIsAllThere(GameTestHelper helper) {
        var madeiras = net.thaumcraft.naturalis.NaturalisBlocks.shown();
        if (madeiras.size() != 7) helper.fail("o original tem sete feitios de madeira arcana; há " + madeiras.size());
        for (var bloco : madeiras) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(bloco);
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
                helper.fail(id + " devia ter item de bloco");
            }
        }
        for (var nome : new String[]{"greatwood_planks_horizontal", "silverwood_planks_horizontal", "silverwood_planks_vertical"}) {
            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id(nome));
            if (!new ItemStack(item).is(net.minecraft.tags.ItemTags.PLANKS)) helper.fail(nome + " devia contar como tábua");
        }
        helper.succeed();
    }

    /** A foice do vazio distorce quem a carrega, como no original. */
    @GameTest
    public void theVoidSickleWarps(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack stack = new ItemStack(NaturalisItems.VOID_SICKLE);
        int warp = NaturalisItems.VOID_SICKLE instanceof WarpEvents.WarpingGear gear ? gear.getWarp(stack, player) : -1;
        if (warp != 1) helper.fail("a foice do vazio distorce um; distorce " + warp);
        helper.succeed();
    }
}
