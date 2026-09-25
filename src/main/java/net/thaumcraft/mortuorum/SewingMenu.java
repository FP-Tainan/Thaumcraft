package net.thaumcraft.mortuorum;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela da Máquina de Costura: o {@code ContainerSewing} do Necromancy.
 *
 * <p>Uma grade de <b>quatro por quatro</b>, as duas casas do que a costura gasta — a agulha e a linha — e a casa
 * do que sai. Sem agulha ou sem linha, nada sai, mesmo que o desenho esteja certo.
 */
public class SewingMenu extends AbstractContainerMenu {
    private final Container machine;
    private final Container grid = new SimpleContainer(SewingRecipe.GRID * SewingRecipe.GRID) {
        @Override
        public void setChanged() {
            super.setChanged();
            SewingMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();
    private final Player player;

    public SewingMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(2));
    }

    public SewingMenu(int id, Inventory inventory, Container machine) {
        super(TCMenus.SEWING, id);
        checkContainerSize(machine, 2);
        this.machine = machine;
        this.player = inventory.player;

        // a grade, quatro por quatro
        for (int linha = 0; linha < SewingRecipe.GRID; linha++) {
            for (int coluna = 0; coluna < SewingRecipe.GRID; coluna++) {
                this.addSlot(new Slot(this.grid, coluna + linha * SewingRecipe.GRID, 8 + coluna * 18, 8 + linha * 18));
            }
        }
        // a agulha e a linha
        this.addSlot(new Slot(machine, SewingMachineBlockEntity.NEEDLE, 95, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(MortuorumItems.BONE_NEEDLE);
            }
        });
        this.addSlot(new Slot(machine, SewingMachineBlockEntity.THREAD, 95, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.world.item.Items.STRING);
            }
        });
        // e o que sai
        this.addSlot(new SewingResultSlot(this.player, this.grid, this.machine, this.result, 0, 145, 35));

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(inventory, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(inventory, coluna, 8 + coluna * 18, 142));
        }
    }

    /** O que a grade faz agora — se houver agulha e linha na máquina. */
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.player.level().isClientSide()) return;
        boolean pronta = this.machine.getItem(SewingMachineBlockEntity.NEEDLE).is(MortuorumItems.BONE_NEEDLE)
                && this.machine.getItem(SewingMachineBlockEntity.THREAD).is(net.minecraft.world.item.Items.STRING);
        SewingRecipe recipe = pronta ? SewingRecipe.find(this.grid) : null;
        this.result.setItem(0, recipe == null ? ItemStack.EMPTY : recipe.result().copy());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            this.clearContainer(player, this.grid);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.machine.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // o que se move com o clique de atalho: da grade e das casas da máquina para a mochila, e da mochila
        // para a primeira casa que aceitar
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copia = stack.copy();
        int grade = SewingRecipe.GRID * SewingRecipe.GRID;
        int mochila = grade + 3;
        if (index < mochila) {
            if (!this.moveItemStackTo(stack, mochila, this.slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copia);
        } else if (!this.moveItemStackTo(stack, 0, grade + 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copia;
    }
}
