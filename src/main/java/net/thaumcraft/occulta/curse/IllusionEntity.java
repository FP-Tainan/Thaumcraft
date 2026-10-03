package net.thaumcraft.occulta.curse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Uma <b>visão</b>: o {@code EntityIllusion} do Witchery, que é a melhor ideia da Loucura.
 *
 * <p>Ela tem a cara de um creeper, de uma aranha ou de um zumbi, anda na direção de quem a vê, e <b>não é
 * nada</b>:
 *
 * <ul>
 *   <li><b>Não faz dano.</b> O ataque dela devolve que acertou, e não tira nada.</li>
 *   <li><b>Não leva dano.</b> Bater nela é bater no ar — e ela não larga nada ao sair.</li>
 *   <li><b>Ela se apaga sozinha.</b> Um em cada quinze batidas perde um de vida, e abaixo de meio some.</li>
 *   <li><b>E o barulho é só para quem a vê.</b> O chiado da aranha, o gemido do zumbi: o original manda o som
 *       <b>a uma pessoa só</b>, a que está amaldiçoada. Quem estiver ao lado não ouve nada.</li>
 * </ul>
 *
 * <p>É isso que faz dela horror e não um bicho: quem a tem vê um creeper vindo, corre, e no meio da fuga ele
 * desaparece — e ninguém mais viu nada.
 */
public class IllusionEntity extends Monster {
    /** Quantas batidas, em média, até ela perder um de vida. */
    public static final int APAGA = 15;

    /** E de quanto em quanto ela tenta fazer o barulho do bicho que finge ser. */
    public static final int BARULHO = 40;

    /** Até onde quem a vê ouve o barulho dela: os oito blocos do original, ao quadrado. */
    public static final double OUVE_SE = 64.0;

    /**
     * Quem a está vendo.
     *
     * <p>Não vai pela rede, e não precisa: o desenho dela é o do bicho que finge ser, e o barulho sai daqui.
     * O jogo de hoje também não tem serializador de UUID para dados sincronizados.
     */
    @Nullable
    private UUID vítima;

    public IllusionEntity(EntityType<? extends IllusionEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                // zero de ataque, porque ela não bate: é o do original
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Quem a está vendo. */
    public void vitima(Player quem) {
        this.vítima = quem.getUUID();
        this.setTarget(quem);
    }

    @Nullable
    public Player vitima() {
        return this.vítima == null ? null : this.level().getPlayerByUUID(this.vítima);
    }

    /** O alvo dela é sempre quem a vê: o {@code getAttackTarget} do original. */
    @Override
    @Nullable
    public LivingEntity getTarget() {
        Player quem = this.vitima();
        return quem != null ? quem : super.getTarget();
    }

    /** O som que ela finge. Cada visão tem o seu; o creeper, como no original, não faz nenhum. */
    @Nullable
    protected SoundEvent fingimento() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel mundo)) return;

        if (mundo.getRandom().nextInt(APAGA) == 0) {
            float menos = this.getHealth() - 1.0f;
            if (menos <= 0.5f) {
                this.discard();
                return;
            }
            this.setHealth(menos);
        }

        if (mundo.getRandom().nextInt(BARULHO) != 0) return;
        SoundEvent qual = this.fingimento();
        if (qual == null) return;
        Player quem = this.vitima();
        if (quem == null || quem.distanceToSqr(this) >= OUVE_SE) return;
        // e o barulho vai a UMA pessoa só: quem está ao lado não ouve nada
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(qual), SoundSource.HOSTILE,
                    this.getX(), this.getY(), this.getZ(), 1.0f, 1.0f, mundo.getRandom().nextLong()));
        }
    }

    /** Ela acerta, e não tira nada. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        return true;
    }

    /** E bater nela é bater no ar. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource fonte) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.vítima != null) dados.store("Victim", net.minecraft.core.UUIDUtil.CODEC, this.vítima);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.vítima = dados.read("Victim", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    /** A visão de aranha, que chia. */
    public static class Spider extends IllusionEntity {
        public Spider(EntityType<? extends IllusionEntity> type, Level level) {
            super(type, level);
        }

        @Override
        protected SoundEvent fingimento() {
            return net.minecraft.sounds.SoundEvents.SPIDER_AMBIENT;
        }
    }

    /** E a de zumbi, que geme. */
    public static class Zombie extends IllusionEntity {
        public Zombie(EntityType<? extends IllusionEntity> type, Level level) {
            super(type, level);
        }

        @Override
        protected SoundEvent fingimento() {
            return net.minecraft.sounds.SoundEvents.ZOMBIE_AMBIENT;
        }
    }
}
