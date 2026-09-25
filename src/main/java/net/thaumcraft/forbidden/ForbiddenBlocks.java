package net.thaumcraft.forbidden;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Os blocos do Forbidden Magic 0.575: a árvore maculada e a pedra que sai dela. */
public final class ForbiddenBlocks {
    private static final List<Block> ORDER = new ArrayList<>();

    /** O tronco maculado, que na fornalha vira o carvão maculado. */
    public static final Block TAINT_LOG = register("taint_log", properties ->
            new RotatedPillarBlock(properties.mapColor(MapColor.COLOR_PURPLE).strength(2.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));

    /** As tábuas maculadas. */
    public static final Block TAINT_PLANKS = register("taint_planks", properties ->
            new Block(properties.mapColor(MapColor.COLOR_PURPLE).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD).ignitedByLava()));

    /** As folhas maculadas, que dão o Fruto Maculado quando apodrecem. */
    public static final Block TAINT_LEAVES = register("taint_leaves", properties ->
            new TaintedLeavesBlock(properties.mapColor(MapColor.COLOR_PURPLE).strength(0.2f).randomTicks()
                    .sound(SoundType.GRASS).noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY)
                    .isRedstoneConductor((state, level, pos) -> false)));

    /** A muda maculada. */
    public static final Block TAINT_SAPLING = register("taint_sapling", properties ->
            new TaintedSaplingBlock(properties.mapColor(MapColor.COLOR_PURPLE).noCollision().randomTicks()
                    .instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** A pedra maculada, lisa, e o tijolo dela: os dois números do {@code BlockStoneTainted}. */
    public static final Block TAINT_STONE = register("taint_stone", properties -> new Block(stone(properties)));
    public static final Block TAINT_STONE_BRICKS = register("taint_stone_bricks", properties -> new Block(stone(properties)));

    /** O Bolo Arcano: doze fatias que voltam a crescer sozinhas. */
    public static final Block ARCANE_CAKE = register("arcane_cake", properties ->
            new ArcaneCakeBlock(properties.mapColor(MapColor.WOOL).strength(0.5f).sound(SoundType.WOOL)
                    .randomTicks().noOcclusion().pushReaction(PushReaction.DESTROY)));

    /** A Flor de Tinta, que se espalha sozinha e dá tinta preta. */
    public static final Block INK_FLOWER = register("ink_flower", properties ->
            new InkFlowerBlock(properties.mapColor(MapColor.COLOR_BLACK).noCollision().instabreak()
                    .sound(SoundType.GRASS).offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)));

    /** O Arbusto Umbrio, de dois blocos, que espalha as flores. */
    public static final Block UMBRAL_BUSH = register("umbral_bush", properties ->
            new UmbralBushBlock(properties.mapColor(MapColor.COLOR_BLACK).noCollision().instabreak()
                    .randomTicks().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** O Bloco de Estrelas: nove estrelas do Nether, e serve de base de farol. */
    public static final Block NETHER_STAR_BLOCK = register("nether_star_block", properties ->
            new Block(properties.mapColor(MapColor.SNOW).strength(5.0f, 6.0f)
                    .sound(SoundType.METAL).lightLevel(state -> 10)));

    /** A Gaiola da Ira: o gerador de monstros que come essência. */
    public static final Block WRATH_CAGE = register("wrath_cage", properties ->
            new WrathCageBlock(properties.mapColor(MapColor.METAL).strength(5.0f, 2000.0f)
                    .sound(SoundType.METAL)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<WrathCageBlockEntity> WRATH_CAGE_ENTITY =
            net.minecraft.core.Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("wrath_cage"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(WrathCageBlockEntity::new,
                            java.util.Set.of(WRATH_CAGE)));

    private ForbiddenBlocks() {
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).strength(2.0f, 10.0f)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    public static List<Block> shown() {
        return List.copyOf(ORDER);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(key));
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        ORDER.add(block);
        return block;
    }

    public static void init() {
    }
}
