package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O <b>Ovo do Infinito</b>: a {@code BlockInfinityEgg} do Witchery.
 *
 * <p>Por fora é um Ovo de Dragão. Por dentro é o avesso dele: <b>ele não foge</b>.
 *
 * <p>Ele <b>não estende o Ovo de Dragão</b> — estende o que ele estende, que é o bloco que cai. A razão é
 * de carpintaria: o Ovo de Dragão de hoje tranca o molde dele a si próprio, e um bloco que o estenda não
 * consegue dar o seu. O que se herdava dele era a forma e a queda, e as duas estão aqui à mão.
 *
 * <p>O ovo do jogo é uma piada — bate-se nele e ele salta para outro lugar, e quem o quiser tem de o
 * empurrar com pistões ou partir o chão por baixo. Este fica onde o puseram, batam nele ou não. O original
 * consegue isso escrevendo duas funções vazias por cima das do ovo: a do clique e a da pancada.
 *
 * <h2>O que ele faz</h2>
 *
 * <p>Posto perto de um <b>Altar da Bruxa</b>, ele vale <b>mil</b> de natureza — quatro vezes o ovo de
 * dragão, que já é o que mais vale de longe. E posto <b>em cima</b> de uma das seis pedras, ele
 * <b>multiplica por dez</b> o teto e a velocidade de recarga do altar inteiro.
 *
 * <p>Dez. Não é um enfeite como a caveira ou o candelabro: é o fim da escala. Um altar com um Ovo do
 * Infinito em cima deixa de ter contas que valha a pena fazer.
 *
 * <p>E ele <b>não se fabrica</b>. O original não lhe dá receita nenhuma, nem rito, nem despojo: ele existe
 * na aba do criativo e mais nada. É um objeto de quem constrói mundos, e não de quem joga neles.
 */
public class InfinityEggBlock extends FallingBlock {
    public static final MapCodec<InfinityEggBlock> CODEC = simpleCodec(InfinityEggBlock::new);

    /** Por quanto ele multiplica o altar. */
    public static final int VEZES = 10;

    /** E quanto ele vale de natureza. */
    public static final int VALE = 1000;

    /** A forma do Ovo de Dragão, que é a dele: uma coluna de catorze por dezesseis. */
    private static final net.minecraft.world.phys.shapes.VoxelShape FORMA = Block.column(14.0, 0.0, 16.0);

    public InfinityEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState feitio,
                                                                  net.minecraft.world.level.BlockGetter mundo,
                                                                  BlockPos onde,
                                                                  net.minecraft.world.phys.shapes.CollisionContext quem) {
        return FORMA;
    }

    /** E ele cai com um atraso, como o do jogo. */
    @Override
    protected int getDelayAfterPlace() {
        return 5;
    }

    /** A cor do pó que ele larga ao cair: o preto do Ovo de Dragão. */
    @Override
    public int getDustColor(BlockState feitio, net.minecraft.world.level.BlockGetter mundo, BlockPos onde) {
        return 0xFF000000;
    }

    @Override
    protected boolean isPathfindable(BlockState feitio,
                                     net.minecraft.world.level.pathfinder.PathComputationType como) {
        return false;
    }

    @Override
    public MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    /** Clicando nele não acontece nada: as duas funções vazias do original. */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level mundo, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState feitio, Level mundo, BlockPos onde, Player quem) {
    }
}
