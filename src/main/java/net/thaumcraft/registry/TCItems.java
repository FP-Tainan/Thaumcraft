package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.ThaumometerItem;
import net.thaumcraft.item.ThaumonomiconItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.api.wands.WandParts;

import java.util.ArrayList;
import java.util.List;

/** Os itens do mod, na ordem em que aparecem na aba do criativo. */
public final class TCItems {
    private static final List<Item> ORDER = new ArrayList<>();

    /**
     * O que o mod registra mas não põe na aba do criativo.
     *
     * <p>É a peça de dentro do maquinário: a lasca de gelo, por exemplo, só existe para dar cara ao
     * projétil do foco de gelo, e não é para ninguém carregar no inventário.
     */
    public static final java.util.Set<Item> HIDDEN = new java.util.LinkedHashSet<>();

    /** O primeiro aparelho de todo taumaturgo: com ele se examina o mundo. */
    public static final Item THAUMOMETER = register("thaumometer", properties ->
            new ThaumometerItem(properties.stacksTo(1)));

    /** O livro em que a pesquisa fica anotada. */
    public static final Item THAUMONOMICON = register("thaumonomicon", properties ->
            new ThaumonomiconItem(properties.stacksTo(1)));

    static {
        // a matéria-prima do mod, que só existe para entrar em receita
        for (String name : TCResources.NAMES) {
            TCResources.ALL.put(name, register(name, Item::new));
        }
    }

    /** As ferramentas e armaduras de táumio e de metal do vazio. */
    public static final java.util.Map<String, Item> GEAR = new java.util.LinkedHashMap<>();

    static {
        for (TCGear.Piece piece : TCGear.PIECES) {
            var tool = piece.material().equals("thaumium")
                    ? net.thaumcraft.item.TCMaterials.THAUMIUM
                    : net.thaumcraft.item.TCMaterials.VOID;
            var armor = piece.material().equals("thaumium")
                    ? net.thaumcraft.item.TCMaterials.THAUMIUM_ARMOR
                    : net.thaumcraft.item.TCMaterials.VOID_ARMOR;
            GEAR.put(piece.name(), register(piece.name(), properties -> switch (piece.kind()) {
                case "pickaxe" -> new Item(properties.pickaxe(tool, 1.0f, -2.8f));
                case "axe" -> new Item(properties.axe(tool, 6.0f, -3.1f));
                case "shovel" -> new Item(properties.shovel(tool, 1.5f, -3.0f));
                case "hoe" -> new Item(properties.hoe(tool, -1.0f, -1.0f));
                case "sword" -> new Item(properties.sword(tool, 3.0f, -2.4f));
                case "helmet" -> new Item(properties.humanoidArmor(armor,
                        net.minecraft.world.item.equipment.ArmorType.HELMET));
                case "chestplate" -> new Item(properties.humanoidArmor(armor,
                        net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
                case "leggings" -> new Item(properties.humanoidArmor(armor,
                        net.minecraft.world.item.equipment.ArmorType.LEGGINGS));
                case "boots" -> new Item(properties.humanoidArmor(armor,
                        net.minecraft.world.item.equipment.ArmorType.BOOTS));
                default -> new Item(properties);
            }));
        }
    }

    /** Os fragmentos de aspecto: um por primário, que é o que o original tira do minério infundido. */
    public static final java.util.Map<String, Item> SHARDS = new java.util.LinkedHashMap<>();

    static {
        for (String tag : new String[]{"air", "fire", "water", "earth", "order", "entropy"}) {
            SHARDS.put(tag, register("shard_" + tag, Item::new));
        }
    }

    static {
        // o minério infundido também vai para a aba do criativo
        for (var entry : TCBlocks.INFUSED_STONE.entrySet()) {
            register("infused_stone_" + entry.getKey(), properties ->
                    new net.minecraft.world.item.BlockItem(entry.getValue(), properties));
        }
    }

    static {
        // os blocos de construção também vão para a aba do criativo
        for (var entry : TCBlocks.BUILDING.entrySet()) {
            register(entry.getKey(), properties ->
                    new net.minecraft.world.item.BlockItem(entry.getValue(), properties));
        }
    }

    /** Os Óculos da Revelação: com eles no rosto, os nós de aura aparecem. */
    public static final Item GOGGLES = register("goggles", properties ->
            new Item(properties.humanoidArmor(net.thaumcraft.item.TCMaterials.GOGGLES,
                    net.minecraft.world.item.equipment.ArmorType.HELMET)));

    /** Os focos de varinha, pelo nome que o original dá a cada um. */
    public static final java.util.Map<String, Item> FOCI = new java.util.LinkedHashMap<>();

    static {
        // o de fogo cobra dez centésimos de ignis por tique, como no original
        FOCI.put("fire", register("focus_fire", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "fire",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.FIRE, 10), true)));
        // o de escavação cobra quinze centésimos de terra por bloco quebrado
        FOCI.put("excavation", register("focus_excavation", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "excavation",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.EARTH, 15), true)));
        // o de gelo é tiro único: aqua 5, ignis 2 e perditio 2 por lasca, como no original
        FOCI.put("frost", register("focus_frost", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "frost",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.WATER, 5)
                        .add(net.thaumcraft.api.aspects.Aspects.FIRE, 2)
                        .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 2), false)));
        // o do raio é jato contínuo e caro: aer 25 por tique, como no original
        FOCI.put("shock", register("focus_shock", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "shock",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.AIR, 25), true)));
        // o do buraco portátil: perditio 10 e aer 10 por bloco de profundidade, tiro único
        FOCI.put("portable_hole", register("focus_portable_hole", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "portable_hole",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 10)
                        .add(net.thaumcraft.api.aspects.Aspects.AIR, 10), false)));
        // o da troca equivalente: perditio, terra e ordo 5 por bloco trocado
        FOCI.put("trade", register("focus_trade", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "trade",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 5)
                        .add(net.thaumcraft.api.aspects.Aspects.EARTH, 5)
                        .add(net.thaumcraft.api.aspects.Aspects.ORDER, 5), false)));
        // o primordial: o custo de verdade é sorteado a cada disparo, de 50 a 250 de cada primário; aqui fica
        // o piso, que é o que se confere antes de atirar
        FOCI.put("primal", register("focus_primal", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), "primal",
                net.thaumcraft.item.Focuses.primalCost(0L).copy(), false)));
    }

    /** A lasca de gelo: não é item de verdade, é só a cara do projétil do foco de gelo. */
    public static final Item FROST_SHARD = registerHidden("frost_shard", Item::new);

    /** Os golens guardados na mão, um por matéria. */
    public static final java.util.Map<String, Item> GOLEM_PLACERS = new java.util.LinkedHashMap<>();

    /** Os núcleos de golem, um por serviço. */
    public static final java.util.Map<String, Item> GOLEM_CORES = new java.util.LinkedHashMap<>();

    static {
        for (String material : net.thaumcraft.api.golems.GolemTypes.ALL.keySet()) {
            GOLEM_PLACERS.put(material, register("golem_" + material, properties ->
                    new net.thaumcraft.item.GolemPlacerItem(properties.stacksTo(16), material)));
        }
        for (String core : net.thaumcraft.api.golems.GolemTypes.CORES) {
            GOLEM_CORES.put(core, register("golem_core_" + core, properties ->
                    new net.thaumcraft.item.GolemCoreItem(properties, core)));
        }
    }

    /** O núcleo em branco: o disco de barro sem serviço nenhum, de onde saem todos os outros. */
    public static final Item GOLEM_CORE_BLANK = register("golem_core_blank", Item::new);

    /** O sino do golem: com ele se diz ao golem para onde levar o que junta. */
    public static final Item GOLEM_BELL = register("golem_bell", properties ->
            new net.thaumcraft.item.GolemBellItem(properties.stacksTo(1)));

    /** O frasco de essência, que guarda um aspecto. */
    public static final Item PHIAL = register("phial", properties ->
            new net.thaumcraft.item.PhialItem(properties.stacksTo(16)));

    /** O jarro lacrado, para levar na mão. */
    public static final Item JAR = register("jar", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.JAR, properties));

    public static final Item JAR_VOID = register("jar_void", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.JAR_VOID, properties));

    /** O tubo de essência, para levar na mão. */
    public static final Item TUBE = register("tube", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TUBE, properties));

    /** A válvula do cano. */
    public static final Item TUBE_VALVE = register("tube_valve", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TUBE_VALVE, properties));

    public static final Item TUBE_RESTRICT = register("tube_restrict", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_RESTRICT, properties));

    public static final Item TUBE_ONEWAY = register("tube_oneway", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_ONEWAY, properties));

    public static final Item TUBE_BUFFER = register("tube_buffer", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_BUFFER, properties));

    /** O alambique, para levar na mão. */
    public static final Item ALEMBIC = register("alembic", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ALEMBIC, properties));

    /** O forno alquímico, para levar na mão. */
    public static final Item ALCHEMICAL_FURNACE = register("alchemical_furnace", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ALCHEMICAL_FURNACE, properties));

    /** O pedestal arcano, para levar na mão. */
    public static final Item PEDESTAL = register("pedestal", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.PEDESTAL, properties));

    /** A matriz rúnica, para levar na mão. */
    public static final Item INFUSION_MATRIX = register("infusion_matrix", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.INFUSION_MATRIX, properties));

    /** A folha-cintilante, para levar na mão. */
    public static final Item SHIMMERLEAF = register("shimmerleaf", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SHIMMERLEAF, properties));

    /** A mácula e a flor que a desfaz, para levar na mão. */
    public static final Item TAINT_CRUST = register("taint_crust", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TAINT_CRUST, properties));

    public static final Item TAINT_SOIL = register("taint_soil", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TAINT_SOIL, properties));

    public static final Item TAINT_FIBRES = register("taint_fibres", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TAINT_FIBRES, properties));

    public static final Item ETHEREAL_BLOOM = register("ethereal_bloom", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ETHEREAL_BLOOM, properties));

    /** A bancada arcana, para levar na mão. */
    public static final Item ARCANE_WORKBENCH = register("arcane_workbench", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_WORKBENCH, properties));

    /** O crisol, para levar na mão. */
    public static final Item CRUCIBLE = register("crucible", properties ->
            new net.minecraft.world.item.BlockItem(net.thaumcraft.registry.TCBlocks.CRUCIBLE, properties));

    /** O fragmento equilibrado: o que o crisol faz de seis primários. */
    public static final Item SHARD_BALANCED = register("shard_balanced", Item::new);

    /** O carvão da alquimia, que queima muito mais que o comum. */
    public static final Item ALUMENTUM = register("alumentum", Item::new);

    /** A chama fria que não queima nada. */
    public static final Item NITOR = register("nitor", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.NITOR, properties));

    /** A mesa de madeira do mod. */
    public static final Item TABLE = register("table", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TABLE, properties.useBlockDescriptionPrefix()));

    /** As ferramentas de escrita: pena e tinteiro, com tinta para cem riscos. */
    public static final Item SCRIBING_TOOLS = register("scribing_tools", properties ->
            new net.thaumcraft.item.ScribingToolsItem(properties.durability(100)));

    /** As notas de pesquisa, e a descoberta em que elas viram. Não ficam na aba: saem do livro. */
    public static final Item RESEARCH_NOTES = registerHidden("research_notes", properties ->
            new net.thaumcraft.item.ResearchNotesItem(properties.stacksTo(64)));

    /** A varinha: a haste e as pontas vêm nos dados dela, como no original. */
    public static final Item WAND = register("wand", properties ->
            new WandItem(properties.stacksTo(1)
                    .component(net.thaumcraft.registry.TCComponents.WAND_ROD, "wood")
                    .component(net.thaumcraft.registry.TCComponents.WAND_CAP, "iron"), false));

    /** O bastão: a mesma coisa, com haste maior e mais fôlego. */
    public static final Item STAFF = register("staff", properties ->
            new WandItem(properties.stacksTo(1)
                    .component(net.thaumcraft.registry.TCComponents.WAND_ROD, "greatwood")
                    .component(net.thaumcraft.registry.TCComponents.WAND_CAP, "iron"), true));

    /** As peças soltas: cada haste e cada ponta do original é um item. */
    public static final java.util.Map<String, Item> WAND_RODS = new java.util.LinkedHashMap<>();
    public static final java.util.Map<String, Item> WAND_CAPS = new java.util.LinkedHashMap<>();
    /** As pontas de taumínio, de vazio e de prata antes da infusão. */
    private static final String[] INERT = {"thaumium", "void", "silver"};
    public static final java.util.Map<String, Item> INERT_CAPS = new java.util.LinkedHashMap<>();
    /** Os núcleos de bastão, de que se fazem os bastões. */
    public static final java.util.Map<String, Item> STAFF_RODS = new java.util.LinkedHashMap<>();

    static {
        for (String tag : WandParts.RODS.keySet()) {
            // a haste de madeira é o graveto do próprio jogo, como no original
            if (tag.equals("wood")) continue;
            WAND_RODS.put(tag, register("wand_rod_" + tag, Item::new));
        }
        for (String tag : WandParts.CAPS.keySet()) {
            WAND_CAPS.put(tag, register("wand_cap_" + tag, Item::new));
        }
        // as pontas que saem inertes da bancada e só pegam depois da infusão, como no original
        for (String tag : INERT) {
            INERT_CAPS.put(tag, register("wand_cap_" + tag + "_inert", Item::new));
        }
        // e os núcleos de bastão: um por haste, mais o primordial
        for (String tag : WandParts.STAFF_RODS.keySet()) {
            STAFF_RODS.put(tag, register("staff_rod_" + tag, Item::new));
        }
    }

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("thaumcraft"));

    private TCItems() {
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Item item = raw(name, factory);
        ORDER.add(item);
        return item;
    }

    /**
     * Registra um item que não vai para a aba do criativo.
     *
     * <p>Serve para o que só existe para o jogo funcionar por dentro — a lasca de gelo, por exemplo, que
     * nunca fica na mão de ninguém: ela é só a cara do projétil que o foco de gelo atira.
     */
    private static Item registerHidden(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Item item = raw(name, factory);
        HIDDEN.add(item);
        return item;
    }

    private static Item raw(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    /**
     * A ordem em que as coisas aparecem na aba do criativo.
     *
     * <p>A ordem de registro é acidental — segue a ordem em que as fatias foram entrando. Esta é a ordem
     * que faz sentido para quem abre a aba: primeiro o que se usa para descobrir, depois a varinha e as
     * peças dela, depois a matéria-prima, o equipamento e por fim os blocos.
     */
    private static final String[] SHELF = {
            "thaumometer", "thaumonomicon", "goggles",
            "wand", "staff", "focus_fire", "focus_excavation", "focus_frost", "focus_shock", "focus_portable_hole", "focus_trade", "focus_primal",
            // as pontas na ordem da aba do original, cada inerte logo depois da sua
            "wand_cap_iron", "wand_cap_gold", "wand_cap_copper", "wand_cap_silver", "wand_cap_silver_inert",
            "wand_cap_thaumium", "wand_cap_thaumium_inert", "wand_cap_void", "wand_cap_void_inert",
            "wand_rod_greatwood", "wand_rod_obsidian", "wand_rod_silverwood", "wand_rod_ice",
            "wand_rod_quartz", "wand_rod_reed", "wand_rod_blaze", "wand_rod_bone",
            "staff_rod_greatwood", "staff_rod_obsidian", "staff_rod_silverwood", "staff_rod_ice",
            "staff_rod_quartz", "staff_rod_reed", "staff_rod_blaze", "staff_rod_bone", "staff_rod_primal",
            "shard_air", "shard_fire", "shard_water", "shard_earth", "shard_order", "shard_entropy",
            "shard_balanced", "salis_mundus", "phial",
            "thaumium_ingot", "thaumium_nugget", "void_ingot", "void_nugget", "quicksilver", "magic_tallow", "amber", "enchanted_fabric",
            "vis_filter", "knowledge_fragment", "mirrored_glass", "jar_label", "primal_charm", "gold_coin",
            "alumentum", "nitor",
            "thaumium_pickaxe", "thaumium_axe", "thaumium_shovel", "thaumium_hoe", "thaumium_sword",
            "thaumium_helmet", "thaumium_chestplate", "thaumium_leggings", "thaumium_boots",
            "void_pickaxe", "void_axe", "void_shovel", "void_hoe", "void_sword",
            "void_helmet", "void_chestplate", "void_leggings", "void_boots",
            "scribing_tools", "table", "crucible", "arcane_workbench", "alchemical_furnace", "alembic", "jar", "jar_void", "tube", "tube_valve", "tube_restrict", "tube_oneway", "tube_buffer",
            "infusion_matrix", "pedestal",
            "shimmerleaf", "ethereal_bloom", "taint_crust", "taint_soil", "taint_fibres",
            "golem_bell",
            "golem_straw", "golem_wood", "golem_tallow", "golem_clay",
            "golem_flesh", "golem_stone", "golem_iron", "golem_thaumium",
            "golem_core_fill", "golem_core_empty", "golem_core_gather", "golem_core_harvest",
            "golem_core_guard", "golem_core_decanting", "golem_core_alchemy", "golem_core_chop",
            "golem_core_use", "golem_core_butcher", "golem_core_sorting", "golem_core_fishing",
            "arcane_stone", "thaumium_block", "tallow_block",
            "paving_stone_travel", "paving_stone_warding",
            "infused_stone_air", "infused_stone_fire", "infused_stone_water",
            "infused_stone_earth", "infused_stone_order", "infused_stone_entropy",
    };

    /**
     * O que a aba mostra, na ordem: primeiro o que a prateleira nomeia, e depois o que tiver ficado de
     * fora dela — assim nada some da aba quando uma fatia nova traz peças e alguém esquece de listá-las.
     */
    public static java.util.List<Item> displayOrder() {
        java.util.Set<Item> shown = new java.util.LinkedHashSet<>();
        for (String name : SHELF) {
            Item found = BuiltInRegistries.ITEM.getValue(Thaumcraft.id(name));
            if (found != null && found != net.minecraft.world.item.Items.AIR) shown.add(found);
        }
        shown.addAll(ORDER);
        return java.util.List.copyOf(shown);
    }

    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft"))
                .icon(() -> new ItemStack(THAUMOMETER))
                .displayItems((parameters, output) -> displayOrder().forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
