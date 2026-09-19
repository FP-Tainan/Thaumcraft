package net.thaumcraft.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ArcaneSpaBlockEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCMenus;
import org.jetbrains.annotations.Nullable;

/** O {@code ContainerSpa} da 4.2.3.5: a casa dos sais e o inventário; o botão 1 troca misturar/só o fluido. */
public class ArcaneSpaMenu extends AbstractContainerMenu {
    @Nullable
    public final ArcaneSpaBlockEntity spa;

    public ArcaneSpaMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ArcaneSpaBlockEntity found ? found : null);
    }

    public ArcaneSpaMenu(int id, Inventory inventory, @Nullable ArcaneSpaBlockEntity spa) {
        super(TCMenus.ARCANE_SPA, id);
        this.spa = spa;
        Container source = spa != null ? spa : new SimpleContainer(1);
        this.addSlot(new Slot(source, 0, 65, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TCItems.BATH_SALTS);
            }
        });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1 && this.spa != null && !player.level().isClientSide()) this.spa.toggleMix();
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.spa == null || this.spa.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!stack.is(TCItems.BATH_SALTS)) return ItemStack.EMPTY;
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
