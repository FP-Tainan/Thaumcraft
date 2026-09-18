package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.HashMap;
import java.util.Map;

/**
 * O facho que sai da varinha: o {@code FXBeamWand} da 4.2.3.5, descompilado, com o {@code beamCont} do
 * {@code ClientProxy} que o mantém vivo.
 *
 * <p>São três fitas cruzadas a sessenta graus, girando em torno do próprio eixo, com a textura correndo ao
 * longo delas. O facho nasce fino e engrossa em quatro tiques, e some em quatro depois que a varinha para de
 * pedir. Onde ele bate, pisca uma estrelinha que muda de quadro a cada tique.
 */
public final class WandBeam implements ThaumFx.Effect {
    private static final Map<Integer, WandBeam> BY_PLAYER = new HashMap<>();

    private final Player player;
    private final float red, green, blue;
    private final int type;
    private final boolean reverse;
    private final double offset;
    private float endMod;
    private int impact;
    private int age;
    private int maxAge;
    private double tX, tY, tZ, ptX, ptY, ptZ;
    private float rotYaw, rotPitch, prevYaw, prevPitch;
    private float prevSize;
    private boolean dead;

    /** O {@code beamCont}: renova o facho deste jogador, ou acende um novo. */
    public static void cont(Player player, double tx, double ty, double tz, int type, int colour, boolean reverse,
                            float endMod, int impact) {
        WandBeam beam = BY_PLAYER.get(player.getId());
        if (beam != null && !beam.dead && beam.player == player) {
            beam.update(tx, ty, tz);
            beam.endMod = endMod;
            beam.impact = impact;
            return;
        }
        beam = new WandBeam(player, tx, ty, tz, (colour >> 16 & 255) / 255.0f, (colour >> 8 & 255) / 255.0f,
                (colour & 255) / 255.0f, 8, type, reverse);
        beam.endMod = endMod;
        BY_PLAYER.put(player.getId(), beam);
        ThaumFx.add(beam);
    }

    private WandBeam(Player player, double tx, double ty, double tz, float red, float green, float blue, int age,
                     int type, boolean reverse) {
        this.player = player;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.type = type;
        this.reverse = reverse;
        // o jogador daqui tem a altura no olho; os outros, no meio do corpo, um quarto acima
        this.offset = player == Minecraft.getInstance().player ? player.getEyeHeight()
                : player.getBbHeight() / 2.0f + 0.25;
        this.tX = this.ptX = tx;
        this.tY = this.ptY = ty;
        this.tZ = this.ptZ = tz;
        this.aim();
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        this.maxAge = age;
    }

    private void update(double x, double y, double z) {
        this.tX = x;
        this.tY = y;
        this.tZ = z;
        while (this.maxAge - this.age < 4) this.maxAge++;
    }

    private float length() {
        float xd = (float) (this.player.getX() - this.tX);
        float yd = (float) (this.player.getY() + this.offset - this.tY);
        float zd = (float) (this.player.getZ() - this.tZ);
        return Mth.sqrt(xd * xd + yd * yd + zd * zd);
    }

    private void aim() {
        float xd = (float) (this.player.getX() - this.tX);
        float yd = (float) (this.player.getY() + this.offset - this.tY);
        float zd = (float) (this.player.getZ() - this.tZ);
        double horizontal = Math.sqrt(xd * xd + zd * zd);
        this.rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
        this.rotPitch = (float) (Math.atan2(yd, horizontal) * 180.0 / Math.PI);
    }

    @Override
    public boolean tick() {
        this.ptX = this.tX;
        this.ptY = this.tY;
        this.ptZ = this.tZ;
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        this.aim();
        while (this.rotPitch - this.prevPitch < -180.0f) this.prevPitch -= 360.0f;
        while (this.rotPitch - this.prevPitch >= 180.0f) this.prevPitch += 360.0f;
        while (this.rotYaw - this.prevYaw < -180.0f) this.prevYaw -= 360.0f;
        while (this.rotYaw - this.prevYaw >= 180.0f) this.prevYaw += 360.0f;
        if (this.impact > 0) this.impact--;
        if (this.age++ >= this.maxAge || this.player.isRemoved()) {
            this.dead = true;
            BY_PLAYER.remove(this.player.getId(), this);
            return false;
        }
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        int rotationSpeed = 5;
        float slide = minecraft.player.tickCount;
        float rot = (float) (minecraft.level.getGameTime() % (360 / rotationSpeed) * rotationSpeed)
                + rotationSpeed * partial;
        float size = Math.min(this.age / 4.0f, 1.0f);
        size = this.prevSize + (size - this.prevSize) * partial;
        float op = 0.4f;
        if (this.maxAge - this.age <= 4) op = 0.4f - (4 - (this.maxAge - this.age)) * 0.1f;
        Identifier texture = Thaumcraft.id(switch (this.type) {
            case 1 -> "textures/misc/beam1.png";
            case 2 -> "textures/misc/beam2.png";
            case 3 -> "textures/misc/beam3.png";
            default -> "textures/misc/beam.png";
        });
        float var11 = slide + partial;
        if (this.reverse) var11 *= -1.0f;
        float var12 = -var11 * 0.2f - Mth.floor(-var11 * 0.1f);

        // a mão: um tanto para o lado e à frente do olho, como no original
        double prex = this.player.xo, prey = this.player.yo + this.offset, prez = this.player.zo;
        double px = this.player.getX(), py = this.player.getY() + this.offset, pz = this.player.getZ();
        Vec3 look = this.player.getViewVector(1.0f);
        prex -= Mth.cos(this.player.yRotO / 180.0f * (float) Math.PI) * 0.066f;
        prey -= 0.06;
        prez -= Mth.sin(this.player.yRotO / 180.0f * (float) Math.PI) * 0.04f;
        prex += look.x * 0.3;
        prey += look.y * 0.3;
        prez += look.z * 0.3;
        px -= Mth.cos(this.player.getYRot() / 180.0f * (float) Math.PI) * 0.066f;
        py -= 0.06;
        pz -= Mth.sin(this.player.getYRot() / 180.0f * (float) Math.PI) * 0.04f;
        px += look.x * 0.3;
        py += look.y * 0.3;
        pz += look.z * 0.3;
        Vec3 cam = view.camera();

        pose.pushPose();
        pose.translate(prex + (px - prex) * partial - cam.x, prey + (py - prey) * partial - cam.y,
                prez + (pz - prez) * partial - cam.z);
        float ry = this.prevYaw + (this.rotYaw - this.prevYaw) * partial;
        float rp = this.prevPitch + (this.rotPitch - this.prevPitch) * partial;
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZN.rotationDegrees(180.0f + ry));
        pose.mulPose(Axis.XP.rotationDegrees(rp));
        float x0 = -0.15f * size, x1 = 0.15f * size;
        float x0b = -0.15f * size * this.endMod, x1b = 0.15f * size * this.endMod;
        pose.mulPose(Axis.YP.rotationDegrees(rot));
        float length = this.length() * size;
        int colour = Mth.clamp((int) (op * 255.0f), 0, 255) << 24 | (int) (this.red * 255) << 16
                | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        for (int t = 0; t < 3; t++) {
            pose.mulPose(Axis.YP.rotationDegrees(60.0f));
            float v0 = -1.0f + var12 + t / 3.0f;
            float v1 = length + v0;
            collector.submitCustomGeometry(pose, AdditiveGlow.of(texture), (matrix, consumer) ->
                    LightningBolt.quad(matrix, consumer, colour,
                            x0b, length, 0.0f, 1.0f, v1,
                            x0, 0.0f, 0.0f, 1.0f, v0,
                            x1, 0.0f, 0.0f, 0.0f, v0,
                            x1b, length, 0.0f, 0.0f, v1));
        }
        pose.popPose();

        if (this.impact > 0) {
            int part = this.age % 16;
            float u0 = part / 16.0f, u1 = u0 + 0.0624375f;
            float v0 = 0.3125f, v1 = v0 + 0.0624375f;
            float s = this.endMod / 2.0f / (6 - this.impact);
            float ix = (float) (this.ptX + (this.tX - this.ptX) * partial - cam.x);
            float iy = (float) (this.ptY + (this.tY - this.ptY) * partial - cam.y);
            float iz = (float) (this.ptZ + (this.tZ - this.ptZ) * partial - cam.z);
            int glow = 0xA8000000 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8
                    | (int) (this.blue * 255);
            collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (matrix, consumer) ->
                    Sparkle.billboard(matrix, consumer, view, ix, iy, iz, s, u0, u1, v0, v1, glow));
        }
        this.prevSize = size;
    }
}
