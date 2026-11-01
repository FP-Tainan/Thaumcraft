package net.thaumcraft.arcana;

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
import net.minecraft.world.level.material.PushReaction;
import net.thaumcraft.Thaumcraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Os blocos do Ars Arcana. */
public final class ArcanaBlocks {
    /**
     * A <b>Runa</b> desenhada no chão: um feitiço que espera quem pisar nela.
     *
     * <p>Ela não se parte a pico e não se empurra a pistão: ou dispara e se gasta, ou fica. É o que o original
     * faz, e é o que a torna uma armadilha e não um bloco.
     */
    public static final Block SPELL_RUNE = register("spell_rune", properties ->
            new SpellRuneBlock(properties
                    .mapColor(MapColor.NONE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.STONE)
                    .lightLevel(estado -> 3)));

    public static final BlockEntityType<SpellRuneBlockEntity> SPELL_RUNE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("spell_rune"),
                    new BlockEntityType<>(SpellRuneBlockEntity::new, Set.of(SPELL_RUNE)));

    /**
     * A <b>Mesa de Inscrição</b>: a bancada do arcanista, onde as peças viram frase.
     *
     * <p>De madeira e pedra, como no original, e com a resistência de uma bancada de verdade — quem a põe
     * quer que ela fique.
     */
    public static final Block INSCRIPTION_TABLE = register("inscription_table", properties ->
            new InscriptionTableBlock(properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final BlockEntityType<InscriptionTableBlockEntity> INSCRIPTION_TABLE_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("inscription_table"),
                    new BlockEntityType<>(InscriptionTableBlockEntity::new, Set.of(INSCRIPTION_TABLE)));

    /**
     * O <b>Óculus</b>: o pedestal onde se olha para o que se pode aprender.
     *
     * <p>De pedra, e acende um pouco, porque o olho dele é de vidro e guarda luz.
     */
    public static final Block OCCULUS = register("occulus", properties ->
            new OcculusBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(estado -> 7)));

    /**
     * O <b>Giz de Mago</b>: o risco no chão com que se desenham as figuras do ramo.
     *
     * <p>Não tem corpo, não se empurra, parte-se num toque e não deixa nada.
     */
    public static final Block WIZARD_CHALK = register("wizard_chalk", properties ->
            new net.thaumcraft.arcana.machine.WizardChalkBlock(properties
                    .mapColor(MapColor.NONE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.STONE)));

    /**
     * A <b>Cerublossom</b>: a florzinha azul que acende um pouco.
     *
     * <p>Nasce onde uma flor do jogo nasce, e é um dos dois ingredientes raros da Chave de Cristal.
     */
    public static final Block CERUBLOSSOM = register("cerublossom", properties ->
            new net.thaumcraft.arcana.machine.ArcanaFlowerBlock(properties
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.GRASS)
                    .lightLevel(estado -> 5)));

    /** A <b>Nova do Deserto</b>: a flor da areia, que abre um anel de luz à sua volta. */
    public static final Block DESERT_NOVA = register("desert_nova", properties ->
            new net.thaumcraft.arcana.machine.DesertNovaBlock(properties
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .noCollision()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.GRASS)));

    /** O <b>Elo Partido</b>: a marca que a rede deixa onde um caminho se rompeu. Não se vê e não se parte. */
    public static final Block BROKEN_POWER_LINK = register("broken_power_link", properties ->
            new net.thaumcraft.arcana.machine.BrokenPowerLinkBlock(properties
                    .mapColor(MapColor.NONE)
                    .noCollision()
                    .noOcclusion()
                    .noLootTable()
                    .strength(-1.0f, 3600000.0f)
                    .pushReaction(PushReaction.DESTROY)));

    /** O <b>Conduto de Essência</b>: o degrau que leva a rede mais dez casas adiante. */
    public static final Block ESSENCE_CONDUIT = register("essence_conduit", properties ->
            new net.thaumcraft.arcana.machine.EssenceConduitBlock(properties
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final BlockEntityType<net.thaumcraft.arcana.machine.EssenceConduitBlockEntity> ESSENCE_CONDUIT_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("essence_conduit"),
                    new BlockEntityType<>(net.thaumcraft.arcana.machine.EssenceConduitBlockEntity::new, Set.of(ESSENCE_CONDUIT)));

    /** A <b>Bateria de Mana</b>: o depósito da rede, e o único. */
    public static final Block MANA_BATTERY = register("mana_battery", properties ->
            new net.thaumcraft.arcana.machine.ManaBatteryBlock(properties
                    .mapColor(MapColor.METAL)
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.METAL)));

    public static final BlockEntityType<net.thaumcraft.arcana.machine.ManaBatteryBlockEntity> MANA_BATTERY_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("mana_battery"),
                    new BlockEntityType<>(net.thaumcraft.arcana.machine.ManaBatteryBlockEntity::new, Set.of(MANA_BATTERY)));

    /**
     * O <b>Obelisco</b>: a primeira fonte de Etherium, que queima pó de vinteum e dá Neutro.
     *
     * <p>Dois blocos de altura num lugar só, e acende — luz <b>onze</b>, que é o 0,73 do original.
     */
    public static final Block OBELISK = register("obelisk", properties ->
            new net.thaumcraft.arcana.machine.ObeliskBlock(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(estado -> 11)));

    public static final BlockEntityType<net.thaumcraft.arcana.machine.ObeliskBlockEntity> OBELISK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("obelisk"),
                    new BlockEntityType<>(net.thaumcraft.arcana.machine.ObeliskBlockEntity::new, Set.of(OBELISK)));

    /** O <b>Aurem Negro</b>: a fonte que come vida e dá Trevas. Sem corpo — atravessa-se. */
    public static final Block BLACK_AUREM = register("black_aurem", properties ->
            new net.thaumcraft.arcana.machine.BlackAuremBlock(properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noCollision()
                    .lightLevel(estado -> 11)));

    public static final BlockEntityType<net.thaumcraft.arcana.machine.BlackAuremBlockEntity> BLACK_AUREM_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("black_aurem"),
                    new BlockEntityType<>(net.thaumcraft.arcana.machine.BlackAuremBlockEntity::new, Set.of(BLACK_AUREM)));

    /** O <b>Prisma Celeste</b>: a fonte que bebe do céu e dá Luz. */
    public static final Block CELESTIAL_PRISM = register("celestial_prism", properties ->
            new net.thaumcraft.arcana.machine.CelestialPrismBlock(properties
                    .mapColor(MapColor.QUARTZ)
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(estado -> 11)));

    public static final BlockEntityType<net.thaumcraft.arcana.machine.CelestialPrismBlockEntity> CELESTIAL_PRISM_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Thaumcraft.id("celestial_prism"),
                    new BlockEntityType<>(net.thaumcraft.arcana.machine.CelestialPrismBlockEntity::new, Set.of(CELESTIAL_PRISM)));

    /**
     * Os <b>cinco minérios</b> do ramo: o {@code BlockAMOre} da 1.4.0.009, que eram cinco metadados de um
     * bloco só.
     *
     * <p>Dureza <b>3</b>, resistência <b>3</b>, picareta de <b>ferro</b> — o {@code getHarvestLevel} do
     * original devolve 2 para todos, sem olhar para o metadado.
     *
     * <p><b>Nenhum deles dá experiência.</b> O {@code BlockOre} de 2014 só dava a quem estivesse na lista
     * dele — carvão, diamante, lápis, esmeralda, quartzo —, e um minério de outro mod não estava. Quem
     * quebra um destes vê o que o original mostrava: nada de bolinhas verdes.
     */
    public static final Map<String, Block> ORES = new LinkedHashMap<>();

    /**
     * Os <b>cinco blocos de guardar</b>, que no original eram os metadados 5 a 9 do mesmo bloco.
     *
     * <p>Nove pedras fazem um bloco e um bloco desfaz-se em nove, como todo bloco de guardar do jogo.
     */
    public static final Map<String, Block> MATERIAL_BLOCKS = new LinkedHashMap<>();

    static {
        // a ordem é a dos metadados do original: vinteum, quimerita, topázio azul, pedra-da-lua, pedra-do-sol
        for (String qual : new String[]{"vinteum", "chimerite", "blue_topaz", "moonstone", "sunstone"}) {
            ORES.put(qual, register(qual + "_ore", properties -> new Block(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))));
            MATERIAL_BLOCKS.put(qual, register(qual + "_block", properties -> new Block(properties
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))));
        }
    }

    private ArcanaBlocks() {
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Thaumcraft.id(name);
        Block block = factory.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela os blocos a se registrarem. */
    public static void init() {
    }
}
