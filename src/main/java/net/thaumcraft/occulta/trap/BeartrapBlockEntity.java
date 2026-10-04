package net.thaumcraft.occulta.trap;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.Spawn;
import net.thaumcraft.occulta.wolf.Moon;
import net.thaumcraft.occulta.wolf.WolfmanEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A alma das duas armadilhas: o {@code TileEntityBeartrap} do Witchery.
 *
 * <p>Ela guarda <b>três coisas</b>: se está disparada, <b>quem a pôs</b> e — só a de prata — <b>qual
 * lobisomem</b> ela chamou.
 *
 * <p>O <b>dono</b> é o que a torna invisível. Uma armadilha armada por outra pessoa fica quase transparente,
 * e só quem a pôs a vê inteira. A de ferro comum, isto é: a <b>de prata nunca se esconde</b>, porque ela não
 * é para pegar gente.
 *
 * <p>E o lobisomem guardado é a parte mais estranha e mais bonita do bloco. A <b>Armadilha de Lobo</b> não
 * espera que um lobisomem passe por ela: ela <b>chama um</b>. Posta ao pé de um <b>Altar do Lobo</b>, com uma
 * <b>ovelha na corda</b> a oito blocos de distância, ela espera a <b>lua cheia</b> e põe um lobisomem no mato
 * a dezesseis ou trinta e dois blocos — e anota <b>quem</b> ela chamou.
 *
 * <p>Depois ela só aceita <b>aquele</b>. Nenhum outro lobisomem a dispara, e quando o certo pisa nela ela
 * <b>o torna contagioso</b>: a mordida dele passa a licantropia, e é esse o caminho do original para alguém
 * virar lobisomem de propósito em vez de por azar.
 *
 * <p>Que é dizer: a Armadilha de Lobo não é uma armadilha. É uma <b>isca</b>, e a ovelha é o anzol.
 */
public class BeartrapBlockEntity extends BlockEntity {
    /** Quanto tempo ela leva a ficar sensível depois de armada. */
    public static final int ACALMA = 20;

    /** A que distância ela procura a ovelha. */
    public static final double ISCA = 8.0;

    /** De quantas em quantas batidas ela olha a lua e a isca. */
    public static final int OLHA_DE = 200;

    /** E onde o lobisomem nasce: no anel do original. */
    public static final int PERTO = 16;
    public static final int LONGE = 32;

    /** Quantas fumaças a chegada dele levanta. */
    public static final int FUMAÇAS = 20;

    private @Nullable UUID dono;
    private boolean disparada = true;
    private long armadaEm;
    private long esperaDesde;
    private @Nullable UUID loboChamado;
    private int batidas;

    public BeartrapBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.BEARTRAP_ENTITY, onde, feitio);
    }

    /** Se esta é a <b>de prata</b>, que é a que chama o lobisomem e não pega gente. */
    public boolean dePrata() {
        return this.getBlockState().is(OccultaBlocks.WOLFTRAP);
    }

    public boolean disparada() {
        return this.disparada;
    }

    /** Quem a pôs, para o desenhista saber a quem mostrá-la. */
    public void dono(Player quem) {
        this.dono = quem.getUUID();
    }

    /**
     * Arma ou desarma, que é o que um clique faz.
     *
     * <p>Ela nasce <b>disparada</b>: quem a põe no chão tem de a armar com um clique, e é por isso que o
     * original a põe assim — uma armadilha não se arma sozinha ao cair da mão de ninguém.
     */
    public void vira(Level level) {
        this.disparada = !this.disparada;
        if (!this.disparada) this.armadaEm = level.getGameTime();
        this.avisa();
    }

    /** Dispara, que é o que um pé faz. */
    public void dispara() {
        this.disparada = true;
        this.avisa();
    }

    /** Se ela já está sensível: o original lhe dá <b>vinte batidas</b> para quem a armou sair de cima. */
    public boolean sensível(Level level) {
        return !this.disparada && level.getGameTime() > this.armadaEm + ACALMA;
    }

    /**
     * Se este é o lobisomem que <b>ela</b> chamou — e, sendo, o faz <b>contagioso</b>.
     *
     * <p>É o {@code tryTrapWolf} do original. Nenhum outro lobisomem serve: a armadilha de prata reconhece o
     * dela, e só nele é que ela trabalha.
     */
    public boolean apanhaOLobo(net.minecraft.world.entity.LivingEntity quem) {
        if (!this.dePrata() || this.loboChamado == null) return false;
        if (!(quem instanceof WolfmanEntity lobo)) return false;
        if (!this.loboChamado.equals(lobo.getUUID())) return false;

        lobo.contagioso(true);
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, OccultaSounds.WOLFMAN_LORD.value(),
                    SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        return true;
    }

    /**
     * Se ela está <b>à vista</b> de alguém: o {@code isVisibleTo} do original.
     *
     * <p>Uma armadilha <b>disparada</b> está à vista de todos — já não serve de nada escondê-la. Uma <b>sem
     * dono</b> também, que é o caso de quem a põe por comando ou por geração. A <b>de prata</b> também, que
     * não é para pegar gente. Só sobra uma: a de ferro, armada, posta por alguém — e essa só quem a pôs vê.
     */
    public boolean àVistaDe(@Nullable Player quem) {
        if (this.disparada || this.dono == null || this.dePrata()) return true;
        return quem != null && this.dono.equals(quem.getUUID());
    }

    /**
     * O relógio dela, que só a <b>de prata</b> tem.
     *
     * <p>De dez em dez segundos, armada e sem lobisomem chamado, ela olha se a isca está no lugar e se a lua
     * está cheia. Estando, ela <b>espera uma volta</b> e só então chama — que é o que o original faz com o
     * {@code startTime}: ele guarda a hora da primeira vez que viu tudo pronto e chama na segunda.
     *
     * <p>Faltando a isca, o relógio <b>volta a zero</b>: uma ovelha que se solta da corda desfaz a espera.
     */
    public void bate(ServerLevel level) {
        this.batidas++;
        if (!this.dePrata() || this.disparada || this.loboChamado != null) return;
        if (this.batidas % OLHA_DE != 0) return;

        if (!this.ascoisasNoLugar(level)) {
            this.esperaDesde = 0L;
            return;
        }
        if (this.esperaDesde == 0L) {
            this.esperaDesde = level.getGameTime();
            return;
        }
        if (level.getGameTime() <= this.esperaDesde) return;

        var bicho = Spawn.perto(level, net.thaumcraft.occulta.OccultaEntities.WOLFMAN,
                this.worldPosition, PERTO, LONGE);
        if (!(bicho instanceof WolfmanEntity lobo)) return;

        lobo.setPersistenceRequired();
        this.loboChamado = lobo.getUUID();

        /*
         * A fumaça e a fala, pelas contas do {@code spawnCreature}: <b>vinte</b> fumaças, espalhadas um
         * bloco para cada lado e a altura do bicho para cima, e a fala dele a meia volume com o tom baixo.
         */
        for (int volta = 0; volta < FUMAÇAS; volta++) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    lobo.getX() + level.getRandom().nextDouble() * 2.0 - 1.0,
                    lobo.getY() + level.getRandom().nextDouble() * lobo.getBbHeight(),
                    lobo.getZ() + level.getRandom().nextDouble() * 2.0 - 1.0,
                    1, 0.02, 0.02, 0.02, 0.0);
        }
        level.playSound(null, lobo.blockPosition(), OccultaSounds.WOLFMAN_SAY.value(),
                SoundSource.HOSTILE, net.thaumcraft.occulta.trap.BeartrapBlock.MEIO,
                net.thaumcraft.occulta.trap.BeartrapBlock.tom(level));
        this.avisa();
    }

    /**
     * A isca: uma <b>ovelha na corda</b> a oito blocos, e um <b>Altar do Lobo</b> ao lado.
     *
     * <p>A corda é o que faz a diferença. Uma ovelha solta não serve: ela tem de estar <b>presa</b>, porque o
     * original quer que alguém a tenha levado até lá de propósito.
     */
    public boolean ascoisasNoLugar(ServerLevel level) {
        if (!Moon.cheia(level)) return false;
        if (!oaltarAoLado(level)) return false;
        return aovelhaNaCorda(level);
    }

    /** Um Altar do Lobo numa das quatro casas ao lado — nas quinas não vale. */
    private boolean oaltarAoLado(ServerLevel level) {
        for (var lado : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(this.worldPosition.relative(lado))
                    .is(OccultaBlocks.WEREWOLF_STATUE)) {
                return true;
            }
        }
        return false;
    }

    /** E uma ovelha presa, dentro da bola de oito blocos — a conta é por distância, não pela caixa. */
    private boolean aovelhaNaCorda(ServerLevel level) {
        double meioX = this.worldPosition.getX() + 0.5;
        double meioY = this.worldPosition.getY() + 0.5;
        double meioZ = this.worldPosition.getZ() + 0.5;
        AABB onde = new AABB(meioX - ISCA, meioY - ISCA, meioZ - ISCA,
                meioX + ISCA, meioY + ISCA, meioZ + ISCA);
        for (Sheep ovelha : level.getEntitiesOfClass(Sheep.class, onde)) {
            if (!ovelha.isLeashed()) continue;
            if (ovelha.distanceToSqr(meioX, meioY, meioZ) <= ISCA * ISCA) return true;
        }
        return false;
    }

    private void avisa() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ------------------------------------------------------------------ o que ela guarda

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        dados.putBoolean("Sprung", this.disparada);
        dados.putLong("WolftrapStart", this.esperaDesde);
        dados.putLong("SetTime", this.armadaEm);
        if (this.dono != null) dados.store("Owner", net.minecraft.core.UUIDUtil.CODEC, this.dono);
        if (this.loboChamado != null) {
            dados.store("Wolf", net.minecraft.core.UUIDUtil.CODEC, this.loboChamado);
        }
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.disparada = dados.getBooleanOr("Sprung", true);
        this.esperaDesde = dados.getLongOr("WolftrapStart", 0L);
        this.armadaEm = dados.getLongOr("SetTime", 0L);
        this.dono = dados.read("Owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.loboChamado = dados.read("Wolf", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    /** Ela se desenha pelo que guarda, e por isso o cliente precisa de tudo. */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(
            net.minecraft.core.HolderLookup.Provider registros) {
        return this.saveCustomOnly(registros);
    }
}
