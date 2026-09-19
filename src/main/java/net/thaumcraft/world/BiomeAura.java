package net.thaumcraft.world;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Quanta aura cada tipo de terra tem, e de que ela é feita: o {@code BiomeHandler} da 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia4-aura.js} a partir do {@code registerBiomeInfo} do
 * {@code Config} do jar. Os tipos do dicionário de biomas do Forge viraram as marcas de convenção do Fabric, que
 * copiam os mesmos tipos. Terra sem marca nenhuma vale cem, como no original.
 */
public final class BiomeAura {
    /** Uma marca de terra: quanta aura ela carrega e de que aspecto (a mágica não tem). */
    public record Land(TagKey<Biome> tag, int aura, @Nullable Aspect aspect) {
    }

    /** Quanto vale a terra que não cai em marca nenhuma, ou cai numa que a tabela não conhece. */
    public static final int DEFAULT_AURA = 100;

    public static final List<Land> LANDS = List.of(
            new Land(ConventionalBiomeTags.IS_AQUATIC, 100, Aspects.WATER),
            new Land(ConventionalBiomeTags.IS_OCEAN, 120, Aspects.WATER),
            new Land(ConventionalBiomeTags.IS_RIVER, 100, Aspects.WATER),
            new Land(ConventionalBiomeTags.IS_WET, 80, Aspects.WATER),
            new Land(ConventionalBiomeTags.IS_HOT, 100, Aspects.FIRE),
            new Land(ConventionalBiomeTags.IS_DESERT, 100, Aspects.FIRE),
            new Land(ConventionalBiomeTags.IS_NETHER, 120, Aspects.FIRE),
            new Land(ConventionalBiomeTags.IS_BADLANDS, 80, Aspects.FIRE),
            new Land(ConventionalBiomeTags.IS_VEGETATION_DENSE, 100, Aspects.ORDER),
            new Land(ConventionalBiomeTags.IS_SNOWY, 80, Aspects.ORDER),
            new Land(ConventionalBiomeTags.IS_COLD, 80, Aspects.ORDER),
            new Land(ConventionalBiomeTags.IS_ICY, 100, Aspects.ORDER),
            new Land(ConventionalBiomeTags.IS_MUSHROOM, 140, Aspects.ORDER),
            new Land(ConventionalBiomeTags.IS_CONIFEROUS_TREE, 100, Aspects.EARTH),
            new Land(ConventionalBiomeTags.IS_FOREST, 120, Aspects.EARTH),
            new Land(ConventionalBiomeTags.IS_SANDY, 80, Aspects.EARTH),
            new Land(ConventionalBiomeTags.IS_BEACH, 80, Aspects.EARTH),
            new Land(ConventionalBiomeTags.IS_SAVANNA, 80, Aspects.AIR),
            new Land(ConventionalBiomeTags.IS_MOUNTAIN, 100, Aspects.AIR),
            new Land(ConventionalBiomeTags.IS_HILL, 120, Aspects.AIR),
            new Land(ConventionalBiomeTags.IS_PLAINS, 80, Aspects.AIR),
            new Land(ConventionalBiomeTags.IS_DRY, 80, Aspects.ENTROPY),
            new Land(ConventionalBiomeTags.IS_VEGETATION_SPARSE, 80, Aspects.ENTROPY),
            new Land(ConventionalBiomeTags.IS_SWAMP, 120, Aspects.ENTROPY),
            new Land(ConventionalBiomeTags.IS_WASTELAND, 80, Aspects.ENTROPY),
            new Land(ConventionalBiomeTags.IS_JUNGLE, 100, Aspects.PLANT),
            new Land(ConventionalBiomeTags.IS_LUSH, 100, Aspects.PLANT),
            new Land(ConventionalBiomeTags.IS_MAGICAL, 100, null),
            new Land(ConventionalBiomeTags.IS_END, 80, Aspects.VOID),
            new Land(ConventionalBiomeTags.IS_SPOOKY, 80, Aspects.SOUL),
            new Land(ConventionalBiomeTags.IS_DEAD, 50, Aspects.DEATH)
    );

    private BiomeAura() {
    }

    private static List<Land> landsOf(Holder<Biome> biome) {
        List<Land> found = new ArrayList<>();
        for (Land land : LANDS) if (biome.is(land.tag())) found.add(land);
        return found;
    }

    /** O {@code getBiomeAura}: a média das marcas que a terra tiver. */
    public static int auraOf(Holder<Biome> biome) {
        List<Land> found = landsOf(biome);
        if (found.isEmpty()) return DEFAULT_AURA;
        int total = 0;
        for (Land land : found) total += land.aura();
        return total / found.size();
    }

    /** O {@code getRandomBiomeTag}: o aspecto de uma das marcas, sorteada (a mágica não dá nenhum). */
    @Nullable
    public static Aspect aspectOf(Holder<Biome> biome, net.minecraft.util.RandomSource random) {
        List<Land> found = landsOf(biome);
        return found.isEmpty() ? null : found.get(random.nextInt(found.size())).aspect();
    }
}
