package net.thaumcraft.occulta.vampire;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Lilith</b>: o {@code EntityLilith} do Witchery, e a coisa mais estranha que o mod tem.
 *
 * <p>Ela é um chefe — duzentos de vida, teto de doze por pancada, bolas de fogo, feitiços — e mesmo assim
 * <b>não se pode matá-la</b>. Levando o último golpe, ela <b>não morre</b>: volta à vida cheia, fica
 * <b>amiga</b>, aparece ao lado de quem a venceu e oferece o que tem.
 *
 * <p>É por isso que o combate com ela não é um combate: é uma <b>prova</b>. Ela não está se defendendo: está
 * vendo se quem a chamou aguenta.
 *
 * <h2>E o que ela dá</h2>
 *
 * <p>Amiga, ela olha o que se traz na mão:
 *
 * <ul>
 *   <li>um <b>Cálice de Vidro</b>, de quem <b>não</b> é vampiro: ela o enche do <b>sangue dela</b> e
 *       some. Beber esse cálice é <b>virar vampiro</b>, e é a única porta que o mod tem;</li>
 *   <li><b>sementes de alho</b>, de quem <b>é</b> vampiro: ela <b>cura</b>. É a única cura que há;</li>
 *   <li>uma <b>papoula</b>, ao sexto grau: ela dá o sétimo, que é o do voo de morcego;</li>
 *   <li>e qualquer outra coisa <b>encantável</b>: ela a encanta como uma mesa de nível quarenta, e a devolve
 *       inteira.</li>
 * </ul>
 *
 * <p>Seja o que for, ela <b>vai-se embora depois</b>: é uma visita, e uma só.
 *
 * <h2>O combate</h2>
 *
 * <ul>
 *   <li><b>Nenhuma pancada lhe tira mais de doze</b>, e ela <b>sara cinco por segundo</b> — a não ser que
 *       esteja <b>enregelada</b> ou <b>fraca</b>, e então sara só um. É o único jeito de a vencer: tirar-lhe
 *       a cura antes de lhe tirar a vida;</li>
 *   <li>e uma <b>bola de fogo grande</b> na cara dela a deixa fraca por dez segundos. É a resposta que o mod
 *       dá a quem reparar que ela se cura: devolver-lhe o fogo dela;</li>
 *   <li>ela <b>apaga a Resistência ao Fogo</b> de quem estiver a trinta e dois blocos, e chove <b>bolas de
 *       fogo pequenas</b> do céu em cima deles;</li>
 *   <li>e a pancada dela é <b>sete mais até quinze</b>, com levantada, como a do Caçador.</li>
 * </ul>
 */
public class LilithEntity extends Monster implements RangedAttackMob {
    /** A vida dela, e o teto de dano por pancada. */
    public static final double VIDA = 200.0;
    public static final float TETO_DA_PANCADA = 12.0f;

    /** Quanto ela sara por segundo, e quanto sara quando está presa. */
    public static final float SARA = 5.0f;
    public static final float SARA_PRESA = 1.0f;

    /** Quanto tempo uma bola de fogo na cara dela lhe tira a cura. */
    public static final int FRAQUEZA = 10;

    /** A espera da entrada, como a do Caçador. */
    public static final int ESPERA = 150;

    /** O alcance do castigo dela, e de quanto em quanto ele cai. */
    public static final double CASTIGA_A = 32.0;
    public static final int CASTIGA_UMA_EM = 5;

    /** A pancada: sete mais até quinze, e a levantada. */
    public static final int PANCADA = 7;
    public static final int PANCADA_MAIS = 15;
    public static final double LEVANTA = 0.4;

    /** Quanto dura a pancada no desenho dela. */
    public static final int BRAÇO = 10;

    /** O nível com que ela encanta o que lhe derem. */
    public static final int ENCANTA_A = 40;

    private static final EntityDataAccessor<Integer> ESPERANDO =
            SynchedEntityData.defineId(LilithEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PANCADA_NO_BRAÇO =
            SynchedEntityData.defineId(LilithEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AMIGA =
            SynchedEntityData.defineId(LilithEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent barra = new ServerBossEvent(this.getUUID(), Component.empty(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private int semCura;

    public LilithEntity(EntityType<? extends LilithEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 60;
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 20, 60, 30.0f));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0,
                true, false, (quem, onde) -> !this.amiga()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder dados) {
        super.defineSynchedData(dados);
        dados.define(ESPERANDO, 0);
        dados.define(PANCADA_NO_BRAÇO, 0);
        dados.define(AMIGA, false);
    }

    /** Se ela já foi vencida, e por isso está amiga. */
    public boolean amiga() {
        return this.entityData.get(AMIGA);
    }

    public int esperando() {
        return this.entityData.get(ESPERANDO);
    }

    public int pancadaNoBraço() {
        return this.entityData.get(PANCADA_NO_BRAÇO);
    }

    /** Começa a espera, como o Caçador: um quarto da vida, e sarando até ao fim dela. */
    public void acendeAEspera() {
        this.entityData.set(ESPERANDO, ESPERA);
        this.setHealth(this.getMaxHealth() / 4.0f);
    }

    // ------------------------------------------------------------------ o que ela faz

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.esperando() > 0) {
            int falta = this.esperando() - 1;
            if (falta <= 0) level.levelEvent(LevelEvent.SOUND_WITHER_BOSS_SPAWN, this.blockPosition(), 0);
            this.entityData.set(ESPERANDO, falta);
            if (this.tickCount % 10 == 0) this.heal(this.getMaxHealth() * 0.75f / 15.0f);
            return;
        }

        super.customServerAiStep(level);
        if (this.amiga()) return;

        if (this.tickCount % 20 == 0) {
            if (this.semCura > 0) this.semCura--;
            this.heal(this.presa() ? SARA_PRESA : SARA);
        }
        if (this.tickCount % 20 == 0 && level.getRandom().nextInt(CASTIGA_UMA_EM) == 0
                && (this.getTarget() != null || this.getLastHurtByMob() != null)) {
            this.castiga(level);
        }
    }

    /**
     * Se alguma coisa lhe tirou a cura.
     *
     * <p>São três: o <b>Enregelado</b> do ofício, a <b>Fraqueza</b>, e a bola de fogo que um jogador lhe
     * devolveu. Sem nenhuma delas, ela sara cinco por segundo e não há como a vencer.
     */
    public boolean presa() {
        if (this.semCura > 0) return true;
        return this.hasEffect(net.thaumcraft.occulta.OccultaEffects.CHILLED)
                || this.hasEffect(MobEffects.WEAKNESS);
    }

    /**
     * <b>O castigo</b>: ela apaga a Resistência ao Fogo de quem está perto e chove fogo em cima deles.
     *
     * <p>É o que faz dela uma coisa a que não se chega com poções: a primeira coisa que ela tira é a poção
     * que o jogador bebeu para a enfrentar.
     */
    private void castiga(ServerLevel level) {
        AABB roda = new AABB(this.getX() - CASTIGA_A, this.getY() - CASTIGA_A / 2.0,
                this.getZ() - CASTIGA_A, this.getX() + CASTIGA_A, this.getY() + CASTIGA_A / 2.0,
                this.getZ() + CASTIGA_A);
        for (Player quem : level.getEntitiesOfClass(Player.class, roda)) {
            quem.removeEffect(MobEffects.FIRE_RESISTANCE);
            if (level.getRandom().nextInt(2) != 0) continue;

            level.playSound(null, quem.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE, 1.0f, 1.0f);
            int quantas = 3 + this.getRandom().nextInt(4);
            for (int n = 0; n < quantas; n++) {
                var bola = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(level, this,
                        new net.minecraft.world.phys.Vec3(0.0, -0.2, 0.0));
                bola.snapTo(quem.getX() + this.getRandom().nextDouble() * 4.0 - 2.0,
                        quem.getY() + this.getRandom().nextInt(2) + 14.0,
                        quem.getZ() + this.getRandom().nextDouble() * 4.0 - 2.0,
                        0.0f, 0.0f);
                level.addFreshEntity(bola);
            }
        }
    }

    /**
     * <b>Metade das vezes ela ataca</b>, e do que ataca um terço é fogo e o resto é feitiço.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel level) || this.amiga()) return;
        if (!level.getRandom().nextBoolean()) return;

        this.entityData.set(PANCADA_NO_BRAÇO, BRAÇO);
        this.level().broadcastEntityEvent(this, (byte) 4);

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getBoundingBox().minY + alvo.getBbHeight() / 2.0
                - (this.getY() + this.getBbHeight() / 2.0);
        double dz = alvo.getZ() - this.getZ();
        float erro = net.minecraft.util.Mth.sqrt(força) * 0.5f;
        var rumo = new net.minecraft.world.phys.Vec3(
                dx + this.getRandom().nextGaussian() * erro, dy,
                dz + this.getRandom().nextGaussian() * erro);

        level.levelEvent(LevelEvent.SOUND_BLAZE_FIREBALL, this.blockPosition(), 0);
        if (level.getRandom().nextInt(3) == 0) {
            var bola = new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(level, this, rumo, 1);
            bola.snapTo(this.getX() + this.getLookAngle().x,
                    this.getY() + this.getBbHeight() / 2.0 + 0.5,
                    this.getZ() + this.getLookAngle().z, 0.0f, 0.0f);
            level.addFreshEntity(bola);
            return;
        }
        LilithSpellEntity.atira(level, this, rumo);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
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
        if (!this.level().isClientSide()) {
            this.barra.setProgress(this.getHealth() / this.getMaxHealth());
            this.barra.setVisible(!this.amiga());
        }
    }

    /**
     * <b>Nenhuma pancada lhe tira mais de doze</b> — e uma bola de fogo grande lhe tira a cura.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.amiga()) return false;
        if (fonte.getDirectEntity() instanceof net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball
                && fonte.getEntity() instanceof Player) {
            this.semCura = FRAQUEZA;
        }
        return super.hurtServer(level, fonte, Math.min(quanto, TETO_DA_PANCADA));
    }

    /**
     * <b>E ela não morre.</b>
     *
     * <p>Levando o golpe que a mataria, ela volta à vida cheia, perde o que a prendia, fica <b>amiga</b> e
     * <b>aparece ao lado</b> de quem a venceu — ou de quem estiver mais perto, se o vencedor se foi.
     *
     * <p>É a melhor ideia do mod inteiro: o chefe que não se mata, porque vencê-lo nunca foi o ponto.
     */
    @Override
    public void die(DamageSource fonte) {
        if (this.amiga() || !(this.level() instanceof ServerLevel level)) {
            super.die(fonte);
            return;
        }

        this.setHealth(this.getMaxHealth());
        this.entityData.set(AMIGA, true);
        this.setTarget(null);
        this.removeEffect(net.thaumcraft.occulta.OccultaEffects.CHILLED);
        this.removeEffect(MobEffects.WEAKNESS);
        this.semCura = 0;

        Player quem = quemAVenceu(level, fonte);
        if (quem == null) {
            this.discard();
            return;
        }
        this.snapTo(quem.getX() - 1.0 + this.getRandom().nextDouble() * 2.0, quem.getY() + 0.05,
                quem.getZ() - 1.0 + this.getRandom().nextDouble() * 2.0, this.getYRot(), this.getXRot());
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(), 64, 1.0, 2.0, 1.0, 0.2);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0f, 1.0f);
        quem.sendSystemMessage(Component.translatable("tc.lilith.beaten")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    /** Quem a venceu, ou quem estiver mais perto se o vencedor se foi. */
    @Nullable
    private Player quemAVenceu(ServerLevel level, DamageSource fonte) {
        if (fonte.getEntity() instanceof Player quem && quem.isAlive()
                && quem.level() == level && quem.distanceToSqr(this) <= 4096.0) {
            return quem;
        }
        Player perto = null;
        double quão = Double.MAX_VALUE;
        AABB roda = new AABB(this.getX() - 32.0, this.getY() - 16.0, this.getZ() - 32.0,
                this.getX() + 32.0, this.getY() + 16.0, this.getZ() + 32.0);
        for (Player quem : level.getEntitiesOfClass(Player.class, roda)) {
            double agora = this.distanceToSqr(quem);
            if (agora >= quão) continue;
            quão = agora;
            perto = quem;
        }
        return perto;
    }

    // ------------------------------------------------------------------ e o que ela dá

    @Override
    protected InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (!(this.level() instanceof ServerLevel level) || !this.amiga()) {
            return super.mobInteract(quem, mão);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.HOSTILE, 1.0f, 0.7f);

        ItemStack oquê = quem.getItemInHand(mão);
        boolean vaiSe = this.oferece(level, quem, mão, oquê);
        if (vaiSe) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(), 64, 1.0, 2.0, 1.0, 0.2);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 1.0f, 1.0f);
            this.discard();
        }
        return InteractionResult.SUCCESS;
    }

    /** O que ela faz com o que lhe trazem. */
    private boolean oferece(ServerLevel level, Player quem, InteractionHand mão, ItemStack oquê) {
        if (oquê.isEmpty()) {
            diz(quem, "nothing");
            return false;
        }

        if (oquê.getItem() instanceof GobletItem) {
            if (Vampire.é(quem)) {
                diz(quem, "alreadyvampire");
                return false;
            }
            quem.setItemInHand(mão, ItemStack.EMPTY);
            ItemStack cheio = new ItemStack(net.thaumcraft.occulta.OccultaItems.GOBLET);
            GobletItem.enche(cheio, GobletBlood.LILITH);
            quem.drop(cheio, false);
            Blood.põe(quem, 0);
            diz(quem, "life");
            return true;
        }

        if (oquê.is(net.thaumcraft.occulta.OccultaItems.GARLIC)) {
            if (!Vampire.é(quem)) {
                diz(quem, "curefail");
                return false;
            }
            quem.setItemInHand(mão, ItemStack.EMPTY);
            Vampire.grau(quem, 0);
            diz(quem, "cure");
            return true;
        }

        if (oquê.is(Items.POPPY)) {
            if (Vampire.grauDe(quem) != 6 || !Vampire.podeSubir(quem)) {
                diz(quem, "batflightfail");
                return false;
            }
            quem.setItemInHand(mão, ItemStack.EMPTY);
            Vampire.sobeUmGrau(quem);
            diz(quem, "batflight");
            return true;
        }

        return this.encanta(level, quem, mão, oquê);
    }

    /** E o que não é nada disso, ela encanta — como uma mesa de nível quarenta, e de graça. */
    private boolean encanta(ServerLevel level, Player quem, InteractionHand mão, ItemStack oquê) {
        ItemStack encantado = net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                this.getRandom(), oquê.copy(), ENCANTA_A, level.registryAccess(), java.util.Optional.empty());
        if (encantado.getEnchantments().isEmpty()) {
            diz(quem, "nothing");
            return false;
        }
        if (encantado.isDamageableItem()) encantado.setDamageValue(0);
        quem.setItemInHand(mão, ItemStack.EMPTY);
        quem.drop(encantado, false);
        diz(quem, "magic");
        return true;
    }

    private static void diz(Player quem, String oquê) {
        quem.sendSystemMessage(Component.translatable("tc.lilith." + oquê)
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    // ------------------------------------------------------------------ o resto

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance qual) {
        return qual.getEffect() != MobEffects.WITHER && super.canBeAffected(qual);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        // dela não cai nada: ela não morre
    }

    @Override
    public Component getName() {
        return this.hasCustomName() ? super.getName() : Component.translatable("entity.thaumcraft.lilith");
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.amiga() ? null : SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.WITCH_HURT;
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
        dados.putBoolean("Amiga", this.amiga());
        dados.putInt("SemCura", this.semCura);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.entityData.set(ESPERANDO, dados.getIntOr("Invul", 0));
        this.entityData.set(AMIGA, dados.getBooleanOr("Amiga", false));
        this.semCura = dados.getIntOr("SemCura", 0);
        if (this.hasCustomName()) this.barra.setName(this.getDisplayName());
    }

    /** Sem uso fora do porte: onde ela está, para quem precisar de a achar. */
    public BlockPos onde() {
        return this.blockPosition();
    }
}
