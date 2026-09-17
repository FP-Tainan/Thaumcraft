package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/**
 * O crisol tem de ferver e fazer contas como o do Thaumcraft 4.2.3.5.
 */
public class CrucibleGameTest {
    /** Ele só ferve com fogo por baixo, água dentro e passando de cento e cinquenta graus. */
    @GameTest
    public void itOnlyBoilsWithFireAndWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        if (crucible.boiling()) helper.fail("sem água e sem fogo não devia ferver");

        crucible.setWater(true);
        if (crucible.boiling()) helper.fail("com água mas sem fogo ainda não ferve");
        if (CrucibleBlockEntity.BOILING != 150) helper.fail("o ponto de fervura do original é cento e cinquenta");
        if (CrucibleBlockEntity.MAX_HEAT != 200) helper.fail("ele chega a duzentos, como no original");
        helper.succeed();
    }

    /** As receitas de crisol vieram do original e cobram o que têm de cobrar. */
    @GameTest
    public void recipesCameFromTheOriginal(GameTestHelper helper) {
        if (CrucibleRecipes.ALL.size() < 15) {
            helper.fail("só " + CrucibleRecipes.ALL.size() + " receitas de crisol; deviam ser mais");
        }
        // o fragmento equilibrado sai de dois de cada um dos outros cinco primários
        AspectList rich = new AspectList();
        for (var aspect : Aspects.primals()) rich.add(aspect, 2);
        CrucibleRecipe balanced = CrucibleRecipes.find(rich, new ItemStack(TCItems.SHARDS.get("air")));
        if (balanced == null) helper.fail("faltou a receita do fragmento equilibrado");
        if (!balanced.result().is(TCItems.SHARD_BALANCED)) helper.fail("ela devia dar um fragmento equilibrado");

        // e o que sobra na água é o que não foi cobrado
        AspectList left = balanced.removeFrom(rich);
        if (left.getAmount(Aspects.AIR) != 2) helper.fail("o ar entrou como catalisador, não como custo");
        if (left.getAmount(Aspects.FIRE) != 0) helper.fail("o fogo devia ter sido todo cobrado");

        // sem o bastante na água, a receita não fecha
        if (CrucibleRecipes.find(new AspectList().add(Aspects.FIRE, 1),
                new ItemStack(TCItems.SHARDS.get("air"))) != null) {
            helper.fail("com água quase vazia nada devia fechar");
        }
        helper.succeed();
    }

    /** Quem não pesquisou não fabrica: a regra do original vale no crisol e na bancada. */
    @GameTest
    public void recipesNeedTheirResearch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        // uma receita de crisol que pede pesquisa de verdade
        CrucibleRecipe gated = null;
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (net.thaumcraft.research.Researches.get(recipe.research()) != null) {
                gated = recipe;
                break;
            }
        }
        if (gated == null) helper.fail("nenhuma receita de crisol pede pesquisa que exista");

        if (net.thaumcraft.research.ResearchManager.knows(player, gated.research())) {
            helper.fail("quem não pesquisou não devia poder usar " + gated.research());
        }
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.completeResearch(gated.research());
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        if (!net.thaumcraft.research.ResearchManager.knows(player, gated.research())) {
            helper.fail("depois de pesquisar devia poder");
        }
        // receita sem pesquisa marcada passa livre, como as que o mod deixa abertas
        if (!net.thaumcraft.research.ResearchManager.knows(player, "")) {
            helper.fail("receita sem pesquisa marcada devia passar livre");
        }
        helper.succeed();
    }

    /** O frasco tira do crisol fervendo o aspecto mais abundante. */
    @GameTest
    public void aPhialTakesTheThickestAspect(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.LAVA.defaultBlockState());
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        crucible.setWater(true);
        for (int i = 0; i <= CrucibleBlockEntity.MAX_HEAT; i++) {
            CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockState(pos), crucible);
        }
        crucible.aspects().add(Aspects.FIRE, 12);
        crucible.aspects().add(Aspects.EARTH, 3);

        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack phial = new ItemStack(TCItems.PHIAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, phial);
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(pos)),
                net.minecraft.core.Direction.UP, helper.absolutePos(pos), false);
        var context = new net.minecraft.world.item.context.UseOnContext(
                helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND, phial, hit);
        TCItems.PHIAL.useOn(context);

        if (crucible.aspects().getAmount(Aspects.FIRE) != 4) {
            helper.fail("o frasco devia ter levado oito de fogo, sobrou "
                    + crucible.aspects().getAmount(Aspects.FIRE));
        }
        boolean found = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack in = player.getInventory().getItem(slot);
            if (in.is(TCItems.PHIAL) && net.thaumcraft.item.PhialItem.aspectOf(in) == Aspects.FIRE) found = true;
        }
        if (!found) helper.fail("devia ter aparecido um frasco de fogo no inventário");
        helper.succeed();
    }

    /** O que se joga dentro, fervendo, se desfaz nos aspectos que tem. */
    @GameTest
    public void whatFallsInDissolves(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.LAVA.defaultBlockState());
        helper.setBlock(pos, TCBlocks.CRUCIBLE.defaultBlockState());
        CrucibleBlockEntity crucible = helper.getBlockEntity(pos, CrucibleBlockEntity.class);
        crucible.setWater(true);

        // esquenta até passar do ponto
        for (int i = 0; i <= CrucibleBlockEntity.MAX_HEAT; i++) {
            CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockState(pos), crucible);
        }
        if (!crucible.boiling()) helper.fail("com lava embaixo e água dentro ele devia ferver");

        var where = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 3.0, 1.5));
        ItemEntity thrown = new ItemEntity(helper.getLevel(), where.x, where.y, where.z,
                new ItemStack(Items.STONE));
        helper.getLevel().addFreshEntity(thrown);
        CrucibleBlockEntity.tick(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), crucible);

        if (crucible.aspects().getAmount(Aspects.EARTH) <= 0) {
            helper.fail("a pedra devia ter virado terra dentro da água");
        }
        helper.succeed();
    }
}
