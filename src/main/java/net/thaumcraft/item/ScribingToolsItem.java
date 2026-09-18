package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.block.ResearchTableBlock;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * As ferramentas de escrita — pena e tinteiro: o {@code ItemInkwell} da 4.2.3.5.
 *
 * <p>Têm tinta para cem riscos. Postas sobre uma mesa que tenha outra mesa encostada, as duas viram a mesa de
 * pesquisa, e as ferramentas ficam sobre ela.
 */
public class ScribingToolsItem extends Item {
    public ScribingToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(TCBlocks.TABLE)) return InteractionResult.PASS;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos other = pos.relative(direction);
            if (!level.getBlockState(other).is(TCBlocks.TABLE)) continue;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ResearchTableBlock.form(level, pos, direction);
            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
                ItemStack held = context.getItemInHand();
                table.setItem(ResearchTableBlockEntity.INK, held.copyWithCount(1));
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
