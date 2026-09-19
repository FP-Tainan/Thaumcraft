package net.thaumcraft.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * Item desenhado na tela com uma cor por cima, como o {@code glColor4f} que o original punha antes do
 * {@code renderItemAndEffectIntoGUI}: o mapa do Thaumonomicon escurece o ícone da pesquisa que ainda não se pode abrir.
 *
 * <p>O jogo de hoje desenha os itens numa folha e só depois os cola na tela, sempre em branco; a cor pedida aqui fica
 * guardada no estado do item ({@code GuiItemRenderStateMixin}) e vale na hora da colagem ({@code GuiRendererTintMixin}).
 */
public final class TintedItems {
    /** A cor do próximo item que for pedido; branco fora de {@link #item}. */
    public static int current = -1;

    private TintedItems() {
    }

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int color) {
        current = color;
        try {
            graphics.item(stack, x, y);
        } finally {
            current = -1;
        }
    }

    /** O estado do item lembra a cor com que foi pedido. */
    public interface Tinted {
        int thaumcraft$tint();
    }
}
