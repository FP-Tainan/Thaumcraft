package net.thaumcraft.occulta.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.vampire.VampireBookItem;

/**
 * A ponte entre o livro na mão e a folha na tela.
 *
 * <p>O item vive nos dois lados e a folha só num deles; isto é o pedacinho que fica do lado de cá, e existe
 * só para que o item não tenha de saber que há um cliente.
 */
@Environment(EnvType.CLIENT)
public final class VampireBookHook {
    /** O princípio do livro, que é o índice. */
    public static final String PRIMEIRA = "toc";

    private VampireBookHook() {
    }

    /** Abre o livro no índice, com as páginas que este exemplar tiver. */
    public static void abre(ItemStack livro) {
        Minecraft.getInstance().setScreenAndShow(new MarkupBookScreen(
                "tc.vampirebook", VampireBookItem.páginas(livro), PRIMEIRA));
    }
}
