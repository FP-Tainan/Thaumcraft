package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.List;
import java.util.function.Consumer;

/**
 * A essência etérea: o {@code ItemWispEssence} da 4.2.3.5, o que sobra do fogo-fátuo. Carrega dois pontos do aspecto
 * dele (além dos dois de aura do próprio item) e tem a cor dele; na aba do criativo vem uma de cada aspecto.
 */
public class WispEssenceItem extends Item {
    public WispEssenceItem(Properties properties) {
        super(properties);
    }

    /** Uma essência daquele aspecto. */
    public static ItemStack of(Aspect aspect) {
        ItemStack stack = new ItemStack(TCItems.WISP_ESSENCE);
        stack.set(TCComponents.CRYSTAL_ASPECT, aspect.tag());
        return stack;
    }

    /** O {@code getSubItems}: uma por aspecto. */
    public static List<ItemStack> variants() {
        return Aspects.all().stream().map(WispEssenceItem::of).toList();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        Aspect aspect = CrystalEssenceItem.aspectOf(stack);
        if (aspect == null) return;
        lines.accept(CrystalEssenceItem.known.test(aspect) ? Component.literal(aspect.name().getString() + " x2")
                : Component.translatable("tc.aspect.unknown"));
    }
}
