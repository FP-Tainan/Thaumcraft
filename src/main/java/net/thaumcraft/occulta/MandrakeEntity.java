package net.thaumcraft.occulta;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A Mandrágora que anda: o {@code EntityMandrake} do Witchery.
 *
 * <p>É a raiz que alguém arrancou fora de hora. Ela sai do chão, grita e corre atrás de quem a arrancou — e o
 * grito dela <b>cega</b> quem apanha o golpe, quinze segundos, a não ser que a pessoa traga <b>abafadores</b> na
 * cabeça.
 *
 * <p>Não dá experiência nenhuma, porque no original também não dá: ela não é caça, é castigo.
 */
public class MandrakeEntity extends Monster {
    /** Quanto tempo a cegueira dura, e com que força: quinze segundos, do segundo grau. */
    public static final int BLIND_TICKS = 300;
    public static final int BLIND_LEVEL = 1;

    public MandrakeEntity(EntityType<? extends MandrakeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    /** Os números do original: só a velocidade é dele; o resto é o que um bicho comum tem. */
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.65)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    /** O {@code attackEntityAsMob}: o golpe dela cega quem não estiver de abafadores. */
    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level, Entity alvo) {
        boolean acertou = super.doHurtTarget(level, alvo);
        if (!(alvo instanceof Player quemLeva)) return acertou;
        if (quemLeva.hasEffect(MobEffects.BLINDNESS) || wearsEarmuffs(quemLeva)) return acertou;
        quemLeva.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS, BLIND_LEVEL));
        return acertou;
    }

    /** Se a pessoa traz os abafadores na cabeça, o grito não a alcança. */
    public static boolean wearsEarmuffs(Player quem) {
        return quem.getItemBySlot(EquipmentSlot.HEAD).is(OccultaItems.EARMUFFS);
    }

    /** Ela grita como um ghast, que é o que o original lhe dá de voz. */
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_SCREAM;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.GHAST_SCREAM;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    /** Ela respira debaixo de água, como no original — é raiz, não gente. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** Uma mandrágora que escapou de quem a arrancou, no lugar dela. */
    public static void spawn(ServerLevelAccessor level, net.minecraft.core.BlockPos onde) {
        MandrakeEntity bicho = OccultaEntities.MANDRAKE.create(level.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(onde.getX() + 0.5, onde.getY() + 0.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
    }
}
