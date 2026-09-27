package net.thaumcraft.occulta;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A Mandrágora-de-Mina: o {@code EntityMindrake} do Witchery.
 *
 * <p>O bulbo dela, largado no chão, <b>vira bicho</b> ao fim de três segundos — e, se quem o largou foi alguém,
 * o bicho nasce dono dele. É o que o {@code onItemExpireEvent} do original faz.
 *
 * <p>Ela não morde: <b>estoura</b>. Ao chegar em quem persegue, explode e morre — e do chão queimado nasce uma
 * flor. Morta de outro jeito, estoura na mesma, um pouco menos.
 */
public class MinedrakeEntity extends TamableAnimal {
    /** O estouro do golpe e o da morte, que no original são de tamanhos diferentes. */
    public static final float BLAST_ATTACK = 1.5f;
    public static final float BLAST_DEATH = 1.0f;

    public MinedrakeEntity(EntityType<? extends MinedrakeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        // sem dono, ela persegue quem passar; com dono, não
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
                (level, alvo) -> !this.isTame()));
    }

    /** O {@code attackEntityAsMob}: ela estoura, morre, e onde estava nasce uma flor. */
    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level, Entity alvo) {
        level.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_ATTACK, Level.ExplosionInteraction.MOB);
        this.flower(level);
        this.discard();
        return true;
    }

    /** A flor que fica: amarela ou vermelha, se o chão for terra ou grama. */
    public void flower(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.core.BlockPos onde = this.blockPosition();
        BlockState chão = level.getBlockState(onde.below());
        if (!chão.is(Blocks.GRASS_BLOCK) && !chão.is(Blocks.DIRT) && !chão.is(Blocks.COARSE_DIRT)) return;
        level.setBlockAndUpdate(onde, this.random.nextInt(2) == 0
                ? Blocks.POPPY.defaultBlockState() : Blocks.DANDELION.defaultBlockState());
    }

    /** E morta de outro jeito, estoura na mesma. */
    @Override
    public void die(DamageSource fonte) {
        super.die(fonte);
        if (this.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_DEATH, Level.ExplosionInteraction.MOB);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_SCREAM;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** Ela não cria filhote nenhum: nasce de bulbo, e só. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob parceiro) {
        return null;
    }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    /**
     * O bulbo que acabou no chão vira bicho.
     *
     * <p>Se o bulbo foi largado por alguém, ela nasce dona dessa pessoa — é o {@code WITCThrower} do original, que
     * aqui é o dono que o próprio item largado guarda.
     */
    @Nullable
    public static MinedrakeEntity sprout(net.minecraft.server.level.ServerLevel level,
                                         net.minecraft.world.entity.item.ItemEntity bulbo) {
        MinedrakeEntity bicho = OccultaEntities.MINEDRAKE.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bicho == null) return null;
        bicho.snapTo(bulbo.getX(), bulbo.getY(), bulbo.getZ(), level.getRandom().nextFloat() * 360.0f, 0.0f);
        var dono = bulbo.getOwner();
        if (dono instanceof Player quem) bicho.tame(quem);
        level.addFreshEntity(bicho);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, bicho.getX(),
                bicho.getY() + 0.5, bicho.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        return bicho;
    }
}
