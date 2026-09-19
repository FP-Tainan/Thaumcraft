package net.thaumcraft.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.List;

/**
 * As receitas de infusão de encantamento do Thaumcraft 4.2.3.5 (o {@code addInfusionEnchantmentRecipe} do
 * {@code ConfigRecipes}).
 *
 * <p>GERADO por {@code scratchpad/infusao-encantamentos.js} a partir do jar — não editar à mão.
 */
public final class InfusionEnchantments {
    public static final List<InfusionEnchantmentRecipe> ALL = new ArrayList<>();

    private InfusionEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id));
    }

    static {
        // InfEnchRepair
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("thaumcraft:repair"), 4,
                new AspectList().add(Aspects.MAGIC, 8).add(Aspects.CRAFT, 10).add(Aspects.ORDER, 10),
                List.of(Ingredient.of(net.minecraft.world.item.Items.ANVIL), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnchHaste
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("thaumcraft:haste"), 3,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.TRAVEL, 8).add(Aspects.FLIGHT, 8),
                List.of(Ingredient.of(TCItems.NITOR), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch0
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:protection"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.ARMOR, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch1
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:fire_protection"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.ARMOR, 4).add(Aspects.FIRE, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.MAGMA_CREAM), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch2
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:blast_protection"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.ARMOR, 4).add(Aspects.ENTROPY, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch3
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:projectile_protection"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.ARMOR, 4).add(Aspects.FLIGHT, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.ARROW), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch4
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:feather_falling"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.AIR, 4).add(Aspects.FLIGHT, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch5
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:respiration"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.AIR, 8).add(Aspects.WATER, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.SUGAR_CANE), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch6
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:aqua_affinity"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.MOTION, 8).add(Aspects.WATER, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.SUGAR_CANE), Ingredient.of(net.minecraft.world.item.Items.SLIME_BALL), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch7
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:thorns"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 8).add(Aspects.PLANT, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.DEAD_BUSH), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch8
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:sharpness"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch9
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:smite"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.UNDEAD, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch10
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:bane_of_arthropods"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.BEAST, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(TCResources.get("amber")), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch11
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:knockback"), 1,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 3).add(Aspects.MOTION, 3),
                List.of(Ingredient.of(net.minecraft.world.item.Items.PISTON), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch12
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:fire_aspect"), 3,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.FIRE, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(net.minecraft.world.item.Items.BLAZE_POWDER), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch13
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:looting"), 3,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.GREED, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch14
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:efficiency"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.TOOL, 4).add(Aspects.ORDER, 4),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_PICKAXE), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch15
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:silk_touch"), 5,
                new AspectList().add(Aspects.MAGIC, 16).add(Aspects.TOOL, 16).add(Aspects.ORDER, 16).add(Aspects.HARVEST, 16).add(Aspects.MINE, 16),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_PICKAXE), Ingredient.of(net.minecraft.world.item.Items.COBWEB), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch16
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:unbreaking"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.TOOL, 4).add(Aspects.ORDER, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_PICKAXE), Ingredient.of(net.minecraft.world.item.Items.OBSIDIAN), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch17
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:fortune"), 3,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.TOOL, 4).add(Aspects.GREED, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_PICKAXE), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch18
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:power"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.BOW), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch19
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:punch"), 2,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 3).add(Aspects.MOTION, 3),
                List.of(Ingredient.of(net.minecraft.world.item.Items.PISTON), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch20
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:flame"), 3,
                new AspectList().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.FIRE, 8),
                List.of(Ingredient.of(net.minecraft.world.item.Items.BOW), Ingredient.of(net.minecraft.world.item.Items.BLAZE_POWDER), Ingredient.of(TCResources.get("salis_mundus")))));
        // InfEnch21
        ALL.add(new InfusionEnchantmentRecipe("INFUSIONENCHANTMENT", key("minecraft:infinity"), 5,
                new AspectList().add(Aspects.MAGIC, 8).add(Aspects.WEAPON, 16).add(Aspects.VOID, 16).add(Aspects.EXCHANGE, 16),
                List.of(Ingredient.of(net.minecraft.world.item.Items.BOW), Ingredient.of(net.minecraft.world.item.Items.ARROW), Ingredient.of(TCResources.get("salis_mundus")))));
    }
}
