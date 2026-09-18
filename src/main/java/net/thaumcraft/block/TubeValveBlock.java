package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.thaumcraft.block.entity.TubeValveBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A válvula do cano.
 *
 * <p>Por fora é o mesmo tubo, com o miolo de outra cor e um manípulo de lado. Quem o desenha é o
 * {@link net.thaumcraft.client.render.TubeValveRenderer}, porque o manípulo gira e rosqueia para dentro
 * conforme abre e fecha — coisa que arquivo de modelo não faz.
 *
 * <p>O lado para onde o manípulo aponta é escolhido na hora de pôr a válvula: ela nasce com ele virado
 * para quem a pôs, que é o lado por onde se vai batê-la com a varinha. Esse lado nunca conecta.
 */
public class TubeValveBlock extends TubeBlock {
    public static final MapCodec<TubeValveBlock> CODEC = simpleCodec(TubeValveBlock::new);

    /** Para onde o manípulo aponta. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public TubeValveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        // o manípulo nasce virado para quem pôs a válvula, que é de onde se vai batê-la
        return state.setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeValveBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.TUBE_VALVE, TubeValveBlockEntity::tick);
    }
}
