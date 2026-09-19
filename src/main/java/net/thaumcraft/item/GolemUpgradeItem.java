package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma melhoria de golem: o {@code ItemGolemUpgrade} da 4.2.3.5 — ar (passo), terra (força), fogo (dano e carga),
 * água (sentidos e alcance), ordem (organização) e entropia (defesa e raciocínio). Até duas de cada num golem.
 */
public class GolemUpgradeItem extends Item {
    public static final String[] NAMES = {"air", "earth", "fire", "water", "order", "entropy"};
    private final int index;

    public GolemUpgradeItem(Properties properties, int index) {
        super(properties);
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("item.thaumcraft.golem_upgrade_" + NAMES[this.index] + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
