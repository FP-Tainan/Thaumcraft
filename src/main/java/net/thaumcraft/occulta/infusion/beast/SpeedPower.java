package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do que <b>corre</b> — peixinho-de-prata, jaguatirica, lobo e cavalo: a {@code CreaturePowerSpeed}
 * do Witchery.
 *
 * <p>Usado, dá <b>Velocidade IV por vinte segundos</b>. E, sem se usar, quem o tem anda <b>quarenta e cinco
 * por cento mais depressa</b> enquanto estiver andando no chão e fora da água.
 *
 * <p>Esse acréscimo de graça é maior do que o da poção, e não acaba. É o poder que quem anda muito escolhe,
 * e os quatro bichos dele são dos mais fáceis de apanhar — o que é a troca ao contrário da do ghast.
 *
 * <p><b>No gelo ele quase não ajuda:</b> um décimo em vez de quarenta e cinco centésimos. O original
 * escreveu a exceção à mão, e ela é sensata — correr no gelo já é depressa de mais.
 */
public class SpeedPower extends CreaturePower {
    /** Quanto a velocidade dura. */
    public static final int CORRE = 400;

    /** Quanto ele empurra no chão, e quanto no gelo. */
    public static final double EMPURRA = 1.45;
    public static final double NO_GELO = 1.1;

    public SpeedPower(int id, EntityType<?> dequê) {
        super(id, dequê);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.SPEED, CORRE, 3));
        Infusion.toca(level, quem, SoundEvents.FIRE_EXTINGUISH);
    }

    @Override
    public void batida(Player quem) {
        if (quem.zza == 0.0f && quem.xxa == 0.0f) return;
        var chão = net.minecraft.core.BlockPos.containing(quem.getX(), quem.getY() - 2.0, quem.getZ());
        boolean gelo = quem.level().getBlockState(chão).is(net.minecraft.world.level.block.Blocks.ICE);
        if (gelo) {
            quem.setDeltaMovement(quem.getDeltaMovement().multiply(NO_GELO, 1.0, NO_GELO));
            return;
        }
        if (!quem.onGround() || quem.isInWater()) return;
        quem.setDeltaMovement(quem.getDeltaMovement().multiply(EMPURRA, 1.0, EMPURRA));
    }
}
