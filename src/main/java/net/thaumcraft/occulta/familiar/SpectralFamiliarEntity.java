package net.thaumcraft.occulta.familiar;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O <b>Familiar Espectral</b>: o {@code EntityFamiliar} do Witchery.
 *
 * <p>É um <b>porco azul translúcido</b> que acha minério. No original ele é um ocelote por dentro — a
 * classe estende {@code EntityOcelot}, senta-se como gato e doma-se como gato — e um <b>porco</b> por
 * fora, com o modelo, a pele e os grunhidos do porco do jogo. Quando acha alguma coisa, ele diz as
 * coordenadas ao dono <b>grunhindo</b>: «OINK! 142, 11, -308.»
 *
 * <p>Nada disto é engano de quem o escreveu. O bicho é uma piada feita com cuidado, e é das coisas mais
 * queridas do mod.
 *
 * <h2>Como se usa</h2>
 *
 * <p>Dá-se-lhe na mão <b>o que sai do minério</b> — um diamante, uma esmeralda, uma barra de ouro ou de
 * ferro, redstone, lápis-lazúli ou carvão — e ele vai procurar <b>o minério daquilo</b>, por baixo de onde
 * está. Achando, anda até lá, <b>senta-se em cima</b> e grunhe as coordenadas.
 *
 * <p>E ele <b>gasta-se</b>. Cada pedido é uma chance de ele se ir embora num rodopio de portal: nenhuma à
 * primeira, três em cinco à segunda, e daí a subir até à quinta, que é sempre a última. Quem lhe pedir
 * cinco coisas fica sem ele.
 *
 * <p>Mão vazia <b>manda-o sentar</b>, como qualquer bicho domado.
 */
public class SpectralFamiliarEntity extends TamableAnimal {
    /** A vida dele, que é o dobro da de um porco. */
    public static final double VIDA = 40.0;

    /** Quantas vezes se lhe pode pedir antes de ele ser sempre o último pedido. */
    public static final int PEDIDOS = 5;

    /** E a chance de ele se ir embora a cada pedido, a começar no segundo. */
    public static final double[] VAI_EMBORA = {0.0, 0.6, 0.75, 0.85, 0.95};

    /** O que se lhe dá, e o minério de cada coisa, e a fundura a que ele o procura. */
    public static final List<Pedido> PEDE = List.of(
            new Pedido(Items.DIAMOND, List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE), 14),
            new Pedido(Items.EMERALD, List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE), 14),
            new Pedido(Items.GOLD_INGOT, List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE), 30),
            new Pedido(Items.IRON_INGOT, List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE), 64),
            new Pedido(Items.REDSTONE, List.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE), 16),
            new Pedido(Items.LAPIS_LAZULI, List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE), 30),
            new Pedido(Items.COAL, List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE), 64));

    /**
     * Uma coisa que se lhe pode pedir.
     *
     * <p><b>Desvio declarado:</b> cada pedido tem <b>dois</b> minérios e não um — o de pedra e o de
     * ardósia. Em 2014 a ardósia não existia, e hoje metade do minério do mundo é dela.
     */
    public record Pedido(net.minecraft.world.item.Item coisa, List<Block> minérios, int fundura) {
        public boolean é(BlockState oQueTem) {
            for (Block qual : this.minérios) {
                if (oQueTem.is(qual)) return true;
            }
            return false;
        }
    }

    /** Qual deles ele procura agora, ou nada: vai sincronizado porque a <b>cor</b> dele depende disso. */
    private static final EntityDataAccessor<Integer> PROCURA =
            SynchedEntityData.defineId(SpectralFamiliarEntity.class, EntityDataSerializers.INT);

    /** Quantas vezes já lhe pediram. */
    private int pedidos;

    public SpectralFamiliarEntity(EntityType<? extends SpectralFamiliarEntity> tipo, Level level) {
        super(tipo, level);
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(PROCURA, -1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 5.0f));
        this.goalSelector.addGoal(4, new FindOreGoal(this, 1.33));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------ o que ele procura

    /** Qual dos sete ele procura, ou menos um. */
    public int procura() {
        return this.getEntityData().get(PROCURA);
    }

    public void procura(int qual) {
        this.getEntityData().set(PROCURA, qual);
    }

    /** O pedido de agora, se há algum. */
    public @Nullable Pedido oQueProcura() {
        int qual = this.procura();
        return qual < 0 || qual >= PEDE.size() ? null : PEDE.get(qual);
    }

    /** E a que fundura: a do pedido, contada do fundo do mundo. */
    public int fundura() {
        Pedido qual = this.oQueProcura();
        return qual == null ? 1 : qual.fundura();
    }

    /** Qual dos sete é esta coisa, ou menos um. */
    public static int qualPedido(ItemStack coisa) {
        for (int i = 0; i < PEDE.size(); i++) {
            if (coisa.is(PEDE.get(i).coisa())) return i;
        }
        return -1;
    }

    /** Se aquele bloco é o minério que ele procura. */
    public boolean éOQueProcura(BlockState oQueTem) {
        Pedido qual = this.oQueProcura();
        return qual != null && qual.é(oQueTem);
    }

    // ------------------------------------------------------------------ o clique

    @Override
    public InteractionResult mobInteract(Player quem, InteractionHand mão) {
        if (!this.isTame() || !this.isOwnedBy(quem)) return InteractionResult.PASS;
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        ItemStack naMão = quem.getItemInHand(mão);
        int qual = qualPedido(naMão);
        if (qual < 0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }

        this.procura(qual);
        this.setOrderedToSit(false);
        this.pedidos++;
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);

        if (this.seVaiEmbora()) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 0.5, this.getZ(), 32, 0.5, 0.5, 0.5, 0.3);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.NEUTRAL, 1.0f, 1.0f);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, quem.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.NEUTRAL, 0.5f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    /**
     * Se este pedido foi o último: o sorteio do original.
     *
     * <p>Ao <b>primeiro</b> ele fica sempre; do segundo em diante a chance sobe; <b>passados cinco</b>
     * ele vai-se sempre embora.
     */
    public boolean seVaiEmbora() {
        if (this.pedidos <= 1) return false;
        if (this.pedidos > PEDIDOS) return true;
        return this.random.nextDouble() < VAI_EMBORA[Math.min(this.pedidos - 1, VAI_EMBORA.length - 1)];
    }

    /** Quantas vezes já lhe pediram, para as provas. */
    public int pedidos() {
        return this.pedidos;
    }

    public void pedidos(int quantos) {
        this.pedidos = quantos;
    }

    // ------------------------------------------------------------------ e o resto

    /** Ele <b>mal toca</b> em quem quer que seja: meio coração, como no original. */
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity quem) {
        return quem.hurtServer(level, this.damageSources().mobAttack(this), 0.5f);
    }

    /** Dele cai Pó Espectral, sempre: é do que ele é feito. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level,
                                       net.minecraft.world.damagesource.DamageSource porquê,
                                       boolean matouAlguém) {
        super.dropCustomDeathLoot(level, porquê, matouAlguém);
        this.spawnAtLocation(level,
                new ItemStack(net.thaumcraft.occulta.OccultaItems.SPECTRAL_DUST));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob outro) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack coisa) {
        return false;
    }

    @Override
    public boolean canMate(Animal outro) {
        return false;
    }

    /**
     * A <b>voz do porco</b>, pedida ao registro pelo nome.
     *
     * <p>Em 2014 os três grunhidos eram constantes no código. Hoje a voz de um porco vem de uma
     * <b>variante de som</b> escrita em dados, e as constantes do jogo já não as trazem — mas os sons em
     * si continuam lá com os mesmos nomes, que é por onde se lhes chega.
     */
    private static @Nullable SoundEvent grunhido(String qual) {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(
                net.minecraft.resources.Identifier.withDefaultNamespace("entity.pig." + qual));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return grunhido("ambient");
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource porquê) {
        return grunhido("hurt");
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return grunhido("death");
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput dados) {
        super.addAdditionalSaveData(dados);
        dados.putInt("Procura", this.procura());
        dados.putInt("Pedidos", this.pedidos);
    }

    @Override
    public void readAdditionalSaveData(ValueInput dados) {
        super.readAdditionalSaveData(dados);
        this.procura(dados.getIntOr("Procura", -1));
        this.pedidos = dados.getIntOr("Pedidos", 0);
    }
}
