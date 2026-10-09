package net.thaumcraft.occulta.ghost;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * A <b>Banshee</b>: o {@code EntityBanshee} do Witchery.
 *
 * <p>Ela <b>não bate em ninguém</b> — o dano de ataque dela é <b>zero</b>. O que ela faz é <b>gritar</b>:
 * de cinco em cinco segundos, tudo o que estiver vivo a <b>seis blocos</b> perde <b>um décimo da vida
 * máxima</b>, por fora da armadura. E enquanto houver alguém no alcance ela continua gritando, uma vez por
 * segundo, até não haver.
 *
 * <p>É o bicho mais desagradável do mod e o mais honesto: fugir resolve, lutar não. Correr seis blocos é
 * tudo o que é preciso — e, com a vida a cair um décimo por segundo, é tudo o que dá tempo de fazer.
 *
 * <h2>Os abafadores</h2>
 *
 * <p>Há uma saída, e é a melhor piada do mod: <b>quem traz abafadores não ouve</b>. O grito que fura
 * armadura de netherita não fura duas almofadas de couro e lã nas orelhas.
 *
 * @see SummonedUndeadEntity o teto de quinze, que a torna demorada de matar
 */
public class BansheeEntity extends SummonedUndeadEntity {
    /** A que distância o grito dela pega. */
    public static final double ALCANCE = 6.0;

    /** Quanto da vida ele leva, e o mínimo. */
    public static final float QUANTO = 0.1f;
    public static final float NO_MÍNIMO = 1.0f;

    /** De quantas em quantas batidas ela procura gente, e de quantas em quantas ela grita gritando. */
    public static final int PROCURA = 100;
    public static final int GRITANDO = 20;

    /** E de quantas em quantas ela faz o barulho. */
    public static final int O_BARULHO = 60;

    public BansheeEntity(EntityType<? extends BansheeEntity> tipo, Level level) {
        super(tipo, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.3, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isRemoved()) return;

        boolean começou = false;
        if (this.tickCount % PROCURA == 0 || (this.gritando() && this.tickCount % GRITANDO == 0)) {
            começou = this.grita(level);
        }
        if ((começou || this.tickCount % O_BARULHO == 0) && this.gritando()) {
            this.playSound(OccultaSounds.BANSHEE_SCREAM.value(), 1.0f,
                    this.random.nextFloat() * 0.3f + 0.7f);
        }
    }

    /**
     * <b>O grito.</b>
     *
     * <p>Pega em tudo o que for gente, e no que ela estiver perseguindo. Não havendo ninguém, ela se cala.
     *
     * @return se ela <b>começou</b> a gritar agora
     */
    public boolean grita(ServerLevel level) {
        AABB roda = this.getBoundingBox().inflate(ALCANCE);
        boolean achou = false;
        boolean começou = false;

        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (quem == this) continue;
            if (this.distanceToSqr(quem) > ALCANCE * ALCANCE) continue;
            if (!(quem instanceof Player) && quem != this.getTarget()) continue;

            achou = true;
            if (!this.gritando()) {
                this.gritando(true);
                começou = true;
            }
            if (surdo(quem)) continue;
            TouchOfDeath.toca(level, this, quem, Math.max(quem.getMaxHealth() * QUANTO, NO_MÍNIMO));
        }

        if (!achou && this.gritando()) this.gritando(false);
        return começou;
    }

    /** Se aquele traz <b>abafadores</b> nas orelhas, e por isso não ouve. */
    public static boolean surdo(LivingEntity quem) {
        return quem.getItemBySlot(EquipmentSlot.HEAD).is(OccultaItems.EARMUFFS);
    }

    /** Ela não bate em ninguém: o dano de ataque dela é zero, e o toque dela é o grito. */
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity noquê) {
        return false;
    }

    @Override
    protected @org.jetbrains.annotations.Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.SPECTRE_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.SPECTRE_HIT.value();
    }
}
