package net.thaumcraft.occulta.wolf;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Quem é lobisomem: o {@code CreatureUtil.isWerewolf} do Witchery.
 *
 * <p>Ela responde por <b>dois</b>: o bicho — o Lobisomem e o Aldeão Lobo — e a <b>gente</b>, que desde a
 * fatia da licantropia pode ser lobisomem também.
 *
 * <p><b>E a costura fechou.</b> A licantropia do jogador veio, e esta classe passou a responder pelos dois: o
 * bicho e a gente. Tudo o que pergunta por lobisomem — as roupas prateadas que ardem em quem as veste, o
 * Caçador que escolhe o virote de prata, o que a prata faz doer — passou a saber a resposta certa sem mudar
 * uma linha, que era o que ela prometia.
 */
public final class Lycanthropy {
    private Lycanthropy() {
    }

    /**
     * Se quem se pergunta é lobisomem: o bicho, ou gente <b>em forma de bicho</b>.
     *
     * <p>Repare que um jogador de grau alto <b>em forma de gente</b> não conta. É o
     * {@code CreatureUtil.isWerewolf(entity, false)} do original, e é o que faz um lobisomem de dia ser gente
     * para todos os efeitos — as roupas prateadas não lhe ardem, e o virote de prata não o fere.
     */
    public static boolean é(@Nullable LivingEntity quem) {
        if (quem instanceof WolfmanEntity) return true;
        return Werewolf.emBicho(quem);
    }

    /**
     * E se ele é lobisomem <b>mesmo de gente</b>: o {@code isWerewolf(entity, true)}.
     *
     * <p>É a pergunta que o <b>Caçador de Bruxas</b> faz quando decide a quem vem buscar, e a que a doença
     * não pega. Um lobisomem continua lobisomem enquanto dorme.
     */
    public static boolean éMesmoDeGente(@Nullable LivingEntity quem) {
        if (quem instanceof WolfmanEntity) return true;
        return Werewolf.grauDe(quem) > 0;
    }
}
