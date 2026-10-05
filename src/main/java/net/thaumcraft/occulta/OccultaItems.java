package net.thaumcraft.occulta;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.equipment.ArmorType;
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
    public static final Item ICY_NEEDLE = register("icy_needle", properties ->
            new net.thaumcraft.occulta.spirit.IcyNeedleItem(properties));

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

    /**
     * O <b>Coração Congelado</b>: o {@code itemFrozenHeart}.
     *
     * <p>Uma agulha de gelo enfiada num coração de creeper, com uma lágrima de ghast por baixo. É o que acende
     * o <b>Cozimento da Casca de Gelo</b> e o <b>Rito da Expansão Gelada</b> — as duas maneiras que o ofício
     * tem de cobrir um pedaço do mundo com gelo que não derrete.
     *
     * <p><b>Declarado:</b> no original, comê-lo <b>apaga os efeitos de infusão</b> de quem o come, que é o
     * botão de desfazer daquele ramo. A infusão não está portada; quando vier, é aqui que isto entra.
     */
    public static final Item FROZEN_HEART = register("frozen_heart", properties ->
            new Item(properties.food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(1).saturationModifier(0.0f).build())));

    public static final Item PERPETUAL_ICE = register("perpetual_ice", properties ->
            new BlockItem(OccultaBlocks.PERPETUAL_ICE, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_STAIRS = register("ice_stairs", properties ->
            new BlockItem(OccultaBlocks.ICE_STAIRS, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_SLAB = register("ice_slab", properties ->
            new BlockItem(OccultaBlocks.ICE_SLAB, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_FENCE = register("ice_fence", properties ->
            new BlockItem(OccultaBlocks.ICE_FENCE, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_FENCE_GATE = register("ice_fence_gate", properties ->
            new BlockItem(OccultaBlocks.ICE_FENCE_GATE, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_PRESSURE_PLATE = register("ice_pressure_plate", properties ->
            new BlockItem(OccultaBlocks.ICE_PRESSURE_PLATE, properties.useBlockDescriptionPrefix()));

    public static final Item ICE_DOOR = register("ice_door", properties ->
            new net.minecraft.world.item.DoubleHighBlockItem(OccultaBlocks.ICE_DOOR,
                    properties.useBlockDescriptionPrefix()));

    public static final Item SNOW_STAIRS = register("snow_stairs", properties ->
            new BlockItem(OccultaBlocks.SNOW_STAIRS, properties.useBlockDescriptionPrefix()));

    public static final Item SNOW_SLAB = register("snow_slab", properties ->
            new BlockItem(OccultaBlocks.SNOW_SLAB, properties.useBlockDescriptionPrefix()));

    public static final Item SNOW_PRESSURE_PLATE = register("snow_pressure_plate", properties ->
            new BlockItem(OccultaBlocks.SNOW_PRESSURE_PLATE, properties.useBlockDescriptionPrefix()));

    public static final Item LEECH_CHEST = register("leech_chest", properties ->
            new BlockItem(OccultaBlocks.LEECH_CHEST, properties.useBlockDescriptionPrefix()));

    public static final Item GRASSPER = register("grassper", properties ->
            new BlockItem(OccultaBlocks.GRASSPER, properties.useBlockDescriptionPrefix()));

    // ------------------------------------------------------------------ a infusão

    /**
     * O <b>Unguento Místico</b>: o que o rito da Vara Mística pede, com o Galho de Ent.
     *
     * <p>Bebido, dá <b>Fraqueza II por um minuto</b>.
     */
    public static final Item MYSTIC_UNGUENT = register("mystic_unguent", properties ->
            new Item(properties.stacksTo(2).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.WEAKNESS, 1200, 1)))
                            .build())));

    /**
     * A <b>Vara Mística</b>: a que desenha os símbolos.
     *
     * <p>Uma só de cada vez, e ela <b>brilha</b> como uma coisa encantada — é o que o original lhe faz,
     * e é o único sinal de que ela não é um pau.
     */
    public static final Item MYSTIC_BRANCH = register("mystic_branch", properties ->
            new net.thaumcraft.occulta.MysticBranchItem(properties.stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.RARE)
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,
                            true)));

    /**
     * A <b>Mão de Bruxa</b>: a única coisa que a infusão sabe atravessar.
     *
     * <p>Uma só de cada vez, e não se fabrica: cai de uma bruxa morta.
     */
    public static final Item WITCH_HAND = register("witch_hand", properties ->
            new net.thaumcraft.occulta.WitchHandItem(properties.stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /**
     * O <b>Ânimo Infernal</b>: o que o rito da Infusão Infernal pede.
     *
     * <p>Bebido, dá <b>Veneno II por um minuto</b> <i>e</i> <b>Deperecimento III por três</b> — e é o único
     * dos quatro que mata de verdade quem o beber. O Coração de Demônio, que é um dos que entram nele,
     * estava esperando por ele desde que foi portado.
     */
    public static final Item INFERNAL_ANIMUS = register("infernal_animus", properties ->
            new Item(properties.stacksTo(2).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    java.util.List.of(
                                            new net.minecraft.world.effect.MobEffectInstance(
                                                    net.minecraft.world.effect.MobEffects.POISON, 1200, 1),
                                            new net.minecraft.world.effect.MobEffectInstance(
                                                    net.minecraft.world.effect.MobEffects.WITHER, 3600, 2)),
                                    1.0f))
                            .build())));

    /**
     * O <b>Fantasma da Luz</b>: o que o rito da Infusão da Luz pede.
     *
     * <p>Bebido, dá <b>Veneno II por um minuto</b>. Ele também não existe para se beber.
     */
    public static final Item GHOST_OF_THE_LIGHT = register("ghost_of_the_light", properties ->
            new Item(properties.stacksTo(2).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.POISON, 1200, 1)))
                            .build())));

    /**
     * O <b>Espírito do Outro Lugar</b>: o que o rito da Infusão do Outro Lugar pede.
     *
     * <p>Bebido, dá <b>Veneno II por um minuto</b> — e é o que ele merece. Ele não existe para se
     * beber; existe para se oferecer no círculo.
     */
    public static final Item SPIRIT_OF_OTHERWHERE = register("spirit_of_otherwhere", properties ->
            new Item(properties.stacksTo(2).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.POISON, 1200, 1)))
                            .build())));

    /** A <b>Caveira do Chamado</b>, que se prega no chão ou numa parede. */
    public static final Item ALLURING_SKULL = register("alluring_skull", properties ->
            new BlockItem(OccultaBlocks.ALLURING_SKULL, properties.useBlockDescriptionPrefix()
                    .stacksTo(1)));

    /** O <b>Ovo do Infinito</b>, que não se fabrica: ele só existe na aba do criativo. */
    public static final Item INFINITY_EGG = register("infinity_egg", properties ->
            new BlockItem(OccultaBlocks.INFINITY_EGG, properties.useBlockDescriptionPrefix()));

    /** A <b>Bola de Cristal</b>, que não se compra: sai do rito que a faz aparecer. */
    public static final Item CRYSTAL_BALL = register("crystal_ball", properties ->
            new BlockItem(OccultaBlocks.CRYSTAL_BALL, properties.useBlockDescriptionPrefix()));

    /**
     * O <b>Óleo do Acaso</b>: o {@code itemHappenstanceOil} do original.
     *
     * <p>Bebido, dá <b>Visão Noturna por um minuto</b>, e é um uso bobo para ele: o que ele serve mesmo
     * é para o rito que faz aparecer a Bola de Cristal. O nome diz o que ele é — o acaso engarrafado —,
     * e é por isso que a adivinhação começa por ele.
     */
    public static final Item HAPPENSTANCE_OIL = register("happenstance_oil", properties ->
            new Item(properties.stacksTo(1).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.NIGHT_VISION, 1200, 0)))
                            .build())));

    /** O <b>Candelabro</b>, que se põe em qualquer chão firme. */
    public static final Item CANDELABRA = register("candelabra", properties ->
            new BlockItem(OccultaBlocks.CANDELABRA, properties.useBlockDescriptionPrefix()));

    /** O <b>Cálice</b> vazio. */
    public static final Item CHALICE = register("chalice", properties ->
            new ChaliceItem(false, properties));

    /** E o <b>cheio</b>, que é o mesmo bloco posto de outro jeito. */
    public static final Item FILLED_CHALICE = register("filled_chalice", properties ->
            new ChaliceItem(true, properties));

    public static final Item WICKER_BUNDLE = register("wicker_bundle", properties ->
            new net.thaumcraft.occulta.WickerBundleItem(properties.useBlockDescriptionPrefix()));

    public static final Item SHADED_GLASS = register("shaded_glass", properties ->
            new net.thaumcraft.occulta.ShadedGlassItem(properties.useBlockDescriptionPrefix()));

    public static final Item BLOODED_WOOL = register("blooded_wool", properties ->
            new BlockItem(OccultaBlocks.BLOODED_WOOL, properties.useBlockDescriptionPrefix()));

    /** O <b>Pano Escuro</b>: o que sai da lã ensanguentada no forno, e de que se fazem as roupas. */
    public static final Item DARK_CLOTH = register("dark_cloth", Item::new);

    public static final Item STOCKADE = register("stockade", properties ->
            new net.thaumcraft.occulta.StockadeItem(properties.useBlockDescriptionPrefix()));

    public static final Item ICE_STOCKADE = register("ice_stockade", properties ->
            new BlockItem(OccultaBlocks.ICE_STOCKADE, properties.useBlockDescriptionPrefix()));

    public static final Item PLANT_MINE = register("plant_mine", properties ->
            new net.thaumcraft.occulta.PlantMineItem(properties.useBlockDescriptionPrefix()));

    public static final Item CRITTER_SNARE = register("critter_snare", properties ->
            new net.thaumcraft.occulta.CritterSnareItem(properties.useBlockDescriptionPrefix()));

    public static final Item BEARTRAP = register("beartrap", properties ->
            new BlockItem(OccultaBlocks.BEARTRAP, properties.useBlockDescriptionPrefix()));

    public static final Item WOLFTRAP = register("wolftrap", properties ->
            new BlockItem(OccultaBlocks.WOLFTRAP, properties.useBlockDescriptionPrefix()));

    public static final Item WILD_BRAMBLE = register("wild_bramble", properties ->
            new BlockItem(OccultaBlocks.WILD_BRAMBLE, properties.useBlockDescriptionPrefix()));

    public static final Item ENDER_BRAMBLE = register("ender_bramble", properties ->
            new BlockItem(OccultaBlocks.ENDER_BRAMBLE, properties.useBlockDescriptionPrefix()));

    public static final Item VOID_BRAMBLE = register("void_bramble", properties ->
            new BlockItem(OccultaBlocks.VOID_BRAMBLE, properties.useBlockDescriptionPrefix()));

    public static final Item LEAPING_LILY = register("leaping_lily", properties ->
            new net.minecraft.world.item.PlaceOnWaterBlockItem(OccultaBlocks.LEAPING_LILY,
                    properties.useBlockDescriptionPrefix()));

    /** A <b>Porta de Amieiro</b>, que é só uma porta. */
    public static final Item ALDER_DOOR = register("alder_door", properties ->
            new net.minecraft.world.item.DoubleHighBlockItem(OccultaBlocks.ALDER_DOOR,
                    properties.useBlockDescriptionPrefix()));

    /** A <b>Porta de Sorveira</b>, que larga a chave dela ao ser posta. */
    public static final Item ROWAN_DOOR = register("rowan_door", properties ->
            new net.thaumcraft.occulta.door.RowanDoorItem(properties.useBlockDescriptionPrefix()));

    /** A <b>Chave</b> daquela porta, e só daquela. */
    public static final Item DOOR_KEY = register("door_key", properties ->
            new net.thaumcraft.occulta.door.DoorKeyItem(properties));

    /** E o <b>Chaveiro</b>, que vale por todas as que tem. */
    public static final Item DOOR_KEYRING = register("door_keyring", properties ->
            new net.thaumcraft.occulta.door.KeyringItem(properties));

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

    /**
     * A <b>Esfera de Quartzo</b>: o {@code itemQuartzSphere} do original.
     *
     * <p>Um vidro vazio, e nada mais — até alguém o pôr num {@linkplain
     * net.thaumcraft.occulta.vampire.DaylightCollectorBlock Coletor de Luz} e deixar o sol entrar nele.
     */
    public static final Item QUARTZ_SPHERE = register("quartz_sphere", Item::new);

    /** A <b>Granada Solar</b>: sol engarrafado, e a única dose de dia que um vampiro aguenta. */
    public static final Item SUN_GRENADE = register("sun_grenade", properties ->
            new net.thaumcraft.occulta.vampire.SunGrenadeItem(properties.stacksTo(16)));

    /** Uma <b>Página Rasgada</b> do Livro do Vampiro, que só cai para quem já tem o livro. */
    public static final Item TORN_PAGE = register("torn_page", properties ->
            new Item(properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /** E o <b>Livro do Vampiro</b>, que levanta o teto do grau até onde as páginas dele chegam. */
    public static final Item VAMPIRE_BOOK = register("vampire_book", properties ->
            new net.thaumcraft.occulta.vampire.VampireBookItem(
                    properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /**
     * O <b>Coração de Demônio</b>: agachado põe-se no chão, de pé come-se.
     *
     * <p>Comê-lo dá mais poder do que qualquer outra coisa deste mod — e põe fogo em quem o comeu por doze
     * segundos a mais do que a proteção contra fogo dura.
     */
    public static final Item DEMON_HEART = register("demon_heart", properties ->
            new net.thaumcraft.occulta.demon.DemonHeartItem(
                    properties.rarity(net.minecraft.world.item.Rarity.RARE)));

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

    /**
     * A Pedra Sintonizada: o {@code itemAttunedStone} do Witchery.
     *
     * <p>Um diamante passado por um Sopro de Magia sobre um balde de lava. É a pedra que faz uma coisa de madeira
     * e ferro virar coisa de bruxa — está no Caldeirão de Pote, na Destilaria e na Roca.
     */
    public static final Item ATTUNED_STONE = register("attuned_stone", properties ->
            new Item(properties.rarity(net.minecraft.world.item.Rarity.RARE)));

    /**
     * A Pedra Sintonizada <b>Carregada</b>: o {@code itemAttunedStoneCharged} do Witchery.
     *
     * <p>É a mesma pedra passada pelo Rito da Carga, e é a coisa que os ritos <b>grandes</b> pedem. Vinte e
     * cinco dos noventa e seis ritos do original não correm sem ela.
     */
    public static final Item ATTUNED_STONE_CHARGED = register("attuned_stone_charged", properties ->
            new Item(properties.rarity(net.minecraft.world.item.Rarity.RARE)));

    /** O Fio Dourado, que a Roca fia de um fardo de feno. */
    public static final Item GOLDEN_THREAD = register("golden_thread", Item::new);

    /** A Roca, em item. */
    public static final Item SPINNING_WHEEL = register("spinning_wheel", properties ->
            new BlockItem(OccultaBlocks.SPINNING_WHEEL, properties.useBlockDescriptionPrefix()));

    /**
     * A <b>Cabeça de Lobo</b>, que se põe no chão ou se prega numa parede.
     *
     * <p>Um item só para os dois blocos, como o crânio do jogo.
     */
    public static final Item WOLF_HEAD = register("mounted_wolf_head", properties ->
            new net.minecraft.world.item.StandingAndWallBlockItem(OccultaBlocks.WOLF_HEAD,
                    OccultaBlocks.WOLF_HEAD_WALL, net.minecraft.core.Direction.DOWN,
                    properties.useBlockDescriptionPrefix()));

    /** A <b>Estátua do Lobisomem</b>, que é quem dá os dez graus. */
    public static final Item WEREWOLF_STATUE = register("werewolf_statue", properties ->
            new BlockItem(OccultaBlocks.WEREWOLF_STATUE, properties.useBlockDescriptionPrefix()));

    /** O Braseiro, em item. */
    public static final Item BRAZIER = register("brazier", properties ->
            new BlockItem(OccultaBlocks.BRAZIER, properties.useBlockDescriptionPrefix()));

    /** E o Crisol de Sangue. */
    public static final Item BLOOD_CRUCIBLE = register("blood_crucible", properties ->
            new BlockItem(OccultaBlocks.BLOOD_CRUCIBLE, properties.useBlockDescriptionPrefix()));

    /**
     * A <b>Rosa de Sangue</b> na mão.
     *
     * <p>Ela existe como item porque a Boline a colhe, e não porque se compre em lado nenhum: a única
     * maneira de ter a primeira é fazê-la nascer com Mutandis Extremis.
     */
    public static final Item BLOOD_ROSE = register("blood_rose", properties ->
            new BlockItem(OccultaBlocks.BLOOD_ROSE, properties.useBlockDescriptionPrefix()));

    /** E a <b>Guirlanda de Alho</b>, que se pendura numa parede. */
    public static final Item GARLIC_GARLAND = register("garlic_garland", properties ->
            new BlockItem(OccultaBlocks.GARLIC_GARLAND, properties.useBlockDescriptionPrefix()));

    /** O <b>Coletor de Luz</b>, que enche a Esfera de Quartzo de sol. */
    public static final Item DAYLIGHT_COLLECTOR = register("daylight_collector", properties ->
            new BlockItem(OccultaBlocks.DAYLIGHT_COLLECTOR, properties.useBlockDescriptionPrefix()));

    /** E o <b>Caixão</b>, que é a casa de um vampiro e a última coisa que a escada dele pede. */
    public static final Item COFFIN = register("coffin", properties ->
            new BlockItem(OccultaBlocks.COFFIN, properties.useBlockDescriptionPrefix()));

    // ------------------------------------------------------------------ o que é de sonho

    /**
     * A Maçã do Sono: o {@code itemSleepingApple} do Witchery.
     *
     * <p>Come-se, e se dorme — o corpo fica e o espírito se levanta. É a porta do Mundo dos Espíritos, e ela abre
     * sempre para o lado feio: quem a come sem um Apanhador de Sonhos por perto cai em pesadelo.
     */
    public static final Item SLEEPING_APPLE = register("sleeping_apple", properties ->
            new net.thaumcraft.occulta.spirit.SleepingAppleItem(properties.stacksTo(1)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(3).saturationModifier(3.0f).alwaysEdible().build())));

    /** O Algodão Sonhador, em item. */
    public static final Item WISPY_COTTON = register("wispy_cotton", properties ->
            new BlockItem(OccultaBlocks.WISPY_COTTON, properties.useBlockDescriptionPrefix()));

    /** O Algodão Perturbado, que é o mesmo colhido em pesadelo. */
    public static final Item DISTURBED_COTTON = register("disturbed_cotton", Item::new);

    /** A Erva Cintilante, em item. */
    public static final Item GLINT_WEED = register("glint_weed", properties ->
            new BlockItem(OccultaBlocks.GLINT_WEED, properties.useBlockDescriptionPrefix()));

    /** A Fome Melíflua, que só se tira de um pesadelo morto. */
    public static final Item MELLIFLUOUS_HUNGER = register("mellifluous_hunger", Item::new);

    /** O Fio Enfeitado, que a Roca fia do Algodão Sonhador. */
    public static final Item FANCIFUL_THREAD = register("fanciful_thread", Item::new);

    /** E o Cordel Atormentado, que ela fia do Perturbado. */
    public static final Item TORMENTED_TWINE = register("tormented_twine", Item::new);

    /**
     * O Cozimento do Sono, que se bebe para passar ao outro lado.
     *
     * <p>Sai do Caldeirão de Pote, três de cada vez.
     */
    public static final Item BREW_OF_SLEEPING = register("brew_of_sleeping", properties ->
            new net.thaumcraft.occulta.spirit.BrewOfSleepingItem(properties
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(0).saturationModifier(0.0f).alwaysEdible().build(),
                            net.minecraft.world.item.component.Consumables.defaultDrink().build())));

    /**
     * O Cozimento do Espírito Corrente, que só se coze <b>do outro lado</b>: o {@code BrewFluid} do original.
     *
     * <p>Atira-se, e onde bate fica uma <b>poça de Espírito Fluente</b>.
     */
    public static final Item BREW_OF_FLOWING_SPIRIT =
            brew("brew_of_flowing_spirit", net.thaumcraft.occulta.kettle.KettleBrews.Kind.FLOWING_SPIRIT);

    /** E o das Lágrimas Ocas, que sai da Destilaria e faz a poça do contrário. */
    public static final Item BREW_OF_HOLLOW_TEARS =
            brew("brew_of_hollow_tears", net.thaumcraft.occulta.kettle.KettleBrews.Kind.HOLLOW_TEARS);

    /** A Vontade Focada, que a Destilaria tira do Espírito Corrente. */
    public static final Item FOCUSED_WILL = register("focused_will", Item::new);

    /** E o Medo Condensado, que sai da mesma destilação. */
    public static final Item CONDENSED_FEAR = register("condensed_fear", Item::new);

    /**
     * Os cinco Cozimentos Sólidos: o {@code BrewSolidifySpirit} do original.
     *
     * <p>Cada um endurece uma poça inteira de <b>Lágrimas Ocas</b> no que o nome dele diz — e o da Erosão, em
     * nada: ele tira a poça e o chão debaixo dela.
     */
    public static final Item BREW_OF_SOLID_ROCK =
            brew("brew_of_solid_rock", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SOLID_ROCK);
    public static final Item BREW_OF_SOLID_DIRT =
            brew("brew_of_solid_dirt", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SOLID_DIRT);
    public static final Item BREW_OF_SOLID_SAND =
            brew("brew_of_solid_sand", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SOLID_SAND);
    public static final Item BREW_OF_SOLID_SANDSTONE =
            brew("brew_of_solid_sandstone", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SOLID_SANDSTONE);
    public static final Item BREW_OF_SOLID_EROSION =
            brew("brew_of_solid_erosion", net.thaumcraft.occulta.kettle.KettleBrews.Kind.SOLID_EROSION);

    // ------------------------------------------------------------------ a faca do ofício e o que ela abre

    /** A Arthana, a faca de ouro com a vida do ferro. */
    public static final Item ARTHANA = register("arthana", properties ->
            new net.thaumcraft.occulta.ArthanaItem(properties
                    .sword(net.thaumcraft.occulta.ArthanaItem.MATERIAL, 3.0f, -2.4f)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /** O Pó Espectral, que só sai de morto-vivo aberto pela Arthana. */
    public static final Item SPECTRAL_DUST = register("spectral_dust", Item::new);

    /** O Pó de Cemitério, que é o Espectral passado por farinha de osso e Mutandis. */
    public static final Item GRAVEYARD_DUST = register("graveyard_dust", Item::new);

    /** E a Pedra Necrótica, que o Rito de Necromancia faz e que o Braseiro pede. */
    public static final Item NECROTIC_STONE = register("necrotic_stone", properties ->
            new Item(properties.rarity(net.minecraft.world.item.Rarity.RARE)));

    /** Os dois baldes, que é como os líquidos se carregam de um lado para o outro. */
    public static final Item BUCKET_FLOWING_SPIRIT = register("bucket_flowing_spirit", properties ->
            new net.minecraft.world.item.BucketItem(net.thaumcraft.occulta.spirit.SpiritFluids.FLOWING_SPIRIT,
                    properties.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

    public static final Item BUCKET_HOLLOW_TEARS = register("bucket_hollow_tears", properties ->
            new net.minecraft.world.item.BucketItem(net.thaumcraft.occulta.spirit.SpiritFluids.HOLLOW_TEARS,
                    properties.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

    /** As cinco Teias de Sonho, uma por feitio de sonho. */
    public static final Item DREAM_WEAVE_MOVE =
            weave("dream_weave_move", net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave.MOVE);
    public static final Item DREAM_WEAVE_DIG =
            weave("dream_weave_dig", net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave.DIG);
    public static final Item DREAM_WEAVE_EAT =
            weave("dream_weave_eat", net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave.EAT);
    public static final Item DREAM_WEAVE_NIGHTMARE =
            weave("dream_weave_nightmare", net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave.NIGHTMARE);
    public static final Item DREAM_WEAVE_INTENSITY =
            weave("dream_weave_intensity", net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave.INTENSITY);

    /** A teia daquele feitio, para quem tiver o feitio e precisar do item. */
    public static Item weave(net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave qual) {
        return switch (qual) {
            case MOVE -> DREAM_WEAVE_MOVE;
            case DIG -> DREAM_WEAVE_DIG;
            case EAT -> DREAM_WEAVE_EAT;
            case NIGHTMARE -> DREAM_WEAVE_NIGHTMARE;
            case INTENSITY -> DREAM_WEAVE_INTENSITY;
        };
    }

    private static Item weave(String name, net.thaumcraft.occulta.spirit.DreamWeaveItem.Weave qual) {
        return register(name, properties ->
                new net.thaumcraft.occulta.spirit.DreamWeaveItem(qual, properties.stacksTo(1)));
    }

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
     * e elas se empilham — na 1.7.10 uma coisa gasta ainda empilhava. Hoje não: o que tem desgaste vai uma por
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

    /**
     * O <b>Cozimento do Grotesco</b>, que se bebe.
     *
     * <p>Por um minuto, nada de vivo consegue chegar a quatro blocos de quem o bebeu — tudo é empurrado para
     * trás. É o ingrediente das maldições, e faz sentido que seja: para amaldiçoar alguém não é preciso
     * força, é preciso que ninguém chegue perto do círculo.
     */
    public static final Item BREW_GROTESQUE = register("brew_grotesque", properties ->
            new net.thaumcraft.occulta.curse.GrotesqueBrewItem(properties.stacksTo(16)
                    .component(net.minecraft.core.component.DataComponents.CONSUMABLE,
                            net.minecraft.world.item.component.Consumables.DEFAULT_DRINK)
                    .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE)));

    /**
     * O <b>Pó de Prata</b>: o {@code itemSilverDust} do Witchery.
     *
     * <p>Cai de lobisomem, e é a única prata do ofício. É de propósito que ela venha do próprio bicho: o
     * primeiro lobisomem mata-se a pancada, oitenta vezes, e os outros com o que ele deixou.
     */
    /**
     * O <b>Amuleto da Lua</b>: o {@code ItemMoonCharm} do original.
     *
     * <p>Na mochila ele <b>trava a forma</b> de um lobisomem; na mão, muda-a. É a única coisa que um
     * lobisomem não larga ao virar bicho.
     */
    public static final Item MOON_CHARM = register("moon_charm", properties ->
            new net.thaumcraft.occulta.wolf.MoonCharmItem(properties.stacksTo(1)
                    .durability(net.thaumcraft.occulta.wolf.MoonCharmItem.AGUENTA)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));

    /**
     * O <b>Chifre da Caça</b>, que a Estátua do Lobisomem dá ao quarto grau.
     *
     * <p>Sopra-se uma vez e ele parte-se: aguenta um e gasta dois.
     */
    public static final Item HORN_OF_THE_HUNT = register("horn_of_the_hunt", properties ->
            new net.thaumcraft.occulta.wolf.HornOfTheHuntItem(properties.stacksTo(1)
                    .durability(net.thaumcraft.occulta.wolf.HornOfTheHuntItem.AGUENTA)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));

    /** O dano e a velocidade da lança: um a mais do que a espada de diamante, e a cadência dela. */
    public static final float LANÇA_DANO = 4.0f;
    public static final float LANÇA_VELOCIDADE = -2.4f;

    /**
     * E o que ela apara: <b>metade</b>, num arco de noventa graus.
     *
     * <p>No original a lança é uma {@code ItemSword}, e na 1.7.10 <b>toda espada aparava</b> — segurava-se o
     * botão direito e o golpe valia metade. Hoje só o escudo apara, e por isso a lança diz por si mesma que
     * apara: é a mesma metade, pelo jeito de hoje. Sem isto o aviso dela mentiria, porque o lobo que ela
     * chama só vem a quem apanha <b>aparando</b>.
     */
    public static final float LANÇA_APARA = 0.5f;
    public static final float LANÇA_ARCO = 90.0f;

    /**
     * A <b>Lança do Caçador</b>, que cai do Caçador Cornudo uma vez em quatro.
     *
     * <p>Um ponto de dano <b>acima de uma espada de diamante</b> — é o {@code BONUS_DAMAGE} do original — e,
     * na mão, <b>ninguém a empurra</b>: a resistência a empurrão é o que faz dela a arma de quem não sai do
     * lugar.
     *
     * <p>O original ainda lhe nega cavar qualquer bloco. Hoje as regras de cavar de uma espada são as do
     * jogo, e forçá-las a nada não mudaria nada que se sinta: a lança não é ferramenta de ninguém.
     */
    public static final Item HUNTSMANS_SPEAR = register("huntsmans_spear", properties ->
            new Item(properties
                    .sword(net.minecraft.world.item.ToolMaterial.DIAMOND, LANÇA_DANO, LANÇA_VELOCIDADE)
                    .component(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                            net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                                    .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                    Item.BASE_ATTACK_DAMAGE_ID, LANÇA_DANO,
                                                    net.minecraft.world.entity.ai.attributes.AttributeModifier
                                                            .Operation.ADD_VALUE),
                                            net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                                    .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                    Item.BASE_ATTACK_SPEED_ID, LANÇA_VELOCIDADE,
                                                    net.minecraft.world.entity.ai.attributes.AttributeModifier
                                                            .Operation.ADD_VALUE),
                                            net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                                    .add(net.minecraft.world.entity.ai.attributes.Attributes
                                                    .KNOCKBACK_RESISTANCE,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                    net.thaumcraft.Thaumcraft.id("huntsmans_spear_knockback"),
                                                    1.0, net.minecraft.world.entity.ai.attributes
                                                    .AttributeModifier.Operation.ADD_VALUE),
                                            net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                                    .build())
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(net.minecraft.core.component.DataComponents.LORE,
                            new net.minecraft.world.item.component.ItemLore(java.util.List.of(
                                    net.minecraft.network.chat.Component
                                            .translatable("tc.huntsmansspear.tip")
                                            .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE))))
                    .component(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS,
                            new net.minecraft.world.item.component.BlocksAttacks(0.0f, 1.0f,
                                    java.util.List.of(new net.minecraft.world.item.component.BlocksAttacks
                                            .DamageReduction(LANÇA_ARCO, java.util.Optional.empty(),
                                            0.0f, LANÇA_APARA)),
                                    net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction
                                            .DEFAULT,
                                    java.util.Optional.empty(), java.util.Optional.empty(),
                                    java.util.Optional.empty()))
                    .rarity(net.minecraft.world.item.Rarity.EPIC)));

    /** O <b>Sangue Infernal</b>, que cai do Caçador Cornudo. */
    public static final Item INFERNAL_BLOOD = register("infernal_blood", Item::new);

    /** O dano e a cadência da Boline: os de uma faca de madeira. */
    public static final float BOLINE_DANO = 3.0f;
    public static final float BOLINE_VELOCIDADE = -2.4f;

    /**
     * A <b>Boline</b>: a faca de colher do ofício.
     *
     * <p>Bate como uma de madeira e dura como uma de ferro, e é de propósito: ela não é arma, é
     * <b>ferramenta</b>. O que ela faz de especial é cortar folha, teia, relva, trepadeira e fio-armadilha
     * <b>sem se gastar</b> — e sacrificar a galinha que enche o Cálice.
     */
    public static final Item BOLINE = register("boline", properties ->
            new net.thaumcraft.occulta.vampire.BolineItem(properties
                    .sword(net.minecraft.world.item.ToolMaterial.IRON, BOLINE_DANO, BOLINE_VELOCIDADE)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /**
     * O <b>Cálice de Vidro</b>, que é a porta de entrada da vampirice.
     *
     * <p>Vazio é um copo; cheio do sangue de Lilith, é a única coisa no mod que faz de alguém um vampiro.
     */
    public static final Item GOBLET = register("goblet", properties ->
            new net.thaumcraft.occulta.vampire.GobletItem(properties.stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final Item SILVER_DUST = register("silver_dust", Item::new);

    /**
     * A <b>Espada de Prata</b>: uma espada de ouro com oito pós de prata à volta.
     *
     * <p>Na mão do jogo ela é uma espada de ouro e nada mais — a prata não a faz melhor contra nada. O que ela
     * faz é ser a <b>única coisa que fere um lobisomem</b> de verdade.
     */
    public static final Item SILVER_SWORD = register("silver_sword", properties ->
            new Item(properties.sword(net.minecraft.world.item.ToolMaterial.GOLD, 3.0f, -2.4f)));

    /**
     * A <b>Vassoura</b>: dois gravetos e três mudas de espinheiro-alvar.
     *
     * <p>Sozinha ela não voa e não se põe no chão — é <b>ingrediente</b>, e só. Quem voa é a encantada, e para
     * chegar a ela a vassoura tem de passar pelo Rito da Infusão do Céu. É assim no original.
     */
    public static final Item BROOM = register("broom", Item::new);

    /**
     * A <b>Vassoura Encantada</b>, que se põe no chão e se monta.
     *
     * <p>Sai do <b>Rito da Infusão do Céu</b>, de noite, com uma vassoura e um Unguento do Voo no chão e três
     * mil de poder de altar. E volta a ser item quando a vassoura posta se desfaz.
     */
    public static final Item ENCHANTED_BROOM = register("enchanted_broom", properties ->
            new Item(properties.stacksTo(1)) {
                @Override
                public net.minecraft.world.InteractionResult useOn(
                        net.minecraft.world.item.context.UseOnContext uso) {
                    return net.thaumcraft.occulta.broom.Brooms.põe(uso);
                }
            });

    /**
     * O <b>Unguento do Voo</b>: o {@code itemFlyingOintment}, que se bebe e envenena.
     *
     * <p>Beber é um mau negócio — <b>Veneno III por um minuto</b> —, e é de propósito: ele não existe para se
     * beber, existe para se oferecer no círculo. É o original, tal e qual.
     */
    public static final Item FLYING_OINTMENT = register("flying_ointment", properties ->
            new Item(properties.stacksTo(2).food(
                    new net.minecraft.world.food.FoodProperties.Builder()
                            .alwaysEdible().nutrition(0).saturationModifier(0.0f).build(),
                    net.minecraft.world.item.component.Consumables.defaultDrink().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.POISON, 1200, 2)))
                            .build())));

    // ------------------------------------------------------------------ a pedra de caminho

    /**
     * A <b>Pedra de Caminho</b> lisa, que não sabe ir a lugar nenhum: o {@code itemWaystone}.
     *
     * <p>O que ela faz, faz <b>largada no chão</b>, e está em {@link net.thaumcraft.occulta.waystone.Waystones}.
     */
    public static final Item WAYSTONE = register("waystone", Item::new);

    /** A mesma, presa a um lugar: o {@code itemWaystoneBound}. */
    public static final Item BOUND_WAYSTONE = register("bound_waystone",
            properties -> new net.thaumcraft.occulta.waystone.WaystoneItem(properties.stacksTo(8)));

    /**
     * E a mesma presa a um <b>bicho</b>, que é a que custa poder ao altar: o
     * {@code itemWaystonePlayerBound}, a <b>Pedra Sangrada</b>.
     *
     * <p>Ela não guarda um lugar: guarda <b>quem</b>. O destino dela é onde esse alguém estiver na hora, e por
     * isso ela é a única pedra que aponta para um lugar que anda.
     */
    public static final Item BLOODED_WAYSTONE = register("blooded_waystone",
            properties -> new net.thaumcraft.occulta.waystone.WaystoneItem(properties.stacksTo(1)));

    // ------------------------------------------------------------------ o caçador de bruxas

    /**
     * O <b>Catalisador Nulo</b>: o {@code itemNullCatalyst} do original.
     *
     * <p>É a estrela do Nether moída com diamante, pederneira e seis pérolas do Alhures — e o que ele faz é
     * <b>tirar a magia de uma coisa</b>. Com ele se curte o couro que não deixa magia passar, e dele saem os
     * virotes anuladores.
     *
     * <p>E ele <b>multiplica-se</b>: um catalisador com uma pérola e um pó de blaze fazem dois.
     */
    public static final Item NULL_CATALYST = register("null_catalyst", Item::new);

    /**
     * O <b>Couro Anulado</b>: o {@code itemNullifiedLeather}, que é de que se fazem as roupas de caçador.
     *
     * <p>Oito couros em volta de um catalisador dão três — e é só com ele que o conjunto se costura. É por
     * isso que as roupas protegem de maldição: elas são feitas do que não deixa magia passar.
     */
    public static final Item NULLIFIED_LEATHER = register("nullified_leather", Item::new);

    /**
     * A <b>Besta de Mão</b>: o {@code ItemHandBow} do original.
     *
     * <p>Ela se carrega, e o que tem dentro fica lá. O gesto está em
     * {@link net.thaumcraft.occulta.hunter.CrossbowPistolItem}.
     */
    public static final Item CROSSBOW_PISTOL = register("crossbow_pistol", properties ->
            new net.thaumcraft.occulta.hunter.CrossbowPistolItem(properties.stacksTo(1).durability(768)));

    /** O virote de <b>estaca</b>: madeira, que é do que se mata vampiro. */
    public static final Item STAKE_BOLT = register("stake_bolt", Item::new);

    /** O <b>anti-magia</b>, que chupa o poder de quem acerta. */
    public static final Item ANTI_MAGIC_BOLT = register("anti_magic_bolt", Item::new);

    /** O <b>sagrado</b>, que vale uma vez e meia contra morto-vivo e coisa do inferno. */
    public static final Item HOLY_BOLT = register("holy_bolt", Item::new);

    /** O <b>que parte</b>: três de uma vez, num leque, por metade do dano cada. */
    public static final Item SPLITTING_BOLT = register("splitting_bolt", Item::new);

    /** E o de <b>prata</b>, a única coisa de longe que fere um lobisomem. */
    public static final Item SILVER_BOLT = register("silver_bolt", Item::new);

    /** As quatro peças lisas das roupas de caçador. */
    public static final Item HUNTER_HAT = hunter("hunter_hat", ArmorType.HELMET, false, false);
    public static final Item HUNTER_COAT = hunter("hunter_coat", ArmorType.CHESTPLATE, false, false);
    public static final Item HUNTER_LEGS = hunter("hunter_legs", ArmorType.LEGGINGS, false, false);
    public static final Item HUNTER_BOOTS = hunter("hunter_boots", ArmorType.BOOTS, false, false);

    /** As quatro <b>prateadas</b>, que ardem num lobisomem e lhe tiram a força da pancada. */
    public static final Item SILVERED_HUNTER_HAT =
            hunter("silvered_hunter_hat", ArmorType.HELMET, true, false);
    public static final Item SILVERED_HUNTER_COAT =
            hunter("silvered_hunter_coat", ArmorType.CHESTPLATE, true, false);
    public static final Item SILVERED_HUNTER_LEGS =
            hunter("silvered_hunter_legs", ArmorType.LEGGINGS, true, false);
    public static final Item SILVERED_HUNTER_BOOTS =
            hunter("silvered_hunter_boots", ArmorType.BOOTS, true, false);

    /**
     * E as quatro <b>da aurora</b>, que são prateadas <b>e</b> com alho: valem contra os dois.
     *
     * <p>É o {@code ItemHunterClothes(casa, true, true)} do original — o alho nunca vem sozinho, e por isso
     * quem tem a roupa da aurora tem a melhor que há.
     */
    public static final Item GARLICKED_HUNTER_HAT =
            hunter("garlicked_hunter_hat", ArmorType.HELMET, true, true);
    public static final Item GARLICKED_HUNTER_COAT =
            hunter("garlicked_hunter_coat", ArmorType.CHESTPLATE, true, true);
    public static final Item GARLICKED_HUNTER_LEGS =
            hunter("garlicked_hunter_legs", ArmorType.LEGGINGS, true, true);
    public static final Item GARLICKED_HUNTER_BOOTS =
            hunter("garlicked_hunter_boots", ArmorType.BOOTS, true, true);

    /** Uma peça de roupa de caçador: couro para proteger, ferro para durar, e tingível. */
    private static Item hunter(String nome, ArmorType casa, boolean prateada, boolean comAlho) {
        return register(nome, properties -> new net.thaumcraft.occulta.hunter.HunterClothesItem(properties
                .humanoidArmor(OccultaMaterials.HUNTER, casa)
                .rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                prateada, comAlho, corDe(casa)));
    }

    /** A cor de fábrica de cada peça, pelos números do original. */
    private static int corDe(ArmorType casa) {
        return switch (casa) {
            case LEGGINGS -> 0x49372B;
            case BOOTS -> 0x19110B;
            default -> 0x3F2A1E;
        };
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
