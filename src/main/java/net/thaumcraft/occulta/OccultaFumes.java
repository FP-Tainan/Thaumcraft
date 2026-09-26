package net.thaumcraft.occulta;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * O cheiro de cada coisa que se queima no Forno das Bruxas: o {@code generateByProduct} do original.
 *
 * <p>As mudas dizem o que sai, uma a uma; tudo o mais deixa apenas <b>Fumo Fétido</b>. A muda de selva é a única
 * que não deixa cheiro nenhum, e o original não diz por quê.
 *
 * <p><b>Do original ficam de fora</b> os enxertos do Forestry e do Biomes O' Plenty, que o original também
 * atendia: são mods de fora, e aqui não há o que atender.
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

        // e as três do ofício, que é o que o original faz com as mudas dele
        if (queimado.is(OccultaItems.WOOD.get("rowan_sapling"))) return new ItemStack(OccultaItems.WHIFF_OF_MAGIC);
        if (queimado.is(OccultaItems.WOOD.get("alder_sapling"))) return new ItemStack(OccultaItems.REEK_OF_MISFORTUNE);
        if (queimado.is(OccultaItems.WOOD.get("hawthorn_sapling"))) return new ItemStack(OccultaItems.ODOUR_OF_PURITY);
        return new ItemStack(OccultaItems.FOUL_FUME);
    }
}
