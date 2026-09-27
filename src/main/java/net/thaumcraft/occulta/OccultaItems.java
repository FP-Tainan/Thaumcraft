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

    /** A Gota de Sorte, que o caldeirão faz e as bonecas pedem. */
    public static final Item DROP_OF_LUCK = register("drop_of_luck", Item::new);

    /** O Frasco de Vínculo: vazio, enche-se tocando alguém. */
    public static final Item TAGLOCK = register("taglock", properties -> new TaglockItem(properties.stacksTo(16)));

    /** A boneca solta, que ainda não é de ninguém. */
    public static final Item POPPET = poppet("poppet", PoppetItem.Kind.NONE);

    /** E as bonecas presas, cada uma com o seu ofício. */
    public static final Item EARTH_POPPET = poppet("earth_poppet", PoppetItem.Kind.EARTH);
    public static final Item WATER_POPPET = poppet("water_poppet", PoppetItem.Kind.WATER);
    public static final Item FIRE_POPPET = poppet("fire_poppet", PoppetItem.Kind.FIRE);
    public static final Item HUNGER_POPPET = poppet("hunger_poppet", PoppetItem.Kind.HUNGER);
    public static final Item TOOL_POPPET = poppet("tool_poppet", PoppetItem.Kind.TOOL);
    public static final Item DEATH_POPPET = poppet("death_poppet", PoppetItem.Kind.DEATH);
    public static final Item ARMOR_POPPET = poppet("armor_poppet", PoppetItem.Kind.ARMOR);
    public static final Item VOODOO_PROTECTION_POPPET = poppet("voodoo_protection_poppet",
            PoppetItem.Kind.VOODOO_PROTECTION);
    public static final Item VOODOO_POPPET = poppet("voodoo_poppet", PoppetItem.Kind.VOODOO);

    /** A Prateleira de Bonecas, em item. */
    public static final Item POPPET_SHELF = register("poppet_shelf", properties ->
            new net.minecraft.world.item.BlockItem(OccultaBlocks.POPPET_SHELF,
                    properties.useBlockDescriptionPrefix()));

    /** E os dois musgos. */
    public static final Item SPANISH_MOSS = register("spanish_moss", properties ->
            new net.minecraft.world.item.BlockItem(OccultaBlocks.SPANISH_MOSS,
                    properties.useBlockDescriptionPrefix()));
    public static final Item EMBER_MOSS = register("ember_moss", properties ->
            new net.minecraft.world.item.BlockItem(OccultaBlocks.EMBER_MOSS,
                    properties.useBlockDescriptionPrefix()));

    /** O Giz Dourado, que risca o glifo do meio. */
    public static final Item GOLDEN_CHALK = chalk("golden_chalk", () -> OccultaBlocks.CIRCLE_HEART);

    /** E os três gizes de anel. */
    public static final Item RITUAL_CHALK = chalk("ritual_chalk", () -> OccultaBlocks.RITUAL_GLYPH);
    public static final Item OTHERWHERE_CHALK = chalk("otherwhere_chalk", () -> OccultaBlocks.OTHERWHERE_GLYPH);
    public static final Item INFERNAL_CHALK = chalk("infernal_chalk", () -> OccultaBlocks.INFERNAL_GLYPH);

    /** A Cal Virgem, que a destilaria come. */
    public static final Item QUICKLIME = register("quicklime", Item::new);

    /** O Gesso, que sai dela. */
    public static final Item GYPSUM = register("gypsum", Item::new);

    /** O Óleo de Vitríolo, que come diamante. */
    public static final Item OIL_OF_VITRIOL = register("oil_of_vitriol", Item::new);

    /** A Lágrima da Deusa: abre quatro de espaço no caldeirão. */
    public static final Item TEAR_OF_THE_GODDESS = register("tear_of_the_goddess", Item::new);

    /** O Vapor de Diamante: abre seis. */
    public static final Item DIAMOND_VAPOUR = register("diamond_vapour", Item::new);

    /** O Orvalho do Ender. */
    public static final Item ENDER_DEW = register("ender_dew", Item::new);

    /** E o Mal Refinado, que é o que sobra do que era bom. */
    public static final Item REFINED_EVIL = register("refined_evil", Item::new);

    /** A Destilaria, em item. */
    public static final Item DISTILLERY = register("distillery", properties ->
            new net.minecraft.world.item.BlockItem(OccultaBlocks.DISTILLERY,
                    properties.useBlockDescriptionPrefix()));

    /**
     * A Lã de Morcego: o {@code itemBatWool} do original.
     *
     * <p>Sai de um morcego morto por alguém, uma vez em três. É ela que faz o cozimento virar <b>nuvem</b> em vez
     * de estouro.
     */
    public static final Item BAT_WOOL = register("bat_wool", Item::new);

    /** O Galho de Ent, que o Ent larga. */
    public static final Item ENT_BRANCH = register("ent_branch", Item::new);

    /** A Língua de Cão, que o lobo morto deixa. */
    public static final Item DOG_TONGUE = register("dog_tongue", Item::new);

    /**
     * O Coração de Creeper, que o creeper morto deixa.
     *
     * <p>Come-se, e por um instante o fogo não pega: é o {@code Drinkable} com resistência ao fogo do original.
     */
    public static final Item CREEPER_HEART = register("creeper_heart", properties -> new CreeperHeartItem(
            properties.food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(1).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultFood().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 20, 0)))
                            .build())));

    /** E o Dedo de Sapo, que o sapo deixa. */
    public static final Item TOE_OF_FROG = register("toe_of_frog", Item::new);

    /** A Sopa de Redstone, que sai do pote e é a base dos óleos do ofício. */
    public static final Item REDSTONE_SOUP = register("redstone_soup", Item::new);

    /** A Teia do ofício, que é a teia de aranha fiada a linha. */
    public static final Item WITCH_WEB = register("witch_web", Item::new);

    /** A Maçã Bichada, que é maçã, carne podre e açúcar. */
    public static final Item WORMY_APPLE = register("wormy_apple", Item::new);

    /**
     * O Leite Purificado: leite passado pelo Odor de Pureza, em pote de barro.
     *
     * <p>Bebendo-o, uma vez em duas ele tira <b>um</b> efeito qualquer de quem o bebeu — o que der na telha.
     */
    public static final Item PURIFIED_MILK = register("purified_milk", properties -> new PurifiedMilkItem(
            properties.food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(1).saturationModifier(0.0f).build())));

    // ------------------------------------------------------------------ os frascos do pote

    /** O Cozimento de Vinhas, que veste de vinha a parede em que bate. */
    public static final Item BREW_OF_VINES = brew("brew_of_vines", net.thaumcraft.occulta.kettle.KettleBrews.Kind.VINES);

    /** O de Espinhos, que planta cacto. */
    public static final Item BREW_OF_THORNS = brew("brew_of_thorns", net.thaumcraft.occulta.kettle.KettleBrews.Kind.THORNS);

    /** O de Tinta, que cega quem está em roda. */
    public static final Item BREW_OF_INK = brew("brew_of_ink", net.thaumcraft.occulta.kettle.KettleBrews.Kind.INK);

    /** O de Brotação, que faz crescer um galho para onde ele foi. */
    public static final Item BREW_OF_SPROUTING = brew("brew_of_sprouting", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SPROUTING);

    /** O de Erosão, que come uma bola de mundo e dá ácido em quem apanha. */
    public static final Item BREW_OF_EROSION = brew("brew_of_erosion", net.thaumcraft.occulta.kettle.KettleBrews.Kind.EROSION);

    /** O de Amor, que apaixona os bichos em volta. */
    public static final Item BREW_OF_LOVE = brew("brew_of_love", net.thaumcraft.occulta.kettle.KettleBrews.Kind.LOVE);

    /** E o de Erguer os Mortos, que levanta um morto onde bate. */
    public static final Item BREW_OF_RAISING = brew("brew_of_raising", net.thaumcraft.occulta.kettle.KettleBrews.Kind.RAISING);

    /** O de Teias, que enche de teia a casa em que bate. */
    public static final Item BREW_OF_WEBS = brew("brew_of_webs", net.thaumcraft.occulta.kettle.KettleBrews.Kind.WEBS);

    /** O de Gelo, que congela a água, ergue escudo e engaiola quem apanha. */
    public static final Item BREW_OF_ICE = brew("brew_of_ice", net.thaumcraft.occulta.kettle.KettleBrews.Kind.ICE);

    /** O de Infecção, que apodrece a pedra e adoece quem apanha. */
    public static final Item BREW_OF_INFECTION =
            brew("brew_of_infection", net.thaumcraft.occulta.kettle.KettleBrews.Kind.INFECTION);

    /** E o da Troca, que põe no chão o que está largado nele. */
    public static final Item BREW_SUBSTITUTION =
            brew("brew_substitution", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SUBSTITUTION);

    /**
     * O Cozimento das Profundezas, que não se atira: bebe-se.
     *
     * <p>Quinze segundos de mar por casa — e de terra por morte.
     */
    public static final Item BREW_OF_THE_DEPTHS = register("brew_of_the_depths", properties -> new Item(properties
            .stacksTo(1)
            .food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(0).saturationModifier(0.0f).alwaysEdible().build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            OccultaEffects.DEPTHS, 300, 0))).build())));

    /** O Caldeirão de Pote, em item. */
    public static final Item WITCHES_KETTLE = register("witches_kettle", properties ->
            new BlockItem(OccultaBlocks.WITCHES_KETTLE, properties.useBlockDescriptionPrefix()));

    /**
     * O Espelho, que se prega na parede.
     *
     * <p>Um por casa, como no original: ele leva dentro a ligação com a cela dele, e duas ligações não caberiam na
     * mesma pilha.
     */
    public static final Item WITCH_MIRROR = register("witch_mirror", properties ->
            new net.thaumcraft.occulta.mirror.MirrorItem(properties.stacksTo(1)));

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

    /** Uma boneca: as que se gastam aos poucos aguentam mil pontos, como no original. */
    private static Item poppet(String name, PoppetItem.Kind kind) {
        return register(name, properties -> new PoppetItem(kind,
                kind == PoppetItem.Kind.NONE || kind.breaks ? properties.stacksTo(16)
                        : properties.durability(1000)));
    }

    /**
     * Um giz.
     *
     * <p><b>Desvio declarado.</b> No original uma receita dá <b>duas</b> varas de sessenta e quatro riscos cada,
     * e elas empilham-se — na 1.7.10 uma coisa gasta ainda empilhava. Hoje não: o que tem desgaste vai uma por
     * casa. Por isso a receita dá <b>uma</b> vara de <b>cento e vinte e oito</b> riscos, que é o mesmo giz na
     * mesma conta, numa vara só.
     */
    private static Item chalk(String name, java.util.function.Supplier<net.minecraft.world.level.block.Block> glyph) {
        return register(name, properties -> new ChalkItem(glyph, properties.durability(128)));
    }

    /** Um frasco do Caldeirão de Pote: atira-se, e vai até dezesseis por casa como as poções do jogo. */
    private static Item brew(String name, net.thaumcraft.occulta.kettle.KettleBrews.Kind kind) {
        return register(name, properties ->
                new net.thaumcraft.occulta.kettle.KettleBrewItem(kind, properties.stacksTo(16)));
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
