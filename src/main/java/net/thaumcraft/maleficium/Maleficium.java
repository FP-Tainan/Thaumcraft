package net.thaumcraft.maleficium;

import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.ThaumcraftApi;

/**
 * O <i>Maleficium</i>: o Tainted Magic 8.1.1, de Yulife, dentro do Thaumcraft.
 *
 * <p>No original era um mod à parte, que entrava pelo {@code ThaumcraftApi}. Aqui as coisas dele vão no mesmo jar,
 * a pedido de quem joga, mas continuam entrando pela mesma porta ({@link ThaumcraftApi}) — o ramo é uma aba própria
 * no Thaumonomicon, e o nome dela é o da lore: <b>Maleficium</b>, onde o original dizia <i>Obscura</i>.
 *
 * <p>Porte fiel, como o resto: nada é inventado, e o que muda de lugar muda porque o Minecraft de hoje não tem mais
 * onde o original o punha.
 */
public final class Maleficium {
    /** A aba do ramo no livro. */
    public static final String CATEGORY = "MALEFICIUM";

    private Maleficium() {
    }

    public static void init() {
        MaleficiumBlocks.init();
        MaleficiumItems.init();
        MaleficiumWands.init();
        MaleficiumEvents.init();
        research();
        Thaumcraft.LOGGER.info("Maleficium: {} coisas", MaleficiumItems.count());
    }

    private static void research() {
        ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/misc/r_maleficium.png"),
                Thaumcraft.id("textures/gui/gui_maleficium_researchback.png"));

        MaleficiumTable.research();

        // as receitas carregam itens, então só se montam quando o mundo abre
        ThaumcraftApi.onSetup(MaleficiumTable::recipes);
    }
}
