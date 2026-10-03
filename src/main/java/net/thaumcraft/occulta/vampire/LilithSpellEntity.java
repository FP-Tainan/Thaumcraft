package net.thaumcraft.occulta.vampire;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * <b>O feitiço que Lilith atira</b>: o {@code EntitySpellEffect} do Witchery, com os cinco símbolos que ela
 * sabe.
 *
 * <p>Os cinco vêm do sistema de <b>símbolos</b> do mod — o das infusões, que este porte ainda não tem. Eles
 * estão aqui porque <b>ela os atira</b>, e sem eles o combate com ela seria só bolas de fogo. Quando o
 * sistema vier, é daqui que eles saem.
 *
 * <p>Ela os sorteia com peso, e os pesos contam a história: <b>Flipendo</b> e <b>Attraho</b> cinco vezes mais
 * prováveis do que os outros três. Ela passa o combate a <b>empurrar e puxar</b> — a atirar o jogador para
 * longe e a trazê-lo de volta —, e só de vez em quando cega, prende ou queima.
 *
 * <p>E o <b>Ignianima</b> é o mais bonito dos cinco: ele dói mais <b>quanto mais ferida ela estiver</b>. Uma
 * Lilith inteira queima por dois; uma Lilith quase vencida queima por nove. O combate fica <b>pior</b> à
 * medida que se ganha, e é de propósito.
 */
public class LilithSpellEntity extends Projectile {
    /** Os cinco símbolos, com o peso de cada um. */
    public enum Feitiço {
        /** <b>Ignianima</b>: queima, e queima mais quanto mais ferida estiver quem o atirou. */
        IGNIANIMA(1, 1.5),
        /** <b>Flipendo</b>: empurra, no rumo em que o feitiço ia. */
        FLIPENDO(5, 0.0),
        /** <b>Impedimenta</b>: prende, com Lentidão II por trinta segundos. */
        IMPEDIMENTA(1, 0.0),
        /** <b>Confundus</b>: cega, por trinta segundos. */
        CONFUNDUS(1, 0.0),
        /** E <b>Attraho</b>: puxa tudo o que estiver a dois blocos para quem o atirou. */
        ATTRAHO(5, 2.0);

        public final int peso;
        public final double raio;

        Feitiço(int peso, double raio) {
            this.peso = peso;
            this.raio = raio;
        }

        /** Um dos cinco, pelo peso de cada um. */
        public static Feitiço sorteia(net.minecraft.util.RandomSource sorte) {
            int todos = 0;
            for (Feitiço qual : values()) todos += qual.peso;
            int tirou = sorte.nextInt(todos);
            for (Feitiço qual : values()) {
                tirou -= qual.peso;
                if (tirou < 0) return qual;
            }
            return FLIPENDO;
        }
    }

    /** Quanto tempo o Impedimenta e o Confundus duram. */
    public static final int PRENDE = 600;

    /** O empurrão do Flipendo, e o que ele soma a quem já está lento. */
    public static final double EMPURRA = 2.0;
    public static final double EMPURRA_MAIS = 0.5;
    public static final double EMPURRA_PARA_CIMA = 0.3;

    /** E o puxão do Attraho. */
    public static final double PUXA = 0.04;
    public static final double PUXA_PARA_CIMA = 0.1;

    /** Quanto tempo ele voa antes de desistir. */
    public static final int DURA = 100;

    private static final EntityDataAccessor<Integer> QUAL =
            SynchedEntityData.defineId(LilithSpellEntity.class, EntityDataSerializers.INT);

    public LilithSpellEntity(EntityType<? extends LilithSpellEntity> type, Level level) {
        super(type, level);
    }

    /** Lilith atira um dos cinco. */
    public static void atira(ServerLevel level, LilithEntity quem, Vec3 rumo) {
        var feitiço = net.thaumcraft.occulta.OccultaEntities.LILITH_SPELL.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (feitiço == null) return;
        feitiço.setOwner(quem);
        feitiço.qual(Feitiço.sorteia(level.getRandom()));
        feitiço.snapTo(quem.getX(), quem.getY() + quem.getBbHeight() / 2.0, quem.getZ(), 0.0f, 0.0f);
        feitiço.shoot(rumo.x, rumo.y, rumo.z, 0.8f, 2.0f);
        level.addFreshEntity(feitiço);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dados) {
        dados.define(QUAL, 0);
    }

    public Feitiço qual() {
        return Feitiço.values()[Math.clamp(this.entityData.get(QUAL), 0, Feitiço.values().length - 1)];
    }

    public void qual(Feitiço qual) {
        this.entityData.set(QUAL, qual.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > DURA) {
            this.discard();
            return;
        }

        HitResult onde = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnMoveVector(
                this, oquê -> this.canHitEntity(oquê));
        if (onde.getType() != HitResult.Type.MISS) this.onHit(onde);

        this.setPos(this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z);
        if (this.level() instanceof ServerLevel mundo) {
            mundo.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(),
                    2, 0.1, 0.1, 0.1, 0.0);
        }
    }

    @Override
    protected void onHit(HitResult onde) {
        super.onHit(onde);
        if (!(this.level() instanceof ServerLevel level)) {
            this.discard();
            return;
        }
        LivingEntity quemAtirou = this.getOwner() instanceof LivingEntity vivo ? vivo : null;
        LivingEntity acertou = onde instanceof EntityHitResult emQuem
                && emQuem.getEntity() instanceof LivingEntity vivo ? vivo : null;

        this.cai(level, quemAtirou, acertou);
        level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(),
                16, 0.3, 0.3, 0.3, 0.1);
        this.discard();
    }

    /** O que cada um dos cinco faz onde cai. */
    private void cai(ServerLevel level, LivingEntity quemAtirou, LivingEntity acertou) {
        Feitiço qual = this.qual();
        double raio = qual.raio;

        if (raio <= 0.0) {
            if (acertou != null && acertou != quemAtirou) this.pega(level, quemAtirou, acertou);
            return;
        }
        AABB roda = new AABB(this.getX() - raio, this.getY() - raio, this.getZ() - raio,
                this.getX() + raio, this.getY() + raio, this.getZ() + raio);
        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (quem.distanceToSqr(this) > raio * raio) continue;
            if (quem == quemAtirou) continue;
            this.pega(level, quemAtirou, quem);
        }
    }

    /** E o que ele faz a cada um que apanhou. */
    private void pega(ServerLevel level, LivingEntity quemAtirou, LivingEntity quem) {
        switch (this.qual()) {
            case IGNIANIMA -> this.queima(level, quemAtirou, quem);
            case FLIPENDO -> {
                double força = EMPURRA + (quem.hasEffect(MobEffects.SLOWNESS) ? EMPURRA_MAIS : 0.0);
                quem.setDeltaMovement(this.getDeltaMovement().x * força, EMPURRA_PARA_CIMA,
                        this.getDeltaMovement().z * força);
                quem.hurtMarked = true;
            }
            case IMPEDIMENTA -> {
                if (quem.hasEffect(MobEffects.SLOWNESS)) return;
                quem.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, PRENDE, 1));
            }
            case CONFUNDUS -> {
                if (quem.hasEffect(MobEffects.BLINDNESS)) return;
                quem.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, PRENDE));
            }
            case ATTRAHO -> {
                if (quemAtirou == null) return;
                Vec3 rumo = quemAtirou.position().subtract(quem.position());
                if (rumo.lengthSqr() <= 0.0) return;
                rumo = rumo.normalize();
                quem.setDeltaMovement(quem.getDeltaMovement()
                        .add(rumo.x * PUXA, PUXA_PARA_CIMA, rumo.z * PUXA));
                quem.hurtMarked = true;
            }
        }
    }

    /**
     * <b>Ignianima</b>: queima mais quanto mais ferida estiver quem o atirou.
     *
     * <p>A conta é a do original, degrau por degrau — e o último degrau é o que importa: abaixo de dez de
     * vida equivalente, o dano é {@code 6 + (12 - vida) / 2}, que num chefe quase vencido chega a nove. E
     * contra um jogador ele ainda <b>escala com a vida máxima dele</b>, de modo que armadura de corações não
     * o salva.
     */
    private void queima(ServerLevel level, LivingEntity quemAtirou, LivingEntity quem) {
        float dano = 4.0f;
        if (quemAtirou != null) {
            float vida = 20.0f * (quemAtirou.getHealth() / quemAtirou.getMaxHealth());
            if (vida > 19.0f) dano = 2.0f;
            else if (vida > 15.0f) dano = 3.0f;
            else if (vida > 10.0f) dano = 5.0f;
            else dano = 6.0f + (12.0f - vida) / 2.0f;
        }
        float escala = quem instanceof Player ? quem.getMaxHealth() / 20.0f : 1.0f;
        quem.hurtServer(level, level.damageSources().indirectMagic(this, quemAtirou), dano * escala);
        level.sendParticles(ParticleTypes.FLAME, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                16, 0.5, 1.0, 0.5, 0.0);
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity quem) {
        return super.canHitEntity(quem) && quem instanceof LivingEntity;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putInt("Feitico", this.entityData.get(QUAL));
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.entityData.set(QUAL, dados.getIntOr("Feitico", 0));
    }
}
