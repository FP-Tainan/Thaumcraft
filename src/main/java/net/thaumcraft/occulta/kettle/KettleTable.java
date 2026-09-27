package net.thaumcraft.occulta.kettle;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;

/**
 * As receitas do Caldeirão de Pote, da tabela do {@code WitcheryRecipes} do original.
 *
 * <p>A tabela do Witchery tem <b>trinta e sete</b> entradas. Esta é a primeira leva — as que o mod de hoje já
 * consegue pedir e dar. As outras esperam o que ainda não existe aqui: a Teia do ofício, a Asa de Mocho, o
 * Leite Purificado, a Fome Melíflua, o Fio Enfeitado, o Espírito Subjugado, a Pedra Sintonizada e o Coração de
 * Demônio — e, sobretudo, os <b>frascos</b> que elas fazem, que são fatia à parte.
 *
 * <p>As cores são as do original, número por número: é por elas que o líquido do pote muda enquanto se enche.
 */
public final class KettleTable {
    private KettleTable() {
    }

    public static void register() {
        // a Sopa de Redstone, que é a base dos óleos do ofício
        KettleRecipes.add(() -> new ItemStack(OccultaItems.REDSTONE_SOUP), 0xFF1716, 1000.0f,
                Items.REDSTONE, OccultaItems.DROP_OF_LUCK, OccultaItems.BAT_WOOL,
                OccultaItems.DOG_TONGUE, OccultaItems.BELLADONNA_FLOWER, OccultaItems.MANDRAKE_ROOT);
    }
}
