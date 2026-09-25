package net.thaumcraft.forbidden;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.item.ScribingToolsItem;

/**
 * O Tinteiro Primordial: o {@code ItemPrimewell} do Forbidden Magic 0.575.
 *
 * <p>Pena e tinteiro que não secam nunca — o original consegue isso não deixando o dano subir, e é o que se faz
 * aqui.
 */
public class PrimewellItem extends ScribingToolsItem {
    public PrimewellItem(Properties properties) {
        super(properties);
    }

    /** Ele não gasta tinta nunca. */
    @Override
    public boolean spendsInk() {
        return false;
    }

    /** O {@code setDamage} do original: o dano nunca passa de zero. */
    public static void keepFull(ItemStack stack) {
        if (stack.getDamageValue() != 0) stack.setDamageValue(0);
    }
}
