package net.thaumcraft.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;

/**
 * O Alumentum arremessado: o {@code EntityAlumentum} da 4.2.3.5, descompilado.
 *
 * <p>Um arremesso comum de três quartos de velocidade, que não se vê — o {@code RenderAlumentum} não desenha
 * nada — a não ser pelo rastro de fogos-fátuos negros e faíscas. Onde bate, explode com força 1,66, quebrando
 * blocos se a regra de destruição das criaturas deixar.
 */
public class AlumentumEntity extends net.minecraft.world.entity.projectile.ThrowableProjectile {
    /** O rastro, do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public static java.util.function.Consumer<AlumentumEntity> clientTick = alumentum -> {
    };

    public AlumentumEntity(EntityType<? extends AlumentumEntity> type, Level level) {
        super(type, level);
    }

    public AlumentumEntity(Level level, LivingEntity thrower) {
        super(TCEntities.ALUMENTUM, level);
        this.setOwner(thrower);
        Throw.once(this, thrower, 0.75f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        // o func_70185_h do EntityThrowable
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) clientTick.accept(this);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        level.explode(this, this.getX(), this.getY(), this.getZ(), 1.66f, Level.ExplosionInteraction.MOB);
        this.discard();
    }
}
