package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;
import org.jetbrains.annotations.Nullable;

/**
 * O poder da <b>lula</b>: a {@code CreaturePowerSquid} do Witchery.
 *
 * <p>Usado em quem se estiver olhando, <b>o cega por dez segundos</b>. É a tinta, e é a única arma da lista
 * que não faz dano nenhum.
 *
 * <p>E ele faz mais duas coisas sem se usar:
 *
 * <ul>
 *   <li>na água, segurando o <b>andar para a frente</b>, nada <b>quinze por cento mais depressa</b>;</li>
 *   <li>e, <b>afogando-se</b>, gasta <b>uma</b> carga de infusão e ganha <b>Respiração Aquática II por um
 *       minuto</b> e trezentas batidas de fôlego — e o golpe é engolido.</li>
 * </ul>
 *
 * <p>Esse último faz da lula o poder que impede o afogamento enquanto houver carga, e com duzentas cargas
 * isso é bastante fundo.
 */
public class SquidPower extends CreaturePower {
    /** Quanto a cegueira dura. */
    public static final int CEGA = 200;

    /** O que o afogamento custa, em carga de infusão. */
    public static final int CUSTO = 1;

    /** E o que ele dá em troca. */
    public static final int RESPIRA = 1200;
    public static final int FÔLEGO = 300;

    /** Quanto ele ajuda a nadar. */
    public static final double NADA = 1.15;

    public SquidPower(int id) {
        super(id, EntityTypes.SQUID);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        if (!(onde instanceof EntityHitResult bateu)
                || !(bateu.getEntity() instanceof LivingEntity bicho)) {
            Infusion.falha(level, quem);
            return;
        }
        bicho.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, CEGA, 0));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
    }

    @Override
    public boolean levouGolpe(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        if (!fonte.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) return false;
        if (Infusions.energia(quem) < CUSTO) return false;
        Infusions.põeEnergia(quem, Infusions.energia(quem) - CUSTO);
        quem.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, RESPIRA, 1));
        quem.setAirSupply(FÔLEGO);
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
        return true;
    }

    /** E na água ele nada mais depressa, se estiver andando para a frente. */
    @Override
    public void batida(Player quem) {
        if (!quem.isInWater() || quem.zza <= 0.0f) return;
        quem.setDeltaMovement(quem.getDeltaMovement().multiply(NADA, 1.0, NADA));
    }
}
