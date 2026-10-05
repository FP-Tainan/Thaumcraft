package net.thaumcraft.occulta.goblin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.thaumcraft.occulta.OccultaItems;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * O <b>regatear do goblin</b>: o {@code addDefaultEquipmentAndRecipies} do {@code EntityGoblin}.
 *
 * <p>Ele é um aldeão de outra espécie, e isso vê-se melhor aqui do que em qualquer outro lado: as duas
 * tabelas de quantidades que ele usa são, <b>à letra</b>, as do aldeão da 1.7.10. O que muda é <b>o que ele
 * aceita em troca</b>.
 *
 * <h2>O koboldite não se mina</h2>
 *
 * <p>Não há minério de koboldite no mundo. Não há forno que o faça, não há caldeirão que o cozinhe, não há
 * rito que o invoque. Ele <b>só sai de um goblin</b>, e sai numa escada de três degraus que ele só abre um
 * de cada vez:
 *
 * <ol>
 *   <li><b>nove de pó</b> e cinco pepitas de ouro dão <b>uma pepita de koboldite</b>;</li>
 *   <li>feita essa, <b>dezesseis de pó</b> e um lingote de ouro dão <b>duas</b>;</li>
 *   <li>feitas as duas, <b>nove pepitas</b> e uma esmeralda dão <b>um lingote</b>.</li>
 * </ol>
 *
 * <p>E o pó, que é o primeiro degrau de todos, não se compra: ele cai <b>uma vez em três</b> no lugar da
 * esmeralda, quando se vende comida ou minério a um goblin do ofício certo. Quer dizer que o caminho até um
 * lingote de koboldite passa por <b>vender dezenas de coisas a dezenas de goblins</b> e ter sorte.
 *
 * <p>É de propósito que seja assim. O que se faz com o lingote — a <b>Estátua de Adoração</b> e o
 * <b>pentáculo</b> que dobra a recarga do altar — é tudo do fim do mod.
 *
 * <h2>Os quatro ofícios</h2>
 *
 * <p>O ofício é só a cara dele, e decide o que ele regateia: o <b>zero</b> vende comida e lã e troca cascalho
 * por esmeralda e pederneira; o <b>um</b> e o <b>dois</b> são os do koboldite, e <b>não se baralham</b> —
 * são a escada, e uma escada baralhada não é escada; o <b>três</b> é o ferreiro; o <b>quatro</b>, o
 * açougueiro e o curtidor.
 */
public final class GoblinTrades {
    /** Quantos usos uma troca aguenta: os sete do aldeão de 2014. */
    public static final int USOS = 7;

    /** E quanta experiência ela dá: <b>nenhuma</b>, porque o goblin não é aldeão e não a dá. */
    public static final int SEM_EXPERIÊNCIA = 0;

    /** De quantas em quantas vezes a esmeralda vira pó de koboldite. */
    public static final int UMA_EM_TRÊS = 3;

    /** Quanto a escada do koboldite pede e dá, degrau a degrau. */
    public static final int PÓ_DO_PRIMEIRO = 9;
    public static final int OURO_DO_PRIMEIRO = 5;
    public static final int PÓ_DO_SEGUNDO = 16;
    public static final int DÁ_O_SEGUNDO = 2;
    public static final int PEPITAS_DO_TERCEIRO = 9;

    /** Quanto cascalho o ofício zero troca por uma esmeralda, e quanta pederneira ela vale. */
    public static final int SAIBRO = 10;
    public static final int PEDERNEIRA = 4;

    /** O quanto de cada coisa que se dá por uma esmeralda: a {@code villagersSellingList} do aldeão. */
    private static final Map<Item, Quanto> VENDEM = new HashMap<>();

    /** E o quanto de cada coisa que o ferreiro compra ou vende: a {@code blacksmithSellingList}. */
    private static final Map<Item, Quanto> FERREIRO = new HashMap<>();

    /** Um intervalo de quantidade, que é o {@code Tuple} do original. */
    public record Quanto(int menos, int mais) {
        /** Quanto sai desta vez. */
        public int sorteia(RandomSource sorte) {
            return this.menos >= this.mais ? this.menos
                    : this.menos + sorte.nextInt(this.mais - this.menos);
        }
    }

    static {
        VENDEM.put(Items.COAL, new Quanto(16, 24));
        VENDEM.put(Items.IRON_INGOT, new Quanto(8, 10));
        VENDEM.put(Items.GOLD_INGOT, new Quanto(8, 10));
        VENDEM.put(Items.DIAMOND, new Quanto(4, 6));
        VENDEM.put(Items.PAPER, new Quanto(24, 36));
        VENDEM.put(Items.BOOK, new Quanto(11, 13));
        VENDEM.put(Items.WRITTEN_BOOK, new Quanto(1, 1));
        VENDEM.put(Items.ENDER_PEARL, new Quanto(3, 4));
        VENDEM.put(Items.ENDER_EYE, new Quanto(2, 3));
        VENDEM.put(Items.PORKCHOP, new Quanto(14, 18));
        VENDEM.put(Items.BEEF, new Quanto(14, 18));
        VENDEM.put(Items.CHICKEN, new Quanto(14, 18));
        VENDEM.put(Items.COOKED_COD, new Quanto(9, 13));
        VENDEM.put(Items.WHEAT_SEEDS, new Quanto(34, 48));
        VENDEM.put(Items.MELON_SEEDS, new Quanto(30, 38));
        VENDEM.put(Items.PUMPKIN_SEEDS, new Quanto(30, 38));
        VENDEM.put(Items.WHEAT, new Quanto(18, 22));
        VENDEM.put(Items.WOOL.pick(DyeColor.WHITE), new Quanto(14, 22));
        VENDEM.put(Items.ROTTEN_FLESH, new Quanto(36, 64));

        FERREIRO.put(Items.FLINT_AND_STEEL, new Quanto(3, 4));
        FERREIRO.put(Items.SHEARS, new Quanto(3, 4));
        FERREIRO.put(Items.IRON_SWORD, new Quanto(7, 11));
        FERREIRO.put(Items.DIAMOND_SWORD, new Quanto(12, 14));
        FERREIRO.put(Items.IRON_AXE, new Quanto(6, 8));
        FERREIRO.put(Items.DIAMOND_AXE, new Quanto(9, 12));
        FERREIRO.put(Items.IRON_PICKAXE, new Quanto(7, 9));
        FERREIRO.put(Items.DIAMOND_PICKAXE, new Quanto(10, 12));
        FERREIRO.put(Items.IRON_SHOVEL, new Quanto(4, 6));
        FERREIRO.put(Items.DIAMOND_SHOVEL, new Quanto(7, 8));
        FERREIRO.put(Items.IRON_HOE, new Quanto(4, 6));
        FERREIRO.put(Items.DIAMOND_HOE, new Quanto(7, 8));
        FERREIRO.put(Items.IRON_BOOTS, new Quanto(4, 6));
        FERREIRO.put(Items.DIAMOND_BOOTS, new Quanto(7, 8));
        FERREIRO.put(Items.IRON_HELMET, new Quanto(4, 6));
        FERREIRO.put(Items.DIAMOND_HELMET, new Quanto(7, 8));
        FERREIRO.put(Items.IRON_CHESTPLATE, new Quanto(10, 14));
        FERREIRO.put(Items.DIAMOND_CHESTPLATE, new Quanto(16, 19));
        FERREIRO.put(Items.IRON_LEGGINGS, new Quanto(8, 10));
        FERREIRO.put(Items.DIAMOND_LEGGINGS, new Quanto(11, 14));
        FERREIRO.put(Items.CHAINMAIL_BOOTS, new Quanto(5, 7));
        FERREIRO.put(Items.CHAINMAIL_HELMET, new Quanto(5, 7));
        FERREIRO.put(Items.CHAINMAIL_CHESTPLATE, new Quanto(11, 15));
        FERREIRO.put(Items.CHAINMAIL_LEGGINGS, new Quanto(9, 11));
        FERREIRO.put(Items.BREAD, new Quanto(-4, -2));
        FERREIRO.put(Items.MELON_SLICE, new Quanto(-8, -4));
        FERREIRO.put(Items.APPLE, new Quanto(-8, -4));
        FERREIRO.put(Items.COOKIE, new Quanto(-10, -7));
        FERREIRO.put(Items.GLASS, new Quanto(-5, -3));
        FERREIRO.put(Items.BOOKSHELF, new Quanto(3, 4));
        FERREIRO.put(Items.LEATHER_CHESTPLATE, new Quanto(4, 5));
        FERREIRO.put(Items.LEATHER_BOOTS, new Quanto(2, 4));
        FERREIRO.put(Items.LEATHER_HELMET, new Quanto(2, 4));
        FERREIRO.put(Items.LEATHER_LEGGINGS, new Quanto(2, 4));
        FERREIRO.put(Items.SADDLE, new Quanto(6, 8));
        FERREIRO.put(Items.EXPERIENCE_BOTTLE, new Quanto(-4, -1));
        FERREIRO.put(Items.REDSTONE, new Quanto(-4, -1));
        FERREIRO.put(Items.COMPASS, new Quanto(10, 12));
        FERREIRO.put(Items.CLOCK, new Quanto(10, 12));
        FERREIRO.put(Items.GLOWSTONE, new Quanto(-3, -1));
        FERREIRO.put(Items.COOKED_PORKCHOP, new Quanto(-7, -5));
        FERREIRO.put(Items.COOKED_BEEF, new Quanto(-7, -5));
        FERREIRO.put(Items.COOKED_CHICKEN, new Quanto(-8, -6));
        FERREIRO.put(Items.ENDER_EYE, new Quanto(7, 11));
        FERREIRO.put(Items.ARROW, new Quanto(-12, -8));
    }

    private GoblinTrades() {
    }

    /**
     * <b>Monta as trocas de um goblin.</b>
     *
     * <p>Ele <b>não abre a loja toda de uma vez</b>: cada vez que isto corre, só a primeira troca da lista
     * nova vai para a dele, e é por isso que um goblin recém-encontrado tem <b>uma</b> troca. As outras vêm
     * à medida que se lhe esgota a última, como no aldeão.
     *
     * @param sorte  o dado dele
     * @param ofício qual dos quatro ele é
     * @param tem    o que ele já oferece, que decide o degrau da escada do koboldite
     * @param quanto quantas trocas novas se lhe acrescentam
     */
    public static void monta(RandomSource sorte, int ofício, MerchantOffers tem, int quanto) {
        float ajuste = tem.isEmpty() ? 0.0f : net.minecraft.util.Mth.sqrt(tem.size()) * 0.2f;
        MerchantOffers novas = new MerchantOffers();
        boolean baralha = true;

        switch (ofício) {
            case 0 -> {
                porEsmeralda(novas, Items.WHEAT, sorte, ajusta(0.9f, ajuste));
                porEsmeralda(novas, Items.WOOL.pick(DyeColor.WHITE), sorte, ajusta(0.5f, ajuste));
                porEsmeralda(novas, Items.CHICKEN, sorte, ajusta(0.5f, ajuste));
                porEsmeralda(novas, Items.COOKED_COD, sorte, ajusta(0.4f, ajuste));
                compraOuVende(novas, Items.BREAD, sorte, ajusta(0.9f, ajuste));
                compraOuVende(novas, Items.MELON_SLICE, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.APPLE, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.COOKIE, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.SHEARS, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.FLINT_AND_STEEL, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.COOKED_CHICKEN, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.ARROW, sorte, ajusta(0.5f, ajuste));
                if (sorte.nextFloat() < ajusta(0.5f, ajuste)) {
                    novas.add(new MerchantOffer(new ItemCost(Items.GRAVEL, SAIBRO),
                            Optional.of(new ItemCost(Items.EMERALD)),
                            new ItemStack(Items.FLINT, PEDERNEIRA + sorte.nextInt(2)),
                            USOS, SEM_EXPERIÊNCIA, 0.0f));
                }
            }
            case 1, 2 -> {
                baralha = false;
                escadaDoKoboldite(novas, tem);
            }
            case 3 -> {
                porEsmeralda(novas, Items.COAL, sorte, ajusta(0.7f, ajuste));
                porEsmeralda(novas, Items.IRON_INGOT, sorte, ajusta(0.5f, ajuste));
                porEsmeralda(novas, Items.GOLD_INGOT, sorte, ajusta(0.5f, ajuste));
                porEsmeralda(novas, Items.DIAMOND, sorte, ajusta(0.5f, ajuste));
                for (Object[] par : new Object[][]{
                        {Items.IRON_SWORD, 0.5f}, {Items.DIAMOND_SWORD, 0.5f},
                        {Items.IRON_AXE, 0.3f}, {Items.DIAMOND_AXE, 0.3f},
                        {Items.IRON_PICKAXE, 0.5f}, {Items.DIAMOND_PICKAXE, 0.5f},
                        {Items.IRON_SHOVEL, 0.2f}, {Items.DIAMOND_SHOVEL, 0.2f},
                        {Items.IRON_HOE, 0.2f}, {Items.DIAMOND_HOE, 0.2f},
                        {Items.IRON_BOOTS, 0.2f}, {Items.DIAMOND_BOOTS, 0.2f},
                        {Items.IRON_HELMET, 0.2f}, {Items.DIAMOND_HELMET, 0.2f},
                        {Items.IRON_CHESTPLATE, 0.2f}, {Items.DIAMOND_CHESTPLATE, 0.2f},
                        {Items.IRON_LEGGINGS, 0.2f}, {Items.DIAMOND_LEGGINGS, 0.2f},
                        {Items.CHAINMAIL_BOOTS, 0.1f}, {Items.CHAINMAIL_HELMET, 0.1f},
                        {Items.CHAINMAIL_CHESTPLATE, 0.1f}, {Items.CHAINMAIL_LEGGINGS, 0.1f}}) {
                    compraOuVende(novas, (Item) par[0], sorte, ajusta((Float) par[1], ajuste));
                }
            }
            case 4 -> {
                porEsmeralda(novas, Items.COAL, sorte, ajusta(0.7f, ajuste));
                porEsmeralda(novas, Items.PORKCHOP, sorte, ajusta(0.5f, ajuste));
                porEsmeralda(novas, Items.BEEF, sorte, ajusta(0.5f, ajuste));
                compraOuVende(novas, Items.SADDLE, sorte, ajusta(0.1f, ajuste));
                compraOuVende(novas, Items.LEATHER_CHESTPLATE, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.LEATHER_BOOTS, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.LEATHER_HELMET, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.LEATHER_LEGGINGS, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.COOKED_PORKCHOP, sorte, ajusta(0.3f, ajuste));
                compraOuVende(novas, Items.COOKED_BEEF, sorte, ajusta(0.3f, ajuste));
            }
            default -> {
            }
        }

        /*
         * <b>Nenhum goblin fica sem nada para vender.</b> Se o dado recusou tudo — e com probabilidades de
         * um décimo isso acontece —, ele fica com a troca de ouro por esmeralda, que é o fundo do poço do
         * aldeão do original.
         */
        if (novas.isEmpty()) {
            novas.add(new MerchantOffer(
                    new ItemCost(Items.GOLD_INGOT, quantoPorEsmeralda(Items.GOLD_INGOT, sorte)),
                    new ItemStack(Items.EMERALD), USOS, SEM_EXPERIÊNCIA, 0.0f));
        }

        if (baralha) java.util.Collections.shuffle(novas, new java.util.Random(sorte.nextLong()));
        for (int volta = 0; volta < quanto && volta < novas.size(); volta++) {
            tem.add(novas.get(volta));
        }
    }

    /**
     * <b>A escada do koboldite</b>, que é a razão de ser do goblin mercador.
     *
     * <p>Qual degrau ele mostra depende de <b>quantas trocas ele já tem</b> — e, como os ofícios um e dois
     * nunca oferecem mais nada, isso é o mesmo que perguntar quantos degraus já se subiram com ele.
     */
    private static void escadaDoKoboldite(MerchantOffers novas, MerchantOffers tem) {
        if (tem.isEmpty()) {
            novas.add(new MerchantOffer(
                    new ItemCost(OccultaItems.KOBOLDITE_DUST, PÓ_DO_PRIMEIRO),
                    Optional.of(new ItemCost(Items.GOLD_NUGGET, OURO_DO_PRIMEIRO)),
                    new ItemStack(OccultaItems.KOBOLDITE_NUGGET),
                    USOS, SEM_EXPERIÊNCIA, 0.0f));
            return;
        }
        if (tem.size() == 1) {
            novas.add(new MerchantOffer(
                    new ItemCost(OccultaItems.KOBOLDITE_DUST, PÓ_DO_SEGUNDO),
                    Optional.of(new ItemCost(Items.GOLD_INGOT)),
                    new ItemStack(OccultaItems.KOBOLDITE_NUGGET, DÁ_O_SEGUNDO),
                    USOS, SEM_EXPERIÊNCIA, 0.0f));
            return;
        }
        if (tem.size() == 2) {
            novas.add(new MerchantOffer(
                    new ItemCost(OccultaItems.KOBOLDITE_NUGGET, PEPITAS_DO_TERCEIRO),
                    Optional.of(new ItemCost(Items.EMERALD)),
                    new ItemStack(OccultaItems.KOBOLDITE_INGOT),
                    USOS, SEM_EXPERIÊNCIA, 0.0f));
        }
    }

    /**
     * Uma troca de alguma coisa por uma esmeralda — <b>ou por pó de koboldite, uma vez em três</b>.
     *
     * <p>É a única porta de entrada do koboldite no jogo.
     */
    private static void porEsmeralda(MerchantOffers lista, Item oquê, RandomSource sorte, float chance) {
        if (sorte.nextFloat() >= chance) return;
        int quantos = quantoPorEsmeralda(oquê, sorte);
        ItemStack sai = sorte.nextInt(UMA_EM_TRÊS) == 0
                ? new ItemStack(OccultaItems.KOBOLDITE_DUST) : new ItemStack(Items.EMERALD);
        lista.add(new MerchantOffer(new ItemCost(oquê, quantos), sai,
                USOS, SEM_EXPERIÊNCIA, 0.0f));
    }

    /**
     * E uma troca do ferreiro, que se paga em <b>pepitas de koboldite</b> e não em esmeraldas.
     *
     * <p>Um número negativo na tabela quer dizer o contrário do positivo: em vez de <i>tantas pepitas por
     * uma peça</i>, é <i>uma pepita por tantas peças</i>. É assim que o aldeão do original diz «isto é
     * barato».
     */
    private static void compraOuVende(MerchantOffers lista, Item oquê, RandomSource sorte, float chance) {
        if (sorte.nextFloat() >= chance) return;
        int quantos = quantoDoFerreiro(oquê, sorte);
        if (quantos < 0) {
            lista.add(new MerchantOffer(new ItemCost(OccultaItems.KOBOLDITE_NUGGET),
                    new ItemStack(oquê, -quantos), USOS, SEM_EXPERIÊNCIA, 0.0f));
        } else {
            lista.add(new MerchantOffer(new ItemCost(OccultaItems.KOBOLDITE_NUGGET, quantos),
                    new ItemStack(oquê), USOS, SEM_EXPERIÊNCIA, 0.0f));
        }
    }

    public static int quantoPorEsmeralda(Item oquê, RandomSource sorte) {
        Quanto quanto = VENDEM.get(oquê);
        return quanto == null ? 1 : quanto.sorteia(sorte);
    }

    public static int quantoDoFerreiro(Item oquê, RandomSource sorte) {
        Quanto quanto = FERREIRO.get(oquê);
        return quanto == null ? 1 : quanto.sorteia(sorte);
    }

    /** O amortecedor do original: quanto mais trocas ele já tem, menos provável é cada uma nova. */
    private static float ajusta(float chance, float ajuste) {
        float conta = chance + ajuste;
        return conta > 0.9f ? 0.9f - (conta - 0.9f) : conta;
    }
}
