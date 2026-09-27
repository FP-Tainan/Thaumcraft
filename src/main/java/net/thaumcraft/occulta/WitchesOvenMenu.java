package net.thaumcraft.occulta;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela do Forno das Bruxas: o {@code ContainerWitchesOven} do Witchery, com as casas nos lugares dele.
 *
 * <p>O que vai ao fogo em (56, 17), o combustível embaixo em (56, 53), o que sai em (118, 21), o fumo em
 * (118, 53) e os potes de barro no meio, em (83, 53).
 */
public class WitchesOvenMenu extends AbstractContainerMenu {
    private final Container oven;
    private final ContainerData data;

    public WitchesOvenMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(WitchesOvenBlockEntity.SIZE),
                new SimpleContainerData(WitchesOvenBlockEntity.DATA_SIZE));
    }

    public WitchesOvenMenu(int id, Inventory inventory, Container oven, ContainerData data) {
        super(TCMenus.WITCHES_OVEN, id);
        checkContainerSize(oven, WitchesOvenBlockEntity.SIZE);
        checkContainerDataCount(data, WitchesOvenBlockEntity.DATA_SIZE);
        this.oven = oven;
        this.data = data;
        this.addDataSlots(data);

        this.addSlot(new Slot(oven, WitchesOvenBlockEntity.INPUT, 56, 17));
        this.addSlot(new Slot(oven, WitchesOvenBlockEntity.FUEL, 56, 53));
        this.addSlot(new FurnaceResultSlot(inventory.player, oven, WitchesOvenBlockEntity.OUTPUT, 118, 21));
        this.addSlot(new FurnaceResultSlot(inventory.player, oven, WitchesOvenBlockEntity.BYPRODUCT, 118, 53));
        this.addSlot(new Slot(oven, WitchesOvenBlockEntity.JARS, 83, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(OccultaItems.CLAY_JAR);
            }
        });

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(inventory, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(inventory, coluna, 8 + coluna * 18, 142));
        }
    }

    public int data(int index) {
        return this.data.get(index);
    }

    /** O quanto do fogo ainda resta, em pontos de uma altura. */
    public int burnScaled(int altura) {
        int total = this.data.get(WitchesOvenBlockEntity.DATA_BURN_TOTAL);
        if (total == 0) total = 200;
        return this.data.get(WitchesOvenBlockEntity.DATA_BURN) * altura / total;
    }

    /** O quanto do cozimento já andou, em pontos de uma largura. */
    public int cookScaled(int largura) {
        return this.data.get(WitchesOvenBlockEntity.DATA_COOK) * largura / WitchesOvenBlockEntity.COOK_TIME;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.oven.stillValid(player);
    }

    /** O {@code transferStackInSlot} do original, com as cinco casas do forno. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack levado = ItemStack.EMPTY;
        Slot casa = this.slots.get(index);
        if (casa == null || !casa.hasItem()) return levado;
        ItemStack dentro = casa.getItem();
        levado = dentro.copy();
        int inventário = WitchesOvenBlockEntity.SIZE;
        int fim = this.slots.size();

        if (index < inventário) {
            // do forno para o inventário de quem joga
            if (!this.moveItemStackTo(dentro, inventário, fim, true)) return ItemStack.EMPTY;
            casa.onQuickCraft(dentro, levado);
        } else if (dentro.is(OccultaItems.CLAY_JAR)) {
            if (!this.moveItemStackTo(dentro, WitchesOvenBlockEntity.JARS, WitchesOvenBlockEntity.JARS + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (WitchesOvenBlockEntity.isFuel(player.level(), dentro)) {
            if (!this.moveItemStackTo(dentro, WitchesOvenBlockEntity.FUEL, WitchesOvenBlockEntity.FUEL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(dentro, WitchesOvenBlockEntity.INPUT, WitchesOvenBlockEntity.INPUT + 1, false)) {
            // e o resto vai para o outro lado do inventário, como em qualquer tela
            int mochila = inventário + 27;
            if (index < mochila) {
                if (!this.moveItemStackTo(dentro, mochila, fim, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(dentro, inventário, mochila, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (dentro.isEmpty()) {
            casa.setByPlayer(ItemStack.EMPTY);
        } else {
            casa.setChanged();
        }
        if (dentro.getCount() == levado.getCount()) return ItemStack.EMPTY;
        casa.onTake(player, dentro);
        return levado;
    }
}
