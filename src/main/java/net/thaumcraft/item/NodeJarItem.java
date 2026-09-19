package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * O nó no jarro na mão: o {@code ItemJarNode} da 4.2.3.5. Posto, o jarro volta com o nó dentro; a dica lista os
 * aspectos do nó (os que o jogador ainda não conhece aparecem como desconhecidos).
 */
public class NodeJarItem extends BlockItem {
    public NodeJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        JarredNode node = stack.get(TCComponents.JARRED_NODE);
        if (node == null) return;
        for (Aspect aspect : node.aspects().getAspectsSorted()) {
            lines.accept(CrystalEssenceItem.known.test(aspect)
                    ? Component.literal(aspect.name().getString() + " x" + node.aspects().getAmount(aspect))
                    : Component.translatable("tc.aspect.unknown"));
        }
    }
}
