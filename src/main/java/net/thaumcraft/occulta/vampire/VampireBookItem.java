package net.thaumcraft.occulta.vampire;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * <b>Observações de um Imortal</b>: o {@code VAMPIRE_BOOK} do Witchery.
 *
 * <p>É o diário de um erudito condenado que jantou com um vampiro e anotou o que ele contou — em reticências,
 * meias-frases e desenhos à pressa. Não é um manual: é um <b>testemunho</b>, e o autor não sabia que estava
 * a escrever as instruções de uma escada.
 *
 * <h2>O que ele faz, e é uma coisa só</h2>
 *
 * <p>Lê-lo <b>levanta o teto do grau</b> do vampiro até o número de páginas que ele tem. Um vampiro sem
 * livro para no <b>terceiro</b> grau para sempre, por mais aldeões que morda — e nada no jogo lhe diz porquê.
 *
 * <p>E o livro chega <b>rasgado</b>. Ele vem sem nenhuma das nove páginas, e cada uma delas tem de ser
 * encontrada: nos baús da <b>livraria de aldeia</b>, ou no corpo do que se mata. É por isso que a escada do
 * vampiro leva tanto tempo — não é a escada que é lenta, é o <b>livro</b>.
 *
 * <h2>E só se lê até onde ele chega</h2>
 *
 * <p>Cada capítulo pede um número de páginas, e um capítulo que peça mais do que o livro tem <b>não abre</b>.
 * Quem tiver três páginas lê até o terceiro degrau e vê, na folha seguinte, que há mais. É a melhor coisa
 * que este livro faz: ele mostra <b>que falta</b> sem dizer o quê.
 */
public class VampireBookItem extends Item {
    /** Quantas páginas um livro inteiro tem. */
    public static final int PÁGINAS = 9;

    public VampireBookItem(Properties properties) {
        super(properties);
    }

    /** Quantas páginas este exemplar tem. */
    public static int páginas(ItemStack livro) {
        Integer tem = livro.get(net.thaumcraft.occulta.OccultaComponents.VAMPIRE_PAGES);
        return tem == null ? 0 : Math.clamp(tem, 0, PÁGINAS);
    }

    /** Põe o número de páginas. */
    public static ItemStack com(int quantas) {
        ItemStack livro = new ItemStack(net.thaumcraft.occulta.OccultaItems.VAMPIRE_BOOK);
        if (quantas > 0) {
            livro.set(net.thaumcraft.occulta.OccultaComponents.VAMPIRE_PAGES,
                    Math.clamp(quantas, 0, PÁGINAS));
        }
        return livro;
    }

    /** Se este livro ainda aceita páginas, que é o que faz as páginas cair. */
    public static boolean incompleto(ItemStack livro) {
        return livro.is(net.thaumcraft.occulta.OccultaItems.VAMPIRE_BOOK) && páginas(livro) < PÁGINAS;
    }

    /**
     * <b>Trazer um livro incompleto faz as páginas aparecerem</b>: o ramo do {@code LivingDropsEvent} do
     * original.
     *
     * <p>É a única coisa do mod que funciona assim, e é de propósito: as páginas do livro não estão no mundo
     * à espera, elas <b>só existem para quem já anda à procura delas</b>. Quem nunca achou o primeiro
     * exemplar nunca verá uma página cair.
     */
    public static boolean traz(Player quem) {
        for (int slot = 0; slot < quem.getInventory().getContainerSize(); slot++) {
            if (incompleto(quem.getInventory().getItem(slot))) return true;
        }
        return false;
    }

    /**
     * Abrir o livro <b>levanta o teto do grau</b>: é o {@code onBookRead} do original.
     *
     * <p>Quem abre a folha é a máquina de quem lê, no {@code ThaumcraftClient}, para que nada de desenho
     * encoste no lado do servidor. Deste lado fica só o teto, que é a única coisa que o livro faz.
     */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        if (level instanceof net.minecraft.server.level.ServerLevel) {
            Vampire.levantaOTeto(quem, páginas(quem.getItemInHand(mão)) + 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack livro, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, net.minecraft.world.item.TooltipFlag bandeira) {
        linha.accept(Component.translatable("item.thaumcraft.vampire_book.tip")
                .withStyle(ChatFormatting.DARK_GRAY));
        int tem = páginas(livro);
        linha.accept(Component.translatable("item.thaumcraft.vampire_book.pages", tem, PÁGINAS)
                .withStyle(tem >= PÁGINAS ? ChatFormatting.GOLD : ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack livro) {
        return páginas(livro) >= PÁGINAS;
    }

    /** Um livro inteiro é raro; um rasgado é uma coisa que se achou no chão. */
    public static Rarity raridade(ItemStack livro) {
        return páginas(livro) >= PÁGINAS ? Rarity.RARE : Rarity.UNCOMMON;
    }
}
