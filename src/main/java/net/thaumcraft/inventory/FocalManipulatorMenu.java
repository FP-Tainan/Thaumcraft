package net.thaumcraft.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.registry.TCMenus;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code ContainerFocalManipulator} da 4.2.3.5: a casa do foco e o inventário. O botão de número {@code n} começa a
 * melhoria {@code n}; se não dá, o som de falha.
 */
public class FocalManipulatorMenu extends AbstractContainerMenu {
    @Nullable
    public final FocalManipulatorBlockEntity table;

    public FocalManipulatorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof FocalManipulatorBlockEntity found ? found : null);
    }

    public FocalManipulatorMenu(int id, Inventory inventory, @Nullable FocalManipulatorBlockEntity table) {
        super(TCMenus.FOCAL_MANIPULATOR, id);
        this.table = table;
        Container source = table != null ? table : new SimpleContainer(1);
        this.addSlot(new Slot(source, 0, 88, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) this.addSlot(new Slot(inventory, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
        }
        for (int i = 0; i < 9; i++) this.addSlot(new Slot(inventory, i, 16 + i * 18, 209));
    }

    /** O {@code enchantItem}. */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (this.table == null || player.level().isClientSide()) return false;
        if (button >= 0 && !this.table.startCraft(button, player)) {
            player.level().playSound(null, this.table.getBlockPos(), TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 0.33f, 1.0f);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table == null || this.table.stillValid(player);
    }

    /** O {@code transferStackInSlot}: foco para a casa; o resto entre a mochila e a barra. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index != 0) {
            if (stack.getItem() instanceof FocusItem) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (index < 28) {
                if (!this.moveItemStackTo(stack, 28, 37, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
                return ItemStack.EMPTY;
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
}
