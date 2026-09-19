package net.thaumcraft.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.block.entity.FluxScrubberBlockEntity;

/** O {@code BlockStoneDeviceItem} para o purificador: ele olha para longe da face em que foi posto. */
public class FluxScrubberItem extends BlockItem {
    public FluxScrubberItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction() && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluxScrubberBlockEntity te) {
            te.setFacing(context.getClickedFace().getOpposite());
        }
        return result;
    }
}
