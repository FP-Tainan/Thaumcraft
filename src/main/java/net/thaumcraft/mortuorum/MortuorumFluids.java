package net.thaumcraft.mortuorum;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.thaumcraft.Thaumcraft;

/** O fluido do Ars Mortuorum: o sangue, na fonte e na parte que corre. */
public final class MortuorumFluids {
    public static final FlowingFluid BLOOD = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("blood"),
            new BloodFluid.Source());
    public static final FlowingFluid BLOOD_FLOWING = Registry.register(BuiltInRegistries.FLUID, Thaumcraft.id("flowing_blood"),
            new BloodFluid.Flowing());

    private MortuorumFluids() {
    }

    public static void init() {
    }
}
