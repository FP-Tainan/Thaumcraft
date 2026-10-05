package net.thaumcraft.occulta;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * O Feixe de Vime na mão: o {@code MultiItemBlock} do {@code BlockWickerBundle}.
 *
 * <p>São <b>dois nomes</b> no original — o simples e o ensanguentado — e aqui são os mesmos dois, com a
 * chave viajando no feitio do bloco que o item guarda.
 */
public class WickerBundleItem extends BlockItem {
    public WickerBundleItem(Properties properties) {
        super(OccultaBlocks.WICKER_BUNDLE, properties);
    }

    /** Se este feixe foi passado por Sangue Infernal. */
    public static boolean temSangue(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return false;
        Boolean leva = feitio.get(WickerBundleBlock.SANGUE);
        return leva != null && leva;
    }

    @Override
    public Component getName(ItemStack qual) {
        return Component.translatable("item.thaumcraft.wicker_bundle."
                + (temSangue(qual) ? "bloodied" : "plain"));
    }
}
