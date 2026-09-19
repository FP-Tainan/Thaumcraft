package net.thaumcraft.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.List;
import java.util.function.Supplier;

/**
 * O saque do Thaumcraft 4.2.3.5: as três sacolas de tesouro (o {@code addLootBagItem} do {@code Config.initLoot}),
 * as peças do {@code Utils.genGear} e o que vai nos baús do mundo (o {@code ChestGenHooks}).
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/saque.js} a partir do jar descompilado. Não mexer na mão.
 */
public final class ThaumLoot {
    /** Uma coisa com o seu peso no sorteio. */
    public record Entry(Supplier<ItemStack> stack, int weight) {
    }

    /** Uma coisa posta num baú do mundo: a tabela, de quanto a quanto, e o peso. */
    public record Chest(String table, Supplier<Item> item, int min, int max, int weight) {
    }

    /** A sacola comum: 85 coisas. */
    public static final List<Entry> COMMON = List.of(
            new Entry(() -> new ItemStack(TCResources.get("gold_coin")), 2500),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.DIAMOND), 10),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EMERALD), 15),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), 100),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENDER_PEARL), 100),
            new Entry(() -> new ItemStack(TCResources.get("knowledge_fragment")), 25),
            new Entry(() -> new ItemStack(TCItems.MUNDANE_AMULET), 10),
            new Entry(() -> new ItemStack(TCItems.MUNDANE_RING), 10),
            new Entry(() -> new ItemStack(TCItems.MUNDANE_BELT), 10),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE), 5),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE), 1),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE), 3),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.BOOK), 10),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3));

    /** A sacola incomum: 90 coisas. */
    public static final List<Entry> UNCOMMON = List.of(
            new Entry(() -> new ItemStack(TCResources.get("gold_coin"), 2), 2250),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.DIAMOND), 50),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EMERALD), 75),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), 100),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENDER_PEARL), 100),
            new Entry(() -> new ItemStack(TCResources.get("knowledge_fragment")), 25),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("air")), 5),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("earth")), 5),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("fire")), 5),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("water")), 5),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("order")), 5),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("entropy")), 5),
            new Entry(ThaumLoot::visStone, 6),
            new Entry(() -> new ItemStack(TCItems.RUNIC_RING_LESSER), 5),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE), 10),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE), 2),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE), 6),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.BOOK), 10),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3));

    /** A sacola rara: 91 coisas. */
    public static final List<Entry> RARE = List.of(
            new Entry(() -> new ItemStack(TCResources.get("gold_coin"), 3), 2000),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.NETHER_STAR), 1),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.DIAMOND), 50),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EMERALD), 75),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), 100),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENDER_PEARL), 100),
            new Entry(() -> new ItemStack(TCResources.get("knowledge_fragment")), 25),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("air")), 7),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("earth")), 7),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("fire")), 7),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("water")), 7),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("order")), 7),
            new Entry(() -> new ItemStack(TCItems.APPRENTICE_RINGS.get("entropy")), 7),
            new Entry(ThaumLoot::visStone, 6),
            new Entry(() -> new ItemStack(TCItems.RUNIC_RING_LESSER), 5),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE), 20),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE), 3),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE), 9),
            new Entry(() -> new ItemStack(net.minecraft.world.item.Items.BOOK), 10),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_REGENERATION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SWIFTNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.POISON), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_POISON), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_POISON), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HEALING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HEALING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.NIGHT_VISION), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WEAKNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRENGTH), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.SLOWNESS), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SLOWNESS), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.HARMING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 1),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.INVISIBILITY), 2),
            new Entry(() -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.LONG_INVISIBILITY), 3));

    /** O {@code getGearItemForSlot}: [lugar][qualidade] — 0 arma, 1 botas, 2 calça, 3 peitoral, 4 elmo. */
    public static Item gear(int slot, int quality) {
        return switch (slot * 10 + quality) {
            case 0 -> net.minecraft.world.item.Items.IRON_AXE;
            case 1 -> net.minecraft.world.item.Items.IRON_SWORD;
            case 2 -> net.minecraft.world.item.Items.GOLDEN_AXE;
            case 3 -> net.minecraft.world.item.Items.GOLDEN_SWORD;
            case 4 -> TCItems.GEAR.get("thaumium_sword");
            case 5 -> net.minecraft.world.item.Items.DIAMOND_SWORD;
            case 6 -> TCItems.GEAR.get("void_sword");
            case 10 -> net.minecraft.world.item.Items.LEATHER_BOOTS;
            case 11 -> net.minecraft.world.item.Items.GOLDEN_BOOTS;
            case 12 -> net.minecraft.world.item.Items.CHAINMAIL_BOOTS;
            case 13 -> net.minecraft.world.item.Items.IRON_BOOTS;
            case 14 -> TCItems.GEAR.get("thaumium_boots");
            case 15 -> net.minecraft.world.item.Items.DIAMOND_BOOTS;
            case 16 -> TCItems.GEAR.get("void_boots");
            case 20 -> net.minecraft.world.item.Items.LEATHER_LEGGINGS;
            case 21 -> net.minecraft.world.item.Items.GOLDEN_LEGGINGS;
            case 22 -> net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS;
            case 23 -> net.minecraft.world.item.Items.IRON_LEGGINGS;
            case 24 -> TCItems.GEAR.get("thaumium_leggings");
            case 25 -> net.minecraft.world.item.Items.DIAMOND_LEGGINGS;
            case 26 -> TCItems.GEAR.get("void_leggings");
            case 30 -> net.minecraft.world.item.Items.LEATHER_CHESTPLATE;
            case 31 -> net.minecraft.world.item.Items.GOLDEN_CHESTPLATE;
            case 32 -> net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE;
            case 33 -> net.minecraft.world.item.Items.IRON_CHESTPLATE;
            case 34 -> TCItems.GEAR.get("thaumium_chestplate");
            case 35 -> net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
            case 36 -> TCItems.GEAR.get("void_chestplate");
            case 40 -> net.minecraft.world.item.Items.LEATHER_HELMET;
            case 41 -> net.minecraft.world.item.Items.GOLDEN_HELMET;
            case 42 -> net.minecraft.world.item.Items.CHAINMAIL_HELMET;
            case 43 -> net.minecraft.world.item.Items.IRON_HELMET;
            case 44 -> TCItems.GEAR.get("thaumium_helmet");
            case 45 -> net.minecraft.world.item.Items.DIAMOND_HELMET;
            case 46 -> TCItems.GEAR.get("void_helmet");
            default -> null;
        };
    }

    /** O que vai nos baús do mundo. */
    public static final List<Chest> CHESTS = List.of(
            new Chest("chests/simple_dungeon", () -> TCItems.LOOT_BAG, 1, 3, 5),
            new Chest("chests/simple_dungeon", () -> TCResources.get("thaumium_ingot"), 1, 3, 5),
            new Chest("chests/simple_dungeon", () -> TCResources.get("amber"), 1, 3, 5),
            new Chest("chests/jungle_temple", () -> TCItems.LOOT_BAG, 1, 3, 5),
            new Chest("chests/jungle_temple", () -> TCResources.get("thaumium_ingot"), 1, 3, 5),
            new Chest("chests/jungle_temple", () -> TCResources.get("amber"), 1, 3, 5),
            new Chest("chests/desert_pyramid", () -> TCItems.LOOT_BAG, 1, 3, 5),
            new Chest("chests/desert_pyramid", () -> TCResources.get("thaumium_ingot"), 1, 3, 5),
            new Chest("chests/desert_pyramid", () -> TCResources.get("amber"), 1, 3, 5),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.LOOT_BAG, 1, 3, 4),
            new Chest("chests/abandoned_mineshaft", () -> TCResources.get("thaumium_ingot"), 1, 3, 4),
            new Chest("chests/abandoned_mineshaft", () -> TCResources.get("amber"), 1, 3, 4),
            new Chest("chests/stronghold_corridor", () -> TCItems.LOOT_BAG, 1, 3, 4),
            new Chest("chests/stronghold_corridor", () -> TCResources.get("thaumium_ingot"), 1, 3, 4),
            new Chest("chests/stronghold_corridor", () -> TCResources.get("amber"), 1, 3, 4),
            new Chest("chests/stronghold_crossing", () -> TCItems.LOOT_BAG, 1, 3, 4),
            new Chest("chests/stronghold_crossing", () -> TCResources.get("thaumium_ingot"), 1, 3, 4),
            new Chest("chests/stronghold_crossing", () -> TCResources.get("amber"), 1, 3, 4),
            new Chest("chests/stronghold_library", () -> TCItems.LOOT_BAG, 1, 3, 4),
            new Chest("chests/stronghold_library", () -> TCResources.get("thaumium_ingot"), 1, 3, 4),
            new Chest("chests/stronghold_library", () -> TCResources.get("amber"), 1, 3, 4),
            new Chest("chests/simple_dungeon", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 4),
            new Chest("chests/simple_dungeon", () -> TCItems.MUNDANE_AMULET, 1, 2, 4),
            new Chest("chests/simple_dungeon", () -> TCItems.MUNDANE_RING, 1, 2, 4),
            new Chest("chests/simple_dungeon", () -> TCItems.MUNDANE_BELT, 1, 2, 4),
            new Chest("chests/simple_dungeon", () -> TCResources.get("knowledge_fragment"), 1, 2, 4),
            new Chest("chests/jungle_temple", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 4),
            new Chest("chests/jungle_temple", () -> TCItems.MUNDANE_AMULET, 1, 2, 4),
            new Chest("chests/jungle_temple", () -> TCItems.MUNDANE_RING, 1, 2, 4),
            new Chest("chests/jungle_temple", () -> TCItems.MUNDANE_BELT, 1, 2, 4),
            new Chest("chests/jungle_temple", () -> TCResources.get("knowledge_fragment"), 1, 2, 4),
            new Chest("chests/desert_pyramid", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 4),
            new Chest("chests/desert_pyramid", () -> TCItems.MUNDANE_AMULET, 1, 2, 4),
            new Chest("chests/desert_pyramid", () -> TCItems.MUNDANE_RING, 1, 2, 4),
            new Chest("chests/desert_pyramid", () -> TCItems.MUNDANE_BELT, 1, 2, 4),
            new Chest("chests/desert_pyramid", () -> TCResources.get("knowledge_fragment"), 1, 2, 4),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 3),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.MUNDANE_AMULET, 1, 2, 3),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.MUNDANE_RING, 1, 2, 3),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.MUNDANE_BELT, 1, 2, 3),
            new Chest("chests/abandoned_mineshaft", () -> TCResources.get("knowledge_fragment"), 1, 2, 3),
            new Chest("chests/stronghold_corridor", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 3),
            new Chest("chests/stronghold_corridor", () -> TCItems.MUNDANE_AMULET, 1, 2, 3),
            new Chest("chests/stronghold_corridor", () -> TCItems.MUNDANE_RING, 1, 2, 3),
            new Chest("chests/stronghold_corridor", () -> TCItems.MUNDANE_BELT, 1, 2, 3),
            new Chest("chests/stronghold_corridor", () -> TCResources.get("knowledge_fragment"), 1, 2, 3),
            new Chest("chests/stronghold_crossing", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 3),
            new Chest("chests/stronghold_crossing", () -> TCItems.MUNDANE_AMULET, 1, 2, 3),
            new Chest("chests/stronghold_crossing", () -> TCItems.MUNDANE_RING, 1, 2, 3),
            new Chest("chests/stronghold_crossing", () -> TCItems.MUNDANE_BELT, 1, 2, 3),
            new Chest("chests/stronghold_crossing", () -> TCResources.get("knowledge_fragment"), 1, 2, 3),
            new Chest("chests/stronghold_library", () -> TCItems.LOOT_BAG_UNCOMMON, 1, 2, 3),
            new Chest("chests/stronghold_library", () -> TCItems.MUNDANE_AMULET, 1, 2, 3),
            new Chest("chests/stronghold_library", () -> TCItems.MUNDANE_RING, 1, 2, 3),
            new Chest("chests/stronghold_library", () -> TCItems.MUNDANE_BELT, 1, 2, 3),
            new Chest("chests/stronghold_library", () -> TCResources.get("knowledge_fragment"), 1, 2, 3),
            new Chest("chests/simple_dungeon", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/simple_dungeon", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/jungle_temple", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/desert_pyramid", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/abandoned_mineshaft", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/stronghold_corridor", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/stronghold_crossing", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.LOOT_BAG_RARE, 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.THAUMONOMICON, 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.GEAR.get("thaumium_sword"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.GEAR.get("thaumium_pickaxe"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.GEAR.get("thaumium_axe"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.GEAR.get("thaumium_hoe"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.RUNIC_RING_LESSER, 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("air"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("earth"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("fire"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("water"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("order"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.APPRENTICE_RINGS.get("entropy"), 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCItems.VIS_STONE, 1, 1, 1),
            new Chest("chests/stronghold_library", () -> TCResources.get("knowledge_fragment"), 3, 6, 20),
            new Chest("chests/village/village_weaponsmith", () -> TCResources.get("thaumium_ingot"), 1, 3, 10));

    /** A pedra de vis do saque: de zero a quatro centésimos de cada primário, como no {@code initLoot}. */
    public static ItemStack visStone() {
        ItemStack stone = new ItemStack(TCItems.VIS_STONE);
        stone.set(TCComponents.WAND_VIS, visStoneVis());
        return stone;
    }

    /** O vis sorteado da pedra de vis do saque. */
    public static AspectList visStoneVis() {
        RandomSource random = RandomSource.create();
        AspectList vis = new AspectList();
        for (Aspect aspect : Aspects.primals()) {
            int amount = random.nextInt(5);
            if (amount > 0) vis.add(aspect, amount);
        }
        return vis;
    }

    private ThaumLoot() {
    }
}
