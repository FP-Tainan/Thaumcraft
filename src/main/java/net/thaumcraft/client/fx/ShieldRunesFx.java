package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;
import net.thaumcraft.client.render.ObjMesh;
import net.thaumcraft.client.render.ObjModel;

/**
 * O clarão do escudo rúnico: o {@code FXShieldRunes} e o {@code PacketFXShield} da 4.2.3.5.
 *
 * <p>Um hemisfério de runas ({@code hemis.obj}, quadros {@code hemis1} a {@code 15}) em volta de quem apanhou, virado
 * para quem bateu, somando luz e sumindo em oito a doze tiques. Sem atacante, dois hemisférios fecham uma esfera; na
 * queda, um virado para baixo; no bloco que cai, um virado para cima.
 */
public final class ShieldRunesFx implements ThaumFx.Effect {
    private static final Identifier[] FRAMES = new Identifier[16];

    static {
        for (int i = 0; i < 16; i++) FRAMES[i] = Thaumcraft.id("textures/models/hemis" + i + ".png");
    }

    private final Entity target;
    private final float yaw, pitch;
    private final int maxAge;
    private int age;
    private double x, y, z, px, py, pz;

    private ShieldRunesFx(Entity target, int age, float yaw, float pitch) {
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        this.maxAge = age + target.level().getRandom().nextInt(age / 2);
        this.follow();
        this.px = this.x;
        this.py = this.y;
        this.pz = this.z;
    }

    private void follow() {
        this.x = this.target.getX();
        this.y = (this.target.getBoundingBox().minY + this.target.getBoundingBox().maxY) / 2.0;
        this.z = this.target.getZ();
    }

    /** O {@code onMessage} do {@code PacketFXShield}. */
    public static void spawn(int sourceId, int targetId) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity p = level.getEntity(sourceId);
        if (p == null) return;
        if (targetId >= 0) {
            float pitch = 90.0f, yaw = 0.0f;
            Entity t = level.getEntity(targetId);
            if (t != null) {
                double d0 = p.getX() - t.getX();
                double d1 = (p.getBoundingBox().minY + p.getBoundingBox().maxY) / 2.0 - (t.getBoundingBox().minY + t.getBoundingBox().maxY) / 2.0;
                double d2 = p.getZ() - t.getZ();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                yaw = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0f;
                pitch = (float) (-(Math.atan2(d1, d3) * 180.0 / Math.PI));
            }
            ThaumFx.add(new ShieldRunesFx(p, 8, yaw, pitch));
        } else if (targetId == -1) {
            ThaumFx.add(new ShieldRunesFx(p, 8, 0.0f, 90.0f));
            ThaumFx.add(new ShieldRunesFx(p, 8, 0.0f, 270.0f));
        } else if (targetId == -2) {
            ThaumFx.add(new ShieldRunesFx(p, 8, 0.0f, 270.0f));
        } else if (targetId == -3) {
            ThaumFx.add(new ShieldRunesFx(p, 8, 0.0f, 90.0f));
        }
    }

    @Override
    public boolean tick() {
        this.px = this.x;
        this.py = this.y;
        this.pz = this.z;
        if (this.age++ >= this.maxAge) return false;
        this.follow();
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float fade = (this.age + partial) / this.maxAge;
        int frame = Math.min(15, (int) (14.0f * fade) + 1);
        float alpha = Mth.clamp((1.0f - fade) * 3.0f, 0.0f, 1.0f);
        int colour = (int) (alpha * 255.0f) << 24 | 0xFFFFFF;
        float s = 0.4f * this.target.getBbHeight();
        pose.pushPose();
        pose.translate(Mth.lerp(partial, this.px, this.x) - view.camera().x, Mth.lerp(partial, this.py, this.y) - view.camera().y,
                Mth.lerp(partial, this.pz, this.z) - view.camera().z);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - this.yaw));
        pose.mulPose(Axis.XP.rotationDegrees(-this.pitch));
        pose.scale(s, s, s);
        float[] mesh = ObjModel.part("hemis", null);
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(FRAMES[frame]),
                (m, c) -> ObjMesh.draw(mesh, m, c, 220, OverlayTexture.NO_OVERLAY, colour));
        pose.popPose();
    }
}
