package net.thaumcraft.api.aspects;

import java.util.Collection;

/**
 * A tabela de aspectos do Thaumcraft 4.2.3.5, copiada tal e qual: mesmos nomes em latim, mesmas cores e
 * mesmos pares de composição.
 *
 * <p>Os seis primeiros são os primários; os outros 42 nascem do encontro de dois. Mudar um número aqui
 * muda toda a árvore de pesquisa do jogo, então esta tabela não se mexe sem olhar o original.
 */
public final class Aspects {
    public static final Aspect AIR = Aspect.primal("aer", 0xFFFF7E, "e", 1);
    public static final Aspect EARTH = Aspect.primal("terra", 0x56C000, "2", 1);
    public static final Aspect FIRE = Aspect.primal("ignis", 0xFF5A01, "c", 1);
    public static final Aspect WATER = Aspect.primal("aqua", 0x3CD4FC, "3", 1);
    public static final Aspect ORDER = Aspect.primal("ordo", 0xD5D4EC, "7", 1);
    public static final Aspect ENTROPY = Aspect.primal("perditio", 0x404040, "8", 771);
    public static final Aspect VOID = Aspect.compoundDark("vacuos", 0x888888, AIR, ENTROPY);
    public static final Aspect LIGHT = Aspect.compound("lux", 0xFFF663, AIR, FIRE);
    public static final Aspect WEATHER = Aspect.compound("tempestas", 0xFFFFFF, AIR, WATER);
    public static final Aspect MOTION = Aspect.compound("motus", 0xCDCCF4, AIR, ORDER);
    public static final Aspect COLD = Aspect.compound("gelum", 0xE1FFFF, FIRE, ENTROPY);
    public static final Aspect CRYSTAL = Aspect.compound("vitreus", 0x80FFFF, EARTH, ORDER);
    public static final Aspect LIFE = Aspect.compound("victus", 0xDE0005, WATER, EARTH);
    public static final Aspect POISON = Aspect.compound("venenum", 0x89F000, WATER, ENTROPY);
    public static final Aspect ENERGY = Aspect.compound("potentia", 0xC0FFFF, ORDER, FIRE);
    public static final Aspect EXCHANGE = Aspect.compound("permutatio", 0x578357, ENTROPY, ORDER);
    public static final Aspect METAL = Aspect.compound("metallum", 0xB5B5CD, EARTH, CRYSTAL);
    public static final Aspect DEATH = Aspect.compound("mortuus", 0x887788, LIFE, ENTROPY);
    public static final Aspect FLIGHT = Aspect.compound("volatus", 0xE7E7D7, AIR, MOTION);
    public static final Aspect DARKNESS = Aspect.compound("tenebrae", 0x222222, VOID, LIGHT);
    public static final Aspect SOUL = Aspect.compound("spiritus", 0xEBEBFB, LIFE, DEATH);
    public static final Aspect HEAL = Aspect.compound("sano", 0xFF2F34, LIFE, ORDER);
    public static final Aspect TRAVEL = Aspect.compound("iter", 0xE0585B, MOTION, EARTH);
    public static final Aspect ELDRITCH = Aspect.compound("alienis", 0x805080, VOID, DARKNESS);
    public static final Aspect MAGIC = Aspect.compound("praecantatio", 0x9700C0, VOID, ENERGY);
    public static final Aspect AURA = Aspect.compound("auram", 0xFFC0FF, MAGIC, AIR);
    public static final Aspect TAINT = Aspect.compound("vitium", 0x800080, MAGIC, ENTROPY);
    public static final Aspect SLIME = Aspect.compound("limus", 0x01F800, LIFE, WATER);
    public static final Aspect PLANT = Aspect.compound("herba", 0x01AC00, LIFE, EARTH);
    public static final Aspect TREE = Aspect.compound("arbor", 0x876531, AIR, PLANT);
    public static final Aspect BEAST = Aspect.compound("bestia", 0x9F6409, MOTION, LIFE);
    public static final Aspect FLESH = Aspect.compound("corpus", 0xEE478D, DEATH, BEAST);
    public static final Aspect UNDEAD = Aspect.compound("exanimis", 0x3A4000, MOTION, DEATH);
    public static final Aspect MIND = Aspect.compound("cognitio", 0xFFC2B3, FIRE, SOUL);
    public static final Aspect SENSES = Aspect.compound("sensus", 0x0FD9FF, AIR, SOUL);
    public static final Aspect MAN = Aspect.compound("humanus", 0xFFD7C0, BEAST, MIND);
    public static final Aspect CROP = Aspect.compound("messis", 0xE1B371, PLANT, MAN);
    public static final Aspect MINE = Aspect.compound("perfodio", 0xDCD2D8, MAN, EARTH);
    public static final Aspect TOOL = Aspect.compound("instrumentum", 0x4040EE, MAN, ORDER);
    public static final Aspect HARVEST = Aspect.compound("meto", 0xEEAD82, CROP, TOOL);
    public static final Aspect WEAPON = Aspect.compound("telum", 0xC05050, TOOL, FIRE);
    public static final Aspect ARMOR = Aspect.compound("tutamen", 0x00C0C0, TOOL, EARTH);
    public static final Aspect HUNGER = Aspect.compound("fames", 0x9A0305, LIFE, VOID);
    public static final Aspect GREED = Aspect.compound("lucrum", 0xE6BE44, MAN, HUNGER);
    public static final Aspect CRAFT = Aspect.compound("fabrico", 0x809D80, MAN, TOOL);
    public static final Aspect CLOTH = Aspect.compound("pannus", 0xEAEAC2, TOOL, BEAST);
    public static final Aspect MECHANISM = Aspect.compound("machina", 0x8080A0, MOTION, TOOL);
    public static final Aspect TRAP = Aspect.compound("vinculum", 0x9A8080, MOTION, ENTROPY);

    private Aspects() {
    }

    /** Acorda a tabela. Como os campos são estáticos, basta tocar na classe. */
    public static void init() {
    }

    public static Collection<Aspect> all() {
        return Aspect.ASPECTS.values();
    }

    public static int count() {
        return Aspect.ASPECTS.size();
    }
}
