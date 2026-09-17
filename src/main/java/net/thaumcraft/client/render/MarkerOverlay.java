package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * As marcas do sino: a runa que fica pairando sobre o baú que um golem tem por casa.
 *
 * <p>No original, o sino carimba marcas coloridas nas faces dos baús e elas ficam à vista de quem está
 * com o sino na mão — é assim que se enxerga, de relance, o que cada golem foi mandado fazer. Sem isso o
 * sino é um clique no escuro: marca-se um baú e não há sinal nenhum de que a marca pegou.
 *
 * <p>Aqui a marca aparece enquanto se estiver com o sino na mão, em dois lugares: no baú que o sino está
 * guardando neste instante, e na casa de cada golem por perto. A runa é a mesma textura do mod
 * ({@code misc/mark.png}), e gira devagar sobre o bloco.
 */
public final class MarkerOverlay {
    private static final Identifier MARK = Thaumcraft.id("textures/misc/mark.png");
    /** Até onde se enxergam as casas dos golens em volta. */
    private static final double RANGE = 32.0;
    /** O lado da runa. */
    private static final float SIZE = 1.0f;
    /** A cor da marca do sino na mão, e a das casas já entregues a um golem. */
    private static final int HELD_COLOUR = 0xFFFFE07A;
    private static final int BOUND_COLOUR = 0xFF7AC8FF;

    private MarkerOverlay() {
    }

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(MarkerOverlay::draw);
    }

    private static void draw(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        // as marcas só aparecem para quem está com o sino na mão, como no original
        ItemStack held = player.getMainHandItem();
        if (!held.is(TCItems.GOLEM_BELL)) held = player.getOffhandItem();
        if (!held.is(TCItems.GOLEM_BELL)) return;

        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack pose = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        float spin = (minecraft.level.getGameTime() % 360) + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        // a casa que o sino está guardando neste instante
        Set<BlockPos> drawn = new LinkedHashSet<>();
        BlockPos marked = held.get(TCComponents.GOLEM_HOME);
        if (marked != null) {
            mark(pose, collector, camera, marked, HELD_COLOUR, spin);
            drawn.add(marked);
        }

        // e a casa de cada golem por perto
        AABB around = player.getBoundingBox().inflate(RANGE);
        for (GolemEntity golem : minecraft.level.getEntitiesOfClass(GolemEntity.class, around)) {
            BlockPos home = golem.home();
            if (home == null || !drawn.add(home)) continue;
            mark(pose, collector, camera, home, BOUND_COLOUR, spin);
        }
    }

    /** Uma runa pairando sobre o bloco, deitada, girando devagar. */
    private static void mark(PoseStack pose, SubmitNodeCollector collector, Vec3 camera,
                             BlockPos at, int colour, float spin) {
        pose.pushPose();
        pose.translate(at.getX() + 0.5 - camera.x, at.getY() + 1.08 - camera.y, at.getZ() + 0.5 - camera.z);
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(spin * 1.5f));

        float half = SIZE / 2.0f;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(MARK), (matrix, consumer) -> {
            consumer.addVertex(matrix, -half, -half, 0.0f).setColor(colour).setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, -half, half, 0.0f).setColor(colour).setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, half, 0.0f).setColor(colour).setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, -half, 0.0f).setColor(colour).setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
        });
        pose.popPose();
    }
}
