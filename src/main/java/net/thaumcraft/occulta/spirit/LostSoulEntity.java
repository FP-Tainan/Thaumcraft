package net.thaumcraft.occulta.spirit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Alma Perdida</b>: o {@code EntityLostSoul} do Witchery.
 *
 * <p>É um <b>Espírito que briga</b>. O corpo é o mesmo — a lanterna de papel de um quarto de bloco, o pó
 * que ela larga, a mesma folha de textura — e o que muda é tudo o resto: ela tem vinte de vida em vez de
 * quatro, caça gente, e <b>investe</b> em quem achar até encostar.
 *
 * <p>E ela tem <b>três feitios</b>, sorteados ao nascer, que mandam em três coisas ao mesmo tempo:
 *
 * <table border="1">
 *   <caption>Os três feitios</caption>
 *   <tr><th>feitio</th><th>cor</th><th>o golpe dela</th><th>o que a machuca</th></tr>
 *   <tr><td><b>Fogo</b></td><td>vermelha</td><td>queima</td><td><b>só</b> fogo e estouro</td></tr>
 *   <tr><td><b>Golpe</b></td><td>verde</td><td>golpe comum</td><td>tudo <b>menos</b> flecha, magia, fogo,
 *       estouro, parede, cacto, afogamento e murcha</td></tr>
 *   <tr><td><b>Magia</b></td><td>azul</td><td>enfeitiça</td><td><b>só</b> magia</td></tr>
 * </table>
 *
 * <p>Repare no feitio do fogo: o que ele atira é o que o mata. É assim no original, e é a melhor ideia
 * dele — a cor da alma diz, à distância, <b>qual arma serve</b>, e enganar-se é bater nela a tarde toda
 * sem lhe tirar nada.
 *
 * <p>E o golpe do feitio só sai <b>uma vez em quatro</b>: nas outras três ela bate como qualquer bicho.
 * Nenhum golpe tira mais de <b>quinze</b>, venha de onde vier.
 *
 * <p><b>Ela também tem hora para acabar</b>, se lhe puserem uma: é o {@code setTimeToLive}, e é como o
 * <b>Leonard</b> manda embora as almas que chamou. Acabando o prazo ela <b>some calada</b> — sem o estouro
 * de pó e sem largar nada, ao contrário do Espírito de quem herda.
 *
 * <p><b>E teia não a segura</b>: o original escreve um {@code setInWeb} vazio, e é a única coisa que ela
 * acrescenta ao andar do Espírito.
 *
 * <p><b>Desvio declarado:</b> no original ela guarda um <b>segundo</b> relógio, seu, com o <b>mesmo nome
 * de etiqueta</b> do relógio que já herda do Espírito — de modo que gravar uma apaga a outra. Aqui o
 * relógio é um só, o do Espírito, e o que ela muda é o que acontece quando ele acaba. Em jogo dá na mesma,
 * porque a Alma nunca põe o relógio do Espírito a andar.
 */
public class LostSoulEntity extends SpiritEntity {
    /** Os três feitios, pela ordem do original. */
    public static final int FOGO = 0;
    public static final int GOLPE = 1;
    public static final int MAGIA = 2;
    public static final int FEITIOS = 3;

    /** E as três cores, que são as do original: vermelho, verde e azul puros. */
    public static final int VERMELHO = 0xFF0000;
    public static final int VERDE = 0x00FF00;
    public static final int AZUL = 0x0000FF;

    /** Os números dela. */
    public static final double VIDA = 20.0;
    public static final double VELOCIDADE = 0.4;
    public static final double MURRO = 2.0;

    /** Nenhum golpe lhe tira mais do que isto. */
    public static final float TETO = 15.0f;

    /** E o golpe do feitio sai uma vez em quatro. */
    public static final int UMA_EM = 4;

    /** Até onde ela vê quem caçar, e a que distância olha. */
    public static final float OLHA_A = 10.0f;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> FEITIO =
            net.minecraft.network.syncher.SynchedEntityData.defineId(LostSoulEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    public LostSoulEntity(EntityType<? extends LostSoulEntity> tipo, Level level) {
        super(tipo, level);
        // o original sorteia o feitio onde declara o dado sincronizado, que é dentro do construtor
        this.feitioDaAlma(this.random.nextInt(FEITIOS));
    }

    /**
     * Os três números dela, e não os do Espírito.
     *
     * <p>A lista monta-se do zero, e não por cima da do pai: o molde do jogo de hoje recusa a mesma chave
     * duas vezes, de modo que não há como escrever «como o Espírito, mas com vinte de vida».
     */
    public static AttributeSupplier.Builder attributes() {
        return net.minecraft.world.entity.TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO);
    }

    /**
     * As quatro metas dela, e <b>só</b> estas quatro.
     *
     * <p>O construtor do original <b>esvazia</b> as listas que herda do Espírito antes de pôr as suas, e é
     * por isso que uma Alma Perdida sem alvo não vagueia: ela senta, pousa e espera. Aqui a lista nasce
     * vazia porque não se chama a do pai.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FlyerGoals.SentaEFica(this));
        this.goalSelector.addGoal(2, new FlyerGoals.Investe(this, true));
        this.goalSelector.addGoal(9, new FlyerGoals.Pousa(this, true));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, OLHA_A, 0.2f));
        // o sorteio de um é «todas as batidas», que é o que o zero do original quer dizer
        this.targetSelector.addGoal(2,
                new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false, null));
    }

    // ------------------------------------------------------------------ o feitio

    public int feitioDaAlma() {
        return this.entityData.get(FEITIO);
    }

    /** Põe-lhe o feitio, e com ele a cor do pó — que é como se sabe qual é, de longe. */
    public void feitioDaAlma(int qual) {
        this.entityData.set(FEITIO, qual);
        this.cor(switch (qual) {
            case GOLPE -> VERDE;
            case MAGIA -> AZUL;
            default -> VERMELHO;
        });
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(FEITIO, FOGO);
    }

    // ------------------------------------------------------------------ bater e apanhar

    /**
     * O golpe dela: <b>uma vez em quatro</b> é o do feitio, e nas outras três é um golpe de bicho como
     * qualquer outro.
     *
     * <p>O do feitio do golpe <b>é</b> o golpe comum, e por isso só dois dos três mudam alguma coisa.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity noquê) {
        DamageSource doFeitio = this.golpeDoFeitio(level);
        if (doFeitio == null) return super.doHurtTarget(level, noquê);
        return noquê.hurtServer(level, doFeitio,
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    /** Qual golpe o feitio dá desta vez, ou {@code null} se é o comum. */
    private @Nullable DamageSource golpeDoFeitio(ServerLevel level) {
        if (level.getRandom().nextInt(UMA_EM) != 0) return null;
        return switch (this.feitioDaAlma()) {
            case FOGO -> level.damageSources().inFire();
            case MAGIA -> level.damageSources().magic();
            default -> null;
        };
    }

    /**
     * E o que a machuca: <b>só o que o feitio dela deixa</b>, e nunca mais de quinze de uma vez.
     *
     * <p>Tudo o mais passa por ela sem lhe tirar nada — inclusive, nos feitios do fogo e da magia, a
     * queda no vazio e o comando que mata. É assim no original, e é por isso que as almas do Leonard
     * nascem com <b>prazo</b>: sem ele, não haveria como tirá-las do mundo.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource fonte, float quanto) {
        if (!apanhaDe(fonte, this.feitioDaAlma())) return false;
        return super.hurtServer(level, fonte, Math.min(quanto, TETO));
    }

    /** A tabela de quem apanha do quê, lida do {@code attackEntityFrom} do original. */
    public static boolean apanhaDe(DamageSource fonte, int feitio) {
        return switch (feitio) {
            case FOGO -> fonte.is(DamageTypeTags.IS_FIRE) || fonte.is(DamageTypeTags.IS_EXPLOSION);
            case MAGIA -> éMágico(fonte);
            default -> !fonte.is(DamageTypeTags.IS_PROJECTILE)
                    && !éMágico(fonte)
                    && !fonte.is(DamageTypeTags.IS_FIRE)
                    && !fonte.is(DamageTypeTags.IS_EXPLOSION)
                    && !fonte.is(DamageTypes.IN_WALL)
                    && !fonte.is(DamageTypes.CACTUS)
                    && !fonte.is(DamageTypes.DROWN)
                    && !fonte.is(DamageTypes.WITHER);
        };
    }

    /** O que o original chama de dano mágico, que é o mesmo que o resto do ramo chama. */
    private static boolean éMágico(DamageSource fonte) {
        return fonte.is(DamageTypes.MAGIC) || fonte.is(DamageTypes.INDIRECT_MAGIC);
    }

    // ------------------------------------------------------------------ o resto

    /** <b>Acabando o prazo, ela some calada:</b> sem estouro de pó e sem largar nada. */
    @Override
    protected void acabou(ServerLevel level) {
        this.discard();
    }

    /** <b>Teia não a segura.</b> O original escreve um {@code setInWeb} vazio, e é o que isto é. */
    @Override
    public void makeStuckInBlock(BlockState oquê, Vec3 quanto) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("SoulType", this.feitioDaAlma());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        // a cor vem do pai, gravada à parte: aqui só se repõe o feitio, sem lhe mexer
        this.entityData.set(FEITIO, entrada.getIntOr("SoulType", FOGO));
    }
}
