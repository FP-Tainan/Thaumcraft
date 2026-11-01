package net.thaumcraft.occulta.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.thaumcraft.occulta.CauldronBookItem;

/**
 * A ponte entre os <b>Cozimentos das Bruxas</b> na mão e a folha na tela.
 *
 * <p>É o mesmo pedacinho que o Livro do Vampiro tem, e pela mesma razão: o item vive nos dois lados e a
 * folha só num deles.
 */
@Environment(EnvType.CLIENT)
public final class CauldronBookHook {
    private CauldronBookHook() {
    }

    /** Abre-o no índice. Ele não tem páginas que faltem: nasce inteiro. */
    public static void abre() {
        Minecraft.getInstance().setScreenAndShow(new MarkupBookScreen(
                CauldronBookItem.RAIZ, CauldronBookItem.PÁGINAS, CauldronBookItem.PRIMEIRA));
    }
}
