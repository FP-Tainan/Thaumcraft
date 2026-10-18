package net.thaumcraft.occulta.leonard;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A <b>Urna na mochila</b>: o {@code InvUtil.getSlotContainingItem} que os quatro símbolos do Leonard
 * chamam antes de qualquer outra coisa.
 *
 * <p>Eles não pedem a urna na mão — pedem-na <b>em qualquer casa</b> do inventário, e usam a
 * <b>primeira</b> que acharem. É o que faz deles feitiços e não um jeito de usar um item: anda-se com a
 * urna cheia na mochila e lança-se o desenho com a vara.
 */
public final class UrnPouch {
    /** Quando não há urna nenhuma. */
    public static final int EM_LUGAR_NENHUM = -1;

    private UrnPouch() {
    }

    /** Em que casa do inventário está a primeira urna, ou {@link #EM_LUGAR_NENHUM}. */
    public static int naMochila(Player quem) {
        var mochila = quem.getInventory();
        for (int casa = 0; casa < mochila.getContainerSize(); casa++) {
            if (mochila.getItem(casa).getItem() instanceof LeonardsUrnItem) return casa;
        }
        return EM_LUGAR_NENHUM;
    }

    /** O frasco que está naquela casa da urna, <b>sem o tirar</b>. */
    public static ItemStack espreita(Player quem, int ondeEstá, int casa) {
        if (ondeEstá == EM_LUGAR_NENHUM) return ItemStack.EMPTY;
        ItemStack urna = quem.getInventory().getItem(ondeEstá);
        if (!(urna.getItem() instanceof LeonardsUrnItem)) return ItemStack.EMPTY;
        return LeonardsUrnItem.naCasa(urna, casa);
    }

    /**
     * E tira-o: a urna fica com a casa vazia.
     *
     * <p><b>Desvio declarado.</b> O original <b>não tira</b> — ele lê o frasco e atira uma cópia, e a urna
     * fica cheia para sempre. Um cozimento de Dano de grau quatro passaria a ser um feitiço de graça, sem
     * fim, pago só com cargas de infusão; e a dica do próprio item diz «ponha um cozimento na urna», no
     * singular, que é a linguagem de quem gasta. Aqui o frasco sai.
     */
    public static ItemStack tira(Player quem, int ondeEstá, int casa) {
        ItemStack frasco = espreita(quem, ondeEstá, casa);
        if (frasco.isEmpty()) return ItemStack.EMPTY;

        ItemStack urna = quem.getInventory().getItem(ondeEstá);
        NonNullList<ItemStack> dentro = LeonardsUrnItem.dentro(urna);
        dentro.set(casa, ItemStack.EMPTY);
        LeonardsUrnItem.guarda(urna, dentro);
        return frasco.copy();
    }
}
