package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.crafting.InfusionEnchantmentRecipe;
import net.thaumcraft.crafting.InfusionEnchantments;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEnchantments;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.List;

/** Os encantamentos do Thaumcraft (Pressa e Reparo) e a infusão de encantamento. */
public class EnchantmentGameTest {
    @GameTest
    public void theEnchantmentsAreTheOriginals(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var haste = registry.getOrThrow(TCEnchantments.HASTE);
        var repair = registry.getOrThrow(TCEnchantments.REPAIR);
        if (haste.value().getMaxLevel() != 3 || repair.value().getMaxLevel() != 2) helper.fail("Pressa vai a 3, Reparo a 2");
        if (haste.value().getMinCost(1) != 15 || repair.value().getMinCost(2) != 30) helper.fail("os custos do original");
        if (!haste.value().canEnchant(new ItemStack(TCItems.TRAVELLER_BOOTS))) helper.fail("Pressa vai nas botas");
        if (!repair.value().canEnchant(new ItemStack(TCItems.GEAR.get("thaumium_pickaxe")))) helper.fail("Reparo vai nas coisas do Thaumcraft");
        if (repair.value().canEnchant(new ItemStack(Items.IRON_PICKAXE))) helper.fail("e não nas do jogo");
        if (net.minecraft.world.item.enchantment.Enchantment.areCompatible(repair, registry.getOrThrow(Enchantments.UNBREAKING))) {
            helper.fail("Reparo não convive com Inquebrável");
        }
        if (InfusionEnchantments.ALL.size() != 24) helper.fail("o original tem 24 infusões de encantamento");
        helper.succeed();
    }

    @GameTest
    public void repairSpendsVisFromTheWand(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack pick = new ItemStack(TCItems.GEAR.get("thaumium_pickaxe"));
        pick.enchant(registry.getOrThrow(TCEnchantments.REPAIR), 2);
        pick.setDamageValue(10);
        ItemStack wand = new ItemStack(TCItems.WAND);
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        player.getInventory().setItem(0, pick);
        player.getInventory().setItem(1, wand);
        net.thaumcraft.event.Enchantments.doRepair(pick, player);
        if (pick.getDamageValue() != 8) helper.fail("Reparo II conserta dois pontos, está em " + pick.getDamageValue());
        if (WandItem.vis(wand).visSize() >= 2500 * 6) helper.fail("pagando com a varinha");
        helper.succeed();
    }

    @GameTest
    public void infusionRaisesTheLevel(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        knowledge.completeResearch("INFUSIONENCHANTMENT");
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        List<ItemStack> parts = List.of(new ItemStack(Items.IRON_SWORD), new ItemStack(TCResources.get("salis_mundus")));
        InfusionEnchantmentRecipe recipe = InfusionEnchantmentRecipe.find(parts, sword, helper.getLevel(), player);
        if (recipe == null || !recipe.enchantment().equals(Enchantments.SHARPNESS)) throw helper.assertionException("espada de ferro e sal: Afiada");
        ItemStack out = recipe.resultFor(sword, helper.getLevel());
        var sharp = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
        if (EnchantmentHelper.getItemEnchantmentLevel(sharp, out) != 1) helper.fail("sobe um nível");
        if (recipe.xp(out, helper.getLevel()) <= recipe.xp(sword, helper.getLevel())) helper.fail("e o próximo cobra mais experiência");
        if (InfusionEnchantmentRecipe.find(parts, new ItemStack(Items.STICK), helper.getLevel(), player) != null) helper.fail("graveto não se encanta");
        helper.succeed();
    }
}
