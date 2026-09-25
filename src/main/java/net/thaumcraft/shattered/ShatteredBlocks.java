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

/** As portas dos Reinos Fragmentados, e a fenda que mora nelas. */
public final class ShatteredBlocks {
    /** A porta de madeira: a que leva a um bolso qualquer, e a que volta dele. */
    public static final Block OAK_DIMENSIONAL_DOOR = register("oak_dimensional_door", properties ->
            new DimensionalDoorBlock(BlockSetType.OAK, properties.mapColor(MapColor.WOOD)
                    .strength(3.0f).sound(SoundType.WOOD).noOcclusion().pushReaction(
                            net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** A de ferro, que o original dá às portas que já estavam no mundo. */
    public static final Block IRON_DIMENSIONAL_DOOR = register("iron_dimensional_door", properties ->
            new DimensionalDoorBlock(BlockSetType.IRON, properties.mapColor(MapColor.METAL)
                    .strength(5.0f).sound(SoundType.METAL).noOcclusion().pushReaction(
                            net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** A de ouro, que leva sempre ao mesmo bolso. */
    public static final Block GOLD_DIMENSIONAL_DOOR = register("gold_dimensional_door", properties ->
            new DimensionalDoorBlock(BlockSetType.GOLD, properties.mapColor(MapColor.GOLD)
                    .strength(5.0f).sound(SoundType.METAL).noOcclusion().pushReaction(
                            net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** E a de quartzo, que leva ao bolso de quem a atravessa. */
    public static final Block QUARTZ_DIMENSIONAL_DOOR = register("quartz_dimensional_door", properties ->
            new DimensionalDoorBlock(BlockSetType.STONE, properties.mapColor(MapColor.QUARTZ)
                    .strength(5.0f).sound(SoundType.STONE).noOcclusion().pushReaction(
                            net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** A fenda solta, que fica no ar. */
    public static final Block RIFT = register("rift", properties ->
            new FloatingRiftBlock(properties.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f)
                    .noOcclusion().noCollision().noLootTable().lightLevel(state -> 8).randomTicks()
                    .replaceable().air()));

    public static final BlockEntityType<RiftBlockEntity> RIFT_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("rift"),
                    new BlockEntityType<>(RiftBlockEntity::new, Set.of(
                            OAK_DIMENSIONAL_DOOR, IRON_DIMENSIONAL_DOOR,
                            GOLD_DIMENSIONAL_DOOR, QUARTZ_DIMENSIONAL_DOOR, RIFT)));

    private ShatteredBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /** As quatro portas, na ordem da aba. */
    public static java.util.List<Block> doors() {
        return java.util.List.of(OAK_DIMENSIONAL_DOOR, IRON_DIMENSIONAL_DOOR,
                GOLD_DIMENSIONAL_DOOR, QUARTZ_DIMENSIONAL_DOOR);
    }

    public static void init() {
    }
}
