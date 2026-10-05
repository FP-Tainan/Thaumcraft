package net.thaumcraft.occulta.goblin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * O <b>Gulg</b>: o {@code EntityGoblinGulg} do Witchery, e o <b>murro</b> dos dois deuses goblins.
 *
 * <p>Ele é o espelho do Mog. Onde o Mog fica longe e atira, o Gulg <b>vem</b>; onde o Mog fica mais duro
 * perto do par, o Gulg <b>bate mais forte</b>:
 *
 * <ul>
 *   <li>a <b>três blocos</b> do Mog, o murro dele vai a <b>seis mais vinte</b> e atira quem apanha um bloco
 *       e meio para o ar;</li>
 *   <li>a seis, dezesseis e um e três décimos; a nove, catorze e um; a dezesseis, doze e sete décimos;</li>
 *   <li>e longe dele, <b>seis mais quatro</b> e meio bloco — um murro de zumbi.</li>
 * </ul>
 *
 * <p>E ele <b>corre atrás do Mog</b>: uma vontade própria o leva de volta para seis blocos dele, de até
 * sessenta e quatro de distância. Quer dizer que separá-los não basta — é preciso <b>mantê-los separados</b>,
 * e o Gulg trabalha o tempo todo contra isso.
 *
 * <p>Ele também não se mexe com empurrões: a resistência dele é <b>um</b>, que é a máxima.
 *
 * @see GoblinGodEntity a conta da distância, que é a graça dos dois
 */
public class GulgEntity extends GoblinGodEntity {
    /** A armadura dele, que é mais do que a do Mog: é ele quem apanha. */
    public static final double ARMADURA = 8.0;

    /** O murro de base, a que se soma o sorteio da força. */
    public static final int MURRO = 6;

    /** A que distância do Mog ele quer ficar, e de quão longe ele volta. */
    public static final float JUNTO = 6.0f;
    public static final float DE_LONGE = 64.0f;

    public GulgEntity(EntityType<? extends GulgEntity> tipo, Level level) {
        super(tipo, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return GoblinGodEntity.atributos()
                .add(Attributes.ARMOR, ARMADURA)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new GulgFollowsMogGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1.0, 48.0f));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected Class<? extends GoblinGodEntity> oPar() {
        return MogEntity.class;
    }

    @Override
    protected net.minecraft.world.item.Item aSuaPeça() {
        return net.thaumcraft.occulta.OccultaItems.GULGS_GURDLE;
    }

    /** Quanto o murro dele sorteia, conforme a distância ao Mog. */
    public static int força(double longe) {
        if (longe <= INVENCÍVEL) return 20;
        if (longe <= UM_QUINTO) return 15;
        if (longe <= METADE) return 10;
        if (longe <= QUATRO_QUINTOS) return 6;
        return 4;
    }

    /** E quanto ele levanta quem apanha, pela mesma conta. */
    public static double levanta(double longe) {
        if (longe <= INVENCÍVEL) return 1.0;
        if (longe <= UM_QUINTO) return 0.8;
        if (longe <= METADE) return 0.5;
        if (longe <= QUATRO_QUINTOS) return 0.2;
        return 0.0;
    }

    /**
     * <b>O murro.</b>
     *
     * <p>Quanto mais perto do Mog, mais forte — e mais alto voa quem apanha. Meio bloco de base, mais o
     * que a distância der.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        this.murro = 10;
        double longe = this.aoPar();
        level.broadcastEntityEvent(this, (byte) 4);

        boolean pegou = noquê.hurtServer(level, this.damageSources().mobAttack(this),
                MURRO + this.random.nextInt(força(longe)));
        if (pegou) {
            noquê.setDeltaMovement(noquê.getDeltaMovement().add(0.0, 0.5 + levanta(longe), 0.0));
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
        return OccultaSounds.GULG_IDLE.value();
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

    /**
     * A vontade que o leva de volta para junto do Mog: o {@code EntityAIMoveTowardsEntityClass} do original.
     *
     * <p>É a peça que faz a luta ser o que é. Sem ela, bastava afastar o Gulg uma vez; com ela, é preciso
     * <b>mantê-lo</b> longe, e ele não ajuda.
     */
    public static class GulgFollowsMogGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final GulgEntity gulg;
        private @org.jetbrains.annotations.Nullable MogEntity mog;

        public GulgFollowsMogGoal(GulgEntity gulg) {
            this.gulg = gulg;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.mog = this.gulg.level().getEntitiesOfClass(MogEntity.class,
                            this.gulg.getBoundingBox().inflate(DE_LONGE))
                    .stream().min(java.util.Comparator.comparingDouble(this.gulg::distanceToSqr))
                    .orElse(null);
            return this.mog != null && this.gulg.distanceToSqr(this.mog) > JUNTO * JUNTO;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mog != null && this.mog.isAlive() && !this.gulg.getNavigation().isDone()
                    && this.gulg.distanceToSqr(this.mog) > JUNTO * JUNTO;
        }

        @Override
        public void start() {
            if (this.mog != null) this.gulg.getNavigation().moveTo(this.mog, 1.0);
        }

        @Override
        public void stop() {
            this.mog = null;
            this.gulg.getNavigation().stop();
        }
    }
}
