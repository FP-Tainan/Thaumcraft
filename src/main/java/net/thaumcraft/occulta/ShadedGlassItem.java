package net.thaumcraft.occulta;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * O Vidro Sombreado na mão: o {@code MultiItemBlock} do {@code BlockShadedGlass}.
 *
 * <p>No original eram <b>dezesseis itens</b>, um por cor. Aqui é um só, com a cor viajando no feitio do
 * bloco — mas o <b>nome</b> e a <b>cara</b> são os dezesseis do original.
 *
 * <p>E a cara é a do vidro <b>aberto</b>, sempre: ele só escurece quando está posto e com corrente, de modo
 * que o item na mochila mostra como ele fica quando a persiana está levantada.
 */
public class ShadedGlassItem extends BlockItem {
    public ShadedGlassItem(Properties properties) {
        super(OccultaBlocks.SHADED_GLASS, properties);
    }

    /** A cor que este vidro leva no feitio guardado. */
    public static DyeColor cor(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return DyeColor.WHITE;
        DyeColor leva = feitio.get(ShadedGlassBlock.COR);
        return leva == null ? DyeColor.WHITE : leva;
    }

    @Override
    public Component getName(ItemStack qual) {
        return Component.translatable("item.thaumcraft.shaded_glass." + cor(qual).getSerializedName());
    }
}
