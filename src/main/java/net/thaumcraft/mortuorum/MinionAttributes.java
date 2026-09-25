package net.thaumcraft.mortuorum;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * O que cada peça soma ao lacaio: os {@code setAttributes} de cada {@code NecroEntity} do Necromancy.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/am-atributos.js} a partir do jar original — não se escreve à
 * mão. Cada linha é vida, alcance de visão, resistência a empurrão, velocidade e dano, na ordem do
 * {@code addAttributeMods}.
 */
public final class MinionAttributes {
    /** O que uma peça soma: os cinco números do original. */
    public record Bonus(double health, double followRange, double knockback, double speed, double damage) {
        public static final Bonus NONE = new Bonus(0.0, 0.0, 0.0, 0.0, 0.0);
    }

    /** Por bicho e por lugar do corpo. */
    private static final Map<String, Map<String, Bonus>> TABLE = new LinkedHashMap<>();

    private MinionAttributes() {
    }

    static {
        put("Cow", "Head", 0.50, 1.00, 0.00, 0.00, 0.00);
        put("Cow", "Torso", 1.00, 0.00, 0.00, 0.00, 0.00);
        put("Cow", "ArmLeft", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Cow", "ArmRight", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Cow", "Legs", 0.25, 0.00, 1.00, 3.00, 0.00);
        put("Creeper", "Head", 0.50, 1.00, 0.00, 0.00, 0.00);
        put("Creeper", "Torso", 1.00, 0.00, 0.00, 0.00, 0.00);
        put("Creeper", "Legs", 0.25, 0.00, 3.00, 3.00, 0.00);
        put("Enderman", "Head", 1.00, 1.00, 1.00, 1.00, 0.50);
        put("Enderman", "Torso", 4.00, 0.00, 1.00, 0.00, 0.00);
        put("Enderman", "ArmLeft", 1.00, 0.00, 0.00, 0.00, 1.50);
        put("Enderman", "ArmRight", 1.00, 0.00, 0.00, 0.00, 1.50);
        put("Enderman", "Legs", 1.00, 0.00, 4.00, 3.00, 0.00);
        put("Pig", "Head", 0.50, 1.00, 0.00, 0.00, 0.00);
        put("Pig", "Torso", 1.00, 0.00, 0.00, 0.00, 0.00);
        put("Pig", "ArmLeft", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Pig", "ArmRight", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Pig", "Legs", 0.25, 0.00, 1.00, 3.00, 0.00);
        put("Pigzombie", "Head", 1.50, 1.00, 0.00, 0.00, 0.50);
        put("Pigzombie", "Torso", 3.00, 0.00, 0.00, 0.00, 0.00);
        put("Pigzombie", "ArmLeft", 0.50, 0.00, 0.00, 0.00, 0.75);
        put("Pigzombie", "ArmRight", 0.50, 0.00, 0.00, 0.00, 0.75);
        put("Pigzombie", "Legs", 1.50, 0.00, 3.00, 3.00, 0.00);
        put("Skeleton", "Head", 1.00, 1.00, 0.00, 0.00, 0.00);
        put("Skeleton", "Torso", 1.50, 0.00, 0.00, 0.00, 0.00);
        put("Skeleton", "ArmLeft", 0.50, 0.00, 0.00, 0.00, 0.50);
        put("Skeleton", "ArmRight", 0.50, 0.00, 0.00, 0.00, 0.50);
        put("Skeleton", "Legs", 0.75, 0.00, 3.00, 3.00, 0.00);
        put("Spider", "Head", 0.50, 1.00, 0.00, 0.00, 0.50);
        put("Spider", "Torso", 2.00, 0.00, 0.00, 0.00, 0.00);
        put("Spider", "Legs", 0.50, 0.00, 1.00, 2.00, 0.50);
        put("Zombie", "Head", 1.00, 1.00, 0.00, 0.00, 1.00);
        put("Zombie", "Torso", 2.00, 0.00, 0.00, 0.00, 0.00);
        put("Zombie", "ArmLeft", 0.50, 0.00, 0.00, 0.00, 0.50);
        put("Zombie", "ArmRight", 0.50, 0.00, 0.00, 0.00, 0.50);
        put("Zombie", "Legs", 1.00, 0.00, 3.00, 3.00, 0.00);
        put("Chicken", "Head", 0.50, 1.00, 0.00, 0.00, 0.25);
        put("Chicken", "Torso", 0.25, 0.00, 0.00, 0.00, 0.00);
        put("Chicken", "ArmLeft", 0.10, 0.00, 0.00, 0.00, 0.00);
        put("Chicken", "ArmRight", 0.10, 0.00, 0.00, 0.00, 0.00);
        put("Chicken", "Legs", 0.10, 0.00, 0.00, 0.50, 0.00);
        put("Villager", "Head", 0.50, 1.00, 0.00, 0.00, 0.00);
        put("Villager", "Torso", 1.00, 0.00, 0.00, 0.00, 0.00);
        put("Villager", "ArmLeft", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Villager", "ArmRight", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Villager", "Legs", 0.25, 0.00, 1.00, 3.00, 0.00);
        put("Witch", "Head", 1.50, 1.00, 0.00, 0.00, 0.00);
        put("Witch", "Torso", 2.00, 0.00, 0.00, 0.00, 0.00);
        put("Witch", "ArmLeft", 0.50, 0.00, 0.00, 0.00, 0.75);
        put("Witch", "ArmRight", 0.50, 0.00, 0.00, 0.00, 0.75);
        put("Witch", "Legs", 1.50, 0.00, 3.00, 3.00, 0.00);
        put("Squid", "Head", 0.50, 1.00, 0.00, 0.00, 0.50);
        put("Squid", "Torso", 2.00, 0.00, 0.00, 0.00, 0.00);
        put("Squid", "Legs", 0.50, 0.00, 1.00, 2.00, 0.50);
        put("CaveSpider", "Head", 0.50, 1.00, 0.00, 0.00, 0.50);
        put("CaveSpider", "Torso", 2.00, 0.00, 0.00, 0.00, 0.00);
        put("CaveSpider", "Legs", 0.50, 0.00, 1.00, 2.00, 0.50);
        put("Sheep", "Head", 0.50, 1.00, 0.00, 0.00, 0.00);
        put("Sheep", "Torso", 1.00, 0.00, 0.00, 0.00, 0.00);
        put("Sheep", "ArmLeft", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Sheep", "ArmRight", 0.25, 0.00, 0.00, 0.00, 0.25);
        put("Sheep", "Legs", 0.25, 0.00, 1.00, 3.00, 0.00);
        put("IronGolem", "Head", 1.00, 1.00, 2.00, 0.00, 0.50);
        put("IronGolem", "Torso", 5.00, 0.00, 2.00, 0.00, 0.00);
        put("IronGolem", "ArmLeft", 1.00, 0.00, 1.00, 0.00, 1.50);
        put("IronGolem", "ArmRight", 1.00, 0.00, 1.00, 0.00, 1.50);
        put("IronGolem", "Legs", 4.00, 0.00, 3.00, 1.00, 0.00);
        put("Wolf", "Head", 2.00, 1.00, 1.00, 1.00, 2.00);
    }

    private static void put(String mob, String place, double health, double followRange, double knockback,
                           double speed, double damage) {
        TABLE.computeIfAbsent(mob, chave -> new LinkedHashMap<>())
                .put(place, new Bonus(health, followRange, knockback, speed, damage));
    }

    /** O que a peça daquele bicho, naquele lugar, soma. */
    public static Bonus of(String mob, String place) {
        Map<String, Bonus> doBicho = TABLE.get(mob);
        if (doBicho == null) return Bonus.NONE;
        return doBicho.getOrDefault(place, Bonus.NONE);
    }

    public static int size() {
        return TABLE.values().stream().mapToInt(Map::size).sum();
    }
}
