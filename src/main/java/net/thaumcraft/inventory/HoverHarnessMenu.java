package net.thaumcraft.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.event.Hover;
import net.thaumcraft.item.HoverHarnessItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCMenus;

/**
 * O arreio aberto: o {@code ContainerHoverHarness} + {@code InventoryHoverHarness} da 4.2.3.5. Uma casa só, para um
 * jarro com Potentia, e o inventário embaixo com a casa do próprio arreio travada. O jarro volta para o arreio quando
 * a tela fecha.
 */
public class HoverHarnessMenu extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final int blockSlot;

    public HoverHarnessMenu(int id, Inventory inventory) {
        this(id, inventory, ItemStack.EMPTY);
    }

    public HoverHarnessMenu(int id, Inventory inventory, ItemStack armor) {
        super(TCMenus.HOVER_HARNESS, id);
        this.addSlot(new Slot(this.input, 0, 80, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Hover.isFuel(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        this.blockSlot = 28 + inventory.getSelectedSlot();
        if (!armor.isEmpty()) this.input.setItem(0, armor.getOrDefault(TCComponents.HARNESS_JAR, ItemStack.EMPTY).copy());
    }

    public int blockedHotbarSlot() {
        return this.blockSlot - 28;
    }

    @Override
    public void clicked(int slot, int button, ContainerInput type, Player player) {
        // a casa do arreio não se mexe, nem por clique nem pelas teclas de número
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
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!Hover.isFuel(stack) || !this.moveItemStackTo(stack, 0, 1, false)) {
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

    /** O {@code onContainerClosed}: o jarro volta para o arreio na mão. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;
        ItemStack jar = this.input.removeItemNoUpdate(0);
        ItemStack inHand = player.getInventory().getItem(this.blockedHotbarSlot());
        if (!(inHand.getItem() instanceof HoverHarnessItem)) {
            if (!jar.isEmpty()) player.getInventory().placeItemBackInInventory(jar);
            return;
        }
        if (jar.isEmpty()) inHand.remove(TCComponents.HARNESS_JAR);
        else inHand.set(TCComponents.HARNESS_JAR, jar);
    }
}
