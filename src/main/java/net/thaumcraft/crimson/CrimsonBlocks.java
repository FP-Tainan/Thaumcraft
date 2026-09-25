package net.thaumcraft.crimson;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.thaumcraft.Thaumcraft;

import java.util.Set;
import java.util.function.Function;

/** Os blocos do Crimson Warfare: só o altar. */
public final class CrimsonBlocks {
    /** O Altar Antigo, que não se quebra — o {@code setBlockUnbreakable} do original. */
    public static final Block ANCIENT_ALTAR = register("ancient_altar", properties ->
            new AncientAltarBlock(properties.mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f)
                    .noOcclusion().sound(SoundType.STONE).noLootTable()));

    public static final BlockEntityType<AncientAltarBlockEntity> ANCIENT_ALTAR_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("ancient_altar"),
                    new BlockEntityType<>(AncientAltarBlockEntity::new, Set.of(ANCIENT_ALTAR)));

    private CrimsonBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
    }
}
