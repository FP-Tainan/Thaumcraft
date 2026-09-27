package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * O Ent: o {@code EntityEnt} do Witchery — a árvore que se levanta.
 *
 * <p>Sai de uma tora do ofício quebrada, de vez em quando, e é o que um bosque tem de resposta a quem o corta:
 * duzentos de vida, quatro de dano e nada o empurra.
 *
 * <p>Onde ele pisa, a terra melhora: uma vez a cada quinze segundos, o chão debaixo dele recebe <b>farinha de
 * osso</b>. É o que o original faz com um jogador de mentira, e aqui se faz direto.
 *
 * <p><b>Do original fica de fora</b> o dono que ele pode ter: no Witchery isso vem da poção de escravizar, que é
 * coisa do caldeirão e ainda não chegou.
 */
public class EntEntity extends Monster {
    /** Uma vez em trezentas batidas ele aduba o chão, como no original. */
    public static final int BONEMEAL_CHANCE = 300;

    public EntEntity(EntityType<? extends EntEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9, 32.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** O {@code onLivingUpdate}: de longe em longe, o chão debaixo dele floresce. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.random.nextInt(BONEMEAL_CHANCE) != 0) return;
        feedGround(level, this.blockPosition().below());
    }

    /** A farinha de osso que o chão recebe de graça por o Ent ter passado ali. */
    public static void feedGround(ServerLevel level, BlockPos onde) {
        BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, onde);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    /** Um Ent no lugar de uma tora que alguém quebrou. */
    public static void spawn(ServerLevelAccessor level, BlockPos onde) {
        EntEntity bicho = OccultaEntities.ENT.create(level.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(onde.getX() + 0.5, onde.getY() + 1.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
    }
}
