package net.thaumcraft.item;

import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

import java.util.List;
import java.util.Map;

/**
 * As melhorias de foco da 4.2.3.5: os {@code FocusUpgradeType} (número, ícone e aspectos de cada uma) e, por foco, o que
 * cabe em cada um dos cinco postos (o {@code getPossibleUpgradesByRank}).
 *
 * <p>GERADO por {@code scratchpad/melhorias-foco.js} a partir do jar — não editar à mão.
 */
public final class FocusUpgradeTable {
    private FocusUpgradeTable() {
    }

    /** Um tipo de melhoria: o número guardado no foco, o nome (das chaves de texto e do ícone) e os aspectos. */
    public record Type(short id, String name, String icon, AspectList aspects) {
    }

    public static final Type POTENCY = new Type((short) 0, "potency", "potency", new AspectList().add(Aspects.WEAPON, 1));
    public static final Type FRUGAL = new Type((short) 1, "frugal", "frugal", new AspectList().add(Aspects.HUNGER, 1));
    public static final Type TREASURE = new Type((short) 2, "treasure", "treasure", new AspectList().add(Aspects.GREED, 1));
    public static final Type ENLARGE = new Type((short) 3, "enlarge", "enlarge", new AspectList().add(Aspects.TRAVEL, 1));
    public static final Type ALCHEMISTSFIRE = new Type((short) 4, "alchemistsfire", "alchemistsfire", new AspectList().add(Aspects.ENERGY, 1).add(Aspects.SLIME, 1));
    public static final Type ALCHEMISTSFROST = new Type((short) 5, "alchemistsfrost", "alchemistsfrost", new AspectList().add(Aspects.COLD, 1).add(Aspects.TRAP, 1));
    public static final Type ARCHITECT = new Type((short) 6, "architect", "architect", new AspectList().add(Aspects.CRAFT, 1));
    public static final Type EXTEND = new Type((short) 7, "extend", "extend", new AspectList().add(Aspects.EXCHANGE, 1));
    public static final Type SILKTOUCH = new Type((short) 8, "silktouch", "silktouch", new AspectList().add(Aspects.GREED, 1));
    public static final Type FIREBALL = new Type((short) 9, "fireball", "fireball", new AspectList().add(Aspects.DARKNESS, 1));
    public static final Type FIREBEAM = new Type((short) 10, "firebeam", "firebeam", new AspectList().add(Aspects.ENERGY, 1).add(Aspects.AIR, 1));
    public static final Type SCATTERSHOT = new Type((short) 11, "scattershot", "scattershot", new AspectList().add(Aspects.COLD, 1).add(Aspects.WEAPON, 1));
    public static final Type ICEBOULDER = new Type((short) 12, "iceboulder", "iceboulder", new AspectList().add(Aspects.COLD, 1).add(Aspects.CRYSTAL, 1));
    public static final Type BATBOMBS = new Type((short) 13, "batbombs", "batbombs", new AspectList().add(Aspects.ENERGY, 1).add(Aspects.TRAP, 1));
    public static final Type DEVILBATS = new Type((short) 14, "devilbats", "devilbats", new AspectList().add(Aspects.ARMOR, 1));
    public static final Type NIGHTSHADE = new Type((short) 15, "nightshade", "nightshade", new AspectList().add(Aspects.LIFE, 1).add(Aspects.POISON, 1).add(Aspects.MAGIC, 1));
    public static final Type SEEKER = new Type((short) 16, "seeker", "seeker", new AspectList().add(Aspects.SENSES, 1).add(Aspects.MIND, 1));
    public static final Type CHAINLIGHTNING = new Type((short) 17, "chainlightning", "chainlightning", new AspectList().add(Aspects.WEATHER, 1));
    public static final Type EARTHSHOCK = new Type((short) 18, "earthshock", "earthshock", new AspectList().add(Aspects.WEATHER, 1));
    public static final Type VAMPIREBATS = new Type((short) 19, "vampirebats", "vampirebats", new AspectList().add(Aspects.HUNGER, 1).add(Aspects.LIFE, 1));
    public static final Type DOWSING = new Type((short) 20, "dowsing", "dowsing", new AspectList().add(Aspects.MINE, 1));

    /** Todos, pelo número. */
    public static final Map<Short, Type> BY_ID = Map.ofEntries(
            Map.entry((short) 0, POTENCY),
            Map.entry((short) 1, FRUGAL),
            Map.entry((short) 2, TREASURE),
            Map.entry((short) 3, ENLARGE),
            Map.entry((short) 4, ALCHEMISTSFIRE),
            Map.entry((short) 5, ALCHEMISTSFROST),
            Map.entry((short) 6, ARCHITECT),
            Map.entry((short) 7, EXTEND),
            Map.entry((short) 8, SILKTOUCH),
            Map.entry((short) 9, FIREBALL),
            Map.entry((short) 10, FIREBEAM),
            Map.entry((short) 11, SCATTERSHOT),
            Map.entry((short) 12, ICEBOULDER),
            Map.entry((short) 13, BATBOMBS),
            Map.entry((short) 14, DEVILBATS),
            Map.entry((short) 15, NIGHTSHADE),
            Map.entry((short) 16, SEEKER),
            Map.entry((short) 17, CHAINLIGHTNING),
            Map.entry((short) 18, EARTHSHOCK),
            Map.entry((short) 19, VAMPIREBATS),
            Map.entry((short) 20, DOWSING));

    /** Por foco, os cinco postos. */
    public static final Map<String, List<List<Type>>> RANKS = Map.ofEntries(
            Map.entry("fire", List.of(
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, ALCHEMISTSFIRE),
                    List.of(FRUGAL, POTENCY, FIREBALL, FIREBEAM),
                    List.of(FRUGAL, POTENCY, ALCHEMISTSFIRE),
                    List.of(FRUGAL, POTENCY))),
            Map.entry("frost", List.of(
                    List.of(FRUGAL, POTENCY, ALCHEMISTSFROST),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, SCATTERSHOT, ICEBOULDER, ALCHEMISTSFROST),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, ALCHEMISTSFROST))),
            Map.entry("shock", List.of(
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, CHAINLIGHTNING, EARTHSHOCK),
                    List.of(FRUGAL, POTENCY, ENLARGE),
                    List.of(FRUGAL, POTENCY, ENLARGE))),
            Map.entry("excavation", List.of(
                    List.of(FRUGAL, POTENCY, TREASURE),
                    List.of(FRUGAL, POTENCY, ENLARGE),
                    List.of(FRUGAL, POTENCY, TREASURE, DOWSING),
                    List.of(FRUGAL, POTENCY, ENLARGE),
                    List.of(FRUGAL, POTENCY, TREASURE, SILKTOUCH))),
            Map.entry("portable_hole", List.of(
                    List.of(FRUGAL, ENLARGE, EXTEND),
                    List.of(FRUGAL, ENLARGE, EXTEND),
                    List.of(FRUGAL, ENLARGE, EXTEND),
                    List.of(FRUGAL, ENLARGE, EXTEND),
                    List.of(FRUGAL, ENLARGE, EXTEND))),
            Map.entry("trade", List.of(
                    List.of(FRUGAL, ENLARGE),
                    List.of(FRUGAL, ENLARGE),
                    List.of(FRUGAL, ENLARGE, TREASURE, ARCHITECT),
                    List.of(FRUGAL, ENLARGE),
                    List.of(FRUGAL, ENLARGE, SILKTOUCH))),
            Map.entry("warding", List.of(
                    List.of(FRUGAL),
                    List.of(FRUGAL, ARCHITECT),
                    List.of(FRUGAL, ENLARGE),
                    List.of(FRUGAL, ENLARGE),
                    List.of(FRUGAL, ENLARGE))),
            Map.entry("primal", List.of(
                    List.of(FRUGAL),
                    List.of(FRUGAL),
                    List.of(FRUGAL, SEEKER),
                    List.of(FRUGAL),
                    List.of(FRUGAL))),
            Map.entry("pech", List.of(
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, EXTEND),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, EXTEND),
                    List.of(FRUGAL, POTENCY, NIGHTSHADE))),
            Map.entry("hellbat", List.of(
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, BATBOMBS, DEVILBATS),
                    List.of(FRUGAL, POTENCY),
                    List.of(FRUGAL, POTENCY, VAMPIREBATS))));
}
