package net.thaumcraft.arcana;

import java.util.Set;

/**
 * Os Modificadores: os {@code am2.spell.modifiers} do Ars Magica 2.
 *
 * <p>Nenhum deles faz nada sozinho: cada um mexe num número dos outros, e cobra por isso. O que eles cobram é
 * <b>multiplicativo e por vez</b> — pôr o mesmo modificador três vezes triplica o efeito e mais do que triplica
 * a conta. É o que faz de um feitiço de arcanista uma escolha e não uma lista de compras.
 *
 * <p>Todos os números são os do original.
 */
public final class Modifiers {
    private Modifiers() {
    }

    /** Um modificador simples: mexe num número só, por um valor fixo. */
    private record Simple(String name, SpellModifierKind kind, float value, float cost)
            implements SpellPart.Modifier {
        @Override
        public Set<SpellModifierKind> modifies() {
            return Set.of(this.kind);
        }

        @Override
        public float value(SpellModifierKind qual) {
            return this.value;
        }

        @Override
        public float manaMultiplier(int quantas) {
            return this.cost * quantas;
        }
    }

    /**
     * Um modificador que <b>não cobra nada</b>, por muitas vezes que se repita.
     *
     * <p>O original escreve-o devolvendo {@code 1.0F} <i>sem</i> multiplicar pela quantidade, ao contrário de
     * todos os outros. Não é descuido: são os dois modificadores que mudam <i>como</i> o feitiço se comporta e
     * não <i>quanto</i> ele faz, e o original quis que fossem de graça.
     */
    private record Grátis(String name, SpellModifierKind kind, float value)
            implements SpellPart.Modifier {
        @Override
        public Set<SpellModifierKind> modifies() {
            return Set.of(this.kind);
        }

        @Override
        public float value(SpellModifierKind qual) {
            return this.value;
        }

        @Override
        public float manaMultiplier(int quantas) {
            return 1.0f;
        }
    }

    /**
     * <b>Dano</b>: soma <b>2,2</b> ao dano, e cobra trinta por cento a mais por vez.
     *
     * <p>Repare que ele <b>soma</b> e não multiplica — é o que o original faz ao chamar o
     * {@code getModifiedDouble_Add} no dano, e é o que impede um feitiço de dano de crescer sem fim.
     */
    public static final SpellPart.Modifier DAMAGE = SpellParts.modifier(
            new Simple("damage", SpellModifierKind.DAMAGE, 2.2f, 1.3f));

    /** <b>Alcance</b>: soma quatro blocos, por vinte por cento a mais. */
    public static final SpellPart.Modifier RANGE = SpellParts.modifier(
            new Simple("range", SpellModifierKind.RANGE, 4.0f, 1.2f));

    /** <b>Duração</b>: multiplica o tempo por <b>2,2</b>, por vinte e cinco por cento a mais. */
    public static final SpellPart.Modifier DURATION = SpellParts.modifier(
            new Simple("duration", SpellModifierKind.DURATION, 2.2f, 1.25f));

    /**
     * <b>Raio</b>: multiplica o raio por <b>0,7</b> — e sim, isso <b>encolhe</b>.
     *
     * <p>É o número do original e não é engano: no Ars Magica 2 o modificador de raio custa <b>duas vezes e
     * meia</b> por vez e serve para <i>apertar</i> uma área, para o feitiço não pegar quem não devia. Quem o
     * quer maior põe a Forma de Área, que já nasce com três blocos.
     */
    public static final SpellPart.Modifier RADIUS = SpellParts.modifier(
            new Simple("radius", SpellModifierKind.RADIUS, 0.7f, 2.5f));

    /** <b>Cura</b>: dobra o que se cura, e cobra uma vez a mais por vez. */
    public static final SpellPart.Modifier HEALING = SpellParts.modifier(
            new Simple("healing", SpellModifierKind.HEALING, 2.0f, 1.0f));

    /** <b>Força de Mineração</b>: deixa o feitiço cavar pedra mais dura. */
    public static final SpellPart.Modifier MINING_POWER = SpellParts.modifier(
            new Simple("mining_power", SpellModifierKind.MINING_POWER, 1.0f, 1.25f));

    /**
     * <b>Velocidade</b>: multiplica a velocidade do que voa por <b>2,6</b>, por quinze por cento a mais por vez.
     *
     * <p>É o modificador mais barato do original e o que mais muda como o feitiço se sente na mão: um projétil
     * sem ele se arrasta a um bloco por batida, e com ele atravessa uma sala antes de se ver.
     */
    public static final SpellPart.Modifier SPEED = SpellParts.modifier(
            new Simple("speed", SpellModifierKind.SPEED, 2.6f, 1.15f));

    /**
     * <b>Gravidade</b>: soma <b>menos seis centésimos</b> à gravidade, de graça.
     *
     * <p>O valor é negativo e é o sinal que o faz cair, porque o projétil do original lê a gravidade ao
     * contrário do que o nome sugere. E ele <b>não cobra nada</b>: multiplicador um. É o que faz dele o
     * modificador de quem quer um feitiço de arco — pôr três Gravidades põe o projétil caindo em curva.
     */
    public static final SpellPart.Modifier GRAVITY = SpellParts.modifier(
            new Grátis("gravity", SpellModifierKind.GRAVITY, -0.06f));

    /** <b>Ricochete</b>: dá ao projétil <b>dois</b> saltos, por vinte e cinco por cento a mais por vez. */
    public static final SpellPart.Modifier BOUNCE = SpellParts.modifier(
            new Simple("bounce", SpellModifierKind.BOUNCE, 2.0f, 1.25f));

    /**
     * <b>Perfuração</b>: faz o projétil atravessar <b>dois</b> a mais, por metade a mais por vez.
     *
     * <p>A conta de quem atravessa parte de <b>zero</b> e não do dois que o feitio traz: um projétil sem este
     * modificador morre no primeiro que pegar. O dois é o que <i>cada</i> Perfuração acrescenta.
     */
    public static final SpellPart.Modifier PIERCING = SpellParts.modifier(
            new Simple("piercing", SpellModifierKind.PIERCING, 2.0f, 1.5f));

    /**
     * <b>Alvos Não Sólidos</b>: faz o feitiço pegar em água, erva alta e no que não tem caixa, <b>de graça</b>.
     *
     * <p>Ele não tem valor nenhum — o que conta é <b>estar lá</b>. Sem ele, um projétil passa pela água como se
     * ela não existisse; com ele, bate nela.
     */
    public static final SpellPart.Modifier TARGET_NONSOLID_BLOCKS = SpellParts.modifier(
            new Grátis("target_nonsolid_blocks", SpellModifierKind.TARGET_NONSOLID_BLOCKS, 1.0f));

    /**
     * <b>Repetições</b>: soma <b>quatro</b> às vezes que uma coisa acontece, por 65 por cento a mais por vez.
     *
     * <p>Ele só vale para quem conta vezes — hoje, a Runa. Uma Runa com uma Repetição aguenta cinco pisadas
     * em vez de uma.
     */
    public static final SpellPart.Modifier PROCS = SpellParts.modifier(
            new Simple("procs", SpellModifierKind.PROCS, 4.0f, 1.65f));

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela os Modificadores a se registrarem. */
    public static void init() {
    }
}
