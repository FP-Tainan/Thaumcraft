package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.client.render.AdditiveGlow;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayList;
import java.util.List;

/**
 * A mosquinha: o {@code FXSwarm} da 4.2.3.5. Voa em volta de quem a soltou — vira para ele quando está longe (e para
 * longe dele quando perto ou quando ele se machuca, que é quando ela fica mais vermelha), batendo as asas (quadros 7 a
 * 14 da quarta fileira da {@code particles.png}) — e, morto o dono, cai e some em dois segundos e meio. De vez em quando
 * zumbe, até três ao mesmo tempo.
 */
public final class SwarmFx implements ThaumFx.Effect {
    private static final List<Long> BUZZ = new ArrayList<>();
    private final Entity target;
    private final float r, g, b;
    private final float scale;
    private final float turnSpeed, speed, gravity;
    private final RandomSource random;
    private double x, y, z, prevX, prevY, prevZ, mx, my, mz;
    private float yaw, pitch;
    private int age, deathTimer;
    public boolean alive = true;

    private SwarmFx(Entity target, float speed, float turnSpeed, float gravity) {
        this.random = target.level().getRandom();
        this.target = target;
        this.x = this.prevX = target.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0f;
        this.y = this.prevY = target.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0f;
        this.z = this.prevZ = target.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 2.0f;
        this.r = 0.8f + this.random.nextFloat() * 0.2f;
        this.g = this.random.nextFloat() * 0.4f;
        this.b = 1.0f - this.random.nextFloat() * 0.2f;
        this.scale = this.random.nextFloat() * 0.5f + 1.0f;
        float f3 = 0.2f;
        this.mx = (this.random.nextFloat() - this.random.nextFloat()) * f3;
        this.my = (this.random.nextFloat() - this.random.nextFloat()) * f3;
        this.mz = (this.random.nextFloat() - this.random.nextFloat()) * f3;
        this.speed = speed;
        this.turnSpeed = turnSpeed;
        this.gravity = gravity;
    }

    /** O {@code swarmParticleFX}: uma mosquinha em volta de {@code target}. */
    public static SwarmFx spawn(Entity target, float speed, float turnSpeed, float gravity) {
        SwarmFx fx = new SwarmFx(target, speed, turnSpeed, gravity);
        ThaumFx.add(fx);
        return fx;
    }

    private boolean targetGone() {
        return this.target == null || this.target.isRemoved() || this.target instanceof LivingEntity l && l.deathTime > 0;
    }

    @Override
    public boolean tick() {
        if (!this.alive) return false;
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.age++;
        if (this.targetGone()) {
            this.deathTimer++;
            this.my -= this.gravity / 2.0f;
            if (this.deathTimer > 50) {
                this.alive = false;
                return false;
            }
        } else {
            this.my += this.gravity;
        }
        this.pushOutOfBlocks();
        this.x += this.mx;
        this.y += this.my;
        this.z += this.mz;
        this.mx *= 0.985;
        this.my *= 0.985;
        this.mz *= 0.985;
        if (!this.targetGone()) {
            boolean hurt = this.target instanceof LivingEntity l && l.hurtTime > 0;
            double dx = this.target.getX() - this.x, dz = this.target.getZ() - this.z;
            double dy = (this.target.getBoundingBox().minY + this.target.getBoundingBox().maxY) / 2.0 - this.y;
            double distSq = dx * dx + dy * dy + dz * dz;
            float turn = this.turnSpeed / 2.0f + this.random.nextInt(Math.max(1, (int) (this.turnSpeed / 2.0f)));
            if (distSq > this.target.getBbWidth() && !hurt) this.face(dx, dy, dz, turn, turn);
            else this.face(dx, dy, dz, -turn, -turn);
            double hx = -Mth.sin(this.yaw / 180.0f * (float) Math.PI) * Mth.cos(this.pitch / 180.0f * (float) Math.PI);
            double hz = Mth.cos(this.yaw / 180.0f * (float) Math.PI) * Mth.cos(this.pitch / 180.0f * (float) Math.PI);
            double hy = -Mth.sin(this.pitch / 180.0f * (float) Math.PI);
            this.heading(hx, hy, hz, this.speed, 15.0f);
        }
        this.buzz();
        return true;
    }

    private void face(double dx, double dy, double dz, float maxYaw, float maxPitch) {
        double h = Math.sqrt(dx * dx + dz * dz);
        float wantedYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float wantedPitch = (float) (-(Math.atan2(dy, h) * 180.0 / Math.PI));
        this.pitch = rotate(this.pitch, wantedPitch, maxPitch);
        this.yaw = rotate(this.yaw, wantedYaw, maxYaw);
    }

    private static float rotate(float from, float to, float max) {
        float d = Mth.wrapDegrees(to - from);
        if (d > max) d = max;
        if (d < -max) d = -max;
        return from + d;
    }

    private void heading(double x, double y, double z, float velocity, float spread) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x /= f;
        y /= f;
        z /= f;
        x += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        y += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        z += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        this.mx = x * velocity;
        this.my = y * velocity;
        this.mz = z * velocity;
    }

    /** O {@code pushOutOfBlocks}: dentro de um bloco (que não seja fibra), empurra para o lado livre mais perto. */
    private void pushOutOfBlocks() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        BlockState state = level.getBlockState(pos);
        if (state.is(TCBlocks.TAINT_FIBRES) || state.isAir() || !state.isCollisionShapeFullBlock(level, pos)) return;
        double fx = this.x - pos.getX(), fy = this.y - pos.getY(), fz = this.z - pos.getZ();
        double best = 9999.0;
        int side = -1;
        double[] dist = {fx, 1.0 - fx, fy, 1.0 - fy, fz, 1.0 - fz};
        BlockPos[] next = {pos.west(), pos.east(), pos.below(), pos.above(), pos.north(), pos.south()};
        for (int i = 0; i < 6; i++) {
            if (!level.getBlockState(next[i]).isCollisionShapeFullBlock(level, next[i]) && dist[i] < best) {
                best = dist[i];
                side = i;
            }
        }
        float push = this.random.nextFloat() * 0.05f + 0.025f;
        float jitter = (this.random.nextFloat() - this.random.nextFloat()) * 0.1f;
        switch (side) {
            case 0 -> { this.mx = -push; this.my = this.mz = jitter; }
            case 1 -> { this.mx = push; this.my = this.mz = jitter; }
            case 2 -> { this.my = -push; this.mx = this.mz = jitter; }
            case 3 -> { this.my = push; this.mx = this.mz = jitter; }
            case 4 -> { this.mz = -push; this.my = this.mx = jitter; }
            case 5 -> { this.mz = push; this.my = this.mx = jitter; }
            default -> { }
        }
    }

    private void buzz() {
        long now = System.nanoTime();
        var mc = Minecraft.getInstance();
        if (BUZZ.size() < 3 && this.random.nextInt(50) == 0 && mc.player != null && mc.player.distanceToSqr(this.x, this.y, this.z) < 64.0) {
            mc.level.playLocalSound(this.x, this.y, this.z, TCSounds.FLY.value(), SoundSource.HOSTILE, 0.03f,
                    0.5f + this.random.nextFloat() * 0.4f, false);
            BUZZ.add(now + 1500000L);
        }
        if (BUZZ.size() >= 3 && BUZZ.get(0) < now) BUZZ.remove(0);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float bob = Mth.sin(this.age / 3.0f) * 0.25f + 1.0f;
        int part = 7 + this.age % 8;
        float u0 = part / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = 0.25f, v1 = v0 + 0.0624375f;
        float size = 0.1f * this.scale * bob;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        float trans = (50.0f - this.deathTimer) / 50.0f;
        boolean hurt = this.target instanceof LivingEntity l && l.hurtTime > 0;
        float g = hurt ? this.g / 2.0f : this.g, b = hurt ? this.b / 2.0f : this.b;
        int colour = (int) (Mth.clamp(trans, 0.0f, 1.0f) * 255) << 24 | (int) (this.r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, size, u0, u1, v0, v1, colour));
    }
}
