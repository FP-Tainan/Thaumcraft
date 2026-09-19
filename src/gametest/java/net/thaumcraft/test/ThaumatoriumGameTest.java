package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.EssentiaReservoirBlock;
import net.thaumcraft.block.ItemGrateBlock;
import net.thaumcraft.block.MnemonicMatrixBlock;
import net.thaumcraft.block.ThaumatoriumBlock;
import net.thaumcraft.block.ThaumatoriumStructure;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import net.thaumcraft.block.entity.ItemGrateBlockEntity;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.block.entity.ThaumatoriumTopBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

import java.util.HashSet;

/** O taumatório, a matriz mnemônica e a grade de itens da 4.2.3.5. */
public class ThaumatoriumGameTest {
    static final BlockPos BOTTOM = new BlockPos(2, 2, 2);

    /** Nitor embaixo, crisol, e as duas construções alquímicas viradas taumatório olhando para o norte. */
    static ThaumatoriumBlockEntity form(GameTestHelper helper) {
        helper.setBlock(BOTTOM.below(2), TCBlocks.NITOR);
        helper.setBlock(BOTTOM.below(), TCBlocks.CRUCIBLE);
        helper.setBlock(BOTTOM, TCBlocks.ALCHEMICAL_CONSTRUCT);
        helper.setBlock(BOTTOM.above(), TCBlocks.ALCHEMICAL_CONSTRUCT);
        ThaumatoriumStructure.build(helper.getLevel(), helper.absolutePos(BOTTOM), Direction.NORTH);
        return helper.getBlockEntity(BOTTOM, ThaumatoriumBlockEntity.class);
    }

    static CrucibleRecipe alumentum() {
        return CrucibleRecipes.ALL.stream().filter(r -> r.result().is(TCItems.ALUMENTUM)).findFirst().orElseThrow();
    }

    @GameTest
    public void everyCrucibleRecipeHasItsOwnHash(GameTestHelper helper) {
        var seen = new HashSet<Integer>();
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (!seen.add(recipe.hash())) helper.fail("duas receitas com o mesmo número: " + recipe.result());
            if (CrucibleRecipe.byHash(recipe.hash()) != recipe) helper.fail("o número volta à receita");
        }
        helper.succeed();
    }

    @GameTest
    public void theWandBuildsItOverACrucible(GameTestHelper helper) {
        form(helper);
        var bottom = helper.getBlockState(BOTTOM);
        var top = helper.getBlockState(BOTTOM.above());
        if (!bottom.is(TCBlocks.THAUMATORIUM) || bottom.getValue(ThaumatoriumBlock.TOP) || bottom.getValue(ThaumatoriumBlock.FACING) != Direction.NORTH) {
            helper.fail("a de baixo vira o taumatório, virado para a face batida");
        }
        if (!top.is(TCBlocks.THAUMATORIUM) || !top.getValue(ThaumatoriumBlock.TOP)) helper.fail("a de cima vira a metade de cima");
        helper.succeed();
    }

    @GameTest(maxTicks = 300)
    public void itPullsEssentiaAndMakesTheMarkedRecipe(GameTestHelper helper) {
        ThaumatoriumBlockEntity tile = form(helper);
        var player = helper.makeMockServerPlayerInLevel();
        tile.toggle(alumentum(), player);
        tile.setItem(0, new ItemStack(Items.COAL, 1));
        // um reservatório encostado, com o bocal para o taumatório, com o que o alumentum pede
        BlockPos res = BOTTOM.east();
        helper.setBlock(res, TCBlocks.ESSENTIA_RESERVOIR.defaultBlockState().setValue(EssentiaReservoirBlock.FACING, Direction.WEST));
        EssentiaReservoirBlockEntity reservoir = helper.getBlockEntity(res, EssentiaReservoirBlockEntity.class);
        reservoir.addToContainer(Aspects.ENERGY, 3);
        reservoir.addToContainer(Aspects.FIRE, 3);
        reservoir.addToContainer(Aspects.ENTROPY, 3);
        helper.succeedWhen(() -> {
            int made = helper.getEntities(EntityTypes.ITEM, BOTTOM.north(), 2.0).stream()
                    .filter(e -> e.getItem().is(TCItems.ALUMENTUM)).mapToInt(e -> e.getItem().getCount()).sum();
            if (made != 1) throw helper.assertionException("sai um alumentum pela frente");
            if (!tile.getItem(0).isEmpty()) throw helper.assertionException("o carvão é gasto");
            if (reservoir.essentia.visSize() != 0) throw helper.assertionException("a essência veio do reservatório");
        });
    }

    @GameTest
    public void aMnemonicMatrixFacingItGivesTwoMoreRecipes(GameTestHelper helper) {
        ThaumatoriumBlockEntity tile = form(helper);
        helper.setBlock(BOTTOM.west(2), Blocks.STONE);
        helper.setBlock(BOTTOM.west(), TCBlocks.MNEMONIC_MATRIX.defaultBlockState().setValue(MnemonicMatrixBlock.FACING, Direction.EAST));
        tile.getUpgrades();
        if (tile.maxRecipes != 3) helper.fail("uma matriz virada para ele: três receitas, deu " + tile.maxRecipes);
        helper.setBlock(BOTTOM.west(), TCBlocks.MNEMONIC_MATRIX.defaultBlockState().setValue(MnemonicMatrixBlock.FACING, Direction.WEST));
        tile.getUpgrades();
        if (tile.maxRecipes != 1) helper.fail("virada para o outro lado não conta");
        helper.succeed();
    }

    @GameTest
    public void theTopHalfHandsItemsDown(GameTestHelper helper) {
        ThaumatoriumBlockEntity tile = form(helper);
        helper.getBlockEntity(BOTTOM.above(), ThaumatoriumTopBlockEntity.class).setItem(0, new ItemStack(Items.COAL, 3));
        if (tile.getItem(0).getCount() != 3) helper.fail("o que entra em cima vai para a casa de baixo");
        helper.succeed();
    }

    @GameTest
    public void withoutTheCrucibleItFallsApart(GameTestHelper helper) {
        form(helper);
        helper.setBlock(BOTTOM.below(), Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(TCBlocks.ALCHEMICAL_CONSTRUCT, BOTTOM);
            helper.assertBlockPresent(TCBlocks.ALCHEMICAL_CONSTRUCT, BOTTOM.above());
        });
    }

    @GameTest
    public void theGrateLetsItemsThroughFromAHopper(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 2, 1);
        helper.setBlock(at, TCBlocks.ITEM_GRATE);
        ItemGrateBlockEntity grate = helper.getBlockEntity(at, ItemGrateBlockEntity.class);
        if (!grate.canPlaceItemThroughFace(0, new ItemStack(Items.STONE), Direction.UP)) helper.fail("aberta, aceita por cima");
        if (grate.canPlaceItemThroughFace(0, new ItemStack(Items.STONE), Direction.NORTH)) helper.fail("só por cima");
        grate.setItem(0, new ItemStack(Items.STONE, 4));
        helper.assertItemEntityCountIs(Items.STONE, at, 1.0, 4);
        // a redstone fecha
        helper.setBlock(at.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(at).getValue(ItemGrateBlock.CLOSED)) throw helper.assertionException("com sinal, fecha");
            if (grate.canPlaceItemThroughFace(0, new ItemStack(Items.STONE), Direction.UP)) throw helper.assertionException("fechada, não aceita");
        });
    }
}
