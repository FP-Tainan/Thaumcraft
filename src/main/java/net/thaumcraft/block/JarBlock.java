package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O jarro lacrado: um pote de vidro com tampa de chumbo, que guarda essência.
 *
 * <p>Com um rótulo na mão, um toque no jarro cola nele o aspecto do frasco que se estiver segurando — daí
 * em diante aquele jarro só aceita aquilo. Agachado, o toque arranca o rótulo de volta.
 */
public class JarBlock extends BaseEntityBlock {
    public static final MapCodec<JarBlock> CODEC = simpleCodec(JarBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0);

    public JarBlock(Properties properties) {
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
        return SHAPE;
    }

    /**
     * No criativo o jarro cheio também cai.
     *
     * <p>O original solta o jarro no {@code onBlockHarvested}, que roda mesmo no modo criativo — e é
     * assim que se leva essência de um lugar a outro testando. Jarro vazio e sem rótulo não cai, como
     * qualquer bloco no criativo.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()
                && level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.JarBlockEntity jar) {
            ItemStack drop = new ItemStack(this);
            drop.applyComponents(jar.collectComponents());
            if (drop.has(net.thaumcraft.registry.TCComponents.JAR_CONTENTS)) {
                net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(
                        level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) return InteractionResult.PASS;

        // agachado, arranca o rótulo
        if (player.isShiftKeyDown() && jar.label() != null) {
            if (!level.isClientSide()) {
                jar.setLabel(null);
                if (!player.getAbilities().instabuild) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(net.thaumcraft.registry.TCResources.get("jar_label")));
                }
                level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.6f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }

        // com um rótulo na mão, cola nele o aspecto do que já estiver dentro
        if (stack.is(net.thaumcraft.registry.TCResources.get("jar_label")) && jar.label() == null) {
            Aspect wanted = jar.aspect();
            if (wanted == null) {
                if (level.isClientSide()) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("tc.jar.empty"));
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                jar.setLabel(wanted);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.6f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                               @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // o rótulo fica virado para quem pôs o jarro, como no original
        if (placer != null && level.getBlockEntity(pos) instanceof JarBlockEntity jar) {
            jar.setFacing(placer.getDirection().getOpposite());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JarBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.JAR, JarBlockEntity::tick);
    }
}
