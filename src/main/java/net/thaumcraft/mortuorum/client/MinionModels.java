package net.thaumcraft.mortuorum.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * As peças de que o lacaio é montado: os {@code initHead}, {@code initTorso}, {@code initArmLeft},
 * {@code initArmRight} e {@code initLegs} de cada {@code NecroEntity} do Necromancy, com a folha de cada bicho
 * e os pontos onde uma peça pendura a outra.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/am-modelos.js} a partir do jar original — não se escreve à mão.
 */
public final class MinionModels {
    /** Uma caixa: o canto na folha, o canto no mundo, o tamanho e o quanto ela engorda. */
    public record Cube(int u, int v, float x, float y, float z, int w, int h, int d, float inflate) {
    }

    /** Um pedaço de uma peça: o ponto de giro, o giro fixo, se é espelhado, as caixas e o que pende dele. */
    public record Piece(float px, float py, float pz, float rx, float ry, float rz, boolean mirror,
                       List<Cube> cubes, List<Piece> children) {
    }

    /** Um bicho: a folha dele, o tamanho dela, onde ele pendura as peças e o que tem em cada lugar. */
    public record Mob(String texture, int textureWidth, int textureHeight, Family family, float[] torsoPos,
                      float[] armLeftPos, float[] armRightPos, float[] headPos, Map<String, List<Piece>> limbs) {
    }

    /** Que {@code setRotationAngles} o bicho usa: o do {@code NecroEntity} de que ele desce. */
    public enum Family {
        BIPED, QUADRUPED, SPIDER, CHICKEN, CREEPER, VILLAGER, ENDERMAN
    }

    /** Os cinco lugares do corpo, na ordem em que o original os desenha. */
    public static final List<String> PLACES = List.of("Head", "Torso", "ArmLeft", "ArmRight", "Legs");

    private static final Map<String, Mob> MOBS = new LinkedHashMap<>();

    private MinionModels() {
    }

    static {
        // Cow
        Map<String, List<Piece>> cow = new LinkedHashMap<>();
        cow.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 6, 0.0f), new Cube(22, 0, -5.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f), new Cube(22, 0, 4.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f)), List.of())));
        cow.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 4, -2.0f, -12.0f, -12.0f, 12, 18, 10, 0.0f), new Cube(52, 0, 2.0f, 2.0f, -13.0f, 4, 6, 1, 0.0f)), List.of())));
        cow.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        cow.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        cow.put("Legs", List.of(
                new Piece(-4.0f, 10.0f, 2.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(4.0f, 10.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Cow", new Mob("textures/entity/cow/cow_temperate.png", 64, 32, Family.QUADRUPED,
                new float[]{-4.0f, -2.0f, 0.0f}, new float[]{-1.0f, 12.0f, -10.0f},
                new float[]{5.0f, 12.0f, -10.0f}, new float[]{4.0f, 4.0f, -14.0f}, cow));

        // Creeper
        Map<String, List<Piece>> creeper = new LinkedHashMap<>();
        creeper.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.0f)), List.of())));
        creeper.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        creeper.put("Legs", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, 16.0f, 2.0f, 4, 6, 4, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -4.0f, 16.0f, 2.0f, 4, 6, 4, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -4.0f, 16.0f, -6.0f, 4, 6, 4, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, 16.0f, -6.0f, 4, 6, 4, 0.0f)), List.of())));
        MOBS.put("Creeper", new Mob("textures/entity/creeper/creeper.png", 64, 32, Family.CREEPER,
                new float[]{-4.0f, 4.0f, -2.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, creeper));

        // Enderman
        Map<String, List<Piece>> enderman = new LinkedHashMap<>();
        enderman.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -7.0f, -4.0f, 8, 8, 8, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -4.0f, -3.0f, -4.0f, 8, 8, 8, -0.5f)), List.of())));
        enderman.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        enderman.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(56, 0, 2.0f, 0.0f, -1.0f, 2, 30, 2, 0.0f)), List.of())));
        enderman.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(56, 0, 0.0f, 0.0f, -1.0f, 2, 30, 2, 0.0f)), List.of())));
        enderman.put("Legs", List.of(
                new Piece(2.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(56, 0, -1.0f, -4.0f, 1.0f, 2, 30, 2, 0.0f)), List.of()),
                new Piece(-2.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(56, 0, -1.0f, -4.0f, 1.0f, 2, 30, 2, 0.0f)), List.of())));
        MOBS.put("Enderman", new Mob("textures/entity/enderman/enderman.png", 64, 32, Family.ENDERMAN,
                new float[]{-4.0f, -18.0f, 0.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, enderman));

        // Pig
        Map<String, List<Piece>> pig = new LinkedHashMap<>();
        pig.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, -2.0f, 0.0f, -5.0f, 4, 3, 1, 0.0f)), List.of())));
        pig.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(28, 8, -1.0f, -12.0f, -12.0f, 10, 16, 8, 0.0f)), List.of())));
        pig.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        pig.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        pig.put("Legs", List.of(
                new Piece(-3.0f, 10.0f, 3.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(3.0f, 10.0f, 3.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Pig", new Mob("textures/entity/pig/pig_temperate.png", 64, 32, Family.QUADRUPED,
                new float[]{-4.0f, 4.0f, 0.0f}, new float[]{-1.0f, 12.0f, -10.0f},
                new float[]{5.0f, 12.0f, -10.0f}, new float[]{4.0f, 0.0f, -14.0f}, pig));

        // Pigzombie
        Map<String, List<Piece>> pigzombie = new LinkedHashMap<>();
        pigzombie.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.5f)), List.of())));
        pigzombie.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        pigzombie.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        pigzombie.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        pigzombie.put("Legs", List.of(
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -4.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Pigzombie", new Mob("textures/entity/piglin/zombified_piglin.png", 64, 64, Family.BIPED,
                new float[]{-4.0f, -2.0f, -2.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, pigzombie));

        // Skeleton
        Map<String, List<Piece>> skeleton = new LinkedHashMap<>();
        skeleton.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.5f)), List.of())));
        skeleton.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        skeleton.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(40, 16, 2.0f, 0.0f, -1.0f, 2, 12, 2, 0.0f)), List.of())));
        skeleton.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(40, 16, 0.0f, 0.0f, -1.0f, 2, 12, 2, 0.0f)), List.of())));
        skeleton.put("Legs", List.of(
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -3.0f, -2.0f, -1.0f, 2, 12, 2, 0.0f)), List.of()),
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 1.0f, -2.0f, -1.0f, 2, 12, 2, 0.0f)), List.of())));
        MOBS.put("Skeleton", new Mob("textures/entity/skeleton/skeleton.png", 64, 32, Family.BIPED,
                new float[]{-4.0f, -2.0f, -2.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, skeleton));

        // Spider
        Map<String, List<Piece>> spider = new LinkedHashMap<>();
        spider.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 4, -4.0f, -4.0f, -6.0f, 8, 8, 8, 0.0f)), List.of())));
        spider.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 12, -1.0f, 4.0f, 0.0f, 10, 8, 12, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, 1.0f, 5.0f, -6.0f, 6, 6, 6, 0.0f)), List.of())));
        spider.put("Legs", List.of(
                new Piece(-4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of())));
        MOBS.put("Spider", new Mob("textures/entity/spider/spider.png", 64, 32, Family.SPIDER,
                new float[]{-4.0f, 6.0f, 3.0f}, new float[]{-1.0f, 10.0f, -6.0f},
                new float[]{5.0f, 10.0f, -6.0f}, new float[]{4.0f, 8.0f, -7.0f}, spider));

        // Zombie
        Map<String, List<Piece>> zombie = new LinkedHashMap<>();
        zombie.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 0, -4.0f, -4.0f, -4.0f, 8, 8, 8, 0.5f)), List.of())));
        zombie.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        zombie.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        zombie.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        zombie.put("Legs", List.of(
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -4.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Zombie", new Mob("textures/entity/zombie/zombie.png", 64, 64, Family.BIPED,
                new float[]{-4.0f, -2.0f, -2.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, zombie));

        // Chicken
        Map<String, List<Piece>> chicken = new LinkedHashMap<>();
        chicken.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -2.0f, -2.0f, -2.0f, 4, 6, 3, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(14, 0, -2.0f, 0.0f, -4.0f, 4, 2, 2, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(14, 4, -1.0f, 2.0f, -3.0f, 2, 2, 2, 0.0f)), List.of())));
        chicken.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 9, 1.0f, -2.0f, -12.0f, 6, 8, 6, 0.0f)), List.of())));
        chicken.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(24, 13, 3.0f, 0.0f, -3.0f, 1, 4, 6, 0.0f)), List.of())));
        chicken.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(24, 13, 0.0f, 0.0f, -3.0f, 1, 4, 6, 0.0f)), List.of())));
        chicken.put("Legs", List.of(
                new Piece(0.0f, 19.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(26, 0, 0.5f, -1.0f, -1.0f, 3, 5, 3, 0.0f)), List.of()),
                new Piece(0.0f, 19.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(26, 0, -1.5f, -1.0f, -1.0f, 3, 5, 3, 0.0f)), List.of())));
        MOBS.put("Chicken", new Mob("textures/entity/chicken/chicken_temperate.png", 64, 32, Family.CHICKEN,
                new float[]{-3.0f, 8.0f, 0.0f}, new float[]{-3.0f, 6.0f, 2.0f},
                new float[]{7.0f, 6.0f, 2.0f}, new float[]{4.0f, 4.0f, -2.0f}, chicken));

        // Villager
        Map<String, List<Piece>> villager = new LinkedHashMap<>();
        villager.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -6.0f, -4.0f, 8, 10, 8, 0.0f)), List.of()),
                new Piece(0.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(24, 0, -1.0f, 3.0f, -6.0f, 2, 4, 2, 0.0f)), List.of())));
        villager.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 20, 0.0f, 0.0f, -1.0f, 8, 12, 6, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 38, 0.0f, 0.0f, -1.0f, 8, 18, 6, 0.5f)), List.of())));
        villager.put("ArmLeft", List.of(
                new Piece(0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(44, 22, 0.0f, -2.0f, -2.0f, 4, 8, 4, 0.0f), new Cube(44, 22, 4.0f, 2.0f, -2.0f, 4, 4, 4, 0.0f)), List.of())));
        villager.put("ArmRight", List.of(
                new Piece(0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(44, 22, 0.0f, -2.0f, -2.0f, 4, 8, 4, 0.0f), new Cube(44, 22, -4.0f, 2.0f, -2.0f, 4, 4, 4, 0.0f)), List.of())));
        villager.put("Legs", List.of(
                new Piece(-2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 22, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 22, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Villager", new Mob("textures/entity/villager/villager.png", 64, 64, Family.VILLAGER,
                new float[]{-4.0f, 0.0f, -2.0f}, new float[]{-4.0f, 0.0f, 0.0f},
                new float[]{8.0f, 0.0f, 0.0f}, new float[]{4.0f, -4.0f, 2.0f}, villager));

        // Witch
        Map<String, List<Piece>> witch = new LinkedHashMap<>();
        witch.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -6.0f, -4.0f, 8, 10, 8, 0.0f)), List.of(new Piece(-5.0f, -6.03125f, -5.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 64, 0.0f, 0.0f, 0.0f, 10, 2, 10, 0.0f)), List.of(new Piece(1.75f, -4.0f, 2.0f, -0.05235988f, 0.0f, 0.02617994f, false, List.of(new Cube(0, 76, 0.0f, 0.0f, 0.0f, 7, 4, 7, 0.0f)), List.of(new Piece(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f, false, List.of(new Cube(0, 87, 0.0f, 0.0f, 0.0f, 4, 4, 4, 0.0f)), List.of(new Piece(1.75f, -2.0f, 2.0f, -0.20943951023931953f, 0.0f, 0.10471976f, false, List.of(new Cube(0, 95, 0.0f, 0.0f, 0.0f, 1, 2, 1, 0.25f)), List.of()))))))))),
                new Piece(0.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(24, 0, -1.0f, 3.0f, -6.0f, 2, 4, 2, 0.0f)), List.of(new Piece(0.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, 0.0f, 7.0f, -6.75f, 1, 1, 1, -0.25f)), List.of())))));
        witch.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 20, 0.0f, 0.0f, -1.0f, 8, 12, 6, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 38, 0.0f, 0.0f, -1.0f, 8, 18, 6, 0.5f)), List.of())));
        witch.put("ArmLeft", List.of(
                new Piece(0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(44, 22, 0.0f, -2.0f, -2.0f, 4, 8, 4, 0.0f), new Cube(44, 22, 4.0f, 2.0f, -2.0f, 4, 4, 4, 0.0f)), List.of())));
        witch.put("ArmRight", List.of(
                new Piece(0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(44, 22, 0.0f, -2.0f, -2.0f, 4, 8, 4, 0.0f), new Cube(44, 22, -4.0f, 2.0f, -2.0f, 4, 4, 4, 0.0f)), List.of())));
        witch.put("Legs", List.of(
                new Piece(-2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 22, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 22, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Witch", new Mob("textures/entity/witch/witch.png", 64, 128, Family.VILLAGER,
                new float[]{-4.0f, 0.0f, -2.0f}, new float[]{-4.0f, 0.0f, 0.0f},
                new float[]{8.0f, 0.0f, 0.0f}, new float[]{4.0f, -4.0f, 2.0f}, witch));

        // Squid
        Map<String, List<Piece>> squid = new LinkedHashMap<>();
        squid.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -6.0f, -20.0f, -6.0f, 12, 16, 12, 0.0f)), List.of())));
        squid.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -2.0f, -12.0f, -5.0f, 12, 16, 12, 0.0f)), List.of())));
        squid.put("Legs", List.of(
                new Piece(-4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of())));
        MOBS.put("Squid", new Mob("textures/entity/squid/squid.png", 64, 32, Family.SPIDER,
                new float[]{-4.0f, 6.0f, 3.0f}, new float[]{-6.0f, -4.0f, 0.0f},
                new float[]{10.0f, -4.0f, 0.0f}, new float[]{4.0f, -8.0f, 0.0f}, squid));

        // CaveSpider
        Map<String, List<Piece>> cavespider = new LinkedHashMap<>();
        cavespider.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(32, 4, -4.0f, -4.0f, -6.0f, 8, 8, 8, 0.0f)), List.of())));
        cavespider.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 12, -1.0f, 4.0f, 0.0f, 10, 8, 12, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, 1.0f, 5.0f, -6.0f, 6, 6, 6, 0.0f)), List.of())));
        cavespider.put("Legs", List.of(
                new Piece(-4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(-4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of()),
                new Piece(4.0f, 15.0f, -1.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(18, 0, -1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f)), List.of())));
        MOBS.put("CaveSpider", new Mob("textures/entity/spider/cave_spider.png", 64, 32, Family.SPIDER,
                new float[]{-4.0f, 6.0f, 3.0f}, new float[]{-1.0f, 10.0f, -6.0f},
                new float[]{5.0f, 10.0f, -6.0f}, new float[]{4.0f, 8.0f, -7.0f}, cavespider));

        // Sheep
        Map<String, List<Piece>> sheep = new LinkedHashMap<>();
        sheep.put("Head", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -4.0f, -4.0f, 6, 6, 8, 0.0f)), List.of())));
        sheep.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(28, 8, 0.0f, -10.0f, -6.0f, 8, 16, 6, 0.0f)), List.of())));
        sheep.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        sheep.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, 0.0f, -1.0f, 4, 12, 4, 0.0f)), List.of())));
        sheep.put("Legs", List.of(
                new Piece(-3.0f, 10.0f, 3.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(3.0f, 10.0f, 3.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Sheep", new Mob("textures/entity/sheep/sheep.png", 64, 32, Family.QUADRUPED,
                new float[]{-4.0f, 4.0f, 0.0f}, new float[]{-1.0f, 6.0f, -10.0f},
                new float[]{5.0f, 6.0f, -10.0f}, new float[]{4.0f, 0.0f, -14.0f}, sheep));

        // IronGolem
        Map<String, List<Piece>> irongolem = new LinkedHashMap<>();
        irongolem.put("Head", List.of(
                new Piece(0.0f, 0.0f, -2.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -4.0f, -6.0f, -5.5f, 8, 10, 8, 0.0f), new Cube(24, 0, -1.0f, 1.0f, -7.5f, 2, 4, 2, 0.0f)), List.of())));
        irongolem.put("Torso", List.of(
                new Piece(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 40, -5.0f, 4.0f, -6.0f, 18, 12, 11, 0.0f), new Cube(0, 70, -0.5f, 16.0f, -3.0f, 9, 5, 6, 0.5f)), List.of())));
        irongolem.put("ArmLeft", List.of(
                new Piece(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(60, 58, 0.0f, 2.0f, -3.0f, 4, 30, 6, 0.0f)), List.of())));
        irongolem.put("ArmRight", List.of(
                new Piece(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(60, 21, 0.0f, 2.0f, -3.0f, 4, 30, 6, 0.0f)), List.of())));
        irongolem.put("Legs", List.of(
                new Piece(-4.0f, 11.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(37, 0, -3.5f, -3.0f, -3.0f, 6, 16, 5, 0.0f)), List.of()),
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 22, -3.5f, -3.0f, -3.0f, 6, 16, 5, 0.0f)), List.of())));
        MOBS.put("IronGolem", new Mob("textures/entity/iron_golem/iron_golem.png", 128, 128, Family.BIPED,
                new float[]{-4.0f, -4.0f, 0.0f}, new float[]{-9.0f, 0.0f, 0.0f},
                new float[]{13.0f, 0.0f, 0.0f}, new float[]{8.0f, -7.0f, 2.0f}, irongolem));

        // Wolf
        Map<String, List<Piece>> wolf = new LinkedHashMap<>();
        wolf.put("Head", List.of(
                new Piece(-1.0f, 0.0f, -3.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -2.0f, -2.5f, 2.0f, 6, 6, 4, 0.0f), new Cube(16, 14, -2.0f, -4.5f, 4.0f, 2, 2, 1, 0.0f), new Cube(16, 14, 2.0f, -4.5f, 4.0f, 2, 2, 1, 0.0f), new Cube(0, 10, -0.5f, 0.5f, -1.0f, 3, 3, 4, 0.0f)), List.of()),
                new Piece(-1.0f, 0.0f, -3.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 0, -2.0f, -2.5f, 2.0f, 6, 6, 4, 0.0f), new Cube(16, 14, -2.0f, -4.5f, 4.0f, 2, 2, 1, 0.0f), new Cube(16, 14, 2.0f, -4.5f, 4.0f, 2, 2, 1, 0.0f), new Cube(0, 10, -0.5f, 0.5f, -1.0f, 3, 3, 4, 0.0f)), List.of())));
        wolf.put("Torso", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(16, 16, 0.0f, 0.0f, 0.0f, 8, 12, 4, 0.0f)), List.of())));
        wolf.put("ArmLeft", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        wolf.put("ArmRight", List.of(
                new Piece(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(40, 16, 0.0f, 0.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        wolf.put("Legs", List.of(
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, List.of(new Cube(0, 16, -4.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of()),
                new Piece(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, List.of(new Cube(0, 16, 0.0f, -2.0f, -2.0f, 4, 12, 4, 0.0f)), List.of())));
        MOBS.put("Wolf", new Mob("textures/entity/wolf/wolf.png", 64, 32, Family.BIPED,
                new float[]{-4.0f, -2.0f, -2.0f}, new float[]{-4.0f, 0.0f, 2.0f},
                new float[]{8.0f, 0.0f, 2.0f}, new float[]{4.0f, -4.0f, 2.0f}, wolf));

    }

    /** O bicho pelo nome, ou nada. */
    public static Mob of(String mob) {
        return MOBS.get(mob);
    }

    public static Map<String, Mob> all() {
        return java.util.Collections.unmodifiableMap(MOBS);
    }

    public static int size() {
        return MOBS.size();
    }
}
