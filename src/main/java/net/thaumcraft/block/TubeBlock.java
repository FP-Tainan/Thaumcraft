package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo de essência: um cano de latão fino, que se estica sozinho até quem quiser se ligar a ele.
 *
 * <p>Como a cerca e o vidro de vidraça, o tubo tem seis chaves de estado — uma por lado — e o desenho
 * segue as chaves. Um lado se liga quando o tubo o tem aberto <em>e</em> o vizinho daquele lado é coisa de
 * encanar que aceita a ligação.
 */
public class TubeBlock extends BaseEntityBlock {
    public static final MapCodec<TubeBlock> CODEC = simpleCodec(TubeBlock::new);

    /** O miolo do tubo, do qual saem os braços. */
    private static final VoxelShape CORE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    /** Um braço por lado, guardado na ordem de {@link Direction#get3DDataValue()}. */
    private static final VoxelShape[] ARMS = {
            Block.box(6.5, 0.0, 6.5, 9.5, 5.0, 9.5),   // baixo
            Block.box(6.5, 11.0, 6.5, 9.5, 16.0, 9.5), // cima
            Block.box(6.5, 6.5, 0.0, 9.5, 9.5, 5.0),   // norte
            Block.box(6.5, 6.5, 11.0, 9.5, 9.5, 16.0), // sul
            Block.box(0.0, 6.5, 6.5, 5.0, 9.5, 9.5),   // oeste
            Block.box(11.0, 6.5, 6.5, 16.0, 9.5, 9.5), // leste
    };

    public TubeBlock(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty side : PipeBlock.PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(side, false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction dir : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir))) {
                shape = Shapes.or(shape, ARMS[dir.get3DDataValue()]);
            }
        }
        return shape;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connect(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                     net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                     Direction dir, BlockPos neighbourPos, BlockState neighbourState,
                                     net.minecraft.util.RandomSource random) {
        return connect(state, level, pos);
    }

    /**
     * Refaz as seis chaves de estado a partir do que há em volta.
     *
     * <p>É chamado na hora de pôr o tubo e sempre que um vizinho muda; e também pelo próprio tubo quando a
     * varinha abre ou fecha um lado dele.
     */
    public static BlockState connect(BlockState state, BlockGetter level, BlockPos pos) {
        TubeBlockEntity tube = level.getBlockEntity(pos) instanceof TubeBlockEntity found ? found : null;
        for (Direction dir : Direction.values()) {
            boolean open = tube == null || tube.isOpen(dir);
            boolean fits = open && level.getBlockEntity(pos.relative(dir)) instanceof EssentiaTransport side
                    && side.isConnectable(dir.getOpposite());
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), fits);
        }
        return state;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.TUBE, TubeBlockEntity::tick);
    }
}
