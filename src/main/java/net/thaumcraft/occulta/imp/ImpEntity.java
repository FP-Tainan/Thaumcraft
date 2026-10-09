package net.thaumcraft.occulta.imp;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.TaglockItem;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Diabrete</b>: o {@code EntityImp} do Witchery.
 *
 * <p>É o único bicho do mod que não se doma com comida nem se mata por despojo. Ele <b>negocia</b>, e
 * tudo nele é uma conta de paciência.
 *
 * <h2>Ele é de quem assinar</h2>
 *
 * <p>Não há comida que o amanse. O que o compra é um <b>Contrato de Posse assinado com o sangue de quem o
 * leva</b> — um taglock de si próprio preso ao papel — e ele cobra, além disso, <b>vinte e cinco níveis
 * de experiência</b>, que come na hora. Fechado o negócio, ele ganha um <b>nome de demônio</b> e o lugar
 * onde está vira a <b>casa</b> dele: daí em diante não se afasta mais de dezesseis blocos.
 *
 * <h2>E depois é preciso agradar-lhe</h2>
 *
 * <p>Ele tem uma conta de <b>afeição</b> que sobe com <b>coisas brilhantes</b> na mão de quem o tem — e
 * ela desce <b>um de cinco em cinco minutos</b>. Com afeição em zero e mais de uma hora de vida, há uma
 * chance em cem por volta de ele simplesmente <b>ir embora</b>: «o contrato está cumprido».
 *
 * <p>Com afeição de vinte para cima, três minutos desde o último presente e um sorteio que é <b>tanto
 * mais provável quanto mais ele gosta</b>, ele <b>retribui</b> — e a afeição volta a zero. Pela ordem:
 * o Cozimento de Alma da <b>Fome</b>, o do <b>Medo</b>, o da <b>Angústia</b>, um <b>Contrato do
 * Tormento</b>, e daí em diante um ingrediente ao acaso de sete.
 *
 * <p>Os três primeiros são o que destranca o Carnosa Diem, o Morsmordre e o Ignianima. Não há outra
 * maneira de os aprender, e é por isso que ele é a porta de um ramo inteiro do ofício: <b>três feitiços
 * atrás de um bicho que só dá o que quer, quando quer</b>.
 *
 * <h2>Ligar e desligar</h2>
 *
 * <p>Um <b>Coração de Demônio</b> liga-o por uma hora: ele <b>cresce</b>, bate pelo dobro, quase não se
 * magoa e solta chama. Uma <b>Agulha de Gelo</b> o desliga na hora — e ele pergunta porquê.
 *
 * <p>E, ligado, ele <b>não lança nada</b>: «há poder demais para pensar».
 */
public class ImpEntity extends TamableAnimal implements Enemy, Imps.HasHome {
    /** Os números dele. */
    public static final double VIDA = 50.0;
    public static final double VELOCIDADE = 0.3;

    /** Quanto ele bate, e quanto bate ligado. */
    public static final float MURRO = 4.0f;
    public static final float MURRO_LIGADO = 8.0f;

    /** E o teto do que lhe tiram por golpe, solto e ligado. */
    public static final float TETO = 15.0f;
    public static final float TETO_LIGADO = 5.0f;

    /** O tamanho dele, e o que ele fica quando liga. */
    public static final float LARGO = 0.4f;
    public static final float LARGO_LIGADO = 0.6f;

    /** Quanto o Coração de Demônio o liga: uma hora. */
    public static final int LIGADO_POR = 20 * 60 * 60;

    /** De quanto em quanto a afeição desce, e de quanto em quanto ele se cura. */
    public static final int ESQUECE_DE = 20 * 300;
    public static final int CURA_DE = 400;
    public static final int CURA_LIGADO_DE = 20;

    /** A afeição a partir da qual ele retribui, e quanto espera entre presentes. */
    public static final int GOSTA = 20;
    public static final int ENTRE_PRESENTES = 20 * 60 * 3;

    /** A sorte do presente: um em dez, e menos um por cada ponto de afeição acima de vinte. */
    public static final int SORTE = 10;

    /** O que o contrato custa em níveis, e de quanto é a casa dele. */
    public static final int CUSTA_NÍVEIS = 25;
    public static final double CASA = 16.0;

    /** A chance de ele ir embora, e a idade a partir da qual pode. */
    public static final int VAI_EMBORA = 100;
    public static final int DEPOIS_DE = 20 * 60 * 60;

    private static final EntityDataAccessor<Integer> AFEIÇÃO =
            SynchedEntityData.defineId(ImpEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LIGADO =
            SynchedEntityData.defineId(ImpEntity.class, EntityDataSerializers.BOOLEAN);

    private int segredos;
    private long últimoPresente;
    private long atéQuando;
    private BlockPos casa = BlockPos.ZERO;

    public ImpEntity(EntityType<? extends ImpEntity> tipo, Level level) {
        super(tipo, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(AFEIÇÃO, 0);
        construtor.define(LIGADO, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new Imps.WanderNearHome(this, 1.0, this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        /*
         * <b>Solto, ele caça gente.</b> Domado, não caça ninguém por si — só o que lhe mandarem, e é por
         * isso que a conta é esta e não a de sempre: o que ele pode atacar é o que ele <b>já</b> está
         * atacando.
         */
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 0, false,
                true, (quem, level) -> !this.isTame()));
    }

    // ------------------------------------------------------------------ o que ele é

    public int afeição() {
        return this.entityData.get(AFEIÇÃO);
    }

    public void afeição(int quanto) {
        this.entityData.set(AFEIÇÃO, Math.max(0, quanto));
    }

    public boolean ligado() {
        return this.entityData.get(LIGADO);
    }

    /** Sem uso fora do porte: serve à prova para o ligar sem lhe dar o Coração. */
    public void liga(ServerLevel level) {
        this.atéQuando = level.getGameTime() + LIGADO_POR;
        this.ligado(true);
    }

    /** E para o apagar sem lhe dar a Agulha. */
    public void desliga() {
        this.atéQuando = 0L;
        this.ligado(false);
    }

    private void ligado(boolean sim) {
        this.entityData.set(LIGADO, sim);
    }

    /** Quantos segredos ele já contou: é o que diz qual é o presente seguinte. */
    public int segredos() {
        return this.segredos;
    }

    @Override
    public BlockPos home() {
        return this.casa;
    }

    @Override
    public double homeRange() {
        return CASA;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /** <b>Nenhum golpe passa de quinze</b>, e de cinco quando ele está ligado. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        return super.hurtServer(level, fonte, Math.min(quanto, this.ligado() ? TETO_LIGADO : TETO));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        return noquê.hurtServer(level, this.damageSources().mobAttack(this),
                this.ligado() ? MURRO_LIGADO : MURRO);
    }

    // ------------------------------------------------------------------ a batida

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isRemoved()) return;

        /*
         * <b>Ele esquece.</b> De cinco em cinco minutos a afeição desce um — e, com ela em zero e mais de
         * uma hora de vida, há uma chance em cem por volta de ele ir embora. Quem o quer tem de continuar
         * a dar-lhe coisas; quem o deixa, perde-o.
         */
        if (this.tickCount % ESQUECE_DE == 0 && this.getOwnerReference() != null) {
            this.afeição(this.afeição() - 1);
            if (this.afeição() == 0 && this.tickCount > DEPOIS_DE
                    && this.random.nextInt(VAI_EMBORA) == 0) {
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(),
                        16, 0.5, 0.5, 0.5, 0.02);
                level.playSound(null, this.blockPosition(), OccultaSounds.IMP_LAUGH.value(),
                        SoundSource.HOSTILE, 0.5f, this.getVoicePitch());
                if (this.getOwner() instanceof Player dono) this.diz(dono, "goodbye");
                this.discard();
                return;
            }
        }

        if (this.atéQuando > 0L && level.getGameTime() >= this.atéQuando) {
            this.ligado(false);
            this.atéQuando = 0L;
        }

        if (this.tickCount % CURA_LIGADO_DE == 0 && this.ligado()) this.heal(1.0f);
        if (this.tickCount % CURA_DE == 0) this.heal(1.0f);
    }

    /** E a chama dele, do lado de cá, enquanto está ligado. */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() || !this.ligado()) return;
        double largura = this.getBbWidth();
        this.level().addParticle(ParticleTypes.FLAME,
                this.getX() - largura * 0.5 + this.random.nextDouble() * largura,
                this.getY() + 0.1 + this.random.nextDouble() * 2.0,
                this.getZ() - largura * 0.5 + this.random.nextDouble() * largura,
                0.0, 0.0, 0.0);
    }

    // ------------------------------------------------------------------ a mão nele

    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        ItemStack naMão = quem.getItemInHand(mão);
        if (naMão.isEmpty()) return InteractionResult.PASS;

        if (!this.isTame()) {
            return naMão.is(OccultaItems.CONTRACT) ? this.oContrato(level, quem, naMão)
                    : InteractionResult.PASS;
        }

        if (naMão.is(OccultaItems.DEMON_HEART)) return this.liga(level, quem, naMão);
        if (naMão.is(OccultaItems.ICY_NEEDLE)) return this.desliga(level, quem, naMão);
        if (net.thaumcraft.occulta.demon.ContractItem.preso(naMão)) {
            return this.lança(level, quem, naMão);
        }
        return this.oBrilho(level, quem, naMão);
    }

    /**
     * <b>O que ele lança.</b> Um contrato preso a alguém, posto na mão dele, é lido e cumprido — na pessoa
     * do outro lado do papel, esteja ela onde estiver.
     *
     * <p>E ele recusa por quatro motivos, que são os quatro do original e os quatro têm recado próprio:
     *
     * <ul>
     *   <li><b>ligado</b>, não lê nada: «há poder demais para pensar». É a única coisa que o Coração de
     *       Demônio <b>tira</b>, e é por isso que ligá-lo não é só ganho;</li>
     *   <li><b>com pouca afeição</b> — menos de vinte —, pergunta por que haveria de o fazer;</li>
     *   <li><b>há pouco tempo</b> — os mesmos três minutos dos presentes —, manda esperar;</li>
     *   <li>e <b>não achando</b> a pessoa do outro lado, diz o nome dela e não faz nada.</li>
     * </ul>
     *
     * <p>Um contrato que não pegue <b>não se gasta</b>: é o {@code activate} devolvendo falso.
     */
    private InteractionResult lança(ServerLevel level, Player quem, ItemStack papel) {
        this.ri(level, quem);
        if (this.ligado()) {
            this.diz(quem, "spell.toomuchpower");
            return InteractionResult.SUCCESS;
        }
        if (this.afeição() < GOSTA) {
            this.diz(quem, "spell.notliked");
            return InteractionResult.SUCCESS;
        }
        long agora = level.getGameTime();
        if (agora <= this.últimoPresente + ENTRE_PRESENTES && !quem.hasInfiniteMaterials()) {
            this.diz(quem, "spell.toooften");
            return InteractionResult.SUCCESS;
        }

        var alvo = net.thaumcraft.occulta.Voodoo.bound(level, papel);
        if (alvo == null) {
            var vínculo = TaglockItem.bound(papel);
            this.dizDe(quem, "spell.cannotfind", vínculo == null ? "?" : vínculo.name());
            return InteractionResult.SUCCESS;
        }
        if (!(papel.getItem() instanceof net.thaumcraft.occulta.demon.ContractItem contrato)) {
            return InteractionResult.PASS;
        }

        if (!contrato.faz(level, alvo)) {
            this.dizDe(quem, "spell.failed", alvo.getName().getString());
            return InteractionResult.SUCCESS;
        }

        this.últimoPresente = agora;
        this.dizDe(quem, "spell.feelthefire", alvo.getName().getString());
        if (!quem.hasInfiniteMaterials()) papel.shrink(1);
        return InteractionResult.SUCCESS;
    }

    /** <b>O contrato</b>: assinado por quem o traz, e com vinte e cinco níveis para dar. */
    private InteractionResult oContrato(ServerLevel level, Player quem, ItemStack papel) {
        this.ri(level, quem);
        var vínculo = TaglockItem.bound(papel);
        if (vínculo == null) {
            this.diz(quem, "contract.unsigned");
            return InteractionResult.SUCCESS;
        }
        if (!vínculo.owner().equals(quem.getUUID())) {
            this.diz(quem, "contract.notowners");
            return InteractionResult.SUCCESS;
        }
        if (quem.experienceLevel < CUSTA_NÍVEIS && !quem.hasInfiniteMaterials()) {
            this.diz(quem, "contract.noxp");
            return InteractionResult.SUCCESS;
        }

        if (!quem.hasInfiniteMaterials()) {
            papel.shrink(1);
            quem.giveExperienceLevels(-CUSTA_NÍVEIS);
        }
        this.tame(quem);
        this.setTarget(null);
        this.getNavigation().stop();
        this.casa = this.blockPosition();
        this.setPersistenceRequired();
        this.setCustomName(Component.literal(Imps.nome(this.random)));
        this.diz(quem, "contract.deal");
        return InteractionResult.SUCCESS;
    }

    /** O <b>Coração de Demônio</b>: uma hora de poder. */
    private InteractionResult liga(ServerLevel level, Player quem, ItemStack coração) {
        if (!quem.hasInfiniteMaterials()) coração.shrink(1);
        this.atéQuando = level.getGameTime() + LIGADO_POR;
        this.ligado(true);
        this.ri(level, quem);
        this.diz(quem, "gift.power");
        return InteractionResult.SUCCESS;
    }

    /** E a <b>Agulha de Gelo</b>, que o apaga. */
    private InteractionResult desliga(ServerLevel level, Player quem, ItemStack agulha) {
        if (!quem.hasInfiniteMaterials()) agulha.shrink(1);
        this.atéQuando = 0L;
        this.ligado(false);
        this.ri(level, quem);
        this.diz(quem, "gift.powerloss");
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>As coisas brilhantes.</b>
     *
     * <p>Cada uma vale a sua afeição, e o que ele faz com ela é a conta que o torna um bicho e não uma
     * máquina: com vinte ou mais, três minutos de espera e um sorteio de um em
     * {@code max(1, 10 − (afeição − 20))}, ele retribui — e quanto mais gosta, mais depressa.
     */
    private InteractionResult oBrilho(ServerLevel level, Player quem, ItemStack coisa) {
        Integer quanto = Imps.brilho(coisa.getItem());
        if (quanto == null) {
            this.ri(level, quem);
            this.diz(quem, "gift.hate");
            return InteractionResult.SUCCESS;
        }
        if (coisa.isDamaged()) {
            this.ri(level, quem);
            this.diz(quem, "gift.hate");
            return InteractionResult.SUCCESS;
        }

        if (!quem.hasInfiniteMaterials()) coisa.shrink(1);
        this.ri(level, quem);

        int afeição = this.afeição() + quanto;
        long agora = level.getGameTime();
        boolean naHora = agora > this.últimoPresente + ENTRE_PRESENTES || quem.hasInfiniteMaterials();
        boolean sorte = this.random.nextInt(Math.max(1, SORTE - Math.max(afeição - GOSTA, 0))) == 0;

        if (afeição >= GOSTA && naHora && sorte) {
            this.últimoPresente = agora;
            this.afeição(0);
            this.diz(quem, "gift.reciprocate");
            this.retribui(level);
            return InteractionResult.SUCCESS;
        }

        this.afeição(afeição);
        this.diz(quem, naHora ? "gift.like" : "gift.toomany");
        return InteractionResult.SUCCESS;
    }

    /** O presente, pela ordem do original — e, depois dos quatro, um ingrediente ao acaso. */
    private void retribui(ServerLevel level) {
        Item oquê;
        if (this.segredos < Imps.SEGREDOS.length) {
            oquê = Imps.SEGREDOS[this.segredos];
            this.segredos++;
        } else {
            oquê = Imps.SOBRAS[this.random.nextInt(Imps.SOBRAS.length)];
        }
        int quantos = Imps.quantos(oquê);

        level.sendParticles(SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT,
                        1.0f, 1.0f, 1.0f, 1.0f),
                this.getX(), this.getY() + 1.0, this.getZ(), 16, 1.0, 1.0, 1.0, 0.0);
        level.playSound(null, this.blockPosition(),
                net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.HOSTILE,
                1.0f, 1.0f);
        level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(),
                new ItemStack(oquê, quantos)));
    }

    private void ri(ServerLevel level, Player quem) {
        level.playSound(null, quem.blockPosition(), OccultaSounds.IMP_LAUGH.value(),
                SoundSource.HOSTILE, 0.5f, this.getVoicePitch());
    }

    /** E ele fala sempre do mesmo jeito: o nome dele entre sinais, e depois a fala. */
    private void diz(Player quem, String oquê) {
        quem.sendSystemMessage(Component.translatable("tc.occulta.imp." + oquê,
                this.getName()).withStyle(ChatFormatting.DARK_RED));
    }

    /** E o mesmo, com o nome de quem está do outro lado do papel. */
    private void dizDe(Player quem, String oquê, String deQuem) {
        quem.sendSystemMessage(Component.translatable("tc.occulta.imp." + oquê,
                this.getName(), deQuem).withStyle(ChatFormatting.DARK_RED));
    }

    // ------------------------------------------------------------------ os sons

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return OccultaSounds.IMP_LAUGH.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.IMP_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.IMP_DEATH.value();
    }

    /** Ligado, ele fala mais grave. */
    @Override
    public float getVoicePitch() {
        return this.ligado()
                ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 0.7f
                : (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.1f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 20 * 40;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** <b>Não procria.</b> */
    @Override
    public boolean canMate(Animal outro) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob par) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack oquê) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Affection", this.afeição());
        saída.putInt("SecretsShared", this.segredos);
        saída.putLong("LastGiftTime", this.últimoPresente);
        saída.putLong("PowerUpUntil", this.atéQuando);
        saída.store("Home", BlockPos.CODEC, this.casa);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.afeição(entrada.getIntOr("Affection", 0));
        this.segredos = entrada.getIntOr("SecretsShared", 0);
        this.últimoPresente = entrada.getLongOr("LastGiftTime", 0L);
        this.atéQuando = entrada.getLongOr("PowerUpUntil", 0L);
        this.casa = entrada.read("Home", BlockPos.CODEC).orElse(this.blockPosition());
        if (this.level() != null && this.atéQuando > this.level().getGameTime()) this.ligado(true);
    }

    /** O tamanho dele muda com o poder: ele fica <b>gordo</b>, e não alto. */
    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(
            net.minecraft.world.entity.Pose pose) {
        var tamanho = super.getDefaultDimensions(pose);
        return this.ligado() ? tamanho.scale(LARGO_LIGADO / LARGO, 1.0f) : tamanho;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> qual) {
        if (LIGADO.equals(qual)) this.refreshDimensions();
        super.onSyncedDataUpdated(qual);
    }
}
