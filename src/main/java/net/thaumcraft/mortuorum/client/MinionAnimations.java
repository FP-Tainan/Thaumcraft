package net.thaumcraft.mortuorum.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Como cada peça se mexe: os {@code setRotationAngles} dos {@code NecroEntity} do Necromancy, um por família.
 *
 * <p>São sete jeitos ao todo — o do bípede e o do quadrúpede da api, e os cinco que alguns bichos escrevem por
 * conta própria. O lacaio usa, em cada lugar do corpo, o jeito do bicho que emprestou aquela peça: a cabeça de
 * galinha mexe como galinha ainda que as pernas sejam de aranha.
 */
public final class MinionAnimations {
    private MinionAnimations() {
    }

    /** O giro em graus que o original faz dividindo por {@code 180/PI}. */
    private static final float RAD = (float) (Math.PI / 180.0);

    public static void apply(MinionModels.Family family, String place, List<ModelPart> parts, MinionRenderState state) {
        float swing = state.walkAnimationPos;
        float amount = state.walkAnimationSpeed;
        float headYaw = state.yRot;
        float headPitch = state.xRot;
        switch (family) {
            case BIPED -> biped(place, parts, swing, amount, headYaw, headPitch);
            case QUADRUPED -> quadruped(place, parts, swing, amount, headPitch);
            case SPIDER -> spider(place, parts, swing, amount);
            case CHICKEN -> chicken(place, parts, swing, amount, headYaw, headPitch);
            case CREEPER -> creeper(place, parts, swing, amount, headYaw, headPitch);
            case VILLAGER -> villager(place, parts, swing, amount, headYaw, headPitch);
            case ENDERMAN -> enderman(place, parts, swing, amount, headYaw, headPitch);
        }
        attack(place, parts, state);
    }

    /** O {@code NecroEntityBiped}. */
    private static void biped(String place, List<ModelPart> parts, float swing, float amount, float yaw, float pitch) {
        switch (place) {
            case "Head" -> {
                for (ModelPart parte : parts) {
                    parte.yRot = yaw * RAD;
                    parte.xRot = pitch * RAD;
                }
            }
            case "ArmLeft" -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662f) * 2.0f * amount * 0.5f;
                parts.get(0).zRot = 0.0f;
            }
            case "ArmRight" -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 2.0f * amount * 0.5f;
                parts.get(0).zRot = 0.0f;
            }
            case "Legs" -> walk(parts, swing, amount, 1.4f);
            default -> {
            }
        }
    }

    /** O {@code NecroEntityQuadruped}: o tronco deitado e as patas trocadas. */
    private static void quadruped(String place, List<ModelPart> parts, float swing, float amount, float pitch) {
        switch (place) {
            case "Head" -> {
                parts.get(0).xRot = pitch * RAD;
                parts.get(0).yRot = pitch * RAD;
            }
            case "Torso" -> parts.get(0).xRot = (float) (Math.PI / 2.0);
            case "ArmLeft" -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 1.4f * amount;
                parts.get(0).zRot = 0.0f;
            }
            case "ArmRight" -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662f) * 1.4f * amount;
                parts.get(0).zRot = 0.0f;
            }
            case "Legs" -> walk(parts, swing, amount, 1.4f);
            default -> {
            }
        }
    }

    /** O {@code NecroEntitySpider} e o {@code NecroEntitySquid}: as oito pernas abertas em leque. */
    private static void spider(String place, List<ModelPart> parts, float swing, float amount) {
        if (!place.equals("Legs") || parts.size() < 8) return;
        float quarto = (float) (Math.PI / 4.0);
        float oitavo = (float) (Math.PI / 8.0);
        float[] z = {-quarto, quarto, -quarto * 0.74f, quarto * 0.74f, -quarto * 0.74f, quarto * 0.74f, -quarto, quarto};
        float[] y = {oitavo * 2.0f, -oitavo * 2.0f, oitavo, -oitavo, -oitavo, oitavo, -oitavo * 2.0f, oitavo * 2.0f};
        float[] balanco = new float[4];
        float[] passo = new float[4];
        float[] fase = {0.0f, (float) Math.PI, (float) (Math.PI / 2.0), (float) (Math.PI * 3.0 / 2.0)};
        for (int i = 0; i < 4; i++) {
            balanco[i] = -(Mth.cos(swing * 0.6662f * 2.0f + fase[i]) * 0.4f) * amount;
            passo[i] = Math.abs(Mth.sin(swing * 0.6662f + fase[i]) * 0.4f) * amount;
        }
        for (int i = 0; i < 8; i++) {
            int par = i / 2;
            float sinal = i % 2 == 0 ? 1.0f : -1.0f;
            ModelPart perna = parts.get(i);
            perna.zRot = z[i] + sinal * passo[par];
            perna.yRot = y[i] + sinal * balanco[par];
        }
    }

    /** O {@code NecroEntityChicken}: três pedaços de cabeça que olham juntos. */
    private static void chicken(String place, List<ModelPart> parts, float swing, float amount, float yaw, float pitch) {
        switch (place) {
            case "Head" -> {
                for (ModelPart parte : parts) {
                    parte.yRot = yaw * RAD;
                    parte.xRot = pitch * RAD;
                }
            }
            case "Torso" -> parts.get(0).xRot = (float) (Math.PI / 2.0);
            case "Legs" -> walk(parts, swing, amount, 1.4f);
            default -> {
            }
        }
    }

    /** O {@code NecroEntityCreeper}: quatro pernas, das quais só as duas primeiras andam. */
    private static void creeper(String place, List<ModelPart> parts, float swing, float amount, float yaw, float pitch) {
        switch (place) {
            case "Head" -> {
                parts.get(0).yRot = yaw * RAD;
                parts.get(0).xRot = pitch * RAD;
            }
            case "Legs" -> walk(parts, swing, amount, 1.4f);
            default -> {
            }
        }
    }

    /** O {@code NecroEntityVillager}: os braços cruzados à frente e o passo miúdo. */
    private static void villager(String place, List<ModelPart> parts, float swing, float amount, float yaw, float pitch) {
        switch (place) {
            case "Head" -> {
                for (ModelPart parte : parts) {
                    parte.yRot = yaw * RAD;
                    parte.xRot = pitch * RAD;
                }
            }
            case "ArmLeft", "ArmRight" -> parts.get(0).xRot = -0.75f;
            case "Legs" -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 1.4f * amount * 0.5f;
                if (parts.size() > 1) parts.get(1).xRot = Mth.cos(swing * 0.6662f) * 1.4f * amount * 0.5f;
            }
            default -> {
            }
        }
    }

    /** O {@code NecroEntityEnderman}: o do bípede, mas com o passo pela metade e preso a 0,4. */
    private static void enderman(String place, List<ModelPart> parts, float swing, float amount, float yaw, float pitch) {
        biped(place, parts, swing, amount, yaw, pitch);
        switch (place) {
            case "Head" -> {
                if (parts.size() > 1) {
                    ModelPart primeiro = parts.get(0);
                    ModelPart segundo = parts.get(1);
                    primeiro.y = 0.0f;
                    primeiro.z = 0.0f;
                    segundo.x = primeiro.x;
                    segundo.y = primeiro.y;
                    segundo.z = primeiro.z;
                    segundo.xRot = primeiro.xRot;
                    segundo.yRot = primeiro.yRot;
                    segundo.zRot = primeiro.zRot;
                }
            }
            case "Legs" -> {
                for (ModelPart parte : parts) parte.xRot = prende(parte.xRot * 0.5f);
            }
            case "ArmLeft", "ArmRight" -> parts.get(0).xRot = prende(parts.get(0).xRot * 0.5f);
            default -> {
            }
        }
    }

    /** O passo de duas pernas, uma à frente da outra. */
    private static void walk(List<ModelPart> parts, float swing, float amount, float forca) {
        for (int i = 0; i < parts.size(); i++) {
            float fase = i % 2 == 0 ? 0.0f : (float) Math.PI;
            parts.get(i).xRot = Mth.cos(swing * 0.6662f + fase) * forca * amount;
            parts.get(i).yRot = 0.0f;
        }
    }

    private static float prende(float valor) {
        return Math.max(-0.4f, Math.min(0.4f, valor));
    }

    /** O golpe: enquanto o relógio de ataque corre, os dois braços sobem. */
    private static void attack(String place, List<ModelPart> parts, MinionRenderState state) {
        if (state.attackTimer <= 0) return;
        if (!place.equals("ArmLeft") && !place.equals("ArmRight")) return;
        float t = state.attackTimer - state.partial;
        float onda = (Math.abs(t % 10.0f - 5.0f) - 2.5f) / 2.5f;
        for (ModelPart parte : parts) parte.xRot = -2.0f + 1.5f * onda;
    }
}
