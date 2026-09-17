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

    private TCFeatures() {
    }

    public static final ResourceKey<PlacedFeature> INFUSED_STONE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.id("infused_stone"));

    public static void init() {
        // os veios de pedra infundida, de onde saem os fragmentos
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
                INFUSED_STONE_PLACED);
        // um nó a cada trinta e seis pedaços de mundo, que é a raridade do original
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                NODE_PLACED);
    }
}
