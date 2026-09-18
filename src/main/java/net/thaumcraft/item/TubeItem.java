package net.thaumcraft.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.block.entity.TubeBlockEntity;

/**
 * O item dos tubos com lado: o {@code BlockTubeItem} da 4.2.3.5, que faz o tubo nascer apontando para o
 * lado do bloco em que foi posto.
 */
public class TubeItem extends BlockItem {
    public TubeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction()
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TubeBlockEntity tube) {
            tube.setFacing(context.getClickedFace());
        }
        return result;
    }
}
