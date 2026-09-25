package net.thaumcraft.mortuorum;

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

import java.util.function.Function;

/** Os blocos do Ars Mortuorum. */
public final class MortuorumBlocks {
    /** A Máquina de Costura, onde os pedaços viram peça de corpo. */
    public static final Block SEWING_MACHINE = register("sewing_machine", properties ->
            new SewingMachineBlock(properties.mapColor(MapColor.METAL).strength(4.0f).sound(SoundType.METAL)
                    .noOcclusion()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<SewingMachineBlockEntity> SEWING_MACHINE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("sewing_machine"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(SewingMachineBlockEntity::new,
                            java.util.Set.of(SEWING_MACHINE)));

    /** O Altar de Invocação, onde as peças viram lacaio. */
    public static final Block SUMMONING_ALTAR = register("summoning_altar", properties ->
            new SummoningAltarBlock(properties.mapColor(MapColor.STONE).strength(3.0f).sound(SoundType.STONE)
                    .noOcclusion()));

    /** A mesa comprida do altar: os dois blocos que ele ocupa ao lado. */
    public static final Block SUMMONING_ALTAR_PART = register("summoning_altar_part", properties ->
            new SummoningAltarPartBlock(properties.mapColor(MapColor.STONE).strength(3.0f).sound(SoundType.STONE)
                    .noOcclusion().noLootTable()));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<SummoningAltarBlockEntity> SUMMONING_ALTAR_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("summoning_altar"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(SummoningAltarBlockEntity::new,
                            java.util.Set.of(SUMMONING_ALTAR)));

    /** O Muro de Caveiras: cerca de pedra caiada com um crânio em cima, e a dureza da obsidiana. */
    public static final Block SKULL_WALL = register("skull_wall", properties ->
            new SkullWallBlock(properties.mapColor(MapColor.QUARTZ).strength(50.0f, 2000.0f)
                    .sound(SoundType.STONE).noOcclusion()));

    /** O sangue que corre no chão. */
    public static final Block BLOOD = register("blood", properties ->
            new net.thaumcraft.fluid.ThaumFluid.LiquidBlock(MortuorumFluids.BLOOD, properties
                    .mapColor(MapColor.COLOR_RED)
                    .replaceable()
                    .noCollision()
                    .strength(100.0f)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .sound(SoundType.EMPTY)));

    private MortuorumBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
