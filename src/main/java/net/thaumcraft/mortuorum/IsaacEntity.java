package net.thaumcraft.mortuorum;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Isaac: o {@code EntityIsaacBody} e os três que descem dele, no Necromancy.
 *
 * <p>Ele nasce no lugar de um esqueleto, de vez em quando, e não morre de uma vez: o Isaac inteiro chora lágrimas
 * e, quando cai, levanta-se dele o Isaac de sangue, que chora sangue; quando esse cai, ficam de pé a cabeça e o
 * corpo dele, cada um por si. O corpo sem cabeça só bate; a cabeça sem corpo chora e não morre mais — é ela que
 * deixa a Cabeça Decepada.
 */
public class IsaacEntity extends Monster implements RangedAttackMob {
    /** Qual dos quatro é: o do original não é um bicho só, são quatro classes uma dentro da outra. */
    public enum Kind {
        /** O {@code EntityIsaacBody}: o corpo, que só bate. */
        BODY(20.0, 2.0, false),
        /** O {@code EntityIsaacNormal}: o inteiro, que chora lágrimas. */
        NORMAL(20.0, 2.0, true),
        /** O {@code EntityIsaacBlood}: o de sangue, que chora sangue. */
        BLOOD(75.0, 4.0, true),
        /** O {@code EntityIsaacHead}: a cabeça, que chora e não deixa mais nada de si. */
        HEAD(40.0, 2.0, true);

        public final double health;
        public final double damage;
        public final boolean cries;

        Kind(double health, double damage, boolean cries) {
            this.health = health;
            this.damage = damage;
            this.cries = cries;
        }
    }

    /**
     * Qual feitio é cada tipo. Não dá para guardar num campo: o jogo chama o {@code registerGoals} de dentro do
     * construtor de cima, antes de o campo ser posto, e ele ainda estaria vazio quando o bicho escolhe as metas.
     */
    private static final java.util.Map<EntityType<?>, Kind> KINDS = new java.util.IdentityHashMap<>();

    static void bind(EntityType<IsaacEntity> type, Kind kind) {
        KINDS.put(type, kind);
    }

    public IsaacEntity(EntityType<? extends IsaacEntity> type, Level level) {
        super(type, level);
    }

    public Kind kind() {
        return KINDS.getOrDefault(this.getType(), Kind.BODY);
    }

    public static AttributeSupplier.Builder attributes(Kind kind) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, kind.health)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, kind.damage);
    }

    @Override
    protected void registerGoals() {
        if (this.kind().cries) {
            this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0.25, 18, 50.0f));
        }
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new RestrictSunGoal(this));
        this.goalSelector.addGoal(4, new FleeSunGoal(this, 0.25));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.25));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** A lágrima: a do inteiro é de água, a do de sangue e a da cabeça são de sangue. */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel server)) return;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), MortuorumSounds.TEAR, SoundSource.HOSTILE,
                1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        boolean sangue = this.kind() != Kind.NORMAL;
        TearEntity lágrima = new TearEntity(sangue ? MortuorumEntities.TEAR_BLOOD : MortuorumEntities.TEAR,
                server, this);
        lágrima.aimAt(alvo);
        server.addFreshEntity(lágrima);
    }

    /**
     * O {@code onDeath} de cada um: o inteiro deixa o de sangue no lugar dele, o de sangue deixa a cabeça em cima
     * do corpo, e a cabeça não deixa nada — nem morre do jeito que os outros morrem.
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(this.level() instanceof ServerLevel server)) return;
        switch (this.kind()) {
            case NORMAL -> put(server, MortuorumEntities.ISAAC_BLOOD, this.getY());
            case BLOOD -> {
                put(server, MortuorumEntities.ISAAC_HEAD, this.getY() + 1.0);
                put(server, MortuorumEntities.ISAAC_BODY, this.getY());
            }
            default -> {
            }
        }
    }

    private void put(ServerLevel level, EntityType<IsaacEntity> tipo, double y) {
        IsaacEntity novo = tipo.create(level, EntitySpawnReason.TRIGGERED);
        if (novo == null) return;
        novo.snapTo(this.getX(), y, this.getZ(), this.getYRot(), this.getXRot());
        level.addFreshEntity(novo);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.kind().cries ? SoundEvents.GHAST_AMBIENT : super.getAmbientSound();
    }
}
