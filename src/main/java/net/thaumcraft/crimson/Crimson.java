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
    /**
     * Em que aba do Thaumonomicon o ramo mora.
     *
     * <p><b>Desvio declarado:</b> o original tem aba própria, chamada {@code WARFARE}. Aqui a única entrada dele
     * pende do Culto Carmesim, na aba de sempre, a pedido de quem joga.
     */
    public static final String CATEGORY = "BASICS";

    private Crimson() {
    }

    public static void init() {
        CrimsonBlocks.init();
        CrimsonItems.init();
        CrimsonTable.research();
        Thaumcraft.LOGGER.info("Crimson Warfare: o altar antigo e os três chamados");
    }
}
