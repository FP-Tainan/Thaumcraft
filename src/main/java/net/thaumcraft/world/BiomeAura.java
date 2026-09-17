package net.thaumcraft.world;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;

import java.util.List;

/**
 * Quanta aura cada tipo de terra tem, e de que ela é feita.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia4-aura.js} a partir da tabela do
 * {@code Config} do mod original. Os números e os aspectos são os de lá.
 *
 * <p>Uma coisa não tem como ser igual: o original usava o dicionário de biomas do Forge, que tinha
 * marcas como WET, HOT, DENSE e MAGICAL. O Minecraft de hoje só tem parte delas. Ficou o que
 * sobreviveu, com os valores do original; terra que não cai em marca nenhuma vale cem, que é
 * exatamente o que o original devolvia quando não reconhecia o bioma.
 */
public final class BiomeAura {
    /** Uma marca de terra: quanta aura ela carrega e de que aspecto. */
    public record Land(TagKey<Biome> tag, int aura, Aspect aspect) {
    }

    /** Quanto vale a terra que não cai em marca nenhuma. */
    public static final int DEFAULT_AURA = 100;

    public static final List<Land> LANDS = List.of(
            new Land(BiomeTags.IS_OCEAN, 120, Aspects.WATER),
            new Land(BiomeTags.IS_RIVER, 100, Aspects.WATER),
            new Land(BiomeTags.IS_NETHER, 120, Aspects.FIRE),
            new Land(BiomeTags.IS_BADLANDS, 80, Aspects.FIRE),
            new Land(BiomeTags.IS_TAIGA, 100, Aspects.EARTH),
            new Land(BiomeTags.IS_FOREST, 120, Aspects.EARTH),
            new Land(BiomeTags.IS_BEACH, 80, Aspects.EARTH),
            new Land(BiomeTags.IS_SAVANNA, 80, Aspects.AIR),
            new Land(BiomeTags.IS_MOUNTAIN, 100, Aspects.AIR),
            new Land(BiomeTags.IS_HILL, 120, Aspects.AIR),
            new Land(BiomeTags.IS_JUNGLE, 100, Aspects.PLANT),
            new Land(BiomeTags.IS_END, 80, Aspects.VOID)
    );

    private BiomeAura() {
    }

    /** Quanta aura tem esta terra: a média das marcas que ela tiver, como no original. */
    public static int auraOf(Holder<Biome> biome) {
        int total = 0;
        int count = 0;
        for (Land land : LANDS) {
            if (!biome.is(land.tag())) continue;
            total += land.aura();
            count++;
        }
        return count > 0 ? total / count : DEFAULT_AURA;
    }

    /** De que a aura desta terra é feita, sorteando entre as marcas dela. */
    public static Aspect aspectOf(Holder<Biome> biome, net.minecraft.util.RandomSource random) {
        List<Land> found = LANDS.stream().filter(land -> biome.is(land.tag())).toList();
        return found.isEmpty() ? null : found.get(random.nextInt(found.size())).aspect();
    }
}
