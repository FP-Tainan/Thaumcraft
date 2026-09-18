package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A essência cristalizada: o {@code ItemCrystalEssence} da 4.2.3.5.
 *
 * <p>Um ponto de um aspecto preso num cristal, pintado da cor dele. Um cristal sem aspecto — o da aba do
 * criativo — sorteia um assim que chega a um inventário, como no original.
 */
public class CrystalEssenceItem extends Item {
    /** Se quem olha já descobriu o aspecto; o cliente pendura aqui a consulta ao jogador dele. */
    public static java.util.function.Predicate<Aspect> known = aspect -> true;

    public CrystalEssenceItem(Properties properties) {
        super(properties);
    }

    /** Um cristal daquele aspecto. */
    public static ItemStack of(Aspect aspect) {
        ItemStack stack = new ItemStack(TCItems.CRYSTAL_ESSENCE);
        stack.set(TCComponents.CRYSTAL_ASPECT, aspect.tag());
        return stack;
    }

    @Nullable
    public static Aspect aspectOf(ItemStack stack) {
        String tag = stack.get(TCComponents.CRYSTAL_ASPECT);
        return tag == null ? null : Aspect.of(tag);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (aspectOf(stack) == null) {
            List<Aspect> all = new ArrayList<>(Aspects.all());
            stack.set(TCComponents.CRYSTAL_ASPECT, all.get(level.getRandom().nextInt(all.size())).tag());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        Aspect aspect = aspectOf(stack);
        if (aspect == null) return;
        lines.accept(known.test(aspect) ? Component.literal(aspect.name().getString() + " x1")
                : Component.translatable("tc.aspect.unknown"));
    }
}
