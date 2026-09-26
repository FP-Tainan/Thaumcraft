package net.thaumcraft.occulta;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * As coisas do Ars Occulta — o Witchery 0.24.1, de Emoniph.
 *
 * <p>Como os outros ramos, ele mora no mesmo jar do Thaumcraft, com as figuras e os textos no espaço de nome
 * {@code thaumcraft} e aba própria no criativo. Os itens do original que eram um item só com muitos valores — o
 * {@code ItemGeneral} — viram um item por coisa, que é como o jogo de hoje faz.
 *
 * <p>Nas oito plantas, <b>a semente é um item e a colheita é outro</b>, menos em duas: na mindrake e no alho a
 * semente e a colheita são a mesma coisa, porque no original o item de colheita delas é nulo e o mod copia o de
 * semente.
 */
public final class OccultaItems {
    /** A ordem em que as coisas entram na aba do criativo. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("occulta"));

    // ------------------------------------------------------------------ o que se planta

    public static final Item BELLADONNA_SEEDS = seeds("belladonna_seeds", OccultaBlocks.BELLADONNA);
    public static final Item MANDRAKE_SEEDS = seeds("mandrake_seeds", OccultaBlocks.MANDRAKE);

    /**
     * A semente da alcachofra-d'água, que se planta na água: o {@code ItemWitchSeeds} com {@code waterPlant} olha
     * o que está debaixo da água ao clicar, e é isso que o {@code PlaceOnWaterBlockItem} do jogo de hoje faz.
     */
    public static final Item WATER_ARTICHOKE_SEEDS = register("water_artichoke_seeds", properties ->
            new PlaceOnWaterBlockItem(OccultaBlocks.WATER_ARTICHOKE, properties.useItemDescriptionPrefix()));

    public static final Item SNOWBELL_SEEDS = seeds("snowbell_seeds", OccultaBlocks.SNOWBELL);
    public static final Item WORMWOOD_SEEDS = seeds("wormwood_seeds", OccultaBlocks.WORMWOOD);

    /** O bulbo da mindrake: é o que se planta e é o que se colhe. */
    public static final Item MINDRAKE_BULB = seeds("mindrake_bulb", OccultaBlocks.MINDRAKE);

    public static final Item WOLFSBANE_SEEDS = seeds("wolfsbane_seeds", OccultaBlocks.WOLFSBANE);

    /** O alho, que também é semente de si mesmo. */
    public static final Item GARLIC = seeds("garlic", OccultaBlocks.GARLIC);

    // ------------------------------------------------------------------ o que se colhe

    public static final Item BELLADONNA_FLOWER = register("belladonna_flower", Item::new);
    public static final Item MANDRAKE_ROOT = register("mandrake_root", Item::new);
    public static final Item WATER_ARTICHOKE_GLOBE = register("water_artichoke_globe", Item::new);
    public static final Item WORMWOOD_SPRIG = register("wormwood_sprig", Item::new);
    public static final Item WOLFSBANE_SPRIG = register("wolfsbane_sprig", Item::new);

    /** A Agulha de Gelo, que sai de vez em quando ao colher a campainha-de-neve. */
    public static final Item ICY_NEEDLE = register("icy_needle", Item::new);

    private OccultaItems() {
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    /** Uma semente comum: planta a sua planta, e leva o nome de item, não o do bloco. */
    private static Item seeds(String name, net.minecraft.world.level.block.Block crop) {
        return register(name, properties -> new BlockItem(crop, properties.useItemDescriptionPrefix()));
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    /** A aba do criativo do ramo, à parte da do Thaumcraft. */
    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.occulta"))
                .icon(() -> new ItemStack(MANDRAKE_ROOT))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
