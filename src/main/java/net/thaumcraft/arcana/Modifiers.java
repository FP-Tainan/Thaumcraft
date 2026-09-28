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
     * <b>Dano</b>: soma <b>2,2</b> ao dano, e cobra trinta por cento a mais por vez.
     *
     * <p>Repare que ele <b>soma</b> e não multiplica — é o que o original faz ao chamar o
     * {@code getModifiedDouble_Add} no dano, e é o que impede um feitiço de dano de crescer sem fim.
     */
    public static final SpellPart.Modifier DAMAGE = SpellParts.modifier(
            new Simple("damage", SpellModifierKind.DAMAGE, 2.2f, 1.3f));

    /** <b>Alcance</b>: soma quatro casas, por vinte por cento a mais. */
    public static final SpellPart.Modifier RANGE = SpellParts.modifier(
            new Simple("range", SpellModifierKind.RANGE, 4.0f, 1.2f));

    /** <b>Duração</b>: multiplica o tempo por <b>2,2</b>, por vinte e cinco por cento a mais. */
    public static final SpellPart.Modifier DURATION = SpellParts.modifier(
            new Simple("duration", SpellModifierKind.DURATION, 2.2f, 1.25f));

    /**
     * <b>Raio</b>: multiplica o raio por <b>0,7</b> — e sim, isso <b>encolhe</b>.
     *
     * <p>É o número do original e não é engano: no Ars Magica 2 o modificador de raio custa <b>duas vezes e
     * meia</b> por vez e serve para <i>apertar</i> uma área, para o feitiço não apanhar quem não devia. Quem o
     * quer maior põe a Forma de Área, que já nasce com três casas.
     */
    public static final SpellPart.Modifier RADIUS = SpellParts.modifier(
            new Simple("radius", SpellModifierKind.RADIUS, 0.7f, 2.5f));

    /** <b>Cura</b>: dobra o que se cura, e cobra uma vez a mais por vez. */
    public static final SpellPart.Modifier HEALING = SpellParts.modifier(
            new Simple("healing", SpellModifierKind.HEALING, 2.0f, 1.0f));

    /** <b>Força de Mineração</b>: deixa o feitiço cavar pedra mais dura. */
    public static final SpellPart.Modifier MINING_POWER = SpellParts.modifier(
            new Simple("mining_power", SpellModifierKind.MINING_POWER, 1.0f, 1.25f));

    /** Sem uso fora do porte: obriga a classe a carregar-se, e com ela os Modificadores a registarem-se. */
    public static void init() {
    }
}
