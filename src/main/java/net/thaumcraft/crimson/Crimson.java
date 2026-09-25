package net.thaumcraft.crimson;

import net.thaumcraft.Thaumcraft;

/**
 * O Crimson Warfare — o mod de tage para a 1.12.2, que a lore de quem joga guarda com o mesmo nome.
 *
 * <p>É o menor dos ramos e o mais direto: pelo mundo há altares antigos de pedra arcana, em cruz, com um pedestal
 * no meio. Quem sabe o rito põe nele uma Semente do Vazio, e o que vem buscar a semente é um dos três — o portal
 * carmesim, o golem eldritch ou o guardião. Nada mais: nem item, nem receita, nem ferramenta.
 *
 * <p>O original é da 1.12.2 e chama coisas do Thaumcraft 6 que a 4.2.3.5 não tem; por isso ele entra aqui como
 * <b>inspiração</b>, e não como porte — o que dele se guarda é o altar, a estrutura, o rito e os três chamados.
 */
public final class Crimson {
    /** A aba do ramo no Thaumonomicon, com o nome que o original lhe dá. */
    public static final String CATEGORY = "WARFARE";

    private Crimson() {
    }

    public static void init() {
        CrimsonBlocks.init();
        CrimsonItems.init();
        // a aba do ramo no livro
        net.thaumcraft.api.ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/misc/r_crimson.png"),
                Thaumcraft.id("textures/gui/gui_crimson_researchback.png"));
        CrimsonTable.research();
        Thaumcraft.LOGGER.info("Crimson Warfare: o altar antigo e os três chamados");
    }
}
