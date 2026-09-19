package net.thaumcraft.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.thaumcraft.world.TCBiomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Floresta Mágica e a Terra Maculada no mundo novo: o {@code BiomeManager.addBiome} da 4.2.3.5, que as punha entre os
 * biomas quentes e frios com peso cinco e dois.
 *
 * <p>O mundo de hoje não sorteia biomas por peso: ele os escolhe por clima. Aqui elas ficam com a metade "estranha"
 * (a que o jogo usa para as variantes, como a floresta florida) de climas em que o jogo não tem variante: a floresta
 * fria e a temperada viram, metade das vezes, Floresta Mágica; a planície fria, metade das vezes, Terra Maculada. A
 * proporção fica perto da do original — a mágica duas vezes e meia mais comum que a maculada.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
    @Inject(method = "pickMiddleBiome", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$magicalBiomes(int temperature, int humidity, Climate.Parameter weirdness,
                                          CallbackInfoReturnable<ResourceKey<Biome>> result) {
        if (weirdness.max() < 0L) return;
        if (humidity == 2 && (temperature == 1 || temperature == 2)) result.setReturnValue(TCBiomes.MAGICAL_FOREST);
        else if (humidity == 1 && temperature == 1) result.setReturnValue(TCBiomes.TAINTED_LAND);
    }
}
