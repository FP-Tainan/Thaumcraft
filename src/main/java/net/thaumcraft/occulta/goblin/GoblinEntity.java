package net.thaumcraft.occulta.goblin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>goblin</b>: o {@code EntityGoblin} do Witchery.
 *
 * <p>Ele é a coisa mais estranha do mod, e a melhor: <b>um bicho que trabalha</b>. Não é domado, não é
 * invocado, não obedece — é <b>preso por uma corda</b>, e um goblin na corda faz três coisas que nenhum outro
 * bicho do jogo faz:
 *
 * <ol>
 *   <li><b>apanha o que está no chão</b> e o carrega;</li>
 *   <li><b>cava</b>, se lhe puserem uma picareta na mão;</li>
 *   <li>e <b>larga o que cavou</b> onde houver onde largar.</li>
 * </ol>
 *
 * <p>Solto, ele não faz nada disso: anda pela aldeia, entra em casa à noite, abre portas, e <b>comercia</b>.
 * É um aldeão de outra espécie — e <b>odeia os aldeões</b>, mas só quando há <b>três deles juntos</b>: um
 * goblin sozinho tem medo de tudo, e três têm coragem. É a mesma conta que decide se ele foge de gente.
 *
 * <p><b>Ele trepa paredes</b>, como uma aranha, e é por isso que uma cerca não o segura.
 *
 * <p><b>E ele é a única fonte de koboldite que há.</b> O metal não se mina e não se cozinha: sai de um
 * goblin, numa escada de três degraus que ele abre um de cada vez. Veja o {@link GoblinTrades}.
 *
 * <p><b>Fica de fora, declarado:</b> a <b>picareta de koboldite</b> e o que ela faz ao que se cava — ela
 * cavaria quinze vezes mais depressa e fundiria metade do minério. Os dois <b>chefes</b> goblins (o Gulg e
 * o Mog) pendem da Estátua de Adoração, e ficam com ela.
 */
public class GoblinEntity extends AgeableMob implements Merchant {
    /** Quantos goblins juntos bastam para eles terem coragem: os três do original. */
    public static final int CORAGEM = 3;

    /** E a que distância eles se contam. */
    public static final double PERTO = 8.0;

    /** Os quatro ofícios, que são só a cara dele. */
    public static final int OFÍCIOS = 4;

    /** O que ele está fazendo: nada, trabalhar ou adorar. */
    public static final byte PARADO = 0;
    public static final byte TRABALHANDO = 1;
    public static final byte ADORANDO = 2;

    private static final EntityDataAccessor<Integer> OFÍCIO =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> FAZENDO =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> NA_PAREDE =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BOOLEAN);

    /** Quanto tempo ele leva a pôr uma troca nova depois de se lhe esgotar a última. */
    public static final int RECARGA = 40;

    /** E de quantas em quantas batidas ele procura a aldeia dele, com o intervalo do original. */
    public static final int PROCURA = 70;
    public static final int MAIS_UM_BOCADO = 50;

    /** A quantas seções de aldeia ele tem de estar para regatear: as duas que valem os trinta e dois
     * blocos do original. */
    public static final int PERTO_DA_ALDEIA = 2;

    /** Quanto dura a regeneração que ele ganha ao recarregar a loja. */
    public static final int DESCANSO = 200;

    private @Nullable Player quemCompra;
    private @Nullable MerchantOffers ofertas;
    private int atéRecarregar;
    private boolean precisaDeMais;
    private int riqueza;
    private int atéProcurar;
    private boolean semSumir;
    private GoblinWorshipGoal adoração;

    public GoblinEntity(EntityType<? extends GoblinEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_OPEN, 0.0f);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_WOOD_CLOSED, 0.0f);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OFÍCIO, 0);
        builder.define(FAZENDO, PARADO);
        builder.define(NA_PAREDE, false);
    }

    /**
     * A mira do original, pela ordem dele.
     *
     * <p>A casa <b>dois</b> é a do trabalho — apanhar, largar e cavar —, e as três vontades dela só valem com
     * o goblin <b>na corda</b>. A casa três é a do medo, e ela <b>se desliga quando são três</b>.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, this.adoração = new GoblinWorshipGoal(this));
        this.goalSelector.addGoal(2, new GoblinPickUpGoal(this, GoblinPickUpGoal.ALCANCE));
        this.goalSelector.addGoal(2, new GoblinDropOffGoal(this, GoblinDropOffGoal.ALCANCE));
        this.goalSelector.addGoal(2, new GoblinDigGoal(this, GoblinDigGoal.ALCANCE));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class,
                8.0f, 0.6, 0.6, quem -> !this.temCoragem()));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this,
                net.thaumcraft.occulta.village.VillageGuardEntity.class,
                12.0f, 0.8, 0.8, quem -> !this.temCoragem()));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, GoblinEntity.class, 5.0f, 0.02f));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 0,
                true, true, (quem, level) -> this.temCoragem()));
    }

    // ------------------------------------------------------------------ a conta da coragem

    /**
     * Se há <b>três goblins</b> a oito blocos: o {@code shouldAvoid} e o {@code isEntityApplicable} do
     * original, que são a mesma conta vista dos dois lados.
     *
     * <p>Sozinho, ele <b>foge</b> de gente e de guarda. Em três, ele <b>caça aldeão</b>. É a única coisa que
     * decide o que um goblin é, e é por isso que eles andam sempre em grupo.
     */
    public boolean temCoragem() {
        return this.level().getEntitiesOfClass(GoblinEntity.class,
                this.getBoundingBox().inflate(PERTO)).size() >= CORAGEM;
    }

    // ------------------------------------------------------------------ o que ele é e o que ele faz

    public int ofício() {
        return this.entityData.get(OFÍCIO);
    }

    public void ofício(int qual) {
        this.entityData.set(OFÍCIO, qual);
    }

    /** O que ele está fazendo: nada, trabalhar ou adorar. */
    public byte fazendo() {
        return this.entityData.get(FAZENDO);
    }

    public boolean trabalhando() {
        return this.fazendo() == TRABALHANDO;
    }

    public void trabalhando(boolean sim) {
        if (sim == this.trabalhando()) return;
        this.entityData.set(FAZENDO, sim ? TRABALHANDO : PARADO);
    }

    public boolean adorando() {
        return this.fazendo() == ADORANDO;
    }

    /**
     * <b>O chamado da Estátua de Adoração.</b>
     *
     * <p>Ela manda; ele atende <b>duas vezes em três</b>. Ver o {@link GoblinWorshipGoal}.
     */
    public void começaAAdorar(net.minecraft.core.BlockPos daqui) {
        if (this.adoração != null) this.adoração.chama(daqui);
    }

    public void adorando(boolean sim) {
        if (sim == this.adorando()) return;
        this.entityData.set(FAZENDO, sim ? ADORANDO : PARADO);
    }

    /** <b>Ele trepa paredes</b>: é o mesmo laço da aranha, e por isso uma cerca não o segura. */
    @Override
    public boolean onClimbable() {
        return this.entityData.get(NA_PAREDE);
    }

    @Override
    public void tick() {
        super.tick();
        this.olhaAParede();
    }

    /**
     * Olha se ele está encostado a alguma coisa, e é isso que o faz trepar.
     *
     * <p>Corre <b>depois</b> de o bicho se mexer, como no original: a batida do jogo é que diz se ele bateu
     * numa parede, e só depois disso a resposta vale.
     */
    public void olhaAParede() {
        if (this.level().isClientSide()) return;
        this.entityData.set(NA_PAREDE, this.horizontalCollision);
    }

    /**
     * O gesto do original: <b>com ele na corda</b>, a mão troca de dono.
     *
     * <p>De mãos vazias, uma picareta que se lhe dê fica com ele — e ele põe-se a cavar. Com alguma coisa na
     * mão, clicar <b>tira-lha</b> e devolve-a a quem clicou. É assim que se manda num goblin: não há ordem, há
     * uma picareta.
     */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        if (naMão.is(Items.LEAD)) return super.mobInteract(quem, mão);
        if (!this.isAlive() || this.isBaby() || quem.isShiftKeyDown()) {
            return super.mobInteract(quem, mão);
        }

        /*
         * <b>Solto, ele regateia.</b> Na corda, ele trabalha — e as duas coisas não se misturam: um goblin
         * com picareta na mão é um empregado, e um empregado não vende nada.
         */
        if (!this.isLeashed()) {
            if (this.trabalhando() || this.adorando()
                    || naMão.getItem() instanceof net.minecraft.world.item.SpawnEggItem) {
                return super.mobInteract(quem, mão);
            }
            if (this.level().isClientSide()) return InteractionResult.SUCCESS;
            if (!emAldeia()) return super.mobInteract(quem, mão);
            this.setTradingPlayer(quem);
            this.openTradingScreen(quem, this.getDisplayName(), 0);
            return InteractionResult.SUCCESS;
        }
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        ItemStack dele = this.getMainHandItem();
        if (dele.isEmpty()) {
            if (!naMão.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                return InteractionResult.PASS;
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, naMão.copy());
            this.setDropChance(EquipmentSlot.MAINHAND, 2.0f);
            naMão.setCount(0);
            return InteractionResult.SUCCESS;
        }

        if (!quem.getInventory().add(dele.copy())) quem.drop(dele.copy(), false);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ o regateio

    /**
     * <b>Se ele está em casa.</b>
     *
     * <p>O original pergunta à coleção de aldeias de 2014 se há uma a <b>trinta e dois blocos</b>. Essa
     * coleção não existe mais, e o que hoje lhe corresponde é a conta de pontos de interesse do jogo —
     * camas, bancadas, postos de trabalho — que o {@code isCloseToVillage} faz por seções de dezesseis
     * blocos. Duas seções são os trinta e dois do original.
     *
     * <p>Vale a pena dizer por que a pergunta existe: um goblin <b>no mato não vende nada</b>. Ele só
     * regateia onde mora, e é por isso que encontrar uma aldeia com goblins é o começo da linha do
     * koboldite.
     */
    public boolean emAldeia() {
        return this.level() instanceof ServerLevel level
                && level.isCloseToVillage(this.blockPosition(), PERTO_DA_ALDEIA);
    }

    @Override
    public void setTradingPlayer(@Nullable Player quem) {
        this.quemCompra = quem;
    }

    @Override
    public @Nullable Player getTradingPlayer() {
        return this.quemCompra;
    }

    public boolean regateando() {
        return this.quemCompra != null;
    }

    /**
     * <b>Ele não abre a loja toda de uma vez.</b>
     *
     * <p>Um goblin que nunca regateou tem <b>uma</b> troca, e só. As outras vêm uma a uma, à medida que a
     * última se lhe esgota — é o costume do aldeão de 2014, e no goblin ele tem um efeito que no aldeão
     * não tinha: a escada do koboldite <b>é</b> essa fila, e por isso ela se sobe degrau a degrau.
     */
    @Override
    public MerchantOffers getOffers() {
        if (this.ofertas == null) {
            this.ofertas = new MerchantOffers();
            GoblinTrades.monta(this.getRandom(), this.ofício(), this.ofertas, 1);
        }
        return this.ofertas;
    }

    @Override
    public void overrideOffers(MerchantOffers quais) {
        this.ofertas = quais;
    }

    /** E uma troca feita começa a conta da seguinte. */
    @Override
    public void notifyTrade(MerchantOffer qual) {
        qual.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(net.thaumcraft.occulta.OccultaSounds.GOBLIN_YES.value(),
                this.getSoundVolume(), this.getVoicePitch());

        MerchantOffers tem = this.getOffers();
        if (!tem.isEmpty() && qual == tem.get(tem.size() - 1)) {
            this.atéRecarregar = RECARGA;
            this.precisaDeMais = true;
        }
        if (qual.getCostA().is(net.minecraft.world.item.Items.EMERALD)) {
            this.riqueza += qual.getCostA().getCount();
        }
    }

    /** E ele responde a quem lhe põe coisas no balcão: sim se servem, não se não servem. */
    @Override
    public void notifyTradeUpdated(ItemStack oquê) {
        if (this.level().isClientSide()) return;
        if (this.ambientSoundTime <= -this.getAmbientSoundInterval() + 20) return;
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(oquê.isEmpty()
                        ? net.thaumcraft.occulta.OccultaSounds.GOBLIN_NO.value()
                        : net.thaumcraft.occulta.OccultaSounds.GOBLIN_YES.value(),
                this.getSoundVolume(), this.getVoicePitch());
    }

    /**
     * <b>Nenhuma experiência.</b>
     *
     * <p>O aldeão do original larga esferas a quem regateia com ele; o goblin <b>não</b> — o
     * {@code useRecipe} dele não as larga, e é de propósito. Com ele não se sobe de nível: a barra de
     * progresso também não aparece.
     */
    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int quanta) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_YES.value();
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    /**
     * <b>O balcão fecha quando ele morre, e não quando quem compra se afasta.</b>
     *
     * <p>É o que o original faz — o balcão dele só pergunta se quem está do outro lado é o mesmo —, e é
     * também o que o aldeão de hoje faz. Um limite de distância aqui fecharia a loja na cara de quem deu
     * um passo atrás.
     */
    @Override
    public boolean stillValid(Player quem) {
        return this.quemCompra == quem && this.isAlive();
    }

    /** Quanta esmeralda já passou por ele, que é o {@code Riches} do original. */
    public int riqueza() {
        return this.riqueza;
    }

    /**
     * A batida da aldeia e a da loja: o {@code updateAITick} do original.
     *
     * <p>De setenta em setenta batidas, mais um bocado, ele procura a aldeia — e, achando-a, <b>deixa de
     * sumir</b> e passa a ter casa. E, esgotada a última troca, quarenta batidas depois ele põe mais uma
     * e ganha regeneração por dez segundos, que é o original a dizer que regatear cansa.
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        if (--this.atéProcurar <= 0) {
            this.atéProcurar = PROCURA + this.getRandom().nextInt(MAIS_UM_BOCADO);
            if (this.emAldeia()) this.semSumir = true;
        }

        if (this.regateando() || this.atéRecarregar <= 0) return;
        if (--this.atéRecarregar > 0) return;
        if (this.precisaDeMais) {
            GoblinTrades.monta(this.getRandom(), this.ofício(), this.getOffers(), 1);
            this.precisaDeMais = false;
        }
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.REGENERATION, DESCANSO, 0));
    }

    /** Um goblin que já chegou a uma aldeia <b>não some mais</b>, e um que adora também não. */
    @Override
    public boolean removeWhenFarAway(double longe) {
        return !this.semSumir && !this.adorando();
    }

    // ------------------------------------------------------------------ o resto

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob outro) {
        var filho = net.thaumcraft.occulta.OccultaEntities.GOBLIN.create(level,
                net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (filho != null) filho.ofício(this.getRandom().nextInt(OFÍCIOS));
        return filho;
    }

    @Override
    public @Nullable net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance quão,
            net.minecraft.world.entity.EntitySpawnReason razão,
            @Nullable net.minecraft.world.entity.SpawnGroupData dado) {
        var saída = super.finalizeSpawn(level, quão, razão, dado);
        this.ofício(level.getRandom().nextInt(OFÍCIOS));
        return saída;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_IDLE.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.thaumcraft.occulta.OccultaSounds.GOBLIN_DEATH.value();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.4f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Profession", this.ofício());
        saída.putInt("Riches", this.riqueza);
        saída.putBoolean("Settled", this.semSumir);
        if (this.ofertas != null && !this.ofertas.isEmpty()) {
            saída.store("Offers", MerchantOffers.CODEC, this.ofertas);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.ofício(entrada.getIntOr("Profession", 0));
        this.riqueza = entrada.getIntOr("Riches", 0);
        this.semSumir = entrada.getBooleanOr("Settled", false);
        this.ofertas = entrada.read("Offers", MerchantOffers.CODEC).orElse(null);
    }

    /** Quantos goblins há à volta daquele ponto, para quem precisar de contar sem ser um deles. */
    public static int quantosPerto(Level level, AABB onde) {
        return level.getEntitiesOfClass(GoblinEntity.class, onde).size();
    }
}
