package net.thaumcraft.occulta.wolf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * O <b>Caçador Cornudo</b>: o {@code EntityHornedHuntsman} do Witchery.
 *
 * <p>É o quinto degrau da escada do lobisomem, e o único que não se compra: a estátua lhe dá o <b>Chifre da
 * Caça</b> e o manda chamar a coisa que vem quando se sopra. Matá-lo é o degrau.
 *
 * <p>Ele é a <b>caça ao contrário</b>: o lobisomem, que é o que caça, chama o que caça lobisomens.
 *
 * <h2>Porque ele é difícil</h2>
 *
 * <ul>
 *   <li><b>Quatrocentos de vida</b>, e <b>nenhuma pancada lhe tira mais de quinze</b>. Vinte e sete golpes,
 *       no mínimo — e entretanto ele <b>sara um por segundo</b>, o que torna lento demais qualquer golpe
 *       pequeno.</li>
 *   <li><b>Ele entra com um estouro</b>, se foi o chifre que o chamou: cento e cinquenta batidas de
 *       invulnerabilidade, saindo com um estouro de seis — o do Wither — e, enquanto espera, <b>sara vinte de
 *       dez em dez batidas</b>, começando com um quarto da vida. Dá tempo de correr, e é a única coisa que
 *       esse tempo serve.</li>
 *   <li><b>Ele atira.</b> Uma vez em cinco, de segundo a segundo, uma flecha com a força da distância e
 *       <b>empurrão de dois</b>.</li>
 *   <li><b>E traz cães.</b> De duzentas a quinhentas batidas, um <b>lobo raivoso</b> com Regeneração II que
 *       não acaba.</li>
 *   <li><b>Não se pode fugir dele a pé</b>: quando o caminho fecha, ele <b>aparece</b> ao lado de quem
 *       persegue, pelo caminho do enderman.</li>
 *   <li>E a pancada dele é <b>sete mais até quinze</b>, com <b>levantada</b> — que é como se perde um combate
 *       num buraco.</li>
 * </ul>
 *
 * <p>Larga as <b>caveiras de wither</b> que ninguém mais larga fora do Nether, um <b>livro encantado</b>,
 * <b>Sangue Infernal</b> e, uma vez em quatro, a <b>Lança do Caçador</b>.
 */
public class HornedHuntsmanEntity extends Monster implements RangedAttackMob {
    /** A vida dele, e o teto de dano por pancada. */
    public static final double VIDA = 400.0;
    public static final float TETO_DA_PANCADA = 15.0f;

    /** A espera do estouro de entrada, e o estouro. */
    public static final int ESPERA = 150;
    public static final float ESTOURO = 6.0f;

    /** Quanto ele sara enquanto espera, e de quanto em quanto. */
    public static final float SARA_ESPERANDO = 20.0f;
    public static final int SARA_ESPERANDO_DE = 10;

    /** E quanto sara depois, uma vez por segundo. */
    public static final float SARA = 1.0f;
    public static final int SARA_DE = 20;

    /** O tiro: uma vez em cinco, de segundo a segundo, até trinta blocos. */
    public static final int ATIRA_DE = 20;
    public static final int ATIRA_UMA_EM = 5;
    public static final float ALCANCE = 30.0f;
    public static final int EMPURRÃO = 2;

    /** Os cães: de duzentas a quinhentas batidas, e só com o alvo a dezesseis blocos. */
    public static final int CÃES_BASE = 200;
    public static final int CÃES_PASSO = 100;
    public static final double CÃES_PERTO = 256.0;
    public static final int REGENERAÇÃO = 20000;

    /** De quanto em quanto ele pode aparecer ao lado de quem persegue, e a que distância. */
    public static final int ENTRE_SALTOS = 200;
    public static final double SALTO = 8.0;

    /** A pancada: sete mais até quinze, e a levantada. */
    public static final int PANCADA = 7;
    public static final int PANCADA_MAIS = 15;
    public static final double LEVANTA = 0.4;

    /** Quanto dura a pancada no desenho dele. */
    public static final int BRAÇO = 10;

    /** A espera que falta, que o cliente precisa de saber para o pó e o barulho. */
    private static final EntityDataAccessor<Integer> ESPERANDO =
            SynchedEntityData.defineId(HornedHuntsmanEntity.class, EntityDataSerializers.INT);

    /** E o braço levantado, que o modelo lê. */
    private static final EntityDataAccessor<Integer> PANCADA_NO_BRAÇO =
            SynchedEntityData.defineId(HornedHuntsmanEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent barra = new ServerBossEvent(this.getUUID(), Component.empty(),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    /** Se foi o chifre que o chamou — e então a entrada dele estoura. */
    private boolean entradaEstoura;

    private long últimoSalto;

    public HornedHuntsmanEntity(EntityType<? extends HornedHuntsmanEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 70;
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0, 48.0f));
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.0, ATIRA_DE, 60, ALCANCE));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dados) {
        super.defineSynchedData(dados);
        dados.define(ESPERANDO, 0);
        dados.define(PANCADA_NO_BRAÇO, 0);
    }

    // ------------------------------------------------------------------ a entrada

    /** Diz a ele que foi o chifre que o chamou, e que a entrada dele estoura. */
    public void entradaEstoura() {
        this.entradaEstoura = true;
    }

    /**
     * Começa a espera: o {@code func_82206_m} do Wither, que o original copia inteiro.
     *
     * <p>Ele fica com <b>um quarto da vida</b> e sara vinte de dez em dez batidas — chegando ao fim com os
     * quatrocentos e um estouro.
     */
    public void acendeAEspera() {
        this.esperando(ESPERA);
        this.setHealth(this.getMaxHealth() / 4.0f);
    }

    /** A espera que falta. */
    public int esperando() {
        return this.entityData.get(ESPERANDO);
    }

    public void esperando(int quanto) {
        this.entityData.set(ESPERANDO, quanto);
    }

    /** Quanto falta da pancada no braço dele: o que o modelo lê. */
    public int pancadaNoBraço() {
        return this.entityData.get(PANCADA_NO_BRAÇO);
    }

    // ------------------------------------------------------------------ o que ele faz

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.esperando() > 0) {
            this.espera(level);
            return;
        }

        super.customServerAiStep(level);
        if (this.tickCount % SARA_DE == 0) this.heal(SARA);
        this.atiraSeDer(level);
        this.trazUmCão(level);
        this.apareceSePreciso(level);
    }

    /** A espera, e o estouro no fim dela. */
    private void espera(ServerLevel level) {
        int falta = this.esperando() - 1;
        if (falta <= 0) {
            if (this.entradaEstoura) {
                level.explode(this, this.getX(), this.getY() + this.getEyeHeight(), this.getZ(), ESTOURO,
                        false, Level.ExplosionInteraction.MOB);
            }
            level.levelEvent(LevelEvent.SOUND_WITHER_BOSS_SPAWN, this.blockPosition(), 0);
        }
        this.esperando(falta);
        if (this.tickCount % SARA_ESPERANDO_DE == 0) this.heal(SARA_ESPERANDO);
    }

    /** O tiro: uma vez em cinco, de segundo a segundo, e só vendo o alvo. */
    private void atiraSeDer(ServerLevel level) {
        if (this.tickCount % ATIRA_DE != 0) return;
        if (level.getRandom().nextInt(ATIRA_UMA_EM) != 0) return;
        LivingEntity alvo = this.getTarget();
        if (alvo == null || !this.getSensing().hasLineOfSight(alvo)) return;

        this.getLookControl().setLookAt(alvo, 30.0f, 30.0f);
        /*
         * A força do tiro é a distância medida no alcance dele — perto bate pouco, longe bate muito. É o que
         * o original faz, e é ao contrário do que se espera.
         */
        double quão = this.distanceToSqr(alvo.getX(), alvo.getBoundingBox().minY, alvo.getZ());
        this.performRangedAttack(alvo, (float) Math.sqrt(quão) / ALCANCE);
    }

    /**
     * E ele traz cães: um lobo raivoso, de duzentas a quinhentas batidas.
     *
     * <p>Com <b>Regeneração II que não acaba</b> — vinte mil batidas, que são dezesseis minutos. O lobo não
     * morre do que lhe fazem: morre quando o Caçador morrer e deixar de os trazer.
     */
    private void trazUmCão(ServerLevel level) {
        int cada = CÃES_BASE + level.getRandom().nextInt(4) * CÃES_PASSO;
        if (this.tickCount % cada != 0) return;
        LivingEntity alvo = this.getTarget();
        if (alvo == null || this.distanceToSqr(alvo) > CÃES_PERTO) return;
        if (!this.getSensing().hasLineOfSight(alvo)) return;

        var sorte = level.getRandom();
        Wolf cão = net.minecraft.world.entity.EntityTypes.WOLF.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (cão == null) return;
        cão.snapTo(this.getX() - 0.5 + sorte.nextDouble(), this.getY(),
                this.getZ() - 0.5 + sorte.nextDouble(), this.getYHeadRot(), this.getXRot());
        cão.setTarget(alvo);
        cão.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERAÇÃO, 1));
        level.addFreshEntity(cão);
        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                cão.getX(), cão.getY() + 1.0, cão.getZ(), 10, 2.0, 2.0, 2.0, 0.0);
        level.playSound(null, cão.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE,
                1.0f, 1.0f);
    }

    /** Quando o caminho fecha, ele aparece ao lado de quem persegue. */
    private void apareceSePreciso(ServerLevel level) {
        LivingEntity alvo = this.getTarget();
        if (alvo == null || !this.getNavigation().isDone()) return;
        if (this.tickCount - this.últimoSalto <= ENTRE_SALTOS) return;
        this.últimoSalto = this.tickCount;
        this.apareceJunto(level, alvo);
    }

    /** O salto do enderman: oito blocos para o lado de quem persegue. */
    public boolean apareceJunto(ServerLevel level, LivingEntity alvo) {
        Vec3 rumo = new Vec3(this.getX() - alvo.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0 - alvo.getY() - alvo.getEyeHeight(),
                this.getZ() - alvo.getZ()).normalize();
        var sorte = level.getRandom();
        double x = this.getX() + (sorte.nextDouble() - 0.5) * SALTO - rumo.x * SALTO;
        double y = this.getY() + (sorte.nextInt(16) - 8) - rumo.y * SALTO;
        double z = this.getZ() + (sorte.nextDouble() - 0.5) * SALTO - rumo.z * SALTO;
        return this.apareceEm(level, x, y, z);
    }

    private boolean apareceEm(ServerLevel level, double x, double y, double z) {
        BlockPos onde = BlockPos.containing(x, y, z);
        while (onde.getY() > level.getMinY() && !level.getBlockState(onde.below()).isSolid()) {
            onde = onde.below();
        }
        if (onde.getY() <= level.getMinY()) return false;

        Vec3 antes = this.position();
        this.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, this.getYRot(), this.getXRot());
        if (!level.noCollision(this) || level.containsAnyLiquid(this.getBoundingBox())) {
            this.snapTo(antes.x, antes.y, antes.z, this.getYRot(), this.getXRot());
            return false;
        }

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                antes.x, antes.y + 1.0, antes.z, 128, 0.5, 1.0, 0.5, 0.2);
        level.playSound(null, BlockPos.containing(antes), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0f, 1.0f);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0f, 1.0f);
        return true;
    }

    /**
     * A flecha dele: a velocidade do arco e a mira que aperta com a dificuldade.
     *
     * <p>O <b>empurrão de dois</b> do original vem aqui por onde ele vem no jogo de hoje — o
     * <b>Impulso II</b> no arco que ele não mostra. É a mesma conta e o mesmo efeito.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel level)) return;

        AbstractArrow tiro = new Arrow(level, this, new ItemStack(Items.ARROW), arco(level));
        tiro.setBaseDamage(força * 8.0f + this.getRandom().nextGaussian() * 0.25
                + level.getDifficulty().getId() * 0.11f);

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getY(0.3333333333333333) - tiro.getY();
        double dz = alvo.getZ() - this.getZ();
        double plano = Math.sqrt(dx * dx + dz * dz);
        tiro.shoot(dx, dy + plano * 0.2, dz, 1.6f, 14 - level.getDifficulty().getId() * 4);

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f,
                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        level.addFreshEntity(tiro);
    }

    /** O arco que não se vê, só para a flecha levar o empurrão. */
    private static ItemStack arco(ServerLevel level) {
        ItemStack arco = new ItemStack(Items.BOW);
        var quais = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        quais.get(Enchantments.PUNCH).ifPresent(qual -> arco.enchant(qual, EMPURRÃO));
        return arco;
    }

    /**
     * A pancada dele: <b>sete mais até quinze</b>, e quem apanha <b>sobe</b>.
     *
     * <p>A levantada é o que faz dele uma coisa a que não se pode encostar: ele bate, o chão se vai, e o
     * golpe seguinte apanha quem está no ar.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity alvo) {
        this.entityData.set(PANCADA_NO_BRAÇO, BRAÇO);
        this.level().broadcastEntityEvent(this, (byte) 4);

        boolean acertou = alvo.hurtServer(level, this.damageSources().mobAttack(this),
                PANCADA + this.getRandom().nextInt(PANCADA_MAIS));
        if (acertou) alvo.setDeltaMovement(alvo.getDeltaMovement().add(0.0, LEVANTA, 0.0));
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return acertou;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual == 4) {
            this.entityData.set(PANCADA_NO_BRAÇO, BRAÇO);
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
            return;
        }
        super.handleEntityEvent(qual);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int braço = this.pancadaNoBraço();
        if (braço > 0) this.entityData.set(PANCADA_NO_BRAÇO, braço - 1);
        if (!this.level().isClientSide()) this.barra.setProgress(this.getHealth() / this.getMaxHealth());
    }

    /**
     * <b>Nenhuma pancada lhe tira mais de quinze.</b>
     *
     * <p>É o mesmo teto da Baba Yaga, e é o que faz dele um chefe: vinte e sete golpes no mínimo, enquanto ele
     * sara um por segundo.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return super.hurtServer(level, fonte, Math.min(quanto, TETO_DA_PANCADA));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance qual) {
        return qual.getEffect() != MobEffects.WITHER && super.canBeAffected(qual);
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    /** Ele não arde no sol, e a luz dele é sempre a mesma: o {@code getBrightness} do original. */
    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    // ------------------------------------------------------------------ o que ele larga

    /**
     * As <b>caveiras de wither</b>, um <b>livro encantado</b>, o <b>Sangue Infernal</b> e, uma vez em quatro,
     * a <b>Lança do Caçador</b>.
     *
     * <p>Duas caveiras, ou três uma vez em três: é o maior monte de caveiras que o jogo dá de uma vez, e é
     * por isso que o Caçador se chama mais de uma vez.
     *
     * <p>O livro do original é um encantamento sorteado no registro, com o nível entre o mínimo mais dois e o
     * máximo dele. Aqui é o <b>livro de nível trinta</b> do jogo, que é a mesma coisa pelo meio do caminho de
     * hoje, e é o que o resto do porte já usa.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        super.dropCustomDeathLoot(level, fonte, matouGente);
        var sorte = this.getRandom();

        this.spawnAtLocation(level, new ItemStack(Items.WITHER_SKELETON_SKULL,
                sorte.nextInt(3) == 0 ? 3 : 2));
        this.spawnAtLocation(level, EnchantmentHelper.enchantItem(sorte, new ItemStack(Items.BOOK), 30,
                level.registryAccess(), Optional.empty()));
        this.spawnAtLocation(level, new ItemStack(OccultaItems.INFERNAL_BLOOD));
        if (sorte.nextInt(4) == 0) {
            this.spawnAtLocation(level, new ItemStack(OccultaItems.HUNTSMANS_SPEAR));
        }
    }

    // ------------------------------------------------------------------ barulho, nome e barra

    @Override
    public Component getName() {
        return this.hasCustomName() ? super.getName()
                : Component.translatable("entity.thaumcraft.horned_huntsman");
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos onde, net.minecraft.world.level.block.state.BlockState oquê) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer quem) {
        super.startSeenByPlayer(quem);
        this.barra.addPlayer(quem);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer quem) {
        super.stopSeenByPlayer(quem);
        this.barra.removePlayer(quem);
    }

    @Override
    public void setCustomName(@Nullable Component nome) {
        super.setCustomName(nome);
        this.barra.setName(this.getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putInt("Invul", this.esperando());
        dados.putBoolean("explosiveEntrance", this.entradaEstoura);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.esperando(dados.getIntOr("Invul", 0));
        this.entradaEstoura = dados.getBooleanOr("explosiveEntrance", false);
        if (this.hasCustomName()) this.barra.setName(this.getDisplayName());
    }

    /** Sem uso fora do porte: o tipo dele, para quem o chama não ter de o procurar. */
    public static EntityType<HornedHuntsmanEntity> tipo() {
        return OccultaEntities.HORNED_HUNTSMAN;
    }
}
