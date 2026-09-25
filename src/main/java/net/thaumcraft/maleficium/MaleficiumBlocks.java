package net.thaumcraft.maleficium;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;

/**
 * Os blocos do Maleficium: a madeira distorcida inteira, a beladona e o Lumos.
 *
 * <p>No original o nó do tronco era o mesmo bloco com outro número; aqui é um bloco à parte, que é como o jogo de
 * hoje faz. Os dois se parecem de lado, mas o nó é duro como obsidiana e guarda sementes do vazio.
 */
public final class MaleficiumBlocks {
    /** A tora da árvore distorcida. */
    public static final Block WARPWOOD_LOG = register("warpwood_log", properties ->
            new RotatedPillarBlock(log(properties)));

    /** O nó do tronco: de uma a cinco sementes do vazio, e duro de quebrar. */
    public static final Block WARPWOOD_KNOT = register("warpwood_knot", properties ->
            new WarpwoodKnotBlock(log(properties).strength(25.0f, 25.0f).requiresCorrectToolForDrops()));

    /** As tábuas de madeira distorcida. */
    public static final Block WARPWOOD_PLANKS = register("warpwood_planks", properties ->
            new Block(log(properties).strength(2.0f, 3.0f)));

    /** As folhas distorcidas. */
    public static final Block WARPWOOD_LEAVES = register("warpwood_leaves", properties ->
            new net.thaumcraft.block.MagicalLeavesBlock(false, properties
                    .mapColor(MapColor.COLOR_PURPLE).strength(0.1f).randomTicks().sound(SoundType.GRASS)
                    .noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, level, pos) -> false)));

    /** A muda da árvore distorcida. */
    public static final Block WARPWOOD_SAPLING = register("warpwood_sapling", properties ->
            new WarpwoodSaplingBlock(properties.mapColor(MapColor.COLOR_PURPLE).noCollision().randomTicks()
                    .instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** A beladona, que fere e envenena quem passa por ela. */
    public static final Block NIGHTSHADE_BUSH = register("nightshade_bush", properties ->
            new NightshadeBushBlock(properties.mapColor(MapColor.PLANT).noCollision().instabreak()
                    .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** A luzinha que o Lumos deixa pelo caminho. */
    public static final Block LUMOS = register("lumos", properties ->
            new LumosBlock(properties.mapColor(MapColor.NONE).noCollision().instabreak().noLootTable()
                    .lightLevel(state -> 15).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));

    /** A entidade de bloco do Lumos, que só existe para a chama ter onde se pendurar. */
    public static final net.minecraft.world.level.block.entity.BlockEntityType<LumosBlockEntity> LUMOS_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("lumos"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(LumosBlockEntity::new,
                            java.util.Set.of(LUMOS)));

    private MaleficiumBlocks() {
    }

    private static BlockBehaviour.Properties log(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
                .strength(2.5f).sound(SoundType.WOOD).ignitedByLava();
    }

    private static Block register(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
