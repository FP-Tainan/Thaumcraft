package net.thaumcraft.occulta.spirit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O Pesadelo: o {@code EntityNightmare} do Witchery.
 *
 * <p>É o que mora do outro lado quando a noite corre mal. Cem de vida, quatro de dano, e nada o empurra — e ele
 * <b>arromba portas</b>, porque num pesadelo a porta do quarto nunca segura nada.
 *
 * <p>Morto no Mundo dos Espíritos, ele deixa a <b>Fome Melíflua</b>: é a única coisa que se tira de um pesadelo,
 * e é por isso que há quem os procure.
 */
public class NightmareEntity extends Monster {
    /** O que ele deixa, e de quantas em quantas vezes ele deixa dois. */
    public static final int DROP_CHANCE = 10;
    public static final int DROP_FLOOR = 5;

    public NightmareEntity(EntityType<? extends NightmareEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreakDoorGoal(this, dificuldade -> true));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.9, 32.0f));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Ele só existe do outro lado: fora dele, some. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.tickCount % 30 != 1 || SpiritWorld.is(level)) return;
        this.discard();
    }

    /** O {@code dropFewItems}: a Fome Melíflua, e só no Mundo dos Espíritos. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouAlguem) {
        super.dropCustomDeathLoot(level, fonte, matouAlguem);
        if (!SpiritWorld.is(level)) return;
        int quantos = level.getRandom().nextInt(DROP_FLOOR) == 0 ? 2 : 1;
        this.spawnAtLocation(level, new ItemStack(OccultaItems.MELLIFLUOUS_HUNGER, quantos));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }
}
