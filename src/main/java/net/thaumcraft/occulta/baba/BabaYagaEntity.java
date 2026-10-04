package net.thaumcraft.occulta.baba;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A <b>Baba Yaga</b>: o {@code EntityBabaYaga} do Witchery, e a coisa mais perigosa do ofício.
 *
 * <p>Ela é uma bruxa do jogo levada ao extremo e depois torcida: <b>quinhentos de vida</b>, mas o que faz
 * dela um chefe não é a vida — é que ela <b>não se deixa alcançar</b>.
 *
 * <ul>
 *   <li><b>Nenhuma pancada lhe tira mais de quinze.</b> Não importa a espada, o encantamento ou a poção: o
 *       teto é quinze, e por isso ela custa no mínimo trinta e quatro golpes. E <b>magia tira quinze por
 *       cento</b> do que tiraria.</li>
 *   <li><b>Ela teleporta.</b> Sempre que o caminho dela fecha — ou uma vez em cinquenta, a esmo —, ela
 *       aparece a oito blocos de quem persegue, pelo caminho do enderman.</li>
 *   <li><b>E não a deixa fugir pelo ar.</b> Quem estiver caindo ou voando apanha <b>Lentidão VI</b> por dez
 *       segundos, uma vez em vinte.</li>
 *   <li><b>Ela atira os cozimentos do ofício</b> — teias, espinhos, tinta — e, quando não, as poções de
 *       arremesso do jogo. E <b>bebe</b> as dela: resistência ao fogo quando arde, cura quando está ferida,
 *       rapidez quando o alvo está longe. Enquanto bebe, anda um quarto mais devagar.</li>
 * </ul>
 *
 * <h2>E ela pode ser chamada</h2>
 *
 * <p>Com <b>dono</b>, ela deixa de ser inimiga dele e vira outra coisa: de cinco em cinco segundos, se ele
 * estiver a oito blocos, ela <b>larga no chão</b> os ingredientes do ofício — e <b>some ao fim de trinta
 * segundos</b>. É a Baba da bola de cristal: uma visita, não uma conquista.
 */
public class BabaYagaEntity extends Monster implements RangedAttackMob {
    /** A vida dela, e o teto de dano por pancada. */
    public static final double VIDA = 500.0;
    public static final float TETO_DA_PANCADA = 15.0f;

    /** Quanto a magia tira do que tiraria. */
    public static final float MAGIA_VALE = 0.15f;

    /** De quanto em quanto ela pode teleportar, e para que distância. */
    public static final int ENTRE_SALTOS = 100;
    public static final double SALTO = 8.0;

    /** A lentidão de quem tenta fugir pelo ar. */
    public static final int LENTIDÃO_GRAU = 5;
    public static final int LENTIDÃO_TEMPO = 200;

    /** Quanto tempo a chamada dura, e de quanto em quanto ela larga coisa. */
    public static final int VISITA = 600;
    public static final int LARGA_DE = 100;
    public static final double PERTO_DO_DONO = 64.0;

    /** O quarto de velocidade que ela perde enquanto bebe. */
    private static final UUID BEBENDO = UUID.fromString("ab0df555-0786-4951-a8df-ca61749f164e");
    private static final AttributeModifier DEVAGAR = new AttributeModifier(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("thaumcraft", "baba_drinking"),
            -0.25, AttributeModifier.Operation.ADD_VALUE);

    private int bebeAté;
    private long últimoSalto;
    @Nullable
    private UUID dono;

    public BabaYagaEntity(EntityType<? extends BabaYagaEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 70;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0,
                false, true, (quem, onde) -> !this.éDoDono(quem)));
    }

    /** Quem a chamou, se alguém a chamou. */
    public void dono(Player quem) {
        this.dono = quem.getUUID();
    }

    @Nullable
    public UUID dono() {
        return this.dono;
    }

    private boolean éDoDono(LivingEntity quem) {
        return this.dono != null && quem.getUUID().equals(this.dono);
    }

    /** Se ela está bebendo. */
    public boolean bebendo() {
        return this.bebeAté > 0;
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel mundo) {
            if (this.bebendo()) this.acabaDeBeber();
            else this.escolheOQueBeber();

            this.saltaSePreciso(mundo);
            this.prendeQuemVoa(mundo);
            this.aVisita(mundo);
        }
        super.aiStep();
    }

    /** Acabado o gole, o efeito entra e a velocidade volta. */
    private void acabaDeBeber() {
        if (--this.bebeAté > 0) return;
        ItemStack naMão = this.getMainHandItem();
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (naMão.is(Items.POTION)) {
            var oquê = naMão.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            if (oquê != null) oquê.forEachEffect(this::addEffect, 1.0f);
        }
        var velocidade = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (velocidade != null) velocidade.removeModifier(DEVAGAR.id());
    }

    /** Quando beber, e o quê: a escada do original, pela ordem dele. */
    private void escolheOQueBeber() {
        var sorte = this.getRandom();
        net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> qual = null;

        if (sorte.nextFloat() < 0.15f && this.isOnFire() && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            qual = Potions.FIRE_RESISTANCE;
        } else if (sorte.nextFloat() < 0.01f && this.getHealth() < this.getMaxHealth()) {
            qual = Potions.HEALING;
        } else if (sorte.nextFloat() < 0.25f && this.getTarget() != null
                && !this.hasEffect(MobEffects.SPEED)
                && this.getTarget().distanceToSqr(this) > 121.0) {
            qual = Potions.SWIFTNESS;
        }
        if (qual == null) return;

        ItemStack frasco = PotionContents.createItemStack(Items.POTION, qual);
        this.setItemSlot(EquipmentSlot.MAINHAND, frasco);
        this.bebeAté = frasco.getUseDuration(this) - 20;
        var velocidade = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (velocidade != null) {
            velocidade.removeModifier(DEVAGAR.id());
            velocidade.addTransientModifier(DEVAGAR);
        }
    }

    /** Ela salta quando o caminho fecha, ou uma vez em cinquenta. */
    private void saltaSePreciso(ServerLevel level) {
        LivingEntity alvo = this.getTarget();
        if (alvo == null) return;
        if (!this.getNavigation().isDone() && level.getRandom().nextDouble() >= 0.02) return;
        if (this.tickCount - this.últimoSalto <= ENTRE_SALTOS) return;

        this.últimoSalto = this.tickCount;
        this.saltaPara(level, alvo);
    }

    /** O salto do enderman: oito blocos para trás de quem persegue. */
    public boolean saltaPara(ServerLevel level, LivingEntity alvo) {
        Vec3 rumo = new Vec3(this.getX() - alvo.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0 - alvo.getY() - alvo.getEyeHeight(),
                this.getZ() - alvo.getZ()).normalize();
        var sorte = level.getRandom();
        double x = this.getX() + (sorte.nextDouble() - 0.5) * SALTO - rumo.x * SALTO;
        double y = this.getY() + (sorte.nextInt(16) - 8) - rumo.y * SALTO;
        double z = this.getZ() + (sorte.nextDouble() - 0.5) * SALTO - rumo.z * SALTO;
        return this.saltaAté(level, x, y, z);
    }

    /** Desce até achar chão, e desiste se não couber. */
    private boolean saltaAté(ServerLevel level, double x, double y, double z) {
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
                antes.x, antes.y + 1.0, antes.z, 64, 0.5, 1.0, 0.5, 0.2);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(), 64, 0.5, 1.0, 0.5, 0.2);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE,
                1.0f, 1.0f);
        return true;
    }

    /** E quem tenta fugir pelo ar fica pesado. */
    private void prendeQuemVoa(ServerLevel level) {
        if (level.getRandom().nextDouble() >= 0.05) return;
        LivingEntity alvo = this.getTarget();
        if (alvo == null || alvo.hasEffect(MobEffects.SLOWNESS)) return;

        boolean noAr = !alvo.onGround()
                || (alvo instanceof Player gente && gente.getAbilities().flying);
        if (!noAr) return;
        alvo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, LENTIDÃO_TEMPO, LENTIDÃO_GRAU));
    }

    /** A visita: ela larga coisa para quem a chamou, e some ao fim de trinta segundos. */
    private void aVisita(ServerLevel level) {
        if (this.dono == null) return;
        Player quem = level.getPlayerByUUID(this.dono);

        if (quem != null && this.distanceToSqr(quem) < PERTO_DO_DONO && this.tickCount % LARGA_DE == 0) {
            int quantos = this.getRandom().nextInt(3);
            for (int n = 0; n < quantos; n++) {
                this.spawnAtLocation(level, new ItemStack(presente(level)));
            }
        }
        if (this.tickCount > VISITA) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(), 64, 0.5, 1.0, 0.5, 0.2);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 1.0f, 1.0f);
            this.discard();
        }
    }

    /** O que ela larga: os ingredientes do ofício, pela lista do original. */
    public static net.minecraft.world.item.Item presente(ServerLevel level) {
        var quais = java.util.List.of(
                OccultaItems.SPECTRAL_DUST, OccultaItems.BAT_WOOL, OccultaItems.TOE_OF_FROG,
                OccultaItems.DOG_TONGUE, OccultaItems.BREW_OF_VINES, OccultaItems.BREW_OF_SPROUTING,
                OccultaItems.BREW_OF_INK);
        return quais.get(level.getRandom().nextInt(quais.size()));
    }

    /**
     * <b>Nenhuma pancada lhe tira mais de quinze</b>, e magia tira quinze por cento do que tiraria.
     *
     * <p>É isto que faz dela um chefe e não um saco de vida: ela custa no mínimo trinta e quatro golpes, e
     * quem a quiser matar à distância, com magia, precisa de duzentos e vinte.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (fonte.getEntity() == this) return false;
        float depois = Math.min(quanto, TETO_DA_PANCADA);
        if (fonte.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)) {
            depois *= MAGIA_VALE;
        }
        return super.hurtServer(level, fonte, depois);
    }

    /** Ela atira os cozimentos do ofício — e, uma vez em três não, as poções do jogo. */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (this.bebendo()) return;
        if (!(this.level() instanceof ServerLevel level)) return;

        boolean doOfício = level.getRandom().nextInt(3) == 0;
        var frasco = doOfício ? new ItemStack(cozimento(level)) : poçãoDoJogo(level, alvo);

        var atirado = doOfício
                ? new net.thaumcraft.occulta.kettle.KettleBrewProjectile(level, this, frasco)
                : null;

        double dx = alvo.getX() + alvo.getDeltaMovement().x - this.getX();
        double dy = alvo.getEyeY() - 1.1 - this.getY();
        double dz = alvo.getZ() + alvo.getDeltaMovement().z - this.getZ();
        float plano = (float) Math.sqrt(dx * dx + dz * dz);

        if (atirado != null) {
            atirado.setXRot(atirado.getXRot() + 20.0f);
            atirado.shoot(dx, dy + plano * 0.2f, dz, 0.75f, 8.0f);
            level.addFreshEntity(atirado);
            return;
        }

        var poção = new net.minecraft.world.entity.projectile.throwableitemprojectile
                .ThrownSplashPotion(level, this, frasco);
        poção.setXRot(poção.getXRot() + 20.0f);
        poção.shoot(dx, dy + plano * 0.2f, dz, 0.75f, 8.0f);
        level.addFreshEntity(poção);
    }

    /** Os cozimentos que ela atira: os do ofício que este porte tem. */
    private static net.minecraft.world.item.Item cozimento(ServerLevel level) {
        var quais = java.util.List.of(
                OccultaItems.BREW_OF_WEBS, OccultaItems.BREW_OF_WEBS,
                OccultaItems.BREW_OF_THORNS, OccultaItems.BREW_OF_THORNS,
                OccultaItems.BREW_OF_INK, OccultaItems.BREW_OF_INK,
                OccultaItems.BREW_OF_ICE, OccultaItems.BREW_OF_INFECTION);
        return quais.get(level.getRandom().nextInt(quais.size()));
    }

    /** E a poção do jogo, escolhida pelo que o alvo está fazendo — como a bruxa do jogo escolhe. */
    private static ItemStack poçãoDoJogo(ServerLevel level, LivingEntity alvo) {
        double quão = alvo.distanceToSqr(alvo);
        var qual = Potions.HARMING;
        if (!alvo.hasEffect(MobEffects.SLOWNESS)) qual = Potions.SLOWNESS;
        else if (alvo.getHealth() >= 8.0f && !alvo.hasEffect(MobEffects.POISON)) qual = Potions.POISON;
        else if (!alvo.hasEffect(MobEffects.WEAKNESS) && level.getRandom().nextFloat() < 0.25f) {
            qual = Potions.WEAKNESS;
        }
        return PotionContents.createItemStack(Items.SPLASH_POTION, qual);
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return net.thaumcraft.occulta.OccultaSounds.BABA_LIVING.value();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.WITCH_HURT;
    }

    /**
     * E a morte é a dela. O <b>golpe</b>, não: o original deixa nele o som da bruxa do jogo, e é o que
     * fica aqui.
     */
    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return net.thaumcraft.occulta.OccultaSounds.BABA_DEATH.value();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        if (this.dono != null) dados.store("Owner", net.minecraft.core.UUIDUtil.CODEC, this.dono);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.dono = dados.read("Owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }
}
