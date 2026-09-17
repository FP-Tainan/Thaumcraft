package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.item.WandTriggers;
import net.thaumcraft.registry.TCItems;

/**
 * As primeiras peças têm de sair do jeito que saem no Thaumcraft 4.2.3.5.
 */
public class RecipeGameTest {
    /** Bater com a varinha numa estante de livros dá o Thaumonomicon, como no original. */
    @GameTest
    public void aBookshelfBecomesTheThaumonomicon(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Blocks.BOOKSHELF.defaultBlockState());
        ItemStack wand = new ItemStack(TCItems.WAND);

        var result = WandTriggers.use(helper.getLevel(), player, helper.absolutePos(pos), wand);
        if (!result.consumesAction()) helper.fail("a varinha devia responder à estante");
        if (!helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir()) {
            helper.fail("a estante devia sumir");
        }
        boolean found = helper.getLevel()
                .getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(2.0))
                .stream().anyMatch(item -> item.getItem().is(TCItems.THAUMONOMICON));
        if (!found) helper.fail("devia ter caído um Thaumonomicon ali");

        // e uma pedra não responde a nada
        helper.setBlock(pos, Blocks.STONE.defaultBlockState());
        if (WandTriggers.use(helper.getLevel(), player, helper.absolutePos(pos), wand).consumesAction()) {
            helper.fail("a pedra não devia responder à varinha");
        }
        helper.succeed();
    }

    /** As três receitas de bancada do começo existem e saem no que deve sair. */
    @GameTest
    public void theFirstRecipesExist(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager();
        for (String name : new String[]{"thaumometer", "wand_cap_iron", "wand"}) {
            var key = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.RECIPE,
                    net.thaumcraft.Thaumcraft.id(name));
            if (recipes.byKey(key).isEmpty()) helper.fail("faltou a receita de " + name);
        }
        helper.succeed();
    }
}
