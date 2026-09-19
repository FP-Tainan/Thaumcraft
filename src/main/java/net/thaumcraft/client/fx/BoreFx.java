package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * Os efeitos da broca arcana: o {@code FXBeamBore} (o facho de três fitas que sai do centro dela até onde bate), o
 * {@code FXBoreSparkle} (a faísca verde que voa do bloco até o bico) e o {@code boreDigFx} do {@code ClientProxy},
 * que escolhe entre ela e a migalha do bloco ({@link BoreParticle}).
 */
public final class BoreFx {
    private BoreFx() {
    }

    /** O {@code boreDigFx}: uma vez em dez, a faísca; senão, uma migalha do bloco. */
    public static void dig(Level level, BlockPos from, BlockPos to, BlockState state) {
        if (!(level instanceof ClientLevel client)) return;
        RandomSource random = level.getRandom();
        if (random.nextInt(10) == 0) {
            ThaumFx.add(new Spark(from.getX() + random.nextFloat(), from.getY() + random.nextFloat(), from.getZ() + random.nextFloat(),
                    to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5, random));
        } else {
            Minecraft.getInstance().particleEngine.add(new BoreParticle(client, from.getX() + random.nextFloat(),
                    from.getY() + random.nextFloat(), from.getZ() + random.nextFloat(), to.getX() + 0.5, to.getY() + 0.5,
                    to.getZ() + 0.5, state, from));
        }
    }

    /** O {@code beamBore}: renova o facho vivo ou acende um novo. */
    public static Object beam(Level level, double px, double py, double pz, double tx, double ty, double tz, int type, int colour,
                              boolean reverse, float endMod, Object old, int impact) {
        if (old instanceof Beam beam && !beam.dead) {
            beam.update(tx, ty, tz);
            beam.endMod = endMod;
            beam.impact = impact;
            return beam;
        }
        Beam beam = new Beam(px, py, pz, tx, ty, tz, (colour >> 16 & 255) / 255.0f, (colour >> 8 & 255) / 255.0f,
                (colour & 255) / 255.0f, 8, type, reverse);
        beam.endMod = endMod;
        ThaumFx.add(beam);
        return beam;
    }

    /** O {@code FXBeamBore}: o mesmo facho da varinha, parado na broca. */
    static final class Beam implements ThaumFx.Effect {
        private final double x, y, z;
        private final float red, green, blue;
        private final int type;
        private final boolean reverse;
        float endMod = 1.0f;
        int impact;
        private int age;
        private int maxAge;
        private double tX, tY, tZ, ptX, ptY, ptZ;
        private float length, rotYaw, rotPitch;
        private float prevSize;
        boolean dead;

        Beam(double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, int age,
             int type, boolean reverse) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.tX = this.ptX = tx;
            this.tY = this.ptY = ty;
            this.tZ = this.ptZ = tz;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.maxAge = age;
            this.type = type;
            this.reverse = reverse;
            var viewer = Minecraft.getInstance().getCameraEntity();
            if (viewer != null && viewer.distanceToSqr(x, y, z) > 50.0 * 50.0) this.maxAge = 0;
            this.aim();
        }

        void update(double x, double y, double z) {
            this.tX = x;
            this.tY = y;
            this.tZ = z;
            while (this.maxAge - this.age < 4) this.maxAge++;
        }

        private void aim() {
            float xd = (float) (this.x - this.tX), yd = (float) (this.y - this.tY), zd = (float) (this.z - this.tZ);
            this.length = Mth.sqrt(xd * xd + yd * yd + zd * zd);
            double horizontal = Math.sqrt(xd * xd + zd * zd);
            this.rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
            this.rotPitch = (float) (Math.atan2(yd, horizontal) * 180.0 / Math.PI);
        }

        @Override
        public boolean tick() {
            this.ptX = this.tX;
            this.ptY = this.tY;
            this.ptZ = this.tZ;
            this.aim();
            if (this.impact > 0) this.impact--;
            if (this.age++ >= this.maxAge) {
                this.dead = true;
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
            float rot = (float) (minecraft.level.getGameTime() % (360 / rotationSpeed) * rotationSpeed) + rotationSpeed * partial;
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
            var cam = view.camera();
            pose.pushPose();
            pose.translate(this.x - cam.x, this.y - cam.y, this.z - cam.z);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            pose.mulPose(Axis.ZN.rotationDegrees(180.0f + this.rotYaw));
            pose.mulPose(Axis.XP.rotationDegrees(this.rotPitch));
            float x0 = -0.15f * size, x1 = 0.15f * size;
            float x0b = -0.15f * size * this.endMod, x1b = 0.15f * size * this.endMod;
            pose.mulPose(Axis.YP.rotationDegrees(rot));
            float length = this.length * size;
            int colour = Mth.clamp((int) (op * 255.0f), 0, 255) << 24 | (int) (this.red * 255) << 16
                    | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
            for (int t = 0; t < 3; t++) {
                pose.mulPose(Axis.YP.rotationDegrees(60.0f));
                float v0 = -1.0f + var12 + t / 3.0f;
                float v1 = length + v0;
                collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(texture), (matrix, consumer) ->
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
                int glow = 0xA8000000 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
                collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (matrix, consumer) ->
                        Sparkle.billboard(matrix, consumer, view, ix, iy, iz, s, u0, u1, v0, v1, glow));
            }
            this.prevSize = size;
        }
    }

    /** O {@code FXBoreSparkle}: a faísca verde que corre para o bico, piscando em quatro quadros. */
    static final class Spark implements ThaumFx.Effect {
        private double x, y, z, prevX, prevY, prevZ, mx, my, mz;
        private final double targetX, targetY, targetZ;
        private float scale;
        private float red = 0.2f, green, blue = 0.2f;
        private int age;
        private final int maxAge;

        Spark(double x, double y, double z, double tx, double ty, double tz, RandomSource random) {
            this.x = this.prevX = x;
            this.y = this.prevY = y;
            this.z = this.prevZ = z;
            this.targetX = tx;
            this.targetY = ty;
            this.targetZ = tz;
            this.scale = random.nextFloat() * 0.5f + 0.5f;
            double dx = tx - x, dy = ty - y, dz = tz - z;
            int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
            int life = base / 2 + random.nextInt(base);
            this.mx = random.nextGaussian() * 0.01;
            this.my = random.nextGaussian() * 0.01;
            this.mz = random.nextGaussian() * 0.01;
            this.green = 0.6f + random.nextFloat() * 0.3f;
            var viewer = Minecraft.getInstance().getCameraEntity();
            if (viewer != null && viewer.distanceToSqr(x, y, z) > 64.0 * 64.0) life = 0;
            this.maxAge = life;
        }

        /** O {@code setRBGColorF} que a infusão usa para pintar a faísca. */
        Spark colour(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            return this;
        }

        @Override
        public boolean tick() {
            this.prevX = this.x;
            this.prevY = this.y;
            this.prevZ = this.z;
            boolean arrived = Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY)
                    && Mth.floor(this.z) == Mth.floor(this.targetZ);
            if (this.age++ >= this.maxAge || arrived) return false;
            this.x += this.mx;
            this.y += this.my;
            this.z += this.mz;
            this.mx *= 0.985;
            this.my *= 0.985;
            this.mz *= 0.985;
            double dx = this.targetX - this.x, dy = this.targetY - this.y, dz = this.targetZ - this.z;
            double speed = 0.3;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d < 4.0) {
                this.scale *= 0.9f;
                speed = 0.6;
            }
            this.mx = Mth.clamp(this.mx + dx / d * speed, -0.35, 0.35);
            this.my = Mth.clamp(this.my + dy / d * speed, -0.35, 0.35);
            this.mz = Mth.clamp(this.mz + dz / d * speed, -0.35, 0.35);
            return true;
        }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
            float bob = Mth.sin(this.age / 3.0f) * 0.5f + 1.0f;
            int part = this.age % 4;
            float u0 = part / 16.0f, u1 = u0 + 0.0624375f;
            float v0 = 0.25f, v1 = v0 + 0.0624375f;
            float s = 0.1f * this.scale * bob;
            float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
            float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
            float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
            int colour = 0xFF000000 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
            collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES),
                    (m, c) -> Sparkle.billboard(m, c, view, px, py, pz, s, u0, u1, v0, v1, colour));
        }
    }
}
