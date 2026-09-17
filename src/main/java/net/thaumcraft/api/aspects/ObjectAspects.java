package net.thaumcraft.api.aspects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * De que cada coisa do jogo é feita.
 *
 * <p>A tabela é a do Thaumcraft 4.2.3.5, gerada a partir do ConfigAspects do próprio mod. É ela que o
 * thaumômetro lê quando você aponta para alguma coisa, e é dela que saem os pontos de pesquisa.
 */
public final class ObjectAspects {
    private static final Map<Item, AspectList> TABLE = new HashMap<>();
    /** O que o mod antigo anotava e este Minecraft não tem mais; fica registrado para não sumir calado. */
    private static final List<String> MISSING = new ArrayList<>();

    private ObjectAspects() {
    }

    /** O que aquela coisa tem dentro, ou uma lista vazia se ninguém anotou. */
    public static AspectList of(ItemStack stack) {
        return of(stack.getItem());
    }

    public static AspectList of(Item item) {
        AspectList found = TABLE.get(item);
        return found == null ? new AspectList() : found.copy();
    }

    public static int size() {
        return TABLE.size();
    }

    public static List<String> missing() {
        return MISSING;
    }

    /** Blocos que não viram item, como a água e a lava: o thaumômetro lê o bloco direto. */
    private static final Map<net.minecraft.world.level.block.Block, AspectList> BLOCKS = new HashMap<>();

    public static AspectList ofBlock(net.minecraft.world.level.block.Block block) {
        AspectList found = BLOCKS.get(block);
        if (found != null) return found.copy();
        return of(block.asItem());
    }

    private static void add(String id, AspectList aspects) {
        Identifier key = Identifier.withDefaultNamespace(id);
        Item item = BuiltInRegistries.ITEM.getOptional(key).orElse(null);
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            // no original a água parada e a corrente são blocos diferentes, e hoje são o mesmo:
            // vale a primeira anotação, que é a da parada, com o valor cheio
            TABLE.putIfAbsent(item, aspects);
            return;
        }
        // sem item, mas talvez seja um bloco que não se carrega na mão
        var block = BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
        if (block != null) BLOCKS.putIfAbsent(block, aspects);
        else MISSING.add(id);
    }

    public static void init() {
        TABLE.clear();
        BLOCKS.clear();
        MISSING.clear();
        add("stone", new AspectList().add(Aspects.EARTH, 2));
        add("cobblestone", new AspectList().add(Aspects.EARTH, 2));
        add("grass_block", new AspectList().add(Aspects.EARTH, 1).add(Aspects.PLANT, 1));
        add("dirt", new AspectList().add(Aspects.EARTH, 2));
        add("sand", new AspectList().add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        add("gravel", new AspectList().add(Aspects.EARTH, 2));
        add("granite", new AspectList().add(Aspects.EARTH, 3).add(Aspects.CRYSTAL, 1));
        add("diorite", new AspectList().add(Aspects.EARTH, 3).add(Aspects.CRYSTAL, 1));
        add("andesite", new AspectList().add(Aspects.EARTH, 3));
        add("oak_log", new AspectList().add(Aspects.TREE, 4));
        add("birch_log", new AspectList().add(Aspects.TREE, 4));
        add("oak_planks", new AspectList().add(Aspects.TREE, 2));
        add("oak_leaves", new AspectList().add(Aspects.PLANT, 2).add(Aspects.AIR, 1));
        add("birch_leaves", new AspectList().add(Aspects.PLANT, 2).add(Aspects.AIR, 1));
        add("oak_sapling", new AspectList().add(Aspects.PLANT, 2).add(Aspects.TREE, 1));
        add("iron_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.METAL, 3));
        add("gold_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.METAL, 5));
        add("diamond_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.CRYSTAL, 6));
        add("redstone_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.ENERGY, 3));
        add("coal_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.ENERGY, 2));
        add("lapis_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.CRYSTAL, 3));
        add("emerald_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.CRYSTAL, 5));
        add("nether_quartz_ore", new AspectList().add(Aspects.EARTH, 2).add(Aspects.CRYSTAL, 3));
        add("obsidian", new AspectList().add(Aspects.EARTH, 4).add(Aspects.FIRE, 2).add(Aspects.DARKNESS, 2));
        add("glowstone", new AspectList().add(Aspects.LIGHT, 4).add(Aspects.CRYSTAL, 2));
        add("ice", new AspectList().add(Aspects.WATER, 2).add(Aspects.COLD, 3));
        add("snow_block", new AspectList().add(Aspects.WATER, 1).add(Aspects.COLD, 2));
        add("clay", new AspectList().add(Aspects.EARTH, 1).add(Aspects.WATER, 1));
        add("water", new AspectList().add(Aspects.WATER, 4));
        add("water", new AspectList().add(Aspects.WATER, 2));
        add("lava", new AspectList().add(Aspects.FIRE, 3).add(Aspects.EARTH, 1));
        add("lava", new AspectList().add(Aspects.FIRE, 2));
        add("white_wool", new AspectList().add(Aspects.PLANT, 1).add(Aspects.TOOL, 1));
        add("cactus", new AspectList().add(Aspects.PLANT, 2).add(Aspects.WATER, 1));
        add("pumpkin", new AspectList().add(Aspects.PLANT, 2).add(Aspects.LIFE, 1));
        add("melon", new AspectList().add(Aspects.PLANT, 2).add(Aspects.LIFE, 1));
        add("vine", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MOTION, 1));
        add("lily_pad", new AspectList().add(Aspects.PLANT, 2).add(Aspects.WATER, 1));
        add("netherrack", new AspectList().add(Aspects.EARTH, 1).add(Aspects.FIRE, 2));
        add("soul_sand", new AspectList().add(Aspects.EARTH, 1).add(Aspects.SOUL, 2).add(Aspects.DEATH, 1));
        add("dragon_egg", new AspectList().add(Aspects.ELDRITCH, 8).add(Aspects.BEAST, 8).add(Aspects.MAGIC, 8));
        add("iron_pickaxe", new AspectList().add(Aspects.TOOL, 3).add(Aspects.METAL, 3));
        add("iron_axe", new AspectList().add(Aspects.TOOL, 3).add(Aspects.METAL, 3));
        add("iron_shovel", new AspectList().add(Aspects.TOOL, 2).add(Aspects.METAL, 2));
        add("iron_sword", new AspectList().add(Aspects.TOOL, 3).add(Aspects.WEAPON, 3).add(Aspects.METAL, 3));
        add("iron_hoe", new AspectList().add(Aspects.TOOL, 2).add(Aspects.METAL, 2).add(Aspects.HARVEST, 1));
        add("diamond_pickaxe", new AspectList().add(Aspects.TOOL, 4).add(Aspects.CRYSTAL, 5));
        add("diamond_sword", new AspectList().add(Aspects.TOOL, 4).add(Aspects.WEAPON, 4).add(Aspects.CRYSTAL, 4));
        add("diamond_axe", new AspectList().add(Aspects.TOOL, 4).add(Aspects.CRYSTAL, 4));
        add("diamond_shovel", new AspectList().add(Aspects.TOOL, 3).add(Aspects.CRYSTAL, 3));
        add("diamond_hoe", new AspectList().add(Aspects.TOOL, 3).add(Aspects.CRYSTAL, 3).add(Aspects.HARVEST, 2));
        add("golden_pickaxe", new AspectList().add(Aspects.TOOL, 2).add(Aspects.METAL, 4));
        add("golden_sword", new AspectList().add(Aspects.TOOL, 2).add(Aspects.WEAPON, 2).add(Aspects.METAL, 4));
        add("golden_axe", new AspectList().add(Aspects.TOOL, 2).add(Aspects.METAL, 4));
        add("golden_shovel", new AspectList().add(Aspects.TOOL, 1).add(Aspects.METAL, 4));
        add("golden_hoe", new AspectList().add(Aspects.TOOL, 1).add(Aspects.METAL, 4).add(Aspects.HARVEST, 1));
        add("stone_pickaxe", new AspectList().add(Aspects.TOOL, 2).add(Aspects.EARTH, 3));
        add("stone_sword", new AspectList().add(Aspects.TOOL, 2).add(Aspects.WEAPON, 2).add(Aspects.EARTH, 3));
        add("stone_axe", new AspectList().add(Aspects.TOOL, 2).add(Aspects.EARTH, 3));
        add("stone_shovel", new AspectList().add(Aspects.TOOL, 1).add(Aspects.EARTH, 2));
        add("stone_hoe", new AspectList().add(Aspects.TOOL, 1).add(Aspects.EARTH, 2).add(Aspects.HARVEST, 1));
        add("wooden_pickaxe", new AspectList().add(Aspects.TOOL, 1).add(Aspects.TREE, 3));
        add("wooden_sword", new AspectList().add(Aspects.TOOL, 1).add(Aspects.WEAPON, 1).add(Aspects.TREE, 3));
        add("wooden_axe", new AspectList().add(Aspects.TOOL, 1).add(Aspects.TREE, 3));
        add("wooden_shovel", new AspectList().add(Aspects.TOOL, 1).add(Aspects.TREE, 2));
        add("wooden_hoe", new AspectList().add(Aspects.TOOL, 1).add(Aspects.TREE, 2).add(Aspects.HARVEST, 1));
        add("apple", new AspectList().add(Aspects.PLANT, 2).add(Aspects.LIFE, 1));
        add("bread", new AspectList().add(Aspects.PLANT, 2).add(Aspects.LIFE, 2));
        add("cooked_beef", new AspectList().add(Aspects.BEAST, 2).add(Aspects.LIFE, 3));
        add("cooked_porkchop", new AspectList().add(Aspects.BEAST, 2).add(Aspects.LIFE, 3));
        add("cooked_chicken", new AspectList().add(Aspects.BEAST, 2).add(Aspects.LIFE, 2).add(Aspects.FLIGHT, 1));
        add("cooked_cod", new AspectList().add(Aspects.BEAST, 2).add(Aspects.LIFE, 2).add(Aspects.WATER, 1));
        add("iron_ingot", new AspectList().add(Aspects.METAL, 4));
        add("gold_ingot", new AspectList().add(Aspects.METAL, 3).add(Aspects.GREED, 2));
        add("gold_nugget", new AspectList().add(Aspects.METAL, 1));
        add("diamond", new AspectList().add(Aspects.CRYSTAL, 8));
        add("emerald", new AspectList().add(Aspects.CRYSTAL, 6).add(Aspects.EXCHANGE, 2));
        add("redstone", new AspectList().add(Aspects.ENERGY, 3));
        add("coal", new AspectList().add(Aspects.ENERGY, 2));
        add("stick", new AspectList().add(Aspects.TREE, 1));
        add("string", new AspectList().add(Aspects.BEAST, 1).add(Aspects.TOOL, 1));
        add("feather", new AspectList().add(Aspects.BEAST, 1).add(Aspects.FLIGHT, 2));
        add("leather", new AspectList().add(Aspects.BEAST, 2).add(Aspects.FLESH, 1));
        add("bone", new AspectList().add(Aspects.UNDEAD, 2).add(Aspects.EARTH, 1));
        add("gunpowder", new AspectList().add(Aspects.FIRE, 2).add(Aspects.ENERGY, 2).add(Aspects.ENTROPY, 1));
        add("ender_pearl", new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.MAGIC, 2).add(Aspects.TRAVEL, 4));
        add("rotten_flesh", new AspectList().add(Aspects.UNDEAD, 2).add(Aspects.FLESH, 2).add(Aspects.DEATH, 1));
        add("spider_eye", new AspectList().add(Aspects.BEAST, 2).add(Aspects.POISON, 3).add(Aspects.SENSES, 1));
        add("blaze_rod", new AspectList().add(Aspects.FIRE, 4).add(Aspects.ENERGY, 2).add(Aspects.MAGIC, 1));
        add("ghast_tear", new AspectList().add(Aspects.SOUL, 4).add(Aspects.FIRE, 2).add(Aspects.SENSES, 1));
        add("magma_cream", new AspectList().add(Aspects.FIRE, 3).add(Aspects.SLIME, 2));
        add("slime_ball", new AspectList().add(Aspects.SLIME, 3).add(Aspects.LIFE, 1));
        add("glass_bottle", new AspectList().add(Aspects.CRYSTAL, 1).add(Aspects.VOID, 1));
        add("nether_wart", new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAGIC, 2).add(Aspects.FIRE, 1));
        add("golden_carrot", new AspectList().add(Aspects.PLANT, 2).add(Aspects.METAL, 4).add(Aspects.SENSES, 2));
        add("glistering_melon_slice", new AspectList().add(Aspects.PLANT, 2).add(Aspects.METAL, 4).add(Aspects.HEAL, 2));
        add("book", new AspectList().add(Aspects.TREE, 2).add(Aspects.MIND, 2));
        add("paper", new AspectList().add(Aspects.TREE, 1).add(Aspects.MIND, 1));
        add("flint", new AspectList().add(Aspects.EARTH, 1).add(Aspects.TOOL, 1));
        add("bucket", new AspectList().add(Aspects.METAL, 8).add(Aspects.VOID, 1));
        add("music_disc_far", new AspectList().add(Aspects.SENSES, 4).add(Aspects.AIR, 4).add(Aspects.ELDRITCH, 4).add(Aspects.GREED, 4));
        add("nether_star", new AspectList().add(Aspects.ELDRITCH, 8).add(Aspects.MAGIC, 8).add(Aspects.ORDER, 8).add(Aspects.LIGHT, 8));
        add("crafting_table", new AspectList().add(Aspects.CRAFT, 4));
    }
}
