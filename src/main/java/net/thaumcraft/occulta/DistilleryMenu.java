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
 * A tela da Destilaria: o {@code ContainerDistillery} do Witchery, com as casas nos lugares dele.
 *
 * <p>As duas que entram, uma sobre a outra, em (48, 16) e (48, 34); os potes de barro embaixo delas, em (48, 54);
 * e as quatro que saem em quadrado, de (110, 16) a (128, 34).
 */
public class DistilleryMenu extends AbstractContainerMenu {
    private final Container distillery;
    private final ContainerData data;

    public DistilleryMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(DistilleryBlockEntity.SIZE),
                new SimpleContainerData(DistilleryBlockEntity.DATA_SIZE));
    }

    public DistilleryMenu(int id, Inventory inventory, Container distillery, ContainerData data) {
        super(TCMenus.DISTILLERY, id);
        checkContainerSize(distillery, DistilleryBlockEntity.SIZE);
        checkContainerDataCount(data, DistilleryBlockEntity.DATA_SIZE);
        this.distillery = distillery;
        this.data = data;
        this.addDataSlots(data);

        this.addSlot(new Slot(distillery, DistilleryBlockEntity.INPUT_A, 48, 16));
        this.addSlot(new Slot(distillery, DistilleryBlockEntity.INPUT_B, 48, 34));
        this.addSlot(new Slot(distillery, DistilleryBlockEntity.JARS, 48, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(OccultaItems.CLAY_JAR);
            }
        });
        int[][] onde = {{110, 16}, {128, 16}, {110, 34}, {128, 34}};
        for (int i = 0; i < DistilleryBlockEntity.OUTPUT_COUNT; i++) {
            this.addSlot(new FurnaceResultSlot(inventory.player, distillery,
                    DistilleryBlockEntity.OUTPUT_FIRST + i, onde[i][0], onde[i][1]));
        }

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(inventory, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(inventory, coluna, 8 + coluna * 18, 142));
        }
    }

    /** O quanto da destilação já andou, em pontos de uma largura. */
    public int cookScaled(int largura) {
        return this.data.get(DistilleryBlockEntity.DATA_COOK) * largura / DistilleryBlockEntity.COOK_TIME;
    }

    /** Se há altar por perto dando poder. */
    public boolean powered() {
        return this.data.get(DistilleryBlockEntity.DATA_POWER) > 0;
    }

    /** O quanto do tempo total já correu, para a gota que pinga na tela. */
    public int cookTime() {
        return this.data.get(DistilleryBlockEntity.DATA_COOK);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.distillery.stillValid(player);
    }

    /** O {@code transferStackInSlot}: os potes vão para a casa deles, e o resto para a primeira que couber. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack levado = ItemStack.EMPTY;
        Slot casa = this.slots.get(index);
        if (casa == null || !casa.hasItem()) return levado;
        ItemStack dentro = casa.getItem();
        levado = dentro.copy();
        int inventário = DistilleryBlockEntity.SIZE;
        int fim = this.slots.size();

        if (index < inventário) {
            if (!this.moveItemStackTo(dentro, inventário, fim, true)) return ItemStack.EMPTY;
            casa.onQuickCraft(dentro, levado);
        } else if (dentro.is(OccultaItems.CLAY_JAR)) {
            if (!this.moveItemStackTo(dentro, DistilleryBlockEntity.JARS, DistilleryBlockEntity.JARS + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(dentro, DistilleryBlockEntity.INPUT_A, DistilleryBlockEntity.JARS, false)) {
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
