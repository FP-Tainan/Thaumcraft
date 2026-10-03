package net.thaumcraft.occulta.vampire;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Quem é vampiro: a costura por onde a <b>vampirice do jogador</b> vai entrar.
 *
 * <p>Hoje ela responde só pelo bicho, porque só o bicho está portado — e o vampiro do mod não ataca outro
 * vampiro, que é a única pergunta que o bicho lhe faz.
 *
 * <p><b>É uma costura, e não um desvio.</b> Quando a vampirice do jogador vier — os graus, a sede, a forma de
 * morcego, as vítimas —, <b>esta é a única linha que muda</b>, como aconteceu com o {@code temFamiliar} da
 * Bruxa do Coven, que ficou aqui escrito do mesmo jeito e fechou com uma linha na fatia dos familiares.
 */
public final class Vampirism {
    private Vampirism() {
    }

    /** Se quem se pergunta é vampiro. */
    public static boolean é(@Nullable LivingEntity quem) {
        return quem instanceof VampireEntity;
    }
}
