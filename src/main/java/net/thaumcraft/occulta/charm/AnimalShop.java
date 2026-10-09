package net.thaumcraft.occulta.charm;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <b>A loja de um bicho</b>: o estoque que o {@code AnimalMerchant} do {@code ItemPolynesiaCharm} monta.
 *
 * <p>Encantado o bicho, ele passa a ter o que vender — e o estoque é sorteado <b>uma vez</b> e fica com ele
 * para sempre. Cada vaca tem a sua loja, e a vaca do vizinho tem outra.
 *
 * <h2>A conta</h2>
 *
 * <p>Vale descrevê-la porque ela é o que torna a coisa um jogo:
 *
 * <ol>
 *   <li>uma <b>coisa ao acaso</b> de dezoito, que vai de raiz de mandrágora a bola de barro, e que toda loja
 *       tem;
 *   <li>mais o que a <b>espécie</b> dá — o porco dá cogumelos, a galinha penas e ovos, a aranha fio —, e com
 *       ela as <b>moedas</b> que aquela espécie aceita;
 *   <li>e, por cima, as raridades: duas em cem de esmeralda no porco e no lobo, uma em cem de diamante, uma
 *       em cem de <b>sela</b> no cavalo;
 *   <li>de tudo isso sorteiam-se <b>uma ou duas</b> ofertas, e o resto perde-se.
 * </ol>
 *
 * <p>E cada oferta aguenta <b>uma ou duas</b> trocas: o original pega nas sete que o jogo dá a uma oferta de
 * aldeão e desconta seis, ou cinco. Uma loja de bicho é um bolso, não uma venda.
 *
 * <h2>O que a espécie decide</h2>
 *
 * <p>A ordem dos ramos é a do original, e nela há um <b>acidente que fica</b>: a vaca vem antes da
 * vaca-de-cogumelo, e no jogo de 2014 a de cogumelo <b>era</b> uma vaca. Por isso ela caía no ramo da vaca e
 * aceitava <b>trigo</b>, e o ramo que lhe foi escrito nunca corria.
 *
 * <p>Hoje as duas são irmãs em vez de mãe e filha, e por isso a pergunta aqui é pela <b>mãe das duas</b> — que
 * é o que faz a vaca-de-cogumelo continuar aceitando trigo, como sempre aceitou. O ramo dela fica escrito e
 * fica morto, que é o estado dele desde 2014. Veja o desvio declarado no PORTE.
 *
 * <h2>Duas coisas que esperam</h2>
 *
 * <p>Faltam da loja a <b>semente de Treefyd</b>, que qualquer bicho dava três em cem e o creeper dez, e a
 * <b>Teia Densa</b>, que a aranha dava quatro. Nenhuma das duas está portada ainda — a semente espera o
 * Treefyd, e a teia espera o frasco que se atira —, de modo que a aranha vende só fio e o sorteio da semente
 * não se faz. Veja o desvio declarado no PORTE.
 */
public final class AnimalShop {
    /** A loja daquele bicho, guardada nele: o {@code WitcheryShopStock} do original. */
    public static final AttachmentType<MerchantOffers> ESTOQUE =
            AttachmentRegistry.<MerchantOffers>builder()
                    .persistent(MerchantOffers.CODEC)
                    .buildAndRegister(Thaumcraft.id("animal_shop"));

    /** Quantas ofertas sobram no fim: uma ou duas. */
    public static final int ATÉ = 2;

    /** O que as sete trocas de uma oferta de aldeão levam de desconto: seis, ou cinco. */
    public static final int TROCAS = 7;
    public static final int DESCONTO = 6;

    /** As raridades. */
    public static final double ESMERALDA = 0.02;
    public static final double DIAMANTE = 0.01;
    public static final double SELA = 0.01;
    public static final double PÓ_ESPECTRAL = 0.05;
    public static final double CORAÇÃO = 0.02;

    /**
     * A chance da semente de Treefyd em qualquer bicho, e a do creeper.
     *
     * <p>Ficam escritas porque são do original; o sorteio não se faz enquanto o Treefyd não estiver portado.
     */
    public static final double TREEFYD = 0.03;
    public static final double TREEFYD_DO_CREEPER = 0.1;

    /** O que vale o dobro no preço: as coisas caras, e tudo o que um morto-vivo vende. */
    public static final int CARO = 2;

    private AnimalShop() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o que fica no bicho

    /** Se aquele bicho já tem loja: o {@code hasStockInventory} do original. */
    public static boolean temLoja(LivingEntity bicho) {
        return bicho.hasAttached(ESTOQUE);
    }

    /**
     * Dá-lhe uma loja <b>vazia</b>: o {@code setEmptyStockInventory} do original.
     *
     * <p>É o que o Apanha-Bicho faz ao soltar um morcego que já tinha sido vendido — um morcego não se
     * engarrafa duas vezes.
     */
    public static void lojaVazia(LivingEntity bicho) {
        bicho.setAttached(ESTOQUE, new MerchantOffers());
    }

    /** A loja daquele bicho, abrindo-a na primeira vez. */
    public static MerchantOffers loja(LivingEntity bicho) {
        MerchantOffers tem = bicho.getAttached(ESTOQUE);
        if (tem != null) return tem;

        MerchantOffers feitas = monta(bicho);
        bicho.setAttached(ESTOQUE, feitas);
        return feitas;
    }

    /** Guarda a loja de volta no bicho, que é o que faz a troca gasta ficar gasta. */
    public static void guarda(LivingEntity bicho, MerchantOffers quais) {
        bicho.setAttached(ESTOQUE, quais);
    }

    // ------------------------------------------------------------------ o estoque

    /** As dezoito coisas que qualquer bicho pode ter. */
    public static List<ItemStack> asDezoito() {
        return List.of(
                new ItemStack(OccultaItems.MANDRAKE_ROOT, 3),
                new ItemStack(OccultaItems.BELLADONNA_FLOWER, 3),
                new ItemStack(OccultaItems.WATER_ARTICHOKE_GLOBE, 3),
                new ItemStack(Items.OAK_SAPLING, 4),
                new ItemStack(Items.SPRUCE_SAPLING, 4),
                new ItemStack(Items.BIRCH_SAPLING, 4),
                new ItemStack(Items.JUNGLE_SAPLING, 4),
                new ItemStack(Items.SUGAR_CANE, 2),
                new ItemStack(Items.CACTUS, 2),
                new ItemStack(Items.GOLD_NUGGET, 5),
                new ItemStack(Items.IRON_INGOT, 2),
                new ItemStack(Items.BONE, 4),
                new ItemStack(Items.FLINT, 5),
                new ItemStack(OccultaItems.DOG_TONGUE, 2),
                new ItemStack(Items.POTATO, 5),
                new ItemStack(Items.POISONOUS_POTATO, 2),
                new ItemStack(Items.CARROT, 5),
                new ItemStack(Items.CLAY_BALL, 10));
    }

    /** Se este bicho é morto-vivo: o {@code isEntityUndead} do original. */
    public static boolean mortoVivo(LivingEntity bicho) {
        return bicho.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
    }

    /**
     * Monta a loja de um bicho: o {@code populateList} do original, espécie por espécie.
     *
     * <p>A ordem dos ramos é a dele, de ponta a ponta, <b>incluindo o acidente da vaca-de-cogumelo</b>.
     */
    public static MerchantOffers monta(LivingEntity bicho) {
        RandomSource sorte = bicho.getRandom();
        List<ItemStack> moedas = new ArrayList<>();
        List<ItemStack> coisas = new ArrayList<>();

        List<ItemStack> dezoito = asDezoito();
        coisas.add(dezoito.get(sorte.nextInt(dezoito.size())));
        // e aqui a semente de Treefyd, três em cem, quando o Treefyd estiver portado

        boolean morto = mortoVivo(bicho);

        if (bicho instanceof Pig) {
            moedas.add(new ItemStack(Items.CARROT));
            moedas.add(new ItemStack(Items.APPLE));
            moedas.add(new ItemStack(Items.POTATO));
            coisas.add(new ItemStack(Items.RED_MUSHROOM, 5));
            coisas.add(new ItemStack(Items.BROWN_MUSHROOM, 5));
            if (sorte.nextDouble() < ESMERALDA) coisas.add(new ItemStack(Items.EMERALD));
            if (sorte.nextDouble() < DIAMANTE) coisas.add(new ItemStack(Items.DIAMOND));
        } else if (bicho instanceof AbstractHorse) {
            moedas.add(new ItemStack(Items.CARROT));
            moedas.add(new ItemStack(Items.APPLE));
            moedas.add(new ItemStack(Items.WHEAT));
            if (sorte.nextDouble() < SELA) coisas.add(new ItemStack(Items.SADDLE));
        } else if (bicho instanceof Wolf) {
            moedas.add(new ItemStack(Items.BEEF));
            moedas.add(new ItemStack(Items.PORKCHOP));
            moedas.add(new ItemStack(Items.CHICKEN));
            coisas.add(new ItemStack(Items.BONE, 5));
            if (sorte.nextDouble() < ESMERALDA) coisas.add(new ItemStack(Items.EMERALD));
            if (sorte.nextDouble() < DIAMANTE) coisas.add(new ItemStack(Items.DIAMOND));
        } else if (bicho instanceof Ocelot || bicho instanceof Cat) {
            moedas.add(new ItemStack(Items.MILK_BUCKET));
            moedas.add(new ItemStack(Items.COD));
        } else if (bicho instanceof AbstractCow) {
            // e aqui a vaca-de-cogumelo cai também, que é o acidente do original
            moedas.add(new ItemStack(Items.WHEAT));
        } else if (bicho instanceof Chicken) {
            moedas.add(new ItemStack(Items.WHEAT_SEEDS));
            coisas.add(new ItemStack(Items.FEATHER, 10));
            coisas.add(new ItemStack(Items.EGG, 5));
        } else if (bicho instanceof MushroomCow) {
            // o ramo que nunca corre: a vaca já a pegou acima. Fica escrito porque está escrito nele.
            moedas.add(new ItemStack(Items.RED_MUSHROOM));
            moedas.add(new ItemStack(Items.BROWN_MUSHROOM));
        } else if (bicho instanceof Sheep) {
            moedas.add(new ItemStack(Items.WHEAT));
        } else if (bicho instanceof Squid) {
            moedas.add(new ItemStack(Items.COD));
            coisas.add(new ItemStack(Items.INK_SAC, 10));
        } else if (bicho instanceof Bat) {
            moedas.add(new ItemStack(Items.WHEAT_SEEDS));
            moedas.add(new ItemStack(Items.WHEAT));
            moedas.add(new ItemStack(Items.BEEF));
            moedas.add(new ItemStack(Items.PORKCHOP));
            coisas.add(new ItemStack(OccultaItems.BAT_WOOL, 5));
        } else if (bicho instanceof Spider) {
            moedas.add(new ItemStack(Items.BEEF));
            moedas.add(new ItemStack(Items.PORKCHOP));
            moedas.add(new ItemStack(Items.CHICKEN));
            moedas.add(new ItemStack(Items.COD));
            coisas.add(new ItemStack(Items.STRING, 8));
            // e aqui a Teia Densa, quatro, quando ela estiver portada
        } else if (bicho instanceof Creeper) {
            moedas.add(new ItemStack(Items.GUNPOWDER));
            moedas.add(new ItemStack(Items.COD));
            if (sorte.nextDouble() < PÓ_ESPECTRAL) {
                coisas.add(new ItemStack(OccultaItems.SPECTRAL_DUST, 2));
            }
            // e aqui a semente de Treefyd, dez em cem, que é o jeito de o creeper dar a semente
            if (sorte.nextDouble() < CORAÇÃO) coisas.add(new ItemStack(OccultaItems.CREEPER_HEART));
        } else if (morto) {
            moedas.add(new ItemStack(Items.BONE));
            coisas.add(new ItemStack(OccultaItems.SPECTRAL_DUST));
        } else {
            moedas.add(new ItemStack(Items.BEEF));
            moedas.add(new ItemStack(Items.PORKCHOP));
            moedas.add(new ItemStack(Items.CHICKEN));
            moedas.add(new ItemStack(Items.COD));
            moedas.add(new ItemStack(Items.WHEAT));
            moedas.add(new ItemStack(Items.WHEAT_SEEDS));
            moedas.add(new ItemStack(Items.CARROT));
            moedas.add(new ItemStack(Items.APPLE));
            moedas.add(new ItemStack(Items.POTATO));
        }

        List<MerchantOffer> todas = new ArrayList<>();
        for (ItemStack cada : coisas) {
            if (cada.isEmpty()) continue;
            ItemStack dá = cada.copy();
            dá.setCount(Math.min(sorte.nextInt(cada.getCount()) + (cada.getCount() > 4 ? 3 : 1),
                    dá.getMaxStackSize()));

            ItemStack moeda = moedas.get(sorte.nextInt(moedas.size()));
            int vezes = caro(dá) || morto ? CARO : 1;
            int fator = dá.getCount() > 4 ? 1 : 2;
            int quanto = Math.min(sorte.nextInt(2)
                    + dá.getCount() * vezes * (sorte.nextInt(2) + fator), moeda.getMaxStackSize());

            todas.add(new MerchantOffer(new ItemCost(moeda.getItem(), quanto), dá,
                    TROCAS - DESCONTO + sorte.nextInt(2), 0, 0.0f));
        }

        Collections.shuffle(todas, new java.util.Random(sorte.nextLong()));
        MerchantOffers feitas = new MerchantOffers();
        int quantas = sorte.nextInt(ATÉ) + 1;
        for (int i = 0; i < quantas && i < todas.size(); i++) feitas.add(todas.get(i));
        return feitas;
    }

    /** O que custa o dobro: diamante, esmeralda e sela — e, no original, a semente de Treefyd. */
    private static boolean caro(ItemStack oquê) {
        return oquê.is(Items.DIAMOND) || oquê.is(Items.EMERALD) || oquê.is(Items.SADDLE);
    }
}
