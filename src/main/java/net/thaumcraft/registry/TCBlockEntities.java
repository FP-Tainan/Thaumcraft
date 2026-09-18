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
            new BlockEntityType<>(NodeBlockEntity::new, java.util.Set.of(TCBlocks.NODE, TCBlocks.SILVERWOOD_KNOT)));

    public static final BlockEntityType<net.thaumcraft.block.entity.CrucibleBlockEntity> CRUCIBLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("crucible"),
            new BlockEntityType<>(net.thaumcraft.block.entity.CrucibleBlockEntity::new, java.util.Set.of(TCBlocks.CRUCIBLE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity> ARCANE_WORKBENCH =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("arcane_workbench"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity::new,
                            java.util.Set.of(TCBlocks.ARCANE_WORKBENCH)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ResearchTableBlockEntity> RESEARCH_TABLE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, net.thaumcraft.Thaumcraft.id("research_table"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ResearchTableBlockEntity::new,
                            java.util.Set.of(TCBlocks.RESEARCH_TABLE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.JarBlockEntity> JAR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("jar"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.JarBlockEntity::new,
                            java.util.Set.of(TCBlocks.JAR, TCBlocks.JAR_VOID)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeBlockEntity> TUBE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.HoleBlockEntity> HOLE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("hole"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.HoleBlockEntity::new, java.util.Set.of(TCBlocks.HOLE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.InfusionPillarBlockEntity> INFUSION_PILLAR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("infusion_pillar"),
            new BlockEntityType<>(net.thaumcraft.block.entity.InfusionPillarBlockEntity::new, java.util.Set.of(TCBlocks.INFUSION_PILLAR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.DeconstructionTableBlockEntity> DECONSTRUCTION_TABLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("deconstruction_table"),
            new BlockEntityType<>(net.thaumcraft.block.entity.DeconstructionTableBlockEntity::new, java.util.Set.of(TCBlocks.DECONSTRUCTION_TABLE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.BellowsBlockEntity> BELLOWS = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("bellows"),
            new BlockEntityType<>(net.thaumcraft.block.entity.BellowsBlockEntity::new, java.util.Set.of(TCBlocks.BELLOWS)));

    public static final BlockEntityType<net.thaumcraft.block.entity.WardingStoneBlockEntity> WARDING_STONE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("warding_stone"),
            new BlockEntityType<>(net.thaumcraft.block.entity.WardingStoneBlockEntity::new,
                    java.util.Set.of(TCBlocks.BUILDING.get("paving_stone_warding"))));

    public static final BlockEntityType<net.thaumcraft.block.entity.WardedBlockEntity> WARDED = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("warded"),
            new BlockEntityType<>(net.thaumcraft.block.entity.WardedBlockEntity::new, java.util.Set.of(TCBlocks.WARDED)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeRestrictBlockEntity> TUBE_RESTRICT =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube_restrict"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeRestrictBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE_RESTRICT)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeFilterBlockEntity> TUBE_FILTER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube_filter"),
            new BlockEntityType<>(net.thaumcraft.block.entity.TubeFilterBlockEntity::new, java.util.Set.of(TCBlocks.TUBE_FILTER)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeOnewayBlockEntity> TUBE_ONEWAY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube_oneway"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeOnewayBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE_ONEWAY)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeBufferBlockEntity> TUBE_BUFFER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube_buffer"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeBufferBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE_BUFFER)));

    public static final BlockEntityType<net.thaumcraft.block.entity.TubeValveBlockEntity> TUBE_VALVE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("tube_valve"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.TubeValveBlockEntity::new,
                            java.util.Set.of(TCBlocks.TUBE_VALVE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AlembicBlockEntity> ALEMBIC =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("alembic"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AlembicBlockEntity::new,
                            java.util.Set.of(TCBlocks.ALEMBIC)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity>
            ALCHEMICAL_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Thaumcraft.id("alchemical_furnace"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity::new,
                            java.util.Set.of(TCBlocks.ALCHEMICAL_FURNACE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.PedestalBlockEntity> PEDESTAL =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("pedestal"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.PedestalBlockEntity::new,
                            java.util.Set.of(TCBlocks.PEDESTAL)));

    public static final BlockEntityType<net.thaumcraft.block.entity.InfusionMatrixBlockEntity>
            INFUSION_MATRIX = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Thaumcraft.id("infusion_matrix"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.InfusionMatrixBlockEntity::new,
                            java.util.Set.of(TCBlocks.INFUSION_MATRIX)));

    public static final BlockEntityType<net.thaumcraft.block.entity.NitorBlockEntity> NITOR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("nitor"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.NitorBlockEntity::new,
                            java.util.Set.of(TCBlocks.NITOR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.EtherealBloomBlockEntity> ETHEREAL_BLOOM =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("ethereal_bloom"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.EtherealBloomBlockEntity::new,
                            java.util.Set.of(TCBlocks.ETHEREAL_BLOOM)));

    public static final BlockEntityType<net.thaumcraft.block.entity.CentrifugeBlockEntity> CENTRIFUGE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("centrifuge"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.CentrifugeBlockEntity::new,
                            java.util.Set.of(TCBlocks.CENTRIFUGE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.EssentiaCrystalizerBlockEntity> ESSENTIA_CRYSTALIZER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("essentia_crystalizer"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.EssentiaCrystalizerBlockEntity::new,
                            java.util.Set.of(TCBlocks.ESSENTIA_CRYSTALIZER)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ArcaneLampBlockEntity> ARCANE_LAMP =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("arcane_lamp"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ArcaneLampBlockEntity::new, java.util.Set.of(TCBlocks.ARCANE_LAMP)));

    public static final BlockEntityType<net.thaumcraft.block.entity.GrowthLampBlockEntity> GROWTH_LAMP =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("growth_lamp"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.GrowthLampBlockEntity::new, java.util.Set.of(TCBlocks.GROWTH_LAMP)));

    public static final BlockEntityType<net.thaumcraft.block.entity.FertilityLampBlockEntity> FERTILITY_LAMP =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("fertility_lamp"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.FertilityLampBlockEntity::new, java.util.Set.of(TCBlocks.FERTILITY_LAMP)));

    public static final BlockEntityType<net.thaumcraft.block.entity.HungryChestBlockEntity> HUNGRY_CHEST =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("hungry_chest"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.HungryChestBlockEntity::new, java.util.Set.of(TCBlocks.HUNGRY_CHEST)));

    public static final BlockEntityType<net.thaumcraft.block.entity.LevitatorBlockEntity> LEVITATOR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("levitator"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.LevitatorBlockEntity::new, java.util.Set.of(TCBlocks.LEVITATOR)));

    private TCBlockEntities() {
    }

    public static void init() {
    }
}
