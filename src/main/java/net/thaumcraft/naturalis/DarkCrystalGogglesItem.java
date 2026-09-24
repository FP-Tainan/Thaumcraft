package net.thaumcraft.naturalis;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.VisDiscountGear;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Os Óculos de Cristal Escuro: o {@code DarkCrystalGogglesItem} do Magia Naturalis 0.5.0.
 *
 * <p>Revelam os nós, descontam cinco por cento de vis — e sete de Perditio, ou nove enquanto é dia —, não deixam
 * quem os veste ficar cego e fazem os olhos brilharem no escuro. Também mostram o que está invisível.
 */
public class DarkCrystalGogglesItem extends Item implements VisDiscountGear {
    public DarkCrystalGogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        if (aspect != Aspects.ENTROPY) return 5;
        // de dia o desconto de Perditio é maior, como no original
        return player != null && player.level().isBrightOutside() ? 9 : 7;
    }

    /** O {@code onArmorTick}: com eles no rosto ninguém fica cego. */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != EquipmentSlot.HEAD || !(entity instanceof Player player)) return;
        if (player.hasEffect(MobEffects.BLINDNESS)) player.removeEffect(MobEffects.BLINDNESS);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.visdiscount").append(": 5%").withStyle(ChatFormatting.DARK_PURPLE));
        lines.accept(Component.translatable("tc.visdiscount").append(" (Perditio): 7-9%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
