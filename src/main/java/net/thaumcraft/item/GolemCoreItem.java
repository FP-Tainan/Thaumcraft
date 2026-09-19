package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.golems.GolemTypes;

import java.util.function.Consumer;

/**
 * Um núcleo de animação de golem: o {@code ItemGolemCore} da 4.2.3.5. Todos se chamam "Núcleo de Animação de Golem";
 * o serviço (encher, esvaziar, juntar...) aparece embaixo do nome, como no original. O núcleo vazio (o 100 de então)
 * não anima nada: é a matéria-prima dos outros.
 */
public class GolemCoreItem extends Item {
    public static final int BLANK = 100;
    private final int index;

    public GolemCoreItem(Properties properties, int index) {
        super(properties);
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    /** O serviço, pelo nome que o original dá a ele ({@code fill}, {@code empty}...), ou {@code blank}. */
    public String core() {
        return this.index == BLANK ? "blank" : GolemTypes.CORES[this.index];
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_core");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("item.thaumcraft.golem_core_" + this.core()).withStyle(ChatFormatting.GRAY));
    }

    /** Os núcleos que abrem a tela do golem. */
    public static boolean hasGUI(int core) {
        return switch (core) {
            case 0, 1, 2, 4, 5, 8, 10 -> true;
            default -> false;
        };
    }

    /** Os núcleos que mostram as opções de comparação (dicionário, dano, NBT) com a entropia. */
    public static boolean canSort(int core) {
        return switch (core) {
            case 0, 1, 2, 8, 10 -> true;
            default -> false;
        };
    }

    /** Os núcleos que têm casas fantasmas. */
    public static boolean hasInventory(int core) {
        return switch (core) {
            case 0, 1, 2, 5, 8 -> true;
            default -> false;
        };
    }
}
