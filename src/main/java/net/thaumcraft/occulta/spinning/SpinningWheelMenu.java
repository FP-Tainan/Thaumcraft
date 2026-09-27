package net.thaumcraft.occulta.spinning;

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
 * A tela da Roca: o {@code ContainerSpinningWheel} do Witchery, com as casas nos lugares dele — a fibra em
 * (56, 20), os três temperos em fila a (56, 53), (74, 53) e (92, 53), e o que sai em (118, 21).
 */
public class SpinningWheelMenu extends AbstractContainerMenu {
    private final Container wheel;
    private final ContainerData data;

    public SpinningWheelMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SpinningWheelBlockEntity.SIZE),
                new SimpleContainerData(SpinningWheelBlockEntity.DATA_SIZE));
    }

    public SpinningWheelMenu(int id, Inventory inventory, Container wheel, ContainerData data) {
        super(TCMenus.SPINNING_WHEEL, id);
        checkContainerSize(wheel, SpinningWheelBlockEntity.SIZE);
        checkContainerDataCount(data, SpinningWheelBlockEntity.DATA_SIZE);
        this.wheel = wheel;
        this.data = data;
        this.addDataSlots(data);

        this.addSlot(new Slot(wheel, SpinningWheelBlockEntity.FIBRE, 56, 20));
        this.addSlot(new Slot(wheel, SpinningWheelBlockEntity.MOD_A, 56, 53));
        this.addSlot(new FurnaceResultSlot(inventory.player, wheel, SpinningWheelBlockEntity.RESULT, 118, 21));
        this.addSlot(new Slot(wheel, SpinningWheelBlockEntity.MOD_B, 74, 53));
        this.addSlot(new Slot(wheel, SpinningWheelBlockEntity.MOD_C, 92, 53));

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(inventory, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(inventory, coluna, 8 + coluna * 18, 142));
        }
    }

    /** O quanto do fio já se fiou, em pontos de uma largura. */
    public int spinScaled(int largura) {
        return this.data.get(SpinningWheelBlockEntity.DATA_SPIN) * largura / SpinningWheelBlockEntity.SPIN_TIME;
    }

    /** Se há altar por perto dando poder. */
    public boolean powered() {
        return this.data.get(SpinningWheelBlockEntity.DATA_POWER) > 0;
    }

    @Override
    public boolean stillValid(Player quem) {
        return this.wheel.stillValid(quem);
    }

    @Override
    public ItemStack quickMoveStack(Player quem, int qual) {
        ItemStack levado = ItemStack.EMPTY;
        Slot casa = this.slots.get(qual);
        if (casa == null || !casa.hasItem()) return levado;
        ItemStack dentro = casa.getItem();
        levado = dentro.copy();
        int roca = SpinningWheelBlockEntity.SIZE;
        int fim = this.slots.size();

        if (qual < roca) {
            if (!this.moveItemStackTo(dentro, roca, fim, true)) return ItemStack.EMPTY;
            casa.onQuickCraft(dentro, levado);
        } else if (!this.moveItemStackTo(dentro, SpinningWheelBlockEntity.FIBRE,
                SpinningWheelBlockEntity.RESULT, false)
                && !this.moveItemStackTo(dentro, SpinningWheelBlockEntity.MOD_B,
                SpinningWheelBlockEntity.MOD_C + 1, false)) {
            int mochila = roca + 27;
            if (qual < mochila) {
                if (!this.moveItemStackTo(dentro, mochila, fim, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(dentro, roca, mochila, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (dentro.isEmpty()) casa.setByPlayer(ItemStack.EMPTY);
        else casa.setChanged();
        if (dentro.getCount() == levado.getCount()) return ItemStack.EMPTY;
        casa.onTake(quem, dentro);
        return levado;
    }
}
