package net.thaumcraft.occulta.vampire;

import net.minecraft.world.entity.player.Player;

/**
 * <b>Comida não alimenta um vampiro</b>: o pedaço do {@code onLivingUpdate} que zera a fome dele.
 *
 * <p>Um vampiro pode mastigar o que quiser e não lhe faz nada. A barra dele sobe por <b>uma</b> porta só, que
 * é o sangue — e é isso que faz da sede uma coisa que não se contorna com um baú de pão.
 *
 * <p><b>O jeito mudou, e para melhor.</b> O original deixa a comida entrar e, na batida seguinte, <b>apaga a
 * barra inteira</b> quando repara que ela subiu — um porrete: quem comesse um pão perdia também o que já
 * tinha dentro. Aqui a comida simplesmente <b>não alimenta</b>: o
 * {@link net.thaumcraft.mixin.FoodPropertiesVampireMixin} atalha o alimento no lugar onde ele conta, e o
 * sangue continua a entrar porque não passa por lá.
 *
 * <p>E há um ganho que o original não tinha: uma maçã dourada <b>ainda cura</b> um vampiro. O que ela deixa
 * de fazer é <b>sustentar</b>, que é o que o mod queria dizer.
 */
public final class VampireHunger {
    private VampireHunger() {
    }

    /** O sangue que vira comida: a única porta por onde a barra de um vampiro sobe. */
    public static void alimenta(Player quem, int quanto, float fartura) {
        quem.getFoodData().eat(quanto, fartura);
    }
}
