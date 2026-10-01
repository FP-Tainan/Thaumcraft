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

    /**
     * As cores de ponto, que são o que marca o tempo do ramo.
     *
     * <p>Três delas se ganham subindo de nível. A quarta — a <b>prateada</b> — não: ela não vem do nível
     * nenhum, vem de <b>descobrir</b>. Lançar um feitiço com a combinação certa de peças destranca uma perícia
     * prateada e dá o ponto para a comprar, e é assim que o original esconde as dez coisas mais estranhas que
     * ele tem. Quem nunca tentar a combinação nunca saberá que elas existem.
     */
    public enum Point {
        /** <b>Azul</b>: até o nível vinte. */
        BLUE(0x5555FF),
        /** <b>Verde</b>: do vinte ao quarenta. */
        GREEN(0x55FF55),
        /** <b>Vermelho</b>: do quarenta ao cinquenta. */
        RED(0xFF5555),
        /** E <b>prateado</b>: um por segredo descoberto, e nível nenhum o dá. */
        SILVER(0x888888);

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

    /**
     * Quantos pontos daquela cor se ganham do nível zero até aquele.
     *
     * <p>O prateado dá sempre <b>zero</b>: ele não se ganha por nível, se ganha por descoberta, e quem o conta
     * é o {@link SkillData#silver()}.
     */
    public static int pointsUpTo(Point qual, int level) {
        if (qual == Point.SILVER) return 0;
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

    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    /**
     * O quadro, tal como o original o escreve.
     *
     * <p>Estas linhas <b>não foram escritas à mão</b>: saíram do {@code SkillTreeManager} do Ars Magica 2,
     * lidas uma a uma. Cada perícia fica no ramo, na cor e no lugar em que ele a pôs.
     *
     * <p>O que muda é o que <b>não está portado</b>. O original tem 120 perícias em quatro ramos; o quarto —
     * os <b>Talentos</b>, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito
     * de peças de feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma
     * perícia desaparece assim, os filhos dela passam a pender do <b>antepassado portado mais próximo</b>, que
     * é a única maneira de o quadro continuar inteiro — sem isso haveria peças penduradas no nada, que ninguém
     * poderia comprar nunca.
     */
    static {

        // ------------------------------------------------------------------ Ofensa
        put(Shapes.PROJECTILE, Branch.OFFENSE, Point.BLUE, 300, 45);
        put(Essences.PHYSICAL_DAMAGE, Branch.OFFENSE, Point.BLUE, 300, 90, Shapes.PROJECTILE);
        put(Modifiers.GRAVITY, Branch.OFFENSE, Point.BLUE, 255, 70, Shapes.PROJECTILE);
        put(Modifiers.BOUNCE, Branch.OFFENSE, Point.BLUE, 345, 70, Shapes.PROJECTILE);
        put(Essences.FIRE_DAMAGE, Branch.OFFENSE, Point.BLUE, 210, 135, Essences.PHYSICAL_DAMAGE);
        put(Essences.LIGHTNING_DAMAGE, Branch.OFFENSE, Point.BLUE, 255, 135, Essences.FIRE_DAMAGE);
        put(Essences.IGNITION, Branch.OFFENSE, Point.GREEN, 165, 135, Essences.FIRE_DAMAGE);
        put(Essences.FORGE, Branch.OFFENSE, Point.GREEN, 120, 135, Essences.IGNITION);
        put(Shapes.CONTINGENCY_FIRE, Branch.OFFENSE, Point.GREEN, 165, 180, Essences.IGNITION);
        put(Essences.MAGIC_DAMAGE, Branch.OFFENSE, Point.BLUE, 390, 135, Essences.PHYSICAL_DAMAGE);
        put(Essences.FROST_DAMAGE, Branch.OFFENSE, Point.BLUE, 345, 135, Essences.MAGIC_DAMAGE);
        put(Essences.DROWN, Branch.OFFENSE, Point.BLUE, 435, 135, Essences.MAGIC_DAMAGE);
        put(Essences.BLIND, Branch.OFFENSE, Point.GREEN, 233, 180, Essences.FIRE_DAMAGE, Essences.LIGHTNING_DAMAGE);
        put(Shapes.AOE, Branch.OFFENSE, Point.GREEN, 300, 180,
                Essences.PHYSICAL_DAMAGE, Essences.FROST_DAMAGE, Essences.FIRE_DAMAGE,
                Essences.LIGHTNING_DAMAGE, Essences.MAGIC_DAMAGE);
        put(Essences.FREEZE, Branch.OFFENSE, Point.GREEN, 345, 180, Essences.FROST_DAMAGE);
        put(Essences.KNOCKBACK, Branch.OFFENSE, Point.GREEN, 390, 180, Essences.MAGIC_DAMAGE);
        put(Modifiers.SOLAR, Branch.OFFENSE, Point.RED, 210, 225, Essences.BLIND);
        put(Essences.STORM, Branch.OFFENSE, Point.RED, 255, 225, Essences.LIGHTNING_DAMAGE);
        put(Essences.ASTRAL_DISTORTION, Branch.OFFENSE, Point.GREEN, 367, 215,
                Essences.MAGIC_DAMAGE, Essences.FROST_DAMAGE);
        put(Essences.SILENCE, Branch.OFFENSE, Point.RED, 345, 245, Essences.ASTRAL_DISTORTION);
        put(Essences.FLING, Branch.OFFENSE, Point.GREEN, 390, 245, Essences.KNOCKBACK);
        put(Modifiers.VELOCITY_ADDED, Branch.OFFENSE, Point.RED, 390, 290, Essences.FLING);
        put(Essences.WATERY_GRAVE, Branch.OFFENSE, Point.GREEN, 435, 245, Essences.DROWN);
        put(Modifiers.PIERCING, Branch.OFFENSE, Point.RED, 323, 215, Essences.FREEZE);
        put(Shapes.BEAM, Branch.OFFENSE, Point.RED, 300, 270, Shapes.AOE);
        put(Modifiers.DAMAGE, Branch.OFFENSE, Point.RED, 300, 315, Shapes.BEAM);
        put(Essences.FURY, Branch.OFFENSE, Point.RED, 255, 315, Shapes.BEAM, Essences.STORM);
        put(Shapes.WAVE, Branch.OFFENSE, Point.RED, 367, 315, Shapes.BEAM, Essences.FLING);
        put(Essences.BLIZZARD, Branch.OFFENSE, Point.SILVER, 75, 45);
        put(Essences.FALLING_STAR, Branch.OFFENSE, Point.SILVER, 75, 90);
        put(Essences.FIRE_RAIN, Branch.OFFENSE, Point.SILVER, 75, 135);
        put(Modifiers.DISMEMBERING, Branch.OFFENSE, Point.SILVER, 75, 180);

        // ------------------------------------------------------------------ Defesa
        put(Shapes.SELF, Branch.DEFENSE, Point.BLUE, 267, 45);
        put(Essences.LEAP, Branch.DEFENSE, Point.BLUE, 222, 90, Shapes.SELF);
        put(Essences.REGENERATION, Branch.DEFENSE, Point.BLUE, 357, 90, Shapes.SELF);
        put(Essences.SHRINK, Branch.DEFENSE, Point.BLUE, 402, 90, Essences.REGENERATION);
        put(Essences.SLOWFALL, Branch.DEFENSE, Point.BLUE, 222, 135, Essences.LEAP);
        put(Essences.HEAL, Branch.DEFENSE, Point.BLUE, 357, 135, Essences.REGENERATION);
        put(Essences.LIFE_TAP, Branch.DEFENSE, Point.GREEN, 312, 135, Essences.HEAL);
        put(Modifiers.HEALING, Branch.DEFENSE, Point.RED, 402, 135, Essences.HEAL);
        put(Shapes.CONTINGENCY_DAMAGE, Branch.DEFENSE, Point.GREEN, 447, 180, Modifiers.HEALING);
        put(Essences.HASTE, Branch.DEFENSE, Point.BLUE, 177, 155, Essences.SLOWFALL);
        put(Essences.SLOW, Branch.DEFENSE, Point.BLUE, 132, 155, Essences.SLOWFALL);
        put(Essences.GRAVITY_WELL, Branch.DEFENSE, Point.GREEN, 222, 180, Essences.SLOWFALL);
        put(Essences.LIFE_DRAIN, Branch.DEFENSE, Point.GREEN, 312, 180, Essences.LIFE_TAP);
        put(Essences.DISPEL, Branch.DEFENSE, Point.GREEN, 357, 180, Essences.HEAL);
        put(Shapes.CONTINGENCY_FALL, Branch.DEFENSE, Point.GREEN, 267, 180, Essences.GRAVITY_WELL);
        put(Essences.SWIFT_SWIM, Branch.DEFENSE, Point.BLUE, 177, 200, Essences.HASTE);
        put(Essences.REPEL, Branch.DEFENSE, Point.GREEN, 132, 200, Essences.SLOW);
        put(Essences.LEVITATION, Branch.DEFENSE, Point.GREEN, 222, 225, Essences.GRAVITY_WELL);
        put(Essences.MANA_DRAIN, Branch.DEFENSE, Point.GREEN, 312, 225, Essences.LIFE_DRAIN);
        put(Shapes.ZONE, Branch.DEFENSE, Point.RED, 357, 225, Essences.DISPEL);
        put(Shapes.WALL, Branch.DEFENSE, Point.GREEN, 87, 200, Essences.REPEL);
        put(Essences.ACCELERATE, Branch.DEFENSE, Point.GREEN, 177, 245, Essences.SWIFT_SWIM);
        put(Essences.ENTANGLE, Branch.DEFENSE, Point.GREEN, 132, 245, Essences.REPEL);
        put(Essences.APPROPRIATION, Branch.DEFENSE, Point.RED, 87, 245, Essences.ENTANGLE);
        put(Essences.FLIGHT, Branch.DEFENSE, Point.RED, 222, 270, Essences.LEVITATION);
        put(Essences.SHIELD, Branch.DEFENSE, Point.BLUE, 357, 270, Shapes.ZONE);
        put(Shapes.CONTINGENCY_HEALTH, Branch.DEFENSE, Point.RED, 402, 270, Essences.SHIELD);
        put(Shapes.RUNE, Branch.DEFENSE, Point.GREEN, 157, 315, Essences.ACCELERATE, Essences.ENTANGLE);
        put(Modifiers.PROCS, Branch.DEFENSE, Point.GREEN, 157, 360, Shapes.RUNE);
        put(Modifiers.SPEED, Branch.DEFENSE, Point.RED, 202, 315, Essences.ACCELERATE, Essences.FLIGHT);
        put(Essences.REFLECT, Branch.DEFENSE, Point.RED, 357, 315, Essences.SHIELD);
        put(Essences.CHRONO_ANCHOR, Branch.DEFENSE, Point.RED, 312, 315, Essences.REFLECT);
        put(Modifiers.DURATION, Branch.DEFENSE, Point.RED, 312, 360, Essences.CHRONO_ANCHOR);
        put(Essences.MANA_LINK, Branch.DEFENSE, Point.SILVER, 30, 45);
        put(Essences.MANA_SHIELD, Branch.DEFENSE, Point.SILVER, 30, 90);
        put(Modifiers.BUFF_POWER, Branch.DEFENSE, Point.SILVER, 30, 135);

        // ------------------------------------------------------------------ Utilidade
        put(Shapes.TOUCH, Branch.UTILITY, Point.BLUE, 275, 75);
        put(Essences.DIG, Branch.UTILITY, Point.BLUE, 275, 120, Shapes.TOUCH);
        put(Essences.WIZARDS_AUTUMN, Branch.UTILITY, Point.BLUE, 315, 120, Essences.DIG);
        put(Modifiers.TARGET_NONSOLID_BLOCKS, Branch.UTILITY, Point.BLUE, 230, 75, Shapes.TOUCH);
        put(Essences.PLACE_BLOCK, Branch.UTILITY, Point.BLUE, 185, 93, Essences.DIG);
        put(Modifiers.FEATHER_TOUCH, Branch.UTILITY, Point.BLUE, 230, 137, Essences.DIG);
        put(Modifiers.MINING_POWER, Branch.UTILITY, Point.GREEN, 185, 137, Modifiers.FEATHER_TOUCH);
        put(Essences.LIGHT, Branch.UTILITY, Point.BLUE, 275, 165, Essences.DIG);
        put(Essences.NIGHT_VISION, Branch.UTILITY, Point.BLUE, 185, 165, Essences.LIGHT);
        put(Shapes.BINDING_PICKAXE, Branch.UTILITY, Point.BLUE, 275, 210, Essences.LIGHT);
        put(Essences.DISARM, Branch.UTILITY, Point.BLUE, 230, 210, Shapes.BINDING_PICKAXE);
        put(Essences.CHARM, Branch.UTILITY, Point.BLUE, 315, 235, Shapes.BINDING_PICKAXE);
        put(Essences.TRUE_SIGHT, Branch.UTILITY, Point.BLUE, 185, 210, Essences.NIGHT_VISION);
        put(Modifiers.LUNAR, Branch.UTILITY, Point.RED, 145, 210, Essences.TRUE_SIGHT);
        put(Essences.HARVEST_PLANTS, Branch.UTILITY, Point.GREEN, 365, 120, Shapes.BINDING_PICKAXE);
        put(Essences.PLOW, Branch.UTILITY, Point.BLUE, 365, 165, Shapes.BINDING_PICKAXE);
        put(Essences.PLANT, Branch.UTILITY, Point.BLUE, 365, 210, Shapes.BINDING_PICKAXE);
        put(Essences.CREATE_WATER, Branch.UTILITY, Point.GREEN, 365, 255, Shapes.BINDING_PICKAXE);
        put(Essences.DROUGHT, Branch.UTILITY, Point.GREEN, 365, 300, Shapes.BINDING_PICKAXE);
        put(Essences.BANISH_RAIN, Branch.UTILITY, Point.GREEN, 365, 345, Essences.DROUGHT);
        put(Essences.WATER_BREATHING, Branch.UTILITY, Point.BLUE, 410, 345, Essences.DROUGHT);
        put(Essences.GROW, Branch.UTILITY, Point.RED, 410, 210,
                Essences.DROUGHT, Essences.CREATE_WATER, Essences.PLANT, Essences.PLOW, Essences.HARVEST_PLANTS);
        put(Shapes.CHAIN, Branch.UTILITY, Point.RED, 455, 210, Essences.GROW);
        put(Essences.INVISIBILITY, Branch.UTILITY, Point.GREEN, 185, 255, Essences.TRUE_SIGHT);
        put(Essences.RANDOM_TELEPORT, Branch.UTILITY, Point.BLUE, 185, 300, Essences.INVISIBILITY);
        put(Essences.ATTRACT, Branch.UTILITY, Point.GREEN, 245, 300, Shapes.BINDING_PICKAXE);
        put(Essences.TELEKINESIS, Branch.UTILITY, Point.GREEN, 305, 300, Shapes.BINDING_PICKAXE);
        put(Essences.BLINK, Branch.UTILITY, Point.GREEN, 185, 345, Essences.RANDOM_TELEPORT);
        put(Modifiers.RANGE, Branch.UTILITY, Point.RED, 140, 345, Essences.BLINK);
        put(Shapes.CHANNEL, Branch.UTILITY, Point.GREEN, 275, 345, Essences.ATTRACT, Essences.TELEKINESIS);
        put(Modifiers.RADIUS, Branch.UTILITY, Point.RED, 275, 390, Shapes.CHANNEL);
        put(Essences.TRANSPLACE, Branch.UTILITY, Point.BLUE, 185, 390, Essences.BLINK);
        put(Essences.MARK, Branch.UTILITY, Point.GREEN, 155, 435, Essences.TRANSPLACE);
        put(Essences.RECALL, Branch.UTILITY, Point.GREEN, 215, 435, Essences.TRANSPLACE);
        put(Essences.DIVINE_INTERVENTION, Branch.UTILITY, Point.RED, 172, 480, Essences.RECALL, Essences.MARK);
        put(Essences.ENDER_INTERVENTION, Branch.UTILITY, Point.RED, 198, 480, Essences.RECALL, Essences.MARK);
        put(Shapes.CONTINGENCY_DEATH, Branch.UTILITY, Point.RED, 198, 524, Essences.ENDER_INTERVENTION);
        put(Essences.DAYLIGHT, Branch.UTILITY, Point.SILVER, 75, 45);
        put(Essences.MOONRISE, Branch.UTILITY, Point.SILVER, 75, 90);
        put(Modifiers.PROSPERITY, Branch.UTILITY, Point.SILVER, 75, 135);

        // A Cor mora no quarto ramo do original — o dos Talentos, que não é feito de peças de feitiço e
        // não está portado. Fica na Utilidade, ao pé do Toque, que é onde um arcanista a acharia cedo.
        put(Modifiers.COLOUR, Branch.UTILITY, Point.BLUE, 155, 75, Shapes.TOUCH);

        // As outras cinco ferramentas vinculadas. No original o Vínculo é UMA perícia que dá
        // a ferramenta conforme o que se estiver segurando; aqui são seis peças, e as outras
        // cinco se penduram na primeira, que fica no lugar que o Vínculo tinha.
        put(Shapes.BINDING_AXE, Branch.UTILITY, Point.BLUE, 320, 232, Shapes.BINDING_PICKAXE);
        put(Shapes.BINDING_SWORD, Branch.UTILITY, Point.BLUE, 230, 232, Shapes.BINDING_PICKAXE);
        put(Shapes.BINDING_SHOVEL, Branch.UTILITY, Point.GREEN, 342, 268, Shapes.BINDING_AXE);
        put(Shapes.BINDING_HOE, Branch.UTILITY, Point.GREEN, 208, 268, Shapes.BINDING_SWORD);
        put(Shapes.BINDING_BOW, Branch.UTILITY, Point.RED, 275, 292, Shapes.BINDING_PICKAXE);
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
