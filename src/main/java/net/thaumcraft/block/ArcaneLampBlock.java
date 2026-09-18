package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import net.thaumcraft.block.entity.ArcaneLampBlockEntity;
import net.thaumcraft.block.entity.FertilityLampBlockEntity;
import net.thaumcraft.block.entity.GrowthLampBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * As lâmpadas do {@code BlockMetalDevice} da 4.2.3.5: a arcana (7), a do crescimento (8) e a da fertilidade (13).
 *
 * <p>Presas no bloco em que foram encostadas — o {@code BlockMetalDeviceItem} guarda o lado ao contrário do
 * clicado —, caem se ele sumir. A arcana brilha quinze; as outras duas, oito apagadas e quinze com essência.
 */
public class ArcaneLampBlock extends BaseEntityBlock {
    public enum Kind { ARCANE, GROWTH, FERTILITY }

    public static final MapCodec<ArcaneLampBlock> CODEC = simpleCodec(properties -> new ArcaneLampBlock(Kind.ARCANE, properties));
    public static final MapCodec<ArcaneLampBlock> GROWTH_CODEC = simpleCodec(properties -> new ArcaneLampBlock(Kind.GROWTH, properties));
    public static final MapCodec<ArcaneLampBlock> FERTILITY_CODEC = simpleCodec(properties -> new ArcaneLampBlock(Kind.FERTILITY, properties));
    /** O lado em que a lâmpada está presa: o {@code facing} dos tiles de lâmpada. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    /** Com essência (as de crescimento e de fertilidade): o {@code charges > 0} do original. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);

    private final Kind kind;

    public ArcaneLampBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(LIT, false));
    }

    public Kind kind() {
        return this.kind;
    }

    /**
     * A luz das lâmpadas de crescimento e de fertilidade: quinze acesas, oito apagadas. (A arcana é sempre quinze;
     * o jogo calcula a luz de cada estado já no construtor do bloco, antes de o tipo existir, por isso cada uma
     * leva a sua regra no registro.)
     */
    public static int lightOf(BlockState state) {
        return state.getValue(LIT) ? 15 : 8;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return switch (this.kind) {
            case ARCANE -> CODEC;
            case GROWTH -> GROWTH_CODEC;
            case FERTILITY -> FERTILITY_CODEC;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        // o kind ainda não existe aqui (o construtor do Block chama isto antes); LIT entra em todas e a arcana
        // simplesmente não o usa
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Sem o bloco de apoio, a lâmpada cai. */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == state.getValue(FACING) && neighbour.isAir()) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isEmptyBlock(pos.relative(state.getValue(FACING)))) level.destroyBlock(pos, true);
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (this.kind) {
            case ARCANE -> new ArcaneLampBlockEntity(pos, state);
            case GROWTH -> new GrowthLampBlockEntity(pos, state);
            case FERTILITY -> new FertilityLampBlockEntity(pos, state);
        };
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return switch (this.kind) {
            case ARCANE -> createTickerHelper(type, TCBlockEntities.ARCANE_LAMP, ArcaneLampBlockEntity::tick);
            case GROWTH -> createTickerHelper(type, TCBlockEntities.GROWTH_LAMP, GrowthLampBlockEntity::tick);
            case FERTILITY -> createTickerHelper(type, TCBlockEntities.FERTILITY_LAMP, FertilityLampBlockEntity::tick);
        };
    }
}
