package net.thaumcraft.occulta.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A cara do espelho: o {@code EntityMirrorFace} do Witchery.
 *
 * <p>É uma cabeça que aparece no vidro quando alguém pergunta ao espelho quem é a mais bela. Não anda, não cai,
 * não se machuca e não se empurra — olha para quem estiver perto e se some em <b>dez segundos</b>.
 */
public class MirrorFaceEntity extends Mob {
    /** Quanto tempo ela fica: os dez segundos do original. */
    public static final int LIFE = 20 * 10;

    /** E a quanto ela entra no vidro: as quatro décimas do original. */
    public static final double INSIDE = 0.4;

    public MirrorFaceEntity(EntityType<? extends MirrorFaceEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0f, 0.4f));
    }

    /** Põe a cara no vidro daquele espelho: o {@code showMirrorHead} do original. */
    public static @Nullable MirrorFaceEntity show(ServerLevel level, BlockPos espelho, Direction olha) {
        MirrorFaceEntity cara = OccultaEntities.MIRROR_FACE.create(level, EntitySpawnReason.TRIGGERED);
        if (cara == null) return null;
        Direction dentro = olha.getOpposite();
        cara.snapTo(espelho.getX() + 0.5 + dentro.getStepX() * INSIDE, espelho.getY() + 0.1,
                espelho.getZ() + 0.5 + dentro.getStepZ() * INSIDE, olha.toYRot(), 0.0f);
        level.addFreshEntity(cara);
        return cara;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
        if (!this.level().isClientSide() && this.tickCount > LIFE) this.discard();
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel level, DamageSource source) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity outro) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canBeCollidedWith(@Nullable net.minecraft.world.entity.Entity outro) {
        return false;
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 80;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
