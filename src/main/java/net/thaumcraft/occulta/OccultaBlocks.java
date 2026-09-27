package net.thaumcraft.occulta;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
