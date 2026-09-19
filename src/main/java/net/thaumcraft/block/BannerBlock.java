package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/**
 * O estandarte: o aparelho de madeira 8 do {@code BlockWoodenDevice} da 4.2.3.5. Dois blocos de altura, de pé num mastro
 * ou pendurado na parede, sem colisão. Um frasco de essência pinta o aspecto dele no pano (gastando o frasco); agachado,
 * a pintura sai. Só o estandarte colorido aceita pintura — o dos cultistas não.
 */
public class BannerBlock extends BaseEntityBlock {
    public static final MapCodec<BannerBlock> CODEC = simpleCodec(BannerBlock::new);

    public BannerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** O {@code setBlockBoundsBasedOnState}: o mastro no meio, ou a faixa encostada na parede. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity banner)) return Shapes.block();
        if (!banner.getWall()) return Block.box(5.28, 0.0, 5.28, 10.56, 32.0, 10.56);
        return switch (banner.getFacing()) {
            case 4 -> Block.box(12.0, 0.0, 0.0, 16.0, 32.0, 16.0);
            case 8 -> Block.box(0.0, 0.0, 12.0, 16.0, 32.0, 16.0);
            case 12 -> Block.box(0.0, 0.0, 0.0, 4.0, 32.0, 16.0);
            default -> Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 4.0);
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && !stack.is(TCItems.PHIAL)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity banner) || banner.getColor() < 0) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            banner.setAspect(null);
        } else {
            String tag = stack.get(TCComponents.PHIAL_ASPECT);
            Aspect aspect = tag == null ? null : Aspect.of(tag);
            if (aspect != null) {
                banner.setAspect(aspect);
                stack.shrink(1);
            }
        }
        level.playSound(null, pos, SoundEvents.WOOL_STEP, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        return this.useItemOn(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(pos, state);
    }

    /** O estandarte colorido, como sai da bancada arcana (sem cor é o dos cultistas). */
    public static ItemStack stack(int color) {
        ItemStack stack = new ItemStack(TCItems.BANNER);
        if (color >= 0) stack.set(TCComponents.BANNER_COLOR, color);
        return stack;
    }
}
