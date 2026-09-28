package net.thaumcraft.arcana;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Mesa de Inscrição</b>: o {@code BlockInscriptionTable} do Ars Magica 2.
 *
 * <p>É a bancada do arcanista, e é o que separa o ramo de uma caixa de brinquedos: sem ela as peças de feitiço
 * são palavras soltas, e é aqui que elas viram frase.
 */
public class InscriptionTableBlock extends BaseEntityBlock {
    public static final MapCodec<InscriptionTableBlock> CODEC = simpleCodec(InscriptionTableBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public InscriptionTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return this.defaultBlockState()
                .setValue(FACING, contexto.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState state) {
        return new InscriptionTableBlockEntity(onde, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(onde) instanceof InscriptionTableBlockEntity mesa) {
            quem.openMenu(mesa);
        }
        return InteractionResult.SUCCESS;
    }

    /** Quebrada, ela devolve o que estiver dentro — menos o feitiço, que se escreve outra vez. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
                                               BlockPos onde, boolean mexeu) {
        if (level.getBlockEntity(onde) instanceof InscriptionTableBlockEntity mesa) {
            mesa.removeItemNoUpdate(InscriptionTableBlockEntity.RESULT);
            net.minecraft.world.Containers.dropContents(level, onde, mesa);
        }
        super.affectNeighborsAfterRemoval(state, level, onde, mexeu);
    }
}
