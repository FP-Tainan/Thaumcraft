package net.thaumcraft.arcana;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCMenus;

/**
 * A tela da Mesa de Inscrição por dentro: as nove casas da frase e a que devolve o feitiço.
 *
 * <p>As nove ficam em fila, porque uma frase se lê da esquerda para a direita e é a <b>ordem</b> que separa as
 * etapas: cada Forma começa uma etapa nova, e o que vier depois dela é dessa etapa.
 *
 * <p>A casa do feitiço <b>não gasta</b> as peças: tirar o feitiço de lá deixa a frase escrita, pronta para
 * fazer outro igual. É o que o original faz — lá as peças nem são coisas, são perícias sabidas.
 */
public class InscriptionMenu extends AbstractContainerMenu {
    private final Container mesa;

    /** O primeiro x das nove casas da frase, e o y delas. */
    public static final int RECIPE_X = 8;
    public static final int RECIPE_Y = 22;

    /** E onde fica a casa do feitiço. */
    public static final int RESULT_X = 80;
    public static final int RESULT_Y = 52;

    public InscriptionMenu(int id, Inventory mochila) {
        this(id, mochila, new SimpleContainer(InscriptionTableBlockEntity.SIZE));
    }

    public InscriptionMenu(int id, Inventory mochila, Container mesa) {
        super(TCMenus.INSCRIPTION_TABLE, id);
        checkContainerSize(mesa, InscriptionTableBlockEntity.SIZE);
        this.mesa = mesa;

        for (int i = 0; i < InscriptionTableBlockEntity.RECIPE_SIZE; i++) {
            this.addSlot(new Slot(mesa, i, RECIPE_X + i * 18, RECIPE_Y) {
                /**
                 * Peças — e <b>tinta</b>, que é a escolha da Cor.
                 *
                 * <p>No original a tinta é ingrediente da receita da Mesa, ao lado das peças. Aqui ela fica
                 * numa casa logo a seguir à Cor, que é o mesmo lugar lido da esquerda para a direita.
                 */
                @Override
                public boolean mayPlace(ItemStack isso) {
                    return isso.getItem() instanceof SpellPartItem || Modifiers.dye(isso) != null;
                }
            });
        }

        // a casa do feitiço: tira-se dela, mas não se põe nada nela
        this.addSlot(new Slot(mesa, InscriptionTableBlockEntity.RESULT, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack isso) {
                return false;
            }

            /**
             * Tirar o feitiço não gasta a frase: ela fica escrita, e a mesa escreve outro igual na hora.
             */
            @Override
            public void onTake(Player quem, ItemStack isso) {
                if (mesa instanceof InscriptionTableBlockEntity tábua) tábua.reread();
                super.onTake(quem, isso);
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

    /**
     * O que a mesa acha da frase, para a tela dizer.
     *
     * <p>A leitura é feita <b>aqui</b>, e não pedida ao bloco, porque o cliente não tem o bloco — ele tem um
     * baú de mentira com os mesmos itens dentro. E tem: as casas são sincronizadas como as de qualquer
     * inventário, e a gramática é conta pura. Ler de novo deste lado dá o mesmo que do outro, e poupa mandar
     * o resultado pela rede.
     */
    public SpellValidator.Result reading() {
        return SpellValidator.validateWithData(this.postas());
    }

    /** As casas da frase já lidas, com a tinta junto da peça a que ela pertence. */
    public java.util.List<SpellValidator.Posta> postas() {
        var casas = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < InscriptionTableBlockEntity.RECIPE_SIZE; i++) {
            casas.add(this.mesa.getItem(i));
        }
        return InscriptionTableBlockEntity.ler(casas, InscriptionTableBlockEntity.RECIPE_SIZE);
    }

    /** E só as peças, para quem só quiser a gramática. */
    public java.util.List<SpellPart> recipe() {
        return SpellValidator.onlyParts(this.postas());
    }

    @Override
    public ItemStack quickMoveStack(Player quem, int qual) {
        int mesaFim = InscriptionTableBlockEntity.SIZE;
        Slot casa = this.slots.get(qual);
        if (!casa.hasItem()) return ItemStack.EMPTY;

        ItemStack pilha = casa.getItem();
        ItemStack cópia = pilha.copy();

        if (qual < mesaFim) {
            // da mesa para a mochila
            if (!this.moveItemStackTo(pilha, mesaFim, this.slots.size(), true)) return ItemStack.EMPTY;
            casa.onQuickCraft(pilha, cópia);
        } else {
            // e da mochila para as casas da frase, se for peça ou tinta
            if (!(pilha.getItem() instanceof SpellPartItem) && Modifiers.dye(pilha) == null) {
                return ItemStack.EMPTY;
            }
            if (!this.moveItemStackTo(pilha, 0, InscriptionTableBlockEntity.RECIPE_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (pilha.isEmpty()) casa.setByPlayer(ItemStack.EMPTY);
        else casa.setChanged();
        if (pilha.getCount() == cópia.getCount()) return ItemStack.EMPTY;
        casa.onTake(quem, pilha);
        return cópia;
    }

    @Override
    public boolean stillValid(Player quem) {
        return this.mesa.stillValid(quem);
    }
}
