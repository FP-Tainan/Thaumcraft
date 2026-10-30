package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * O <b>nenúfar de bruxa</b>: o {@code BlockWitchLilyPad} do Witchery.
 *
 * <p>É o nenúfar do jogo que <b>se alastra sozinho</b>. Quem o põe é o cozimento de <b>nenufarização</b>, e a
 * força do cozimento diz quantas vezes ele ainda vai se alastrar: a cada vinte batidas ele escolhe uma casa
 * em volta e, se houver água lá, põe outro nenúfar nela. O novo nasce com <b>uma vez a menos</b> que o pai, e
 * por isso a coisa tem fim.
 *
 * <p>E há um detalhe do original que é dele e vale guardar: o pai só <b>gasta</b> uma das vezes dele com a
 * chance de um sobre o que lhe resta. Com três pela frente ele gasta uma em cada três; com uma só, gasta
 * sempre. Na prática o pai aguenta mais do que a conta diria, e o tapete sai mais largo de uma nascente só.
 *
 * <p>As doze direções que ele sorteia também são dele, e não são iguais: as <b>quinas contam duas vezes</b> e
 * os lados uma. É por isso que o tapete sai redondo e não em cruz.
 *
 * <p>Ele <b>larga um nenúfar do jogo uma vez em quatro</b>, e é o nenúfar do jogo que se apanha com a roda do
 * meio — nunca este, que não se planta à mão.
 */
public class WitchLilyPadBlock extends LilyPadBlock {
    /** Quantas vezes ele ainda se alastra: o {@code meta >> 2 & 3} do original. */
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 3);

    /** De quantas em quantas batidas ele tenta: vinte, que é um segundo. */
    public static final int TENTA_A_CADA = 20;

    /** Uma em quatro, ele larga um nenúfar do jogo. */
    public static final int LARGA_UMA_EM = 4;

    /**
     * As doze direções do sorteio, na conta do original: as quatro quinas <b>duas vezes</b> cada e os quatro
     * lados uma. Oito das doze são quinas, e é daí que vem o feitio redondo do tapete.
     */
    private static final int[][] RUMOS = {
            {-1, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 1}, {0, -1},
            {0, 1}, {1, -1}, {1, -1}, {1, 0}, {1, 1}, {1, 1},
    };

    public WitchLilyPadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(GROWTH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GROWTH);
    }

    @Override
    protected void onPlace(BlockState feitio, Level level, BlockPos onde, BlockState antes, boolean mexeu) {
        super.onPlace(feitio, level, onde, antes, mexeu);
        level.scheduleTick(onde, this, TENTA_A_CADA);
    }

    @Override
    protected void tick(BlockState feitio, ServerLevel level, BlockPos onde, RandomSource sorte) {
        int restam = feitio.getValue(GROWTH);
        if (restam <= 0) return;

        int[] rumo = RUMOS[sorte.nextInt(RUMOS.length)];
        BlockPos lá = onde.offset(rumo[0], 0, rumo[1]);
        if (this.canSurvive(feitio, level, lá) && level.getBlockState(lá).isAir()) {
            // o pai só gasta uma das vezes dele de vez em quando, e é por isso que o tapete sai largo
            if (sorte.nextInt(restam) == 0) {
                level.setBlock(onde, feitio.setValue(GROWTH, restam - 1), Block.UPDATE_ALL);
            }
            level.setBlock(lá, this.defaultBlockState().setValue(GROWTH, restam - 1), Block.UPDATE_ALL);
        }
        level.scheduleTick(onde, this, TENTA_A_CADA);
    }

    /** Ele <b>não se larga a si mesmo</b>: o que cai é um nenúfar do jogo, e só uma vez em quatro. */
    public static net.minecraft.world.item.ItemStack oQueLarga(RandomSource sorte) {
        return sorte.nextInt(LARGA_UMA_EM) == 0
                ? new net.minecraft.world.item.ItemStack(Blocks.LILY_PAD)
                : net.minecraft.world.item.ItemStack.EMPTY;
    }
}
