package net.thaumcraft.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * O {@code tradeInventory} do {@code EntityPech} da 4.2.3.5: o que cada tipo de pech tem para dar, com o valor de cada
 * coisa (de um a cinco). As poções eram números de 1.7 (8201 força, 8194 velocidade, 8265 força longa, 8262 visão
 * noturna longa, 8193 regeneração, 8261 cura, 8225 regeneração II, 8229 cura II, 8270 invisibilidade longa).
 *
 * <p>Ficam de fora, até chegarem: as pepitas nativas de estanho, prata e chumbo (só existiam com outro mod) e os livros
 * de Pressa e de Reparo, os encantamentos do próprio Thaumcraft.
 */
public final class PechTrades {
    /** Uma coisa da tabela: quanto vale e como fazê-la. */
    public record Trade(int value, Function<ServerLevel, ItemStack> make) {
    }

    private PechTrades() {
    }

    private static Trade item(int value, net.minecraft.world.level.ItemLike item) {
        return new Trade(value, level -> new ItemStack(item));
    }

    private static Trade resource(int value, String name) {
        return new Trade(value, level -> new ItemStack(TCResources.get(name)));
    }

    private static Trade potion(int value, Holder<Potion> potion) {
        return new Trade(value, level -> PotionContents.createItemStack(Items.POTION, potion));
    }

    private static Trade book(int value, ResourceKey<Enchantment> enchantment) {
        return new Trade(value, level -> net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                new EnchantmentInstance(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), 1)));
    }

    /** A tabela do tipo: 0 o coletor, 1 o mago, 2 o caçador. */
    public static List<Trade> of(int type) {
        List<Trade> list = new ArrayList<>();
        switch (type) {
            case PechEntity.MAGE -> {
                list.add(new Trade(1, level -> new ItemStack(TCItems.MANA_BEAN)));
                for (String shard : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) list.add(item(1, TCItems.SHARDS.get(shard)));
                list.add(resource(1, "knowledge_fragment"));
                list.add(resource(2, "knowledge_fragment"));
                list.add(potion(2, Potions.REGENERATION));
                list.add(potion(2, Potions.HEALING));
                list.add(item(3, Items.GOLDEN_APPLE));
                list.add(potion(3, Potions.STRONG_REGENERATION));
                list.add(potion(3, Potions.STRONG_HEALING));
                for (var crystal : TCBlocks.CRYSTAL_CLUSTERS.values()) list.add(item(4, crystal));
                list.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
                list.add(item(5, TCItems.FOCUS_POUCH));
                list.add(item(5, TCItems.FOCI.get("pech")));
                list.add(item(5, TCItems.VIS_STONE));
            }
            case PechEntity.STALKER -> {
                list.add(new Trade(1, level -> new ItemStack(TCItems.MANA_BEAN)));
                // as velas de 0 a 14 (a preta fica de fora, como no original)
                for (int a = 0; a < 15; a++) list.add(item(1, TCBlocks.TALLOW_CANDLES.get(TCBlocks.CANDLE_COLOURS[a])));
                list.add(item(2, Items.GHAST_TEAR));
                list.add(potion(2, Potions.SWIFTNESS));
                list.add(potion(2, Potions.STRENGTH));
                list.add(book(2, Enchantments.POWER));
                list.add(item(3, Items.EXPERIENCE_BOTTLE));
                list.add(resource(3, "knowledge_fragment"));
                list.add(potion(3, Potions.LONG_INVISIBILITY));
                list.add(potion(3, Potions.STRONG_REGENERATION));
                list.add(item(3, Items.GOLDEN_APPLE));
                list.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
                list.add(item(4, TCItems.GEAR.get("thaumium_boots")));
                list.add(item(5, TCItems.RUNIC_RING_LESSER));
                list.add(book(5, Enchantments.FLAME));
                list.add(book(5, Enchantments.INFINITY));
            }
            default -> {
                list.add(new Trade(1, level -> new ItemStack(TCItems.MANA_BEAN)));
                list.add(resource(1, "native_iron_cluster"));
                list.add(resource(1, "native_gold_cluster"));
                list.add(resource(1, "native_cinnabar_cluster"));
                list.add(resource(1, "native_copper_cluster"));
                list.add(item(2, Items.BLAZE_ROD));
                list.add(item(2, TCBlocks.GREATWOOD_SAPLING));
                list.add(potion(2, Potions.STRENGTH));
                list.add(potion(2, Potions.SWIFTNESS));
                list.add(item(3, Items.EXPERIENCE_BOTTLE));
                list.add(resource(3, "knowledge_fragment"));
                list.add(item(3, Items.GOLDEN_APPLE));
                list.add(potion(3, Potions.LONG_STRENGTH));
                list.add(potion(3, Potions.LONG_NIGHT_VISION));
                list.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
                list.add(item(4, TCItems.GEAR.get("thaumium_pickaxe")));
                list.add(item(5, TCBlocks.SILVERWOOD_SAPLING));
                list.add(item(5, TCBlocks.SILVERWOOD_SAPLING));
            }
        }
        return list;
    }

    /**
     * O que sai do baú de masmorra de 2014 quando o pech tira um tesouro grande: só as coisas raras e de uma unidade
     * (peso até cinco, uma só): maçã dourada, os dois discos, as armaduras de cavalo e o livro encantado.
     */
    public static ItemStack dungeonTreasure(ServerLevel level, RandomSource random) {
        return switch (random.nextInt(7)) {
            case 0 -> new ItemStack(Items.GOLDEN_APPLE);
            case 1 -> new ItemStack(Items.MUSIC_DISC_13);
            case 2 -> new ItemStack(Items.MUSIC_DISC_CAT);
            case 3 -> new ItemStack(Items.GOLDEN_HORSE_ARMOR);
            case 4 -> new ItemStack(Items.IRON_HORSE_ARMOR);
            case 5 -> new ItemStack(Items.DIAMOND_HORSE_ARMOR);
            default -> net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(random, new ItemStack(Items.BOOK), 30,
                    level.registryAccess(), level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .get(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE).map(set -> set));
        };
    }

}
