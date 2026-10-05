package net.thaumcraft.occulta.kettle;

import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;

/**
 * As receitas do Caldeirão de Pote, da tabela do {@code WitcheryRecipes} do original.
 *
 * <p>A tabela do Witchery tem <b>trinta e sete</b> entradas. Esta é a primeira leva — as que o mod de hoje já
 * consegue pedir e dar. As outras esperam o que ainda não existe aqui: a Teia do ofício, a Asa de Mocho, o
 * Leite Purificado, a Fome Melíflua, o Fio Enfeitado, o Espírito Subjugado, a Pedra Sintonizada e o Coração de
 * Demônio.
 *
 * <p>As cores e as quantidades são as do original, número por número: é pela cor que o líquido do pote muda
 * enquanto se enche, e é por ela que se sabe, olhando, que se está no caminho certo.
 */
public final class KettleTable {
    /** Quantos frascos cada cozimento dá: os três do original. */
    public static final int BREW_COUNT = 3;

    private KettleTable() {
    }

    public static void register() {
        // ---------------------------------------------------------- os frascos que se atiram
        // ---------------------------------------------------------- o Cozimento do Grotesco
        // Três de cada vez, quinhentos de poder, e a cor -13491946 do original lida como ARGB.
        KettleRecipes.add(OccultaItems.BREW_GROTESQUE, 3, 0xFF3184D6, 500.0f,
                OccultaItems.MUTANDIS_EXTREMIS, OccultaItems.MANDRAKE_ROOT,
                OccultaItems.WATER_ARTICHOKE_GLOBE, OccultaItems.DOG_TONGUE,
                Items.GOLDEN_APPLE, Items.POISONOUS_POTATO);

        // ---------------------------------------------------------- o Unguento do Voo
        // A cor é o -17620 do original, lido como cor de 32 bits: 0xFFFFBB6C.
        // <b>Um desvio declarado:</b> o original pede uma Poção de Rapidez longa e de arremesso, com o número
        // de poção da 1.7.10 (8258). A tabela deste pote casa por <b>item</b>, e uma poção de hoje leva o que
        // ela é num componente, não no item — por isso aqui o que entra é a poção, qualquer que seja. Está no
        // {@code PORTE.md}.
        KettleRecipes.add(OccultaItems.FLYING_OINTMENT, 1, 0xFFFFBB6C, 3000.0f,
                OccultaItems.REDSTONE_SOUP, Items.SPLASH_POTION, Items.DIAMOND, Items.FEATHER,
                OccultaItems.BAT_WOOL, OccultaItems.BELLADONNA_FLOWER);

        // ---------------------------------------------------------- o Óleo do Acaso
        // A cor é o 8534058 do original lido como cor de 32 bits, e o poder são dois mil.
        // <b>O mesmo desvio do Unguento do Voo:</b> o original pede uma poção com o número de 1.7.10
        // (8262, que era Visão Noturna), e aqui entra a poção, qualquer que seja. Está no PORTE.md.
        KettleRecipes.add(OccultaItems.HAPPENSTANCE_OIL, 1, 0xFF823CAA, 2000.0f,
                OccultaItems.REDSTONE_SOUP, Items.POTION, Items.ENDER_EYE, Items.GOLDEN_CARROT,
                Items.SPIDER_EYE, OccultaItems.MANDRAKE_ROOT);

        KettleRecipes.add(OccultaItems.BREW_OF_VINES, BREW_COUNT, 0xFF005B07, 0.0f,
                Items.VINE, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM,
                OccultaItems.DOG_TONGUE, Items.WHEAT, OccultaItems.REEK_OF_MISFORTUNE);

        KettleRecipes.add(OccultaItems.BREW_OF_THORNS, BREW_COUNT, 0xFF66FF20, 0.0f,
                Items.DYE.pick(net.minecraft.world.item.DyeColor.GREEN), Items.RED_MUSHROOM, OccultaItems.OIL_OF_VITRIOL,
                OccultaItems.ODOUR_OF_PURITY, Items.DANDELION, OccultaItems.MANDRAKE_ROOT);

        KettleRecipes.add(OccultaItems.BREW_OF_INK, BREW_COUNT, 0xFF333333, 0.0f,
                Items.INK_SAC, OccultaItems.QUICKLIME, OccultaItems.OIL_OF_VITRIOL,
                Items.SLIME_BALL, OccultaItems.BELLADONNA_FLOWER, OccultaItems.ROWAN_BERRIES);

        KettleRecipes.add(OccultaItems.BREW_OF_SPROUTING, BREW_COUNT, 0xFF543727, 0.0f,
                OccultaItems.WOOD.get("rowan_sapling"), OccultaItems.WOOD.get("alder_sapling"),
                OccultaItems.WOOD.get("hawthorn_sapling"), OccultaItems.DOG_TONGUE,
                OccultaItems.MANDRAKE_ROOT, Items.DANDELION);

        KettleRecipes.add(OccultaItems.BREW_OF_EROSION, BREW_COUNT, 0xFFBBFF30, 0.0f,
                OccultaItems.OIL_OF_VITRIOL, OccultaItems.OIL_OF_VITRIOL, OccultaItems.QUICKLIME,
                OccultaItems.BELLADONNA_FLOWER, Items.DANDELION, Items.GHAST_TEAR);

        KettleRecipes.add(OccultaItems.BREW_OF_LOVE, BREW_COUNT, 0xFFFFA5FC, 0.0f,
                Items.DANDELION, OccultaItems.WHIFF_OF_MAGIC, OccultaItems.WATER_ARTICHOKE_GLOBE,
                Items.GOLDEN_CARROT, Items.VINE, Items.COCOA_BEANS);

        KettleRecipes.add(OccultaItems.BREW_OF_RAISING, BREW_COUNT, 0xFF470E47, 500.0f,
                OccultaItems.BAT_WOOL, OccultaItems.MUTANDIS, Items.REDSTONE,
                OccultaItems.OIL_OF_VITRIOL, Items.BONE, Items.ROTTEN_FLESH);

        KettleRecipes.add(OccultaItems.BREW_OF_WEBS, BREW_COUNT, 0xFFFFFFFF, 0.0f,
                OccultaItems.WITCH_WEB, Items.BROWN_MUSHROOM, OccultaItems.BAT_WOOL,
                Items.DANDELION, OccultaItems.WHIFF_OF_MAGIC, OccultaItems.BELLADONNA_FLOWER);

        KettleRecipes.add(OccultaItems.BREW_OF_ICE, BREW_COUNT, 0xFF31A3FF, 1000.0f,
                OccultaItems.ICY_NEEDLE, Items.SNOWBALL, OccultaItems.WATER_ARTICHOKE_GLOBE,
                Items.MAGMA_CREAM, Items.BROWN_MUSHROOM, OccultaItems.ODOUR_OF_PURITY);

        KettleRecipes.add(OccultaItems.BREW_OF_INFECTION, BREW_COUNT, 0xFF5A1F2E, 0.0f,
                OccultaItems.TOE_OF_FROG, OccultaItems.CREEPER_HEART, OccultaItems.WORMY_APPLE,
                OccultaItems.BELLADONNA_FLOWER, Items.ROTTEN_FLESH, OccultaItems.MUTANDIS);

        KettleRecipes.add(OccultaItems.BREW_SUBSTITUTION, BREW_COUNT, 0xFF95E0E0, 0.0f,
                OccultaItems.ENDER_DEW, OccultaItems.ENDER_DEW, OccultaItems.MUTANDIS_EXTREMIS,
                Items.EGG, Items.GHAST_TEAR, OccultaItems.ENT_BRANCH);

        // ---------------------------------------------------------- o que se bebe
        KettleRecipes.add(OccultaItems.BREW_OF_THE_DEPTHS, BREW_COUNT, 0xFF17394A, 0.0f,
                OccultaItems.MANDRAKE_ROOT, OccultaItems.WATER_ARTICHOKE_GLOBE, OccultaItems.ODOUR_OF_PURITY,
                OccultaItems.TEAR_OF_THE_GODDESS, Items.VINE, Items.INK_SAC);

        KettleRecipes.add(OccultaItems.BREW_OF_SLEEPING, BREW_COUNT, 0xFF8AB0B8, 0.0f,
                OccultaItems.PURIFIED_MILK, Items.COOKIE, OccultaItems.BREW_OF_LOVE,
                OccultaItems.WHIFF_OF_MAGIC, OccultaItems.ICY_NEEDLE, OccultaItems.WATER_ARTICHOKE_GLOBE);

        // ------------------------------------------ e o que só se coze do outro lado
        // O original prende este cozimento à dimensão do Sonho: um pote fervido no mundo de cá nunca o dá.
        KettleRecipes.add(OccultaItems.BREW_OF_FLOWING_SPIRIT, BREW_COUNT, 0xFF00A0A6, 0.0f,
                net.thaumcraft.occulta.spirit.SpiritWorld.LEVEL,
                OccultaItems.FANCIFUL_THREAD, OccultaItems.WATER_ARTICHOKE_GLOBE, OccultaItems.MANDRAKE_ROOT,
                OccultaItems.SPANISH_MOSS, OccultaItems.GLINT_WEED, OccultaItems.BAT_WOOL);

        // ------------------------------------------ os cinco que endurecem uma poça de Lágrimas Ocas
        // Os cinco pedem a mesma coisa, e o que muda é a primeira: é ela que diz no que a poça vira.
        solid(OccultaItems.BREW_OF_SOLID_DIRT, 0xFF503A50, Items.DIRT);
        solid(OccultaItems.BREW_OF_SOLID_ROCK, 0xFF808080, Items.STONE);
        solid(OccultaItems.BREW_OF_SOLID_SAND, 0xFFCAAF65, Items.SAND);
        solid(OccultaItems.BREW_OF_SOLID_SANDSTONE, 0xFF7F8000, Items.SANDSTONE);
        solid(OccultaItems.BREW_OF_SOLID_EROSION, 0xFFFFF31C, OccultaItems.BREW_OF_EROSION);

        // ---------------------------------------------------------- e a base dos óleos do ofício
        KettleRecipes.add(OccultaItems.REDSTONE_SOUP, 1, 0xFFFF1616, 1000.0f,
                Items.REDSTONE, OccultaItems.DROP_OF_LUCK, OccultaItems.BAT_WOOL,
                OccultaItems.DOG_TONGUE, OccultaItems.BELLADONNA_FLOWER, OccultaItems.MANDRAKE_ROOT);
    }

    /**
     * Um dos cinco Cozimentos Sólidos: a mesma receita para todos, com a marca de cada um na primeira casa.
     *
     * <p>Dois mil de poder, que é o que o original pede — e é o mais caro do pote inteiro.
     */
    private static void solid(net.minecraft.world.item.Item sai, int cor, net.minecraft.world.item.Item marca) {
        KettleRecipes.add(sai, BREW_COUNT, cor, 2000.0f,
                marca, OccultaItems.FOUL_FUME, OccultaItems.ODOUR_OF_PURITY,
                OccultaItems.MUTANDIS, OccultaItems.WOOD_ASH, OccultaItems.SPANISH_MOSS);
    }
}
