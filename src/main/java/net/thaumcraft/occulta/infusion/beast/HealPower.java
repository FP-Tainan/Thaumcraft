package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder dos <b>bichos de capoeira</b>: a {@code CreaturePowerHeal} do Witchery.
 *
 * <p>Cura meio coração. E é tudo.
 *
 * <p>A graça está no preço: uma ovelha, uma vaca, uma galinha ou um porco dão <b>uma carga</b>, e um aldeão
 * ou uma cogumelada dão <b>duas</b> — contra as dez de qualquer bicho que custe a apanhar. Matar uma ovelha
 * para se curar meio coração é um péssimo negócio, e o original quis que fosse.
 *
 * <p>São <b>seis dos vinte e cinco</b> poderes, o que diz quanto do ramo é feito de dizer «este não vale a
 * pena».
 */
public class HealPower extends CreaturePower {
    private final int porBicho;

    public HealPower(int id, EntityType<?> dequê, int porBicho) {
        super(id, dequê);
        this.porBicho = porBicho;
    }

    @Override
    public int porBicho() {
        return this.porBicho;
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 10, 0));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
    }
}
