package net.thaumcraft.occulta;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Quem conta como <b>morto-vivo</b>: o {@code CreatureUtil.isUndead} do Witchery.
 *
 * <p>Parece uma pergunta de uma linha, e tem uma segunda metade que é o contrário de uma linha: além dos
 * bichos do rótulo, contam como mortos-vivos <b>as pessoas</b> que bebam o
 * {@linkplain net.thaumcraft.occulta.infusion.InfusedBrews Cozimento da Cova}.
 *
 * <p>Pelo original isto vale em tudo o que pergunte: a Caveira Seductora não chama quem o bebeu, a poça de
 * doença não o adoece, o virote do caçador fere-o a dobrar, e o Caçador de Bruxas ataca-o. Duas horas a ser
 * uma coisa morta não são um adorno.
 *
 * <p>No original a pergunta vale também para quem for <b>vampiro</b>. Este porte tem vampiros, e a conta
 * deles corre por outro caminho — fica <b>declarado</b> que só o cozimento entra aqui.
 */
public final class Undead {
    private Undead() {
    }

    /** Se este é um morto-vivo: pelo rótulo, ou por ter bebido o da Cova. */
    public static boolean é(@Nullable Entity quem) {
        if (!(quem instanceof LivingEntity vivo)) return false;
        if (vivo.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) return true;
        return quem instanceof Player gente
                && net.thaumcraft.occulta.infusion.InfusedBrews.tem(gente,
                net.thaumcraft.occulta.infusion.InfusedBrews.COVA);
    }
}
