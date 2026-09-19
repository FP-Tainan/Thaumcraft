package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.item.HandMirrorItem;
import net.thaumcraft.registry.TCMenus;

/**
 * O {@code ContainerHandMirror} da 4.2.3.5: uma casa só, no meio do vidro; o que se põe nela some pelo espelho da mão
 * (a que está sendo segurada) e sai no espelho ligado. O próprio espelho de mão não entra.
 */
public class HandMirrorMenu extends AbstractContainerMenu {
    private final Player player;
    private final ItemStack mirror;
    private final SimpleContainer input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            HandMirrorMenu.this.slotsChanged(this);
        }
    };

    public HandMirrorMenu(int id, Inventory inventory) {
        super(TCMenus.HAND_MIRROR, id);
        this.player = inventory.player;
        this.mirror = inventory.getSelectedItem();
        this.addSlot(new Slot(this.input, 0, 80, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !(stack.getItem() instanceof HandMirrorItem);
            }
        });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
    }

    /** O {@code onCraftMatrixChanged}: o que entrou na casa vai embora pelo espelho. */
    @Override
    public void slotsChanged(Container container) {
        ItemStack in = this.input.getItem(0);
        if (!this.player.level().isClientSide() && !in.isEmpty()
                && HandMirrorItem.transport(this.mirror, in, this.player, this.player.level())) {
            this.input.removeItemNoUpdate(0);
            this.broadcastChanges();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || slot.getItem().getItem() instanceof HandMirrorItem) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** O {@code onContainerClosed}: o que ficou na casa volta para quem segura. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) this.clearContainer(player, this.input);
    }
}
