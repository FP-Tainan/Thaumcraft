package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.CrucibleBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O crisol: o número 0 do {@code BlockMetalDevice} da 4.2.3.5 — um caldeirão de ferro com o fundo de cinco dezesseis
 * avos e as paredes de um oitavo até 0,85 (a caixa de colisão do original), que se enche com balde ou garrafa d'água e
 * estala ({@code liquid.lavapop}) quando ferve. O comparador lê a essência dissolvida; quebrado, despeja o que tinha.
 */
public class CrucibleBlock extends BaseEntityBlock {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);
    private static final VoxelShape COLLISION = Shapes.or(Block.box(0, 0, 0, 16, 5, 16),
            Block.box(0, 0, 0, 2, 13.6, 16), Block.box(0, 0, 0, 16, 13.6, 2),
            Block.box(14, 0, 0, 16, 13.6, 16), Block.box(0, 0, 14, 16, 13.6, 16));

    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    /** O {@code onBlockActivated}: balde ou garrafa d'água enchem o tanque inteiro e voltam vazios. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) return InteractionResult.PASS;
        ItemStack empty = null;
        if (stack.is(Items.WATER_BUCKET)) {
            empty = new ItemStack(Items.BUCKET);
        } else if (stack.is(Items.POTION)
                && stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            empty = new ItemStack(Items.GLASS_BOTTLE);
        }
        if (empty == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!crucible.fillBucket()) return InteractionResult.SUCCESS;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (!player.getInventory().add(empty)) player.drop(empty, false);
        }
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.33f,
                1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) crucible.touched(level, entity);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) crucible.getBellows();
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible ? crucible.comparator() : 0;
    }

    /** O {@code randomDisplayTick}: o estalo da água fervendo. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) != 0) return;
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible && crucible.boiling()) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + random.nextFloat() * 0.1f, 1.2f + random.nextFloat() * 0.2f, false);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.CRUCIBLE, CrucibleBlockEntity::tick);
    }
}
