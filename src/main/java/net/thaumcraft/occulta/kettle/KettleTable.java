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

        // ---------------------------------------------------------- e a base dos óleos do ofício
        KettleRecipes.add(OccultaItems.REDSTONE_SOUP, 1, 0xFFFF1616, 1000.0f,
                Items.REDSTONE, OccultaItems.DROP_OF_LUCK, OccultaItems.BAT_WOOL,
                OccultaItems.DOG_TONGUE, OccultaItems.BELLADONNA_FLOWER, OccultaItems.MANDRAKE_ROOT);
    }
}
