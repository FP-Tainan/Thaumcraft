package net.thaumcraft.occulta.wolf;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Quem é lobisomem: a costura por onde a <b>licantropia do jogador</b> vai entrar.
 *
 * <p>É a gémea do {@link net.thaumcraft.occulta.vampire.Vampirism}, e está aqui pela mesma razão: hoje ela
 * responde só pelo bicho, porque só o bicho está portado — o Lobisomem e o Aldeão Lobo.
 *
 * <p><b>É uma costura, e não um desvio.</b> Quando a licantropia do jogador vier — os dez graus, as duas
 * formas, a lua, a romaria dos pedaços —, <b>esta é a única linha que muda</b>, e tudo o que pergunta por
 * lobisomem passa a saber a resposta certa: as roupas prateadas que ardem em quem as veste, o Caçador que
 * escolhe o virote de prata, e o que a prata faz doer.
 */
public final class Lycanthropy {
    private Lycanthropy() {
    }

    /** Se quem se pergunta é lobisomem. */
    public static boolean é(@Nullable LivingEntity quem) {
        return quem instanceof WolfmanEntity;
    }
}
