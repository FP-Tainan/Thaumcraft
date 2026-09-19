package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.thaumcraft.block.entity.eldritch.AncientLockBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O mecanismo antigo de tranca: o número 8 do {@code BlockEldritch} da 4.2.3.5. A face da fechadura ({@link #FACING}, a
 * textura {@code deco_2}) dá para fora da sala do chefe; o resto é a porta antiga. Não se quebra. Com a tábua rúnica na
 * mão, a fechadura a toma e começa a abrir — e solta faíscas azuis enquanto isso.
 */
public class AncientLockBlock extends BaseEntityBlock {
    public static final MapCodec<AncientLockBlock> CODEC = simpleCodec(AncientLockBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    /** As faíscas, do lado de quem vê. */
    public static ClientEffects clientEffects = (level, pos, random) -> {
    };

    public interface ClientEffects {
        void spark(Level level, BlockPos pos, RandomSource random);
    }

    public AncientLockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientLockBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ANCIENT_LOCK, AncientLockBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!stack.is(TCItems.RUNED_TABLET)) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (level.getBlockEntity(pos) instanceof AncientLockBlockEntity lock && lock.count < 0) {
            if (!level.isClientSide()) {
                lock.unlock();
                stack.consume(1, player);
                level.playSound(null, pos, TCSounds.RUNIC_SHIELD_CHARGE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof AncientLockBlockEntity lock && lock.count >= 0) clientEffects.spark(level, pos, random);
    }
}
