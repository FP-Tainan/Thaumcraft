package net.thaumcraft.occulta;

import net.thaumcraft.Thaumcraft;

/**
 * O Ars Occulta — o Witchery 0.24.1, de Emoniph, com o nome que a lore de quem joga lhe dá.
 *
 * <p>É o ofício das bruxas: as plantas que se criam em terra e em água, o caldeirão, o altar que junta poder da
 * natureza em volta, os rituais desenhados no chão e os espíritos com que se fala. Como os outros ramos, mora no
 * mesmo jar do Thaumcraft, com figuras e textos no espaço de nome {@code thaumcraft}, aba própria no criativo e
 * chaves de pesquisa com o prefixo {@code AO_}.
 *
 * <p><b>Por onde vai:</b> esta é a primeira fatia — as oito plantas. O caldeirão, o altar, os rituais e o resto
 * vêm depois, na ordem que o {@code docs/PORTE.md} marca.
 */
public final class Occulta {
    /** A aba do ramo no Thaumonomicon, quando ela chegar. */
    public static final String CATEGORY = "OCCULTA";

    private Occulta() {
    }

    public static void init() {
        OccultaBlocks.init();
        OccultaItems.init();
        OccultaAspects.init();
        Thaumcraft.LOGGER.info("Ars Occulta: {} coisas", OccultaItems.count());
    }
}
