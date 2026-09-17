package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.NodeBlockEntity;

/** Os blocos que guardam alguma coisa por dentro. */
public final class TCBlockEntities {
    public static final BlockEntityType<NodeBlockEntity> NODE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("node"),
            new BlockEntityType<>(NodeBlockEntity::new, java.util.Set.of(TCBlocks.NODE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.CrucibleBlockEntity> CRUCIBLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("crucible"),
            new BlockEntityType<>(net.thaumcraft.block.entity.CrucibleBlockEntity::new, java.util.Set.of(TCBlocks.CRUCIBLE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity> ARCANE_WORKBENCH =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("arcane_workbench"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity::new,
                            java.util.Set.of(TCBlocks.ARCANE_WORKBENCH)));

    public static final BlockEntityType<net.thaumcraft.block.entity.JarBlockEntity> JAR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("jar"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.JarBlockEntity::new,
                            java.util.Set.of(TCBlocks.JAR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeBlockEntity> TUBE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AlembicBlockEntity> ALEMBIC =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("alembic"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AlembicBlockEntity::new,
                            java.util.Set.of(TCBlocks.ALEMBIC)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity>
            ALCHEMICAL_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Thaumcraft.id("alchemical_furnace"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity::new,
                            java.util.Set.of(TCBlocks.ALCHEMICAL_FURNACE)));

    private TCBlockEntities() {
    }

    public static void init() {
    }
}
