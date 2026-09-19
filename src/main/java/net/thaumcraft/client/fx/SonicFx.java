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
 * O grito do guardião: o {@code FXSonic} e o {@code PacketFXSonic} da 4.2.3.5. O hemisfério ({@code hemis.obj}) esticado
 * num cone à frente da cabeça de quem grita, com a ondulação ({@code ripple1} a {@code 15}) passando, em cinza somando luz,
 * seguindo a cabeça por quinze a vinte e dois tiques.
 */
public final class SonicFx implements ThaumFx.Effect {
    private static final Identifier[] FRAMES = new Identifier[16];

    static {
        for (int i = 1; i < 16; i++) FRAMES[i] = Thaumcraft.id("textures/models/ripple" + i + ".png");
    }

    private final Entity target;
    private final float yaw, pitch;
    private final int maxAge;
    private int age;
    private double x, y, z, px, py, pz;

    private SonicFx(Entity target, int age) {
        this.target = target;
        this.yaw = target.getYHeadRot();
        this.pitch = target.getXRot();
        this.maxAge = age + target.level().getRandom().nextInt(age / 2);
        this.follow();
        this.px = this.x;
        this.py = this.y;
        this.pz = this.z;
    }

    private void follow() {
        this.x = this.target.getX();
        this.y = this.target.getY() + this.target.getEyeHeight();
        this.z = this.target.getZ();
    }

    /** O {@code onMessage} do {@code PacketFXSonic}. */
    public static void spawn(int entityId) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity e = level.getEntity(entityId);
        if (e != null) ThaumFx.add(new SonicFx(e, 15));
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
        float h = this.target.getBbHeight();
        pose.pushPose();
        pose.translate(Mth.lerp(partial, this.px, this.x) - view.camera().x, Mth.lerp(partial, this.py, this.y) - view.camera().y,
                Mth.lerp(partial, this.pz, this.z) - view.camera().z);
        pose.mulPose(Axis.YP.rotationDegrees(-this.yaw));
        pose.mulPose(Axis.XP.rotationDegrees(this.pitch));
        pose.translate(0.0f, 0.0f, 2.0f * h + this.target.getBbWidth() / 2.0f);
        pose.scale(0.25f * h, 0.25f * h, -1.0f * h);
        float[] mesh = ObjModel.part("hemis", null);
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(FRAMES[frame]),
                (m, c) -> ObjMesh.draw(mesh, m, c, 220, OverlayTexture.NO_OVERLAY, 0xFF7F7F7F));
        pose.popPose();
    }
}
