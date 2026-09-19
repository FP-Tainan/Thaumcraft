package net.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O {@code FXBoreParticles} da 4.2.3.5: uma migalha do bloco que sai dele e voa até um ponto — o nó faminto puxando
 * pedaços do que está em volta. Acelera na direção do alvo (mais forte a menos de dois blocos, onde também encolhe) e
 * some ao chegar ou quando a vida acaba.
 */
public class BoreParticle extends TerrainParticle {
    private final double targetX, targetY, targetZ;

    public BoreParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, BlockState state, BlockPos pos) {
        super(level, x, y, z, 0.0, 0.0, 0.0, state, pos);
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.3f + 0.4f);
        double dx = tx - x, dy = ty - y, dz = tz - z;
        int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
        this.lifetime = base / 2 + this.random.nextInt(base);
        this.xd = level.getRandom().nextGaussian() * 0.01;
        this.yd = level.getRandom().nextGaussian() * 0.01;
        this.zd = level.getRandom().nextGaussian() * 0.01;
        this.gravity = 0.2f;
        this.hasPhysics = true;
        var viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer != null && viewer.distanceToSqr(x, y, z) > 64.0 * 64.0) this.lifetime = 0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        boolean arrived = Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY)
                && Mth.floor(this.z) == Mth.floor(this.targetZ);
        if (this.age++ >= this.lifetime || arrived) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;
        double dx = this.targetX - this.x, dy = this.targetY - this.y, dz = this.targetZ - this.z;
        double speed = 0.3;
        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d < 4.0) {
            this.quadSize *= 0.9f;
            speed = 0.6;
        }
        this.xd = Mth.clamp(this.xd + dx / d * speed, -0.35, 0.35);
        this.yd = Mth.clamp(this.yd + dy / d * speed, -0.35, 0.35);
        this.zd = Mth.clamp(this.zd + dz / d * speed, -0.35, 0.35);
    }

    /** O {@code hungryNodeFX}: uma migalha de um bloco em volta, saindo de um canto qualquer dele, rumo ao nó. */
    public static void hungry(ClientLevel level, BlockPos from, BlockState state, BlockPos node) {
        var random = level.getRandom();
        Minecraft.getInstance().particleEngine.add(new BoreParticle(level, from.getX() + random.nextFloat(), from.getY() + random.nextFloat(),
                from.getZ() + random.nextFloat(), node.getX() + 0.5, node.getY() + 0.5, node.getZ() + 0.5, state, from));
    }
}
