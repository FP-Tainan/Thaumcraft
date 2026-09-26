package net.thaumcraft.occulta;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * O cheiro de cada coisa que se queima no Forno das Bruxas: o {@code generateByProduct} do original.
 *
 * <p>As mudas dizem o que sai, uma a uma; tudo o mais deixa apenas <b>Fumo Fétido</b>. A muda de selva é a única
 * que não deixa cheiro nenhum, e o original não diz por quê.
 *
 * <p><b>Do original fica de fora, por agora,</b> as três mudas do ofício — a sorveira, o amieiro e o
 * espinheiro-alvar —, que dão a Lufada de Magia, o Fedor de Má Sorte e o Odor de Pureza. As árvores vêm em fatia
 * própria, e os três fumos já estão aqui à espera delas. (Ficam de fora também os enxertos do Forestry e do
 * Biomes O' Plenty, que o original também atendia.)
 */
public final class OccultaFumes {
    private OccultaFumes() {
    }

    /** O fumo que aquela coisa deixa ao queimar; vazio se não deixa nenhum. */
    public static ItemStack of(ItemStack queimado) {
        if (queimado.is(Items.OAK_SAPLING)) return new ItemStack(OccultaItems.EXHALE_OF_THE_HORNED_ONE);
        if (queimado.is(Items.SPRUCE_SAPLING)) return new ItemStack(OccultaItems.HINT_OF_REBIRTH);
        if (queimado.is(Items.BIRCH_SAPLING)) return new ItemStack(OccultaItems.BREATH_OF_THE_GODDESS);
        // a muda de selva é a marca 3 do original, a única que o mod tira da conta
        if (queimado.is(Items.JUNGLE_SAPLING)) return ItemStack.EMPTY;
        return new ItemStack(OccultaItems.FOUL_FUME);
    }
}
