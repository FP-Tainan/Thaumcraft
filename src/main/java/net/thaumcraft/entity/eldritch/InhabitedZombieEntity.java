package net.thaumcraft.entity.eldritch;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O zumbi habitado: o {@code EntityInhabitedZombie} da 4.2.3.5. Um zumbi das Terras de Fora com o elmo dos cavaleiros
 * (e às vezes o resto da placa), trinta de vida e cinco de dano, que também caça cultistas. Não deixa nada: ao morrer,
 * estoura e de dentro sai o caranguejo eldritch que o habitava, de elmo. Só nasce se não houver outro por perto.
 */
public class InhabitedZombieEntity extends Zombie {
    public InhabitedZombieEntity(EntityType<? extends InhabitedZombieEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        float diff = level.getDifficulty() == Difficulty.HARD ? 0.9f : 0.6f;
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_PLATE_HELMET));
        if (this.random.nextFloat() <= diff) this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CULTIST_PLATE_CHESTPLATE));
        if (this.random.nextFloat() <= diff) this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CULTIST_PLATE_LEGGINGS));
        return data;
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    /** Não converte aldeão ({@code onKillEntity} vazio). */
    @Override
    public boolean killedEntity(ServerLevel level, net.minecraft.world.entity.LivingEntity victim, DamageSource source) {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
    }

    /** O {@code onDeathUpdate}: sem a queda de morte, estoura e solta o caranguejo de elmo. */
    @Override
    protected void tickDeath() {
        if (this.level() instanceof ServerLevel server) {
            EldritchCrabEntity crab = TCEntities.ELDRITCH_CRAB.create(server, EntitySpawnReason.MOB_SUMMONED);
            if (crab != null) {
                crab.snapTo(this.getX(), this.getY() + this.getEyeHeight(), this.getZ(), this.getYRot(), this.getXRot());
                crab.setHelm(true);
                server.addFreshEntity(crab);
            }
            if (this.shouldDropExperience() && server.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS)
                    && this.getLastHurtByPlayer() != null) {
                ExperienceOrb.award(server, this.position(), this.getBaseExperienceReward(server));
            }
            for (int i = 0; i < 20; i++) {
                server.sendParticles(ParticleTypes.POOF, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                        this.getY() + this.random.nextFloat() * this.getBbHeight(),
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(), 1,
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, 0.0);
            }
        }
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CRAB_TALK.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOSTILE_HURT;
    }

    /** O {@code getCanSpawnHere}: não nasce com outro zumbi habitado a até trinta e dois blocos. */
    public static boolean canSpawn(EntityType<InhabitedZombieEntity> type, LevelAccessor level, EntitySpawnReason reason,
                                   net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!level.getEntitiesOfClass(InhabitedZombieEntity.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()) return false;
        return net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, (ServerLevelAccessor) level, reason, pos, random);
    }
}
