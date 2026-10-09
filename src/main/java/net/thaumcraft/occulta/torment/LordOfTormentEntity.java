package net.thaumcraft.occulta.torment;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.spirit.FlyerGoals;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * O <b>Senhor do Tormento</b>: a {@code EntityLordOfTorment} do Witchery.
 *
 * <p>Quinhentos de vida, cinquenta de couraça, imune a fogo — e <b>nenhum golpe lhe tira mais de cinco</b>,
 * ou <b>oito</b> se for demoníaco. Quer dizer: cem golpes, no mínimo, e há uma única maneira de baixar essa
 * conta para sessenta e três, que é bater-lhe com o que é do inferno.
 *
 * <h2>A fuga, que é o que ele é</h2>
 *
 * <p>Ele não luta até morrer. Chegado a <b>metade da vida</b>, <b>fora do Tormento</b>, ele some — e leva
 * consigo toda gente que o feriu e toda gente que estiver a dezesseis de lado e trinta e dois de alto. Lá
 * embaixo, no andar sorteado, <b>há outro</b>, com metade da vida, à espera na sala do meio.
 *
 * <p>É a melhor briga do mod e é por isto: a primeira metade da vida dele é a conta de quem o chamou; a
 * segunda é a conta de quem souber atravessar um labirinto invisível no escuro, com ele atrás.
 *
 * <h2>O que ele faz</h2>
 *
 * <p>De perto, o murro: sete mais até vinte e um, e quem apanha é <b>erguido</b>. De longe, ele atira um
 * <b>Ignianima</b> e <b>três bolas de fogo de alma</b> — <b>nove</b>, uma vez em dez. E, se o alvo estiver
 * <b>voar</b>, uma vez em vinte ele lhe dá <b>lentidão seis por dez segundos</b>, que é o original dizendo
 * que daqui não se foge pelo ar.
 *
 * <p>O que ele larga: <b>dois livros encantados</b>, um <b>Coração de Demônio</b> e o <b>Cozimento de Alma
 * do Tormento</b> — que é a única fonte dele, e o que destranca o Tormentum.
 */
public class LordOfTormentEntity extends Monster implements FlyerGoals.Atirador {
    /** A vida, o passo, o murro e a couraça: os quinhentos, os dois décimos, os oito e os cinquenta. */
    public static final double VIDA = 500.0;
    public static final double VELOCIDADE = 0.2;
    public static final double MURRO = 8.0;
    public static final double COURAÇA = 50.0;

    /** A experiência que ele deixa. */
    public static final int VALE = 80;

    /** Nenhum golpe lhe tira mais do que isto — e os demoníacos tiram um pouco mais. */
    public static final float TETO = 5.0f;
    public static final float TETO_DEMONÍACO = 8.0f;

    /** O murro: sete mais até quinze, e o quanto ele ergue quem apanha. */
    public static final int MURRO_DE = 7;
    public static final int MURRO_ATÉ = 15;
    public static final double ERGUE = 0.4;

    /** Quanto tempo o braço fica levantado depois de bater. */
    public static final int BRAÇO = 10;

    /** Uma em vinte, lentidão seis por dez segundos, em quem estiver voando. */
    public static final int VOANDO_UMA_EM = 20;
    public static final int LENTIDÃO = 200;
    public static final int LENTIDÃO_GRAU = 5;

    /** As bolas de fogo: três, e nove uma vez em dez. */
    public static final int BOLAS = 3;
    public static final int BOLAS_DEMAIS = 9;
    public static final int MUITAS_UMA_EM = 10;

    /** Os números da meta de atirar: o prazo de recarga e o alcance. */
    public static final int RECARGA_DE = 20;
    public static final int RECARGA_ATÉ = 60;
    public static final float ALCANCE = 12.0f;

    /** A metade da vida em que ele foge, e o que o puxão alcança. */
    public static final float METADE = 0.5f;
    public static final double PUXÃO = 16.0;
    public static final double PUXÃO_ALTO = 32.0;

    /** O número do Ignianima, que é o feitiço que ele atira junto com as bolas. */
    public static final int IGNIANIMA = 39;

    /** De quanto em quanto ele ri: dez segundos. */
    public static final int RI_DE = 200;

    /** O que ele larga de livros, e o grau deles. */
    public static final int LIVROS = 2;
    public static final int GRAU_A_MAIS = 3;
    public static final int GRAU_A_MAIS_DO_SEGUNDO = 1;

    /** Que o braço está levantado, para o desenho. */
    private static final EntityDataAccessor<Boolean> BATENDO =
            SynchedEntityData.defineId(LordOfTormentEntity.class, EntityDataSerializers.BOOLEAN);

    /** O aviso de que ele bateu, para o cliente levantar o braço. */
    public static final byte BATEU = 4;

    private int braço;

    /** Quem lhe bateu, que é quem ele leva consigo quando foge. */
    private final Set<UUID> quemBateu = new LinkedHashSet<>();

    public LordOfTormentEntity(EntityType<? extends LordOfTormentEntity> tipo, Level mundo) {
        super(tipo, mundo);
        this.xpReward = VALE;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO)
                .add(Attributes.ARMOR, COURAÇA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(2, new FlyerGoals.Atira(this, RECARGA_DE, RECARGA_ATÉ, ALCANCE));
        /*
         * <b>Desvio declarado:</b> a lista de metas do original não tem nenhuma de bater de perto — e no
         * entanto o murro dele existe e acontece. Em 2014 o {@code EntityCreature} ainda corria a <b>via
         * velha</b> por baixo das metas, e era ela que chamava o {@code attackEntity} quando o alvo estava
         * a menos de dois. Hoje essa via não existe: ou há uma meta, ou o murro é código morto. Fica a
         * meta de bater de perto do jogo, que é o que a via velha fazia.
         */
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new FlyerGoals.Pousa(this, false));
        this.goalSelector.addGoal(6, new FlyerGoals.Vagueia(this, 10.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(BATENDO, false);
    }

    /** Ele voa: a gravidade não lhe pega, como em todo {@code EntityFlyingMob} do original. */
    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double quanto, float vezes, DamageSource fonte) {
        return false;
    }

    /** Ele se vê no escuro como em pleno dia: o {@code getBrightness} devolvendo um. */
    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    // ------------------------------------------------------------------ a batida

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // a chama que ele solta, uma por batida
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() - this.getBbWidth() * 0.5 + this.random.nextDouble() * this.getBbWidth(),
                    0.1 + this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ() - this.getBbWidth() * 0.5 + this.random.nextDouble() * this.getBbWidth(),
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.braço > 0) this.braço--;
        if (this.level().isClientSide()) return;
        this.entityData.set(BATENDO, this.braço > 0);

        /*
         * E o castigo de quem foge pelo ar: uma vez em vinte, lentidão seis por dez segundos. O original
         * só a dá a quem estiver <b>mesmo voando</b> — a quem tem a asa aberta, não a quem salta.
         */
        if (!(this.random.nextDouble() < 1.0 / VOANDO_UMA_EM)) return;
        if (!(this.getTarget() instanceof Player alvo)) return;
        if (!alvo.getAbilities().flying && !alvo.isFallFlying()) return;
        if (alvo.hasEffect(MobEffects.SLOWNESS)) return;
        alvo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, LENTIDÃO, LENTIDÃO_GRAU));
    }

    /** O murro: sete mais até quinze, e quem apanha sobe. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity alvo) {
        this.braço = BRAÇO;
        level.broadcastEntityEvent(this, BATEU);
        boolean pegou = alvo.hurtServer(level, this.damageSources().mobAttack(this),
                MURRO_DE + this.random.nextInt(MURRO_ATÉ));
        if (pegou) alvo.setDeltaMovement(alvo.getDeltaMovement().add(0.0, ERGUE, 0.0));
        this.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return pegou;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual != BATEU) {
            super.handleEntityEvent(qual);
            return;
        }
        this.braço = BRAÇO;
        this.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
    }

    /** Sem uso fora do desenho: quantas batidas falta do braço levantado. */
    public int oBraço() {
        return this.entityData.get(BATENDO) ? BRAÇO : 0;
    }

    // ------------------------------------------------------------------ o que ele atira

    /**
     * O tiro: um <b>Ignianima</b> e três bolas de fogo de alma — nove, uma vez em dez.
     *
     * <p>A dispersão é a do original: cada bola sai com um empurrão gaussiano vezes a raiz da distância
     * sobre dois, de modo que de longe ele erra mais e de perto acerta tudo.
     */
    @Override
    public void atira(ServerLevel level, LivingEntity alvo, float quão) {
        this.braço = BRAÇO;
        level.broadcastEntityEvent(this, BATEU);

        double dx = alvo.getX() - this.getX();
        double dy = alvo.getBoundingBox().minY + alvo.getBbHeight() / 2.0f - (this.getY() + this.getBbHeight() / 2.0f);
        double dz = alvo.getZ() - this.getZ();
        float espalha = net.minecraft.util.Mth.sqrt(quão) * 0.5f;

        // o Ignianima, que é o feitiço trinta e nove do original
        var ignianima = net.thaumcraft.occulta.symbol.Symbols.daquele(IGNIANIMA);
        if (ignianima != null) {
            var fogo = new net.thaumcraft.occulta.symbol.SpellEffectEntity(level, this,
                    new Vec3(dx, dy, dz), ignianima, 1);
            fogo.setPos(this.getX(), this.getY() + this.getBbHeight() / 2.0f, this.getZ());
            level.addFreshEntity(fogo);
        }

        int quantas = this.random.nextInt(MUITAS_UMA_EM) == 0 ? BOLAS_DEMAIS : BOLAS;
        for (int volta = 0; volta < quantas; volta++) {
            Vec3 rumo = new Vec3(dx + this.random.nextGaussian() * espalha, dy,
                    dz + this.random.nextGaussian() * espalha);
            var bola = new SoulfireEntity(level, this, rumo);
            bola.setPos(this.getX(), this.getY() + this.getBbHeight() / 2.0f + 0.5, this.getZ());
            level.addFreshEntity(bola);
        }
        level.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.GHAST_SHOOT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ o teto e a fuga

    /**
     * O que ele apanha, e o que faz quando apanha demais.
     *
     * <p>Nenhum golpe lhe tira mais de <b>cinco</b>, ou <b>oito</b> se for demoníaco; e estouro nenhum lhe
     * tira nada. Chegado a metade da vida, <b>fora do Tormento</b>, ele foge e leva gente consigo.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (fonte.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return false;
        if (fonte.getDirectEntity() instanceof Player quem) this.quemBateu.add(quem.getUUID());

        float teto = Demonic.oGolpeÉ(fonte) ? TETO_DEMONÍACO : TETO;
        boolean doeu = super.hurtServer(level, fonte, Math.min(quanto, teto));
        if (Torment.is(level)) return doeu;
        if (this.getHealth() > this.getMaxHealth() * METADE) return doeu;

        foge(level);
        return doeu;
    }

    /** A fuga: ele arrasta toda gente que o feriu e toda gente de perto, e some. */
    private void foge(ServerLevel level) {
        int andar = Torment.randomLevel(level);
        AABB perto = new AABB(this.getX() - PUXÃO, this.getY() - PUXÃO_ALTO, this.getZ() - PUXÃO,
                this.getX() + PUXÃO, this.getY() + PUXÃO_ALTO, this.getZ() + PUXÃO);

        List<net.minecraft.server.level.ServerPlayer> leva = new ArrayList<>(
                level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, perto));
        for (UUID quem : this.quemBateu) {
            if (level.getPlayerByUUID(quem) instanceof net.minecraft.server.level.ServerPlayer gente
                    && !leva.contains(gente)) {
                leva.add(gente);
            }
        }
        for (var gente : leva) Torment.order(gente, Torment.COM_O_CHEFE, andar);

        level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(),
                64, 1.0, 2.0, 1.0, 0.0);
        level.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        this.discard();
    }

    // ------------------------------------------------------------------ o que ele deixa

    /**
     * Os despojos: <b>dois livros encantados</b>, um <b>Coração de Demônio</b> e o <b>Cozimento de Alma do
     * Tormento</b>.
     *
     * <p>Os livros saem da lista de encantamentos de livro do jogo, com um grau alto, que é a conta do
     * original. E o Cozimento é a única fonte dele no mod inteiro.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean quemMatou) {
        super.dropCustomDeathLoot(level, fonte, quemMatou);
        for (int volta = 0; volta < LIVROS; volta++) {
            this.spawnAtLocation(level, livro(level, volta == 0 ? GRAU_A_MAIS : GRAU_A_MAIS_DO_SEGUNDO));
        }
        this.spawnAtLocation(level, new ItemStack(OccultaItems.DEMON_HEART));
        this.spawnAtLocation(level, new ItemStack(OccultaItems.BREW_SOUL_TORMENT));
    }

    /**
     * Um livro encantado ao acaso, com o grau do original.
     *
     * <p>A conta dele é esta: do grau mínimo mais o acréscimo até o grau máximo, e nunca acima do máximo.
     * O primeiro livro leva três a mais e o segundo um — e é por isso que o primeiro quase sempre sai no
     * teto do encantamento.
     */
    private ItemStack livro(ServerLevel level, int aMais) {
        var registro = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var quais = registro.listElements().toList();
        if (quais.isEmpty()) return new ItemStack(Items.BOOK);
        Holder<net.minecraft.world.item.enchantment.Enchantment> qual =
                quais.get(this.random.nextInt(quais.size()));
        int teto = qual.value().getMaxLevel();
        int piso = Math.min(qual.value().getMinLevel() + aMais, teto);
        int grau = piso + this.random.nextInt(Math.max(1, teto - piso + 1));

        var mexe = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        mexe.set(qual, grau);
        ItemStack feito = new ItemStack(Items.ENCHANTED_BOOK);
        feito.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, mexe.toImmutable());
        return feito;
    }

    // ------------------------------------------------------------------ o resto

    @Override
    public boolean removeWhenFarAway(double distância) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return OccultaSounds.TORMENT_LAUGH.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.TORMENT_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.TORMENT_DEATH.value();
    }

    @Override
    public int getAmbientSoundInterval() {
        return RI_DE;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.store("WITCAttackers", net.minecraft.core.UUIDUtil.CODEC_LINKED_SET, this.quemBateu);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.quemBateu.clear();
        entrada.read("WITCAttackers", net.minecraft.core.UUIDUtil.CODEC_LINKED_SET)
                .ifPresent(this.quemBateu::addAll);
    }

    /** Sem uso fora do porte: serve à prova para saber quem ele já anotou. */
    public Set<UUID> oQueAnotou() {
        return Set.copyOf(this.quemBateu);
    }

    /** E o tipo dele, para quem o quiser chamar. */
    public static EntityType<LordOfTormentEntity> tipo() {
        return OccultaEntities.LORD_OF_TORMENT;
    }

    /** Sem uso fora do porte: a prova precisa de pôr um Mob qualquer de alvo. */
    public void mira(@Nullable Mob quem) {
        this.setTarget(quem);
    }
}
