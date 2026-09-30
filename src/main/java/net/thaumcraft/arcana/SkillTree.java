package net.thaumcraft.arcana;

import java.util.List;
import java.util.Locale;

/**
 * A <b>árvore de perícias</b>: o {@code SkillTreeManager} do Ars Magica 2.
 *
 * <p>É o que dá <b>curva</b> ao ramo. Sem ela um arcanista nasce sabendo tudo; com ela, começa sabendo três
 * Formas e vai comprando o resto com o que aprende lançando.
 *
 * <p>São três ramos — <b>Ofensa</b>, <b>Defesa</b> e <b>Utilidade</b> — e três cores de ponto: o <b>azul</b>,
 * que se ganha até o nível vinte; o <b>verde</b>, do vinte ao quarenta; e o <b>vermelho</b>, do quarenta ao
 * cinquenta. Uma perícia vermelha não se compra cedo por mais pontos azuis que se tenha, e é isso que faz o
 * ramo ter começo, meio e fim.
 *
 * <p>Ganha-se <b>um ponto a cada dois níveis</b>, e só até o cinquenta: depois disso o nível ainda sobe — e
 * ainda enche a mana e apressa o relógio — mas não compra mais nada. São <b>vinte e cinco pontos</b> ao todo
 * para <b>trinta e duas</b> perícias: <b>não dá para ter tudo</b>, e é de propósito.
 *
 * <p>As posições, os ramos, as cores e a forma do grafo são as do original, lidas do jar.
 */
public final class SkillTree {
    /** Os três ramos em que as perícias se arrumam. */
    public enum Branch {
        /** <b>Ofensa</b>: o que fere. */
        OFFENSE,
        /** <b>Defesa</b>: o que guarda. */
        DEFENSE,
        /** E <b>Utilidade</b>: o que faz o resto. */
        UTILITY;

        public String key() {
            return "tc.spell.branch." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** As três cores de ponto, que são o que marca o tempo do ramo. */
    public enum Point {
        /** <b>Azul</b>: até o nível vinte. */
        BLUE(0x5555FF),
        /** <b>Verde</b>: do vinte ao quarenta. */
        GREEN(0x55FF55),
        /** E <b>vermelho</b>: do quarenta ao cinquenta. */
        RED(0xFF5555);

        public final int color;

        Point(int color) {
            this.color = color;
        }

        public String key() {
            return "tc.spell.point." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Uma perícia na árvore.
     *
     * @param part   a peça que ela dá
     * @param branch em que ramo ela fica
     * @param point  e de que cor é o ponto que ela custa
     * @param x      onde ela fica desenhada, nas coordenadas do original
     * @param y      o mesmo
     * @param needs  e o que é preciso saber antes dela
     */
    public record Entry(SpellPart part, Branch branch, Point point, int x, int y, List<SpellPart> needs) {
    }

    /** De quantos em quantos níveis se ganha um ponto. */
    public static final int LEVELS_PER_POINT = 2;

    /** Até que nível cada cor se ganha: o {@code addMagicXP} do original. */
    public static final int BLUE_UNTIL = 20;
    public static final int GREEN_UNTIL = 40;
    public static final int RED_UNTIL = 50;

    /** Com quantos pontos azuis se começa: os três do original. */
    public static final int STARTING_BLUE = 3;

    private SkillTree() {
    }

    /** A cor do ponto que se ganha naquele nível, ou nada se ele já não dá nenhum. */
    public static Point pointFor(int level) {
        if (level % LEVELS_PER_POINT != 0) return null;
        if (level <= BLUE_UNTIL) return Point.BLUE;
        if (level <= GREEN_UNTIL) return Point.GREEN;
        if (level <= RED_UNTIL) return Point.RED;
        return null;
    }

    /** Quantos pontos daquela cor se ganham do nível zero até aquele. */
    public static int pointsUpTo(Point qual, int level) {
        int quantos = qual == Point.BLUE ? STARTING_BLUE : 0;
        for (int i = LEVELS_PER_POINT; i <= level; i += LEVELS_PER_POINT) {
            if (pointFor(i) == qual) quantos++;
        }
        return quantos;
    }

    // ------------------------------------------------------------------ o quadro

    private static final java.util.LinkedHashMap<SpellPart, Entry> ENTRIES = new java.util.LinkedHashMap<>();

    private static void put(SpellPart qual, Branch ramo, Point ponto, int x, int y, SpellPart... precisa) {
        ENTRIES.put(qual, new Entry(qual, ramo, ponto, x, y, List.of(precisa)));
    }

    static {
        // --- Defesa: começa em Autoconjuração
        put(Shapes.SELF, Branch.DEFENSE, Point.BLUE, 267, 45);
        put(Essences.HEAL, Branch.DEFENSE, Point.BLUE, 357, 135, Shapes.SELF);
        put(Modifiers.HEALING, Branch.DEFENSE, Point.RED, 402, 135, Essences.HEAL);
        put(Shapes.CONTINGENCY_FALL, Branch.DEFENSE, Point.GREEN, 267, 180, Shapes.SELF);
        put(Shapes.CONTINGENCY_DAMAGE, Branch.DEFENSE, Point.GREEN, 447, 180, Modifiers.HEALING);
        put(Shapes.WALL, Branch.DEFENSE, Point.GREEN, 87, 200, Shapes.SELF);
        put(Shapes.ZONE, Branch.DEFENSE, Point.RED, 357, 225, Essences.HEAL);
        put(Shapes.CONTINGENCY_HEALTH, Branch.DEFENSE, Point.RED, 402, 270, Shapes.ZONE);
        put(Shapes.RUNE, Branch.DEFENSE, Point.GREEN, 157, 315, Shapes.SELF);
        put(Modifiers.SPEED, Branch.DEFENSE, Point.RED, 202, 315, Shapes.SELF);
        put(Modifiers.PROCS, Branch.DEFENSE, Point.GREEN, 157, 360, Shapes.RUNE);
        put(Modifiers.DURATION, Branch.DEFENSE, Point.RED, 312, 360, Shapes.ZONE);

        // --- Ofensa: começa em Projétil
        put(Shapes.PROJECTILE, Branch.OFFENSE, Point.BLUE, 300, 45);
        put(Modifiers.GRAVITY, Branch.OFFENSE, Point.BLUE, 255, 70, Shapes.PROJECTILE);
        put(Modifiers.BOUNCE, Branch.OFFENSE, Point.BLUE, 345, 70, Shapes.PROJECTILE);
        put(Essences.FIRE_DAMAGE, Branch.OFFENSE, Point.BLUE, 210, 135, Shapes.PROJECTILE);
        put(Essences.FROST_DAMAGE, Branch.OFFENSE, Point.BLUE, 345, 135, Shapes.PROJECTILE);
        put(Shapes.CONTINGENCY_FIRE, Branch.OFFENSE, Point.GREEN, 165, 180, Essences.FIRE_DAMAGE);
        put(Shapes.AOE, Branch.OFFENSE, Point.GREEN, 300, 180,
                Essences.FIRE_DAMAGE, Essences.FROST_DAMAGE);
        put(Modifiers.PIERCING, Branch.OFFENSE, Point.RED, 323, 215, Essences.FROST_DAMAGE);
        put(Shapes.BEAM, Branch.OFFENSE, Point.RED, 300, 270, Shapes.AOE);
        put(Modifiers.DAMAGE, Branch.OFFENSE, Point.RED, 300, 315, Shapes.BEAM);
        put(Shapes.WAVE, Branch.OFFENSE, Point.RED, 367, 315, Shapes.BEAM);

        // --- Utilidade: começa em Toque
        put(Shapes.TOUCH, Branch.UTILITY, Point.BLUE, 275, 75);
        put(Essences.DIG, Branch.UTILITY, Point.BLUE, 275, 120, Shapes.TOUCH);
        put(Modifiers.MINING_POWER, Branch.UTILITY, Point.GREEN, 185, 137, Essences.DIG);
        put(Essences.LIGHT, Branch.UTILITY, Point.BLUE, 275, 165, Essences.DIG);
        put(Shapes.CHAIN, Branch.UTILITY, Point.RED, 455, 210, Essences.LIGHT);
        put(Modifiers.RANGE, Branch.UTILITY, Point.RED, 140, 345, Essences.LIGHT);
        put(Modifiers.RADIUS, Branch.UTILITY, Point.RED, 275, 390, Essences.LIGHT);
        put(Shapes.CONTINGENCY_DEATH, Branch.UTILITY, Point.RED, 198, 524, Essences.LIGHT);

        // O Vínculo, que no original é uma perícia só em (275, 210), azul, depois da Luz. Aqui são cinco —
        // uma por ferramenta —, postas em leque a partir do lugar dele. A primeira fica onde ele ficava.
        put(Shapes.BINDING_PICKAXE, Branch.UTILITY, Point.BLUE, 275, 210, Essences.LIGHT);
        put(Shapes.BINDING_AXE, Branch.UTILITY, Point.BLUE, 320, 232, Shapes.BINDING_PICKAXE);
        put(Shapes.BINDING_SWORD, Branch.UTILITY, Point.BLUE, 230, 232, Shapes.BINDING_PICKAXE);
        put(Shapes.BINDING_SHOVEL, Branch.UTILITY, Point.GREEN, 342, 268, Shapes.BINDING_AXE);
        put(Shapes.BINDING_HOE, Branch.UTILITY, Point.GREEN, 208, 268, Shapes.BINDING_SWORD);
        put(Shapes.BINDING_BOW, Branch.UTILITY, Point.RED, 275, 292, Shapes.BINDING_PICKAXE);

        // Os Alvos Não Sólidos não estão na árvore do original — declarado. Aqui são raiz e de graça em azul,
        // porque sem eles o Projétil e a Onda não sabem pegar em água.
        put(Modifiers.TARGET_NONSOLID_BLOCKS, Branch.UTILITY, Point.BLUE, 87, 45);
    }

    /** Tudo o que há para aprender, na ordem em que foi escrito. */
    public static List<Entry> entries() {
        return List.copyOf(ENTRIES.values());
    }

    /** A perícia daquela peça, ou nada se ela não estiver na árvore. */
    public static Entry of(SpellPart qual) {
        return ENTRIES.get(qual);
    }

    /** As de um ramo só. */
    public static List<Entry> of(Branch ramo) {
        return ENTRIES.values().stream().filter(e -> e.branch() == ramo).toList();
    }

    /** As que não precisam de nada: os começos de cada ramo. */
    public static List<Entry> roots() {
        return ENTRIES.values().stream().filter(e -> e.needs().isEmpty()).toList();
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada. */
    public static void init() {
    }
}
