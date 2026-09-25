package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.world.NodeFeature;

/** O que o mod semeia pelo mundo. */
public final class TCFeatures {
    /** O nó de aura, que nasce sozinho pelo mundo como no original. */
    public static final Feature<NoneFeatureConfiguration> NODE = Registry.register(
            BuiltInRegistries.FEATURE, Thaumcraft.id("node"), new NodeFeature(NoneFeatureConfiguration.CODEC));

    public static final ResourceKey<PlacedFeature> NODE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("node"));

    /** A grande-madeira e o pinheiro-de-prata. */
    public static final Feature<NoneFeatureConfiguration> GREATWOOD = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("greatwood"), new net.thaumcraft.world.MagicalTreeFeature(NoneFeatureConfiguration.CODEC, false));
    public static final Feature<NoneFeatureConfiguration> SILVERWOOD = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("silverwood"), new net.thaumcraft.world.MagicalTreeFeature(NoneFeatureConfiguration.CODEC, true));

    /** As pérolas de cinzas do deserto. */
    public static final Feature<NoneFeatureConfiguration> CINDERPEARL = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("cinderpearl"), new net.thaumcraft.world.CinderpearlFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> CINDERPEARL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("cinderpearl"));

    public static final ResourceKey<PlacedFeature> GREATWOOD_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("greatwood"));
    public static final ResourceKey<PlacedFeature> SILVERWOOD_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("silverwood"));

    /** O que os biomas do mod semeiam: vagens de mana, cogumelos-vis, as árvores da Floresta Mágica e as fibras. */
    public static final Feature<NoneFeatureConfiguration> MANA_PODS = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("mana_pods"), new net.thaumcraft.world.MagicalForestFeatures.ManaPods(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> VISHROOMS = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("vishrooms"), new net.thaumcraft.world.MagicalForestFeatures.Vishrooms(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> TALL_SILVERWOOD = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("tall_silverwood"), new net.thaumcraft.world.MagicalForestFeatures.TallSilverwood(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> FOREST_GREATWOOD = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("forest_greatwood"), new net.thaumcraft.world.MagicalForestFeatures.ForestGreatwood(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> TAINT_FIBRES = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("taint_fibres"), new net.thaumcraft.world.MagicalForestFeatures.TaintFibres(NoneFeatureConfiguration.CODEC));

    private TCFeatures() {
    }

    /** Os minérios do mod: cinábrio, âmbar e os veios de pedra infundida, como no generateOres. */
    public static final Feature<NoneFeatureConfiguration> THAUM_ORES = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("thaum_ores"), new net.thaumcraft.world.ThaumOresFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> THAUM_ORES_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("thaum_ores"));

    /** As ruínas do mundo de cima: túmulos, anéis eldritch, pedras do topo e totens de obsidiana. */
    public static final Feature<NoneFeatureConfiguration> RUINS = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("ruins"), new net.thaumcraft.world.RuinsFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> RUINS_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("ruins"));

    /** As fendas que já estavam no mundo, raras, num oco debaixo da terra. */
    public static final Feature<NoneFeatureConfiguration> RIFT = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("rift"), new net.thaumcraft.shattered.RiftFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> RIFT_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("rift"));

    /** O gerador do Limbo: terra de tecido desfiado sobre um chão de tecido eterno. */
    public static final com.mojang.serialization.MapCodec<net.thaumcraft.shattered.LimboChunkGenerator> LIMBO_GENERATOR =
            Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Thaumcraft.id("limbo"),
                    net.thaumcraft.shattered.LimboChunkGenerator.CODEC);

    /** O altar antigo do Crimson Warfare: um disco de pedra arcana com o pedestal no meio. */
    public static final Feature<NoneFeatureConfiguration> ANCIENT_ALTAR = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("ancient_altar"), new net.thaumcraft.crimson.AncientAltarFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> ANCIENT_ALTAR_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("ancient_altar"));

    /** O gerador das Terras de Fora: chunks vazios, com os recursos do bioma. */
    public static final com.mojang.serialization.MapCodec<net.thaumcraft.world.outer.OuterChunkGenerator> OUTER_GENERATOR = Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR, Thaumcraft.id("outer"), net.thaumcraft.world.outer.OuterChunkGenerator.CODEC);

    /** O labirinto das Terras de Fora: cada chunk que é casa dele vira a sala da casa. */
    public static final Feature<NoneFeatureConfiguration> MAZE = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("maze"), new net.thaumcraft.world.outer.MazeFeature());

    public static void init() {
        // as ruínas: uma tentativa por pedaço, como o generateSurface
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.SURFACE_STRUCTURES,
                RUINS_PLACED);
        // o altar antigo do Crimson Warfare: uma tentativa por pedaço, e uma em mil dá certo
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.SURFACE_STRUCTURES,
                ANCIENT_ALTAR_PLACED);
        // as fendas que já estavam lá
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_DECORATION,
                RIFT_PLACED);
        // os minérios: cinábrio, âmbar e os veios de pedra infundida, de onde saem os fragmentos
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                THAUM_ORES_PLACED);
        // um nó a cada trinta e seis pedaços de mundo, que é a raridade do original
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                NODE_PLACED);
        // as árvores mágicas; a folha-cintilante nasce em volta do pé do pinheiro-de-prata
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                SILVERWOOD_PLACED);
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                GREATWOOD_PLACED);
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                CINDERPEARL_PLACED);
    }
}
