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

    public static final BlockEntityType<net.thaumcraft.block.entity.FocalManipulatorBlockEntity> FOCAL_MANIPULATOR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("focal_manipulator"),
            new BlockEntityType<>(net.thaumcraft.block.entity.FocalManipulatorBlockEntity::new, java.util.Set.of(TCBlocks.FOCAL_MANIPULATOR)));

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

    public static final BlockEntityType<net.thaumcraft.block.entity.NodeStabilizerBlockEntity> NODE_STABILIZER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("node_stabilizer"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.NodeStabilizerBlockEntity::new,
                            java.util.Set.of(TCBlocks.NODE_STABILIZER, TCBlocks.NODE_STABILIZER_ADVANCED)));

    public static final BlockEntityType<net.thaumcraft.block.entity.NodeConverterBlockEntity> NODE_CONVERTER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("node_converter"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.NodeConverterBlockEntity::new,
                            java.util.Set.of(TCBlocks.NODE_CONVERTER)));

    public static final BlockEntityType<net.thaumcraft.block.entity.EnergizedNodeBlockEntity> ENERGIZED_NODE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("energized_node"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.EnergizedNodeBlockEntity::new,
                            java.util.Set.of(TCBlocks.ENERGIZED_NODE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.VisRelayBlockEntity> VIS_RELAY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("vis_relay"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.VisRelayBlockEntity::new,
                            java.util.Set.of(TCBlocks.VIS_RELAY)));

    public static final BlockEntityType<net.thaumcraft.block.entity.WorkbenchChargerBlockEntity> WORKBENCH_CHARGER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("workbench_charger"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.WorkbenchChargerBlockEntity::new,
                            java.util.Set.of(TCBlocks.WORKBENCH_CHARGER)));

    public static final BlockEntityType<net.thaumcraft.block.entity.OwnedBlockEntity> OWNED =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("owned"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.OwnedBlockEntity::new, java.util.Set.of(TCBlocks.ARCANE_DOOR, TCBlocks.WARDED_GLASS)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ArcanePressurePlateBlockEntity> ARCANE_PRESSURE_PLATE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("arcane_pressure_plate"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ArcanePressurePlateBlockEntity::new, java.util.Set.of(TCBlocks.ARCANE_PRESSURE_PLATE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ArcaneEarBlockEntity> ARCANE_EAR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("arcane_ear"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ArcaneEarBlockEntity::new, java.util.Set.of(TCBlocks.ARCANE_EAR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.MirrorBlockEntity> MIRROR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("mirror"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.MirrorBlockEntity::new, java.util.Set.of(TCBlocks.MIRROR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.EssentiaMirrorBlockEntity> ESSENTIA_MIRROR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("essentia_mirror"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.EssentiaMirrorBlockEntity::new, java.util.Set.of(TCBlocks.ESSENTIA_MIRROR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.InfernalFurnaceBlockEntity> INFERNAL_FURNACE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("infernal_furnace"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.InfernalFurnaceBlockEntity::new, java.util.Set.of(TCBlocks.INFERNAL_FURNACE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.InfernalFurnaceNozzleBlockEntity> INFERNAL_FURNACE_NOZZLE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("infernal_furnace_nozzle"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.InfernalFurnaceNozzleBlockEntity::new, java.util.Set.of(TCBlocks.INFERNAL_FURNACE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity> ADVANCED_ALCHEMICAL_FURNACE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("advanced_alchemical_furnace"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceBlockEntity::new, java.util.Set.of(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceNozzleBlockEntity> ADVANCED_ALCHEMICAL_FURNACE_NOZZLE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("advanced_alchemical_furnace_nozzle"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.AdvancedAlchemicalFurnaceNozzleBlockEntity::new, java.util.Set.of(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.EssentiaReservoirBlockEntity> ESSENTIA_RESERVOIR =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("essentia_reservoir"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.EssentiaReservoirBlockEntity::new, java.util.Set.of(TCBlocks.ESSENTIA_RESERVOIR)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ThaumatoriumBlockEntity> THAUMATORIUM =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("thaumatorium"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ThaumatoriumBlockEntity::new, java.util.Set.of(TCBlocks.THAUMATORIUM)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ThaumatoriumTopBlockEntity> THAUMATORIUM_TOP =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("thaumatorium_top"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ThaumatoriumTopBlockEntity::new, java.util.Set.of(TCBlocks.THAUMATORIUM)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ItemGrateBlockEntity> ITEM_GRATE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("item_grate"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ItemGrateBlockEntity::new, java.util.Set.of(TCBlocks.ITEM_GRATE)));

    public static final BlockEntityType<net.thaumcraft.block.entity.ManaPodBlockEntity> MANA_POD =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("mana_pod"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.ManaPodBlockEntity::new, java.util.Set.of(TCBlocks.MANA_POD)));

    public static final BlockEntityType<net.thaumcraft.block.entity.CrystalClusterBlockEntity> CRYSTAL_CLUSTER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("crystal_cluster"),
                    new BlockEntityType<>(net.thaumcraft.block.entity.CrystalClusterBlockEntity::new,
                            java.util.Set.copyOf(TCBlocks.CRYSTAL_CLUSTERS.values())));

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
