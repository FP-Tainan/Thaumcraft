package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** A urna e o caixote como item: o {@code BlockLootItem} da 4.2.3.5, que escreve a raridade embaixo do nome. */
public class LootBlockItem extends BlockItem {
    public LootBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        Rarity rarity = stack.getRarity();
        String name = switch (rarity) {
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case EPIC -> "Epic";
            default -> "Common";
        };
        tooltip.accept(Component.literal(name));
    }
}
