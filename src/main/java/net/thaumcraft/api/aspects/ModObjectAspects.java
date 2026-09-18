package net.thaumcraft.api.aspects;

import net.minecraft.world.item.Item;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.function.BiConsumer;

/**
 * De que são feitas as coisas do próprio mod.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/aspectos-mod.js} a partir do {@code ConfigAspects} do
 * original — a mesma fonte da tabela das coisas do jogo, na parte que fala dos fragmentos, da pedra infundida,
 * das plantas e das toras mágicas, da matéria-prima. Vale a primeira anotação de cada item.
 *
 * <p>Ainda sem item por aqui (59): ConfigItems.itemNugget, 1, 17; ConfigItems.itemNugget, 1, 18; ConfigItems.itemNugget, 1, 19; ConfigItems.itemNugget, 1, 20; ConfigItems.itemNugget, 1, 5; ConfigItems.itemNugget, 1, 16; ConfigItems.itemNugget, 1, 31; ConfigItems.itemNugget, 1, 21; ConfigItems.itemNuggetBeef, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemNuggetChicken, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemNuggetPork, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemNuggetFish, 1, OreDictionary.WILDCARD_VALUE; ConfigBlocks.blockCustomOre, 1, 0; ConfigBlocks.blockCustomOre, 1, 7; ConfigBlocks.blockCosmeticSolid; ConfigBlocks.blockCosmeticSolid, 1, 7; ConfigBlocks.blockMetalDevice; ConfigBlocks.blockCandle; ConfigBlocks.blockAiry, 1, 2; ConfigBlocks.blockAiry, 1, 3; ConfigBlocks.blockArcaneFurnace, 1, OreDictionary.WILDCARD_VALUE; ConfigBlocks.blockCustomPlant, 1, 5; ConfigItems.itemEssence, 1, 1; ConfigItems.itemWispEssence, 1, 0; ConfigItems.itemCrystalEssence, 1, 0; ConfigItems.itemResource, 1, 11; ConfigItems.itemResource, 1, 12; ConfigItems.itemZombieBrain; ConfigItems.itemLootBag, 1, 0; ConfigItems.itemLootBag, 1, 1; ConfigItems.itemLootBag, 1, 2; ConfigItems.itemBaubleBlanks, 1, 0; ConfigItems.itemBaubleBlanks, 1, 1; ConfigItems.itemBaubleBlanks, 1, 2; ConfigItems.itemBaubleBlanks, 1, 3; ConfigBlocks.blockCosmeticSolid, 1, 11; ConfigBlocks.blockCosmeticSolid, 1, 12; ConfigItems.focusPech; ConfigItems.itemHelmetCultistPlate, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemCultistPlate, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemLegsCultistPlate, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemHelmetCultistRobe, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemCultistRobe, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemLegsCultistRobe, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemHelmetCultistLeader, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemCultistLeader, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemLegsCultistLeader, 1, OreDictionary.WILDCARD_VALUE; ConfigItems.itemCultistBoots, 1, OreDictionary.WILDCARD_VALUE; ConfigBlocks.blockWoodenDevice, 1, 8; ConfigItems.itemEldritchObject, 1, 0; ConfigItems.itemEldritchObject, 1, 1; ConfigItems.itemEldritchObject, 1, 2; ConfigItems.itemEldritchObject, 1, 3; ConfigBlocks.blockEldritch, 1, OreDictionary.WILDCARD_VALUE; ConfigBlocks.blockEldritchPortal; ConfigBlocks.blockEldritch, 1, 3; ConfigBlocks.blockEldritch, 1, 4; ConfigBlocks.blockEldritch, 1, 5; ConfigBlocks.blockEldritch, 1, 6.
 */
public final class ModObjectAspects {
    private ModObjectAspects() {
    }

    public static void register(BiConsumer<Item, AspectList> put) {
        put.accept(TCItems.SHARDS.get("air"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.AIR, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARDS.get("fire"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.FIRE, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARDS.get("water"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.WATER, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARDS.get("earth"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.EARTH, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARDS.get("order"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ORDER, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARDS.get("entropy"), new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ENTROPY, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCItems.SHARD_BALANCED, new AspectList().add(Aspects.AIR, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2).add(Aspects.EARTH, 2).add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2).add(Aspects.CRYSTAL, 1));
        put.accept(TCResources.get("thaumium_nugget"), new AspectList().add(Aspects.METAL, 1));
        put.accept(TCBlocks.INFUSED_STONE.get("air").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.AIR, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.INFUSED_STONE.get("fire").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.FIRE, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.INFUSED_STONE.get("water").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.WATER, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.INFUSED_STONE.get("earth").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.EARTH, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.INFUSED_STONE.get("order").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.ORDER, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.INFUSED_STONE.get("entropy").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.ENTROPY, 3).add(Aspects.CRYSTAL, 2));
        put.accept(TCBlocks.TAINT_CRUST.asItem(), new AspectList().add(Aspects.TREE, 1).add(Aspects.TAINT, 3));
        put.accept(TCBlocks.TAINT_SOIL.asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.TAINT, 3));
        put.accept(TCBlocks.TAINT_FIBRES.asItem(), new AspectList().add(Aspects.LIFE, 1).add(Aspects.TAINT, 2));
        put.accept(TCBlocks.GREATWOOD_LOG.asItem(), new AspectList().add(Aspects.TREE, 3).add(Aspects.MAGIC, 1));
        put.accept(TCBlocks.SILVERWOOD_LOG.asItem(), new AspectList().add(Aspects.TREE, 3).add(Aspects.MAGIC, 1).add(Aspects.ORDER, 1));
        put.accept(TCBlocks.GREATWOOD_LEAVES.asItem(), new AspectList().add(Aspects.PLANT, 1));
        put.accept(TCBlocks.SILVERWOOD_LEAVES.asItem(), new AspectList().add(Aspects.PLANT, 1));
        put.accept(TCBlocks.BUILDING.get("arcane_stone").asItem(), new AspectList().add(Aspects.EARTH, 1).add(Aspects.MAGIC, 1));
        put.accept(TCBlocks.GREATWOOD_SAPLING.asItem(), new AspectList().add(Aspects.PLANT, 2).add(Aspects.TREE, 1).add(Aspects.MAGIC, 1));
        put.accept(TCBlocks.SILVERWOOD_SAPLING.asItem(), new AspectList().add(Aspects.PLANT, 2).add(Aspects.TREE, 1).add(Aspects.MAGIC, 1));
        put.accept(TCBlocks.SHIMMERLEAF.asItem(), new AspectList().add(Aspects.PLANT, 2).add(Aspects.EXCHANGE, 2).add(Aspects.MAGIC, 2));
        put.accept(TCBlocks.CINDERPEARL.asItem(), new AspectList().add(Aspects.PLANT, 2).add(Aspects.FIRE, 2).add(Aspects.MAGIC, 2));
        put.accept(TCItems.PHIAL, new AspectList().add(Aspects.VOID, 1));
        put.accept(TCItems.THAUMONOMICON, new AspectList().add(Aspects.TREE, 2).add(Aspects.MIND, 4).add(Aspects.MAGIC, 2));
        put.accept(TCResources.get("quicksilver"), new AspectList().add(Aspects.METAL, 3).add(Aspects.POISON, 1).add(Aspects.EXCHANGE, 2));
        put.accept(TCResources.get("amber"), new AspectList().add(Aspects.TRAP, 2).add(Aspects.CRYSTAL, 2));
        put.accept(TCResources.get("knowledge_fragment"), new AspectList().add(Aspects.MIND, 8));
        put.accept(TCResources.get("gold_coin"), new AspectList().add(Aspects.GREED, 1));
        put.accept(TCBlocks.TABLE.asItem(), new AspectList().add(Aspects.TREE, 4).add(Aspects.CRAFT, 2));
        put.accept(TCItems.SCRIBING_TOOLS, new AspectList().add(Aspects.WATER, 1).add(Aspects.DARKNESS, 1).add(Aspects.TOOL, 1));
        put.accept(TCItems.THAUMOMETER, new AspectList().add(Aspects.SENSES, 3).add(Aspects.METAL, 2).add(Aspects.CRYSTAL, 1).add(Aspects.MAGIC, 1));
        put.accept(TCBlocks.BUILDING.get("thaumium_block").asItem(), new AspectList().add(Aspects.METAL, 8).add(Aspects.MAGIC, 2));
        put.accept(TCBlocks.BUILDING.get("tallow_block").asItem(), new AspectList().add(Aspects.FLESH, 4).add(Aspects.LIGHT, 1).add(Aspects.MAGIC, 1));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_helmet")), new AspectList().add(Aspects.METAL, 10).add(Aspects.ARMOR, 6).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_chestplate")), new AspectList().add(Aspects.METAL, 14).add(Aspects.ARMOR, 8).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_leggings")), new AspectList().add(Aspects.METAL, 12).add(Aspects.ARMOR, 7).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_boots")), new AspectList().add(Aspects.METAL, 8).add(Aspects.ARMOR, 5).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_sword")), new AspectList().add(Aspects.METAL, 8).add(Aspects.WEAPON, 5).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_pickaxe")), new AspectList().add(Aspects.METAL, 8).add(Aspects.TOOL, 5).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_axe")), new AspectList().add(Aspects.METAL, 8).add(Aspects.TOOL, 5).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_shovel")), new AspectList().add(Aspects.METAL, 6).add(Aspects.TOOL, 4).add(Aspects.MAGIC, 2));
        put.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id("thaumium_hoe")), new AspectList().add(Aspects.METAL, 6).add(Aspects.TOOL, 4).add(Aspects.MAGIC, 2));
    }
}
