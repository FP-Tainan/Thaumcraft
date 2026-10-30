package net.thaumcraft.occulta.symbol;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Marca Negra</b>: o {@code EntityDarkMark} do Witchery.
 *
 * <p>É o que o <b>Morsmordre</b> deixa no céu. Uma caveira de dois blocos de lado, parada no ar, fumegando,
 * que ri de vez em quando e <b>empurra para longe</b> tudo o que anda — num raio de dez casas, de cima a
 * baixo da coluna inteira.
 *
 * <p>Ela não se mata, não cai, não se empurra e não larga nada. Some sozinha ao fim de <b>cinco minutos</b>,
 * e é a única maneira de a tirar do céu.
 *
 * <p>Ela não serve para nada a não ser ser vista de longe — e é exatamente isso que ela é. Quem a põe não
 * está atacando ninguém: está <b>dizendo onde esteve</b>.
 *
 * <p><b>Mudança declarada:</b> o original faz a conta do empurrão <b>só no lado do cliente</b>, onde ela
 * quase não se vê — o bicho é empurrado na tela de quem olha e volta ao lugar na batida seguinte. Aqui ela
 * é feita no servidor, que é onde ela vale. É o que o código do original diz querer fazer.
 *
 * <p><b>E uma curiosidade do original que fica anotada:</b> ele registra um som próprio dela,
 * {@code mob/darkmark/idle}, e <b>nunca o toca</b> — o que ela ri é a risada do Senhor do Tormento. O som
 * está portado e continua sem dono, como lá.
 */
public class DarkMarkEntity extends Mob {
    /** Quanto ela dura: cinco minutos, e nem uma batida a mais. */
    public static final int DURA = 5 * 60 * 20;

    /** De quantas em quantas batidas ela empurra. */
    public static final int EMPURRA_A_CADA = 4;

    /** E até onde, medido no chão e não no ar. */
    public static final double RAIO = 10.0;

    /** Quantas fumaças ela solta por batida, e o quanto elas se espalham. */
    public static final int FUMAÇAS = 5;
    public static final double ESPALHA = 1.4;

    /** De quantas em quantas batidas ela ri. */
    public static final int RI_A_CADA = 80;

    private long vividas;

    public DarkMarkEntity(EntityType<? extends DarkMarkEntity> tipo, Level level) {
        super(tipo, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0f));
    }

    // ------------------------------------------------------------------ o que ela não faz

    /** <b>Não se mata.</b> */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource de, float quanto) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource de) {
    }

    @Override
    public boolean causeFallDamage(double distância, float quanto, DamageSource de) {
        return false;
    }

    // ------------------------------------------------------------------ e o que ela faz

    @Override
    public int getAmbientSoundInterval() {
        return RI_A_CADA;
    }

    /** A risada é a do Senhor do Tormento, como no original. */
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return net.thaumcraft.occulta.OccultaSounds.TORMENT_LAUGH.value();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource de) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        // ela não cai: a queda é zerada a cada batida, que é o que o original faz
        Vec3 anda = this.getDeltaMovement();
        this.setDeltaMovement(anda.x, 0.0, anda.z);
        if (this.level().isClientSide()) this.fumega();
    }

    /**
     * As <b>cinco fumaças por batida</b> que o original lhe dá, na caixa dela: um bloco e quatro décimos
     * para cada lado e duas casas de altura. É o que a faz parecer que está queimando sem fogo.
     */
    private void fumega() {
        var sorte = this.level().getRandom();
        for (int i = 0; i < FUMAÇAS; i++) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    this.getX() - ESPALHA + sorte.nextDouble() * (2 * ESPALHA),
                    this.getY() + sorte.nextDouble() * 2.0,
                    this.getZ() - ESPALHA + sorte.nextDouble() * (2 * ESPALHA),
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.vividas = Math.max(this.tickCount, ++this.vividas);

        if (this.vividas > DURA) {
            if (!this.level().isClientSide()) this.discard();
            return;
        }

        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.vividas % EMPURRA_A_CADA != 0L) return;

        AABB coluna = new AABB(this.getX() - RAIO, level.getMinY(), this.getZ() - RAIO,
                this.getX() + RAIO, level.getMaxY(), this.getZ() + RAIO);
        for (PathfinderMob bicho : level.getEntitiesOfClass(PathfinderMob.class, coluna)) {
            double dx = bicho.getX() - this.getX();
            double dz = bicho.getZ() - this.getZ();
            if (dx * dx + dz * dz > RAIO * RAIO) continue;
            net.thaumcraft.occulta.rite.Rites.PushCircle.empurra(bicho,
                    this.getX(), bicho.getY(), this.getZ());
        }
    }

    /** Quantas batidas ela já viveu: serve às provas, que não têm cinco minutos para esperar. */
    public long vividas() {
        return this.vividas;
    }

    public void vividas(long quantas) {
        this.vividas = quantas;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong("TicksAlive", this.vividas);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.vividas = input.getLongOr("TicksAlive", 0L);
    }

    /** Põe uma no mundo, onde o feitiço bateu. */
    @Nullable
    public static DarkMarkEntity põe(ServerLevel level, Vec3 onde) {
        DarkMarkEntity marca = net.thaumcraft.occulta.OccultaEntities.DARK_MARK.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (marca == null) return null;
        marca.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);
        level.addFreshEntity(marca);
        return marca;
    }

    /** E ela não some por estar longe de gente. */
    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }
}
