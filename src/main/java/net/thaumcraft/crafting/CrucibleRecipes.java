package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCItems;

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
        ALL.add(new CrucibleRecipe("CRUCIBLE", new ItemStack(TCItems.SHARD_BALANCED), TCItems.ALUMENTUM,
                new AspectList().add(Aspects.ENERGY, 3).add(Aspects.FIRE, 3).add(Aspects.ENTROPY, 3)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.GUNPOWDER), net.minecraft.world.item.Items.GUNPOWDER,
                new AspectList().add(Aspects.FIRE, 4).add(Aspects.ENTROPY, 4)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.SLIME_BALL), net.minecraft.world.item.Items.SLIME_BALL,
                new AspectList().add(Aspects.WATER, 2).add(Aspects.LIFE, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALDUPLICATION", new ItemStack(net.minecraft.world.item.Items.CLAY_BALL), net.minecraft.world.item.Items.CLAY_BALL,
                new AspectList().add(Aspects.WATER, 1).add(Aspects.EARTH, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.COBWEB.asItem()), net.minecraft.world.item.Items.STRING,
                new AspectList().add(Aspects.TRAP, 2).add(Aspects.CLOTH, 2)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE.asItem()), net.minecraft.world.level.block.Blocks.COBBLESTONE.asItem(),
                new AspectList().add(Aspects.PLANT, 2).add(Aspects.MAGIC, 1)));
        ALL.add(new CrucibleRecipe("ALCHEMICALMANUFACTURE", new ItemStack(net.minecraft.world.level.block.Blocks.ICE.asItem()), net.minecraft.world.level.block.Blocks.PACKED_ICE.asItem(),
                new AspectList().add(Aspects.ORDER, 1).add(Aspects.COLD, 1)));
        ALL.add(new CrucibleRecipe("ENTROPICPROCESSING", new ItemStack(net.minecraft.world.level.block.Blocks.CRACKED_STONE_BRICKS.asItem()), net.minecraft.world.level.block.Blocks.STONE_BRICKS.asItem(),
                new AspectList().add(Aspects.ENTROPY, 2)));
        ALL.add(new CrucibleRecipe("ENTROPICPROCESSING", new ItemStack(net.minecraft.world.item.Items.BONE_MEAL), net.minecraft.world.item.Items.BONE,
                new AspectList().add(Aspects.ENTROPY, 1)));
        ALL.add(new CrucibleRecipe("TRANSGOLD", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET), net.minecraft.world.item.Items.GOLD_NUGGET,
                new AspectList().add(Aspects.METAL, 2).add(Aspects.GREED, 1)));
    }
}
