package net.thaumcraft.occulta.brew;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A <b>Bolsa de Cozimentos</b> aberta: o {@code ContainerBrewBag} do Witchery.
 *
 * <p>Oito casas numa <b>fila só</b>, e não nove — a bolsa conta por oito porque os rumos da cabeça são
 * oito, e a ordem delas na fila é a mesma ordem da roda: a primeira é o <b>cima</b>, e dali seguem à roda
 * do relógio. Quem arruma a bolsa está arrumando a roda.
 *
 * <p>Só cozimento de atirar entra, e isso vale tanto para a mão como para o <b>empurrão</b> do turno: no
 * original, o {@code transferStackInSlot} começa por recusar o que não é cozimento, de modo que um
 * diamante no inventário não vai parar à bolsa por descuido.
 *
 * <p>Enquanto ela está aberta, a casa do inventário onde ela está <b>não se mexe</b>: não se tira da mão
 * uma bolsa que se está vendo por dentro. É o mesmo que a Urna do Leonard faz.
 */
public class BrewBagMenu extends AbstractContainerMenu {
    /** Onde a fila de casas começa, e onde o inventário de quem joga começa. */
    public static final int CASAS_EM_X = 16;
    public static final int CASAS_EM_Y = 18;
    public static final int INVENTÁRIO_EM = 49;

    private final SimpleContainer bolsa = new SimpleContainer(BrewBagItem.CASAS);
    private final int casaTrancada;

    /** O construtor do lado de quem joga. */
    public BrewBagMenu(int id, Inventory inventário) {
        super(TCMenus.BREW_BAG, id);

        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) {
            this.addSlot(new Slot(this.bolsa, casa, CASAS_EM_X + casa * 18, CASAS_EM_Y) {
                @Override
                public boolean mayPlace(ItemStack oquê) {
                    return BrewBagItem.cabe(oquê);
                }
            });
        }
        for (int fila = 0; fila < 3; fila++) {
            for (int casa = 0; casa < 9; casa++) {
                this.addSlot(new Slot(inventário, casa + fila * 9 + 9,
                        8 + casa * 18, INVENTÁRIO_EM + fila * 18));
            }
        }
        for (int casa = 0; casa < 9; casa++) {
            this.addSlot(new Slot(inventário, casa, 8 + casa * 18, INVENTÁRIO_EM + 58));
        }
        this.casaTrancada = BrewBagItem.CASAS + 27 + inventário.getSelectedSlot();
    }

    /** E o do lado do servidor, que tem a bolsa à mão. */
    public BrewBagMenu(int id, Inventory inventário, ItemStack bolsa) {
        this(id, inventário);
        NonNullList<ItemStack> dentro = BrewBagItem.dentro(bolsa);
        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) {
            this.bolsa.setItem(casa, dentro.get(casa));
        }
    }

    /** Qual casa da barra está trancada: a da bolsa. */
    public int barraTrancada() {
        return this.casaTrancada - BrewBagItem.CASAS - 27;
    }

    @Override
    public void clicked(int casa, int botão, ContainerInput jeito, Player quem) {
        if (casa == this.casaTrancada) return;
        if (jeito == ContainerInput.SWAP && botão == this.barraTrancada()) return;
        super.clicked(casa, botão, jeito, quem);
    }

    @Override
    public ItemStack quickMoveStack(Player quem, int índice) {
        if (índice == this.casaTrancada) return ItemStack.EMPTY;
        Slot casa = this.slots.get(índice);
        if (!casa.hasItem()) return ItemStack.EMPTY;
        ItemStack oquê = casa.getItem();
        // o original recusa aqui o que não é cozimento, nos dois sentidos
        if (!BrewBagItem.cabe(oquê)) return ItemStack.EMPTY;

        ItemStack cópia = oquê.copy();
        if (índice < BrewBagItem.CASAS) {
            if (!this.moveItemStackTo(oquê, BrewBagItem.CASAS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(oquê, 0, BrewBagItem.CASAS, false)) {
            return ItemStack.EMPTY;
        }
        if (oquê.isEmpty()) casa.setByPlayer(ItemStack.EMPTY);
        else casa.setChanged();
        return cópia;
    }

    @Override
    public boolean stillValid(Player quem) {
        return true;
    }

    /** O que ficou dentro volta para a bolsa que está na mão. */
    @Override
    public void removed(Player quem) {
        super.removed(quem);
        if (quem.level().isClientSide()) return;
        ItemStack naMão = quem.getInventory().getItem(this.barraTrancada());
        if (!(naMão.getItem() instanceof BrewBagItem)) return;
        NonNullList<ItemStack> lista = NonNullList.withSize(BrewBagItem.CASAS, ItemStack.EMPTY);
        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) lista.set(casa, this.bolsa.getItem(casa));
        BrewBagItem.guarda(naMão, lista);
    }

    /** Abre-a. */
    public static void abre(ServerPlayer quem, ItemStack bolsa) {
        quem.openMenu(new SimpleMenuProvider(
                (id, inventário, p) -> new BrewBagMenu(id, inventário, bolsa),
                Component.translatable("item.thaumcraft.brew_bag")));
    }
}
