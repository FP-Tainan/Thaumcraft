package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Baú de Sanguessugas</b>: a {@code BlockLeechChest} do Witchery.
 *
 * <p>Por fora é um baú com <b>sacos de sangue</b> na frente, e a tampa abre em <b>quatro quartos</b> que se
 * afastam uns dos outros em vez de dobrar numa dobradiça. Por dentro é um baú comum, de vinte e sete
 * lugares.
 *
 * <p>O que ele tem a mais é a <b>memória</b>: ele anota o nome de quem o abre, até três, e mostra um saco por
 * nome. Um <b>Frasco de Vínculo</b> usado nele sai com um desses nomes.
 *
 * <p>É a armadilha mais paciente deste mod. Não fere, não prende, não some: fica ali, parecendo um baú
 * interessante, e espera que alguém tenha curiosidade. E é honesta — os sacos ficam à vista, e quem souber
 * lê neles quantas pessoas já caíram.
 */
public class LeechChestBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<LeechChestBlock> CODEC = simpleCodec(LeechChestBlock::new);

    /** Ele é um pouco menor do que a casa, como o baú do jogo. */
    private static final VoxelShape FORMA = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public LeechChestBlock(Properties properties) {
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

    /** Quem o desenha é a alma dele, com a tampa de quatro quartos. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new LeechChestBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState feitio,
                                                                            BlockEntityType<T> tipo) {
        if (!level.isClientSide() || tipo != OccultaBlocks.LEECH_CHEST_ENTITY) return null;
        return (mundo, onde, feitio2, alma) -> {
            if (alma instanceof LeechChestBlockEntity baú) LeechChestBlockEntity.tick(mundo, onde, feitio2, baú);
        };
    }

    /**
     * <b>Abrir é deixar o nome.</b>
     *
     * <p>A ordem é a do original: ele anota <b>antes</b> de abrir. Quem desistir no meio do caminho já
     * deixou o nome — e é aí que a armadilha pega.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(onde) instanceof LeechChestBlockEntity baú)) {
            return InteractionResult.PASS;
        }
        baú.anota(quem);
        quem.openMenu(baú);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState feitio, Level level, BlockPos onde, int qual, int quanto) {
        super.triggerEvent(feitio, level, onde, qual, quanto);
        BlockEntity alma = level.getBlockEntity(onde);
        return alma != null && alma.triggerEvent(qual, quanto);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState feitio) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState feitio, Level level, BlockPos onde, Direction lado) {
        return net.minecraft.world.inventory.AbstractContainerMenu
                .getRedstoneSignalFromBlockEntity(level.getBlockEntity(onde));
    }
}
