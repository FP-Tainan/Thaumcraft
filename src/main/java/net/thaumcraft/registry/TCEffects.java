package net.thaumcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.thaumcraft.Thaumcraft;

/** Os efeitos do Thaumcraft 4.2.3.5, com as cores dos {@code Potion} do {@code Config}. */
public final class TCEffects {
    /** A proteção contra a dobra ({@code PotionWarpWard}): o fluido purificante a dá; segura os efeitos da dobra. */
    public static final Holder<MobEffect> WARP_WARD = register("warp_ward", new MobEffect(MobEffectCategory.BENEFICIAL, 14742263) {
    });

    private TCEffects() {
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumcraft.id(name), effect);
    }

    public static void init() {
    }
}
