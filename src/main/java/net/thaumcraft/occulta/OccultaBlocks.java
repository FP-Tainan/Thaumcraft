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

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
