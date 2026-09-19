package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Um acessório de golem: o {@code ItemGolemDecoration} da 4.2.3.5 — cartola (H), óculos (G), gravata (B), barrete (F),
 * lança-dardos (R), visor (V), blindagem (P) e braço de maça (M). O nome sai "Acessório: Cartola", como no original.
 */
public class GolemDecorationItem extends Item {
    public static final String[] NAMES = {"tophat", "glasses", "bowtie", "fez", "dart", "visor", "armor", "mace"};
    private static final String[] CHARS = {"H", "G", "B", "F", "R", "V", "P", "M"};
    private final int index;

    public GolemDecorationItem(Properties properties, int index) {
        super(properties);
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    /** O {@code getDecoChar}: a letra que o golem guarda. */
    public String decoChar() {
        return CHARS[this.index];
    }

    public static String decoChar(int index) {
        return index >= 0 && index < CHARS.length ? CHARS[index] : "";
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_decoration").append(": ")
                .append(Component.translatable("item.thaumcraft.golem_decoration_" + NAMES[this.index]));
    }
}
