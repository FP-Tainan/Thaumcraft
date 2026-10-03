package net.thaumcraft.occulta.vampire;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.thaumcraft.Thaumcraft;

/**
 * As duas dores que só um vampiro sente: o {@code DamageSourceVampireFire} do Witchery e o sol.
 *
 * <p>Elas existem à parte porque <b>nenhuma armadura e nenhuma poção as apara</b>. O fogo de um vampiro
 * atravessa a Resistência ao Fogo — é a piada cruel do original: a poção que salva todo mundo é inútil
 * justamente para quem mais arde. E o sol não fere: <b>mata</b>.
 */
public final class VampireHurt {
    public static final ResourceKey<DamageType> FOGO =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("vampire_fire"));
    public static final ResourceKey<DamageType> SOL =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("vampire_sun"));

    private VampireHurt() {
    }

    /** O fogo que arde num vampiro mesmo com a poção. */
    public static DamageSource fogo(ServerLevel level) {
        return level.damageSources().source(FOGO);
    }

    /** E o sol, que não fere: mata. */
    public static DamageSource sol(ServerLevel level) {
        return level.damageSources().source(SOL);
    }
}
