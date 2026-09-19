package net.thaumcraft.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.Thaumcraft;

/**
 * Os biomas do mod: a Floresta Mágica, a Terra Maculada e o Sinistro (o {@code BiomeGenMagicalForest}, o
 * {@code BiomeGenTaint} e o {@code BiomeGenEerie}). Os dados de cada um ficam em {@code data/thaumcraft/worldgen/biome};
 * o Sinistro não nasce sozinho, só onde um nó sombrio o pinta.
 */
public final class TCBiomes {
    public static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
    public static final ResourceKey<Biome> TAINTED_LAND = key("tainted_land");
    public static final ResourceKey<Biome> EERIE = key("eerie");

    private TCBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Thaumcraft.id(name));
    }
}
