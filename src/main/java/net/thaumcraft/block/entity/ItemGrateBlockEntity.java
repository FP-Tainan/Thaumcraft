package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.ItemGrateBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileGrate} da 4.2.3.5: o inventário de uma casa que nunca guarda nada. Aberta, aceita por cima o que um funil
 * empurra e o solta logo abaixo da grade, caindo.
 */
public class ItemGrateBlockEntity extends BlockEntity implements WorldlyContainer {
    public ItemGrateBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ITEM_GRATE, pos, state);
    }

    private boolean open() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ItemGrateBlock.CLOSED) && !state.getValue(ItemGrateBlock.CLOSED);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    /** O {@code setInventorySlotContents}: o item sai logo abaixo da chapa, descendo. */
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.level == null || this.level.isClientSide() || stack.isEmpty()) return;
        ItemEntity ei = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.6,
                this.worldPosition.getZ() + 0.5, stack.copy());
        ei.setDeltaMovement(0.0, -0.1, 0.0);
        this.level.addFreshEntity(ei);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.open();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return this.open() && side == Direction.UP ? new int[]{0} : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return this.open() && side == Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }
}
