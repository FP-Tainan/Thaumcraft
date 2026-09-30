package net.thaumcraft.arcana;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;

/**
 * Os tipos de dano do Ars Arcana: o {@code am2.damage.DamageSources} do Ars Magica 2.
 *
 * <p>Eles existem por duas razões. A primeira é a que se lê: <b>quem morre de um feitiço de gelo não morreu
 * afogado</b>, e o texto que aparece no chat é o do original, traduzido. A segunda é de regra: o Ars Magica 2
 * marca todos eles como <b>impossíveis de aparar</b> — um escudo não para um raio —, e alguns deles
 * <b>atravessam a armadura</b>.
 *
 * <p>Os arquivos ficam em {@code data/thaumcraft/damage_type}, que é onde o jogo de hoje os quer.
 */
public final class ArcanaDamage {
    /** O {@code am2.fire}: queima, e atravessa a armadura. */
    public static final ResourceKey<DamageType> FIRE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_fire"));

    /** O {@code am2.frost}: congela, e atravessa a armadura. */
    public static final ResourceKey<DamageType> FROST =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_frost"));

    /** O {@code am2.lightning}: eletrocuta, e atravessa a armadura. */
    public static final ResourceKey<DamageType> LIGHTNING =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_lightning"));

    /** O {@code am2.wind}: o sopro que leva. */
    public static final ResourceKey<DamageType> WIND =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_wind"));

    /** O {@code am2.holy}: a luz que desfaz morto-vivo. */
    public static final ResourceKey<DamageType> HOLY =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_holy"));

    /** O afogar de fora d'água: o {@code causeEntityDrownDamage}. */
    public static final ResourceKey<DamageType> DROWN =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("aa_drown"));

    private ArcanaDamage() {
    }

    private static DamageSource de(Level level, ResourceKey<DamageType> qual, LivingEntity quem) {
        return new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(qual), quem);
    }

    public static DamageSource fire(Level level, LivingEntity quem) {
        return de(level, FIRE, quem);
    }

    public static DamageSource frost(Level level, LivingEntity quem) {
        return de(level, FROST, quem);
    }

    public static DamageSource lightning(Level level, LivingEntity quem) {
        return de(level, LIGHTNING, quem);
    }

    public static DamageSource wind(Level level, LivingEntity quem) {
        return de(level, WIND, quem);
    }

    public static DamageSource holy(Level level, LivingEntity quem) {
        return de(level, HOLY, quem);
    }

    public static DamageSource drown(Level level, LivingEntity quem) {
        return de(level, DROWN, quem);
    }
}
