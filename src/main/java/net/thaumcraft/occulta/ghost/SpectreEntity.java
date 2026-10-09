package net.thaumcraft.occulta.ghost;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * O <b>Espectro</b>: o {@code EntitySpectre} do Witchery.
 *
 * <p>Um vulto de braços estendidos que persegue e toca. E o toque dele é o que importa: ele não bate um
 * número, ele <b>tira quinze por cento da vida máxima</b> de quem toca — e tira por fora da armadura.
 *
 * <p>Leia isso duas vezes: contra o Espectro, <b>armadura não vale nada e vida a mais é pior</b>. Um
 * jogador com coração reforçado perde mais por toque do que um jogador pelado. Sete toques e qualquer um
 * morre, venha de couro ou de netherita.
 *
 * <p>Ele nasce <b>apagado</b> — quase transparente — e com uma peça de armadura ou outra apanhada do chão
 * do mundo, como um esqueleto. É a única coisa do mod que se veste de restos.
 *
 * @see SummonedUndeadEntity o teto de quinze, que o torna demorado de matar
 */
public class SpectreEntity extends SummonedUndeadEntity {
    /** Quanto da vida do alvo o toque dele leva, e o mínimo que ele leva. */
    public static final float QUANTO = 0.15f;
    public static final float NO_MÍNIMO = 1.0f;

    /**
     * E a <b>armadura</b> que ele soma por cima da que vestir.
     *
     * <p>O original escreve {@code getTotalArmorValue() + 2}, com teto em vinte. Aqui é a base da
     * propriedade de armadura, que é a mesma coisa: o jogo soma a dela à das peças vestidas. O teto de
     * vinte não vem porque nunca chega lá — ele só apanha capacete e peito, e nem sempre.
     */
    public static final int COURAÇA = 2;

    public SpectreEntity(EntityType<? extends SpectreEntity> tipo, Level level) {
        super(tipo, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, COURAÇA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 32.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão,
            net.minecraft.world.entity.EntitySpawnReason razão,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        var saída = super.finalizeSpawn(level, quão, razão, dado);
        this.populateDefaultEquipmentSlots(level.getRandom(), quão);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), quão);
        this.apagado(true);
        return saída;
    }

    /**
     * <b>O toque.</b>
     *
     * <p>Quinze por cento da vida máxima de quem apanha, nunca menos de um — e por fora da armadura.
     *
     * <p><b>Diferença.</b> O original soma ao toque o que a Afiação e a Repulsão da arma dele dariam, e
     * o Fogo também. Mas ele nunca tem arma: as peças que ele apanha ao nascer são só capacete e peito.
     * As três somas são portanto sempre zero, e não estão aqui.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        if (!(noquê instanceof LivingEntity vivo)) {
            return super.doHurtTarget(level, noquê);
        }
        float quanto = Math.max(vivo.getMaxHealth() * QUANTO, NO_MÍNIMO);
        return TouchOfDeath.toca(level, this, vivo, quanto);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OccultaSounds.SPECTRE_SAY.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.SPECTRE_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.SPECTRE_DIE.value();
    }

}
