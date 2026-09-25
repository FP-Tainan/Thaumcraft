package net.thaumcraft.mortuorum;

import net.thaumcraft.Thaumcraft;

/**
 * O Ars Mortuorum — o Necromancy 1.7.10, de sirolf2009, com o nome que a lore de quem joga lhe dá.
 *
 * <p>É a arte de costurar os mortos: tirar pedaços de bicho com a agulha de osso, guardar sangue e alma em pote,
 * e remendar de tudo isso um lacaio que anda. Como os outros ramos, mora no mesmo jar do Thaumcraft, com figuras e
 * textos no espaço de nome {@code thaumcraft}, aba própria no criativo e no Thaumonomicon, e chaves de pesquisa
 * com o prefixo {@code AM_}.
 */
public final class Mortuorum {
    /** A aba do ramo no Thaumonomicon. */
    public static final String CATEGORY = "MORTUORUM";

    private Mortuorum() {
    }

    public static void init() {
        MortuorumBlocks.init();
        MortuorumItems.init();
        Thaumcraft.LOGGER.info("Ars Mortuorum: {} coisas, {} peças de corpo",
                MortuorumItems.count(), MortuorumItems.PARTS.size());
    }
}
