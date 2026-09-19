package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.List;

/**
 * As receitas de crisol do Thaumcraft 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia5-crisol.js} a partir do
 * {@code ConfigRecipesCrucibleSlice} do mod original. Só entram as receitas que fecham com coisas que
 * já existem por aqui — as outras vão entrando conforme as fatias trouxerem as peças que faltam.
 */
public final class CrucibleRecipes {
    public static final List<CrucibleRecipe> ALL = new ArrayList<>();

    private CrucibleRecipes() {
    }

    public static void init() {
    }

    /** A receita que esta água e esta coisa fecham, se houver alguma. */
    public static CrucibleRecipe find(AspectList inside, ItemStack thrown) {
        for (CrucibleRecipe recipe : ALL) {
            if (recipe.matches(inside, thrown)) return recipe;
        }
        return null;
    }

    static {
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("air"),
                new AspectList().add(Aspects.FIRE, 2).add(Aspects.WATER, 2).add(Aspects.EARTH, 2).add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("fire"),
                new AspectList().add(Aspects.AIR, 2).add(Aspects.WATER, 2).add(Aspects.EARTH, 2).add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("water"),
                new AspectList().add(Aspects.AIR, 2).add(Aspects.FIRE, 2).add(Aspects.EARTH, 2).add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("earth"),
                new AspectList().add(Aspects.AIR, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2).add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("order"),
                new AspectList().add(Aspects.AIR, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2).add(Aspects.EARTH, 2).add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.SHARDS.get("entropy"),
                new AspectList().add(Aspects.AIR, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2).add(Aspects.EARTH, 2).add(Aspects.ORDER, 2)));
        ALL.add(new CrucibleRecipe("ALUMENTUM", new ItemStack(TCItems.ALUMENTUM), net.minecraft.world.item.Items.COAL,
                new AspectList().add(Aspects.ENERGY, 3).add(Aspects.FIRE, 3).add(Aspects.ENTROPY, 3)));
        ALL.add(new CrucibleRecipe("NITOR", new ItemStack(TCItems.NITOR), net.minecraft.world.item.Items.GLOWSTONE_DUST,
                new AspectList().add(Aspects.ENERGY, 3).add(Aspects.FIRE, 3).add(Aspects.LIGHT, 3)));
        ALL.add(new CrucibleRecipe("THAUMIUM", new ItemStack(TCResources.get("thaumium_ingot")), net.minecraft.world.item.Items.IRON_INGOT,
                new AspectList().add(Aspects.MAGIC, 4)));
        ALL.add(new CrucibleRecipe("TALLOW", new ItemStack(TCResources.get("magic_tallow")), net.minecraft.world.item.Items.ROTTEN_FLESH,
                new AspectList().add(Aspects.MAGIC, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.GUNPOWDER, 2), net.minecraft.world.item.Items.GUNPOWDER,
                new AspectList().add(Aspects.FIRE, 4).add(Aspects.ENTROPY, 4)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.SLIME_BALL, 2), net.minecraft.world.item.Items.SLIME_BALL,
                new AspectList().add(Aspects.WATER, 2).add(Aspects.LIFE, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.CLAY_BALL, 2), net.minecraft.world.item.Items.CLAY_BALL,
                new AspectList().add(Aspects.WATER, 1).add(Aspects.EARTH, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.GLOWSTONE_DUST, 2), net.minecraft.world.item.Items.GLOWSTONE_DUST,
                new AspectList().add(Aspects.LIGHT, 3).add(Aspects.SENSES, 1)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.INK_SAC, 2), net.minecraft.world.item.Items.INK_SAC,
                new AspectList().add(Aspects.WATER, 2).add(Aspects.SENSES, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.COBWEB.asItem()), net.minecraft.world.item.Items.STRING,
                new AspectList().add(Aspects.TRAP, 2).add(Aspects.CLOTH, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE.asItem()), net.minecraft.world.level.block.Blocks.COBBLESTONE.asItem(),
                new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAGIC, 1)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.ICE.asItem()), net.minecraft.world.level.block.Blocks.SNOW_BLOCK.asItem(),
                new AspectList().add(Aspects.ORDER, 1).add(Aspects.COLD, 1)));
        ALL.add(new CrucibleRecipe("ENTROPICPROCESSING", new ItemStack(net.minecraft.world.level.block.Blocks.CRACKED_STONE_BRICKS.asItem()), net.minecraft.world.level.block.Blocks.STONE_BRICKS.asItem(),
                new AspectList().add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("ENTROPICPROCESSING", new ItemStack(net.minecraft.world.item.Items.BONE_MEAL, 4), net.minecraft.world.item.Items.BONE,
                new AspectList().add(Aspects.ENTROPY, 1)));
        ALL.add(new CrucibleRecipe("PUREIRON", new ItemStack(TCResources.get("native_iron_cluster")), net.minecraft.world.item.Items.IRON_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("PUREIRON", new ItemStack(TCResources.get("native_iron_cluster")), net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("PUREGOLD", new ItemStack(TCResources.get("native_gold_cluster")), net.minecraft.world.item.Items.GOLD_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("PUREGOLD", new ItemStack(TCResources.get("native_gold_cluster")), net.minecraft.world.item.Items.DEEPSLATE_GOLD_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("PURECOPPER", new ItemStack(TCResources.get("native_copper_cluster")), net.minecraft.world.item.Items.COPPER_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("PURECOPPER", new ItemStack(TCResources.get("native_copper_cluster")), net.minecraft.world.item.Items.DEEPSLATE_COPPER_ORE,
                new AspectList().add(Aspects.METAL, 1).add(Aspects.ORDER, 1)));
        ALL.add(new CrucibleRecipe("TRANSIRON", new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 3), net.minecraft.world.item.Items.IRON_NUGGET,
                new AspectList().add(Aspects.METAL, 2)));
        ALL.add(new CrucibleRecipe("TRANSGOLD", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 3), net.minecraft.world.item.Items.GOLD_NUGGET,
                new AspectList().add(Aspects.METAL, 2).add(Aspects.GREED, 1)));
        ALL.add(new CrucibleRecipe("TRANSCOPPER", new ItemStack(net.minecraft.world.item.Items.COPPER_NUGGET, 3), net.minecraft.world.item.Items.COPPER_NUGGET,
                new AspectList().add(Aspects.METAL, 2).add(Aspects.EXCHANGE, 1)));
        ALL.add(new CrucibleRecipe("ETHEREALBLOOM", new ItemStack(TCBlocks.ETHEREAL_BLOOM.asItem()), TCBlocks.SHIMMERLEAF.asItem(),
                new AspectList().add(Aspects.MAGIC, 16).add(Aspects.PLANT, 16).add(Aspects.HEAL, 16).add(Aspects.TAINT, 8)));
        ALL.add(new CrucibleRecipe("LIQUIDDEATH", new ItemStack(TCItems.BUCKET_DEATH), net.minecraft.world.item.Items.GLASS_BOTTLE,
                new AspectList().add(Aspects.DEATH, 32).add(Aspects.POISON, 32).add(Aspects.ENTROPY, 32)));
        ALL.add(new CrucibleRecipe("BOTTLETAINT", new ItemStack(TCItems.BOTTLE_TAINT), TCItems.PHIAL,
                new AspectList().add(Aspects.TAINT, 8).add(Aspects.MAGIC, 8)));
        ALL.add(new CrucibleRecipe("GOLEMSTRAW", new ItemStack(TCItems.GOLEM_PLACERS.get("straw")), net.minecraft.world.level.block.Blocks.HAY_BLOCK.asItem(),
                new AspectList().add(Aspects.MAN, 4).add(Aspects.MOTION, 4).add(Aspects.SOUL, 4)));
        ALL.add(new CrucibleRecipe("GOLEMWOOD", new ItemStack(TCItems.GOLEM_PLACERS.get("wood")), TCBlocks.GREATWOOD_LOG.asItem(),
                new AspectList().add(Aspects.MAN, 4).add(Aspects.MOTION, 4).add(Aspects.SOUL, 4)));
        ALL.add(new CrucibleRecipe("GOLEMTALLOW", new ItemStack(TCItems.GOLEM_PLACERS.get("tallow")), TCBlocks.BUILDING.get("tallow_block").asItem(),
                new AspectList().add(Aspects.MAN, 8).add(Aspects.MOTION, 8).add(Aspects.SOUL, 8)));
        ALL.add(new CrucibleRecipe("GOLEMCLAY", new ItemStack(TCItems.GOLEM_PLACERS.get("clay")), net.minecraft.world.level.block.Blocks.BRICKS.asItem(),
                new AspectList().add(Aspects.MAN, 4).add(Aspects.MOTION, 4).add(Aspects.SOUL, 4)));
        ALL.add(new CrucibleRecipe("GOLEMFLESH", new ItemStack(TCItems.GOLEM_PLACERS.get("flesh")), TCBlocks.FLESH_BLOCK.asItem(),
                new AspectList().add(Aspects.MAN, 8).add(Aspects.MOTION, 8).add(Aspects.SOUL, 8)));
        ALL.add(new CrucibleRecipe("GOLEMSTONE", new ItemStack(TCItems.GOLEM_PLACERS.get("stone")), net.minecraft.world.level.block.Blocks.STONE_BRICKS.asItem(),
                new AspectList().add(Aspects.MAN, 4).add(Aspects.MOTION, 4).add(Aspects.SOUL, 4)));
        ALL.add(new CrucibleRecipe("GOLEMIRON", new ItemStack(TCItems.GOLEM_PLACERS.get("iron")), net.minecraft.world.level.block.Blocks.IRON_BLOCK.asItem(),
                new AspectList().add(Aspects.MAN, 4).add(Aspects.MOTION, 4).add(Aspects.SOUL, 4)));
        ALL.add(new CrucibleRecipe("GOLEMTHAUMIUM", new ItemStack(TCItems.GOLEM_PLACERS.get("thaumium")), TCBlocks.BUILDING.get("thaumium_block").asItem(),
                new AspectList().add(Aspects.MAN, 8).add(Aspects.MOTION, 8).add(Aspects.SOUL, 8)));
        ALL.add(new CrucibleRecipe("COREGATHER", new ItemStack(TCItems.GOLEM_CORES.get("gather")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.GREED, 5).add(Aspects.EARTH, 5)));
        ALL.add(new CrucibleRecipe("COREFILL", new ItemStack(TCItems.GOLEM_CORES.get("fill")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.HUNGER, 5).add(Aspects.VOID, 5)));
        ALL.add(new CrucibleRecipe("COREEMPTY", new ItemStack(TCItems.GOLEM_CORES.get("empty")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.GREED, 5).add(Aspects.VOID, 5)));
        ALL.add(new CrucibleRecipe("COREHARVEST", new ItemStack(TCItems.GOLEM_CORES.get("harvest")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.HARVEST, 5).add(Aspects.CROP, 5)));
        ALL.add(new CrucibleRecipe("COREGUARD", new ItemStack(TCItems.GOLEM_CORES.get("guard")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.WEAPON, 5).add(Aspects.TRAP, 5)));
        ALL.add(new CrucibleRecipe("COREBUTCHER", new ItemStack(TCItems.GOLEM_CORES.get("butcher")), TCItems.GOLEM_CORES.get("guard"),
                new AspectList().add(Aspects.FLESH, 5).add(Aspects.BEAST, 5)));
        ALL.add(new CrucibleRecipe("CORELIQUID", new ItemStack(TCItems.GOLEM_CORES.get("decanting")), TCItems.GOLEM_CORE_BLANK,
                new AspectList().add(Aspects.WATER, 5).add(Aspects.VOID, 5)));
        ALL.add(new CrucibleRecipe("BATHSALTS", new ItemStack(TCItems.BATH_SALTS), TCResources.get("salis_mundus"),
                new AspectList().add(Aspects.MIND, 6).add(Aspects.AURA, 6).add(Aspects.ORDER, 6).add(Aspects.HEAL, 6)));
    }
}
