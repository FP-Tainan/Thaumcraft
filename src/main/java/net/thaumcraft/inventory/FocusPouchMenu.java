package net.thaumcraft.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusPouchItem;
import net.thaumcraft.registry.TCMenus;

/**
 * A bolsa de focos aberta: o {@code ContainerFocusPouch} da 4.2.3.5. Dezoito casas só para focos, seis por
 * fileira, e o inventário de quem joga embaixo — com a casa da própria bolsa travada, para ela não sair da mão
 * enquanto está aberta. O que ficar dentro volta para a bolsa quando a tela fecha.
 */
public class FocusPouchMenu extends AbstractContainerMenu {
    private final SimpleContainer pouch = new SimpleContainer(FocusPouchItem.SIZE);
    private final ItemStack held;
    private final int blockSlot;

    public FocusPouchMenu(int id, Inventory inventory) {
        this(id, inventory, ItemStack.EMPTY);
    }

    public FocusPouchMenu(int id, Inventory inventory, ItemStack held) {
        super(TCMenus.FOCUS_POUCH, id);
        this.held = held;
        if (!held.isEmpty()) {
            NonNullList<ItemStack> inside = FocusPouchItem.contents(held);
            for (int i = 0; i < inside.size(); i++) this.pouch.setItem(i, inside.get(i));
        }
        for (int a = 0; a < FocusPouchItem.SIZE; a++) {
            this.addSlot(new Slot(this.pouch, a, 37 + a % 6 * 18, 51 + a / 6 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof FocusItem;
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 209));
        this.blockSlot = FocusPouchItem.SIZE + 27 + inventory.getSelectedSlot();
    }

    public int blockedHotbarSlot() {
        return this.blockSlot - FocusPouchItem.SIZE - 27;
    }

    @Override
    public void clicked(int slot, int button, ContainerInput type, Player player) {
        // a casa da bolsa não se mexe, nem por clique nem pelas teclas de número
        if (slot == this.blockSlot) return;
        if (type == ContainerInput.SWAP && button == this.blockedHotbarSlot()) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == this.blockSlot) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < FocusPouchItem.SIZE) {
            if (!this.moveItemStackTo(stack, FocusPouchItem.SIZE, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!(stack.getItem() instanceof FocusItem) || !this.moveItemStackTo(stack, 0, FocusPouchItem.SIZE, false)) {
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

    /** O {@code onContainerClosed}: o que ficou dentro volta para a bolsa na mão. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;
        ItemStack inHand = player.getInventory().getItem(this.blockedHotbarSlot());
        if (!(inHand.getItem() instanceof FocusPouchItem)) return;
        NonNullList<ItemStack> list = NonNullList.withSize(FocusPouchItem.SIZE, ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) list.set(i, this.pouch.getItem(i));
        FocusPouchItem.setContents(inHand, list);
    }
}
