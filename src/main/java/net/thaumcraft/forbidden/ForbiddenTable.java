package net.thaumcraft.forbidden;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.List;

/**
 * As receitas do Forbidden Magic 0.575 — o {@code ForbiddenRecipes} do original.
 *
 * <p>Só entra o que fecha com coisas que já existem por aqui: o que o original guarda atrás de outro mod
 * (Blood Magic, Botania, Ars Magica, Twilight Forest, Thaumic Tinkerer) fica de fora, como ele mesmo faz quando
 * esses mods não estão instalados.
 *
 * <p>Os encantamentos sombrios se põem por <b>infusão na própria peça</b>, que é o
 * {@code addInfusionEnchantmentRecipe} do original: a coisa do meio sai da matriz com o encantamento gravado.
 */
public final class ForbiddenTable {
    private ForbiddenTable() {
    }

    private static AspectList aspects() {
        return new AspectList();
    }

    /** Um aspecto do ramo, pelo nome. */
    private static net.thaumcraft.api.aspects.Aspect dark(String name) {
        return ForbiddenAspects.ASPECTS.get(name);
    }

    public static void recipes() {
        // ---------------------------------------------------------------- as ferramentas
        ThaumcraftApi.bookRecipe("ItemTaintShovel", ThaumcraftApi.infusion("FM_TAINTSHOVEL",
                new ItemStack(ForbiddenItems.PURIFIER_SHOVEL), 5,
                aspects().add(Aspects.HEAL, 8).add(Aspects.CRYSTAL, 24).add(Aspects.MINE, 12),
                Ingredient.of(TCItems.GEAR.get("thaumium_shovel")),
                List.of(Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCResources.get("amber")),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(TCBlocks.SILVERWOOD_LOG.asItem()))));

        ThaumcraftApi.bookRecipe("ItemTaintPickaxe", ThaumcraftApi.infusion("FM_TAINTPICK",
                new ItemStack(ForbiddenItems.DISTORTION_PICKAXE), 1,
                aspects().add(Aspects.ENTROPY, 8).add(Aspects.MINE, 8).add(Aspects.TAINT, 12),
                Ingredient.of(TCItems.GEAR.get("thaumium_pickaxe")),
                List.of(Ingredient.of(TCItems.SHARDS.get("entropy")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("taint")),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(TCBlocks.GREATWOOD_LOG.asItem()))));

        ThaumcraftApi.bookRecipe("ItemSkullAxe", ThaumcraftApi.infusion("FM_SKULLAXE",
                new ItemStack(ForbiddenItems.SKULLTAKER_AXE), 1,
                aspects().add(dark("ira"), 8).add(Aspects.WEAPON, 8).add(dark("infernus"), 8),
                Ingredient.of(TCItems.GEAR.get("thaumium_axe")),
                List.of(Ingredient.of(ForbiddenItems.SHARDS.get("wrath")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("wrath")),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(Items.WITHER_SKELETON_SKULL))));

        ThaumcraftApi.bookRecipe("ItemFork", ThaumcraftApi.infusion("FM_FORK",
                new ItemStack(ForbiddenItems.DIABOLIST_FORK), 1,
                aspects().add(dark("infernus"), 8).add(Aspects.MECHANISM, 8).add(Aspects.ENERGY, 8),
                Ingredient.of(TCItems.GEAR.get("thaumium_sword")),
                List.of(Ingredient.of(Items.QUARTZ), Ingredient.of(Items.QUARTZ), Ingredient.of(Items.QUARTZ),
                        Ingredient.of(Items.REDSTONE))));

        ThaumcraftApi.bookRecipe("ItemRidingCrop", ThaumcraftApi.crafting(
                () -> new ItemStack(ForbiddenItems.RIDING_CROP), 1, 3,
                List.of(List.of(new ItemStack(Items.LEATHER)), List.of(new ItemStack(Items.STICK)),
                        List.of(new ItemStack(Items.STICK)))));

        // as quatro camaleão saem da mesma infusão, cada uma com a ferramenta de táumio no meio
        morph("ItemMorphPickaxe", ForbiddenItems.CHAMELEON_PICKAXE, "thaumium_pickaxe", Aspects.TOOL);
        morph("ItemMorphSword", ForbiddenItems.CHAMELEON_SWORD, "thaumium_sword", Aspects.WEAPON);
        morph("ItemMorphShovel", ForbiddenItems.CHAMELEON_SHOVEL, "thaumium_shovel", Aspects.TOOL);
        morph("ItemMorphAxe", ForbiddenItems.CHAMELEON_AXE, "thaumium_axe", Aspects.TOOL);

        // ---------------------------------------------------------------- a árvore e a pedra maculadas
        ThaumcraftApi.bookRecipe("BlockTaintSapling", ThaumcraftApi.crucible("FM_TAINTTREE",
                new ItemStack(ForbiddenBlocks.TAINT_SAPLING), Items.OAK_SAPLING,
                aspects().add(Aspects.TAINT, 10).add(Aspects.POISON, 4)));

        ThaumcraftApi.bookRecipe("BlockTaintPlanks", ThaumcraftApi.crafting(
                () -> new ItemStack(ForbiddenItems.TAINT_PLANKS, 4), 1, 1,
                List.of(List.of(new ItemStack(ForbiddenItems.TAINT_LOG)))));

        ThaumcraftApi.bookRecipe("BlockTaintStone", ThaumcraftApi.arcane("FM_TAINTSTONE",
                new ItemStack(ForbiddenItems.TAINT_STONE, 9), aspects().add(Aspects.ENTROPY, 2).add(Aspects.ORDER, 1),
                List.of(Ingredient.of(Blocks.STONE.asItem()), Ingredient.of(Blocks.STONE.asItem()), Ingredient.of(Blocks.STONE.asItem()),
                        Ingredient.of(Blocks.STONE.asItem()), Ingredient.of(ForbiddenItems.SHARDS.get("taint")), Ingredient.of(Blocks.STONE.asItem()),
                        Ingredient.of(Blocks.STONE.asItem()), Ingredient.of(Blocks.STONE.asItem()), Ingredient.of(Blocks.STONE.asItem()))));

        ThaumcraftApi.bookRecipe("BlockTaintBricks", ThaumcraftApi.crafting(
                () -> new ItemStack(ForbiddenItems.TAINT_STONE_BRICKS, 4), 2, 2,
                List.of(List.of(new ItemStack(ForbiddenItems.TAINT_STONE)), List.of(new ItemStack(ForbiddenItems.TAINT_STONE)),
                        List.of(new ItemStack(ForbiddenItems.TAINT_STONE)), List.of(new ItemStack(ForbiddenItems.TAINT_STONE)))));

        // ---------------------------------------------------------------- a mesa e as bijuterias
        ThaumcraftApi.bookRecipe("ItemCrystalwell", ThaumcraftApi.arcaneShapeless("FM_CRYSTALWELL",
                new ItemStack(ForbiddenItems.CRYSTALWELL), aspects().add(Aspects.WATER, 1).add(Aspects.ORDER, 1),
                List.of(Ingredient.of(TCItems.SCRIBING_TOOLS), Ingredient.of(Items.INK_SAC),
                        Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));

        ThaumcraftApi.bookRecipe("ItemPrimewell", ThaumcraftApi.arcaneShapeless("FM_PRIMEWELL",
                new ItemStack(ForbiddenItems.PRIMEWELL),
                aspects().add(Aspects.WATER, 50).add(Aspects.EARTH, 50).add(Aspects.FIRE, 50)
                        .add(Aspects.AIR, 50).add(Aspects.ORDER, 50).add(Aspects.ENTROPY, 50),
                List.of(Ingredient.of(Items.FEATHER), Ingredient.of(TCItems.PRIMORDIAL_PEARL),
                        Ingredient.of(Items.GLASS_BOTTLE))));

        ThaumcraftApi.bookRecipe("ItemSubCollar", ThaumcraftApi.infusion("FM_SUBCOLLAR",
                new ItemStack(ForbiddenItems.COLLAR), 1,
                aspects().add(dark("luxuria"), 8).add(Aspects.TRAP, 8).add(dark("infernus"), 4).add(Aspects.FLESH, 4),
                Ingredient.of(TCItems.VIS_AMULET),
                List.of(Ingredient.of(ForbiddenItems.SHARDS.get("lust")), Ingredient.of(ForbiddenItems.SHARDS.get("lust")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("lust")), Ingredient.of(Items.LEAD),
                        Ingredient.of(TCItems.MUNDANE_AMULET))));

        ThaumcraftApi.bookRecipe("ItemRingFood", ThaumcraftApi.arcane("FM_RINGFOOD",
                new ItemStack(ForbiddenItems.NUTRITION_RING),
                aspects().add(Aspects.ENTROPY, 10).add(Aspects.WATER, 20).add(Aspects.EARTH, 20),
                java.util.Arrays.asList(null, Ingredient.of(Items.DIAMOND), null,
                        Ingredient.of(ForbiddenItems.GLUTTONY_SHARD), Ingredient.of(TCItems.MUNDANE_RING),
                        Ingredient.of(ForbiddenItems.GLUTTONY_SHARD),
                        null, Ingredient.of(ForbiddenItems.GLUTTONY_SHARD), null)));

        // ---------------------------------------------------------------- o bolo, as flores e o foco
        ThaumcraftApi.bookRecipe("ItemArcaneCake", ThaumcraftApi.infusion("FM_ARCANECAKE",
                new ItemStack(ForbiddenItems.ARCANE_CAKE), 3,
                aspects().add(dark("gula"), 12).add(Aspects.HUNGER, 24).add(Aspects.CRAFT, 24),
                Ingredient.of(Items.CAKE),
                List.of(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(Items.EGG),
                        Ingredient.of(Items.MILK_BUCKET), Ingredient.of(Items.EGG),
                        Ingredient.of(ForbiddenItems.GLUTTONY_SHARD), Ingredient.of(ForbiddenItems.GLUTTONY_SHARD))));

        ThaumcraftApi.bookRecipe("BlockUmbralBush", ThaumcraftApi.crucible("FM_BLACKFLOWER",
                new ItemStack(ForbiddenItems.UMBRAL_BUSH), Items.ROSE_BUSH,
                aspects().add(Aspects.DARKNESS, 8).add(Aspects.LIFE, 5)));

        ThaumcraftApi.bookRecipe("ItemBlackInk", ThaumcraftApi.crafting(
                () -> new ItemStack(Items.INK_SAC, 2), 1, 1,
                List.of(List.of(new ItemStack(ForbiddenItems.INK_FLOWER)))));

        ThaumcraftApi.bookRecipe("ItemFocusBlink", ThaumcraftApi.infusion("FM_FOCUSBLINK",
                new ItemStack(ForbiddenItems.FOCUS_BLINK), 3,
                aspects().add(Aspects.TRAVEL, 25).add(dark("infernus"), 10).add(dark("desidia"), 10).add(Aspects.ENTROPY, 25),
                Ingredient.of(Items.ENDER_PEARL),
                List.of(Ingredient.of(Items.QUARTZ), Ingredient.of(ForbiddenItems.SHARDS.get("sloth")),
                        Ingredient.of(Items.QUARTZ), Ingredient.of(ForbiddenItems.SHARDS.get("sloth")),
                        Ingredient.of(Items.QUARTZ), Ingredient.of(ForbiddenItems.SHARDS.get("sloth")))));

        // ---------------------------------------------------------------- as peças de varinha
        ThaumcraftApi.bookRecipe("ItemWandRodTainted", ThaumcraftApi.infusion("FM_ROD_tainted",
                new ItemStack(ForbiddenItems.WAND_ROD_TAINTED), 5,
                aspects().add(Aspects.TAINT, 24).add(Aspects.MAGIC, 12).add(Aspects.ENTROPY, 12),
                Ingredient.of(TCResources.get("taint_tendril")),
                List.of(Ingredient.of(TCResources.get("salis_mundus")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("taint")), Ingredient.of(ForbiddenItems.SHARDS.get("taint")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("taint")), Ingredient.of(ForbiddenItems.SHARDS.get("taint")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("taint")), Ingredient.of(ForbiddenItems.SHARDS.get("taint")))));

        ThaumcraftApi.bookRecipe("ItemWandRodInfernal", ThaumcraftApi.infusion("FM_ROD_infernal",
                new ItemStack(ForbiddenItems.WAND_ROD_INFERNAL), 5,
                aspects().add(dark("infernus"), 32).add(Aspects.MAGIC, 12).add(dark("superbia"), 12),
                Ingredient.of(Items.BLAZE_ROD),
                List.of(Ingredient.of(TCResources.get("salis_mundus")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("pride")), Ingredient.of(ForbiddenItems.SHARDS.get("pride")),
                        Ingredient.of(Blocks.SOUL_SAND.asItem()), Ingredient.of(Items.WITHER_SKELETON_SKULL),
                        Ingredient.of(Items.QUARTZ), Ingredient.of(Items.BLAZE_POWDER))));

        // ---------------------------------------------------------------- a gaiola
        ThaumcraftApi.bookRecipe("BlockWrathCage", ThaumcraftApi.infusion("FM_WRATHCAGE",
                new ItemStack(ForbiddenItems.WRATH_CAGE), 10,
                aspects().add(dark("ira"), 32).add(Aspects.MAGIC, 32).add(Aspects.BEAST, 32).add(Aspects.MECHANISM, 16),
                Ingredient.of(TCBlocks.BUILDING.get("thaumium_block").asItem()),
                List.of(Ingredient.of(ForbiddenItems.SHARDS.get("wrath")), Ingredient.of(ForbiddenItems.SHARDS.get("wrath")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("wrath")), Ingredient.of(ForbiddenItems.SHARDS.get("wrath")),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(Items.DIAMOND),
                        Ingredient.of(TCBlocks.JAR.asItem()), Ingredient.of(TCBlocks.JAR.asItem()),
                        Ingredient.of(TCBlocks.JAR.asItem()))));

        ThaumcraftApi.bookRecipe("ItemMobCrystal", ThaumcraftApi.crucible("FM_WRATHCAGE",
                new ItemStack(ForbiddenItems.MOB_CRYSTAL), Items.DIAMOND,
                aspects().add(Aspects.MIND, 10).add(Aspects.ENERGY, 10)));

        // ---------------------------------------------------------------- as estrelas e as esmeraldas
        ThaumcraftApi.bookRecipe("BlockNetherStar", ThaumcraftApi.crafting(
                () -> new ItemStack(ForbiddenItems.NETHER_STAR_BLOCK), 3, 3,
                nine(new ItemStack(Items.NETHER_STAR))));
        ThaumcraftApi.bookRecipe("ItemEmeraldNugget", ThaumcraftApi.crafting(
                () -> new ItemStack(ForbiddenItems.EMERALD_NUGGET, 9), 1, 1,
                List.of(List.of(new ItemStack(Items.EMERALD)))));
        ThaumcraftApi.bookRecipe("ItemEmeraldFromNuggets", ThaumcraftApi.crafting(
                () -> new ItemStack(Items.EMERALD), 3, 3,
                nine(new ItemStack(ForbiddenItems.EMERALD_NUGGET))));

        // ---------------------------------------------------------------- os encantamentos sombrios
        enchant("Wrath", ForbiddenEnchantments.WRATH, 8,
                aspects().add(dark("ira"), 16).add(Aspects.WEAPON, 16).add(dark("infernus"), 8),
                List.of(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(Items.DIAMOND_SWORD),
                        Ingredient.of(ForbiddenItems.SHARDS.get("wrath")), Ingredient.of(ForbiddenItems.SHARDS.get("wrath")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("wrath"))));
        enchant("Greedy", ForbiddenEnchantments.GREEDY, 4,
                aspects().add(dark("infernus"), 16).add(Aspects.WEAPON, 8).add(Aspects.GREED, 16),
                List.of(Ingredient.of(Items.GOLDEN_SWORD), Ingredient.of(Items.DIAMOND),
                        Ingredient.of(ForbiddenItems.SHARDS.get("greed")), Ingredient.of(ForbiddenItems.SHARDS.get("greed")),
                        Ingredient.of(TCResources.get("salis_mundus"))));
        enchant("Consuming", ForbiddenEnchantments.CONSUMING, 3,
                aspects().add(Aspects.VOID, 8).add(Aspects.TOOL, 8).add(Aspects.HUNGER, 8),
                List.of(Ingredient.of(Items.IRON_PICKAXE), Ingredient.of(Items.LAVA_BUCKET),
                        Ingredient.of(TCResources.get("salis_mundus"))));
        enchant("Educational", ForbiddenEnchantments.EDUCATIONAL, 3,
                aspects().add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 4).add(Aspects.MIND, 8),
                List.of(Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(Items.BOOK),
                        Ingredient.of(TCResources.get("salis_mundus"))));
        enchant("Corrupting", ForbiddenEnchantments.CORRUPTING, 4,
                aspects().add(dark("infernus"), 16).add(Aspects.EXCHANGE, 16).add(Aspects.CRYSTAL, 8),
                List.of(Ingredient.of(Items.NETHER_WART), Ingredient.of(Blocks.SOUL_SAND.asItem()),
                        Ingredient.of(TCResources.get("salis_mundus"))));
        enchant("Cluster", ForbiddenEnchantments.CLUSTER, 3,
                aspects().add(Aspects.FIRE, 4).add(Aspects.METAL, 4).add(Aspects.GREED, 4),
                List.of(Ingredient.of(TCItems.ELEMENTAL_PICKAXE), Ingredient.of(TCResources.get("salis_mundus"))));
        enchant("Impact", ForbiddenEnchantments.IMPACT, 4,
                aspects().add(Aspects.ENTROPY, 16).add(Aspects.MINE, 16),
                List.of(Ingredient.of(TCResources.get("salis_mundus")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("envy")), Ingredient.of(ForbiddenItems.SHARDS.get("envy")),
                        Ingredient.of(TCItems.ELEMENTAL_SHOVEL)));
        enchant("Voidtouched", ForbiddenEnchantments.VOIDTOUCHED, 8,
                aspects().add(Aspects.VOID, 16).add(Aspects.DARKNESS, 16).add(dark("invidia"), 24).add(Aspects.ELDRITCH, 16),
                List.of(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("void_ingot")),
                        Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("envy")), Ingredient.of(ForbiddenItems.SHARDS.get("envy")),
                        Ingredient.of(ForbiddenItems.SHARDS.get("envy"))));
    }

    /** Uma das quatro ferramentas camaleão: a mesma infusão, com a ferramenta de táumio no meio. */
    private static void morph(String book, net.minecraft.world.item.Item result, String thaumium,
                              net.thaumcraft.api.aspects.Aspect kind) {
        ThaumcraftApi.bookRecipe(book, ThaumcraftApi.infusion("FM_MORPHTOOLS",
                new ItemStack(result), 6,
                aspects().add(Aspects.EXCHANGE, 32).add(dark("invidia"), 16).add(kind, 16),
                Ingredient.of(TCItems.GEAR.get(thaumium)),
                List.of(Ingredient.of(ForbiddenItems.SHARDS.get("envy")), Ingredient.of(ForbiddenItems.SHARDS.get("envy")),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(TCResources.get("quicksilver")),
                        Ingredient.of(TCBlocks.SILVERWOOD_LOG.asItem()))));
    }

    /**
     * Um encantamento sombrio: o {@code addInfusionEnchantmentRecipe} do original, que grava o encantamento na
     * própria peça posta no meio da matriz. Por aqui isso é a receita de infusão de encantamento do mod.
     */
    private static void enchant(String book, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> which,
                                int instability, AspectList essentia, List<Ingredient> components) {
        var recipe = new net.thaumcraft.crafting.InfusionEnchantmentRecipe(
                "FM_" + book.toUpperCase(java.util.Locale.ROOT), which, instability, essentia, components);
        net.thaumcraft.crafting.InfusionEnchantments.ALL.add(recipe);
        ThaumcraftApi.bookRecipe("Enchantment" + book, recipe);
    }

    private static List<List<ItemStack>> nine(ItemStack what) {
        return List.of(List.of(what), List.of(what), List.of(what), List.of(what), List.of(what),
                List.of(what), List.of(what), List.of(what), List.of(what));
    }
}
