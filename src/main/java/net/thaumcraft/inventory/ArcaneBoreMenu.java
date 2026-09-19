package net.thaumcraft.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import net.thaumcraft.registry.TCMenus;
import org.jetbrains.annotations.Nullable;

/** O {@code ContainerArcaneBore} da 4.2.3.5: a casa do foco de escavação, a da picareta e o inventário. */
public class ArcaneBoreMenu extends AbstractContainerMenu {
    @Nullable
    public final ArcaneBoreBlockEntity bore;

    public ArcaneBoreMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ArcaneBoreBlockEntity found ? found : null);
    }

    public ArcaneBoreMenu(int id, Inventory inventory, @Nullable ArcaneBoreBlockEntity bore) {
        super(TCMenus.ARCANE_BORE, id);
        this.bore = bore;
        Container source = bore != null ? bore : new SimpleContainer(2);
        this.addSlot(new Slot(source, 0, 26, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArcaneBoreBlockEntity.isExcavationFocus(stack);
            }
        });
        this.addSlot(new Slot(source, 1, 74, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArcaneBoreBlockEntity.isPickaxe(stack);
            }
        });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 59 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 117));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.bore == null || this.bore.stillValid(player);
    }

    /** O {@code transferStackInSlot}: das casas para o inventário; do inventário, foco e picareta para as casas deles. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index <= 1) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (ArcaneBoreBlockEntity.isExcavationFocus(stack)) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (ArcaneBoreBlockEntity.isPickaxe(stack)) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        return copy;
    }
}
