package net.thaumcraft.occulta.leonard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;

import java.util.List;

/**
 * O <b>Leonard</b>: o {@code EntityLeonard} do Witchery, e o chefe do ramo.
 *
 * <p>São <b>seiscentos</b> de vida, e nada o mata depressa. A luta dele não é de dano — é de <b>regras</b>,
 * e é a mais escrita do mod.
 *
 * <h2>Como ele entra</h2>
 *
 * <p>Chamado pelo ritual, ele nasce com <b>um quarto</b> da vida e, nos <b>sete segundos e meio</b>
 * seguintes, <b>enche-se dos três quartos que faltam</b> — quinze goles de dez em dez batidas. É a entrada
 * de um chefe: aparece fraco e cresce à frente de quem o veio buscar.
 *
 * <p><b>E o nome mente.</b> O original chama a esse tempo {@code invulnerableStartTicks} e ao gatilho
 * {@code setInvulnerableStart}, mas <b>nada no código o torna invulnerável</b>: o
 * {@code attackEntityFrom} dele só pergunta pelas Almas. Quem o apanhar a nascer pode bater-lhe — e, pelos
 * tetos, tirar doze de cada vez enquanto ele ganha trinta. Os nomes são do original e ficam; a conta é a
 * que está escrita aqui.
 *
 * <h2>O que ele faz</h2>
 *
 * <ul>
 *   <li><b>Cura-se um por segundo</b>, sempre.</li>
 *   <li>De segundo em segundo, uma em cinco, lança o <b>Enrolamento Mortal</b> a toda a gente a
 *       <b>quarenta blocos</b> — e quem já o tem não o leva outra vez.</li>
 *   <li>Não havendo ninguém por hexar, uma em cinco ele escolhe <b>uma</b> pessoa e faz-lhe uma de quatro
 *       coisas: <b>limpa-lhe as poções boas</b> (três em dez), <b>afunda-a</b> (três em dez),
 *       <b>enlouquece-a</b> (três em dez) ou <b>aquece-a</b> (uma em dez).</li>
 *   <li>Uma em cinco, por segundo, <b>põe fogo a todo o cozimento</b> que houver a quatro blocos — o gás e
 *       o líquido viram fogo. É a resposta dele a quem o tenta envenenar.</li>
 *   <li>Abaixo de <b>metade</b> da vida chama <b>Almas Perdidas</b>, quatro ou cinco de cada vez, e
 *       <b>enquanto houver uma viva ele é imune</b>. É a regra que faz a luta: não se bate nele, bate-se
 *       nelas.</li>
 *   <li>Abaixo de <b>um quarto</b>, uma em três, <b>cresce</b> — o Redimensionar de grau quatro.</li>
 *   <li>E morrendo, <b>tira o Enrolamento Mortal</b> de toda a gente a quarenta blocos.</li>
 * </ul>
 *
 * <h2>Como se lhe bate</h2>
 *
 * <p>Só <b>golpe de gente</b> lhe faz mal: flecha, feitiço, fogo e queda passam por ele sem lhe tirar nada.
 * Acima de um quarto da vida o golpe vale <b>no máximo doze</b>; abaixo, vale <b>quatro</b> — ou <b>um</b>,
 * se ele estiver crescido. É de propósito que a conta piore: quanto mais perto do fim, mais devagar.
 *
 * <p>E há uma porta: o {@link #feriDeFraqueza}, que abaixo de <b>quatro décimos</b> da vida lhe tira até
 * <b>quinze</b> (oito, crescido) sem olhar aos tetos. Quem a abre é o <b>Cozimento de Ferir Demônios</b>,
 * que vem na fatia da Urna — ver os desvios no {@code PORTE.md}.
 *
 * <h2>O que ele atira</h2>
 *
 * <p>Uma em duas vezes, uma <b>bola de feitiço</b>: Ignianima quatorze em vinte e um, Expelliarmus,
 * Flipendo e Impedimenta dois cada, Confundus um.
 */
public class LeonardEntity extends Monster implements RangedAttackMob {
    /** Os números dele. */
    public static final double VIDA = 600.0;
    public static final double VELOCIDADE = 0.35;
    public static final double ALCANCE_DE_CAÇA = 50.0;
    public static final double NÃO_SE_EMPURRA = 1.0;
    public static final int EXPERIÊNCIA = 100;

    /** A entrada: sete segundos e meio a encher, a partir de um quarto da vida. */
    public static final int ENTRADA = 150;
    public static final int ENCHE_DE = 10;
    public static final float ENCHE_ATÉ = 0.75f;
    public static final float ENCHE_EM = 15.0f;

    /** Até onde o Enrolamento Mortal alcança, e o que ele dura. */
    public static final int HEXA_A = 40;
    public static final int HEXA_DURA = 90 * 20;

    /** Uma em cinco, de segundo em segundo. */
    public static final int UMA_EM_CINCO = 5;

    /** O raio em que o cozimento pega fogo. */
    public static final int QUEIMA_A = 4;

    /** Onde as Almas nascem, e quantas. */
    public static final int ALMAS_A = 15;
    public static final double ALMAS_ALTURA = 5.0;
    public static final int ALMAS_DE = 4;
    public static final int ALMAS_ATÉ = 2;
    public static final int ALMAS_NO_MÍNIMO = 3;
    public static final int ESPERA_DAS_ALMAS = 10;
    /** E quanto cada uma dura: de um minuto a um minuto e meio. */
    public static final int ALMA_DURA = 60 * 20;
    public static final int ALMA_DURA_MAIS = 30 * 20;

    /** Os tetos do golpe de gente. */
    public static final float TETO = 12.0f;
    public static final float TETO_NO_FIM = 4.0f;
    public static final float TETO_CRESCIDO = 1.0f;
    /** E os da porta da fraqueza. */
    public static final float FRAQUEZA = 15.0f;
    public static final float FRAQUEZA_CRESCIDO = 8.0f;
    public static final float ABAIXO_DE = 0.4f;

    /** O grau do Redimensionar com que ele cresce, e a partir de qual ele conta como grande. */
    public static final int CRESCE = 3;
    public static final int GRANDE_A_PARTIR_DE = 2;
    public static final int CRESCE_DURA = 60 * 20;

    /** O golpe de perto: sete mais um sorteio de quinze, e um empurrão para cima. */
    public static final int MURRO = 7;
    public static final int MURRO_MAIS = 15;
    public static final double LEVANTA = 0.4;
    public static final int BRAÇO = 10;

    /** E os pesos dos cinco feitiços que ele atira. */
    public static final int[] PESOS = {14, 2, 2, 2, 1};
    public static final int[] FEITIÇOS = {39, 15, 17, 19, 8};

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ENTRANDO =
            net.minecraft.network.syncher.SynchedEntityData.defineId(LeonardEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> VIVE_HÁ =
            net.minecraft.network.syncher.SynchedEntityData.defineId(LeonardEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> PANCADA_NO_BRAÇO =
            net.minecraft.network.syncher.SynchedEntityData.defineId(LeonardEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BYTE);

    private final ServerBossEvent barra = new ServerBossEvent(this.getUUID(),
            this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private boolean imune;
    private int esperaDasAlmas;

    public LeonardEntity(EntityType<? extends LeonardEntity> tipo, Level level) {
        super(tipo, level);
        this.xpReward = EXPERIÊNCIA;
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.FOLLOW_RANGE, ALCANCE_DE_CAÇA)
                .add(Attributes.KNOCKBACK_RESISTANCE, NÃO_SE_EMPURRA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 20, 60, 30.0f));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(ENTRANDO, 0);
        construtor.define(VIVE_HÁ, 0);
        construtor.define(PANCADA_NO_BRAÇO, (byte) 0);
    }

    // ------------------------------------------------------------------ a entrada

    public int entrando() {
        return this.entityData.get(ENTRANDO);
    }

    public void entrando(int quantas) {
        this.entityData.set(ENTRANDO, quantas);
    }

    /** Há quanto tempo ele está no mundo. O original conta e não lê: fica, porque é dele. */
    public int viveHá() {
        return this.entityData.get(VIVE_HÁ);
    }

    /** <b>Começa a entrada</b>: o {@code setInvulnerableStart} que o ritual chama ao pô-lo no mundo. */
    public void começaAEntrada() {
        this.entrando(ENTRADA);
        this.setHealth(this.getMaxHealth() / 4.0f);
    }

    /** Se ele está <b>imune</b> porque há Almas vivas à volta. */
    public boolean imune() {
        return this.imune;
    }

    /** E se ele está <b>crescido</b>, que é o que piora os tetos. */
    public boolean crescido() {
        var cresceu = this.getEffect(OccultaEffects.RESIZING);
        return cresceu != null && cresceu.getAmplifier() >= GRANDE_A_PARTIR_DE;
    }

    // ------------------------------------------------------------------ a batida dele

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.entrando() > 0) {
            int falta = this.entrando() - 1;
            if (falta <= 0) level.levelEvent(1013, this.blockPosition(), 0);
            this.entrando(falta);
            if (this.tickCount % ENCHE_DE == 0) {
                this.heal(this.getMaxHealth() * ENCHE_ATÉ / ENCHE_EM);
            }
            this.barra.setProgress(this.getHealth() / this.getMaxHealth());
            return;
        }

        super.customServerAiStep(level);
        this.entityData.set(VIVE_HÁ, this.viveHá() + 1);
        if (this.tickCount % 20 == 0) this.heal(1.0f);

        if (this.tickCount % 20 == 0 && level.getRandom().nextInt(UMA_EM_CINCO) == 0 && this.temQuemCaçar()) {
            this.hexa(level);
        }
        if (this.tickCount % 20 == 2) {
            if (level.getRandom().nextInt(UMA_EM_CINCO) == 0) queimaOCozimento(level, this.blockPosition());
            this.asAlmas(level);
        }
        this.barra.setProgress(this.getHealth() / this.getMaxHealth());
    }

    /** Ele só faz o que faz tendo <b>a quem</b>: alvo, ou alguém que lhe bateu. */
    private boolean temQuemCaçar() {
        return this.getTarget() != null || this.getLastHurtByMob() != null;
    }

    /** Toda a gente a quarenta blocos dele, viva. */
    private List<Player> gentePerto(ServerLevel level, int raio, double altura) {
        var caixa = new AABB(this.getX() - raio, this.getY() - altura, this.getZ() - raio,
                this.getX() + raio, this.getY() + altura, this.getZ() + raio);
        return level.getEntitiesOfClass(Player.class, caixa,
                quem -> quem.isAlive() && quem.getHealth() > 0.0f);
    }

    /**
     * <b>O Enrolamento Mortal</b>, e as quatro coisas que ele faz quando já não há quem hexar.
     *
     * <p>Repare na ordem: ele <b>primeiro</b> tenta hexar toda a gente, e só se não houver ninguém por
     * hexar é que sorteia uma pessoa para o resto. É o que torna o Enrolamento a espinha da luta — enquanto
     * houver quem não o tenha, é só isso que ele faz.
     */
    private void hexa(ServerLevel level) {
        List<Player> gente = this.gentePerto(level, HEXA_A, HEXA_A);
        boolean hexou = false;
        for (Player quem : gente) {
            if (this.distanceToSqr(quem.getX(), this.getY(), quem.getZ()) > (double) HEXA_A * HEXA_A) continue;
            if (quem.hasEffect(OccultaEffects.MORTAL_COIL)) continue;
            hexou = true;
            net.thaumcraft.research.Incurable.add(quem,
                    new MobEffectInstance(OccultaEffects.MORTAL_COIL, HEXA_DURA));
            level.sendParticles(ParticleTypes.WITCH, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                    40, 0.5, 1.0, 0.5, 0.0);
        }
        if (hexou) {
            level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1.0, this.getZ(),
                    40, 0.5, 1.0, 0.5, 0.0);
            return;
        }
        if (level.getRandom().nextInt(UMA_EM_CINCO) != 1 || gente.isEmpty()) return;

        Player quem = gente.get(level.getRandom().nextInt(gente.size()));
        if (this.distanceToSqr(quem.getX(), this.getY(), quem.getZ()) > (double) HEXA_A * HEXA_A) return;
        level.sendParticles(ParticleTypes.WITCH, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                40, 0.5, 1.0, 0.5, 0.0);
        castiga(level, quem, level.getRandom().nextInt(10));
    }

    /**
     * As quatro: limpar as poções boas, afundar, enlouquecer, aquecer. Pública para a prova, que de outro
     * jeito teria de sortear até cair em cada uma.
     */
    public static void castiga(ServerLevel level, Player quem, int sorte) {
        switch (sorte) {
            case 0, 1, 2 -> {
                var tirar = new java.util.ArrayList<net.minecraft.core.Holder<
                        net.minecraft.world.effect.MobEffect>>();
                for (var tem : quem.getActiveEffects()) {
                    if (tem.getEffect().value().getCategory()
                            == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                        continue;
                    }
                    if (OccultaEffects.incurable(tem.getEffect())) continue;
                    tirar.add(tem.getEffect());
                }
                for (var qual : tirar) quem.removeEffect(qual);
            }
            case 3, 4, 5 -> net.thaumcraft.research.Incurable.add(quem,
                    new MobEffectInstance(OccultaEffects.SINKING, CRESCE_DURA, CRESCE));
            case 6, 7, 8 -> net.thaumcraft.research.Incurable.add(quem,
                    new MobEffectInstance(OccultaEffects.INSANITY, CRESCE_DURA, CRESCE));
            default -> net.thaumcraft.research.Incurable.add(quem,
                    new MobEffectInstance(OccultaEffects.OVERHEATING, CRESCE_DURA, CRESCE));
        }
    }

    /**
     * <b>O cozimento pega fogo.</b> Numa esfera cheia de quatro blocos, dois acima dele, o gás e o líquido
     * de cozimento viram <b>fogo</b>.
     */
    public static void queimaOCozimento(ServerLevel level, BlockPos onde) {
        BlockPos meio = onde.above(2);
        for (BlockPos casa : BlockPos.betweenClosed(meio.offset(-QUEIMA_A, -QUEIMA_A, -QUEIMA_A),
                meio.offset(QUEIMA_A, QUEIMA_A, QUEIMA_A))) {
            if (casa.distSqr(meio) > (double) QUEIMA_A * QUEIMA_A) continue;
            BlockState oquê = level.getBlockState(casa);
            if (!(oquê.getBlock() instanceof net.thaumcraft.occulta.brew.BrewGasBlock)) continue;
            level.setBlockAndUpdate(casa.immutable(), Blocks.FIRE.defaultBlockState());
        }
    }

    /**
     * <b>As Almas Perdidas</b>, e a imunidade que elas dão.
     *
     * <p>Abaixo de metade da vida, e havendo a quem, ele chama quatro ou cinco de cada vez — e, não
     * conseguindo pôr quatro, insiste até ter pelo menos <b>três</b>. Enquanto houver uma viva a quinze
     * blocos, <b>ele é imune</b>, e é essa a regra que faz a luta.
     *
     * <p>E chamando-as ele <b>tira o Enrolamento Mortal</b> de quem está perto — é o respiro que a luta dá
     * antes de recomeçar.
     */
    private void asAlmas(ServerLevel level) {
        if (this.getHealth() >= this.getMaxHealth() * 0.5f || !this.temQuemCaçar()) {
            this.imune = false;
            return;
        }

        if (this.getHealth() < this.getMaxHealth() * 0.25f
                && level.getRandom().nextInt(3) == 1
                && !this.hasEffect(OccultaEffects.RESIZING)) {
            this.addEffect(new MobEffectInstance(OccultaEffects.RESIZING, CRESCE_DURA, CRESCE));
        }

        var caixa = new AABB(this.getX() - ALMAS_A, this.getY() - ALMAS_ALTURA, this.getZ() - ALMAS_A,
                this.getX() + ALMAS_A, this.getY() + ALMAS_ALTURA, this.getZ() + ALMAS_A);
        var almas = level.getEntitiesOfClass(net.thaumcraft.occulta.spirit.LostSoulEntity.class, caixa,
                Entity::isAlive);
        if (!almas.isEmpty()) {
            this.imune = true;
            return;
        }

        this.imune = false;
        if (--this.esperaDasAlmas > 0) return;
        this.esperaDasAlmas = ESPERA_DAS_ALMAS;
        this.tiraOEnrolamento(level, ALMAS_A, ALMAS_ALTURA);

        int nasceram = 0;
        for (int volta = 0; volta < ALMAS_DE + level.getRandom().nextInt(ALMAS_ATÉ); volta++) {
            if (this.chamaUmaAlma(level, 1, 4)) nasceram++;
        }
        for (int volta = nasceram; volta < ALMAS_NO_MÍNIMO; volta++) {
            this.chamaUmaAlma(level, 0, 0);
        }
    }

    /** Uma alma, com o prazo dela. */
    private boolean chamaUmaAlma(ServerLevel level, int perto, int longe) {
        var nasceu = net.thaumcraft.occulta.Spawn.perto(level,
                net.thaumcraft.occulta.OccultaEntities.LOST_SOUL, this.blockPosition().above(), perto, longe);
        if (!(nasceu instanceof net.thaumcraft.occulta.spirit.LostSoulEntity alma)) return false;
        alma.prazo(ALMA_DURA + level.getRandom().nextInt(ALMA_DURA_MAIS));
        level.sendParticles(ParticleTypes.SMOKE, alma.getX(), alma.getY() + 0.5, alma.getZ(),
                20, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, alma.blockPosition(), SoundEvents.CHICKEN_EGG,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
        return true;
    }

    /** Tira o Enrolamento Mortal de quem estiver no raio: o {@code removeCoilEffects}. */
    public void tiraOEnrolamento(ServerLevel level, int raio, double altura) {
        for (Player quem : this.gentePerto(level, raio, altura)) {
            if (!quem.hasEffect(OccultaEffects.MORTAL_COIL)) continue;
            quem.removeEffect(OccultaEffects.MORTAL_COIL);
            net.thaumcraft.research.Incurable.remove(quem, OccultaEffects.MORTAL_COIL);
        }
    }

    // ------------------------------------------------------------------ bater e apanhar

    /**
     * <b>Só golpe de gente lhe faz mal</b>, e nunca mais do que o teto da hora.
     *
     * <p>Flecha, feitiço, fogo, queda e estouro passam por ele sem lhe tirar nada — e, havendo <b>Alma
     * viva</b>, nem o golpe de gente passa.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (this.imune) return false;
        if (!fonte.is(DamageTypes.PLAYER_ATTACK)) return false;
        if (this.getHealth() >= this.getMaxHealth() * 0.25f) {
            return super.hurtServer(level, fonte, Math.min(quanto, TETO));
        }
        return super.hurtServer(level, fonte,
                Math.min(quanto, this.crescido() ? TETO_CRESCIDO : TETO_NO_FIM));
    }

    /**
     * <b>A porta da fraqueza</b>: abaixo de quatro décimos da vida, até quinze de uma vez — oito, se ele
     * estiver crescido — e sem olhar aos tetos nem à imunidade das Almas.
     *
     * <p>Quem a abre, no original, é o <b>Cozimento de Ferir Demônios</b>. Ele ainda não está portado, e é
     * por isso que esta porta está aqui sozinha: ela é do bicho, e o cozimento vem buscá-la.
     */
    public void feriDeFraqueza(ServerLevel level, int quanto) {
        if (this.getHealth() >= this.getMaxHealth() * ABAIXO_DE) return;
        super.hurtServer(level, level.damageSources().magic(),
                Math.min(quanto, this.crescido() ? FRAQUEZA_CRESCIDO : FRAQUEZA));
    }

    /** O murro dele: sete mais um sorteio de quinze, e quem apanha vai ao ar. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        this.entityData.set(PANCADA_NO_BRAÇO, (byte) BRAÇO);
        this.level().broadcastEntityEvent(this, (byte) 4);
        boolean acertou = noquê.hurtServer(level, this.damageSources().mobAttack(this),
                MURRO + this.getRandom().nextInt(MURRO_MAIS));
        if (acertou) noquê.setDeltaMovement(noquê.getDeltaMovement().add(0.0, LEVANTA, 0.0));
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return acertou;
    }

    /**
     * <b>A bola de feitiço</b>, uma vez em duas.
     *
     * <p>Os pesos são os do original: Ignianima quatorze em vinte e um, Expelliarmus, Flipendo e
     * Impedimenta dois cada, Confundus um. E ela sai sempre de <b>grau um</b>.
     */
    @Override
    public void performRangedAttack(LivingEntity alvo, float força) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!this.getRandom().nextBoolean()) return;

        this.entityData.set(PANCADA_NO_BRAÇO, (byte) BRAÇO);
        this.level().broadcastEntityEvent(this, (byte) 4);

        Symbol qual = sorteia(this.getRandom());
        if (qual == null) return;
        double dx = alvo.getX() - this.getX();
        double dy = alvo.getBoundingBox().minY + alvo.getBbHeight() / 2.0 - (this.getY() + this.getBbHeight() / 2.0);
        double dz = alvo.getZ() - this.getZ();
        float dispersão = net.minecraft.util.Mth.sqrt(força) * 0.5f;
        Vec3 rumo = new Vec3(dx + this.getRandom().nextGaussian() * dispersão, dy,
                dz + this.getRandom().nextGaussian() * dispersão);

        var bola = new net.thaumcraft.occulta.symbol.SpellEffectEntity(level, this, rumo, qual, 1);
        bola.snapTo(this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(),
                this.getYRot(), this.getXRot());
        level.addFreshEntity(bola);
        level.levelEvent(1009, this.blockPosition(), 0);
    }

    /** Qual dos cinco, pelos pesos do original. */
    public static @org.jetbrains.annotations.Nullable Symbol sorteia(
            net.minecraft.util.RandomSource sorte) {
        int total = 0;
        for (int peso : PESOS) total += peso;
        int tirou = sorte.nextInt(total);
        for (int qual = 0; qual < PESOS.length; qual++) {
            tirou -= PESOS[qual];
            if (tirou < 0) return Symbols.daquele(FEITIÇOS[qual]);
        }
        return null;
    }

    @Override
    public void handleEntityEvent(byte qual) {
        if (qual == 4) {
            this.entityData.set(PANCADA_NO_BRAÇO, (byte) BRAÇO);
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
            return;
        }
        super.handleEntityEvent(qual);
    }

    /** Quantas batidas faltam do braço erguido: é o que o desenhista lê. */
    public int braço() {
        return this.entityData.get(PANCADA_NO_BRAÇO);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        byte braço = this.entityData.get(PANCADA_NO_BRAÇO);
        if (braço > 0) this.entityData.set(PANCADA_NO_BRAÇO, (byte) (braço - 1));
    }

    // ------------------------------------------------------------------ o fim dele

    @Override
    public void die(DamageSource fonte) {
        super.die(fonte);
        if (this.level() instanceof ServerLevel level) this.tiraOEnrolamento(level, HEXA_A, HEXA_A);
    }

    /**
     * <b>O que ele larga</b>: um livro encantado ao acaso e um <b>Coração de Demônio</b>.
     *
     * <p><b>Falta a Urna do Leonard</b>, que é o terceiro, e que vem na fatia dela — a urna sem os quatro
     * símbolos que bebem dela não faz nada, e por isso as duas andam juntas.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean matouGente) {
        super.dropCustomDeathLoot(level, fonte, matouGente);
        this.spawnAtLocation(level, livroAoAcaso(level), 0.0f);
        this.spawnAtLocation(level,
                new net.minecraft.world.item.ItemStack(net.thaumcraft.occulta.OccultaItems.DEMON_HEART), 0.0f);
    }

    /**
     * Um <b>livro encantado</b> de um encantamento sorteado, do nível de sempre <b>mais dois</b> até o
     * máximo — a conta é a do original, e é a mesma que o Senhor do Tormento usa.
     */
    public static net.minecraft.world.item.ItemStack livroAoAcaso(ServerLevel level) {
        var registro = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var quais = registro.listElements().toList();
        if (quais.isEmpty()) return new net.minecraft.world.item.ItemStack(
                net.minecraft.world.item.Items.BOOK);
        var qual = quais.get(level.getRandom().nextInt(quais.size()));
        int teto = qual.value().getMaxLevel();
        int piso = Math.min(qual.value().getMinLevel() + 2, teto);
        int grau = piso + level.getRandom().nextInt(Math.max(1, teto - piso + 1));

        var mexe = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        mexe.set(qual, grau);
        var feito = new net.minecraft.world.item.ItemStack(
                net.minecraft.world.item.Items.ENCHANTED_BOOK);
        feito.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, mexe.toImmutable());
        return feito;
    }

    // ------------------------------------------------------------------ a barra, e o resto

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
    public void setCustomName(net.minecraft.network.chat.@org.jetbrains.annotations.Nullable Component nome) {
        super.setCustomName(nome);
        this.barra.setName(this.getDisplayName());
    }

    /** <b>Teia não o segura</b>, como a Alma: o original escreve um {@code setInWeb} vazio. */
    @Override
    public void makeStuckInBlock(BlockState oquê, Vec3 quanto) {
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double longe) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return OccultaSounds.LEONARD_SAY.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return OccultaSounds.LEONARD_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return OccultaSounds.LEONARD_DEATH.value();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("Invul", this.entrando());
        saída.putInt("Lifetime", this.viveHá());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.entrando(entrada.getIntOr("Invul", 0));
        this.entityData.set(VIVE_HÁ, entrada.getIntOr("Lifetime", 0));
        this.barra.setName(this.getDisplayName());
    }
}
