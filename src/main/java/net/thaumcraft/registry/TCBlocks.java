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

    /** O minério infundido: a pedra de onde os fragmentos de aspecto saem. */
    public static final java.util.Map<String, Block> INFUSED_STONE = new java.util.LinkedHashMap<>();

    static {
        for (String tag : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) {
            // o BlockCustomOre: dureza 1,5, resistência 5, de zero a três de experiência; o brilho é o da veia, no modelo
            INFUSED_STONE.put(tag, register("infused_stone_" + tag, properties -> new net.minecraft.world.level.block.DropExperienceBlock(
                    net.minecraft.util.valueproviders.UniformInt.of(0, 3), properties
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))));
        }
    }

    /** O minério de cinábrio: dá a si mesmo, e fundido vira mercúrio. */
    public static final Block CINNABAR_ORE = register("cinnabar_ore", properties -> new Block(properties
            .mapColor(MapColor.STONE).strength(1.5f, 5.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /** O âmbar preso em pedra: dá âmbar e de um a quatro de experiência. */
    public static final Block AMBER_ORE = register("amber_ore", properties -> new net.minecraft.world.level.block.DropExperienceBlock(
            net.minecraft.util.valueproviders.UniformInt.of(1, 4), properties
            .mapColor(MapColor.STONE).strength(1.5f, 5.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /** Os aglomerados de cristal: um por primordial e o misto. */
    public static final java.util.Map<String, Block> CRYSTAL_CLUSTERS = new java.util.LinkedHashMap<>();

    static {
        String[] kinds = {"air", "fire", "water", "earth", "order", "entropy", "balanced"};
        SoundType crystal = new SoundType(1.0f, 1.0f, TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value(),
                TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value());
        for (int i = 0; i < kinds.length; i++) {
            int kind = i;
            CRYSTAL_CLUSTERS.put(kinds[i], register("crystal_cluster_" + kinds[i], properties -> new net.thaumcraft.block.CrystalClusterBlock(kind, properties
                    .mapColor(MapColor.NONE).strength(0.7f, 1.0f).lightLevel(state -> 7).noOcclusion().sound(crystal))));
        }
    }

    /** As cores das velas, na ordem do número de cada uma no original (a da lã). */
    public static final String[] CANDLE_COLOURS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
            "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    /** O {@code Utils.colors}: a tinta de cada vela. */
    public static final int[] CANDLE_TINTS = {15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131,
            10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019};
    /** As velas de sebo, uma por cor: luz 14 (o 0,95 do original), dureza 0,1, som de lã, sem colisão. */
    public static final java.util.Map<String, Block> TALLOW_CANDLES = new java.util.LinkedHashMap<>();

    static {
        for (String colour : CANDLE_COLOURS) {
            TALLOW_CANDLES.put(colour, register(colour + "_tallow_candle", properties -> new net.thaumcraft.block.TallowCandleBlock(properties
                    .mapColor(MapColor.NONE).strength(0.1f).noCollision().noOcclusion().lightLevel(state -> 14)
                    .sound(SoundType.WOOL).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY))));
        }
    }

    /** O jarro lacrado: um pote de vidro com tampa de chumbo, que guarda essência. */
    public static final Block JAR = register("jar", properties ->
            new net.thaumcraft.block.JarBlock(properties
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));

    /** O buraco do Buraco Portátil: some sozinho, devolvendo o bloco que estava ali. */
    /** O bloco protegido: inquebrável, com a cara e a luz do que estava ali. */
    public static final Block WARDED = register("warded", properties ->
            new net.thaumcraft.block.WardedBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .lightLevel(state -> state.getValue(net.thaumcraft.block.WardedBlock.LIGHT))
                    .pushReaction(PushReaction.BLOCK)
                    .sound(SoundType.STONE)));

    /** As escadas e a laje de pedra arcana, com a cara dos tijolos, como o {@code BlockCosmeticStairs} do original. */
    public static final Block ARCANE_STONE_STAIRS = register("arcane_stone_stairs", properties ->
            new net.minecraft.world.level.block.StairBlock(net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),
                    properties.mapColor(MapColor.STONE).strength(2.0f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final Block ARCANE_STONE_SLAB = register("arcane_stone_slab", properties ->
            new net.minecraft.world.level.block.SlabBlock(
                    properties.mapColor(MapColor.STONE).strength(2.0f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /** O fole, que sopra no forno. */
    public static final Block BELLOWS = register("bellows", properties ->
            new net.thaumcraft.block.BellowsBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f, 6.0f)
                    .noOcclusion()
                    .ignitedByLava()
                    .sound(SoundType.WOOD)));

    /** A base da broca arcana e a broca, os números 4 e 5 do BlockWoodenDevice. */
    public static final Block ARCANE_BORE_BASE = register("arcane_bore_base", properties ->
            new net.thaumcraft.block.ArcaneBoreBaseBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f, 10.0f)
                    .noOcclusion()
                    .ignitedByLava()
                    .sound(SoundType.WOOD)));
    public static final Block ARCANE_BORE = register("arcane_bore", properties ->
            new net.thaumcraft.block.ArcaneBoreBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f, 10.0f)
                    .noOcclusion()
                    .ignitedByLava()
                    .sound(SoundType.WOOD)));

    /** A barreira que a pedra de proteção levanta: parede para bicho, ar para gente. */
    public static final Block WARDING_BARRIER = register("warding_barrier", properties ->
            new net.thaumcraft.block.WardingBarrierBlock(properties
                    .mapColor(MapColor.NONE)
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .noOcclusion()
                    .replaceable()

                    .pushReaction(PushReaction.DESTROY)));

    /** O pilar do altar de infusão: a base (com o modelo) e o topo, que a varinha faz dos cantos do altar. */
    public static final Block INFUSION_PILLAR = register("infusion_pillar", properties ->
            new net.thaumcraft.block.InfusionPillarBlock(false, pillarProperties(properties)));
    public static final Block INFUSION_PILLAR_TOP = register("infusion_pillar_top", properties ->
            new net.thaumcraft.block.InfusionPillarBlock(true, pillarProperties(properties)));

    /** O {@code BlockStoneDevice}: dureza três, resistência vinte e cinco. */
    private static BlockBehaviour.Properties pillarProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.STONE).strength(3.0f, 15.0f).requiresCorrectToolForDrops()
                .noOcclusion().sound(SoundType.STONE);
    }

    public static final Block HOLE = register("hole", properties ->
            new net.thaumcraft.block.HoleBlock(properties
                    .mapColor(MapColor.NONE)
                    .strength(-1.0f, 6000000.0f)
                    .noLootTable()
                    .noOcclusion()
                    .noCollision()
                    .lightLevel(state -> 10)
                    .pushReaction(PushReaction.BLOCK)
                    .sound(SoundType.WOOL)));

    /** O jarro do vazio: o jarro de obsidiana que nunca enche — o que passa do limite some. */
    public static final Block JAR_VOID = register("jar_void", properties ->
            new net.thaumcraft.block.JarBlock(properties
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));

    /** O tubo de essência: o cano de latão por onde a essência corre. */
    public static final Block TUBE = register("tube", properties ->
            new net.thaumcraft.block.TubeBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(0.5f)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** A válvula: o mesmo cano, com um manípulo que se abre e fecha. */
    public static final Block TUBE_VALVE = register("tube_valve", properties ->
            new net.thaumcraft.block.TubeValveBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(0.5f)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** O tubo estreito: passa adiante só metade da fome. */
    public static final Block TUBE_RESTRICT = register("tube_restrict", properties ->
            new net.thaumcraft.block.TubeRestrictBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O tubo filtro: com um rótulo marcado, só puxa o aspecto dele. */
    public static final Block TUBE_FILTER = register("tube_filter", properties ->
            new net.thaumcraft.block.TubeFilterBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O tubo de mão única: a essência só anda para onde ele aponta. */
    public static final Block TUBE_ONEWAY = register("tube_oneway", properties ->
            new net.thaumcraft.block.TubeOnewayBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O tampão de essência: uma caixinha que segura até oito no meio da tubulação. */
    public static final Block TUBE_BUFFER = register("tube_buffer", properties ->
            new net.thaumcraft.block.TubeBufferBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O estabilizador de nó: trava o nó de cima, e o avançado trava mais. */
    public static final Block NODE_STABILIZER = register("node_stabilizer", properties -> new net.thaumcraft.block.NodeStabilizerBlock(false, properties
            .mapColor(MapColor.STONE).strength(3.0f, 25.0f).noOcclusion().sound(SoundType.STONE)));
    public static final Block NODE_STABILIZER_ADVANCED = register("node_stabilizer_advanced", properties -> new net.thaumcraft.block.NodeStabilizerBlock(true, properties
            .mapColor(MapColor.STONE).strength(3.0f, 25.0f).noOcclusion().sound(SoundType.STONE)));

    /** O transdutor de nó: com redstone, energiza o nó estabilizado de baixo. */
    public static final Block NODE_CONVERTER = register("node_converter", properties -> new net.thaumcraft.block.NodeConverterBlock(properties
            .mapColor(MapColor.STONE).strength(3.0f, 25.0f).noOcclusion().sound(SoundType.STONE)));

    /** O nó energizado: a fonte da rede de vis, entre o estabilizador e o transdutor. */
    public static final Block ENERGIZED_NODE = register("energized_node", properties -> new net.thaumcraft.block.EnergizedNodeBlock(properties
            .mapColor(MapColor.NONE).strength(-1.0f, 3600000.0f).noLootTable().noOcclusion().noCollision().lightLevel(state -> 8)));

    /** O relé de vis: leva o vis da rede adiante. */
    public static final Block VIS_RELAY = register("vis_relay", properties -> new net.thaumcraft.block.VisRelayBlock(properties
            .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)
            .lightLevel(state -> state.getValue(net.thaumcraft.block.VisRelayBlock.LIT) ? 10 : 2)));

    /** O carregador da bancada arcana: enche a varinha da bancada com o vis da rede. */
    public static final Block WORKBENCH_CHARGER = register("workbench_charger", properties -> new net.thaumcraft.block.WorkbenchChargerBlock(properties
            .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** A centrífuga alquímica: parte a essência composta nos dois aspectos de que ela é feita. */
    public static final Block CENTRIFUGE = register("centrifuge", properties ->
            new net.thaumcraft.block.CentrifugeBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O cristalizador de essência: prende um ponto de essência num cristal. */
    public static final Block ESSENTIA_CRYSTALIZER = register("essentia_crystalizer", properties ->
            new net.thaumcraft.block.EssentiaCrystalizerBlock(properties
                    .mapColor(MapColor.METAL).strength(0.5f).noOcclusion().sound(SoundType.METAL)));

    /** O construto alquímico: o metadado nove do BlockMetalDevice, peça da centrífuga e do cristalizador. */
    public static final Block ALCHEMICAL_CONSTRUCT = register("alchemical_construct", properties ->
            new Block(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f).sound(SoundType.METAL)));

    /** A construção alquímica avançada: o aparelho de metal 3, peça da fornalha alquímica avançada. */
    public static final Block ADVANCED_ALCHEMICAL_CONSTRUCT = register("advanced_alchemical_construct", properties ->
            new Block(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f).sound(SoundType.METAL)));

    /** A fornalha alquímica avançada: o 3 × 3 × 2 que a varinha forma; sem item. */
    public static final Block ADVANCED_ALCHEMICAL_FURNACE = register("advanced_alchemical_furnace", properties ->
            new net.thaumcraft.block.AdvancedAlchemicalFurnaceBlock(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f)
                    .sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(net.thaumcraft.block.AdvancedAlchemicalFurnaceBlock.LIGHT))));

    /** O reservatório de essência: 256 de qualquer mistura. */
    public static final Block ESSENTIA_RESERVOIR = register("essentia_reservoir", properties ->
            new net.thaumcraft.block.EssentiaReservoirBlock(properties.mapColor(MapColor.METAL).strength(2.0f, 10.2f)
                    .sound(SoundType.METAL).noOcclusion().isRedstoneConductor((s, l, p) -> false)));

    /** O taumatório: as duas construções alquímicas sobre o crisol que a varinha transforma; sem item. */
    public static final Block THAUMATORIUM = register("thaumatorium", properties ->
            new net.thaumcraft.block.ThaumatoriumBlock(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f)
                    .sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK)));

    /** A matriz mnemônica: duas receitas a mais para o taumatório para que está virada. */
    public static final Block MNEMONIC_MATRIX = register("mnemonic_matrix", properties ->
            new net.thaumcraft.block.MnemonicMatrixBlock(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f)
                    .sound(SoundType.METAL).noOcclusion()));

    /** A grade de itens: deixa passar só os itens, aberta. */
    public static final Block ITEM_GRATE = register("item_grate", properties ->
            new net.thaumcraft.block.ItemGrateBlock(properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f)
                    .sound(SoundType.METAL).noOcclusion().isRedstoneConductor((s, l, p) -> false)));

    /** O campo de faísca que o choque de terra espalha: o número 10 do blockAiry. */
    public static final Block SPARK_FIELD = register("spark_field", properties ->
            new net.thaumcraft.block.SparkFieldBlock(properties.mapColor(MapColor.NONE).strength(100.0f, 30.0f).noCollision()
                    .replaceable().noLootTable().randomTicks().lightLevel(state -> 8).pushReaction(PushReaction.DESTROY)));

    /** O bloco de âmbar: o BlockCosmeticOpaque 0, translúcido. */
    public static final Block AMBER_BLOCK = register("amber_block", properties ->
            new net.thaumcraft.block.AmberBlock(properties.mapColor(MapColor.COLOR_ORANGE).strength(1.5f, 3.0f)
                    .sound(SoundType.STONE).noOcclusion()));

    /** Os tijolos de âmbar: o BlockCosmeticOpaque 1. */
    public static final Block AMBER_BRICKS = register("amber_bricks", properties ->
            new net.thaumcraft.block.AmberBlock(properties.mapColor(MapColor.COLOR_ORANGE).strength(1.5f, 3.0f)
                    .sound(SoundType.STONE).noOcclusion()));

    /** O baú faminto: engole o que cai nele. */
    public static final Block HUNGRY_CHEST = register("hungry_chest", properties ->
            new net.thaumcraft.block.HungryChestBlock(properties.mapColor(MapColor.WOOD).strength(2.5f)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));

    /** O levitador arcano: empurra para cima o que está sobre ele. */
    public static final Block LEVITATOR = register("levitator", properties ->
            new net.thaumcraft.block.LevitatorBlock(properties.mapColor(MapColor.WOOD).strength(2.5f, 9.0f)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));

    /** A lâmpada arcana: ilumina forte e espalha luz invisível pelos cantos escuros em volta. */
    public static final Block ARCANE_LAMP = register("arcane_lamp", properties ->
            new net.thaumcraft.block.ArcaneLampBlock(net.thaumcraft.block.ArcaneLampBlock.Kind.ARCANE, lampProperties(properties).lightLevel(state -> 15)));

    /** A lâmpada do crescimento: com Herba, faz as plantas em volta crescerem depressa. */
    public static final Block GROWTH_LAMP = register("growth_lamp", properties ->
            new net.thaumcraft.block.ArcaneLampBlock(net.thaumcraft.block.ArcaneLampBlock.Kind.GROWTH, lampProperties(properties).lightLevel(net.thaumcraft.block.ArcaneLampBlock::lightOf)));

    /** A lâmpada da fertilidade: com Victus, põe os bichos em volta no cio. */
    public static final Block FERTILITY_LAMP = register("fertility_lamp", properties ->
            new net.thaumcraft.block.ArcaneLampBlock(net.thaumcraft.block.ArcaneLampBlock.Kind.FERTILITY, lampProperties(properties).lightLevel(net.thaumcraft.block.ArcaneLampBlock::lightOf)));

    /** A luz invisível da lâmpada arcana (o blockAiry 3 do original). */
    public static final Block LAMP_LIGHT = register("lamp_light", properties ->
            new net.thaumcraft.block.LampLightBlock(properties.replaceable().noCollision().noLootTable().air()
                    .lightLevel(state -> 15).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** O alambique arcano: o pote que se empilha sobre o forno e recolhe o que sai dele. */
    public static final Block ALEMBIC = register("alembic", properties ->
            new net.thaumcraft.block.AlembicBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(1.5f)
                    .noOcclusion()
                    .sound(SoundType.METAL)));

    /** O forno alquímico: onde a coisa deixa de ser coisa e vira essência. */
    public static final Block ALCHEMICAL_FURNACE = register("alchemical_furnace", properties ->
            new net.thaumcraft.block.AlchemicalFurnaceBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(
                            net.thaumcraft.block.AlchemicalFurnaceBlock.LIT) ? 13 : 0)
                    .sound(SoundType.STONE)));

    /** O pedestal arcano: segura uma coisa só, à vista de todos. */
    public static final Block PEDESTAL = register("pedestal", properties ->
            new net.thaumcraft.block.PedestalBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .sound(SoundType.STONE)));

    /** A matriz rúnica: a pedra entalhada onde a infusão acontece. */
    public static final Block INFUSION_MATRIX = register("infusion_matrix", properties ->
            new net.thaumcraft.block.InfusionMatrixBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> 10)
                    .sound(SoundType.STONE)));

    /** O Nitor: a chama fria que não queima nada e não se apaga. */
    public static final Block NITOR = register("nitor", properties ->
            new net.thaumcraft.block.NitorBlock(properties
                    .mapColor(MapColor.FIRE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .sound(SoundType.WOOL)));

    /** A folha-cintilante: a flor branca de que se faz a Flor Etérea. */
    public static final Block SHIMMERLEAF = register("shimmerleaf", properties ->
            new net.thaumcraft.block.ShimmerleafBlock(properties
                    .mapColor(MapColor.SNOW)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .lightLevel(state -> 8)
                    .sound(SoundType.GRASS)));

    /** A pérola de cinzas: a flor do deserto, com luz oito. */
    public static final Block CINDERPEARL = register("cinderpearl", properties ->
            new net.thaumcraft.block.CinderpearlBlock(properties
                    .mapColor(MapColor.COLOR_ORANGE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .lightLevel(state -> 8)
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    /** O cogumelo-vis: o da Floresta Mágica, com luz oito, que deixa tonto quem encosta. */
    public static final Block VISHROOM = register("vishroom", properties ->
            new net.thaumcraft.block.VishroomBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .lightLevel(state -> 8)
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    /** A vagem de mana: pendura embaixo das toras da Floresta Mágica e dá feijões de mana. */
    public static final Block MANA_POD = register("mana_pod", properties ->
            new net.thaumcraft.block.ManaPodBlock(properties
                    .mapColor(MapColor.PLANT)
                    .strength(0.5f)
                    .noOcclusion()
                    .randomTicks()
                    .lightLevel(state -> state.getValue(net.thaumcraft.block.ManaPodBlock.AGE))
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    /** A porta arcana: de ferro, dura (15), quase à prova de explosão; só abre para o dono e quem tem chave. */
    public static final Block ARCANE_DOOR = register("arcane_door", properties ->
            new net.thaumcraft.block.ArcaneDoorBlock(properties
                    .mapColor(MapColor.METAL).strength(15.0f, 999.0f).noOcclusion().sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    /** A placa de pressão arcana: com dono, dispara com tudo, com tudo menos o dono, ou só com o dono. */
    public static final Block ARCANE_PRESSURE_PLATE = register("arcane_pressure_plate", properties ->
            new net.thaumcraft.block.ArcanePressurePlateBlock(properties
                    .mapColor(MapColor.WOOD).strength(2.0f, 999.0f).noCollision().sound(SoundType.WOOD)
                    .pushReaction(PushReaction.BLOCK)));

    /** O ouvido arcano: escuta os blocos musicais e dá sinal quando ouve a nota dele. */
    public static final Block ARCANE_EAR = register("arcane_ear", properties ->
            new net.thaumcraft.block.ArcaneEarBlock(properties
                    .mapColor(MapColor.WOOD).strength(2.5f, 10.0f).noOcclusion().sound(SoundType.WOOD)));

    /** O vidro protegido: com dono, duro (5), que explosão não quebra e se emenda com o vizinho. */
    public static final Block WARDED_GLASS = register("warded_glass", properties ->
            new net.thaumcraft.block.WardedGlassBlock(properties
                    .mapColor(MapColor.NONE).strength(5.0f, 999.0f).noOcclusion().sound(SoundType.GLASS)
                    .isValidSpawn((s, l, p, e) -> false).isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false)));

    /** O som de vidro do jarro, baixo e agudo: o CustomStepSound("jar", 0.5, 2.0) dos espelhos. */
    private static final SoundType MIRROR_SOUND = new SoundType(0.5f, 2.0f, TCSounds.JAR.value(), TCSounds.JAR.value(), TCSounds.JAR.value(),
            TCSounds.JAR.value(), TCSounds.JAR.value());

    /** O espelho mágico: o que entra por um sai pelo par. */
    public static final Block MIRROR = register("mirror", properties ->
            new net.thaumcraft.block.MirrorBlock(false, properties
                    .mapColor(MapColor.NONE).strength(1.0f, 10.0f).noOcclusion().noCollision().sound(MIRROR_SOUND)
                    .pushReaction(PushReaction.DESTROY)));

    /** O espelho de essência: quem bebe essência dele bebe dos recipientes à frente do par. */
    public static final Block ESSENTIA_MIRROR = register("essentia_mirror", properties ->
            new net.thaumcraft.block.MirrorBlock(true, properties
                    .mapColor(MapColor.NONE).strength(1.0f, 10.0f).noOcclusion().noCollision().sound(MIRROR_SOUND)
                    .pushReaction(PushReaction.DESTROY)));

    /** A fornalha infernal: o cubo que a varinha forma de obsidiana, tijolo do Nether, grade e lava. Sem item. */
    public static final Block INFERNAL_FURNACE = register("infernal_furnace", properties ->
            new net.thaumcraft.block.InfernalFurnaceBlock(properties
                    .mapColor(MapColor.COLOR_BLACK).strength(10.0f, 300.0f).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(state -> { int part = state.getValue(net.thaumcraft.block.InfernalFurnaceBlock.PART); return part == 0 || part == 10 ? 13 : 3; })
                    .isRedstoneConductor((s, l, p) -> s.getValue(net.thaumcraft.block.InfernalFurnaceBlock.PART) != 0 && s.getValue(net.thaumcraft.block.InfernalFurnaceBlock.PART) != 10)
                    .isSuffocating((s, l, p) -> s.getValue(net.thaumcraft.block.InfernalFurnaceBlock.PART) != 0 && s.getValue(net.thaumcraft.block.InfernalFurnaceBlock.PART) != 10)
                    .isValidSpawn((s, l, p, e) -> false).pushReaction(PushReaction.BLOCK)));

    /** A crosta da mácula: o que sobra de um tronco ou de uma folha que ela tomou. */
    public static final Block TAINT_CRUST = register("taint_crust", properties ->
            new net.thaumcraft.block.TaintBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6f)
                    .randomTicks()
                    .sound(SoundType.SLIME_BLOCK)));

    /** O solo maculado: a terra que ela tomou. */
    public static final Block TAINT_SOIL = register("taint_soil", properties ->
            new net.thaumcraft.block.TaintBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6f)
                    .randomTicks()
                    .sound(SoundType.SLIME_BLOCK)));

    /** As fibras da mácula: o mato roxo que nasce por cima do que ela tomou. */
    public static final Block TAINT_FIBRES = register("taint_fibres", properties ->
            new net.thaumcraft.block.TaintFibreBlock(properties
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    /** A Flor Etérea: a única coisa que faz a mácula recuar. */
    public static final Block ETHEREAL_BLOOM = register("ethereal_bloom", properties ->
            new net.thaumcraft.block.EtherealBloomBlock(properties
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .randomTicks()
                    .lightLevel(state -> 15)
                    .sound(SoundType.GRASS)));

    /** A bancada arcana: a mesa que a varinha benzeu. */
    public static final Block ARCANE_WORKBENCH = register("arcane_workbench", properties ->
            new net.thaumcraft.block.ArcaneWorkbenchBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    /** A mesa de desconstrução, que desfaz coisas em pontos de pesquisa. */
    public static final Block DECONSTRUCTION_TABLE = register("deconstruction_table", properties ->
            new net.thaumcraft.block.DeconstructionTableBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    /** O manipulador focal, onde os focos recebem melhorias. */
    public static final Block FOCAL_MANIPULATOR = register("focal_manipulator", properties ->
            new net.thaumcraft.block.FocalManipulatorBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 25.0f)
                    .noOcclusion()
                    .sound(SoundType.STONE)));

    /** O pedestal de recarga de varinhas e o foco composto que vai em cima dele (os números 5 e 8 do BlockStoneDevice). */
    public static final Block WAND_PEDESTAL = register("wand_pedestal", properties ->
            new net.thaumcraft.block.WandPedestalBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 25.0f)
                    .noOcclusion()
                    .sound(SoundType.STONE)));
    public static final Block RECHARGE_FOCUS = register("recharge_focus", properties ->
            new net.thaumcraft.block.RechargeFocusBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 25.0f)
                    .noOcclusion()
                    .sound(SoundType.STONE)));

    /** A mesa de madeira do mod: vira bancada arcana com a varinha e mesa de pesquisa com a pena. */
    public static final Block TABLE = register("table", properties ->
            new net.thaumcraft.block.TableBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    /** A mesa de pesquisa: duas mesas juntadas pelas ferramentas de escrita. */
    public static final Block RESEARCH_TABLE = register("research_table", properties ->
            new net.thaumcraft.block.ResearchTableBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    /** Os blocos de construção do mod. */
    public static final java.util.Map<String, Block> BUILDING = new java.util.LinkedHashMap<>();

    static {
        for (String name : TCBuilding.NAMES) {
            BUILDING.put(name, register(name, properties -> {
                properties.mapColor(MapColor.STONE).strength(2.0f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.STONE);
                // as pedras de pavimento fazem coisas; as outras são só pedra
                if (name.equals("paving_stone_travel")) return new net.thaumcraft.block.PavingStoneBlock(false, properties);
                if (name.equals("paving_stone_warding")) return new net.thaumcraft.block.PavingStoneBlock(true, properties);
                return new Block(properties);
            }));
        }
    }

    private TCBlocks() {
    }

// ----------------------------------------------------------------- as árvores mágicas

    /** A tora de grande-madeira: dureza dois e meio, como no {@code BlockMagicalLog}. */
    public static final Block GREATWOOD_LOG = register("greatwood_log", properties ->
            new net.minecraft.world.level.block.RotatedPillarBlock(logProperties(properties, MapColor.COLOR_BROWN)));

    /** A tora de pinheiro-de-prata. */
    public static final Block SILVERWOOD_LOG = register("silverwood_log", properties ->
            new net.minecraft.world.level.block.RotatedPillarBlock(logProperties(properties, MapColor.QUARTZ)));

    /** O nó do pinheiro-de-prata: um tronco com um nó de aura puro dentro, que brilha com luz sete. */
    public static final Block SILVERWOOD_KNOT = register("silverwood_knot", properties ->
            new net.thaumcraft.block.SilverwoodKnotBlock(logProperties(properties, MapColor.QUARTZ)
                    .lightLevel(state -> 7)));

    /** As folhas da grande-madeira, verdes da folhagem do lugar. */
    public static final Block GREATWOOD_LEAVES = register("greatwood_leaves", properties ->
            new net.thaumcraft.block.MagicalLeavesBlock(false, leafProperties(properties)));

    /** As folhas do pinheiro-de-prata, cinza-azuladas e com luz sete. */
    public static final Block SILVERWOOD_LEAVES = register("silverwood_leaves", properties ->
            new net.thaumcraft.block.MagicalLeavesBlock(true, leafProperties(properties).lightLevel(state -> 7)));

    /** A muda de grande-madeira. */
    public static final Block GREATWOOD_SAPLING = register("greatwood_sapling", properties ->
            new net.thaumcraft.block.MagicalSaplingBlock(false, saplingProperties(properties)));

    /** A muda de pinheiro-de-prata, que já brilha com luz oito. */
    public static final Block SILVERWOOD_SAPLING = register("silverwood_sapling", properties ->
            new net.thaumcraft.block.MagicalSaplingBlock(true, saplingProperties(properties).lightLevel(state -> 8)));

    /** As tábuas: as metas seis e sete do {@code BlockWoodenDevice}, dureza dois e meio. */
    public static final Block GREATWOOD_PLANKS = register("greatwood_planks", properties ->
            new Block(plankProperties(properties, MapColor.COLOR_BROWN)));

    public static final Block SILVERWOOD_PLANKS = register("silverwood_planks", properties ->
            new Block(plankProperties(properties, MapColor.QUARTZ)));

    public static final Block GREATWOOD_STAIRS = register("greatwood_stairs", properties ->
            new net.minecraft.world.level.block.StairBlock(GREATWOOD_PLANKS.defaultBlockState(),
                    plankProperties(properties, MapColor.COLOR_BROWN)));

    public static final Block SILVERWOOD_STAIRS = register("silverwood_stairs", properties ->
            new net.minecraft.world.level.block.StairBlock(SILVERWOOD_PLANKS.defaultBlockState(),
                    plankProperties(properties, MapColor.QUARTZ)));

    public static final Block GREATWOOD_SLAB = register("greatwood_slab", properties ->
            new net.minecraft.world.level.block.SlabBlock(plankProperties(properties, MapColor.COLOR_BROWN)));

    public static final Block SILVERWOOD_SLAB = register("silverwood_slab", properties ->
            new net.minecraft.world.level.block.SlabBlock(plankProperties(properties, MapColor.QUARTZ)));

    private static BlockBehaviour.Properties logProperties(BlockBehaviour.Properties properties, MapColor color) {
        return properties.mapColor(color).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.5f).sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties leafProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.PLANT).strength(0.2f).randomTicks().sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(net.minecraft.world.level.block.Blocks::ocelotOrParrot).isSuffocating((s, l, p) -> false)
                .isViewBlocking((s, l, p) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor((s, l, p) -> false);
    }

    /** O BlockMetalDevice das lâmpadas: metal e dureza três; a luz vai em cada uma. */
    private static BlockBehaviour.Properties lampProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.METAL).strength(3.0f, 10.2f).sound(SoundType.METAL).noOcclusion();
    }

    private static BlockBehaviour.Properties saplingProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties plankProperties(BlockBehaviour.Properties properties, MapColor color) {
        return properties.mapColor(color).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.5f, 6.0f).sound(SoundType.WOOD).ignitedByLava();
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
