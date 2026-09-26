package net.thaumcraft.shattered;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.thaumcraft.Thaumcraft;

import java.util.Set;
import java.util.function.Function;

/** A porta dos Reinos Fragmentados, a antiga que nasce no mundo, e a fenda que mora nelas. */
public final class ShatteredBlocks {
    /** A porta de madeira: a que leva a um bolso qualquer, e a que volta dele. */
    public static final Block OAK_DIMENSIONAL_DOOR = register("oak_dimensional_door", properties ->
            new DimensionalDoorBlock(BlockSetType.OAK, properties.mapColor(MapColor.WOOD)
                    .strength(3.0f).sound(SoundType.WOOD).noOcclusion().pushReaction(
                            net.minecraft.world.level.material.PushReaction.DESTROY)));


    /**
     * A Porta Antiga: a que já estava no mundo, invisível a quem não tenha os Óculos do Véu.
     *
     * <p>Nasce com o mundo, dentro de uma ombreira de pedra num descampado. Não se faz na bancada.
     */
    public static final Block ANCIENT_DIMENSIONAL_DOOR = register("ancient_dimensional_door", properties ->
            new AncientDoorBlock(BlockSetType.STONE, properties.mapColor(MapColor.STONE)
                    .strength(-1.0f, 3600000.0f).sound(SoundType.STONE).noOcclusion().noLootTable()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));

    /**
     * A Placa de Marcação: o {@code BlockMarkingPlate} das Portas Dimensionais.
     *
     * <p>Um poste alto e fino de tecido, de dois blocos de altura, que serve para marcar um lugar. No original
     * não faz mais nada — é enfeite, e é assim que vem para cá.
     */
    public static final Block MARKING_PLATE = register("marking_plate", properties ->
            new net.thaumcraft.shattered.MarkingPlateBlock(properties.mapColor(MapColor.COLOR_BLACK)
                    .strength(0.1f).sound(SoundType.WOOL).noOcclusion()));
    /** A fenda solta, que fica no ar. */
    public static final Block RIFT = register("rift", properties ->
            new FloatingRiftBlock(properties.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f)
                    .noOcclusion().noCollision().noLootTable().lightLevel(state -> 8).randomTicks()
                    .replaceable().air()));

    public static final BlockEntityType<RiftBlockEntity> RIFT_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("rift"),
                    new BlockEntityType<>(RiftBlockEntity::new, Set.of(
                            OAK_DIMENSIONAL_DOOR, ANCIENT_DIMENSIONAL_DOOR, RIFT)));

    private ShatteredBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /**
     * As portas que entram na mochila.
     *
     * <p>Eram cinco — madeira, ferro, ouro, quartzo e o alçapão. Quem manda pediu **só a de madeira**, e o
     * resto saiu. As salas do original que traziam porta de ferro ou de quartzo passam a trazer a de madeira,
     * que é a tradução dos esquemas que resolve isso.
     */
    public static java.util.List<Block> doors() {
        return java.util.List.of(OAK_DIMENSIONAL_DOOR);
    }

    public static void init() {
    }
}
