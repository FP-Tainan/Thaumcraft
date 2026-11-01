package net.thaumcraft.occulta;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Os <b>Cozimentos das Bruxas</b>: o {@code CAULDRON_BOOK} do Witchery.
 *
 * <p>É o <b>manual</b> do ofício — o único livro do mod que explica em vez de insinuar. O Livro do Vampiro
 * é um testemunho em reticências; este é o contrário: índice, capítulos, receitas desenhadas com as coisas
 * em cima da bancada, e atalhos de um capítulo para o outro.
 *
 * <p>Ele cobre o <b>caldeirão inteiro</b>: as três coisas que se fazem nele — cozer, ritual e fabrico —, e
 * depois as cinco contas de um cozimento: <b>espaço</b>, <b>poder</b>, <b>duração</b>, <b>temperos</b> e
 * <b>dispersão</b>. Quem ler isto sabe cozinhar; quem não ler, adivinha.
 *
 * <p>Ao contrário do Livro do Vampiro, ele <b>não chega rasgado</b>: nasce com as sete páginas e abre-se
 * todo. O que se sabe não está trancado atrás de achar folhas — está trancado atrás de ler.
 */
public class CauldronBookItem extends Item {
    /** Quantas páginas ele tem: as sete do {@code ItemMarkupBook(7)} do original. */
    public static final int PÁGINAS = 7;

    /** E por onde ele abre. */
    public static final String PRIMEIRA = "toc";

    /** A raiz das chaves dele no arquivo de idioma. */
    public static final String RAIZ = "tc.cauldronbook";

    public CauldronBookItem(Properties properties) {
        super(properties);
    }

    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1);
    }

    /**
     * Clicar abre-o.
     *
     * <p>Quem o abre é o <b>lado de cá</b>, por um gancho de uso — é o caminho que o Livro do Vampiro já
     * faz, e existe para que o item não tenha de saber que há um cliente.
     */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        return InteractionResult.SUCCESS;
    }
}
