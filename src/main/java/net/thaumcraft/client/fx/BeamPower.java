package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.HashMap;
import java.util.Map;

/**
 * O fio de luz entre dois pontos da rede de vis: o {@code FXBeamPower} da 4.2.3.5, com o {@code beamPower} do
 * {@code ClientProxy} que o mantém vivo enquanto o relé pedir.
 *
 * <p>Duas fitas cruzadas com a {@code beam1.png} correndo, somando luz, e uma estrelinha na ponta. Quase invisível
 * a olho nu (um décimo da força); com os óculos da revelação, inteiro. Quando vis passa, pisca na cor do aspecto.
 */
public final class BeamPower implements ThaumFx.Effect {
    private static final Map<BlockPos, BeamPower> BY_RELAY = new HashMap<>();
    private static final Identifier TEXTURE = Thaumcraft.id("textures/misc/beam1.png");

    private final BlockPos key;
    private double x, y, z, tX, tY, tZ, ptX, ptY, ptZ;
    private float red = 0.5f, green = 0.5f, blue = 0.5f;
    private float opacity = 0.3f;
    private float length, rotYaw, rotPitch;
    private int age, maxAge;
    private boolean dead;

    private BeamPower(BlockPos key, Vec3 from, Vec3 to, int age) {
        this.key = key;
        this.x = from.x;
        this.y = from.y;
        this.z = from.z;
        this.tX = this.ptX = to.x;
        this.tY = this.ptY = to.y;
        this.tZ = this.ptZ = to.z;
        this.maxAge = age;
        var viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer != null && viewer.distanceToSqr(from) > 50.0 * 50.0) this.maxAge = 0;
        this.aim();
    }

    /** O {@code beamPower}: renova o fio deste relé, ou acende um novo. */
    public static void cont(BlockPos relay, Vec3 from, Vec3 to, float r, float g, float b, boolean pulse) {
        BeamPower beam = BY_RELAY.get(relay);
        if (beam == null || beam.dead) {
            beam = new BeamPower(relay.immutable(), from, to, 8);
            BY_RELAY.put(beam.key, beam);
            ThaumFx.add(beam);
        } else {
            beam.x = from.x;
            beam.y = from.y;
            beam.z = from.z;
            beam.tX = to.x;
            beam.tY = to.y;
            beam.tZ = to.z;
            while (beam.maxAge - beam.age < 4) beam.maxAge++;
        }
        beam.red = r;
        beam.green = g;
        beam.blue = b;
        if (pulse) beam.opacity = 0.8f;
    }

    private void aim() {
        float xd = (float) (this.x - this.tX), yd = (float) (this.y - this.tY), zd = (float) (this.z - this.tZ);
        this.length = Mth.sqrt(xd * xd + yd * yd + zd * zd);
        double flat = Math.sqrt(xd * xd + zd * zd);
        this.rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
        this.rotPitch = (float) (Math.atan2(yd, flat) * 180.0 / Math.PI);
    }

    @Override
    public boolean tick() {
        this.ptX = this.tX;
        this.ptY = this.tY;
        this.ptZ = this.tZ;
        this.aim();
        if (this.opacity > 0.3f) this.opacity -= 0.025f;
        if (this.opacity < 0.3f) this.opacity = 0.3f;
        if (this.age++ >= this.maxAge) {
            this.dead = true;
            BY_RELAY.remove(this.key, this);
            return false;
        }
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        // no original só o que vai na cabeça (o IRevealer) acende o fio; o taumômetro na mão não
        var head = minecraft.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        boolean revealing = head.is(net.thaumcraft.registry.TCItems.GOGGLES)
                || Boolean.TRUE.equals(head.get(net.thaumcraft.registry.TCComponents.FORTRESS_GOGGLES));
        float slide = minecraft.player.tickCount + partial;
        float var12 = -slide * 0.2f - Mth.floor(-slide * 0.1f);
        float size = 0.7f;
        Vec3 cam = view.camera();
        pose.pushPose();
        pose.translate(this.x - cam.x, this.y - cam.y, this.z - cam.z);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZN.rotationDegrees(180.0f + this.rotYaw));
        pose.mulPose(Axis.XP.rotationDegrees(this.rotPitch));
        float x0 = -0.15f * size, x1 = 0.15f * size;
        float alpha = this.opacity * (revealing ? 1.0f : 0.1f);
        int colour = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24 | (int) (this.red * 255) << 16
                | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        float length = this.length;
        for (int t = 0; t < 2; t++) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            float v0 = -1.0f + var12 + t / 3.0f;
            float v1 = length + v0;
            collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(TEXTURE), (matrix, consumer) ->
                    LightningBolt.quad(matrix, consumer, colour,
                            x0, length, 0.0f, 1.0f, v1,
                            x0, 0.0f, 0.0f, 1.0f, v0,
                            x1, 0.0f, 0.0f, 0.0f, v0,
                            x1, length, 0.0f, 0.0f, v1));
        }
        pose.popPose();

        // a estrelinha na ponta
        int part = this.age % 16;
        float u0 = part / 16.0f, u1 = u0 + 0.0624375f;
        float fv0 = 0.3125f, fv1 = fv0 + 0.0624375f;
        float s = 0.66f * this.opacity;
        float ix = (float) (this.ptX + (this.tX - this.ptX) * partial - cam.x);
        float iy = (float) (this.ptY + (this.tY - this.ptY) * partial - cam.y);
        float iz = (float) (this.ptZ + (this.tZ - this.ptZ) * partial - cam.z);
        float flareAlpha = this.opacity * (revealing ? 1.0f : 0.2f);
        int glow = Mth.clamp((int) (flareAlpha * 255.0f), 0, 255) << 24 | (int) (this.red * 255) << 16
                | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (matrix, consumer) ->
                Sparkle.billboard(matrix, consumer, view, ix, iy, iz, s, u0, u1, fv0, fv1, glow));
    }
}
