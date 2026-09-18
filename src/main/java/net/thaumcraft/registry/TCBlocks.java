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
            INFUSED_STONE.put(tag, register("infused_stone_" + tag, properties -> new Block(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.STONE))));
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
