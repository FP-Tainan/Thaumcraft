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
