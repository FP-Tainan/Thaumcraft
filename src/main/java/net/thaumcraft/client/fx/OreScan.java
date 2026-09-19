package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;
import net.thaumcraft.research.ScanManager;
import org.joml.Quaternionf;

/**
 * O {@code startScan}/{@code showScannedBlocks} do {@code RenderEventHandler}: a picareta elemental, usada num bloco,
 * mostra por cinco segundos, através das paredes e a até oito blocos, os minérios (o brilho do nó, maior quanto mais
 * aspecto o minério tem), a água (azul) e a lava (laranja), sumindo com a distância.
 */
public final class OreScan implements ThaumFx.Effect {
    private static final Identifier NODES = Thaumcraft.id("textures/misc/nodes.png");
    private static final Identifier GLASS = Thaumcraft.id("textures/misc/warded_glass.png");
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> ORES =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));
    private static OreScan current;

    private final int[][][] scanned = new int[17][17][17];
    private final BlockPos centre;
    private final long end;

    private OreScan(Level level, BlockPos centre, long end, int range) {
        this.centre = centre;
        this.end = end;
        for (int xx = -range; xx <= range; xx++) {
            for (int yy = -range; yy <= range; yy++) {
                for (int zz = -range; zz <= range; zz++) {
                    int value = -1;
                    BlockPos at = centre.offset(xx, yy, zz);
                    BlockState state = level.getBlockState(at);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                        if (state.getFluidState().is(FluidTags.LAVA)) {
                            value = -10;
                        } else if (state.getFluidState().is(FluidTags.WATER)) {
                            value = -5;
                        } else if (state.is(ORES)) {
                            value = ScanManager.aspectsOf(state).visSize();
                        }
                    }
                    this.scanned[xx + 8][yy + 8][zz + 8] = value;
                }
            }
        }
    }

    /** Começa (ou recomeça) a varredura em volta do bloco. */
    public static void start(Level level, BlockPos pos) {
        if (current != null) current.dead = true;
        current = new OreScan(level, pos, System.currentTimeMillis() + 5000L, 8);
        ThaumFx.add(current);
    }

    private boolean dead;

    @Override
    public boolean tick() {
        return !this.dead && System.currentTimeMillis() < this.end;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        long time = System.currentTimeMillis();
        long dif = this.end - time;
        if (dif <= 0) return;
        int frame = (int) (time / 50L % 32L);
        for (int xx = -8; xx <= 8; xx++) {
            for (int yy = -8; yy <= 8; yy++) {
                for (int zz = -8; zz <= 8; zz++) {
                    int value = this.scanned[xx + 8][yy + 8][zz + 8];
                    if (value == -1) continue;
                    float alpha = 1.0f;
                    if (dif > 4750L) alpha = 1.0f - (dif - 4750L) / 5.0f;
                    if (dif < 1500L) alpha = dif / 1500.0f;
                    alpha *= 1.0f - (xx * xx + yy * yy + zz * zz) / 64.0f;
                    if (alpha <= 0.0f) continue;
                    BlockPos at = this.centre.offset(xx, yy, zz);
                    if (value == -5) {
                        blockOverlay(pose, collector, view, at, 0x3CD4FC, alpha);
                    } else if (value == -10) {
                        blockOverlay(pose, collector, view, at, 0xFF5A01, alpha);
                    } else if (value >= 0) {
                        float size = value / 7.0f;
                        strip(pose, collector, view, at, 0.2f * size, alpha, frame, 0xAAAA11);
                        strip(pose, collector, view, at, 0.5f * size, alpha, frame, 0xAA1122);
                    }
                }
            }
        }
    }

    /** O {@code renderFacingStrip}: o quadro da vez do brilho do nó, virado para quem olha, no meio do bloco. */
    private static void strip(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, BlockPos at, float scale, float alpha,
                              int frame, int rgb) {
        float u0 = frame / 32.0f, u1 = (frame + 1) / 32.0f, v0 = 0.0f, v1 = 1.0f / 32.0f;
        float px = (float) (at.getX() + 0.5 - view.camera().x);
        float py = (float) (at.getY() + 0.5 - view.camera().y);
        float pz = (float) (at.getZ() + 0.5 - view.camera().z);
        int colour = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24 | rgb;
        collector.submitCustomGeometry(pose, AdditiveGlow.xray(NODES),
                (m, c) -> Sparkle.billboard(m, c, view, px, py, pz, scale, u0, u1, v0, v1, colour));
    }

    /** O {@code drawSpecialBlockoverlay}: o vidro protegido nas seis faces, na cor dada. */
    private static void blockOverlay(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, BlockPos at, int rgb, float alpha) {
        int colour = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24 | rgb;
        for (Direction dir : Direction.values()) {
            pose.pushPose();
            pose.translate(at.getX() + 0.5 - view.camera().x, at.getY() + 0.5 - view.camera().y, at.getZ() + 0.5 - view.camera().z);
            pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -dir.getStepY(), dir.getStepX(), -dir.getStepZ()));
            pose.translate(0.0, 0.0, dir.getStepZ() < 0 ? 0.5 : -0.5);
            collector.submitCustomGeometry(pose, AdditiveGlow.xray(GLASS), (m, c) -> {
                vertex(m, c, -0.5f, 0.5f, 0.0f, 1.0f, colour);
                vertex(m, c, 0.5f, 0.5f, 1.0f, 1.0f, colour);
                vertex(m, c, 0.5f, -0.5f, 1.0f, 0.0f, colour);
                vertex(m, c, -0.5f, -0.5f, 0.0f, 0.0f, colour);
            });
            pose.popPose();
        }
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200).setNormal(m, 0.0f, 0.0f, 1.0f);
    }

    /** Para a prova de tela: quem chama sem cliente não vê nada. */
    public static boolean active() {
        return current != null && !current.dead && System.currentTimeMillis() < current.end && Minecraft.getInstance().level != null;
    }
}
