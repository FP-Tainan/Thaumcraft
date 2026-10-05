package net.thaumcraft.occulta.goblin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * O <b>Mog</b>: o {@code EntityGoblinMog} do Witchery, e o <b>arqueiro</b> dos dois deuses goblins.
 *
 * <p>Ele fica <b>longe</b>, atira flechas que machucam muito mais no ar do que no chão, e <b>salta para
 * trás</b> de quem se aproxima. E, perdendo o arco, ele arranja outro: uma vez em cem batidas, do nada,
 * com um estalo de magia.
 *
 * <p>O que ele é, de verdade, é o <b>escudo do Gulg</b>: enquanto os dois estão juntos, nada os fere.
 * Separá-los é a luta, e ele é a metade que não quer ser separada — por isso corre.
 *
 * <p><b>E o arco dele não é seu.</b> O original larga-o e faz sumir em cinco segundos; aqui ele não cai,
 * que é a mesma mesquinhez dita de outro jeito.
 *
 * @see GoblinGodEntity a conta da distância, que é a graça dos dois
 */
public class MogEntity extends GoblinGodEntity implements RangedAttackMob {
    /** A armadura dele, que é menos do que a do Gulg: ele não é o que apanha. */
    public static final double ARMADURA = 5.0;

    /** O murro dele, que é fraco para um chefe — ele não existe para dar murros. */
    public static final int MURRO = 7;
    public static final int MURRO_VARIA = 15;

    /** E o quanto ele levanta quem acerta. */
    public static final double LEVANTA = 0.4;

    /** De quantas em quantas batidas ele arranja um arco novo. */
    public static final int ARRANJA_ARCO = 100;

    /** As batidas entre flechas, e a que distância ele as atira. */
    public static final int ENTRE_FLECHAS = 40;
    public static final int ENTRE_FLECHAS_NO_MÁXIMO = 80;
    public static final float ALCANCE = 30.0f;

    /** A flecha sai mais depressa contra quem está <b>no ar</b>: é a assinatura dele. */
    public static final double NO_CHÃO = 1.5;
    public static final double NO_AR = 2.5;

    public MogEntity(EntityType<? extends MogEntity> tipo, Level level) {
        super(tipo, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return GoblinGodEntity.atributos().add(Attributes.ARMOR, ARMADURA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.0, ENTRE_FLECHAS,
                ENTRE_FLECHAS_NO_MÁXIMO, ALCANCE));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected Class<? extends GoblinGodEntity> oPar() {
        return GulgEntity.class;
    }

    @Override
    protected net.minecraft.world.item.Item aSuaPeça() {
        return net.thaumcraft.occulta.OccultaItems.MOGS_QUIVER;
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão,
            net.minecraft.world.entity.EntitySpawnReason razão,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        /*
         * <b>O arco não cai.</b> No original ele cai e some em cinco segundos, que é o jeito de 2014 de
         * dizer «este arco não é seu»; hoje o tempo de vida de um item largado não se mexe de fora, e a
         * chance de queda zero diz a mesma coisa sem rodeios. Está no PORTE.md.
         */
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        return super.finalizeSpawn(level, quão, razão, dado);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.despertando() > 0) return;

        // e sem arco na mão, uma vez em cem ele arranja outro
        if (this.getMainHandItem().isEmpty() && this.random.nextInt(ARRANJA_ARCO) == 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT,
                            1.0f, 1.0f, 1.0f, 1.0f),
                    this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    /**
     * <b>A flecha dele.</b>
     *
     * <p>Ela sai com uma e meia vezes a velocidade de sempre — e com <b>duas e meia</b> contra quem estiver
     * <b>no ar</b>. É a assinatura do Mog, e é o que torna saltar à frente dele uma má ideia.
     */
    @Override
    public void performRangedAttack(LivingEntity noquê, float força) {
        if (this.getMainHandItem().isEmpty()) return;
        if (!(this.level() instanceof ServerLevel level)) return;

        ItemStack flecha = new ItemStack(Items.ARROW);
        AbstractArrow tiro = net.minecraft.world.entity.projectile.ProjectileUtil.getMobArrow(
                this, flecha, força, this.getMainHandItem());
        double alvoX = noquê.getX() - this.getX();
        double alvoY = noquê.getY(0.3333333333333333) - tiro.getY();
        double alvoZ = noquê.getZ() - this.getZ();
        double chão = Math.sqrt(alvoX * alvoX + alvoZ * alvoZ);
        tiro.shoot(alvoX, alvoY + chão * 0.2, alvoZ, 1.6f,
                14 - level.getDifficulty().getId() * 4);

        double quanto = noquê.isFallFlying() || !noquê.onGround() ? NO_AR : NO_CHÃO;
        tiro.setDeltaMovement(tiro.getDeltaMovement().scale(quanto));
        tiro.setBaseDamage(força * 8.0 + this.random.nextGaussian() * 0.25
                + level.getDifficulty().getId() * 0.11);

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f,
                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        level.addFreshEntity(tiro);
    }

    /** O murro dele levanta quem apanha, mas pouco: não é ele quem dá os murros. */
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity noquê) {
        this.murro = 10;
        level.broadcastEntityEvent(this, (byte) 4);
        boolean pegou = noquê.hurtServer(level, this.damageSources().mobAttack(this),
                MURRO + this.random.nextInt(MURRO_VARIA));
        if (pegou) {
            noquê.setDeltaMovement(noquê.getDeltaMovement().add(0.0, LEVANTA, 0.0));
            noquê.hurtMarked = true;
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return pegou;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual == 4) {
            this.murro = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else {
            super.handleEntityEvent(qual);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OccultaSounds.MOG_IDLE.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos onde,
                                 net.minecraft.world.level.block.state.BlockState feitio) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }
}
