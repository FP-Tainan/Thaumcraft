package net.thaumcraft.arcana;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCParticles;

/**
 * A <b>Estrela Cadente</b>: a {@code EntityThrownRock} do Ars Magica 2 no feitio de estrela.
 *
 * <p>Ela cai do teto do mundo até o lugar que o feitiço marcou, e ao chegar fere <b>tudo o que estiver a
 * cinco blocos</b> e tiver linha de vista para ela. Não é um projétil que se aponta: é uma coisa que se chama
 * e que depois cai sozinha.
 *
 * <p><b>Ela não tem desenho nenhum, e isso é do original.</b> A mesma entidade, usada pelo Guardião da Terra,
 * é uma pedra de três caixas com a pele dele — mas o desenhista dela começa por perguntar se é uma estrela
 * cadente e, se for, <b>não desenha nada</b>. O que se vê é só o rastro: brasas azuladas ao longo do caminho,
 * uma a cada décimo de bloco que ela desce.
 */
public class ShootingStarEntity extends Projectile {
    /** O quanto ela acelera por batida, e o mais depressa que pode cair. */
    public static final double ACELERA = 0.1;
    public static final double MAIS_DEPRESSA = -2.0;

    /** A que distância do ponto de queda ela fere. */
    public static final double ALCANCE = 5.0;

    /** A cor do rastro: o {@code 0.24F, 0.58F, 0.71F} do original, um azul de madrugada. */
    public static final int COR = 0x3D94B5;

    /** De quanto em quanto se deixa uma brasa pelo caminho. */
    public static final double PASSO = 0.1;

    private float damage = 30.0f;

    public ShootingStarEntity(EntityType<? extends ShootingStarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public ShootingStarEntity(ServerLevel level, LivingEntity quem, float dano) {
        this(ArcanaEntities.SHOOTING_STAR, level);
        this.setOwner(quem);
        this.damage = dano;
    }

    public float damage() {
        return this.damage;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 anda = this.getDeltaMovement();
        double desce = Math.max(MAIS_DEPRESSA, anda.y - ACELERA);
        this.setDeltaMovement(anda.x, desce, anda.z);

        var bateu = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnMoveVector(
                this, e -> e != this.getOwner() && !e.isSpectator());
        this.setPos(this.position().add(this.getDeltaMovement()));

        if (this.level() instanceof ServerLevel level) {
            rastro(level);
            if (bateu.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                this.cai(level, bateu.getLocation());
                return;
            }
            if (this.getY() < level.getMinY() - 16) this.discard();
        }
    }

    /** As brasas que ela vai deixando: uma a cada décimo de bloco do que desceu nesta batida. */
    private void rastro(ServerLevel level) {
        double quanto = Math.abs(this.getDeltaMovement().y);
        var cor = ColorParticleOption.create(TCParticles.SPELL, COR | 0xFF000000);
        for (double i = 0.0; i < quanto; i += PASSO) {
            level.sendParticles(cor,
                    this.getX() + this.getDeltaMovement().x * i,
                    this.getY() + this.getDeltaMovement().y * i,
                    this.getZ() + this.getDeltaMovement().z * i,
                    1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    /** E o que acontece quando ela chega: cinco blocos de dano arcano, a quem a estrela puder ver. */
    private void cai(ServerLevel level, Vec3 onde) {
        var quem = this.getOwner() instanceof LivingEntity vivo ? vivo : null;
        var caixa = new AABB(onde, onde).inflate(ALCANCE);
        for (LivingEntity perto : level.getEntitiesOfClass(LivingEntity.class, caixa)) {
            if (perto == quem) continue;
            // a estrela só fere quem ela pode ver: o original pergunta isso ao sentido de vista dela
            if (!level.clip(new net.minecraft.world.level.ClipContext(this.getEyePosition(),
                    perto.getEyePosition(), net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, this))
                    .getType().equals(net.minecraft.world.phys.HitResult.Type.MISS)) {
                continue;
            }
            perto.hurtServer(level,
                    level.damageSources().indirectMagic(quem == null ? perto : quem, quem),
                    this.damage);
        }

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                onde.x, onde.y, onde.z, 8, 1.5, 1.5, 1.5, 0.0);
        level.playSound(null, net.minecraft.core.BlockPos.containing(onde),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.8f);
        this.discard();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("damage", 30.0f);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distância) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    /** Ela não bate em quem a chamou, e não bate em quem já está morto. */
    @Override
    protected boolean canHitEntity(Entity alvo) {
        return super.canHitEntity(alvo) && alvo != this.getOwner();
    }
}
