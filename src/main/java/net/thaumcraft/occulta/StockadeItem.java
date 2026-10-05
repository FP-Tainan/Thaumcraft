package net.thaumcraft.occulta;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * A Paliçada na mão: o {@code MultiItemBlock} do {@code BlockStockade}.
 *
 * <p>No original eram <b>nove itens</b>, um por madeira. Aqui é um só, e a madeira viaja no feitio do bloco
 * que ele guarda — mas o <b>nome</b> e a <b>cara</b> são os nove do original, de modo que uma paliçada de
 * sorveira na mochila se chama e se parece com uma paliçada de sorveira.
 */
public class StockadeItem extends BlockItem {
    public StockadeItem(Properties properties) {
        super(OccultaBlocks.STOCKADE, properties);
    }

    /** A madeira que esta paliçada leva no feitio guardado. */
    public static StockadeBlock.Wood madeira(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return StockadeBlock.Wood.OAK;
        StockadeBlock.Wood leva = feitio.get(StockadeBlock.MADEIRA);
        return leva == null ? StockadeBlock.Wood.OAK : leva;
    }

    @Override
    public Component getName(ItemStack qual) {
        return Component.translatable("item.thaumcraft.stockade." + madeira(qual).getSerializedName());
    }
}
