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
            new SewingMachineBlock(properties.mapColor(MapColor.METAL).strength(4.0f).sound(SoundType.METAL)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<SewingMachineBlockEntity> SEWING_MACHINE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("sewing_machine"),
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(SewingMachineBlockEntity::new,
                            java.util.Set.of(SEWING_MACHINE)));

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
