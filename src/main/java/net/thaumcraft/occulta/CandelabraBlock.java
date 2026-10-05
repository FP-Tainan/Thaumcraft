package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Candelabro</b>: a {@code BlockCandelabra} do Witchery.
 *
 * <p>Cinco velas num pé de ferro — quatro em volta e uma no meio, mais alta que as outras. Ele <b>arde
 * sempre</b>, dá luz cheia, e nunca se apaga: não há feitio aceso nem apagado, há o candelabro.
 *
 * <h2>Para que ele serve</h2>
 *
 * <p>Posto em cima de um <b>Altar da Bruxa</b>, ele soma <b>dois à velocidade</b> com que o altar se enche —
 * o dobro do que uma tocha dá. E é o mesmo lugar: o altar conta <b>uma luz só</b>, e quem tiver um
 * candelabro não ganha nada por pôr uma tocha ao lado.
 *
 * <p>É o enfeite mais barato que vale a pena. A tocha custa um pau e um carvão; o candelabro custa três
 * tochas, três ferros e uma <b>Pedra Afinada</b> — e rende o dobro.
 *
 * <h2>E ele cai</h2>
 *
 * <p>Precisa de chão por baixo, e cai no instante em que o tirarem. O original pede que o bloco de baixo
 * <b>tranque o passo e tape a luz</b>; aqui se pede que a <b>face de cima dele seja firme</b>, que é o que o
 * jogo de hoje pergunta às tochas e dá o mesmo resultado nos blocos todos que existiam em 2014.
 */
public class CandelabraBlock extends Block implements EntityBlock {
    public static final MapCodec<CandelabraBlock> CODEC = simpleCodec(CandelabraBlock::new);

    /** O que ele ocupa: do décimo ao nono décimo, de alto a baixo. */
    private static final VoxelShape FORMA = Block.box(1.6, 0.0, 1.6, 14.4, 16.0, 14.4);

    /** Quantas das cinco chamas fumegam a cada batida: três em cada quatro. */
    public static final int FUMAÇA = 4;

    public CandelabraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Ele se desenha sozinho, pelo {@link net.thaumcraft.occulta.client.CandelabraRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /** Ele pousa em chão firme, e só nele. */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader mundo, BlockPos onde) {
        BlockPos chão = onde.below();
        return mundo.getBlockState(chão).isFaceSturdy(mundo, chão, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState feitio, LevelReader mundo, ScheduledTickAccess relógio,
                                     BlockPos onde, Direction lado, BlockPos vizinho, BlockState doVizinho,
                                     RandomSource sorte) {
        if (lado == Direction.DOWN && !feitio.canSurvive(mundo, onde)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(feitio, mundo, relógio, onde, lado, vizinho, doVizinho, sorte);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new CandelabraBlockEntity(onde, feitio);
    }

    /**
     * As cinco chamas, e o fumo de cada uma.
     *
     * <p>A do meio sobe mais alto — um vigésimo de bloco acima do topo —, e as quatro de fora ficam um
     * décimo abaixo dela. Cada uma só aparece em <b>três de cada quatro</b> batidas, que é o que dá ao fogo
     * do original aquele piscar irregular de vela de verdade.
     */
    @Override
    public void animateTick(BlockState feitio, Level mundo, BlockPos onde, RandomSource sorte) {
        double meio = onde.getY() + 1.05;
        double fora = onde.getY() + 0.9;
        chama(mundo, sorte, onde.getX() + 0.5, meio, onde.getZ() + 0.5);
        chama(mundo, sorte, onde.getX() + 0.8, fora, onde.getZ() + 0.5);
        chama(mundo, sorte, onde.getX() + 0.2, fora, onde.getZ() + 0.5);
        chama(mundo, sorte, onde.getX() + 0.5, fora, onde.getZ() + 0.8);
        chama(mundo, sorte, onde.getX() + 0.5, fora, onde.getZ() + 0.2);
    }

    private static void chama(Level mundo, RandomSource sorte, double x, double y, double z) {
        if (sorte.nextInt(FUMAÇA) == 0) return;
        mundo.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        mundo.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
    }
}
