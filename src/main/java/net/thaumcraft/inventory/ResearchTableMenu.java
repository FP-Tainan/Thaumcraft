package net.thaumcraft.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.item.ScribingToolsItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCMenus;

/**
 * As casas da mesa de pesquisa: o {@code ContainerResearchTable} da 4.2.3.5.
 *
 * <p>A ferramenta de escrita fica em (14, 10) e a nota em (70, 10), no alto à esquerda; o inventário de quem
 * pesquisa, embaixo, a partir de (48, 175). O botão cinco é o de copiar a descoberta.
 */
public class ResearchTableMenu extends AbstractContainerMenu {
    public static final int DUPLICATE = 5;

    private final Container table;
    private final ResearchTableBlockEntity entity;

    /** Do lado de quem vê: a mesa vem pela posição, e a entidade é a que o mundo daqui conhece. */
    public ResearchTableMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity found
                ? found : null);
    }

    public ResearchTableMenu(int id, Inventory inventory, ResearchTableBlockEntity entity) {
        super(TCMenus.RESEARCH_TABLE, id);
        this.entity = entity;
        this.table = entity != null ? entity : new SimpleContainer(2);
        this.table.startOpen(inventory.player);

        this.addSlot(new Slot(this.table, ResearchTableBlockEntity.INK, 14, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ScribingToolsItem;
            }
        });
        this.addSlot(new Slot(this.table, ResearchTableBlockEntity.NOTE, 70, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TCItems.RESEARCH_NOTES);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 48 + col * 18, 175 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 48 + col * 18, 233));
        }
    }

    /** A mesa por trás da tela, quando se sabe qual é. */
    public ResearchTableBlockEntity entity() {
        return this.entity;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == DUPLICATE && this.entity != null) {
            this.entity.duplicate(player);
            return true;
        }
        return id == 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.table.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
