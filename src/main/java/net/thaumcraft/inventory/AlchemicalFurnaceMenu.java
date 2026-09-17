package net.thaumcraft.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity;
import net.thaumcraft.registry.TCMenus;

/**
 * As duas casas do forno alquímico, nos lugares do Thaumcraft 4.2.3.5.
 *
 * <p>O que vai virar essência entra em (56, 17) e o combustível em (56, 53), com a chama entre os dois —
 * os mesmos lugares do forno comum, que é de onde o original tirou a tela dele.
 */
public class AlchemicalFurnaceMenu extends AbstractContainerMenu {
    /** Quantos números a tela precisa do forno: fogo, fogo total, cozimento, cozimento total e onde ele está. */
    public static final int DATA_SIZE = 7;
    public static final int DATA_BURN = 0;
    public static final int DATA_BURN_TOTAL = 1;
    public static final int DATA_COOK = 2;
    public static final int DATA_SMELT = 3;
    private static final int DATA_X = 4;
    private static final int DATA_Y = 5;
    private static final int DATA_Z = 6;

    private final Container furnace;
    private final net.minecraft.world.inventory.ContainerData data;

    public AlchemicalFurnaceMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(AlchemicalFurnaceBlockEntity.SIZE),
                new net.minecraft.world.inventory.SimpleContainerData(DATA_SIZE));
    }

    public AlchemicalFurnaceMenu(int id, Inventory inventory, AlchemicalFurnaceBlockEntity furnace) {
        this(id, inventory, furnace, furnace.data());
    }

    public AlchemicalFurnaceMenu(int id, Inventory inventory, Container furnace,
            net.minecraft.world.inventory.ContainerData data) {
        super(TCMenus.ALCHEMICAL_FURNACE, id);
        checkContainerSize(furnace, AlchemicalFurnaceBlockEntity.SIZE);
        checkContainerDataCount(data, DATA_SIZE);
        this.furnace = furnace;
        this.data = data;
        this.addDataSlots(data);
        furnace.startOpen(inventory.player);

        this.addSlot(new Slot(furnace, AlchemicalFurnaceBlockEntity.INPUT_SLOT, 56, 17));
        this.addSlot(new Slot(furnace, AlchemicalFurnaceBlockEntity.FUEL_SLOT, 56, 53));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    /** O forno que esta tela está mostrando, para a tela ler o calor e a essência guardada. */
    public Container container() {
        return this.furnace;
    }

    public int data(int index) {
        return this.data.get(index);
    }

    /**
     * Onde o forno está no mundo.
     *
     * <p>O nome do lugar viaja junto dos outros números da tela, e é assim que a tela alcança a lista de
     * aspectos do forno: ela não vem pela tela, vem pelo próprio bloco, que o servidor já mantém acertado
     * em quem está por perto. Assim a lista pode ter os quarenta e oito aspectos sem precisar de um número
     * de tela para cada um.
     */
    public net.minecraft.core.BlockPos where() {
        return new net.minecraft.core.BlockPos(
                this.data.get(DATA_X), this.data.get(DATA_Y), this.data.get(DATA_Z));
    }

    /** O quanto o fogo já andou, de zero a um. */
    public float burnt() {
        int total = this.data.get(DATA_BURN_TOTAL);
        return total <= 0 ? 0.0f : this.data.get(DATA_BURN) / (float) total;
    }

    /** O quanto o cozimento já andou, de zero a um. */
    public float cooked() {
        int total = this.data.get(DATA_SMELT);
        return total <= 0 ? 0.0f : Math.min(1.0f, this.data.get(DATA_COOK) / (float) total);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return moved;

        ItemStack stack = slot.getItem();
        moved = stack.copy();
        int bag = AlchemicalFurnaceBlockEntity.SIZE;

        if (index < bag) {
            // do forno para as mãos
            if (!this.moveItemStackTo(stack, bag, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, bag, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.furnace.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.furnace.stopOpen(player);
    }
}
