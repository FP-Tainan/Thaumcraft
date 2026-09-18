package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * As árvores mágicas pelo mundo: o {@code generateGreatwood} e o {@code generateSilverwood} do
 * {@code ThaumcraftWorldGenerator} da 4.2.3.5.
 *
 * <p>A cada pedaço de mundo, uma chance em vinte e cinco de tentar uma grande-madeira e uma em sessenta de
 * tentar um pinheiro-de-prata, num ponto qualquer do pedaço, na altura do que estiver mais alto ali — em
 * cima de copa não pega, e aí a árvore não sai. A grande-madeira ainda tira a sorte do bioma: certa nas
 * florestas, uma em cinco nas taigas, savanas, planícies e pântanos, meio a meio nas terras viçosas. O
 * pinheiro-de-prata só nasce em terra mágica e nos morros de floresta e de bétula do jogo antigo, que o de
 * hoje fundiu na floresta e na floresta de bétulas.
 */
public class MagicalTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean silverwood;

    public MagicalTreeFeature(Codec<NoneFeatureConfiguration> codec, boolean silverwood) {
        super(codec);
        this.silverwood = silverwood;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        Holder<Biome> biome = level.getBiome(pos);
        if (this.silverwood) {
            if (!silverwoodGrows(biome)) return false;
            return SilverwoodTree.generate(level, random, pos, 7, 4, true);
        }
        if (!(greatwoodChance(biome) > random.nextFloat())) return false;
        return GreatwoodTree.generate(level, random, pos, true, random.nextInt(8) == 0);
    }

    /**
     * O {@code getBiomeSupportsGreatwood}: a chance da primeira marca de terra que aceita grande-madeira. As
     * marcas são as de convenção do Fabric, que copiam o dicionário de biomas do Forge.
     */
    public static float greatwoodChance(Holder<Biome> biome) {
        if (biome.is(ConventionalBiomeTags.IS_TAIGA)) return 0.2f;
        if (biome.is(ConventionalBiomeTags.IS_SWAMP)) return 0.2f;
        if (biome.is(ConventionalBiomeTags.IS_SAVANNA)) return 0.2f;
        if (biome.is(BiomeTags.IS_FOREST)) return 1.0f;
        if (biome.is(ConventionalBiomeTags.IS_LUSH)) return 0.5f;
        if (biome.is(ConventionalBiomeTags.IS_PLAINS)) return 0.2f;
        return 0.0f;
    }

    public static boolean silverwoodGrows(Holder<Biome> biome) {
        return biome.is(ConventionalBiomeTags.IS_MAGICAL) || biome.is(Biomes.FOREST) || biome.is(Biomes.BIRCH_FOREST);
    }
}
