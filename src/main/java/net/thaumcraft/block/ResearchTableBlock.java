package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * A mesa de pesquisa: as duas mesas que as ferramentas de escrita juntaram, os metadados dois a nove do
 * {@code BlockTable} da 4.2.3.5.
 *
 * <p>São dois blocos. O principal é onde as ferramentas foram postas: guarda a tinta e a nota, e é ele que
 * desenha a mesa inteira, de trinta e dois pontos de comprimento. O outro só ocupa lugar. {@link #FACING}
 * aponta, em cada metade, para a outra. Quebrando qualquer uma, a que sobra volta a ser mesa comum, e cada
 * metade devolve a sua mesa.
 */
public class ResearchTableBlock extends BaseEntityBlock {
    public static final MapCodec<ResearchTableBlock> CODEC = simpleCodec(ResearchTableBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public enum Part implements StringRepresentable {
        MAIN, SIDE;

        @Override
        public String getSerializedName() {
            return this == MAIN ? "main" : "side";
        }
    }

    public ResearchTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.MAIN));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    /** Junta a mesa em {@code pos} com a vizinha naquele rumo. */
    public static void form(Level level, BlockPos pos, Direction towardsPartner) {
        BlockState main = TCBlocks.RESEARCH_TABLE.defaultBlockState().setValue(FACING, towardsPartner).setValue(PART, Part.MAIN);
        BlockState side = TCBlocks.RESEARCH_TABLE.defaultBlockState().setValue(FACING, towardsPartner.getOpposite())
                .setValue(PART, Part.SIDE);
        level.setBlock(pos.relative(towardsPartner), side, 3);
        level.setBlock(pos, main, 3);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // quem desenha é o desenhista da metade principal, como o TileResearchTableRenderer do original
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? new ResearchTableBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return state.getValue(PART) == Part.MAIN
                ? createTickerHelper(type, TCBlockEntities.RESEARCH_TABLE, ResearchTableBlockEntity::tick) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos main = state.getValue(PART) == Part.MAIN ? pos : pos.relative(state.getValue(FACING));
        if (level.getBlockEntity(main) instanceof ResearchTableBlockEntity table) player.openMenu(table);
        return InteractionResult.CONSUME;
    }

    /** Sem a outra metade, a que sobra volta a ser mesa comum, como o {@code onNeighborBlockChange} original. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        BlockPos partner = pos.relative(state.getValue(FACING));
        BlockState other = level.getBlockState(partner);
        if (other.is(this) && other.getValue(FACING) == state.getValue(FACING).getOpposite()) {
            level.setBlock(partner, TCBlocks.TABLE.defaultBlockState(), 3);
        }
    }
}
