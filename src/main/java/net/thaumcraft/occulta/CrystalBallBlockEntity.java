package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma da Bola de Cristal: a {@code TileEntityCrystalBall} do Witchery.
 *
 * <p>Guarda uma coisa só: <b>quando ela foi usada pela última vez</b>. Entre uma leitura e outra passam
 * <b>cem batidas</b> — cinco segundos —, e quem bater nela antes disso ouve um tambor e um recado de que ela
 * ainda está se recompondo.
 *
 * <p>É uma recarga curta de propósito: o que trava quem lê a sorte não é o tempo, é o <b>poder do altar</b>,
 * que são quinhentos por leitura. Os cinco segundos existem só para que um clique duplo não gaste mil.
 */
public class CrystalBallBlockEntity extends BlockEntity {
    /** Quanto tempo entre uma leitura e outra. */
    public static final long RECARGA = 100L;

    /** O vaivém do miolo: oito segundos de ida e volta. */
    public static final long VOLTA = 160L;
    public static final long MEIA = 80L;
    public static final long TETO = 100L;

    /**
     * <b>O cinzento do miolo àquela hora.</b>
     *
     * <p>A conta é a do original e é de uma simplicidade que convém não estragar: toma-se a hora do mundo,
     * dá-se o resto por <b>cento e sessenta</b>, mede-se a distância desse resto a <b>oitenta</b> e tira-se
     * isso de <b>cem</b>. O que sai vai de vinte a cem e volta, num vaivém de oito segundos.
     *
     * <p>Quer dizer que a bola <b>respira</b>, e que todas as bolas do mundo respiram <b>ao mesmo tempo</b>,
     * porque todas leem a mesma hora. Numa casa com duas, elas batem juntas.
     *
     * <p>A conta mora aqui, e não no desenhista, para que as provas do servidor a possam ver.
     */
    public static int pulso(long hora) {
        long quanto = TETO - Math.abs(hora % VOLTA - MEIA);
        int tom = (int) Math.round(quanto * 0.01 * 255.0);
        return 0xFF000000 | (tom << 16) | (tom << 8) | tom;
    }

    private long últimaVez;

    public CrystalBallBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.CRYSTAL_BALL_ENTITY, onde, feitio);
    }

    public boolean podeSerUsada(ServerLevel level) {
        return level.getGameTime() - this.últimaVez > RECARGA;
    }

    public void usada(ServerLevel level) {
        this.últimaVez = level.getGameTime();
        this.setChanged();
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput dados) {
        super.loadAdditional(dados);
        this.últimaVez = dados.getLongOr("LastUsedTime", 0L);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput dados) {
        super.saveAdditional(dados);
        dados.putLong("LastUsedTime", this.últimaVez);
    }
}
