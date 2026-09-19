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

    private TCFeatures() {
    }

    /** Os minérios do mod: cinábrio, âmbar e os veios de pedra infundida, como no generateOres. */
    public static final Feature<NoneFeatureConfiguration> THAUM_ORES = Registry.register(BuiltInRegistries.FEATURE,
            Thaumcraft.id("thaum_ores"), new net.thaumcraft.world.ThaumOresFeature(NoneFeatureConfiguration.CODEC));
    public static final ResourceKey<PlacedFeature> THAUM_ORES_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("thaum_ores"));

    public static void init() {
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
