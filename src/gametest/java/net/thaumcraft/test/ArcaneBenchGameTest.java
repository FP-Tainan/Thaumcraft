package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity;
import net.thaumcraft.inventory.ArcaneWorkbenchMenu;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Researches;

/**
 * A bancada arcana pelo caminho da tela: a casa do resultado tem de mostrar o que a grade monta, com a varinha pagando.
 */
public class ArcaneBenchGameTest {
    /** Uma varinha cheia de cada primário. */
    private static ItemStack fullWand(String cap, String rod) {
        ItemStack wand = WandItem.bookStack(cap, rod, false);
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, WandItem.maxVis(wand));
        WandItem.setVis(wand, vis);
        return wand;
    }

    private static ArcaneWorkbenchMenu menu(net.minecraft.world.entity.player.Player player, SimpleContainer bench) {
        return new ArcaneWorkbenchMenu(1, player.getInventory(), bench);
    }

    @GameTest
    public void theBenchShowsWhatTheGridMakes(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        PlayerKnowledge knowledge = Knowledges.of(player);
        for (var research : Researches.ALL.values()) knowledge.completeResearch(research.key());
        Knowledges.save(player, knowledge);

        // a montagem de varinha: pontas de ouro nos cantos, haste de madeira-grande no meio
        SimpleContainer bench = new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE);
        bench.setItem(2, new ItemStack(TCItems.WAND_CAPS.get("gold")));
        bench.setItem(6, new ItemStack(TCItems.WAND_CAPS.get("gold")));
        bench.setItem(4, new ItemStack(TCItems.WAND_RODS.get("greatwood")));
        bench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, fullWand("iron", "greatwood"));
        ArcaneWorkbenchMenu menu = menu(player, bench);
        if (menu.getSlot(0).getItem().isEmpty()) helper.fail("a varinha de ouro e madeira-grande devia aparecer no resultado");

        // uma receita de tabela: o taumômetro
        SimpleContainer other = new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE);
        var recipe = net.thaumcraft.crafting.ArcaneRecipes.ALL.stream()
                .filter(r -> r.shaped() && r.pattern().size() == 9 && !r.cost().isEmpty()).findFirst().orElse(null);
        if (recipe == null) helper.fail("faltou receita arcana com forma para o teste");
        for (int slot = 0; slot < 9; slot++) {
            var ingredient = recipe.pattern().get(slot);
            if (ingredient != null) other.setItem(slot, ingredient.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY));
        }
        other.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, fullWand("gold", "greatwood"));
        ArcaneWorkbenchMenu second = menu(player, other);
        if (second.getSlot(0).getItem().isEmpty()) helper.fail("a receita " + recipe.research() + " devia aparecer no resultado");

        // e tirar o resultado cobra o vis e gasta a grade
        ItemStack wand = other.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
        Aspect paid = recipe.cost().getAspects().getFirst();
        int before = WandItem.vis(wand).getAmount(paid);
        second.take();
        int after = WandItem.vis(other.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT)).getAmount(paid);
        if (after >= before) {
            helper.fail("tirar da bancada devia cobrar o vis da varinha: " + recipe.research() + " custo " + recipe.cost()
                    + ", antes " + before + ", depois " + after + ", grade agora " + other.getItem(0) + "/" + other.getItem(1));
        }
        helper.succeed();
    }

    /** A varinha de graveto e ferro é a da bancada comum, como no original: a arcana não a monta. */
    @GameTest
    public void theStarterWandIsNotMadeHere(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        PlayerKnowledge knowledge = Knowledges.of(player);
        for (var research : Researches.ALL.values()) knowledge.completeResearch(research.key());
        Knowledges.save(player, knowledge);
        SimpleContainer bench = new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE);
        bench.setItem(2, new ItemStack(TCItems.WAND_CAPS.get("iron")));
        bench.setItem(6, new ItemStack(TCItems.WAND_CAPS.get("iron")));
        bench.setItem(4, new ItemStack(Items.STICK));
        bench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, fullWand("iron", "greatwood"));
        if (!menu(player, bench).getSlot(0).getItem().isEmpty()) helper.fail("essa é a da bancada comum");
        helper.succeed();
    }
}
