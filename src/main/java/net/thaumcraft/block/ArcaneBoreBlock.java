package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * A broca arcana: o número 5 do {@code BlockWoodenDevice} da 4.2.3.5, com o {@code TileArcaneBore}. Só vai em cima
 * ou embaixo de uma base; o bico cava para o lado em que o jogador estava ao pô-la (como o pistão), e a varinha o
 * vira para a face batida. A mão abre a tela do foco e da picareta. Sem a base, cai.
 */
public class ArcaneBoreBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneBoreBlock> CODEC = simpleCodec(ArcaneBoreBlock::new);
    /** O {@code orientation}: para onde cava. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    /** O {@code baseOrientation}: a face da base em que a broca foi posta (a base fica do lado oposto). */
    public static final EnumProperty<Direction> BASE = EnumProperty.create("base", Direction.class, Direction.UP, Direction.DOWN);

    public ArcaneBoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(BASE, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BASE);
    }

    /** O {@code canPlaceItemBlockOnSide}: só na face de cima ou de baixo de uma base. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (side.getAxis() != Direction.Axis.Y) return null;
        BlockPos clicked = context.getClickedPos().relative(side.getOpposite());
        if (!context.getLevel().getBlockState(clicked).is(TCBlocks.ARCANE_BORE_BASE)) return null;
        return this.defaultBlockState().setValue(BASE, side).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** A caixa do original: o bloco e mais um para o lado em que cava, onde o bico sai. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(FACING);
        return Block.box(d.getStepX() < 0 ? -16 : 0, d.getStepY() < 0 ? -16 : 0, d.getStepZ() < 0 ? -16 : 0,
                d.getStepX() > 0 ? 32 : 16, d.getStepY() > 0 ? 32 : 16, d.getStepZ() > 0 ? 32 : 16);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ARCANE_BORE, ArcaneBoreBlockEntity::tick);
    }

    /** O {@code onNeighborBlockChange}: sem uma base sólida do lado dela, a broca cai com o que tem dentro. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        BlockPos base = pos.relative(state.getValue(BASE).getOpposite());
        if (!level.isClientSide() && !level.getBlockState(base).is(TCBlocks.ARCANE_BORE_BASE)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) player.openMenu(bore);
        return InteractionResult.SUCCESS;
    }

}
