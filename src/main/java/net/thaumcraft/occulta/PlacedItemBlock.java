package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Item Posto</b>: a {@code BlockPlacedItem} do Witchery.
 *
 * <p>É um bloco que <b>não é nada</b> — não tem forma, não tem textura, não se fabrica e não aparece em
 * aba nenhuma. O que ele faz é guardar <b>uma coisa deitada no chão</b>, parada, virada para onde quem a pôs
 * estava olhando, e desenhá-la ali.
 *
 * <p>Ele existe por uma razão só: <b>o altar precisa de saber o que está em cima dele</b>. Um item largado
 * no chão é uma entidade que rola, que se junta a outra igual, que o jogo apanha quando alguém passa por
 * perto e que some ao fim de cinco minutos. Nada disso serve para um altar que conta o que tem em cima de
 * cada pedra. O Item Posto é a resposta: a coisa vira <b>bloco</b>, e um bloco fica onde o puseram.
 *
 * <h2>O que ele dá ao altar</h2>
 *
 * <ul>
 *   <li>a <b>Arthana</b> em cima dele soma <b>um ao alcance</b> — dezesseis blocos viram trinta e dois;</li>
 *   <li>e no original há mais dois que ainda não foram portados: o <b>Ramo Místico</b>, que soma ao poder de
 *       encanto, e o <b>Pentáculo de Kobolditas</b>, que <b>dobra</b> a velocidade de recarga.</li>
 * </ul>
 *
 * <h2>Como ele se põe e se tira</h2>
 *
 * <p>Com a Arthana na mão, clicando no <b>topo de uma pedra de altar</b> com ar por cima. A faca sai do
 * inventário e fica deitada ali. Partindo o bloco, ela volta — a não ser no <b>criativo</b>, onde o original
 * acende um número que faz o bloco não largar nada, para que quem está construindo não encha o chão de
 * facas.
 */
public class PlacedItemBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<PlacedItemBlock> CODEC = simpleCodec(PlacedItemBlock::new);

    /** Uma lasca de bloco: o que mal chega para a coisa não ficar enterrada no chão. */
    private static final VoxelShape FORMA = Block.box(3.2, 0.0, 3.2, 12.8, 0.8, 12.8);

    public PlacedItemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING);
    }

    /** Ele não se desenha: o que se vê é a coisa, pelo {@link net.thaumcraft.occulta.client.PlacedItemRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /** E não tranca o passo: passa-se por cima dele como se não estivesse lá. */
    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                           CollisionContext quem) {
        return Shapes.empty();
    }

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
        return new PlacedItemBlockEntity(onde, feitio);
    }

    /** O botão do meio tira o que está posto, e não o bloco — que não é nada. */
    @Override
    public ItemStack getCloneItemStack(LevelReader mundo, BlockPos onde, BlockState feitio,
                                       boolean incluirDados) {
        if (mundo.getBlockEntity(onde) instanceof PlacedItemBlockEntity alma && !alma.oquê().isEmpty()) {
            return alma.oquê().copy();
        }
        return new ItemStack(OccultaItems.ARTHANA);
    }

    /**
     * <b>Partido, ele devolve o que guardava</b> — menos no criativo.
     *
     * <p>O original acende o número oito do bloco quando quem o parte está no criativo, e o
     * {@code getDrops} dele olha esse número antes de largar o que quer que seja. Aqui a pergunta é feita
     * direto, que dá no mesmo e poupa um feitio inteiro só para dizer «foi o criativo».
     */
    @Override
    public BlockState playerWillDestroy(Level mundo, BlockPos onde, BlockState feitio, Player quem) {
        if (mundo instanceof ServerLevel level && !quem.getAbilities().instabuild
                && level.getBlockEntity(onde) instanceof PlacedItemBlockEntity alma) {
            ItemStack oquê = alma.oquê();
            if (!oquê.isEmpty()) {
                Block.popResource(level, onde, oquê.copy());
                alma.põe(ItemStack.EMPTY);
            }
        }
        return super.playerWillDestroy(mundo, onde, feitio, quem);
    }

    /**
     * <b>Põe uma coisa no mundo.</b>
     *
     * <p>Virada para o mesmo lado que quem a pôs estava olhando, de modo que uma faca deitada num altar
     * aponta sempre para quem a deitou.
     */
    public static void põe(Level mundo, BlockPos onde, ItemStack oquê, @Nullable Player quem) {
        Direction rumo = quem == null ? Direction.NORTH : quem.getDirection();
        mundo.setBlock(onde, OccultaBlocks.PLACED_ITEM.defaultBlockState().setValue(FACING, rumo),
                Block.UPDATE_ALL);
        if (mundo.getBlockEntity(onde) instanceof PlacedItemBlockEntity alma) {
            alma.põe(oquê.copy());
        }
    }
}
