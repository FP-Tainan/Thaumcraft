package net.thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;

/** Os tipos de dano do {@code DamageSourceThaumcraft} (os arquivos ficam em {@code data/thaumcraft/damage_type}). */
public final class TCDamageTypes {
    /** O {@code dissolve}: a morte líquida. */
    public static final ResourceKey<DamageType> DISSOLVE = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("dissolve"));

    /** O {@code taint}: o fluxo da mácula. */
    public static final ResourceKey<DamageType> TAINT = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("taint"));

    private TCDamageTypes() {
    }

    public static DamageSource dissolve(Level level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DISSOLVE));
    }

    public static DamageSource taint(Level level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(TAINT));
    }
}
