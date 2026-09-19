package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A névoa escura do guardião: o {@code FXWispEG} da 4.2.3.5. Um fiapo quase preto (a quarta linha da folha de partículas,
 * em mistura comum) que escorre dos pés do guardião, acompanha o passo dele até tocar o chão e some em uns quarenta tiques,
 * mais apagado quanto mais longe de quem vê.
 */
public final class GuardianWisp implements ThaumFx.Effect {
    private final Level level;
    private final Entity target;
    private double x, y, z, prevX, prevY, prevZ;
    private double motionX, motionY, motionZ;
    private final float red, green, blue, scale;
    private final int maxAge;
    private int age;
    private boolean onGround;

    private GuardianWisp(Level level, double x, double y, double z, Entity target) {
        this.level = level;
        this.target = target;
        var random = level.getRandom();
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        this.motionX = random.nextGaussian() * 0.03;
        this.motionY = -0.05;
        this.motionZ = random.nextGaussian() * 0.03;
        this.scale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f * 0.4f;
        int life = (int) (40.0 / (Math.random() * 0.3 + 0.7));
        var camera = Minecraft.getInstance().getCameraEntity();
        if (camera != null && camera.distanceToSqr(x, y, z) > 50 * 50) life = 0;
        this.maxAge = life;
        this.red = random.nextFloat() * 0.05f;
        this.green = random.nextFloat() * 0.05f;
        this.blue = random.nextFloat() * 0.05f;
    }

    public static void spawn(Entity target, double x, double y, double z) {
        ThaumFx.add(new GuardianWisp(target.level(), x, y, z, target));
    }

    private boolean solid(double px, double py, double pz) {
        BlockPos pos = BlockPos.containing(px, py, pz);
        return !this.level.getBlockState(pos).getCollisionShape(this.level, pos).isEmpty();
    }

    @Override
    public boolean tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        if (this.target != null && !this.onGround) {
            this.x += this.target.getDeltaMovement().x;
            this.z += this.target.getDeltaMovement().z;
        }
        if (!this.solid(this.x + this.motionX, this.y, this.z)) this.x += this.motionX;
        else this.motionX = 0.0;
        if (!this.solid(this.x, this.y + this.motionY, this.z)) {
            this.y += this.motionY;
            this.onGround = false;
        } else {
            this.onGround = this.motionY < 0.0;
            this.motionY = 0.0;
        }
        if (!this.solid(this.x, this.y, this.z + this.motionZ)) this.z += this.motionZ;
        else this.motionZ = 0.0;
        this.motionX *= 0.98;
        this.motionY *= 0.98;
        this.motionZ *= 0.98;
        if (this.onGround) {
            this.motionX *= 0.85;
            this.motionZ *= 0.85;
        }
        return this.age++ < this.maxAge;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float agescale = 1.0f - (float) this.age / this.maxAge;
        float d6 = 1024.0f;
        var e = Minecraft.getInstance().getCameraEntity();
        float base = e == null ? 1.0f : (float) (1.0 - Math.min(d6, e.distanceToSqr(this.x, this.y, this.z)) / d6);
        float alpha = 0.2f * agescale * base;
        if (alpha <= 0.0f) return;
        float size = 0.5f * this.scale;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        float u0 = this.age % 13 / 16.0f, u1 = u0 + 0.0624375f;
        int colour = (int) (Mth.clamp(alpha, 0, 1) * 255) << 24 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8
                | (int) (this.blue * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, size, u0, u1, 0.1875f, 0.1875f + 0.0624375f, colour));
    }
}
