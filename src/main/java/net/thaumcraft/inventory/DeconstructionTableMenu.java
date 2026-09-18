package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela da mesa de desconstrução: o {@code ContainerDeconstructionTable} da 4.2.3.5 — a casa em (64, 16), que só
 * aceita coisas com aspecto, e o inventário de quem joga embaixo. O botão um recolhe o primário à espera.
 */
public class DeconstructionTableMenu extends AbstractContainerMenu {
    public static final int COLLECT = 1;

    private final Container table;
    private final ContainerData data;

    public DeconstructionTableMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    public DeconstructionTableMenu(int id, Inventory inventory, Container table, ContainerData data) {
        super(TCMenus.DECONSTRUCTION_TABLE, id);
        checkContainerSize(table, 1);
        checkContainerDataCount(data, 2);
        this.table = table;
        this.data = data;
        this.addDataSlots(data);
        this.addSlot(new Slot(table, 0, 64, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !ObjectAspects.of(stack).isEmpty();
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    /** Quanto falta para desfazer, de 0 a 40. */
    public int breaktime() {
        return this.data.get(0);
    }

    public Aspect aspect() {
        return DeconstructionTableBlockEntity.byIndex(this.data.get(1));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == COLLECT && this.table instanceof DeconstructionTableBlockEntity table && table.aspect() != null) {
            table.collect(player);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index != 0) {
            if (ObjectAspects.of(stack).isEmpty() || !this.moveItemStackTo(stack, 0, 1, false)) {
                if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!this.moveItemStackTo(stack, 1, 37, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table.stillValid(player);
    }
}
