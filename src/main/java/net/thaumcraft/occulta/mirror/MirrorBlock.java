package net.thaumcraft.occulta.mirror;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O Espelho: o {@code BlockMirror} do Witchery.
 *
 * <p>São <b>dois blocos</b>, um em cima do outro, pregados numa parede — e a alma mora no de cima, que é onde o
 * original guarda tudo. A figura é a mesma nas duas metades, virada de cabeça para baixo na de baixo: é assim que
 * a moldura oval se fecha.
 *
 * <p>Quem passa <b>à frente dele, olhando para ele</b>, atravessa. Para onde depende do que o espelho é:
 *
 * <ul>
 *   <li>um espelho <b>habitado</b> — recém-feito — leva ao {@linkplain MirrorWorld Mundo do Espelho}, a uma cela
 *       só de quem entrou, e acorda lá o {@linkplain ReflectionEntity Reflexo} que a guarda;</li>
 *   <li>morto o Reflexo, o espelho fica <b>vazado</b>, e passa a ser ponte: dois espelhos de costas um para o
 *       outro furam a parede entre eles, e dois em prumo furam o chão.</li>
 * </ul>
 *
 * <p>Há dois destes blocos, como no original: o que se faz na bancada, que se quebra, e o <b>selado</b>, que é o
 * da cela e não se quebra nunca.
 *
 * <p><b>Uma coisa do original que aqui vai certa:</b> lá a caixa que dispara a travessia era escrita à mão para
 * cada lado, e a do lado leste ficou com o número trocado — {@code maxZ} onde devia ser {@code maxX}. Aqui a
 * caixa sai de uma conta só, e por isso os quatro lados ficam iguais.
 */
public class MirrorBlock extends BaseEntityBlock {
    /** Para onde o vidro olha. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Qual das duas metades é esta. */
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /** Quanto o vidro entra no bloco: os {@code 0.15} do original. */
    private static final double PANE = 0.15;
    /** E a caixa que dispara a travessia, um pouco mais funda: os {@code 0.32}. */
    private static final double TRIGGER = 0.32;

    /** De quantas em quantas batidas se olha para quem está à frente: o {@code ticksExisted % 5 == 1}. */
    public static final int EVERY = 5;

    private final boolean sealed;

    public MirrorBlock(Properties properties, boolean sealed) {
        super(properties);
        this.sealed = sealed;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    /** Se este é o espelho selado da cela, que não se quebra e não vira ponte. */
    public boolean sealed() {
        return this.sealed;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new MirrorBlock(properties, this.sealed));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // quem o desenha é o MirrorRenderer, como no original
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return slab(state.getValue(FACING), PANE);
    }

    /** O vidro é uma lâmina encostada na parede, do lado contrário ao que ele olha. */
    private static VoxelShape slab(Direction facing, double thick) {
        double fundo = (1.0 - thick) * 16.0;
        double frente = thick * 16.0;
        return switch (facing) {
            case NORTH -> Block.box(0.0, 0.0, fundo, 16.0, 16.0, 16.0);
            case SOUTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, frente);
            case WEST -> Block.box(fundo, 0.0, 0.0, 16.0, 16.0, 16.0);
            default -> Block.box(0.0, 0.0, 0.0, frente, 16.0, 16.0);
        };
    }

    /** A caixa que dispara a travessia, naquele lugar. */
    public static AABB trigger(BlockPos pos, Direction facing) {
        return slab(facing, TRIGGER).bounds().move(pos);
    }

    /** A metade de cima daquele espelho, seja esta qual for; nada, se o par não está de pé. */
    public static @Nullable BlockPos top(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return pos;
        BlockPos acima = pos.above();
        BlockState outro = level.getBlockState(acima);
        return outro.getBlock() == state.getBlock() && outro.getValue(HALF) == DoubleBlockHalf.UPPER ? acima : null;
    }

    /**
     * As <b>duas</b> metades têm alma, como no original — mas só a de cima guarda a ligação.
     *
     * <p>A de baixo serve para duas coisas: contar quem lhe fica diante, e ter desenhista. Sem ela a moldura de
     * baixo não se desenhava, e o oval ficava pela metade.
     */
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirrorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return createTickerHelper(type, net.thaumcraft.occulta.OccultaBlocks.WITCH_MIRROR_ENTITY,
                (mundo, onde, feitio, alma) -> alma.tick());
    }

    /**
     * O {@code onEntityWalking} do original: de cinco em cinco batidas, quem estiver na caixa do vidro e
     * <b>olhando para ele</b> atravessa.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel server)) return;
        if (quem.tickCount % EVERY != 1) return;
        if (!MirrorTravel.transportable(quem)) return;
        MirrorTravel.step(server, pos, state, quem);
    }

    /** O clique: é ele que chama a cara do espelho, como no original. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player quem,
                                               BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (this.sealed) return InteractionResult.SUCCESS;
        BlockPos alto = top(level, pos, state);
        if (alto == null) return InteractionResult.SUCCESS;
        if (server.getBlockEntity(alto) instanceof MirrorBlockEntity espelho) espelho.askTheMirror(quem);
        return InteractionResult.SUCCESS;
    }

    /** Sem a outra metade, esta cai: o {@code onNeighborBlockChange} do original. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block vizinho,
                                   @Nullable Orientation orientation, boolean moved) {
        boolean alto = state.getValue(HALF) == DoubleBlockHalf.UPPER;
        BlockState par = level.getBlockState(alto ? pos.below() : pos.above());
        boolean temPar = par.getBlock() == this
                && par.getValue(HALF) == (alto ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
        if (temPar) return;
        if (alto && level instanceof ServerLevel server && !this.sealed) drop(server, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    /**
     * O {@code getDrops} e o {@code onBlockHarvested} do original juntos: quebrado, o espelho <b>volta em item
     * com a ligação dentro</b>, e a outra metade vai-se com ele. No criativo não larga nada.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player quem) {
        if (level instanceof ServerLevel server && !this.sealed) {
            BlockPos alto = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos : pos.above();
            if (!quem.hasInfiniteMaterials()) drop(server, alto);
            BlockPos outra = pos.equals(alto) ? alto.below() : alto;
            if (level.getBlockState(outra).getBlock() == this) {
                level.setBlock(outra, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        return super.playerWillDestroy(level, pos, state, quem);
    }

    /** O item do espelho, com a ligação que a alma guardava. */
    private static void drop(ServerLevel level, BlockPos alto) {
        ItemStack item = new ItemStack(net.thaumcraft.occulta.OccultaItems.WITCH_MIRROR);
        if (level.getBlockEntity(alto) instanceof MirrorBlockEntity espelho) {
            espelho.writeToItem(item);
            espelho.unlinkOther();
        }
        Block.popResource(level, alto, item);
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }
}
