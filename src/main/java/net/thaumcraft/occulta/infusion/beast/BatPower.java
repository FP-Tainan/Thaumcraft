package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>morcego</b> — e o da <b>coruja</b>, que é o mesmo: a {@code CreaturePowerBat} do Witchery.
 *
 * <p>Usado, dá <b>Visão Noturna por vinte segundos</b>. E, sem se usar, dá <b>o voo do morcego</b>: segurando
 * o <b>pular</b> no ar, se sobe; e a queda nunca conta <b>mais do que cinco blocos</b>.
 *
 * <p>Esse segundo pedaço é o poder inteiro. Com um morcego no bolso, qualquer parede é escada e qualquer
 * buraco é de ida e volta — e nada disso gasta carga de bicho, porque o voo é <b>de graça</b>. O que gasta
 * é a visão noturna, que é o que menos importa.
 */
public class BatPower extends CreaturePower {
    /** Quanto a visão noturna dura. */
    public static final int VÊ = 400;

    /** Quanto o pular empurra para cima, e o pulo que ele dá no ar. */
    public static final double EMPURRA = 0.067;
    public static final double PULO = 0.42;

    /** E de quanto a queda nunca passa. */
    public static final float CAI = 5.0f;

    public BatPower(int id, EntityType<?> dequê) {
        super(id, dequê);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, VÊ, 0));
    }

    /**
     * O voo, do lado de cá.
     *
     * <p>O original lê a <b>tecla de pular</b> direto. Aqui se lê o que o jogador está pedindo ao jogo —
     * que é a mesma coisa e ainda vale para quem jogar com outra tecla.
     */
    @Override
    public void batida(Player quem) {
        if (!pulando(quem)) return;
        Vec3 anda = quem.getDeltaMovement();
        if (anda.y > 0.0) {
            quem.setDeltaMovement(anda.x, anda.y + EMPURRA, anda.z);
            return;
        }
        if (!quem.onGround()) quem.setDeltaMovement(anda.x, PULO, anda.z);
    }

    /**
     * Se ele está pedindo para pular.
     *
     * <p>O campo do jogo é protegido, e por isso a pergunta é feita pelo <b>mixin</b>
     * {@link net.thaumcraft.mixin.LivingEntityJumpingAccessor}.
     */
    public static boolean pulando(Player quem) {
        return ((net.thaumcraft.mixin.LivingEntityJumpingAccessor) quem).thaumcraft$pulando();
    }

    @Override
    public float cai(ServerPlayer quem, float distância) {
        return Math.min(distância, CAI);
    }
}
