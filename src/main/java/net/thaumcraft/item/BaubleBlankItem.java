package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.api.wands.VisDiscountGear;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * As peças sem magia do {@code ItemBaubleBlanks} da 4.2.3.5 — o amuleto, o anel e o cinto comuns, base das peças
 * encantadas — e os anéis de aprendiz, um por primário, que dão um por cento de desconto naquele aspecto.
 */
public class BaubleBlankItem extends Item implements BaubleItem, VisDiscountGear {
    private final BaubleType type;
    @Nullable
    private final Aspect aspect;

    public BaubleBlankItem(BaubleType type, @Nullable Aspect aspect, Properties properties) {
        super(properties);
        this.type = type;
        this.aspect = aspect;
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return this.type;
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        return this.aspect != null && this.aspect == aspect ? 1 : 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (this.aspect == null) return super.getName(stack);
        return Component.translatable("item.thaumcraft.apprentice_ring", this.aspect.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        if (this.aspect == null) return;
        lines.accept(this.aspect.name().copy().append(" ").append(Component.translatable("tc.discount")).append(": 1%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
