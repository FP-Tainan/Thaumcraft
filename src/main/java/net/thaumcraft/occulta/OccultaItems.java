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

    /** O Braseiro, em item. */
    public static final Item BRAZIER = register("brazier", properties ->
            new BlockItem(OccultaBlocks.BRAZIER, properties.useBlockDescriptionPrefix()));

    /** E o Crisol de Sangue. */
    public static final Item BLOOD_CRUCIBLE = register("blood_crucible", properties ->
            new BlockItem(OccultaBlocks.BLOOD_CRUCIBLE, properties.useBlockDescriptionPrefix()));

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
