package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O orbe do choque de terra, a melhoria do foco de raio: o {@code EntityShockOrb} da 4.2.3.5. Cai de leve e, batendo,
 * dá o dano dele (mágico) a tudo o que enxerga dentro da área, e espalha vinte sorteios de campos de faísca pelo chão
 * em volta, onde se enxerga.
 */
public class ShockOrbEntity extends ThrowableProjectile {
    /** A área: 4, mais 2 por ampliação. */
    public int area = 4;
    /** O dano: 5, mais 1,33 por potência. */
    public int damage = 5;

    /** O clarão do estouro, do lado de quem vê. */
    public static java.util.function.Consumer<ShockOrbEntity> clientBurst = orb -> {
    };

    public ShockOrbEntity(EntityType<? extends ShockOrbEntity> type, Level level) {
        super(type, level);
    }

    public ShockOrbEntity(Level level, LivingEntity thrower) {
        super(TCEntities.SHOCK_ORB, level);
        this.setOwner(thrower);
        Throw.once(this, thrower, 1.5f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 500) this.discard();
    }

    /** O {@code EntityUtils.canEntityBeSeen}: a linha de um ponto a outro não bate em bloco. */
    private boolean sees(Vec3 from, Vec3 to) {
        return this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /** O {@code isAirBlock} do original, que conta o próprio campo de faísca como ar. */
    private static boolean airy(ServerLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) || level.getBlockState(pos).is(TCBlocks.SPARK_FIELD);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            Vec3 here = this.position();
            AABB box = new AABB(here, here).inflate(this.area);
            DamageSource source = this.damageSources().indirectMagic(this, this.getOwner());
            for (Entity e : server.getEntities(this, box)) {
                if (this.sees(here, e.position())) e.hurtServer(server, source, this.damage);
            }
            var random = this.random;
            for (int a = 0; a < 20; a++) {
                int xx = Mth.floor(this.getX()) + random.nextInt(this.area) - random.nextInt(this.area);
                int yy = Mth.floor(this.getY()) + this.area;
                int zz = Mth.floor(this.getZ()) + random.nextInt(this.area) - random.nextInt(this.area);
                while (airy(server, new BlockPos(xx, yy, zz)) && yy > Mth.floor(this.getY()) - this.area) yy--;
                BlockPos floor = new BlockPos(xx, yy, zz);
                if (airy(server, floor.above()) && !airy(server, floor) && !server.getBlockState(floor.above()).is(TCBlocks.SPARK_FIELD)
                        && this.sees(new Vec3(xx + 0.5, yy + 1.5, zz + 0.5), here)) {
                    server.setBlockAndUpdate(floor.above(), TCBlocks.SPARK_FIELD.defaultBlockState());
                }
            }
            server.broadcastEntityEvent(this, (byte) 3);
            server.playSound(null, this.getX(), this.getY(), this.getZ(), TCSounds.SHOCK.value(), SoundSource.NEUTRAL, 1.0f,
                    1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
        }
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }

    /** O {@code attackEntityFrom}: batido, sai na direção em que o atacante olha, com um zap. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableToBase(source)) return false;
        this.markHurt();
        Entity by = source.getEntity();
        if (by == null) return false;
        this.setDeltaMovement(by.getLookAngle().scale(0.9));
        level.playSound(null, this, TCSounds.ZAP.value(), SoundSource.NEUTRAL, 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.1f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("area", this.area);
        output.putInt("damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.area = input.getIntOr("area", 4);
        this.damage = input.getIntOr("damage", 5);
    }
}
