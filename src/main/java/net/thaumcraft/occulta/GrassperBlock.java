package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Apanha-Erva</b>: o {@code BlockGrassper} do Witchery.
 *
 * <p>Uma planta que <b>segura o que lhe dão</b>. Clicada de mão cheia, ela tira <b>uma</b> coisa da mão e
 * fica com ela à vista; clicada outra vez, larga o que tinha no chão.
 *
 * <p>Parece um enfeite e não é. O ofício inteiro o usa como <b>peça de receita</b>: há mutações que pedem
 * quatro Apanha-Ervas nas diagonais, cada um com a coisa certa na boca. Um Apanha-Erva com uma pérola do fim
 * não é uma planta bonita — é meia receita de uma <b>Sarça do Fim</b>.
 *
 * <p>E ele <b>não se desenha como bloco</b>: o original devolve o tipo de desenho −1 e quem o põe no mundo é
 * um desenhista de alma, com dez caixas de folha e caule e a coisa que ele segura girando devagar por cima.
 */
public class GrassperBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<GrassperBlock> CODEC = simpleCodec(GrassperBlock::new);

    /** Ele é raso: meia casa de alto, e sem nada que estorve a passagem. */
    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public GrassperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState().setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    /** Quem o desenha é a alma dele, como no original. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader level, BlockPos onde) {
        return !level.getBlockState(onde.below()).isAir();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new GrassperBlockEntity(onde, feitio);
    }

    /**
     * <b>Dá e tira</b>: cheio, larga; vazio, pega uma.
     *
     * <p>É a ordem do original, e ela importa: quem clica num Apanha-Erva cheio <b>sempre</b> esvazia,
     * mesmo trazendo outra coisa na mão. Não há como trocar o que ele segura sem primeiro o esvaziar.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(onde) instanceof GrassperBlockEntity alma)) {
            return InteractionResult.PASS;
        }

        if (!alma.naBoca().isEmpty()) {
            Block.popResource(level, onde.above(), alma.tira());
            return InteractionResult.SUCCESS;
        }

        ItemStack naMão = quem.getMainHandItem();
        if (naMão.isEmpty()) return InteractionResult.SUCCESS;
        alma.põe(naMão.split(1));
        return InteractionResult.SUCCESS;
    }

    /** O que este Apanha-Erva segura, se houver um aqui. */
    public static ItemStack oQueSegura(LevelReader level, BlockPos onde) {
        return level.getBlockEntity(onde) instanceof GrassperBlockEntity alma
                ? alma.naBoca() : ItemStack.EMPTY;
    }

    /** E se ele segura <b>aquilo</b>: a conta que as mutações fazem. */
    public static boolean segura(LevelReader level, BlockPos onde, net.minecraft.world.item.Item oquê) {
        if (!level.getBlockState(onde).is(OccultaBlocks.GRASSPER)) return false;
        return oQueSegura(level, onde).is(oquê);
    }
}
