package net.thaumcraft.occulta;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Os blocos do Ars Occulta: por agora, as oito plantas do ofício.
 *
 * <p>Quem faz o trabalho é o {@link WitchCropBlock}; aqui fica só o jeito de cada uma, que é o do
 * {@code WitcheryBlocks} do original.
 */
public final class OccultaBlocks {
    /** Quatro idades, em terra, com farinha de osso: o jeito da maioria. */
    private static final WitchCropBlock.Traits COMUM = new WitchCropBlock.Traits(4, false, true, false, false);
    /** O mesmo, mas na água: a alcachofra. */
    private static final WitchCropBlock.Traits NA_AGUA = new WitchCropBlock.Traits(4, true, true, false, false);
    /** O mesmo, mas empilhando-se quando feita: a losna. */
    private static final WitchCropBlock.Traits EMPILHA = new WitchCropBlock.Traits(4, false, true, false, true);
    /** Quatro idades, farinha de osso que só adianta uma e crescimento devagar: a mindrake. */
    private static final WitchCropBlock.Traits BULBO = new WitchCropBlock.Traits(4, false, false, true, false);
    /** Sete idades, e a farinha de osso só adianta uma: a acônito. */
    private static final WitchCropBlock.Traits ACONITO = new WitchCropBlock.Traits(7, false, false, false, false);
    /** Cinco idades: o alho. */
    private static final WitchCropBlock.Traits ALHO = new WitchCropBlock.Traits(5, false, true, false, false);

    /** A beladona, de que sai a flor. */
    public static final Block BELLADONNA = crop("belladonna", COMUM, () -> OccultaItems.BELLADONNA_SEEDS);
    /** A mandrágora, que só se deixa arrancar à noite. */
    public static final Block MANDRAKE = crop("mandrake", COMUM, () -> OccultaItems.MANDRAKE_SEEDS);
    /** A alcachofra-d'água, que nasce sobre a água. */
    public static final Block WATER_ARTICHOKE = crop("water_artichoke", NA_AGUA, () -> OccultaItems.WATER_ARTICHOKE_SEEDS);
    /** A campainha-de-neve, de que sai a bola de neve e, de vez em quando, a Agulha de Gelo. */
    public static final Block SNOWBELL = crop("snowbell", COMUM, () -> OccultaItems.SNOWBELL_SEEDS);
    /** A losna, que se empilha. */
    public static final Block WORMWOOD = crop("wormwood", EMPILHA, () -> OccultaItems.WORMWOOD_SEEDS);
    /** A mindrake, devagar, que dá bulbo. */
    public static final Block MINDRAKE = crop("mindrake", BULBO, () -> OccultaItems.MINDRAKE_BULB);
    /** A acônito, de sete idades. */
    public static final Block WOLFSBANE = crop("wolfsbane", ACONITO, () -> OccultaItems.WOLFSBANE_SEEDS);
    /** E o alho. */
    public static final Block GARLIC = crop("garlic", ALHO, () -> OccultaItems.GARLIC);

    // ------------------------------------------------------------------ o forno e os funis

    /** O Forno das Bruxas, onde o que se queima deixa o cheiro num pote. */
    public static final Block WITCHES_OVEN = register("witches_oven", properties ->
            new WitchesOvenBlock(properties.mapColor(MapColor.METAL).strength(3.5f)
                    .sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(WitchesOvenBlock.LIT) ? WitchesOvenBlock.LIGHT : 0)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<WitchesOvenBlockEntity> WITCHES_OVEN_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("witches_oven"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(WitchesOvenBlockEntity::new,
                            java.util.Set.of(WITCHES_OVEN)));

    /** O Funil de Fumos, que apressa o forno e melhora a sorte do cheiro. */
    public static final Block FUME_FUNNEL = register("fume_funnel", properties ->
            new FumeFunnelBlock(properties.mapColor(MapColor.METAL).strength(3.5f)
                    .sound(SoundType.METAL).noOcclusion(), false));

    /** E o mesmo com filtro, que melhora mais. */
    public static final Block FILTERED_FUME_FUNNEL = register("filtered_fume_funnel", properties ->
            new FumeFunnelBlock(properties.mapColor(MapColor.METAL).strength(3.5f)
                    .sound(SoundType.METAL).noOcclusion(), true));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<FumeFunnelBlockEntity> FUME_FUNNEL_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("fume_funnel"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(FumeFunnelBlockEntity::new,
                            java.util.Set.of(FUME_FUNNEL, FILTERED_FUME_FUNNEL)));

    // ------------------------------------------------------------------ as três árvores do ofício

    /** A sorveira, que tem afinidade com a magia. */
    public static final Block ROWAN_LOG = log("rowan_log");
    public static final Block ROWAN_LEAVES = leaves("rowan_leaves", () -> OccultaBlocks.ROWAN_SAPLING, true);
    public static final Block ROWAN_SAPLING = sapling("rowan_sapling",
            (level, pos) -> WitchTree.generate(level, level.getRandom(), pos, false));
    public static final Block ROWAN_PLANKS = planks("rowan_planks");
    public static final Block ROWAN_STAIRS = stairs("rowan_stairs", () -> ROWAN_PLANKS);
    public static final Block ROWAN_SLAB = slab("rowan_slab");

    /** O amieiro, cuja madeira parece sangrar e que traz má sorte. */
    public static final Block ALDER_LOG = log("alder_log");
    public static final Block ALDER_LEAVES = leaves("alder_leaves", () -> OccultaBlocks.ALDER_SAPLING, false);
    public static final Block ALDER_SAPLING = sapling("alder_sapling",
            (level, pos) -> LargeWitchTree.generate(level, level.getRandom(), pos, false, LargeWitchTree.alder()));
    public static final Block ALDER_PLANKS = planks("alder_planks");
    public static final Block ALDER_STAIRS = stairs("alder_stairs", () -> ALDER_PLANKS);
    public static final Block ALDER_SLAB = slab("alder_slab");

    /**
     * O <b>Gelo Perpétuo</b>: o {@code BlockPerpetualIce}.
     *
     * <p>Gelo que <b>não derrete</b>. É a única diferença entre ele e o gelo do mundo, e é a diferença
     * inteira: um bloco de gelo ao sol é um relógio, e este não é. Com ele se constrói — e por isso ele tem
     * escada, laje, cerca, portão, placa e porta, que o gelo comum nunca teve.
     *
     * <p>Ele é <b>duro</b> onde o gelo é mole: dois de dureza e cinco de resistência, contra meio do gelo
     * comum. E escorrega igual.
     */
    public static final Block PERPETUAL_ICE = register("perpetual_ice", properties ->
            new net.minecraft.world.level.block.HalfTransparentBlock(gelo(properties)));

    public static final Block ICE_STAIRS = register("ice_stairs", properties ->
            new net.minecraft.world.level.block.StairBlock(PERPETUAL_ICE.defaultBlockState(),
                    gelo(properties)));

    public static final Block ICE_SLAB = register("ice_slab", properties ->
            new net.minecraft.world.level.block.SlabBlock(gelo(properties)));

    public static final Block ICE_FENCE = register("ice_fence", properties ->
            new net.minecraft.world.level.block.FenceBlock(gelo(properties)));

    public static final Block ICE_FENCE_GATE = register("ice_fence_gate", properties ->
            new net.minecraft.world.level.block.FenceGateBlock(
                    net.minecraft.world.level.block.state.properties.WoodType.OAK, gelo(properties)));

    /** A placa de gelo, que é <b>mole</b>: dois décimos de dureza, como todas as placas. */
    public static final Block ICE_PRESSURE_PLATE = register("ice_pressure_plate", properties ->
            new net.minecraft.world.level.block.PressurePlateBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.STONE,
                    properties.mapColor(MapColor.ICE).strength(0.2f, 5.0f).sound(SoundType.GLASS)
                            .noCollision().noOcclusion().forceSolidOn()));

    /**
     * A <b>Porta de Gelo</b>.
     *
     * <p><b>Declarado:</b> no original ela é uma classe à parte por uma razão só — no jogo de 2014, o gelo
     * não contava como chão sólido, e uma porta comum não ficava de pé sobre ele. O {@code
     * BlockPerpetualIceDoor} existia para abrir essa exceção.
     *
     * <p>No jogo de hoje a conta é outra: o que decide é o <b>feitio</b> do que está por baixo, e o gelo
     * perpétuo é um cubo inteiro. <b>Qualquer</b> porta já fica de pé sobre ele, e a exceção deixou de ter
     * o que fazer. Fica como porta comum.
     */
    public static final Block ICE_DOOR = register("ice_door", properties ->
            new net.minecraft.world.level.block.DoorBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.STONE, gelo(properties)
                    .noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** E a neve, que ganha escada, laje e placa pelo mesmo motivo: para dar para construir com ela. */
    public static final Block SNOW_STAIRS = register("snow_stairs", properties ->
            new net.minecraft.world.level.block.StairBlock(
                    net.minecraft.world.level.block.Blocks.SNOW_BLOCK.defaultBlockState(), neve(properties)));

    public static final Block SNOW_SLAB = register("snow_slab", properties ->
            new net.minecraft.world.level.block.SlabBlock(neve(properties)));

    public static final Block SNOW_PRESSURE_PLATE = register("snow_pressure_plate", properties ->
            new net.minecraft.world.level.block.PressurePlateBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.STONE,
                    properties.mapColor(MapColor.SNOW).strength(0.2f, 0.2f).sound(SoundType.SNOW)
                            .noCollision().forceSolidOn()));

    /** O <b>Baú de Sanguessugas</b>: o baú que anota o nome de quem o abre. */
    public static final Block LEECH_CHEST = register("leech_chest", properties ->
            new net.thaumcraft.occulta.LeechChestBlock(properties.mapColor(MapColor.CRIMSON_HYPHAE)
                    .strength(2.5f).sound(SoundType.WOOD).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.LeechChestBlockEntity> LEECH_CHEST_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("leech_chest"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.LeechChestBlockEntity::new,
                            java.util.Set.of(LEECH_CHEST)));

    /** O <b>Apanha-Erva</b>: a planta que segura o que lhe dão, e que as mutações pedem. */
    public static final Block GRASSPER = register("grassper", properties ->
            new net.thaumcraft.occulta.GrassperBlock(properties.mapColor(MapColor.PLANT)
                    .instabreak().sound(SoundType.GRASS).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.GrassperBlockEntity> GRASSPER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("grassper"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.GrassperBlockEntity::new,
                            java.util.Set.of(GRASSPER)));

    // ------------------------------------------------------------ a caveira e o ovo

    /**
     * A <b>Caveira do Chamado</b>: a que puxa os mortos-vivos para si.
     *
     * <p>Os números do original: <b>inquebrável</b>, mil de resistência a explosão, som de pedra e
     * <b>sete de luz</b>. Um creeper ao lado dela não a tira do lugar.
     */
    public static final Block ALLURING_SKULL = register("alluring_skull", properties ->
            new AlluringSkullBlock(properties.mapColor(MapColor.SAND).strength(-1.0f, 1000.0f)
                    .sound(SoundType.STONE).lightLevel(feitio -> 7).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<AlluringSkullBlockEntity>
            ALLURING_SKULL_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("alluring_skull"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            AlluringSkullBlockEntity::new, java.util.Set.of(ALLURING_SKULL)));

    /**
     * O <b>Ovo do Infinito</b>: o Ovo de Dragão que não foge.
     *
     * <p>Os números do original: <b>três de dureza</b>, quinze de resistência, som de pedra e <b>dois de
     * luz</b>.
     */
    public static final Block INFINITY_EGG = register("infinity_egg", properties ->
            new InfinityEggBlock(properties.mapColor(MapColor.COLOR_BLACK).strength(3.0f, 15.0f)
                    .sound(SoundType.STONE).lightLevel(feitio -> 2).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    // ------------------------------------------------------------ o Item Posto

    /**
     * O <b>Item Posto</b>: um bloco que não é nada e que guarda uma coisa deitada no chão.
     *
     * <p>Os números do original: <b>dureza zero</b> — parte-se com um sopro — e som de metal. Ele não se
     * fabrica, não aparece em aba nenhuma e não tem item próprio: quem o põe é a Arthana.
     */
    public static final Block PLACED_ITEM = register("placed_item", properties ->
            new PlacedItemBlock(properties.mapColor(MapColor.NONE).strength(0.0f)
                    .sound(SoundType.METAL).noOcclusion().noCollision()
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<PlacedItemBlockEntity>
            PLACED_ITEM_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("placed_item"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            PlacedItemBlockEntity::new, java.util.Set.of(PLACED_ITEM)));

    // ------------------------------------------------------------ a Bola de Cristal

    /**
     * A <b>Bola de Cristal</b>, que lê a sorte de quem estiver perto.
     *
     * <p>Os números do original: <b>dois de dureza</b> e som de metal. Ela não dá luz.
     */
    public static final Block CRYSTAL_BALL = register("crystal_ball", properties ->
            new CrystalBallBlock(properties.mapColor(MapColor.QUARTZ).strength(2.0f)
                    .sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<CrystalBallBlockEntity>
            CRYSTAL_BALL_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("crystal_ball"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            CrystalBallBlockEntity::new, java.util.Set.of(CRYSTAL_BALL)));

    // ------------------------------------------------------------ o candelabro e o cálice

    /**
     * O <b>Candelabro</b>: cinco velas que ardem sempre, e dois de velocidade no altar.
     *
     * <p>Os números do original: <b>dois de dureza</b>, som de metal e <b>luz cheia</b>.
     */
    public static final Block CANDELABRA = register("candelabra", properties ->
            new CandelabraBlock(properties.mapColor(MapColor.METAL).strength(2.0f)
                    .sound(SoundType.METAL).lightLevel(feitio -> 15).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<CandelabraBlockEntity>
            CANDELABRA_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("candelabra"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            CandelabraBlockEntity::new, java.util.Set.of(CANDELABRA)));

    /**
     * O <b>Cálice</b>: uma taça de ouro que vale mais cheia do que vazia.
     *
     * <p>Os números do original: <b>três de dureza</b> e som de metal. Ele não dá luz.
     */
    public static final Block CHALICE = register("chalice", properties ->
            new ChaliceBlock(properties.mapColor(MapColor.GOLD).strength(3.0f)
                    .sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<ChaliceBlockEntity>
            CHALICE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("chalice"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            ChaliceBlockEntity::new, java.util.Set.of(CHALICE)));

    // ------------------------------------------------------------ o Feixe de Vime

    /**
     * O <b>Feixe de Vime</b>: a peça de que se constrói o Homem de Vime.
     *
     * <p>Os números do original: <b>meio de dureza</b>, som de grama, e pega fogo como madeira — vinte de
     * chama e vinte de espalhar, que é mais do que qualquer tronco.
     */
    public static final Block WICKER_BUNDLE = register("wicker_bundle", properties ->
            new net.thaumcraft.occulta.WickerBundleBlock(properties.mapColor(MapColor.WOOD)
                    .strength(0.5f).sound(SoundType.GRASS).ignitedByLava()));

    // ------------------------------------------------------------ o vidro, a lã e o globo

    /**
     * O <b>Vidro Sombreado</b>: o vidro tingido que a redstone fecha, e com ele a luz.
     *
     * <p>Os números do original: <b>três décimos de dureza</b>, que é a do vidro.
     */
    public static final Block SHADED_GLASS = register("shaded_glass", properties ->
            new net.thaumcraft.occulta.ShadedGlassBlock(properties.mapColor(MapColor.NONE)
                    .strength(0.3f).sound(SoundType.GLASS).noOcclusion()
                    .isValidSpawn((f, l, p, t) -> false).isRedstoneConductor((f, l, p) -> false)
                    .isSuffocating((f, l, p) -> false).isViewBlocking((f, l, p) -> false)));

    /** A <b>Lã Ensanguentada</b>: a lã branca que um vampiro tingiu com o próprio sangue. */
    public static final Block BLOODED_WOOL = register("blooded_wool", properties ->
            new Block(properties.mapColor(MapColor.COLOR_RED).strength(0.8f)
                    .sound(SoundType.WOOL).ignitedByLava()));

    /**
     * O <b>Globo de Luz</b>: a bolinha que o símbolo da luz deixa onde cai.
     *
     * <p>Dureza zero, acende quinze, e <b>não tem item</b> — não se apanha, não cai de nada e não está na
     * aba do criativo, como no original.
     */
    public static final Block GLOW_GLOBE = register("glow_globe", properties ->
            new net.thaumcraft.occulta.GlowGlobeBlock(properties.mapColor(MapColor.NONE)
                    .instabreak().noCollision().noOcclusion().pushReaction(PushReaction.DESTROY)
                    .lightLevel(f -> net.thaumcraft.occulta.GlowGlobeBlock.ACENDE)));

    // ------------------------------------------------------------ a Paliçada

    /**
     * A <b>Paliçada</b>, nas nove madeiras: o bloco que fere quem encosta.
     *
     * <p>Os números do original: <b>vinte e cinco de dureza</b> e vinte de resistência. Ela demora a cair.
     */
    public static final Block STOCKADE = register("stockade", properties ->
            new net.thaumcraft.occulta.StockadeBlock.Wooden(properties.mapColor(MapColor.WOOD)
                    .strength(25.0f, 20.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava()));

    /** E a <b>de gelo</b>, que é bloco à parte e não se liga às de madeira. */
    public static final Block ICE_STOCKADE = register("ice_stockade", properties ->
            new net.thaumcraft.occulta.StockadeBlock(properties.mapColor(MapColor.ICE)
                    .strength(25.0f, 20.0f).friction(0.98f).sound(SoundType.GLASS).noOcclusion()));

    // ------------------------------------------------------------ a Mina de Planta

    /**
     * A <b>Mina de Planta</b>: a flor que não é uma flor.
     *
     * <p>Os números do original: <b>seis de dureza</b> e <b>mil de resistência</b> — mais do que a obsidiana.
     * Não se abre caminho num campo de minas com TNT.
     */
    public static final Block PLANT_MINE = register("plant_mine", properties ->
            new net.thaumcraft.occulta.PlantMineBlock(properties.mapColor(MapColor.PLANT)
                    .strength(6.0f, 1000.0f).sound(SoundType.GRASS).noOcclusion().noCollision()
                    .pushReaction(PushReaction.DESTROY)));

    // ------------------------------------------------------------ o Apanha-Bicho

    /**
     * O <b>Apanha-Bicho</b>: a planta que engole o que é pequeno.
     *
     * <p>Ela é uma planta de verdade — quebra-se à mão, não estorva a passagem, e vai-se embora se o chão
     * debaixo dela sair.
     */
    public static final Block CRITTER_SNARE = register("critter_snare", properties ->
            new net.thaumcraft.occulta.CritterSnareBlock(properties.mapColor(MapColor.PLANT)
                    .instabreak().sound(SoundType.GRASS).noOcclusion().noCollision()
                    .pushReaction(PushReaction.DESTROY)));

    // ------------------------------------------------------------ as duas armadilhas

    /**
     * A <b>Armadilha de Urso</b>: rasa, sem colisão, e invisível para quem não a pôs.
     *
     * <p>Os números do original: <b>cinco de dureza</b> e <b>dez de resistência</b>, que é ferro.
     */
    public static final Block BEARTRAP = register("beartrap", properties ->
            new net.thaumcraft.occulta.trap.BeartrapBlock(properties.mapColor(MapColor.METAL)
                    .strength(5.0f, 10.0f).sound(SoundType.METAL).noOcclusion().noCollision()));

    /** E a <b>Armadilha de Prata</b>, que é a mesma com uma chave virada: só apanha o lobisomem dela. */
    public static final Block WOLFTRAP = register("wolftrap", properties ->
            new net.thaumcraft.occulta.trap.BeartrapBlock(properties.mapColor(MapColor.METAL)
                    .strength(5.0f, 10.0f).sound(SoundType.METAL).noOcclusion().noCollision()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.trap.BeartrapBlockEntity> BEARTRAP_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("beartrap"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.trap.BeartrapBlockEntity::new,
                            java.util.Set.of(BEARTRAP, WOLFTRAP)));

    // ------------------------------------------------------------ as sarças e o nenúfar

    /** A <b>Sarça Selvagem</b>, que espinha quem passa — e que cortada se espalha. */
    public static final Block WILD_BRAMBLE = register("wild_bramble", properties ->
            new net.thaumcraft.occulta.BrambleBlock(sarça(properties), false));

    /** A <b>Sarça do Fim</b>, que manda quem passa para quinhentos blocos de distância. */
    public static final Block ENDER_BRAMBLE = register("ender_bramble", properties ->
            new net.thaumcraft.occulta.BrambleBlock(sarça(properties), true));

    /** E a <b>Sarça do Vazio</b>, à volta da qual círculo nenhum acende. */
    public static final Block VOID_BRAMBLE = register("void_bramble", properties ->
            new net.thaumcraft.occulta.VoidBrambleBlock(sarça(properties).lightLevel(feitio -> 2)));

    /** O <b>Lírio-Saltador</b>: um nenúfar que brilha e dá salto a quem lhe pisa. */
    public static final Block LEAPING_LILY = register("leaping_lily", properties ->
            new net.thaumcraft.occulta.LeapingLilyBlock(properties.mapColor(MapColor.PLANT)
                    .instabreak().sound(SoundType.LILY_PAD).noOcclusion()
                    .lightLevel(feitio -> 6).pushReaction(PushReaction.DESTROY)));

    // ------------------------------------------------------------ as gêmeas amaldiçoadas
    /*
     * Nenhuma delas tem item, receita ou lugar no criativo: ninguém as põe no mundo. Elas acontecem a uma
     * peça que já lá estava, quando um frasco de gatilho bate nela.
     */

    public static final Block CURSED_STONE_BUTTON = register("cursed_stone_button", properties ->
            new net.thaumcraft.occulta.curse.CursedTwins.CursedButton(BlockSetType.STONE, 20,
                    () -> Blocks.STONE_BUTTON, properties.mapColor(MapColor.NONE).noCollision()
                    .strength(0.5f).pushReaction(PushReaction.DESTROY)));

    public static final Block CURSED_WOODEN_BUTTON = register("cursed_wooden_button", properties ->
            new net.thaumcraft.occulta.curse.CursedTwins.CursedButton(BlockSetType.OAK, 30,
                    () -> Blocks.OAK_BUTTON, properties.mapColor(MapColor.NONE).noCollision()
                    .strength(0.5f).pushReaction(PushReaction.DESTROY).ignitedByLava()));

    public static final Block CURSED_LEVER = register("cursed_lever", properties ->
            new net.thaumcraft.occulta.curse.CursedTwins.CursedLever(
                    () -> Blocks.LEVER, properties.mapColor(MapColor.NONE).noCollision()
                    .strength(0.5f).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)));

    public static final Block CURSED_WOODEN_DOOR = register("cursed_wooden_door", properties ->
            new net.thaumcraft.occulta.curse.CursedTwins.CursedDoor(BlockSetType.OAK,
                    () -> Blocks.OAK_DOOR, properties.mapColor(MapColor.WOOD).strength(3.0f)
                    .noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));

    public static final Block CURSED_WOODEN_PRESSURE_PLATE = register("cursed_wooden_pressure_plate",
            properties -> new net.thaumcraft.occulta.curse.CursedTwins.CursedPlate(BlockSetType.OAK,
                    () -> Blocks.OAK_PRESSURE_PLATE, properties.mapColor(MapColor.WOOD).forceSolidOn()
                    .noCollision().strength(0.5f).ignitedByLava().pushReaction(PushReaction.DESTROY)));

    public static final Block CURSED_STONE_PRESSURE_PLATE = register("cursed_stone_pressure_plate",
            properties -> new net.thaumcraft.occulta.curse.CursedTwins.CursedPlate(BlockSetType.STONE,
                    () -> Blocks.STONE_PRESSURE_PLATE, properties.mapColor(MapColor.STONE).forceSolidOn()
                    .requiresCorrectToolForDrops().noCollision().strength(0.5f)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block CURSED_SNOW_PRESSURE_PLATE = register("cursed_snow_pressure_plate",
            properties -> new net.thaumcraft.occulta.curse.CursedTwins.CursedPlate(BlockSetType.STONE,
                    () -> OccultaBlocks.SNOW_PRESSURE_PLATE, properties.mapColor(MapColor.SNOW)
                    .forceSolidOn().noCollision().strength(0.2f).sound(SoundType.SNOW)
                    .pushReaction(PushReaction.DESTROY)));

    /** E a alma que todas elas carregam: o cozimento preso à espera. */
    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.curse.CursedBlockEntity> CURSED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("cursed_block"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.curse.CursedBlockEntity::new,
                            java.util.Set.of(CURSED_STONE_BUTTON, CURSED_WOODEN_BUTTON, CURSED_LEVER,
                                    CURSED_WOODEN_DOOR, CURSED_WOODEN_PRESSURE_PLATE,
                                    CURSED_STONE_PRESSURE_PLATE, CURSED_SNOW_PRESSURE_PLATE)));

    /**
     * A <b>Porta de Amieiro</b>, que é uma porta e mais nada — e é de propósito: ela existe para que a de
     * sorveira não seja a única porta do ofício, e para que escolher a trancada seja uma escolha.
     */
    public static final Block ALDER_DOOR = register("alder_door", properties ->
            new net.minecraft.world.level.block.DoorBlock(
                    net.minecraft.world.level.block.state.properties.BlockSetType.OAK, properties
                    .mapColor(MapColor.WOOD).strength(3.0f, 3.0f).sound(SoundType.WOOD)
                    .noOcclusion().ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** E a <b>Porta de Sorveira</b>, que só abre para quem tem a chave que nasceu com ela. */
    public static final Block ROWAN_DOOR = register("rowan_door", properties ->
            new net.thaumcraft.occulta.door.RowanDoorBlock(properties
                    .mapColor(MapColor.WOOD).strength(5.0f, 5.0f).sound(SoundType.WOOD)
                    .noOcclusion().ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** E o espinheiro-alvar, a árvore da pureza, que quase não pega fogo. */
    public static final Block HAWTHORN_LOG = log("hawthorn_log");
    public static final Block HAWTHORN_LEAVES = leaves("hawthorn_leaves", () -> OccultaBlocks.HAWTHORN_SAPLING, false);
    public static final Block HAWTHORN_SAPLING = sapling("hawthorn_sapling",
            (level, pos) -> LargeWitchTree.generate(level, level.getRandom(), pos, false, LargeWitchTree.hawthorn()));
    public static final Block HAWTHORN_PLANKS = planks("hawthorn_planks");
    public static final Block HAWTHORN_STAIRS = stairs("hawthorn_stairs", () -> HAWTHORN_PLANKS);
    public static final Block HAWTHORN_SLAB = slab("hawthorn_slab");

    // ------------------------------------------------------------------ o caldeirão

    /** O Caldeirão da Bruxa, onde se ferve o que o ofício pede. */
    public static final Block WITCHES_CAULDRON = register("witches_cauldron", properties ->
            new WitchesCauldronBlock(properties.mapColor(MapColor.METAL).strength(2.0f)
                    .sound(SoundType.METAL).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<WitchesCauldronBlockEntity> WITCHES_CAULDRON_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("witches_cauldron"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(WitchesCauldronBlockEntity::new,
                            java.util.Set.of(WITCHES_CAULDRON)));

    // ------------------------------------------------------------------ as bonecas

    /** A Prateleira de Bonecas, onde as bonecas valem de longe. */
    public static final Block POPPET_SHELF = register("poppet_shelf", properties ->
            new PoppetShelfBlock(properties.mapColor(MapColor.WOOD).strength(2.0f)
                    .sound(SoundType.WOOD).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<PoppetShelfBlockEntity> POPPET_SHELF_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("poppet_shelf"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(PoppetShelfBlockEntity::new,
                            java.util.Set.of(POPPET_SHELF)));

    /** O Musgo Espanhol, que pende das árvores do pântano. */
    public static final Block SPANISH_MOSS = register("spanish_moss", properties ->
            new net.minecraft.world.level.block.BushBlock(properties.mapColor(MapColor.PLANT)
                    .instabreak().sound(SoundType.GRASS).ignitedByLava()));

    /** E o Musgo de Brasa, que arde em quem lhe pisa. */
    public static final Block EMBER_MOSS = register("ember_moss", properties ->
            new EmberMossBlock(properties.mapColor(MapColor.COLOR_ORANGE)
                    .instabreak().lightLevel(state -> 6).sound(SoundType.GRASS)));

    // ------------------------------------------------------------------ os círculos de giz

    /** O glifo do meio, que o giz dourado risca: é nele que se bate para começar um rito. */
    public static final Block CIRCLE_HEART = register("circle_heart", properties ->
            new CircleHeartBlock(properties.mapColor(MapColor.SAND).strength(2.0f, 1000.0f)
                    .sound(SoundType.WOOL).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<CircleHeartBlockEntity> CIRCLE_HEART_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("circle_heart"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(CircleHeartBlockEntity::new,
                            java.util.Set.of(CIRCLE_HEART)));

    /** Os três glifos de anel, um por giz. */
    public static final Block RITUAL_GLYPH = glyph("ritual_glyph");
    public static final Block OTHERWHERE_GLYPH = glyph("otherwhere_glyph");
    public static final Block INFERNAL_GLYPH = glyph("infernal_glyph");

    private static Block glyph(String name) {
        return register(name, properties -> new GlyphBlock(properties.mapColor(MapColor.SAND)
                .strength(2.0f, 1000.0f).sound(SoundType.WOOL).noOcclusion()));
    }

    // ------------------------------------------------------------------ a destilaria

    /** A Destilaria, que separa uma coisa em quatro com o poder do altar. */
    public static final Block DISTILLERY = register("distillery", properties ->
            new DistilleryBlock(properties.mapColor(MapColor.STONE).strength(3.5f)
                    .sound(SoundType.STONE).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<DistilleryBlockEntity> DISTILLERY_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("distillery"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(DistilleryBlockEntity::new,
                            java.util.Set.of(DISTILLERY)));

    // ------------------------------------------------------------------ a nuvem de cozimento

    /** A nuvem que um frasco de gás deixa no chão: não se apanha, não se pisa, e some sozinha. */
    public static final Block BREW_GAS = register("brew_gas", properties ->
            new net.thaumcraft.occulta.brew.BrewGasBlock(properties.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .replaceable().noLootTable().strength(100.0f).noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<net.thaumcraft.occulta.brew.BrewFluidBlockEntity> BREW_GAS_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("brew_gas"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.brew.BrewFluidBlockEntity::new, java.util.Set.of(BREW_GAS)));

    // ------------------------------------------------------------------ o que é de sonho

    /** O Algodão Sonhador, que só nasce do outro lado. */
    public static final Block WISPY_COTTON = register("wispy_cotton", properties ->
            new net.thaumcraft.occulta.spirit.DreamPlantBlock(properties.mapColor(MapColor.SNOW)
                    .noCollision().instabreak().randomTicks().sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY), true));

    /** E a Erva Cintilante, que alumia o outro lado. */
    public static final Block GLINT_WEED = register("glint_weed", properties ->
            new net.thaumcraft.occulta.spirit.DreamPlantBlock(properties.mapColor(MapColor.COLOR_YELLOW)
                    .noCollision().instabreak().randomTicks().lightLevel(state -> 15)
                    .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY), false));

    /** O Apanhador de Sonhos, que diz o que se sonha a quem dorme perto dele. */
    public static final Block DREAM_CATCHER = register("dream_catcher", properties ->
            new net.thaumcraft.occulta.spirit.DreamCatcherBlock(properties.mapColor(MapColor.WOOD)
                    .instabreak().sound(SoundType.WOOL).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.spirit.DreamCatcherBlockEntity> DREAM_CATCHER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("dream_catcher"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.spirit.DreamCatcherBlockEntity::new,
                            java.util.Set.of(DREAM_CATCHER)));

    /** A casa de barreira, que os ritos de proteção põem e que some sozinha. */
    public static final Block BARRIER = register("barrier", properties ->
            new net.thaumcraft.occulta.BarrierBlock(properties.mapColor(MapColor.NONE)
                    .strength(-1.0f, 1000.0f).noLootTable().noOcclusion().isValidSpawn((a, b, c, d) -> false)
                    .sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.BarrierBlockEntity> BARRIER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("barrier"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.BarrierBlockEntity::new, java.util.Set.of(BARRIER)));

    // ------------------------------------------------------------------ os dois líquidos do outro lado

    /** O Espírito Fluente, que se atira e faz poça. */
    public static final Block FLOWING_SPIRIT = register("flowing_spirit", properties ->
            net.thaumcraft.occulta.spirit.SpiritLiquidBlock.spirit(properties.mapColor(MapColor.COLOR_CYAN)
                    .replaceable().noCollision().strength(100.0f).pushReaction(PushReaction.DESTROY)
                    .noLootTable().liquid().sound(SoundType.EMPTY)));

    /** E as Lágrimas Ocas, que a Destilaria tira dele. */
    public static final Block HOLLOW_TEARS = register("hollow_tears", properties ->
            net.thaumcraft.occulta.spirit.SpiritLiquidBlock.tears(properties.mapColor(MapColor.COLOR_GRAY)
                    .replaceable().noCollision().strength(100.0f).pushReaction(PushReaction.DESTROY)
                    .noLootTable().liquid().sound(SoundType.EMPTY)));

    /** O Portal do Espírito, que se acende com Espírito Fluente numa moldura de neve. */
    public static final Block SPIRIT_PORTAL = register("spirit_portal", properties ->
            new net.thaumcraft.occulta.spirit.SpiritPortalBlock(properties.mapColor(MapColor.COLOR_CYAN)
                    .noCollision().noLootTable().strength(-1.0f).lightLevel(state -> 12)
                    .sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK)));

    // ------------------------------------------------------------------ a roca, o braseiro e o crisol

    /** A Roca, que fia o que não se fia à mão. */
    public static final Block SPINNING_WHEEL = register("spinning_wheel", properties ->
            new net.thaumcraft.occulta.spinning.SpinningWheelBlock(properties.mapColor(MapColor.WOOD)
                    .strength(3.5f).sound(SoundType.WOOD).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.spinning.SpinningWheelBlockEntity> SPINNING_WHEEL_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("spinning_wheel"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.spinning.SpinningWheelBlockEntity::new,
                            java.util.Set.of(SPINNING_WHEEL)));

    /**
     * A <b>Cabeça de Lobo</b>, que cai de um lobo morto e é o que a Estátua do Lobisomem pede.
     *
     * <p>Dois blocos, como o crânio do jogo: um no chão e outro na parede.
     */
    public static final Block WOLF_HEAD = register("mounted_wolf_head", properties ->
            new net.thaumcraft.occulta.wolf.WolfHeadBlock(properties.mapColor(MapColor.WOOL)
                    .strength(1.0f).sound(SoundType.WOOL).noOcclusion()));

    public static final Block WOLF_HEAD_WALL = register("mounted_wolf_head_wall", properties ->
            new net.thaumcraft.occulta.wolf.WolfHeadWallBlock(properties.mapColor(MapColor.WOOL)
                    .strength(1.0f).sound(SoundType.WOOL).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.wolf.WolfHeadBlockEntity> WOLF_HEAD_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("mounted_wolf_head"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.wolf.WolfHeadBlockEntity::new,
                            java.util.Set.of(WOLF_HEAD, WOLF_HEAD_WALL)));

    /**
     * A <b>Estátua do Lobisomem</b>, que é quem dá os dez graus.
     *
     * <p>Pedra que <b>aguenta mil de estouro</b>: não se tira um lugar de culto com um creeper.
     */
    public static final Block WEREWOLF_STATUE = register("werewolf_statue", properties ->
            new net.thaumcraft.occulta.wolf.WerewolfStatueBlock(properties.mapColor(MapColor.STONE)
                    .strength(2.5f, 1000.0f).sound(SoundType.STONE).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.wolf.WerewolfStatueBlockEntity> WEREWOLF_STATUE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("werewolf_statue"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.wolf.WerewolfStatueBlockEntity::new,
                            java.util.Set.of(WEREWOLF_STATUE)));

    /** O Braseiro, em que o que arde vale para quem está em volta. */
    public static final Block BRAZIER = register("brazier", properties ->
            new net.thaumcraft.occulta.brazier.BrazierBlock(properties.mapColor(MapColor.METAL)
                    .strength(3.5f).sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> 0)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.brazier.BrazierBlockEntity> BRAZIER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("brazier"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.brazier.BrazierBlockEntity::new,
                            java.util.Set.of(BRAZIER)));

    /** E o Crisol de Sangue, que espera o vampiro. */
    public static final Block BLOOD_CRUCIBLE = register("blood_crucible", properties ->
            new BloodCrucibleBlock(properties.mapColor(MapColor.STONE)
                    .strength(2.5f, 1000.0f).sound(SoundType.STONE).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<BloodCrucibleBlockEntity>
            BLOOD_CRUCIBLE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("blood_crucible"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            BloodCrucibleBlockEntity::new, java.util.Set.of(BLOOD_CRUCIBLE)));

    /**
     * O <b>Coletor de Luz</b>: a garra de ferro que enche uma Esfera de Quartzo de sol, de um em um,
     * ao ritmo dos Sensores de Luz Solar encostados a ela.
     */
    public static final Block DAYLIGHT_COLLECTOR = register("daylight_collector", properties ->
            new net.thaumcraft.occulta.vampire.DaylightCollectorBlock(properties.mapColor(MapColor.METAL)
                    .strength(3.5f).sound(SoundType.METAL).noOcclusion()));

    /** O <b>Caixão</b>: uma cama com tampa, e a casa de um vampiro. */
    public static final Block COFFIN = register("coffin", properties ->
            new net.thaumcraft.occulta.vampire.CoffinBlock(properties.mapColor(MapColor.WOOD)
                    .strength(1.0f).sound(SoundType.WOOD).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.vampire.CoffinBlockEntity> COFFIN_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("coffin"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.vampire.CoffinBlockEntity::new,
                            java.util.Set.of(COFFIN)));

    /**
     * A <b>Rosa de Sangue</b>, que se lembra de quem pisou nela.
     *
     * <p>Não se colhe com a mão: só a Boline a tira do chão, e tira-a com o que ela guarda dentro.
     */
    public static final Block BLOOD_ROSE = register("blood_rose", properties ->
            new BloodRoseBlock(properties.mapColor(MapColor.COLOR_RED)
                    .noCollision().instabreak().noLootTable().sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<BloodRoseBlockEntity>
            BLOOD_ROSE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("blood_rose"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            BloodRoseBlockEntity::new, java.util.Set.of(BLOOD_ROSE)));

    /** E a <b>Guirlanda de Alho</b>, que cospe vampiros para fora e queima quem a quiser arrancar. */
    public static final Block GARLIC_GARLAND = register("garlic_garland", properties ->
            new GarlicGarlandBlock(properties.mapColor(MapColor.TERRACOTTA_WHITE)
                    .strength(0.2f).sound(SoundType.GRASS).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<GarlicGarlandBlockEntity>
            GARLIC_GARLAND_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("garlic_garland"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            GarlicGarlandBlockEntity::new, java.util.Set.of(GARLIC_GARLAND)));

    /**
     * O <b>Coração de Demônio</b> posto no chão, que bate de vinte e cinco em vinte e cinco batidas.
     *
     * <p>É a fonte de poder mais forte que um Altar pode ter — quarenta cada, até dois.
     */
    public static final Block DEMON_HEART = register("demon_heart", properties ->
            new net.thaumcraft.occulta.demon.DemonHeartBlock(properties.mapColor(MapColor.COLOR_RED)
                    .strength(1.0f).sound(SoundType.SLIME_BLOCK).noOcclusion()
                    .lightLevel(state -> net.thaumcraft.occulta.demon.DemonHeartBlock.LUZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.demon.DemonHeartBlockEntity> DEMON_HEART_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("demon_heart"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.demon.DemonHeartBlockEntity::new,
                            java.util.Set.of(DEMON_HEART)));

    // ------------------------------------------------------------------ o marcador da muralha

    /**
     * O marcador invisível que levanta a muralha da aldeia e depois se apaga.
     *
     * <p>Não se vê, não se pega e não se fabrica: ele só nasce com a aldeia.
     */
    public static final Block VILLAGE_WALL_GEN = register("village_wall_gen", properties ->
            new net.thaumcraft.occulta.village.VillageWallGenBlock(
                    properties.strength(-1.0f, 3600000.0f).noOcclusion().noLootTable()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.village.VillageWallGenBlockEntity> VILLAGE_WALL_GEN_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("village_wall_gen"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.village.VillageWallGenBlockEntity::new,
                            java.util.Set.of(VILLAGE_WALL_GEN)));

    // ------------------------------------------------------------------ o caldeirão de pote

    /** O Caldeirão de Pote, pendurado nas correntes, onde se fazem os cozimentos de frasco. */
    public static final Block WITCHES_KETTLE = register("witches_kettle", properties ->
            new net.thaumcraft.occulta.kettle.KettleBlock(properties.mapColor(MapColor.METAL)
                    .strength(2.0f).sound(SoundType.METAL).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.kettle.KettleBlockEntity> WITCHES_KETTLE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("witches_kettle"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.kettle.KettleBlockEntity::new,
                            java.util.Set.of(WITCHES_KETTLE)));

    // ------------------------------------------------------------------ os espelhos

    /** O Espelho, que se prega na parede e leva ao Mundo do Espelho. */
    public static final Block WITCH_MIRROR = register("witch_mirror", properties ->
            new net.thaumcraft.occulta.mirror.MirrorBlock(properties.mapColor(MapColor.QUARTZ)
                    .strength(1.0f, 9999.0f).sound(SoundType.GLASS).lightLevel(state -> 10)
                    .noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK), false));

    /** E o selado, que é o da cela e não se quebra. */
    public static final Block SEALED_WITCH_MIRROR = register("sealed_witch_mirror", properties ->
            new net.thaumcraft.occulta.mirror.MirrorBlock(properties.mapColor(MapColor.QUARTZ)
                    .strength(-1.0f, 3600000.0f).sound(SoundType.GLASS).lightLevel(state -> 10)
                    .noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK), true));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<
            net.thaumcraft.occulta.mirror.MirrorBlockEntity> WITCH_MIRROR_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("witch_mirror"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(
                            net.thaumcraft.occulta.mirror.MirrorBlockEntity::new,
                            java.util.Set.of(WITCH_MIRROR, SEALED_WITCH_MIRROR)));

    /** A superfície de espelho, que forra as celas do Mundo do Espelho. */
    public static final Block MIRROR_WALL = register("mirror_wall", properties ->
            new net.thaumcraft.occulta.mirror.MirrorWallBlock(properties.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(-1.0f, 3600000.0f).sound(SoundType.GLASS).noLootTable()));

    // ------------------------------------------------------------------ o altar

    /** O Altar da Bruxa: seis deles, dois por três, fazem um altar de verdade. */
    public static final Block WITCH_ALTAR = register("witch_altar", properties ->
            new AltarBlock(properties.mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<AltarBlockEntity> WITCH_ALTAR_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("witch_altar"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(AltarBlockEntity::new,
                            java.util.Set.of(WITCH_ALTAR)));

    private OccultaBlocks() {
    }

    private static Block crop(String name, WitchCropBlock.Traits traits, Supplier<ItemLike> seed) {
        return register(name, properties -> new WitchCropBlock(properties
                .mapColor(MapColor.PLANT)
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY), traits, seed));
    }

    /** Uma tora: dureza dois, como a madeira do jogo — e o Ent que às vezes acorda com o machado. */
    private static Block log(String name) {
        return register(name, properties -> new WitchLogBlock(properties
                .mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD).ignitedByLava()));
    }

    /** Uma folhagem, com a muda que ela larga e se dá bagas. */
    private static Block leaves(String name, Supplier<ItemLike> sapling, boolean berries) {
        return register(name, properties -> new WitchLeavesBlock(properties
                .mapColor(MapColor.PLANT).strength(0.2f).randomTicks().sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(net.minecraft.world.level.block.Blocks::ocelotOrParrot)
                .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)
                .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false),
                sapling, berries));
    }

    /** Uma muda, com a árvore que ela faz. */
    private static Block sapling(String name,
                                 java.util.function.BiFunction<net.minecraft.server.level.ServerLevel,
                                         net.minecraft.core.BlockPos, Boolean> tree) {
        return register(name, properties -> new WitchSaplingBlock(properties
                .mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak()
                .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY), tree));
    }

    private static Block planks(String name) {
        return register(name, properties -> new Block(properties
                .mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    }

    private static Block stairs(String name, Supplier<Block> planks) {
        return register(name, properties -> new net.minecraft.world.level.block.StairBlock(
                planks.get().defaultBlockState(), properties
                .mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    }

    private static Block slab(String name) {
        return register(name, properties -> new net.minecraft.world.level.block.SlabBlock(properties
                .mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava()));
    }

    /**
     * Os números das sarças: <b>vinte de dureza</b>, que é a da obsidiana.
     *
     * <p>Não se atravessa uma sarça com pressa, e é de propósito: o tempo que ela custa a cortar é o tempo
     * em que ela está espinhando ou atirando quem a corta.
     */
    private static BlockBehaviour.Properties sarça(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.PLANT).strength(20.0f, 20.0f).sound(SoundType.GRASS)
                .noOcclusion().noCollision().pushReaction(PushReaction.DESTROY);
    }

    /** Os números do gelo perpétuo, que são os mesmos em toda a família dele. */
    private static BlockBehaviour.Properties gelo(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.ICE).strength(2.0f, 5.0f).friction(0.98f)
                .sound(SoundType.GLASS).noOcclusion();
    }

    /** E os da neve. */
    private static BlockBehaviour.Properties neve(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.SNOW).strength(0.2f, 0.2f).sound(SoundType.SNOW);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
