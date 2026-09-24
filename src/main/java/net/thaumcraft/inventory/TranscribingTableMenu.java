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
import net.thaumcraft.naturalis.ResearchLogItem;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela da Mesa de Transcrição: o {@code ContainerTranscribingTable} do Magia Naturalis 0.5.0 — a casa de cima
 * em (64, 16), que só aceita o Diário de Pesquisa, e a de baixo em (64, 48), de onde só se tira.
 */
public class TranscribingTableMenu extends AbstractContainerMenu {
    private final Container table;
    private final ContainerData data;

    public TranscribingTableMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(2), new SimpleContainerData(1));
    }

    public TranscribingTableMenu(int id, Inventory inventory, Container table, ContainerData data) {
        super(TCMenus.TRANSCRIBING_TABLE, id);
        checkContainerSize(table, 2);
        checkContainerDataCount(data, 1);
        this.table = table;
        this.data = data;
        this.addDataSlots(data);
        this.addSlot(new Slot(table, 0, 64, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ResearchLogItem;
            }
        });
        // a casa de baixo é o SlotInvalid do original: o diário pronto sai dela, mas nada entra
        this.addSlot(new Slot(table, 1, 64, 48) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
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

    /** Quanto falta para a próxima colheita, de 0 a 40. */
    public int timer() {
        return this.data.get(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (stack.getItem() instanceof ResearchLogItem) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table.stillValid(player);
    }
}
