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
 * <p>Por fora é o mesmo tubo, sempre com a junta de latão, e com uma roda de registro de lado. Quem
 * desenha a roda é o {@link net.thaumcraft.client.render.TubeValveRenderer}, porque ela gira uma volta e
 * meia e afunda conforme a válvula fecha — coisa que arquivo de modelo não faz.
 *
 * <p>Clicar nela com a mão abre e fecha; a varinha gira a roda para outro lado. O lado da roda é
 * escolhido na hora de pôr a válvula — ela nasce virada para quem a pôs — e nunca conecta.
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

    /**
     * O clique com a mão abre e fecha a válvula.
     *
     * <p>Menos com varinha ou com cano na mão: a varinha tem o que fazer aqui (girar a roda), e o cano é
     * para ser posto do lado. É exatamente a exceção que o {@code BlockTube} do original abre.
     */
    @Override
    protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,
            BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (stack.getItem() instanceof net.thaumcraft.item.WandItem
                || stack.getItem() instanceof net.minecraft.world.item.BlockItem block
                && block.getBlock() instanceof TubeBlock) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        return this.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TubeValveBlockEntity valve)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!level.isClientSide()) valve.toggleByHand(level, pos);
        return net.minecraft.world.InteractionResult.SUCCESS;
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
