package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do que <b>salta</b> — cubo de magma, slime e sapo: a {@code CreaturePowerJump} do Witchery.
 *
 * <p>Usado, dá <b>Impulso IV por vinte segundos</b>. E, sem se usar, dá duas coisas de graça: segurando o
 * <b>pular</b> no ar, se sobe um pouco mais; e a queda <b>não conta de todo</b>.
 *
 * <p>A queda que não conta é o que separa este poder do morcego: o morcego trava a queda em cinco, este
 * <b>a apaga</b>. Com um slime no bolso não há altura que mate.
 */
public class JumpPower extends CreaturePower {
    /** Quanto o impulso dura. */
    public static final int SALTA = 400;

    /** E quanto o pular empurra. */
    public static final double EMPURRA = 0.06;

    public JumpPower(int id, EntityType<?> dequê) {
        super(id, dequê);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, SALTA, 3));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
    }

    @Override
    public void batida(Player quem) {
        if (!BatPower.pulando(quem)) return;
        Vec3 anda = quem.getDeltaMovement();
        if (anda.y > 0.0) quem.setDeltaMovement(anda.x, anda.y + EMPURRA, anda.z);
    }

    /** E ele não cai: o original põe a distância a zero e toca o baque do slime grande. */
    @Override
    public float cai(ServerPlayer quem, float distância) {
        if (distância > 3.0f) {
            quem.level().playSound(null, quem.getX(), quem.getY(), quem.getZ(),
                    SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return 0.0f;
    }
}
