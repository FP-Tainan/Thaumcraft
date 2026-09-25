package net.thaumcraft.mortuorum;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela do Altar de Invocação: o {@code ContainerAltar} do Necromancy, com as sete casas nos lugares dele — o
 * sangue à esquerda, a alma à direita e as peças no meio, no feitio de um corpo.
 */
public class SummoningAltarMenu extends AbstractContainerMenu {
    private final Container altar;

    public SummoningAltarMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(7));
    }

    public SummoningAltarMenu(int id, Inventory inventory, Container altar) {
        super(TCMenus.SUMMONING_ALTAR, id);
        checkContainerSize(altar, 7);
        this.altar = altar;
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.BLOOD, 26, 40));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.SOUL, 134, 39));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.HEAD, 80, 19));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.TORSO, 80, 36));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.LEGS, 80, 53));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.ARM_RIGHT, 63, 36));
        this.addSlot(new Slot(altar, SummoningAltarBlockEntity.ARM_LEFT, 97, 36));

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(inventory, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(inventory, coluna, 8 + coluna * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.altar.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copia = stack.copy();
        if (index < 7) {
            if (!this.moveItemStackTo(stack, 7, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, 7, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copia;
    }
}
