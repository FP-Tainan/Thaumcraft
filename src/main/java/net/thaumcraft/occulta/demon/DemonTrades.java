package net.thaumcraft.occulta.demon;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.thaumcraft.occulta.OccultaItems;

/**
 * <b>O que um Demônio vende</b>: o {@code addDefaultEquipmentAndRecipies} do {@code EntityDemon}.
 *
 * <p>A lista é sorteada quando alguém lhe fala pela primeira vez e tem <b>de seis a nove</b> trocas, contando
 * com o Coração. Ela se monta assim:
 *
 * <ul>
 *   <li>primeiro, <b>tantos livros encantados quantas trocas vão caber</b>, cada um com um encantamento
 *       sorteado e um preço que sobe com o grau dele;</li>
 *   <li>depois, uma de cada quatro vezes, <b>Pó Espectral</b> e <b>Língua de Cão</b>; e uma de cada seis e
 *       dois terços, <b>Sopa de Pedra Vermelha</b>, <b>duas lágrimas de ghast</b> por um diamante e
 *       <b>duas pérolas do fim</b> por um diamante;</li>
 *   <li>a lista é <b>embaralhada</b>;</li>
 *   <li>o <b>Coração de Demônio</b> é enfiado num dos <b>três primeiros lugares</b>;</li>
 *   <li>e então se <b>corta</b> no número de trocas que saiu.</li>
 * </ul>
 *
 * <p>A ordem disto é o que faz a lista ser o que é. Como os livros entram primeiro e tantos quantas as trocas
 * couberem, o embaralhar é o que decide quais das coisas boas sobrevivem ao corte — e o Coração, enfiado
 * <b>depois</b> de embaralhar, nunca é cortado. Um demônio pode não ter uma única coisa do ofício para
 * vender; ele tem sempre o coração.
 *
 * <h2>A moeda, e a armadilha</h2>
 *
 * <p>Cada troca é cobrada <b>numa moeda sorteada só para ela</b>: <b>um em cinco</b> em vara de blaze, um em
 * cinco em <b>creme de magma</b>, um em dez em diamante, um em quatro em esmeralda, e o resto em ouro. O
 * preço é o mesmo em valor; o que muda é quantas peças dele cabem numa esmeralda. E o Coração sorteia a dele
 * à parte: <b>trinta</b> peças, se for ouro, ou <b>três</b>.
 *
 * <p>E então a armadilha: pagar a um demônio com a <b>matéria do inferno</b> — vara de blaze ou creme de
 * magma — e ele aceita, entrega, e <b>estoura</b>. Como a moeda é por troca, a mesma lista pode ter uma troca
 * segura e outra que o solta, uma em cima da outra.
 */
public final class DemonTrades {
    /** Quantas trocas: de seis a nove. */
    public static final int QUANTAS = DemonEntity.TROCAS_DE;
    public static final int QUANTAS_A_MAIS = DemonEntity.TROCAS_A_MAIS;

    /**
     * Quantas vezes cada troca se pode fazer.
     *
     * <p>No original é o {@code increaseMaxTradeUses(-5)} sobre as <b>sete</b> de um pedido novo: sobram
     * <b>duas</b>. Um demônio vende dois corações e nunca mais.
     */
    public static final int ESTOQUE = 2;

    /** As fatias da moeda, pela ordem do original. */
    public static final double VARA = 0.2;
    public static final double CREME = 0.4;
    public static final double DIAMANTE = 0.5;
    public static final double ESMERALDA = 0.75;

    /** Quanto vale uma peça de cada moeda, em esmeraldas. */
    public static final int VALE_OURO = 1;
    public static final int VALE_ESMERALDA = 3;
    public static final int VALE_INFERNO = 4;
    public static final int VALE_DIAMANTE = 5;

    /** O desconto que a Língua do Diabo faz em cada moeda. */
    public static final int DESCONTO_DO_OURO = 5;
    public static final int DESCONTO_DA_ESMERALDA = 2;
    public static final int DESCONTO_DE_TUDO_O_MAIS = 1;

    /** E o que o Coração custa: trinta peças de ouro, ou três de qualquer outra coisa. */
    public static final int CORAÇÃO_EM_OURO = 30;
    public static final int CORAÇÃO = 3;

    private DemonTrades() {
    }

    /**
     * A moeda de <b>uma</b> troca.
     *
     * <p>No original isto está dentro do {@code getPrice}, e por isso é sorteado <b>a cada troca</b> e não
     * uma vez por demônio.
     */
    public static Item moeda(RandomSource sorte) {
        double qual = sorte.nextDouble();
        if (qual < VARA) return Items.BLAZE_ROD;
        if (qual < CREME) return Items.MAGMA_CREAM;
        if (qual < DIAMANTE) return Items.DIAMOND;
        return qual < ESMERALDA ? Items.EMERALD : Items.GOLD_INGOT;
    }

    /** Se esta moeda é a matéria do inferno — a que faz o demônio estourar quando a recebe. */
    public static boolean éFogo(Item moeda) {
        return moeda == Items.BLAZE_ROD || moeda == Items.MAGMA_CREAM;
    }

    /** Quanto vale uma peça desta moeda, em esmeraldas. */
    public static int vale(Item moeda) {
        if (moeda == Items.GOLD_INGOT) return VALE_OURO;
        if (moeda == Items.EMERALD) return VALE_ESMERALDA;
        if (moeda == Items.DIAMOND) return VALE_DIAMANTE;
        return VALE_INFERNO;
    }

    /** Quantas peças de uma moeda sorteada agora valem o preço pedido. */
    private static ItemStack preço(RandomSource sorte, int emEsmeraldas) {
        Item moeda = moeda(sorte);
        return new ItemStack(moeda, Math.max(1, emEsmeraldas / vale(moeda)));
    }

    /** Monta a lista deste demônio. */
    public static MerchantOffers monta(ServerLevel level) {
        RandomSource sorte = level.getRandom();
        int quantas = sorte.nextInt(QUANTAS_A_MAIS) + QUANTAS;
        List<MerchantOffer> lista = new ArrayList<>();

        // os livros encantados, cada um com o preço do grau que saiu
        var registro = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (int n = 0; n < quantas; n++) {
            var qual = registro.getRandomElementOf(net.minecraft.tags.EnchantmentTags.TRADEABLE, sorte);
            if (qual.isEmpty()) break;
            net.minecraft.core.Holder<Enchantment> encanto = qual.get();
            int grau = net.minecraft.util.Mth.nextInt(sorte,
                    encanto.value().getMinLevel(), encanto.value().getMaxLevel());
            ItemStack livro = net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                    new EnchantmentInstance(encanto, grau));
            int custa = 2 + sorte.nextInt(5 + grau * 10) + 3 * grau;
            lista.add(troca(preço(sorte, custa), livro));
        }

        // e o que o ofício compra dele: duas coisas uma em quatro, três uma em seis e dois terços
        if (sorte.nextDouble() < 0.25) {
            lista.add(troca(preço(sorte, sorte.nextInt(3) + 8),
                    new ItemStack(OccultaItems.SPECTRAL_DUST, sorte.nextInt(4) + 3)));
        }
        if (sorte.nextDouble() < 0.25) {
            lista.add(troca(preço(sorte, sorte.nextInt(3) + 8),
                    new ItemStack(OccultaItems.DOG_TONGUE, sorte.nextInt(4) + 4)));
        }
        if (sorte.nextDouble() < 0.15) {
            lista.add(troca(preço(sorte, sorte.nextInt(10) + 20),
                    new ItemStack(OccultaItems.REDSTONE_SOUP)));
        }
        if (sorte.nextDouble() < 0.15) {
            lista.add(troca(new ItemStack(Items.DIAMOND), new ItemStack(Items.GHAST_TEAR, 2)));
        }
        if (sorte.nextDouble() < 0.15) {
            lista.add(troca(new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_PEARL, 2)));
        }

        /*
         * Embaralhar, enfiar o Coração num dos três primeiros lugares, e cortar no número que saiu. O Coração
         * entra depois de embaralhar e por isso nunca é cortado: o original quer que a primeira coisa que se
         * veja ao abrir a lista seja a possibilidade de comprar um coração.
         */
        java.util.Collections.shuffle(lista, new java.util.Random(sorte.nextLong()));
        Item moedaDoCoração = moeda(sorte);
        lista.add(Math.min(sorte.nextInt(3), lista.size()), troca(
                new ItemStack(moedaDoCoração,
                        moedaDoCoração == Items.GOLD_INGOT ? CORAÇÃO_EM_OURO : CORAÇÃO),
                new ItemStack(OccultaItems.DEMON_HEART)));

        MerchantOffers quais = new MerchantOffers();
        for (int n = 0; n < quantas && n < lista.size(); n++) quais.add(lista.get(n));
        return quais;
    }

    /**
     * A <b>mesma lista, mais barata</b>: o desconto da Língua do Diabo.
     *
     * <p><b>Cinco</b> de desconto no ouro, <b>dois</b> na esmeralda, <b>nada</b> no diamante e <b>um</b> em
     * tudo o mais — nunca abaixo de um. O diamante não desconta porque já é o que ele menos pede.
     *
     * <p>A lista que sai é <b>outra</b>, e é de propósito: o desconto não se guarda no demônio, de modo que
     * largar a língua põe os preços de volta no lugar na tela seguinte. O original faz o mesmo, copiando cada
     * troca pelo NBT dela.
     *
     * <p><b>E com isso vem uma coisa do original que fica:</b> o que se compra na lista barata não gasta o
     * estoque da lista verdadeira. Quem negocia com a Língua do Diabo na mão compra as duas peças de cada
     * troca e depois mais duas, porque os usos se contam na cópia. É um buraco de 2014; fica, porque é dele,
     * e porque fechá-lo mudaria o preço que a língua já cobra — cinco usos por troca.
     */
    public static MerchantOffers maisBarato(MerchantOffers quais) {
        MerchantOffers baratas = new MerchantOffers();
        for (MerchantOffer cada : quais) {
            ItemStack custa = cada.getCostA();
            int quanto = Math.max(custa.getCount() - desconto(custa.getItem()), 1);
            MerchantOffer barata = new MerchantOffer(new ItemCost(custa.getItem(), quanto),
                    cada.getResult(), cada.getMaxUses(), cada.getXp(), cada.getPriceMultiplier());
            for (int n = 0; n < cada.getUses(); n++) barata.increaseUses();
            baratas.add(barata);
        }
        return baratas;
    }

    /** O desconto de cada moeda. */
    public static int desconto(Item moeda) {
        if (moeda == Items.GOLD_INGOT) return DESCONTO_DO_OURO;
        if (moeda == Items.EMERALD) return DESCONTO_DA_ESMERALDA;
        return moeda == Items.DIAMOND ? 0 : DESCONTO_DE_TUDO_O_MAIS;
    }

    private static MerchantOffer troca(ItemStack custa, ItemStack dá) {
        return new MerchantOffer(new ItemCost(custa.getItem(), custa.getCount()), dá, ESTOQUE, 0, 0.0f);
    }
}
