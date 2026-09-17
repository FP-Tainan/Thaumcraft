package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.NodeBlock;

/** Os blocos do mod. */
public final class TCBlocks {
    /** O nó de aura: uma bolha de magia parada no ar, que não se quebra na mão. */
    public static final Block NODE = register("node", properties -> new NodeBlock(properties
            .mapColor(MapColor.NONE)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
            .noOcclusion()
            .noCollision()
            .lightLevel(state -> 7)
            .sound(SoundType.AMETHYST)
            .pushReaction(PushReaction.BLOCK)));

    /** O crisol: um caldeirão que a varinha benzeu. */
    public static final Block CRUCIBLE = register("crucible", properties -> new net.thaumcraft.block.CrucibleBlock(properties
            .mapColor(MapColor.METAL)
            .strength(2.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .sound(SoundType.METAL)));

    /** O minério infundido: a pedra de onde os fragmentos de aspecto saem. */
    public static final java.util.Map<String, Block> INFUSED_STONE = new java.util.LinkedHashMap<>();

    static {
        for (String tag : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) {
            INFUSED_STONE.put(tag, register("infused_stone_" + tag, properties -> new Block(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.STONE))));
        }
    }

    /** O jarro lacrado: um pote de vidro com tampa de chumbo, que guarda essência. */
    public static final Block JAR = register("jar", properties ->
            new net.thaumcraft.block.JarBlock(properties
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));

    /** O tubo de essência: o cano de latão por onde a essência corre. */
    public static final Block TUBE = register("tube", properties ->
            new net.thaumcraft.block.TubeBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(0.5f)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** O alambique arcano: o pote que se empilha sobre o forno e recolhe o que sai dele. */
    public static final Block ALEMBIC = register("alembic", properties ->
            new net.thaumcraft.block.AlembicBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(1.5f)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** O forno alquímico: onde a coisa deixa de ser coisa e vira essência. */
    public static final Block ALCHEMICAL_FURNACE = register("alchemical_furnace", properties ->
            new net.thaumcraft.block.AlchemicalFurnaceBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(
                            net.thaumcraft.block.AlchemicalFurnaceBlock.LIT) ? 13 : 0)
                    .sound(SoundType.STONE)));

    /** O pedestal arcano: segura uma coisa só, à vista de todos. */
    public static final Block PEDESTAL = register("pedestal", properties ->
            new net.thaumcraft.block.PedestalBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .sound(SoundType.STONE)));

    /** A matriz rúnica: a pedra entalhada onde a infusão acontece. */
    public static final Block INFUSION_MATRIX = register("infusion_matrix", properties ->
            new net.thaumcraft.block.InfusionMatrixBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> 6)
                    .sound(SoundType.STONE)));

    /** A folha-cintilante: a flor branca de que se faz a Flor Etérea. */
    public static final Block SHIMMERLEAF = register("shimmerleaf", properties ->
            new net.thaumcraft.block.ShimmerleafBlock(properties
                    .mapColor(MapColor.SNOW)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .lightLevel(state -> 4)
                    .sound(SoundType.GRASS)));

    /** A crosta da mácula: o que sobra de um tronco ou de uma folha que ela tomou. */
    public static final Block TAINT_CRUST = register("taint_crust", properties ->
            new net.thaumcraft.block.TaintBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6f)
                    .randomTicks()
                    .sound(SoundType.SLIME_BLOCK)));

    /** O solo maculado: a terra que ela tomou. */
    public static final Block TAINT_SOIL = register("taint_soil", properties ->
            new net.thaumcraft.block.TaintBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6f)
                    .randomTicks()
                    .sound(SoundType.SLIME_BLOCK)));

    /** As fibras da mácula: o mato roxo que nasce por cima do que ela tomou. */
    public static final Block TAINT_FIBRES = register("taint_fibres", properties ->
            new net.thaumcraft.block.TaintFibreBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    /** A Flor Etérea: a única coisa que faz a mácula recuar. */
    public static final Block ETHEREAL_BLOOM = register("ethereal_bloom", properties ->
            new net.thaumcraft.block.EtherealBloomBlock(properties
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .randomTicks()
                    .lightLevel(state -> 7)
                    .sound(SoundType.GRASS)));

    /** A bancada arcana: a mesa que a varinha benzeu. */
    public static final Block ARCANE_WORKBENCH = register("arcane_workbench", properties ->
            new net.thaumcraft.block.ArcaneWorkbenchBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    /** Os blocos de construção do mod. */
    public static final java.util.Map<String, Block> BUILDING = new java.util.LinkedHashMap<>();

    static {
        for (String name : TCBuilding.NAMES) {
            BUILDING.put(name, register(name, properties -> new Block(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))));
        }
    }

    private TCBlocks() {
    }

    private static Block register(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
