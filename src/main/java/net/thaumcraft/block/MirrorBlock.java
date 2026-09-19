package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.EssentiaMirrorBlockEntity;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.block.entity.MirrorBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code BlockMirror} da 4.2.3.5: o espelho mágico (os números 0 a 5) e o de essência (6 a 11), cada um virado para
 * um dos seis lados. É uma placa fina colada na face em que se clicou, sem colisão; cai se o bloco de trás sair. Ao
 * quebrar, o espelho ligado cai lembrando o par (e o par deixa de estar ligado), para poder ser posto em outro lugar.
 */
public class MirrorBlock extends BaseEntityBlock {
    public static final MapCodec<MirrorBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            com.mojang.serialization.Codec.BOOL.fieldOf("essentia").forGetter(b -> b.essentia),
            propertiesCodec()).apply(i, MirrorBlock::new));
    /** Para onde o vidro olha: a face do bloco de apoio em que ele foi posto. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private static final float W = 1.0f;
    private static final VoxelShape[] SHAPES = {
            Block.box(0, 16 - W, 0, 16, 16, 16), // olhando para baixo, no teto
            Block.box(0, 0, 0, 16, W, 16), // para cima, no chão
            Block.box(0, 0, 16 - W, 16, 16, 16), // norte
            Block.box(0, 0, 0, 16, 16, W), // sul
            Block.box(16 - W, 0, 0, 16, 16, 16), // oeste
            Block.box(0, 0, 0, W, 16, 16), // leste
    };

    public final boolean essentia;

    public MirrorBlock(boolean essentia, Properties properties) {
        super(properties);
        this.essentia = essentia;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.essentia ? new EssentiaMirrorBlockEntity(pos, state) : new MirrorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return this.essentia
                ? createTickerHelper(type, TCBlockEntities.ESSENTIA_MIRROR, EssentiaMirrorBlockEntity::serverTick)
                : createTickerHelper(type, TCBlockEntities.MIRROR, MirrorBlockEntity::serverTick);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** O {@code onEntityCollidedWithBlock} do original vale para a casa inteira, não só para a placa. */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    /** O {@code canPlaceBlockOnSide}: o bloco de trás tem de ter a face sólida. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
    }

    /** O item do espelho; o ligado leva junto onde está o par (o número 1 ou 7 do original). */
    public ItemStack dropFor(LinkedMirrorBlockEntity mirror, Level level) {
        ItemStack drop = new ItemStack(this);
        if (mirror.linked) {
            CompoundTag tag = new CompoundTag();
            mirror.writeLinkTo(tag, level.dimension());
            drop.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
            mirror.invalidateLink();
        }
        return drop;
    }

    /** O {@code getDrops}: o espelho, lembrando o par se estava ligado. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LinkedMirrorBlockEntity mirror) {
            drops.add(this.dropFor(mirror, params.getLevel()));
        } else {
            drops.add(new ItemStack(this));
        }
        return drops;
    }

    /** O {@code onBlockHarvested}: o original solta o espelho mesmo no criativo. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server && player.isCreative()) {
            Block.dropResources(state, server, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** O {@code onEntityCollidedWithBlock}: o item que encosta no espelho mágico passa para o par. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (this.essentia || level.isClientSide() || !(entity instanceof ItemEntity item) || item.isRemoved() || item.isOnPortalCooldown()) return;
        if (level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) mirror.transport(item);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }
}
