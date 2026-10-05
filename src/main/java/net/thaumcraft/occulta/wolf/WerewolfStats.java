package net.thaumcraft.occulta.wolf;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * O que cada grau dá em cada forma: os {@code boostWolf} e {@code boostWolfman} do {@code Shapeshift}.
 *
 * <p>São duas tabelas de onze linhas — do grau zero ao dez —, e elas contam a história melhor do que qualquer
 * descrição:
 *
 * <ul>
 *   <li><b>O lobo é depressa desde o princípio</b>: meio ponto de velocidade já no primeiro grau, e um ponto
 *       e três quartos no décimo. Ele é o que foge e o que persegue.</li>
 *   <li><b>O lobisomem não vale nada até o quinto grau</b> — zeros em tudo — e então, de uma vez, passa a ter
 *       <b>vinte de vida a mais</b>, quatro de dano e salto. É a forma que se ganha, não a que se recebe.</li>
 *   <li><b>E o teto da pancada desce com o grau</b>: quatro no princípio, dois do quinto em diante. É o único
 *       número que melhora <i>baixando</i>, e é o que faz um lobisomem de grau alto difícil de matar.</li>
 * </ul>
 *
 * <p>A <b>queda</b> também perdoa com o grau: um lobo de grau sete cai quatro blocos de graça, e um de grau
 * dez, doze.
 */
public record WerewolfStats(float velocidade, double salto, double arranco, int vida, float dano,
                            float resistência, int queda, float tetoDaPancada) {
    /** O que não dá nada: o grau zero das duas tabelas. */
    public static final WerewolfStats NADA = new WerewolfStats(0.0f, 0.0, 0.0, 0, 0.0f, 0.0f, 0, 4.0f);

    /** A tabela do <b>lobo</b>, do grau zero ao dez. */
    public static final WerewolfStats[] LOBO = {
            NADA,
            new WerewolfStats(0.5f, 0.2, 0.2, 0, 1.0f, 0.0f, 2, 4.0f),
            new WerewolfStats(0.5f, 0.2, 0.2, 0, 1.0f, 0.0f, 2, 3.0f),
            new WerewolfStats(0.75f, 0.2, 0.3, 0, 2.0f, 0.0f, 2, 3.0f),
            new WerewolfStats(0.75f, 0.2, 0.4, 0, 2.0f, 0.0f, 3, 3.0f),
            new WerewolfStats(0.75f, 0.2, 0.5, 0, 2.0f, 0.0f, 3, 2.0f),
            new WerewolfStats(1.0f, 0.2, 0.6, 0, 2.0f, 1.0f, 3, 2.0f),
            new WerewolfStats(1.25f, 0.3, 0.7, 4, 2.0f, 1.0f, 4, 2.0f),
            new WerewolfStats(1.5f, 0.3, 0.8, 8, 3.0f, 2.0f, 4, 2.0f),
            new WerewolfStats(1.75f, 0.3, 0.9, 12, 3.0f, 3.0f, 5, 2.0f),
            new WerewolfStats(1.75f, 0.3, 1.0, 12, 3.0f, 3.0f, 5, 2.0f),
    };

    /** E a do <b>lobisomem</b>, que só começa a valer ao quinto. */
    public static final WerewolfStats[] LOBISOMEM = {
            NADA,
            NADA,
            new WerewolfStats(0.0f, 0.0, 0.0, 0, 0.0f, 0.0f, 0, 3.0f),
            new WerewolfStats(0.0f, 0.0, 0.0, 0, 0.0f, 0.0f, 0, 3.0f),
            new WerewolfStats(0.0f, 0.0, 0.0, 0, 0.0f, 0.0f, 0, 3.0f),
            new WerewolfStats(0.2f, 0.2, 0.2, 20, 4.0f, 3.0f, 3, 2.0f),
            new WerewolfStats(0.2f, 0.3, 0.2, 20, 4.0f, 3.0f, 4, 2.0f),
            new WerewolfStats(0.4f, 0.4, 0.4, 20, 5.0f, 4.0f, 5, 2.0f),
            new WerewolfStats(0.4f, 0.5, 0.4, 30, 6.0f, 4.0f, 6, 2.0f),
            new WerewolfStats(0.5f, 0.6, 0.6, 40, 7.0f, 5.0f, 7, 2.0f),
            new WerewolfStats(0.5f, 0.6, 0.6, 40, 7.0f, 5.0f, 7, 2.0f),
    };

    /** As quatro marcas com que os modificadores vão e vêm. */
    public static final net.minecraft.resources.Identifier VELOCIDADE = Thaumcraft.id("werewolf_speed");
    public static final net.minecraft.resources.Identifier DANO = Thaumcraft.id("werewolf_damage");
    public static final net.minecraft.resources.Identifier VIDA = Thaumcraft.id("werewolf_health");
    public static final net.minecraft.resources.Identifier PASSADA = Thaumcraft.id("werewolf_step");

    /** A passada de um bicho: o degrau de um bloco inteiro que o original dá às duas formas. */
    public static final double PASSADA_DE_BICHO = 0.5;

    /** O que esta pessoa ganha agora, ou nada se ela anda de gente. */
    @Nullable
    public static WerewolfStats de(Player quem) {
        Werewolf oQueÉ = Werewolf.de(quem);
        int grau = Math.clamp(oQueÉ.grau(), 0, Werewolf.TETO);
        return switch (oQueÉ.forma()) {
            case LOBO -> LOBO[grau];
            case LOBISOMEM -> LOBISOMEM[grau];
            case GENTE -> null;
        };
    }

    /**
     * Põe no corpo o que a forma dá — ou tira tudo, se ela voltou a ser gente.
     *
     * <p>Os três modificadores são os do original, com as mesmas contas: a velocidade e o dano <b>multiplicam
     * o total</b>, e a vida <b>soma</b>. É por isso que meio ponto de velocidade num lobo é meia vez mais
     * depressa, e vinte de vida num lobisomem são dez corações a mais e não dez vezes.
     */
    public static void põe(Player quem) {
        WerewolfStats dá = de(quem);
        aplica(quem.getAttribute(Attributes.MOVEMENT_SPEED), VELOCIDADE,
                dá == null ? 0.0 : dá.velocidade(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, dá != null);
        aplica(quem.getAttribute(Attributes.ATTACK_DAMAGE), DANO,
                dá == null ? 0.0 : dá.dano(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, dá != null);
        aplica(quem.getAttribute(Attributes.MAX_HEALTH), VIDA,
                dá == null ? 0.0 : dá.vida(), AttributeModifier.Operation.ADD_VALUE, dá != null);
        // e a passada: as duas formas sobem um bloco inteiro, que é meio a mais do que gente sobe
        aplica(quem.getAttribute(Attributes.STEP_HEIGHT), PASSADA,
                PASSADA_DE_BICHO, AttributeModifier.Operation.ADD_VALUE, dá != null);

        // a vida que sobra não pode passar do teto novo, ou o jogo mostra corações a mais
        if (quem.getHealth() > quem.getMaxHealth()) quem.setHealth(quem.getMaxHealth());
    }

    private static void aplica(@Nullable AttributeInstance qual, net.minecraft.resources.Identifier marca,
                               double quanto, AttributeModifier.Operation como, boolean põe) {
        if (qual == null) return;
        qual.removeModifier(marca);
        if (!põe || quanto == 0.0) return;
        qual.addTransientModifier(new AttributeModifier(marca, quanto, como));
    }
}
