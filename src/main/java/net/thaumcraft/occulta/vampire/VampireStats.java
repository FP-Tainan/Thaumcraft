package net.thaumcraft.occulta.vampire;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * O que cada grau de vampiro dá: o {@code boostVampire} do {@code Shapeshift}.
 *
 * <p>E a tabela dele é uma lição ao lado da do lobisomem: onde o lobo tem oito números por grau, o vampiro
 * tem <b>um só</b> — o <b>dano</b>, que sobe de um em um a cada três graus e para em três.
 *
 * <p>Não há vida a mais, não há velocidade, não há resistência, não há teto de pancada. Um vampiro de grau
 * dez, de dia, no corpo dele, é quase <b>gente</b>: o que ele tem não está no corpo, está no <b>sangue</b> e
 * no que ele compra com ele.
 */
public final class VampireStats {
    /** O dano a mais por grau, do zero ao dez. */
    public static final float[] DANO = {
            0.0f, 1.0f, 1.0f, 1.0f, 2.0f, 2.0f, 2.0f, 3.0f, 3.0f, 3.0f, 3.0f,
    };

    /** A marca com que o modificador vai e vem. */
    public static final net.minecraft.resources.Identifier MARCA = Thaumcraft.id("vampire_damage");

    private VampireStats() {
    }

    /**
     * O que esta pessoa ganha agora.
     *
     * <p>Em <b>forma de morcego</b> não é o grau que manda: é <b>menos seis</b>, e o grau não conta para
     * nada. No original são duas tabelas diferentes, e a do morcego <b>substitui</b> a do vampiro em vez de
     * se somar a ela. Um vampiro de décimo grau em forma de morcego bate <b>menos</b> do que gente, e é esse
     * o preço do voo.
     */
    public static float de(Player quem) {
        if (VampirePowers.emMorcego(quem)) return VampirePowers.MORCEGO_DANO;
        return DANO[Math.clamp(Vampire.grauDe(quem), 0, Vampire.TETO)];
    }

    /**
     * Põe no corpo o que o grau dá, ou tira tudo se ela deixou de ser vampira.
     *
     * <p>O dano <b>soma</b> e não multiplica, ao contrário do do lobisomem: o vampiro não bate mais forte
     * porque é mais rápido, bate mais forte porque é mais <b>velho</b>.
     */
    public static void põe(Player quem) {
        var dano = quem.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dano == null) return;
        dano.removeModifier(MARCA);
        float quanto = de(quem);
        if (quanto == 0.0f) return;
        dano.addTransientModifier(new AttributeModifier(MARCA, quanto,
                AttributeModifier.Operation.ADD_VALUE));
    }
}
