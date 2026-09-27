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

    // ------------------------------------------------------------------ o forno e o que sai dele

    /** O Forno das Bruxas e os dois funis. */
    public static final Item WITCHES_OVEN = register("witches_oven", properties ->
            new BlockItem(OccultaBlocks.WITCHES_OVEN, properties.useBlockDescriptionPrefix()));
    public static final Item FUME_FUNNEL = register("fume_funnel", properties ->
            new BlockItem(OccultaBlocks.FUME_FUNNEL, properties.useBlockDescriptionPrefix()));
    public static final Item FILTERED_FUME_FUNNEL = register("filtered_fume_funnel", properties ->
            new BlockItem(OccultaBlocks.FILTERED_FUME_FUNNEL, properties.useBlockDescriptionPrefix()));

    /** O pote de barro: mole como sai da bancada, e feito depois de ir ao fogo. */
    public static final Item SOFT_CLAY_JAR = register("soft_clay_jar", Item::new);
    public static final Item CLAY_JAR = register("clay_jar", Item::new);

    /** A Cinza de Madeira, no que uma muda vira quando se queima. */
    public static final Item WOOD_ASH = register("wood_ash", Item::new);

    /** E os sete fumos que o forno guarda nos potes. */
    public static final Item FOUL_FUME = register("foul_fume", Item::new);
    public static final Item EXHALE_OF_THE_HORNED_ONE = register("exhale_of_the_horned_one", Item::new);
    public static final Item BREATH_OF_THE_GODDESS = register("breath_of_the_goddess", Item::new);
    public static final Item HINT_OF_REBIRTH = register("hint_of_rebirth", Item::new);
    public static final Item WHIFF_OF_MAGIC = register("whiff_of_magic", Item::new);
    public static final Item REEK_OF_MISFORTUNE = register("reek_of_misfortune", Item::new);
    public static final Item ODOUR_OF_PURITY = register("odour_of_purity", Item::new);

    /** O Filtro de Fumos, que faz o funil com filtro. */
    public static final Item FUME_FILTER = register("fume_filter", Item::new);

    // ------------------------------------------------------------------ as três árvores

    /** As Bagas de Sorveira, que caem da folhagem da sorveira e se comem. */
    public static final Item ROWAN_BERRIES = register("rowan_berries", properties -> new Item(properties.food(
            new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(6.0f).build())));

    // ------------------------------------------------------------------ o caldeirão e o que ele faz

    /** O Caldeirão da Bruxa não se fabrica: unta-se um caldeirão comum com a Pasta de Unção. */
    public static final Item WITCHES_CAULDRON = register("witches_cauldron", properties ->
            new BlockItem(OccultaBlocks.WITCHES_CAULDRON, properties.useBlockDescriptionPrefix()));

    /** A Pasta de Unção, que faz o caldeirão de um caldeirão comum. */
    public static final Item ANOINTING_PASTE = register("anointing_paste", AnointingPasteItem::new);

    /** O Mutandis, que muda uma planta noutra, e o Extremis, que muda as que ele não alcança. */
    public static final Item MUTANDIS = register("mutandis", properties -> new MutandisItem(properties, false));
    public static final Item MUTANDIS_EXTREMIS = register("mutandis_extremis",
            properties -> new MutandisItem(properties, true));

    // ------------------------------------------------------------------ os bichos do ofício

    /**
     * Os Abafadores: o {@code ItemEarmuffs} do original, que tapam o grito da mandrágora.
     *
     * <p>Não protegem de golpe nenhum — são pano nas orelhas, e é só isso que fazem.
     */
    public static final Item EARMUFFS = register("earmuffs", properties ->
            new Item(properties.humanoidArmor(OccultaMaterials.EARMUFFS,
                    net.minecraft.world.item.equipment.ArmorType.HELMET)));

    /**
     * O Frasco de Cozimento: o {@code ItemBrew} do original.
     *
     * <p>Não tem nome nem cor próprios — o que ele é vem do que estava no caldeirão. Empilha-se um a um, como
     * qualquer poção, e não aparece na aba do criativo, porque não há um frasco: há todos os que se possam
     * cozer.
     */
    public static final Item BREW = register("brew", properties ->
            new net.thaumcraft.occulta.brew.BrewItem(properties.stacksTo(1)
                    .component(net.minecraft.core.component.DataComponents.CONSUMABLE,
                            net.minecraft.world.item.component.Consumables.DEFAULT_DRINK)
                    .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE)
                    // a cor vai no componente de tinta só para o desenho: não é para se ler "tingido" no frasco
                    .component(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY,
                            new net.minecraft.world.item.component.TooltipDisplay(false,
                                    new java.util.LinkedHashSet<>(java.util.List.of(
                                            net.minecraft.core.component.DataComponents.DYED_COLOR))))));

    /** O Galho de Ent, que o Ent larga. */
    public static final Item ENT_BRANCH = register("ent_branch", Item::new);

    /** O Altar da Bruxa, que junta o poder da natureza em volta. */
    public static final Item WITCH_ALTAR = register("witch_altar", properties ->
            new BlockItem(OccultaBlocks.WITCH_ALTAR, properties.useBlockDescriptionPrefix()));

    /** As peças das árvores: o item de cada bloco, pelo nome. */
    public static final java.util.Map<String, Item> WOOD = new java.util.LinkedHashMap<>();

    static {
        // seis peças por árvore, na ordem em que aparecem na aba
        for (String árvore : java.util.List.of("rowan", "alder", "hawthorn")) {
            bloco(árvore + "_log");
            bloco(árvore + "_leaves");
            bloco(árvore + "_sapling");
            bloco(árvore + "_planks");
            bloco(árvore + "_stairs");
            bloco(árvore + "_slab");
        }
    }

    private static void bloco(String name) {
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Thaumcraft.id(name));
        WOOD.put(name, register(name, properties -> new BlockItem(block, properties.useBlockDescriptionPrefix())));
    }

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
        return register(name, properties -> new WitchSeedItem(crop, properties.useItemDescriptionPrefix()));
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
