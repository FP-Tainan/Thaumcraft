package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.RunicArmor;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * As armaduras do Culto Carmesim: o {@code ItemCultistRobeArmor}, o {@code ItemCultistPlateArmor}, o
 * {@code ItemCultistLeaderArmor} e o {@code ItemCultistBoots} da 4.2.3.5. O robe e as botas dão um por cento de desconto
 * de vis e um de distorção cada peça; a placa e a do pretor só protegem. Todas aceitam o reforço rúnico (com zero de
 * carga própria) e se consertam com ferro.
 */
public class CultistArmorItem extends Item implements RunicArmor, VisDiscountGear, WarpEvents.WarpingGear {
    public enum Kind { ROBE, PLATE, LEADER, BOOTS }

    public final Kind kind;

    public CultistArmorItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    private boolean arcane() {
        return this.kind == Kind.ROBE || this.kind == Kind.BOOTS;
    }

    @Override
    public int runicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        return this.arcane() ? 1 : 0;
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return this.arcane() ? 1 : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        if (!this.arcane()) return;
        lines.accept(Component.translatable("tc.visdiscount").append(": 1%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
