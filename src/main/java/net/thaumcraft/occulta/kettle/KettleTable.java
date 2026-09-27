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

        // ---------------------------------------------------------- e a base dos óleos do ofício
        KettleRecipes.add(OccultaItems.REDSTONE_SOUP, 1, 0xFFFF1616, 1000.0f,
                Items.REDSTONE, OccultaItems.DROP_OF_LUCK, OccultaItems.BAT_WOOL,
                OccultaItems.DOG_TONGUE, OccultaItems.BELLADONNA_FLOWER, OccultaItems.MANDRAKE_ROOT);
    }
}
