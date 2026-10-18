package net.thaumcraft.occulta.leonard;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A <b>Urna do Leonard</b> aberta: o {@code ContainerLeonardsUrn} do Witchery.
 *
 * <p>As casas dela não estão em fila: estão num <b>losango</b>, e é essa a única coisa que a tela tem de
 * invulgar. A ordem é a do original, e é a que os quatro símbolos leem — <b>cima</b>, <b>baixo</b>,
 * <b>direita</b>, <b>esquerda</b>.
 *
 * <p>Enquanto ela está aberta, a casa do inventário onde ela está <b>não se mexe</b>: não se tira da mão
 * uma bolsa que se está a ver por dentro. É o mesmo que a Bolsa de Focos faz.
 */
public class UrnMenu extends AbstractContainerMenu {
    /** Onde cada casa fica na tela: o losango do original. */
    public static final int[][] LOSANGO = {{80, 22}, {80, 70}, {103, 46}, {56, 46}};

    /** Onde o inventário de quem joga começa. */
    public static final int INVENTÁRIO_EM = 103;

    private final SimpleContainer urna;
    private final int casas;
    private final int casaTrancada;

    /** O construtor do lado de quem joga: o número de casas vem pela rede. */
    public UrnMenu(int id, Inventory inventário, Integer casas) {
        this(id, inventário, casas == null ? 1 : casas);
    }

    /** E o do lado do servidor, que tem a urna à mão. */
    public UrnMenu(int id, Inventory inventário, ItemStack urna) {
        this(id, inventário, LeonardsUrnItem.casas(urna));
        NonNullList<ItemStack> dentro = LeonardsUrnItem.dentro(urna);
        for (int casa = 0; casa < Math.min(dentro.size(), this.casas); casa++) {
            this.urna.setItem(casa, dentro.get(casa));
        }
    }

    private UrnMenu(int id, Inventory inventário, int casas) {
        super(TCMenus.LEONARDS_URN, id);
        this.casas = Math.clamp(casas, 1, LeonardsUrnItem.TAMANHOS);
        this.urna = new SimpleContainer(this.casas);

        for (int casa = 0; casa < this.casas; casa++) {
            this.addSlot(new Slot(this.urna, casa, LOSANGO[casa][0], LOSANGO[casa][1]) {
                @Override
                public boolean mayPlace(ItemStack oquê) {
                    return LeonardsUrnItem.cabe(oquê);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
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
        this.casaTrancada = this.casas + 27 + inventário.getSelectedSlot();
    }

    /** Quantas casas esta urna tem. */
    public int casas() {
        return this.casas;
    }

    /** Qual casa da barra está trancada: a da urna. */
    public int barraTrancada() {
        return this.casaTrancada - this.casas - 27;
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
        ItemStack cópia = oquê.copy();
        if (índice < this.casas) {
            if (!this.moveItemStackTo(oquê, this.casas, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!LeonardsUrnItem.cabe(oquê) || !this.moveItemStackTo(oquê, 0, this.casas, false)) {
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

    /** O que ficou dentro volta para a urna que está na mão. */
    @Override
    public void removed(Player quem) {
        super.removed(quem);
        if (quem.level().isClientSide()) return;
        ItemStack naMão = quem.getInventory().getItem(this.barraTrancada());
        if (!(naMão.getItem() instanceof LeonardsUrnItem)) return;
        NonNullList<ItemStack> lista = NonNullList.withSize(this.casas, ItemStack.EMPTY);
        for (int casa = 0; casa < this.casas; casa++) lista.set(casa, this.urna.getItem(casa));
        LeonardsUrnItem.guarda(naMão, lista);
    }

    /** Abre-a, mandando o número de casas pela rede. */
    public static void abre(ServerPlayer quem, ItemStack urna) {
        quem.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<Integer>() {
            @Override
            public Integer getScreenOpeningData(ServerPlayer p) {
                return LeonardsUrnItem.casas(urna);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("item.thaumcraft.leonards_urn");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventário, Player p) {
                return new UrnMenu(id, inventário, urna);
            }
        });
    }
}
