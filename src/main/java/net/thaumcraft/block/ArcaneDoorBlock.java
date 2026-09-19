package net.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.OwnedBlockEntity;
import net.thaumcraft.item.KeyItem;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A porta arcana: o {@code BlockArcaneDoor} da 4.2.3.5. Uma porta de ferro que só o dono (e quem tem chave dele) abre
 * com a mão; a redstone não a mexe, mas a placa de pressão arcana ao lado, do mesmo dono ou de quem tem chave, abre
 * (ligada) e fecha (desligada). Dura (15), quase indestrutível por explosão e sem medo de chefão.
 */
public class ArcaneDoorBlock extends DoorBlock implements EntityBlock {
    public ArcaneDoorBlock(Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwnedBlockEntity(pos, state);
    }

    /** O dono é quem pôs a porta, nas duas metades. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof Player player)) return;
        for (BlockPos half : new BlockPos[]{pos, pos.above()}) {
            if (level.getBlockEntity(half) instanceof OwnedBlockEntity owned) {
                owned.owner = player.getName().getString();
                owned.setChanged();
            }
        }
    }

    /** A chave na mão vale antes da porta (o {@code onItemUseFirst} dela). */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (stack.getItem() instanceof KeyItem key) return key.useOnWarded(stack, level, pos, player);
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /** O {@code onBlockActivated}: quem não é dono nem tem chave leva um "não" e o som da porta emperrada. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof OwnedBlockEntity owned)) return InteractionResult.SUCCESS;
        if (!owned.mayUse(player)) {
            player.sendSystemMessage(Component.translatable("tc.door.refuses"));
            level.playSound(null, pos, TCSounds.DOOR_FAIL.value(), SoundSource.BLOCKS, 0.66f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        boolean open = !state.getValue(OPEN);
        this.toggle(level, pos, state, open);
        level.playSound(null, pos, level.getRandom().nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        return InteractionResult.SUCCESS;
    }

    private void toggle(Level level, BlockPos pos, BlockState state, boolean open) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lower);
        if (!(lowerState.getBlock() instanceof ArcaneDoorBlock)) return;
        level.setBlock(lower, lowerState.setValue(OPEN, open), 10);
        BlockState upperState = level.getBlockState(lower.above());
        if (upperState.getBlock() instanceof ArcaneDoorBlock) level.setBlock(lower.above(), upperState.setValue(OPEN, open), 10);
    }

    /**
     * O {@code onNeighborBlockChange}: a redstone não mexe na porta; uma placa arcana dos lados, de alguém que a porta
     * conhece, abre quando ligada e fecha quando desligada.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (!(neighbor instanceof ArcanePressurePlateBlock) || !(level.getBlockEntity(pos) instanceof OwnedBlockEntity owned)) return;
        List<String> users = new ArrayList<>();
        users.add(owned.owner);
        for (String entry : owned.accessList) users.add(entry.substring(1));
        int open = 0;
        search:
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState there = level.getBlockState(pos.relative(dir));
            if (!(there.getBlock() instanceof ArcanePressurePlateBlock)
                    || !(level.getBlockEntity(pos.relative(dir)) instanceof OwnedBlockEntity plate)) continue;
            for (String user : users) {
                if (plate.owner.equals(user) || plate.accessList.contains(user)) {
                    if (there.getValue(ArcanePressurePlateBlock.POWERED)) {
                        open = 1;
                        break search;
                    }
                    open = -1;
                    break;
                }
            }
        }
        if (open != 0 && state.getValue(OPEN) != (open == 1)) {
            this.toggle(level, pos, state, open == 1);
            // o evento 1003 de 2014: o som de porta, abrir ou fechar ao acaso
            level.playSound(null, pos, level.getRandom().nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                    SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion,
                                  java.util.function.BiConsumer<ItemStack, BlockPos> drops) {
        // o onBlockExploded vazio: explosão não a derruba
    }
}
