package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.fluid.LiquidDeathFluid;
import net.thaumcraft.fluid.PurifyingFluid;

/** Os fluidos do Thaumcraft 4.2.3.5: o purificante e a morte líquida (fonte e a parte que corre de cada um). */
public final class TCFluids {
    public static final FlowingFluid PURIFYING = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("purifying_fluid"),
            new PurifyingFluid.Source());
    public static final FlowingFluid PURIFYING_FLOWING = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("flowing_purifying_fluid"),
            new PurifyingFluid.Flowing());
    public static final FlowingFluid DEATH = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("liquid_death"),
            new LiquidDeathFluid.Source());
    public static final FlowingFluid DEATH_FLOWING = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("flowing_liquid_death"),
            new LiquidDeathFluid.Flowing());

    private TCFluids() {
    }

    public static void init() {
    }
}
