package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Espírito</b>: o {@code EntitySpirit} do Witchery.
 *
 * <p>Uma lanterna de quatro dedos que <b>deriva</b> pelo mundo dos sonhos. Quatro de vida, e não há nada
 * nele que magoe ninguém — o ataque de quatro que ele tem no papel nunca sai, porque ele não tem meta que
 * lhe mande bater.
 *
 * <p>O que ele é, de verdade, é <b>moeda</b>: os cinco efeitos de fetiche custam <b>três espíritos cada</b>,
 * e não há outra maneira de os conseguir. Quem quer um Espantalho que grite tem de ir ao outro lado buscar
 * três destes.
 *
 * <h2>Não se doma, e vem ver</h2>
 *
 * <p>Clicar nele não faz nada — o original devolve {@code false} ao toque, e é de propósito. O que o traz
 * perto é a <b>Vontade Concentrada</b> na mão: ele deriva até ela, sobe um bocadinho para a cheirar, e se
 * quem a segura <b>se mexer</b>, ele perde o interesse por cem batidas. Não é um bicho que se segue; é um
 * bicho que se espera.
 *
 * <h2>O prazo e o rumo</h2>
 *
 * <p>Posto no mundo pelo <b>Espírito Dominado</b>, ele fica. Posto pelo <b>Espírito Dominado da Aldeia</b>,
 * ele ganha <b>dez segundos de vida</b> e o rumo da <b>aldeia mais perto</b> — e, quando o prazo acaba, some
 * num estouro e <b>devolve o item</b>. É uma bússola de aldeia feita de fantasma, e é a melhor ideia pequena
 * do Witchery: a coisa que aponta o caminho é a mesma que se gasta ao apontá-lo.
 *
 * @see FlyerGoals as quatro metas que o fazem derivar em vez de andar
 */
public class SpiritEntity extends TamableAnimal {
    /** Quanto ele vale: quatro de vida, e o resto é enfeite. */
    public static final double VIDA = 4.0;
    public static final double VELOCIDADE = 0.4;
    public static final double MURRO = 4.0;

    /** Quanto dura um espírito com prazo: os dez segundos do original. */
    public static final int PRAZO = 20 * 10;

    /** Os três feitios dele: o que fica, o que aponta a aldeia e o que não larga nada. */
    public static final int FICA = 0;
    public static final int DA_ALDEIA = 1;
    public static final int SEM_DESPOJO = 2;

    /** A que distância a tentação o chama, e a que distância ele foge de gente. */
    public static final double TENTA_A = 10.0;
    public static final double FOGE_A = 10.0;

    /** E de quantas em quantas batidas ele resmunga — o dobro de um bicho comum, e ele é mudo. */
    public static final int CALADO = 2;

    /** A cor do pó dele quando ninguém lhe deu outra: o dourado do original. */
    public static final int DOURADO = 0xFFFFCC00;

    /** E o tamanho do ponto de pó: o décimo de bloco do original, na medida do pó de hoje. */
    public static final float PÓ = 0.5f;

    /** Quanto ele pesa na lista de quem nasce, e de quantos em quantos. */
    public static final int PESO = 1;
    public static final int DE_DOIS = 2;
    public static final int A_CINCO = 5;

    /** E o que ele exige para nascer: altura, luz e sorte. */
    public static final int ACIMA_DE = 60;
    public static final int LUZ = 8;
    public static final int UMA_EM_DEZ = 10;

    /**
     * A <b>cor</b> dele é a única coisa que o lado de cá precisa de saber — o pó sai dela, e o pó é o
     * que de verdade se vê de longe. Por isso ela anda pela rede, e o resto não.
     */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SpiritEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);

    private int prazo = -1;
    private int feitio = FICA;

    private boolean temCasa;
    private double casaX;
    private double casaY;
    private double casaZ;

    public SpiritEntity(EntityType<? extends SpiritEntity> tipo, Level level) {
        super(tipo, level);
        /*
         * <b>Sem gravidade.</b> O original reescreve o andar dele inteiro e o que sobra, no ar, é o atrito
         * de nove décimos e <b>nenhuma queda</b>. É o que isto diz, numa linha.
         */
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, VELOCIDADE)
                .add(Attributes.ATTACK_DAMAGE, MURRO);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation caminho = new FlyingPathNavigation(this, level);
        caminho.setCanOpenDoors(false);
        caminho.setCanFloat(true);
        return caminho;
    }

    /**
     * As seis metas, nas prioridades do original.
     *
     * <p>A de <b>seguir o dono</b> está aqui porque está lá, e não corre nunca: o Espírito não se doma. É a
     * marca de um bicho que o Witchery pensou em domar e depois não domou.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FlyerGoals.SentaEFica(this));
        this.goalSelector.addGoal(3, new FlyerGoals.Tentação(this, SpiritEntity::tenta, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 14.0f, 5.0f));
        this.goalSelector.addGoal(8, new FlyerGoals.VaiParaCasa(this));
        this.goalSelector.addGoal(9, new FlyerGoals.Pousa(this, true));
        this.goalSelector.addGoal(10, new FlyerGoals.Vagueia(this, FOGE_A));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, (float) FOGE_A, 0.2f));
    }

    /** O que o tenta: a <b>Vontade Concentrada</b>, e só ela. */
    public static boolean tenta(ItemStack oquê) {
        return oquê.is(OccultaItems.FOCUSED_WILL);
    }

    // ------------------------------------------------------------------ o prazo e o feitio

    public int feitio() {
        return this.feitio;
    }

    public boolean deEmpréstimo() {
        return this.prazo != -1;
    }

    /**
     * <b>Dá-lhe o rumo da aldeia mais perto</b>, e dez segundos para lá chegar.
     *
     * <p>O original procura o gerador de aldeias do mundo por reflexão e lhe pergunta onde está a mais
     * perto. O jogo de hoje responde à mesma pergunta sem reflexão nenhuma, com a procura de estruturas.
     *
     * @return se achou uma aldeia
     */
    public boolean vaiParaAAldeia(ServerLevel level, int feitio) {
        this.prazo = PRAZO;
        this.feitio = feitio;
        this.setPersistenceRequired();

        var aldeias = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(net.minecraft.tags.StructureTags.VILLAGE);
        if (aldeias.isEmpty()) return false;
        var achou = level.getChunkSource().getGenerator().findNearestMapStructure(
                level, aldeias.get(), this.blockPosition(), 100, false);
        if (achou == null) return false;
        this.casa(achou.getFirst());
        return true;
    }

    /** Põe-lhe uma casa, que é para onde a meta do rumo o leva. */
    public void casa(BlockPos onde) {
        this.temCasa = true;
        this.casaX = onde.getX();
        this.casaY = onde.getY();
        this.casaZ = onde.getZ();
    }

    public boolean temCasa() {
        return this.temCasa;
    }

    public void esqueceACasa() {
        this.temCasa = false;
    }

    public double casaX() {
        return this.casaX;
    }

    public double casaY() {
        return this.casaY;
    }

    public double casaZ() {
        return this.casaZ;
    }

    // ------------------------------------------------------------------ onde ele nasce

    /**
     * <b>Onde ele nasce.</b> O {@code getCanSpawnHere} do original.
     *
     * <p>A primeira linha é a que importa, e é a que faz o Espírito ser o que é: <b>só no mundo dos
     * sonhos</b>. Está posto na lista de nascimentos de todos os biomas de terra do mundo de cima, e é
     * esta linha que o tira de lá — de modo que a única maneira de achar um é atravessar.
     *
     * <p>Depois disso: <b>acima de sessenta</b>, em <b>grama ou areia</b>, com <b>mais de oito de luz</b>
     * e uma chance em dez. É um bicho de campo aberto e de dia.
     */
    public static boolean podeNascer(EntityType<SpiritEntity> qual,
                                     net.minecraft.world.level.ServerLevelAccessor level,
                                     net.minecraft.world.entity.EntitySpawnReason razão,
                                     BlockPos onde,
                                     net.minecraft.util.RandomSource sorte) {
        if (!SpiritWorld.is(level.getLevel())) return false;
        if (onde.getY() < ACIMA_DE) return false;
        if (sorte.nextInt(UMA_EM_DEZ) != 0) return false;

        var chão = level.getBlockState(onde.below());
        if (!chão.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                && !chão.is(net.minecraft.world.level.block.Blocks.SAND)) {
            return false;
        }
        return level.getMaxLocalRawBrightness(onde) > LUZ;
    }

    // ------------------------------------------------------------------ a cor do pó

    /** A cor do pó dele, ou zero se ninguém lhe deu uma. */
    public int cor() {
        return this.entityData.get(COR);
    }

    public void cor(int qual) {
        this.entityData.set(COR, qual);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(COR, 0);
    }

    // ------------------------------------------------------------------ o que ele faz

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.oPrazo(level);
    }

    /**
     * <b>O relógio do prazo.</b> Público para as provas lhe poderem dar corda sem esperar dez segundos.
     *
     * @return se o prazo acabou agora
     */
    public boolean oPrazo(ServerLevel level) {
        if (this.prazo == -1 || this.isRemoved()) return false;
        if (--this.prazo != 0) return false;

        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(),
                16, 1.0, 1.0, 1.0, 0.0);
        this.discard();
        this.dropFromDeath(level);
        return true;
    }

    /**
     * E o que ele larga: o <b>Espírito Dominado</b> de volta, ou o <b>da Aldeia</b> se foi esse que o pôs
     * ali. O feitio dois não larga nada — é o que a Pedra de Caminho usa, e aquele espírito é só um aviso.
     */
    private void dropFromDeath(ServerLevel level) {
        if (this.feitio == SEM_DESPOJO) return;
        this.spawnAtLocation(level, new ItemStack(this.feitio == DA_ALDEIA
                ? OccultaItems.SUBDUED_SPIRIT_VILLAGE : OccultaItems.SUBDUED_SPIRIT), 0.0f);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource fonte, boolean quemMatou) {
        super.dropCustomDeathLoot(level, fonte, quemMatou);
        this.dropFromDeath(level);
    }

    /**
     * O <b>pó</b> dele, uma vez por batida, da cor que lhe deram ou do dourado do original.
     *
     * <p><b>Diferença.</b> O original tem um pó seu, o {@code NaturePowerFX}: um <b>ponto parado</b> de
     * um décimo de bloco que corre oito quadros de uma folha e some em dez batidas. Não se mexe — a
     * gravidade que lhe passam fica guardada e nunca é usada, porque ele nasce com o andar desligado.
     *
     * <p>Aqui é o <b>pó colorido</b> do jogo, que é a mesma coisa que o mod já usa em todo lugar onde o
     * original pedia aquele: um ponto pequeno, da cor que se quiser, que fica onde se põe.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) return;
        double largura = this.getBbWidth();
        int qual = this.cor();
        this.level().addParticle(
                new net.minecraft.core.particles.DustParticleOptions(
                        qual == 0 ? DOURADO & 0xFFFFFF : qual & 0xFFFFFF, PÓ),
                this.getX() - largura * 0.5 + this.random.nextDouble() * largura,
                this.getY() + 0.1 + this.random.nextDouble() * 0.2,
                this.getZ() - largura * 0.5 + this.random.nextDouble() * largura,
                0.0, 0.0, 0.0);
    }

    /** <b>Clicar nele não faz nada.</b> O original devolve {@code false} ao toque. */
    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double quanto, float fator, DamageSource fonte) {
        return false;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * CALADO;
    }

    /** Ele é <b>mudo</b>: os três sons do original devolvem nada. */
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

    /** <b>Não procria.</b> O que o alimenta é um osso, e não serve de nada. */
    @Override
    public boolean isFood(ItemStack oquê) {
        return oquê.is(Items.BONE);
    }

    @Override
    public boolean canMate(Animal outro) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob par) {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        saída.putInt("SuicideIn", this.prazo);
        saída.putInt("SpiritType", this.feitio);
        saída.putInt("FeatherColor", this.cor());
        saída.putBoolean("HasHome", this.temCasa);
        saída.putDouble("HomeX", this.casaX);
        saída.putDouble("HomeY", this.casaY);
        saída.putDouble("HomeZ", this.casaZ);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.prazo = entrada.getIntOr("SuicideIn", -1);
        this.feitio = entrada.getIntOr("SpiritType", FICA);
        this.cor(entrada.getIntOr("FeatherColor", 0));
        this.temCasa = entrada.getBooleanOr("HasHome", false);
        this.casaX = entrada.getDoubleOr("HomeX", 0.0);
        this.casaY = entrada.getDoubleOr("HomeY", 0.0);
        this.casaZ = entrada.getDoubleOr("HomeZ", 0.0);
    }
}
