package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.thaumcraft.block.entity.InfusionMatrixBlockEntity;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * O {@code FXEssentiaTrail} da 4.2.3.5: a bolinha de essência, na cor do aspecto, que sobe do jarro e vai dando voltas
 * até quem a chamou; e o mapa {@code EssentiaHandler.sourceFX} do cliente, que a cada tique solta uma delas de cada
 * fonte viva (quinze tiques depois do último pacote, encolhendo nos cinco últimos).
 */
public final class EssentiaTrail implements ThaumFx.Effect {
    private record Source(BlockPos start, BlockPos end, int colour) {
    }

    private static final Map<String, Source> SOURCES = new LinkedHashMap<>();
    private static final Map<String, Integer> TICKS = new LinkedHashMap<>();
    private static int tickCount;

    private final FxMotion motion;
    private final double targetX, targetY, targetZ;
    private final int count;
    private final float r, g, b;
    private float scale;
    private double prevX, prevY, prevZ;
    private int age;
    private final int maxAge;
    private final RandomSource random;

    private EssentiaTrail(double x, double y, double z, double tx, double ty, double tz, int count, int colour, float scale) {
        var level = Minecraft.getInstance().level;
        this.random = level.getRandom();
        this.motion = new FxMotion(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.scale = (Mth.sin(count / 2.0f) * 0.1f + 1.0f) * scale;
        this.count = count;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        double dx = tx - x, dy = ty - y, dz = tz - z;
        int base = Math.max(1, (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 30.0f));
        int life = base / 2 + this.random.nextInt(base);
        this.motion.mx = Mth.sin(count / 4.0f) * 0.015f + this.random.nextGaussian() * 0.002f;
        this.motion.my = 0.1f + Mth.sin(count / 3.0f) * 0.01f;
        this.motion.mz = Mth.sin(count / 2.0f) * 0.015f + this.random.nextGaussian() * 0.002f;
        float red = (colour >> 16 & 255) / 255.0f, green = (colour >> 8 & 255) / 255.0f, blue = (colour & 255) / 255.0f;
        this.r = red - red * 0.2f + this.random.nextFloat() * red * 0.2f;
        this.g = green - green * 0.2f + this.random.nextFloat() * green * 0.2f;
        this.b = blue - blue * 0.2f + this.random.nextFloat() * blue * 0.2f;
        var viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer != null && viewer.distanceToSqr(x, y, z) > 64.0 * 64.0) life = 0;
        this.maxAge = life;
    }

    /** O {@code essentiaTrailFx}: do meio do bloco {@code from} ao meio do bloco {@code to}. */
    public static void spawn(int x, int y, int z, int x2, int y2, int z2, int count, int colour, float scale) {
        ThaumFx.add(new EssentiaTrail(x + 0.5, y + 0.5, z + 0.5, x2 + 0.5, y2 + 0.5, z2 + 0.5, count, colour, scale));
    }

    /** O {@code PacketFXEssentiaSource}: renova o fio que já existe, ou abre um novo, por quinze tiques. */
    public static void source(BlockPos pos, BlockPos from, int colour) {
        String key = pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + from.getX() + ":" + from.getY() + ":" + from.getZ() + ":" + colour;
        SOURCES.put(key, new Source(pos, from, colour));
        TICKS.put(key, 15);
    }

    /** O pedaço do {@code clientTick} do {@code ClientTickEventsFML} que solta as bolinhas. */
    public static void clientTick(Minecraft minecraft) {
        tickCount++;
        var level = minecraft.level;
        for (String key : SOURCES.keySet().toArray(new String[0])) {
            int ticks = TICKS.get(key);
            if (ticks <= 0 || level == null) {
                SOURCES.remove(key);
                TICKS.remove(key);
                continue;
            }
            Source fx = SOURCES.get(key);
            // a matriz bebe um bloco abaixo dela
            int mod = level.getBlockEntity(fx.start()) instanceof InfusionMatrixBlockEntity ? -1 : 0;
            if (ticks > 5) {
                spawn(fx.end().getX(), fx.end().getY(), fx.end().getZ(), fx.start().getX(), fx.start().getY() + mod, fx.start().getZ(),
                        tickCount, fx.colour(), 1.0f);
            } else {
                spawn(fx.end().getX(), fx.end().getY(), fx.end().getZ(), fx.start().getX(), fx.start().getY() + mod, fx.start().getZ(),
                        tickCount - (5 - ticks), fx.colour(), ticks * ticks / 25.0f);
            }
            TICKS.put(key, ticks - 1);
        }
    }

    @Override
    public boolean tick() {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;
        this.prevX = this.motion.x;
        this.prevY = this.motion.y;
        this.prevZ = this.motion.z;
        if (this.age++ >= this.maxAge) return false;
        this.motion.my += 0.01 * 0.2f;
        this.motion.pushOutOfBlocks(level, this.random);
        this.motion.move(level);
        this.motion.mx = Mth.clamp((float) (this.motion.mx * 0.985), -0.05f, 0.05f);
        this.motion.my = Mth.clamp((float) (this.motion.my * 0.985), -0.05f, 0.05f);
        this.motion.mz = Mth.clamp((float) (this.motion.mz * 0.985), -0.05f, 0.05f);
        double dx = this.targetX - this.motion.x, dy = this.targetY - this.motion.y, dz = this.targetZ - this.motion.z;
        double d = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
        if (d < 2.0) this.scale *= 0.98f;
        if (this.scale < 0.2f) return false;
        double pull = 0.01 / Math.min(1.0, d);
        this.motion.mx += dx / d * pull;
        this.motion.my += dy / d * pull;
        this.motion.mz += dz / d * pull;
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float s = Mth.sin((this.age - this.count) / 5.0f) * 0.25f + 1.0f;
        float size = 0.1f * this.scale * s;
        float px = (float) (this.prevX + (this.motion.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.motion.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.motion.z - this.prevZ) * partial - view.camera().z);
        int colour = 0x80000000 | (int) (Mth.clamp(this.r, 0.0f, 1.0f) * 255) << 16 | (int) (Mth.clamp(this.g, 0.0f, 1.0f) * 255) << 8
                | (int) (Mth.clamp(this.b, 0.0f, 1.0f) * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, size, 0.5625f, 0.625f, 0.0625f, 0.125f, colour));
    }
}
