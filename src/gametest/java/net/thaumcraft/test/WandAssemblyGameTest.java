package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.crafting.ArcaneWandRecipe;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;

import java.util.ArrayList;
import java.util.List;

/**
 * A montagem de varinhas na bancada arcana tem de seguir o {@code ArcaneWandRecipe} da 4.2.3.5.
 */
public class WandAssemblyGameTest {
    private static List<ItemStack> grid(ItemStack cap, ItemStack rod) {
        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < 9; i++) grid.add(ItemStack.EMPTY);
        grid.set(2, cap.copy());
        grid.set(6, cap.copy());
        grid.set(4, rod.copy());
        return grid;
    }

    private static void learn(net.minecraft.world.entity.player.Player player, String... keys) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        for (String key : keys) knowledge.completeResearch(key);
        Knowledges.save(player, knowledge);
    }

    /**
     * O {@code ArcaneSceptreRecipe}: três pontas em volta do canto de cima à direita, o amuleto primordial no canto e a
     * haste no meio dão o cetro — uma vez e meia o vis, um décimo a menos de gasto, e o custo uma vez e meia o da varinha.
     */
    @GameTest
    public void sceptreFollowsTheOriginal(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack gold = new ItemStack(TCItems.WAND_CAPS.get("gold"));
        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < 9; i++) grid.add(ItemStack.EMPTY);
        grid.set(1, gold.copy());
        grid.set(5, gold.copy());
        grid.set(6, gold.copy());
        grid.set(2, new ItemStack(net.thaumcraft.registry.TCResources.get("primal_charm")));
        grid.set(4, new ItemStack(TCItems.WAND_RODS.get("greatwood")));
        learn(player, "CAP_gold", "ROD_greatwood");
        if (ArcaneWandRecipe.find(grid, player) != null) helper.fail("sem a pesquisa do cetro não monta");
        learn(player, "SCEPTRE");
        var recipe = ArcaneWandRecipe.find(grid, player);
        if (recipe == null) helper.fail("com as pesquisas devia montar o cetro");
        ItemStack out = recipe.result();
        if (!WandItem.isSceptre(out)) helper.fail("devia sair um cetro");
        int each = (int) (WandParts.cap("gold").craftCost() * WandParts.rod("greatwood").craftCost() * 1.5f);
        for (var primal : Aspects.primals()) {
            if (recipe.cost().getAmount(primal) != each) helper.fail("o cetro custa uma vez e meia o da varinha");
        }
        ItemStack wand = WandItem.bookStack("gold", "greatwood", false);
        if (WandItem.maxVis(out) != WandItem.maxVis(wand) * 3 / 2) helper.fail("o cetro comporta uma vez e meia");
        float diff = WandItem.modifier(wand, null, Aspects.AIR) - WandItem.modifier(out, null, Aspects.AIR);
        if (Math.abs(diff - 0.1f) > 1e-4) helper.fail("o cetro gasta um décimo a menos, gastou " + diff);
        helper.succeed();
    }

    @GameTest
    public void capsAndRodBecomeAWand(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack gold = new ItemStack(TCItems.WAND_CAPS.get("gold"));
        ItemStack greatwood = new ItemStack(TCItems.WAND_RODS.get("greatwood"));

        // sem as pesquisas, nada
        if (ArcaneWandRecipe.find(grid(gold, greatwood), player) != null) helper.fail("sem pesquisa não monta");
        learn(player, "CAP_gold", "ROD_greatwood");
        var recipe = ArcaneWandRecipe.find(grid(gold, greatwood), player);
        if (recipe == null) helper.fail("com as pesquisas devia montar");
        ItemStack out = recipe.result();
        if (!out.is(TCItems.WAND)) helper.fail("devia sair uma varinha");
        if (!WandItem.rodTag(out).equals("greatwood") || !WandItem.capTag(out).equals("gold")) {
            helper.fail("a varinha devia levar a haste e as pontas da grade");
        }
        int each = WandParts.cap("gold").craftCost() * WandParts.rod("greatwood").craftCost();
        for (var primal : Aspects.primals()) {
            if (recipe.cost().getAmount(primal) != each) helper.fail("o custo é ponta vezes haste em cada primário");
        }

        // pontas diferentes, ou algo sobrando na grade, não montam
        List<ItemStack> mixed = grid(gold, greatwood);
        mixed.set(6, new ItemStack(TCItems.WAND_CAPS.get("iron")));
        if (ArcaneWandRecipe.find(mixed, player) != null) helper.fail("pontas diferentes não montam");
        List<ItemStack> extra = grid(gold, greatwood);
        extra.set(0, new ItemStack(Items.DIRT));
        if (ArcaneWandRecipe.find(extra, player) != null) helper.fail("sobra na grade não monta");

        // graveto com ferro é a receita comum, não esta
        learn(player, "CAP_iron", "ROD_wood");
        if (ArcaneWandRecipe.find(grid(new ItemStack(TCItems.WAND_CAPS.get("iron")), new ItemStack(Items.STICK)),
                player) != null) {
            helper.fail("graveto com ferro não sai da bancada arcana");
        }
        // mas graveto com ouro sai
        if (ArcaneWandRecipe.find(grid(gold, new ItemStack(Items.STICK)), player) == null) {
            helper.fail("graveto com ouro devia sair");
        }
        helper.succeed();
    }

    @GameTest
    public void aStaffCoreBecomesAStaff(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack gold = new ItemStack(TCItems.WAND_CAPS.get("gold"));
        ItemStack core = new ItemStack(TCItems.STAFF_RODS.get("greatwood"));
        learn(player, "CAP_gold", "ROD_greatwood");
        if (ArcaneWandRecipe.find(grid(gold, core), player) != null) helper.fail("o bastão pede a pesquisa própria");
        learn(player, "ROD_greatwood_staff");
        var recipe = ArcaneWandRecipe.find(grid(gold, core), player);
        if (recipe == null || !recipe.result().is(TCItems.STAFF)) helper.fail("devia sair um bastão");
        int each = WandParts.cap("gold").craftCost() * WandParts.rod("greatwood_staff").craftCost();
        if (recipe.cost().getAmount(Aspects.FIRE) != each) helper.fail("o custo do bastão usa o núcleo de bastão");
        helper.succeed();
    }
}
