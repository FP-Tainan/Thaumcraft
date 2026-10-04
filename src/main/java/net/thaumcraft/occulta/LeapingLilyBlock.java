package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A <b>Lírio-Saltador</b>: o {@code BlockLeapingLily} do Witchery.
 *
 * <p>É um nenúfar que <b>brilha</b> e que dá a quem lhe pisa <b>Rapidez</b> e <b>Salto V</b> por meio
 * segundo — meio segundo que se renova a cada passo.
 *
 * <p>Um nenúfar sozinho é um degrau. Uma fileira deles é uma <b>estrada</b>: quem a percorrer atravessa um
 * pântano aos saltos, por cima da água, sem nunca tocar nela. É o jeito do ofício de andar depressa sem
 * vassoura.
 *
 * <p>E o efeito é curto de propósito: saltando para fora da fileira, ele acaba antes de se chegar ao chão.
 */
public class LeapingLilyBlock extends LilyPadBlock {
    /** Quanto o salto dura, que é meio segundo. */
    public static final int QUANTO = 10;

    /** E o grau dele: o quinto, que é o do demônio. */
    public static final int SALTO = 4;

    public LeapingLilyBlock(Properties properties) {
        super(properties);
    }

    /**
     * O {@code onEntityCollidedWithBlock}: ele só põe o efeito em quem <b>ainda não o tem</b>.
     *
     * <p>É assim no original, e faz diferença: quem já traz um salto de outra coisa não o perde para este,
     * que é mais fraco em tempo. O nenúfar <b>não atrapalha</b> quem já está voando.
     */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean dentroMesmo) {
        if (level.isClientSide() || !(quem instanceof LivingEntity vivo)) return;
        salta(vivo);
    }

    /** O salto que ele dá: serve às provas, que não têm como pisar num nenúfar à mão. */
    public static void salta(LivingEntity quem) {
        if (!quem.hasEffect(MobEffects.SPEED)) {
            quem.addEffect(new MobEffectInstance(MobEffects.SPEED, QUANTO, 0));
        }
        if (!quem.hasEffect(MobEffects.JUMP_BOOST)) {
            quem.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, QUANTO, SALTO));
        }
    }
}
