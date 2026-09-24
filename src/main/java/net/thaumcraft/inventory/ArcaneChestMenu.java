package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * O Baú Arcano aberto: o {@code ContainerArcaneChest} do Magia Naturalis 0.5.0. A grade é a do original — nove por
 * seis no de madeira-grande, onze por sete no de prateada — e o inventário de quem abre fica embaixo, deslocado
 * dezoito pontos no baú maior, como lá.
 */
public class ArcaneChestMenu extends AbstractContainerMenu {
    private final Container chest;
    private final int rows;
    private final int columns;

    public ArcaneChestMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(54), 6, 9);
    }

    public ArcaneChestMenu(int id, Inventory inventory, Container chest, int rows, int columns) {
        super(TCMenus.ARCANE_CHEST, id);
        this.chest = chest;
        this.rows = rows;
        this.columns = columns;
        chest.startOpen(inventory.player);

        int offset = columns == 9 ? 0 : 18;
        int slot = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                this.addSlot(new Slot(chest, slot++, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18 + offset, 18 + rows * 18 + 14 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18 + offset, 18 + rows * 18 + 72));
        }
    }

    public Container chest() {
        return this.chest;
    }

    public int rows() {
        return this.rows;
    }

    public int columns() {
        return this.columns;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            int dentro = this.rows * this.columns;
            if (index < dentro) {
                if (!this.moveItemStackTo(stack, dentro, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 0, dentro, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.chest.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.chest.stopOpen(player);
    }
}
