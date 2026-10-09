package net.thaumcraft.occulta.torment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O que é do inferno</b>: o {@code CreatureUtil.isDemonic} e a {@code DemonicDamageSource} do Witchery.
 *
 * <p>São duas coisas que andam sempre juntas e que até aqui estavam espalhadas por três lugares, cada um
 * com a sua lista curta: o <b>Virote Sagrado</b>, que bate mais em coisa do inferno; o <b>Espírito
 * Fluente</b>, que a castiga; e agora o <b>Senhor do Tormento</b>, que a <b>é</b>.
 *
 * <h2>Quem é</h2>
 *
 * <p>A lista do original é o Demônio, o Ghast, o Blaze, o Cubo de Magma, o Leonard, o Senhor do Tormento, o
 * Diabrete, a Lilith e o Wither. <b>Oito dos nove estão portados</b> — falta o Leonard — e passam a estar
 * todos num <b>rótulo</b>, que é onde uma lista de bichos mora hoje. Isso fecha os dois desvios declarados
 * que diziam que o Demônio, o Diabrete e a Lilith não contavam porque não estavam portados: já estão.
 *
 * <h2>E o golpe</h2>
 *
 * <p>O dano demoníaco do original é mágico, <b>passa pela armadura</b>, passa pelos efeitos e passa pelos
 * encantamentos — o {@code setDamageBypassesArmor} mais o {@code setMagicDamage} mais o
 * {@code setDamageIsAbsolute}. Aqui é um tipo de dano próprio posto nos três rótulos que o jogo tem para
 * isso.
 *
 * <p>E ele importa duas vezes: é o que a <b>bola de fogo de alma</b> faz em quem acerta, e é o <b>único</b>
 * golpe de que o Senhor do Tormento apanha oito em vez de cinco.
 */
public final class Demonic {
    /** O rótulo de quem é do inferno. */
    public static final TagKey<EntityType<?>> MARCA =
            TagKey.create(Registries.ENTITY_TYPE, Thaumcraft.id("demonic"));

    /** E o tipo de dano. */
    public static final ResourceKey<DamageType> DANO =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("demonic"));

    private Demonic() {
    }

    /** Se aquilo é do inferno: o {@code isDemonic}. Gente nunca é, mesmo amaldiçoada. */
    public static boolean é(@Nullable Entity quem) {
        if (quem == null || quem instanceof Player) return false;
        return quem.getType().builtInRegistryHolder().is(MARCA);
    }

    /** A fonte do golpe demoníaco, com quem o deu. */
    public static DamageSource fonte(ServerLevel level, @Nullable Entity quem) {
        return new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DANO), quem);
    }

    /** Se aquele golpe é demoníaco. */
    public static boolean oGolpeÉ(DamageSource fonte) {
        return fonte.is(DANO);
    }

    /** Bate. */
    public static boolean bate(ServerLevel level, @Nullable Entity quem, LivingEntity noquê, float quanto) {
        return noquê.hurtServer(level, fonte(level, quem), quanto);
    }
}
