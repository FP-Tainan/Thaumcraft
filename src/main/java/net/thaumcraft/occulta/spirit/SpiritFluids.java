package net.thaumcraft.occulta.spirit;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.thaumcraft.Thaumcraft;

/** Os dois líquidos do Ars Occulta, cada um na fonte e na parte que corre. */
public final class SpiritFluids {
    public static final FlowingFluid FLOWING_SPIRIT = Registry.register(BuiltInRegistries.FLUID,
            Thaumcraft.id("flowing_spirit"), new SpiritFluid.Spirit.Source());
    public static final FlowingFluid FLOWING_SPIRIT_FLOWING = Registry.register(BuiltInRegistries.FLUID,
            Thaumcraft.id("flowing_flowing_spirit"), new SpiritFluid.Spirit.Flowing());

    public static final FlowingFluid HOLLOW_TEARS = Registry.register(BuiltInRegistries.FLUID,
            Thaumcraft.id("hollow_tears"), new SpiritFluid.Tears.Source());
    public static final FlowingFluid HOLLOW_TEARS_FLOWING = Registry.register(BuiltInRegistries.FLUID,
            Thaumcraft.id("flowing_hollow_tears"), new SpiritFluid.Tears.Flowing());

    private SpiritFluids() {
    }

    public static void init() {
    }
}
