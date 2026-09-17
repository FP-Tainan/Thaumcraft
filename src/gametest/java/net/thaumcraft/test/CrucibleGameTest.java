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
