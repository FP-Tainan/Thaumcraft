package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A boia do golem pescador: o {@code EntityGolemBobber} da 4.2.3.5. Voa num arco até a água, boia, e morre se bater em
 * algo sólido, se o golem sumir ou depois de quatro mil tiques. A linha até a mão do golem é desenhada pelo cliente.
 */
public class GolemBobberEntity extends Entity {
    private static final EntityDataAccessor<Integer> FISHER = SynchedEntityData.defineId(GolemBobberEntity.class, EntityDataSerializers.INT);
    private boolean inBlock;
    @Nullable
    private GolemEntity fisher;

    public GolemBobberEntity(EntityType<? extends GolemBobberEntity> type, Level level) {
        super(type, level);
    }

    public GolemBobberEntity(Level level, GolemEntity golem, int x, int y, int z) {
        this(TCEntities.GOLEM_BOBBER, level);
        this.fisher = golem;
        this.entityData.set(FISHER, golem.getId());
        double d1 = x + 0.5 - golem.getX();
        double d3 = y + 1 - golem.getY();
        double d5 = z + 0.5 - golem.getZ();
        double d7 = Math.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
        double d9 = 0.1;
        this.setDeltaMovement(d1 * d9, d3 * d9 + Math.sqrt(d7) * 0.08, d5 * d9);
        this.setPos(golem.getX(), golem.getY(), golem.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FISHER, -1);
    }

    /** O golem que pesca com esta boia (o cliente o acha pelo número). */
    @Nullable
    public GolemEntity fisher() {
        if (this.fisher == null && this.level().getEntity(this.entityData.get(FISHER)) instanceof GolemEntity g) this.fisher = g;
        return this.fisher;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = this.getBoundingBox().getSize() * 4.0 * 64.0;
        return distance < d * d;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            if (this.fisher() == null || !this.fisher.isAlive()) {
                this.discard();
                return;
            }
            if (this.random.nextFloat() < 0.02f) {
                server.sendParticles(ParticleTypes.SPLASH, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat(), this.getZ() + this.random.nextFloat() - this.random.nextFloat(),
                        2 + this.random.nextInt(2), 0.1, 0.0, 0.1, 0.0);
            }
        }
        if (this.tickCount > 4000) {
            this.discard();
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        if (this.inBlock) {
            this.inBlock = false;
            motion = new Vec3(motion.x * (this.random.nextFloat() * 0.2f), motion.y * (this.random.nextFloat() * 0.2f),
                    motion.z * (this.random.nextFloat() * 0.2f));
        }
        Vec3 from = this.position();
        Vec3 to = from.add(motion);
        HitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            this.inBlock = true;
            // bateu em sólido (a água não para o raio): a boia se perde
            this.discard();
            return;
        }
        this.move(MoverType.SELF, motion);
        motion = this.getDeltaMovement();
        double f5 = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.setYRot((float) (Mth.atan2(motion.x, motion.z) * 180.0 / Math.PI));
        this.setXRot((float) (Mth.atan2(motion.y, f5) * 180.0 / Math.PI));
        float f6 = this.onGround() || this.horizontalCollision ? 0.5f : 0.92f;
        int b0 = 5;
        double d10 = 0.0;
        AABB box = this.getBoundingBox();
        for (int j = 0; j < b0; j++) {
            double lo = box.minY + (box.maxY - box.minY) * j / b0 - 0.125 + 0.125;
            double hi = box.minY + (box.maxY - box.minY) * (j + 1) / b0 - 0.125 + 0.125;
            if (this.waterIn(new AABB(box.minX, lo, box.minZ, box.maxX, hi, box.maxZ))) d10 += 1.0 / b0;
        }
        double d2 = d10 * 2.0 - 1.0;
        double my = motion.y + 0.04f * d2;
        if (d10 > 0.0) {
            f6 = (float) (f6 * 0.9);
            my *= 0.8;
        }
        this.setDeltaMovement(motion.x * f6, my * f6, motion.z * f6);
    }

    /** O {@code isMaterialInBB} com água: algum bloco de água dentro da caixa. */
    private boolean waterIn(AABB box) {
        for (BlockPos p : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            if (this.level().getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
