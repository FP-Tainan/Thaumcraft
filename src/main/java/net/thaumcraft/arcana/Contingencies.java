package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * As duas Contingências que se decidem no tique: a de <b>Fogo</b> e a de <b>Queda</b>.
 *
 * <p>As outras três têm um momento certo — a pancada, a vida a descer, a morte — e ficam no mixin. Estas duas
 * são estados, e por isso é preciso olhar para elas a cada batida.
 */
public final class Contingencies {
    /** Até onde se procura o chão, para a de Queda saber se ele está perto. */
    public static final int GROUND_SEARCH = 64;

    private Contingencies() {
    }

    /** Uma batida de quem tem Contingência guardada. */
    public static void tick(ServerLevel level, LivingEntity quem) {
        Contingency guardada = Contingency.of(quem);
        if (!guardada.armed()) return;

        switch (guardada.kind()) {
            case ON_FIRE -> {
                if (quem.isOnFire()) Contingency.proc(level, quem, Contingency.Kind.ON_FIRE);
            }
            case FALL -> queda(level, quem);
            default -> {
                // as outras têm o seu momento e não se decidem aqui
            }
        }
    }

    /**
     * A de <b>Queda</b>: o trecho do {@code onEntityLivingBase} do original.
     *
     * <p>Ela não dispara quando se começa a cair — dispara quando o <b>chão está perto demais</b> para o que
     * falta cair. A conta é a do original: a distância até o chão tem de ser menor que <b>oito vezes</b> a
     * velocidade de queda (que é negativa, e por isso a conta fecha).
     *
     * <p>É o que faz dela um paraquedas e não um planador: ela deixa cair à vontade e só age no fim.
     */
    private static void queda(ServerLevel level, LivingEntity quem) {
        if (quem.onGround()) return;
        if (quem.fallDistance < Contingency.FALL_MIN) return;

        double chão = distânciaAtéOChão(level, quem);
        if (chão < Contingency.FALL_LOOKAHEAD * quem.getDeltaMovement().y) {
            Contingency.proc(level, quem, Contingency.Kind.FALL);
        }
    }

    /** A quantos blocos está o chão debaixo de alguém: o {@code getDistanceToGround} do original. */
    public static double distânciaAtéOChão(ServerLevel level, LivingEntity quem) {
        BlockPos onde = quem.blockPosition();
        for (int i = 0; i < GROUND_SEARCH; i++) {
            BlockPos abaixo = onde.below(i);
            if (abaixo.getY() < level.getMinY()) return i;
            if (!level.getBlockState(abaixo).getCollisionShape(level, abaixo).isEmpty()) return i;
        }
        return GROUND_SEARCH;
    }
}
