package net.thaumcraft.arcana.machine;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela do Obelisco: o {@code ContainerObelisk} da 1.4.0.009.
 *
 * <p>Uma casa só, no meio, em <b>(79, 47)</b>, e por cima dela o mostrador da queima — catorze pixels que
 * descem conforme o combustível se gasta. As medidas são as do original.
 *
 * <p>A casa <b>só aceita o que arde</b>, e é o próprio Obelisco que diz o que é que arde.
 */
public class ObeliskMenu extends AbstractContainerMenu {
    /** Quantos números a tela precisa: quanto falta queimar e quanto era ao todo. */
    public static final int DATA_SIZE = 2;
    public static final int DATA_BURN = 0;
    public static final int DATA_BURN_TOTAL = 1;

    /** A casa do combustível, no lugar do original. */
    public static final int SLOT_X = 79;
    public static final int SLOT_Y = 47;

    private final Container obelisco;
    private final ContainerData dados;

    public ObeliskMenu(int id, Inventory mochila) {
        this(id, mochila, new SimpleContainer(1), new SimpleContainerData(DATA_SIZE));
    }

    public ObeliskMenu(int id, Inventory mochila, ObeliskBlockEntity obelisco) {
        this(id, mochila, obelisco, obelisco.data());
    }

    public ObeliskMenu(int id, Inventory mochila, Container obelisco, ContainerData dados) {
        super(TCMenus.OBELISK, id);
        checkContainerSize(obelisco, 1);
        checkContainerDataCount(dados, DATA_SIZE);
        this.obelisco = obelisco;
        this.dados = dados;
        this.addDataSlots(dados);
        obelisco.startOpen(mochila.player);

        this.addSlot(new Slot(obelisco, 0, SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack pilha) {
                return ObeliskFuel.burnTime(pilha) > 0;
            }
        });

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(mochila, coluna + linha * 9 + 9, 8 + coluna * 18, 84 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(mochila, coluna, 8 + coluna * 18, 142));
        }
    }

    /** O quanto falta da queima, de zero a um. */
    public float burning() {
        int total = this.dados.get(DATA_BURN_TOTAL);
        return total <= 0 ? 0.0f : Math.min(1.0f, this.dados.get(DATA_BURN) / (float) total);
    }

    @Override
    public ItemStack quickMoveStack(Player quem, int qual) {
        ItemStack movido = ItemStack.EMPTY;
        Slot casa = this.slots.get(qual);
        if (casa == null || !casa.hasItem()) return movido;

        ItemStack pilha = casa.getItem();
        movido = pilha.copy();
        if (qual < 1) {
            if (!this.moveItemStackTo(pilha, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (ObeliskFuel.burnTime(pilha) > 0) {
            if (!this.moveItemStackTo(pilha, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (pilha.isEmpty()) casa.set(ItemStack.EMPTY);
        else casa.setChanged();
        return movido;
    }

    @Override
    public boolean stillValid(Player quem) {
        return this.obelisco.stillValid(quem);
    }

    @Override
    public void removed(Player quem) {
        super.removed(quem);
        this.obelisco.stopOpen(quem);
    }
}
