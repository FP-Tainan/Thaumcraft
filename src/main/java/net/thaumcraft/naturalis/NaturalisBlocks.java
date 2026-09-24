package net.thaumcraft.naturalis;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Os blocos do Magia Naturalis 0.5.0.
 *
 * <p>A madeira arcana do original era um bloco só, de sete feitios guardados no metadado; o Minecraft de hoje não
 * tem metadado, então cada feitio é um bloco, com o mesmo nome e a mesma figura.
 */
public final class NaturalisBlocks {
    /** A ordem em que os blocos entram na aba do ramo. */
    private static final List<Block> ORDER = new ArrayList<>();

    // ------------------------------------------------------------------ a madeira arcana, feitio por feitio

    /** Tábua de madeira-grande deitada. */
    public static final Block GREATWOOD_PLANKS_HORIZONTAL = wood("greatwood_planks_horizontal");
    /** Ornamento de madeira-grande. */
    public static final Block GREATWOOD_ORNAMENT = wood("greatwood_ornament");
    /** Tábua de madeira-prateada deitada. */
    public static final Block SILVERWOOD_PLANKS_HORIZONTAL = wood("silverwood_planks_horizontal");
    /** Tábua de madeira-prateada em pé. */
    public static final Block SILVERWOOD_PLANKS_VERTICAL = wood("silverwood_planks_vertical");
    /** Ornamento de madeira-grande com ouro. */
    public static final Block GREATWOOD_GOLD_ORNAMENT = wood("greatwood_gold_ornament");
    /** O outro ornamento de madeira-grande com ouro. */
    public static final Block GREATWOOD_GOLD_ORNAMENT_2 = wood("greatwood_gold_ornament_2");
    /** A cercadura de ouro, que só tem o desenho nos lados. */
    public static final Block GREATWOOD_GOLD_TRIM = wood("greatwood_gold_trim");

    /** O Bicho num Jarro: guarda uma criatura viva inteira. */
    public static final Block PRISON_JAR = register("prison_jar", properties -> new PrisonJarBlock(properties
            .mapColor(net.minecraft.world.level.material.MapColor.NONE)
            .strength(0.3f).sound(SoundType.GLASS).lightLevel(state -> 10).noOcclusion()));

    /** A entidade de bloco do jarro. */
    public static final net.minecraft.world.level.block.entity.BlockEntityType<PrisonJarBlockEntity> PRISON_JAR_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("prison_jar"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(PrisonJarBlockEntity::new, java.util.Set.of(PRISON_JAR)));

    /** O Estandarte do Magia Naturalis: o estandarte do Thaumcraft com a figura do ramo. */
    public static final Block BANNER = register("naturalis_banner", properties ->
            new net.thaumcraft.block.BannerBlock(properties
                    .mapColor(net.minecraft.world.level.material.MapColor.WOOD)
                    .strength(2.5f, 10.0f).sound(SoundType.WOOD).noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<net.thaumcraft.block.entity.BannerBlockEntity> BANNER_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("naturalis_banner"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<net.thaumcraft.block.entity.BannerBlockEntity>(
                            NaturalisBannerBlockEntity::new, java.util.Set.of(BANNER)));

    private NaturalisBlocks() {
    }

    public static List<Block> shown() {
        return List.copyOf(ORDER);
    }

    /** Uma madeira arcana: os números do original — dois de dureza, cinco de resistência, som de madeira. */
    private static Block wood(String name) {
        return register(name, properties -> new Block(properties
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 5.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava()));
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        ORDER.add(block);
        return block;
    }

    public static void init() {
    }
}
