package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.NodeBlock;

/** Os blocos do mod. */
public final class TCBlocks {
    /** O nó de aura: uma bolha de magia parada no ar, que não se quebra na mão. */
    public static final Block NODE = register("node", properties -> new NodeBlock(properties
            .mapColor(MapColor.NONE)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
            .noOcclusion()
            .noCollision()
            .lightLevel(state -> 7)
            .sound(SoundType.AMETHYST)
            .pushReaction(PushReaction.BLOCK)));

    /** O crisol: um caldeirão que a varinha benzeu. */
    public static final Block CRUCIBLE = register("crucible", properties -> new net.thaumcraft.block.CrucibleBlock(properties
            .mapColor(MapColor.METAL)
            .strength(2.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .sound(SoundType.METAL)));

    private TCBlocks() {
    }

    private static Block register(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
