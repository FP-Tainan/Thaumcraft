package net.thaumcraft.mortuorum;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * O Ursinho Animado: o {@code EntityTeddy} do Necromancy.
 *
 * <p>Ele tem três feitios, e o dono os troca clicando nele: andando, ele segue; de guarda, ele vai atrás do
 * monstro mais perto e o afugenta; sentado, ele fica. Quem o costura ganha um bicho de pelúcia que faz fugir o
 * que dá medo às crianças.
 */
public class TeddyEntity extends TamableAnimal {
    /** Os três feitios do {@code EntityState} do original, na ordem em que o clique os troca. */
    public enum State {
        WALKING, DEFENDING, SITTING
    }

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(TeddyEntity.class, EntityDataSerializers.INT);

    public TeddyEntity(EntityType<? extends TeddyEntity> type, Level level) {
        super(type, level);
        this.setTame(true, false);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 0.3, 8.0f, 5.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(6, new ScareMonstersGoal(this, 10.0f, 7.0f));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, State.WALKING.ordinal());
    }

    public State state() {
        return State.values()[this.entityData.get(STATE) % State.values().length];
    }

    public void setState(State state) {
        this.entityData.set(STATE, state.ordinal());
        this.setOrderedToSit(state != State.WALKING);
    }

    /** O clique do dono roda os três feitios, e o ursinho diz em que ficou. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        this.tame(player);
        State seguinte = switch (this.state()) {
            case WALKING -> State.DEFENDING;
            case DEFENDING -> State.SITTING;
            case SITTING -> State.WALKING;
        };
        this.setState(seguinte);
        player.sendSystemMessage(Component.translatable("message.thaumcraft.teddy." + seguinte.name().toLowerCase(java.util.Locale.ROOT)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** O ursinho não come nada: é de pano. */
    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("state", this.state().ordinal());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setState(State.values()[input.getIntOr("state", 0) % State.values().length]);
    }
}
