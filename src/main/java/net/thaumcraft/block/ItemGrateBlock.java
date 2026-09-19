package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.ItemGrateBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A grade de itens: os números 5 (aberta) e 6 (fechada) do {@code BlockMetalDevice} da 4.2.3.5. Uma chapa de grade no
 * alto do bloco que deixa passar os itens (e só os itens) enquanto aberta; fechada, é chão para tudo. A mão ou a
 * redstone abre e fecha. Um funil em cima joga por ela o que tiver, que cai do outro lado.
 */
public class ItemGrateBlock extends BaseEntityBlock {
    public static final MapCodec<ItemGrateBlock> CODEC = simpleCodec(ItemGrateBlock::new);
    /** Fechada: o número 6. */
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape PLATE = Block.box(0, 13, 0, 16, 16, 16);

    public ItemGrateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CLOSED, false).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLOSED, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGrateBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLATE;
    }

    /** Aberta, não segura os itens; fechada, segura tudo. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(CLOSED) && context instanceof EntityCollisionContext entity && entity.getEntity() instanceof ItemEntity) {
            return Shapes.empty();
        }
        return PLATE;
    }

    /** O {@code onBlockActivated}: abre ou fecha, com o som da porta. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        level.setBlock(pos, state.cycle(CLOSED), 2);
        door(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void door(Level level, BlockPos pos, @Nullable Player player) {
        level.playSound(player, pos, level.getRandom().nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    /** O {@code onPoweredBlockChange}: com sinal, fecha; sem sinal, abre. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (!level.isClientSide()) {
            boolean flag = level.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                BlockState next = state.setValue(POWERED, flag);
                if (flag && !state.getValue(CLOSED) || !flag && state.getValue(CLOSED)) {
                    next = next.setValue(CLOSED, flag);
                    door(level, pos, null);
                }
                level.setBlock(pos, next, 2);
            }
        }
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }
}
