package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>homem-porco zumbi</b>: a {@code CreaturePowerPigMan} do Witchery.
 *
 * <p>Usado, dá <b>Resistência III e Força III</b> por trinta segundos — o dobro do zumbi em cada uma.
 *
 * <p>Mas o que ele faz de melhor é passivo e custa carga de <b>infusão</b>, e não de bicho: quem o tem,
 * ao <b>pegar fogo</b>, gasta <b>três</b> e ganha <b>Resistência ao Fogo por um minuto</b> — e o golpe que
 * ia levar é <b>engolido</b>.
 *
 * <p>Quer dizer que um homem-porco no bolso é lava de graça enquanto houver carga. É o poder que torna o
 * Nether andável.
 */
public class PigManPower extends CreaturePower {
    /** Quanto tempo as duas duram. */
    public static final int DURA = 600;

    /** E a resistência ao fogo que ele dá de presente. */
    public static final int CONTRA_O_FOGO = 1200;

    /** O que o fogo lhe custa, em carga de infusão. */
    public static final int CUSTO = 3;

    public PigManPower(int id) {
        super(id, EntityTypes.ZOMBIFIED_PIGLIN);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURA, 2));
        quem.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURA, 2));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
    }

    @Override
    public boolean levouGolpe(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        if (!fonte.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return false;
        if (Infusions.energia(quem) < CUSTO) return false;
        Infusions.põeEnergia(quem, Infusions.energia(quem) - CUSTO);
        quem.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, CONTRA_O_FOGO, 0));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
        return true;
    }
}
