package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Os efeitos soltos do Thaumcraft — raios, fachos, faíscas —: o {@code ParticleEngine} do original, só com o
 * que os focos usam.
 *
 * <p>O jogo novo tem o sistema de partículas dele, mas as do Thaumcraft não são quadradinhos: o raio é uma
 * árvore de segmentos e o facho é uma fita girando. Então elas vivem aqui, numa lista que anda a cada tique
 * do cliente e se desenha junto com o mundo.
 */
public final class ThaumFx {
    /** Um efeito: anda um tique por vez e se desenha em coordenadas de mundo. */
    public interface Effect {
        /** @return se o efeito continua vivo */
        boolean tick();

        void submit(PoseStack pose, SubmitNodeCollector collector, View view, float partial);
    }

    /**
     * O olho de quem vê, com os números que o {@code ActiveRenderInfo} do 1.7.10 dava às partículas.
     *
     * @param camera onde a câmera está
     */
    public record View(Vec3 camera, float cosYaw, float cosPitch, float sinYaw, float sinSinPitch, float cosSinPitch,
                       double playerX, double playerY, double playerZ) {
        public double distanceTo(double x, double y, double z) {
            double dx = x - this.playerX, dy = y - this.playerY, dz = z - this.playerZ;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private static final List<Effect> EFFECTS = new ArrayList<>();

    private ThaumFx() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.level == null) {
                EFFECTS.clear();
                return;
            }
            if (minecraft.isPaused()) return;
            EFFECTS.removeIf(effect -> !effect.tick());
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(ThaumFx::draw);
    }

    public static void add(Effect effect) {
        EFFECTS.add(effect);
    }

    private static void draw(LevelRenderContext context) {
        if (EFFECTS.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        var cameraState = context.levelState().cameraRenderState;
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        // o ActiveRenderInfo.updateRenderInfo do original, conta por conta
        float yaw = cameraState.yRot, pitch = cameraState.xRot;
        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));
        float sinSinPitch = -sinYaw * (float) Math.sin(Math.toRadians(pitch));
        float cosSinPitch = cosYaw * (float) Math.sin(Math.toRadians(pitch));
        float cosPitch = (float) Math.cos(Math.toRadians(pitch));
        Vec3 eyes = minecraft.player.getEyePosition(partial);
        View view = new View(cameraState.pos, cosYaw, cosPitch, sinYaw, sinSinPitch, cosSinPitch,
                eyes.x, eyes.y, eyes.z);
        PoseStack pose = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        for (Effect effect : List.copyOf(EFFECTS)) {
            effect.submit(pose, collector, view, partial);
        }
    }
}
