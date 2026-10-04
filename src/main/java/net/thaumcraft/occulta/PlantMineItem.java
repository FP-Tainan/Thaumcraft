package net.thaumcraft.occulta;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * A Mina de Planta na mão: o {@code MultiItemBlock} do {@code BlockPlantMine}.
 *
 * <p>No original eram <b>doze itens</b>, um por cruzamento de cara e efeito — "Poppy of Webs", "Dandelion of
 * Ink", "Shrub of Thorns" e os outros nove. Aqui é um item só, e as duas chaves viajam no feitio do bloco
 * que ele guarda.
 *
 * <p>O <b>nome</b> é o do original e diz as duas coisas; a <b>cara</b>, porém, só diz uma. Isso é de
 * propósito e é dele: o desenho da mina depende <b>só da cara</b>, de modo que uma papoula de teias e uma
 * papoula de espinhos são a mesma papoula. Quem a planta sabe o que ela é; quem passa por cima, não.
 */
public class PlantMineItem extends BlockItem {
    public PlantMineItem(Properties properties) {
        super(OccultaBlocks.PLANT_MINE, properties);
    }

    /** A cara que esta mina leva no feitio guardado. */
    public static PlantMineBlock.Look cara(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return PlantMineBlock.Look.ROSE;
        PlantMineBlock.Look leva = feitio.get(PlantMineBlock.CARA);
        return leva == null ? PlantMineBlock.Look.ROSE : leva;
    }

    /** E o efeito. */
    public static PlantMineBlock.Effect efeito(ItemStack qual) {
        var feitio = qual.get(DataComponents.BLOCK_STATE);
        if (feitio == null) return PlantMineBlock.Effect.WEBS;
        PlantMineBlock.Effect leva = feitio.get(PlantMineBlock.EFEITO);
        return leva == null ? PlantMineBlock.Effect.WEBS : leva;
    }

    @Override
    public Component getName(ItemStack qual) {
        return Component.translatable("item.thaumcraft.plant_mine."
                + cara(qual).getSerializedName() + "_" + efeito(qual).getSerializedName());
    }
}
