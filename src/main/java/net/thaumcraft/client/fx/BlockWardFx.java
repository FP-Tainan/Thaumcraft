package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;
import net.thaumcraft.registry.TCBlocks;
import org.joml.Vector3f;

/**
 * O escudo que acende quando se bate num bloco protegido: o {@code FXBlockWard} da 4.2.3.5, descompilado.
 *
 * <p>Um hexágono de runas ({@code textures/models/hemis0} a {@code 15}, um quadro por fração da vida) colado na
 * face batida, perto de onde o golpe acertou, girado ao acaso, que acende rápido e apaga devagar.
 */
public final class BlockWardFx implements ThaumFx.Effect {
    private static final Identifier[] FRAMES = new Identifier[16];

    static {
        for (int i = 0; i < 16; i++) FRAMES[i] = Thaumcraft.id("textures/models/hemis" + i + ".png");
    }

    private final double x, y, z;
    private final Direction side;
    private final float sx, sy, sz;
    private final float scale;
    private final int rotation;
    private final int maxAge;
    private int age;
    private float alpha;

    private BlockWardFx(RandomSource random, double x, double y, double z, Direction side, float fx, float fy, float fz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.maxAge = 12 + random.nextInt(5);
        this.scale = (float) (1.4 + random.nextGaussian() * 0.3f);
        this.rotation = random.nextInt(360);
        float ax = Mth.clamp(fx - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        float ay = Mth.clamp(fy - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        float az = Mth.clamp(fz - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.sx = side.getStepX() != 0 ? 0.0f : ax;
        this.sy = side.getStepY() != 0 ? 0.0f : ay;
        this.sz = side.getStepZ() != 0 ? 0.0f : az;
    }

    /** O {@code blockWard} do {@code ClientProxy}: o escudo no meio do bloco, com a fração do golpe na face. */
    public static void spawn(BlockHitResult hit) {
        var pos = hit.getBlockPos();
        var at = hit.getLocation();
        RandomSource random = Minecraft.getInstance().level.getRandom();
        ThaumFx.add(new BlockWardFx(random, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, hit.getDirection(),
                (float) (at.x - pos.getX()), (float) (at.y - pos.getY()), (float) (at.z - pos.getZ())));
    }

    /** A cada tique com o botão de bater apertado num bloco protegido, um escudo: é quando o original o chama. */
    public static void clientTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) return;
        if (!minecraft.options.keyAttack.isDown() || minecraft.player.isSpectator()) return;
        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK
                && minecraft.level.getBlockState(block.getBlockPos()).is(TCBlocks.WARDED)) {
            spawn(block);
        }
    }

    @Override
    public boolean tick() {
        float threshold = this.maxAge / 5.0f;
        this.alpha = this.age <= threshold ? this.age / threshold : (float) (this.maxAge - this.age) / this.maxAge;
        return this.age++ < this.maxAge;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float fade = (this.age + partial) / this.maxAge;
        int frame = Math.min(15, (int) (15.0f * fade));
        int colour = (int) (Mth.clamp(this.alpha / 2.0f, 0.0f, 1.0f) * 255) << 24 | 0xFFFFFF;
        pose.pushPose();
        pose.translate(this.x - view.camera().x + this.sx, this.y - view.camera().y + this.sy,
                this.z - view.camera().z + this.sz);
        // o glRotatef(90, offsetY, -offsetX, offsetZ) do original deita o quadro na face
        pose.mulPose(Axis.of(new Vector3f(this.side.getStepY(), -this.side.getStepX(), this.side.getStepZ()))
                .rotationDegrees(90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(this.rotation));
        if (this.side.getStepZ() > 0) {
            pose.translate(0.0, 0.0, 0.505);
            pose.mulPose(Axis.YN.rotationDegrees(180.0f));
        } else {
            pose.translate(0.0, 0.0, -0.505);
        }
        float h = 0.5f * this.scale;
        collector.submitCustomGeometry(pose, AdditiveGlow.of(FRAMES[frame]), (m, c) -> {
            float[][] k = {{-h, h, 0, 1}, {h, h, 1, 1}, {h, -h, 1, 0}, {-h, -h, 0, 0}};
            for (int i = 0; i < 4; i++) vertex(m, c, k[i], colour);
            for (int i = 3; i >= 0; i--) vertex(m, c, k[i], colour);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float[] k, int colour) {
        c.addVertex(m, k[0], k[1], 0.0f).setColor(colour).setUv(k[2], k[3]).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
