package net.thaumcraft.naturalis;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O Revenante Feroz: o {@code EntityZombieExtended} do Magia Naturalis 0.5.0.
 *
 * <p>Um zumbi pequeno e rápido que o Foco do Revenante levanta contra um alvo. Ele é de quem o levantou, arremete
 * em cima do alvo, arrebenta portas e, quando mata um aldeão, metade das vezes o aldeão levanta como outro
 * revenante. Se o alvo morrer, ou passados quinze segundos, ele desmancha.
 */
public class RevenantEntity extends Zombie {
    /** O {@code ticksExisted > 300} do original: quanto ele aguenta de pé. */
    public static final int LIFE = 300;

    private @Nullable UUID owner;

    public RevenantEntity(EntityType<? extends RevenantEntity> type, Level level) {
        super(type, level);
        this.setBaby(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        // o original limpa as vontades do zumbi e põe as dele
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(5, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    public void owner(@Nullable UUID owner) {
        this.owner = owner;
    }

    /** Quem o levantou, se ainda estiver por aqui. */
    public @Nullable LivingEntity ownerEntity() {
        if (this.owner == null || !(this.level() instanceof net.minecraft.server.level.ServerLevel server)) return null;
        return server.getEntity(this.owner) instanceof LivingEntity living ? living : null;
    }

    /** Ele não arrasta o dono junto: quem levantou o revenante não é alvo dele. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !target.getUUID().equals(this.owner) && super.canAttack(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.tickCount > LIFE) {
            this.hurtServer((net.minecraft.server.level.ServerLevel) this.level(),
                    this.damageSources().genericKill(), 10.0f);
        }
    }

    /** O aldeão que ele mata levanta, metade das vezes, como outro revenante. */
    @Override
    public boolean killedEntity(net.minecraft.server.level.ServerLevel level, LivingEntity victim,
                                net.minecraft.world.damagesource.DamageSource source) {
        boolean morreu = super.killedEntity(level, victim, source);
        if (!(victim instanceof Villager) || this.random.nextBoolean()) return morreu;
        RevenantEntity outro = NaturalisEntities.REVENANT.create(level, net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        if (outro == null) return morreu;
        outro.copyPosition(victim);
        outro.owner(this.owner);
        victim.discard();
        level.addFreshEntity(outro);
        level.levelEvent(null, 1026, this.blockPosition(), 0);
        return morreu;
    }

    /** Ele não deixa nada para trás. */
    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean hurtByPlayer) {
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.owner != null) output.store("Owner", UUIDUtil.CODEC, this.owner);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    /** O {@code interact} do original: não se faz nada com ele. */
    @Override
    public net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        return net.minecraft.world.InteractionResult.PASS;
    }

    /** Para o alvo do foco: a criatura que o revenante vai caçar. */
    public void hunt(Mob self, LivingEntity target) {
        this.setTarget(target);
    }
}
