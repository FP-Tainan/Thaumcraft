package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.VisRelayBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

/**
 * O relé de vis: o metadado 14 do {@code BlockMetalDevice} da 4.2.3.5. Preso na face de um bloco, com o cristal
 * apontando para fora; cai se o apoio sumir. Brilha (luz dez) ligado à rede, e só um tanto (dois) sem ela. Um fragmento
 * na mão afina o cristal na cor do fragmento. Desenhado pelo {@link net.thaumcraft.client.render.VisRelayRenderer}.
 */
public class VisRelayBlock extends BaseEntityBlock {
    public static final MapCodec<VisRelayBlock> CODEC = simpleCodec(VisRelayBlock::new);
    /** A face em que foi posto: o cristal aponta para lá, e o apoio fica do lado oposto. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    /** Ligado à rede. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final float W5 = 5.0f, W11 = 11.0f;

    public VisRelayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Meio bloco encostado no apoio, com seis dezesseis avos de lado. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getOpposite()) {
            case UP -> Block.box(W5, 8, W5, W11, 16, W11);
            case DOWN -> Block.box(W5, 0, W5, W11, 8, W11);
            case EAST -> Block.box(8, W5, W5, 16, W11, W11);
            case WEST -> Block.box(0, W5, W5, 8, W11, W11);
            case SOUTH -> Block.box(W5, W5, 8, W11, W11, 16);
            case NORTH -> Block.box(W5, W5, 0, W11, W11, 8);
        };
    }

    /** Sem o apoio, o relé cai. */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && neighbour.isAir()) ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isEmptyBlock(pos.relative(state.getValue(FACING).getOpposite()))) level.destroyBlock(pos, true);
    }

    /** O fragmento na mão (sem agachar) afina o cristal. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        byte shard = shardColour(stack);
        if (shard < 0 || player.isShiftKeyDown()) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VisRelayBlockEntity relay) relay.attune(shard);
        return InteractionResult.SUCCESS;
    }

    /** O número do fragmento no original: 0 a 5 os primordiais, 6 o balanceado; -1 se não é fragmento. */
    public static byte shardColour(ItemStack stack) {
        byte index = 0;
        for (var shard : TCItems.SHARDS.values()) {
            if (stack.is(shard)) return index;
            index++;
        }
        return stack.is(TCItems.SHARD_BALANCED) ? (byte) 6 : -1;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisRelayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.VIS_RELAY, VisRelayBlockEntity::tick);
    }
}
