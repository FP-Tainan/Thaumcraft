package net.thaumcraft.occulta.ghost;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>toque da morte</b>: o {@code EntityUtil.touchOfDeath} do Witchery.
 *
 * <p>É como os fantasmas machucam, e não se parece com nenhum outro golpe do jogo: ele <b>não passa pela
 * armadura</b>, não passa pela Resistência, não passa pelo Escudo Rúnico. O original faz isso da maneira
 * mais crua que há — ele <b>põe a vida mais baixa à mão</b> e só depois avisa o jogo de que houve dano, com
 * zero de dano, para o grito e a animação saírem.
 *
 * <p>Aqui é um <b>tipo de dano próprio</b>, posto nos rótulos que o jogo já tem para «não passa pela
 * armadura», «não passa pelos efeitos» e «não passa pelos encantamentos». O efeito é o mesmo e o caminho é
 * honesto: o dano é dano, e não uma subtração escondida.
 *
 * <p><b>E ele não pega em quem está no criativo</b>, que é a única exceção que o original escreve.
 */
public final class TouchOfDeath {
    /** O tipo de dano dos fantasmas. */
    public static final ResourceKey<DamageType> QUAL =
            ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("touch_of_death"));

    private TouchOfDeath() {
    }

    /** A fonte dele, com quem o deu. */
    public static DamageSource fonte(ServerLevel level, @Nullable Entity quem) {
        return new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(QUAL), quem);
    }

    /**
     * <b>Toca.</b>
     *
     * @return se pegou
     */
    public static boolean toca(ServerLevel level, @Nullable Entity quem, LivingEntity noquê, float quanto) {
        if (noquê.isInvulnerable()) return false;
        if (noquê instanceof Player gente && gente.getAbilities().invulnerable) return false;
        return noquê.hurtServer(level, fonte(level, quem), quanto);
    }
}
