package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;

import java.util.Random;

/**
 * A esfera do foco Primordial: o {@code EntityPrimalOrb} da 4.2.3.5, descompilado.
 *
 * <p>Sai devagar, meio bloco por tique, quase sem cair, e depois de um segundo passa a vagar ao acaso. Onde
 * bate, explode com força dois. Uma vez em cem, a explosão ainda deixa uma coisa de herança: ou a mácula
 * brota em volta, ou nasce um nó de aura ali.
 */
public class PrimalOrbEntity extends net.minecraft.world.entity.projectile.ThrowableProjectile {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void tick(PrimalOrbEntity orb);

        void burst(PrimalOrbEntity orb);
    }

    public static ClientEffects clientEffects = new ClientEffects() {
        @Override
        public void tick(PrimalOrbEntity orb) {
        }

        @Override
        public void burst(PrimalOrbEntity orb) {
        }
    };

    private int count;
    /** O {@code seeker}: com a melhoria buscadora, persegue a criatura mais perto (menos quem lançou). */
    private boolean seeker;
    /** O {@code oi}: quem lançou, que a buscadora não persegue. */
    private int oi;

    public PrimalOrbEntity(EntityType<? extends PrimalOrbEntity> type, Level level) {
        super(type, level);
    }

    public PrimalOrbEntity(Level level, LivingEntity thrower) {
        this(level, thrower, false);
    }

    public PrimalOrbEntity(Level level, LivingEntity thrower, boolean seeker) {
        super(TCEntities.PRIMAL_ORB, level);
        this.setOwner(thrower);
        this.seeker = seeker;
        this.oi = thrower.getId();
        Throw.once(this, thrower, 0.5f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.001;
    }

    @Override
    public void tick() {
        this.count++;
        if (this.level().isClientSide()) clientEffects.tick(this);
        // depois de um segundo, vaga: um empurrãozinho sorteado por tique
        Random rr = new Random(this.getId() + this.count);
        if (this.tickCount > 20) {
            if (!this.seeker) {
                this.setDeltaMovement(this.getDeltaMovement().add((rr.nextFloat() - rr.nextFloat()) * 0.01f,
                        (rr.nextFloat() - rr.nextFloat()) * 0.01f, (rr.nextFloat() - rr.nextFloat()) * 0.01f));
            } else {
                // a buscadora vai atrás da criatura mais perto a até dezesseis blocos; o original divide pela distância
                // ao quadrado, e aqui também
                double d = Double.MAX_VALUE;
                net.minecraft.world.entity.Entity t = null;
                for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0), e -> true)) {
                    if (e.getId() == this.oi || e.isRemoved()) continue;
                    double dd = this.distanceToSqr(e);
                    if (dd < d) {
                        d = dd;
                        t = e;
                    }
                }
                if (t != null) {
                    double dx = (t.getX() - this.getX()) / d;
                    double dy = (t.getBoundingBox().minY + t.getBbHeight() * 0.9 - this.getY()) / d;
                    double dz = (t.getZ() - this.getZ()) / d;
                    var m = this.getDeltaMovement().add(dx * 0.2, dy * 0.2, dz * 0.2);
                    this.setDeltaMovement(net.minecraft.util.Mth.clamp(m.x, -0.2, 0.2), net.minecraft.util.Mth.clamp(m.y, -0.2, 0.2),
                            net.minecraft.util.Mth.clamp(m.z, -0.2, 0.2));
                }
            }
        }
        super.tick();
        if (this.tickCount > 5000) this.discard();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide()) {
            clientEffects.burst(this);
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        level.explode(this, this.getX(), this.getY(), this.getZ(), 2.0f, Level.ExplosionInteraction.TNT);
        if (this.random.nextInt(100) <= 1) {
            if (this.random.nextBoolean()) this.taintSplosion(level);
            else net.thaumcraft.world.NodeFeature.createRandomNodeAt(level, this.blockPosition(), this.random);
        }
        this.discard();
    }

    /** O {@code taintSplosion}: fibras de mácula brotando no chão em volta. */
    private void taintSplosion(ServerLevel level) {
        int x = (int) this.getX(), z = (int) this.getZ();
        for (int a = 0; a < 10; a++) {
            int xx = x + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0f);
            int zz = z + (int) (this.random.nextFloat() - this.random.nextFloat() * 6.0f);
            if (!this.random.nextBoolean()) continue;
            BlockPos top = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(xx, 0, zz));
            if (!level.getBlockState(top.below()).isAir() && level.getBlockState(top).isAir()) {
                level.setBlock(top, TCBlocks.TAINT_FIBRES.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

}
