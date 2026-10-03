package net.thaumcraft.occulta.vampire;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Quem é vampiro: o {@code CreatureUtil.isVampire} do Witchery.
 *
 * <p>Ela responde por <b>dois</b>: o bicho — o Vampiro do mundo — e a <b>gente</b>, que desde a fatia do
 * corpo do vampiro pode ser vampira também.
 *
 * <p><b>E a costura fechou.</b> Ela foi escrita na fatia do bicho como "a única linha que muda quando a
 * vampirice vier", e foi <b>só</b> ela: o uivo que prende e não pega em vampiros, e o vampiro que não ataca
 * outro vampiro, passaram a saber a resposta certa sem se tocar em mais nada.
 */
public final class Vampirism {
    private Vampirism() {
    }

    /** Se quem se pergunta é vampiro: o bicho, ou gente de grau um para cima. */
    public static boolean é(@Nullable LivingEntity quem) {
        if (quem instanceof VampireEntity) return true;
        return Vampire.é(quem);
    }
}
