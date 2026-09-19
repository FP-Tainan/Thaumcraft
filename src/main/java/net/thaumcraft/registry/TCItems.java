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
            TCResources.ALL.put(name, register(name, name.equals("knowledge_fragment")
                    ? net.thaumcraft.item.KnowledgeFragmentItem::new
                    : name.equals("primal_charm") ? net.thaumcraft.item.PrimalCharmItem::new : Item::new));
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
        register("cinnabar_ore", properties -> new net.minecraft.world.item.BlockItem(TCBlocks.CINNABAR_ORE, properties.useBlockDescriptionPrefix()));
        register("amber_ore", properties -> new net.minecraft.world.item.BlockItem(TCBlocks.AMBER_ORE, properties.useBlockDescriptionPrefix()));
        for (var entry : TCBlocks.CRYSTAL_CLUSTERS.entrySet()) {
            register("crystal_cluster_" + entry.getKey(), properties ->
                    new net.minecraft.world.item.BlockItem(entry.getValue(), properties.useBlockDescriptionPrefix()));
        }
        for (var entry : TCBlocks.TALLOW_CANDLES.entrySet()) {
            register(entry.getKey() + "_tallow_candle", properties ->
                    new net.minecraft.world.item.BlockItem(entry.getValue(), properties.useBlockDescriptionPrefix()));
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
            new net.thaumcraft.item.GogglesItem(properties.humanoidArmor(net.thaumcraft.item.TCMaterials.GOGGLES,
                    net.minecraft.world.item.equipment.ArmorType.HELMET).rarity(net.minecraft.world.item.Rarity.RARE)));

    /** A bolsa de focos: dezoito focos, e a tecla de trocar foco procura dentro dela. */
    public static final Item FOCUS_POUCH = register("focus_pouch", properties ->
            new net.thaumcraft.item.FocusPouchItem(properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));

    /** As peças sem magia do ItemBaubleBlanks: amuleto, anel e cinto comuns. */
    public static final Item MUNDANE_AMULET = blank("mundane_amulet", net.thaumcraft.api.baubles.BaubleType.AMULET, null);
    public static final Item MUNDANE_RING = blank("mundane_ring", net.thaumcraft.api.baubles.BaubleType.RING, null);
    public static final Item MUNDANE_BELT = blank("mundane_belt", net.thaumcraft.api.baubles.BaubleType.BELT, null);

    /** Os anéis de aprendiz, um por primário: um por cento de desconto naquele aspecto. */
    public static final java.util.Map<String, Item> APPRENTICE_RINGS = new java.util.LinkedHashMap<>();

    static {
        String[] names = {"air", "earth", "fire", "water", "order", "entropy"};
        var primals = net.thaumcraft.api.aspects.Aspects.primals();
        for (int i = 0; i < names.length; i++) {
            APPRENTICE_RINGS.put(names[i], blank("apprentice_ring_" + names[i], net.thaumcraft.api.baubles.BaubleType.RING, primals.get(i)));
        }
    }

    private static Item blank(String name, net.thaumcraft.api.baubles.BaubleType type, net.thaumcraft.api.aspects.Aspect aspect) {
        return register(name, properties -> new net.thaumcraft.item.BaubleBlankItem(type, aspect, properties.stacksTo(1)
                .rarity(aspect == null ? net.minecraft.world.item.Rarity.COMMON : net.minecraft.world.item.Rarity.UNCOMMON)));
    }

    /** As peças do escudo rúnico: amuleto, anéis e cinto, com as cargas do original. */
    public static final Item RUNIC_AMULET = runic("runic_amulet", net.thaumcraft.api.baubles.BaubleType.AMULET, net.thaumcraft.item.RunicBaubleItem.Kind.PLAIN, 8, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_AMULET_EMERGENCY = runic("runic_amulet_emergency", net.thaumcraft.api.baubles.BaubleType.AMULET, net.thaumcraft.item.RunicBaubleItem.Kind.EMERGENCY, 7, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_RING_LESSER = runic("runic_ring_lesser", net.thaumcraft.api.baubles.BaubleType.RING, net.thaumcraft.item.RunicBaubleItem.Kind.PLAIN, 1, net.minecraft.world.item.Rarity.UNCOMMON);
    public static final Item RUNIC_RING = runic("runic_ring", net.thaumcraft.api.baubles.BaubleType.RING, net.thaumcraft.item.RunicBaubleItem.Kind.PLAIN, 5, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_RING_CHARGED = runic("runic_ring_charged", net.thaumcraft.api.baubles.BaubleType.RING, net.thaumcraft.item.RunicBaubleItem.Kind.CHARGED, 4, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_RING_REGEN = runic("runic_ring_regen", net.thaumcraft.api.baubles.BaubleType.RING, net.thaumcraft.item.RunicBaubleItem.Kind.HEALING, 4, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_GIRDLE = runic("runic_girdle", net.thaumcraft.api.baubles.BaubleType.BELT, net.thaumcraft.item.RunicBaubleItem.Kind.PLAIN, 10, net.minecraft.world.item.Rarity.RARE);
    public static final Item RUNIC_GIRDLE_KINETIC = runic("runic_girdle_kinetic", net.thaumcraft.api.baubles.BaubleType.BELT, net.thaumcraft.item.RunicBaubleItem.Kind.KINETIC, 9, net.minecraft.world.item.Rarity.RARE);

    /** A pedra de vis e o amuleto de vis: guardam vis e passam para a varinha na mão. */
    public static final Item VIS_STONE = register("vis_stone", properties ->
            new net.thaumcraft.item.VisAmuletItem(false, properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final Item VIS_AMULET = register("vis_amulet", properties ->
            new net.thaumcraft.item.VisAmuletItem(true, properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));

    private static Item runic(String name, net.thaumcraft.api.baubles.BaubleType type, net.thaumcraft.item.RunicBaubleItem.Kind kind,
                              int charge, net.minecraft.world.item.Rarity rarity) {
        return register(name, properties -> new net.thaumcraft.item.RunicBaubleItem(type, kind, charge, properties.stacksTo(1).rarity(rarity)));
    }

    /** Os mantos do taumaturgo: pouca proteção, tingíveis, e um pouco de desconto de vis. */
    public static final Item ROBE_CHESTPLATE = robe("robe_chestplate", net.minecraft.world.item.equipment.ArmorType.CHESTPLATE);
    public static final Item ROBE_LEGGINGS = robe("robe_leggings", net.minecraft.world.item.equipment.ArmorType.LEGGINGS);
    public static final Item ROBE_BOOTS = robe("robe_boots", net.minecraft.world.item.equipment.ArmorType.BOOTS);

    /** As sacolas de tesouro: comum, incomum e rara; dezesseis por pilha. */
    public static final Item LOOT_BAG = register("loot_bag", properties ->
            new net.thaumcraft.item.LootBagItem(0, properties.stacksTo(16)));
    public static final Item LOOT_BAG_UNCOMMON = register("loot_bag_uncommon", properties ->
            new net.thaumcraft.item.LootBagItem(1, properties.stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final Item LOOT_BAG_RARE = register("loot_bag_rare", properties ->
            new net.thaumcraft.item.LootBagItem(2, properties.stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));

    /** O cérebro de zumbi: carne de lobo, 4 de fome e 0,2 de saturação, com 80% de chance de fome por 30 segundos. */
    public static final Item ZOMBIE_BRAIN = register("zombie_brain", properties -> new Item(properties.food(
            new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.2f).build(),
            net.minecraft.world.item.component.Consumables.defaultFood().onConsume(
                    new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 600, 0), 0.8f)).build())));

    /** O feijão de mana: um ponto de um aspecto; comido (meio segundo, mesmo sem fome) dá um efeito ao acaso. */
    public static final Item MANA_BEAN = register("mana_bean", properties -> new net.thaumcraft.item.ManaBeanItem(properties.food(
            new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(0.5f).alwaysEdible().build(),
            net.minecraft.world.item.component.Consumables.defaultFood().consumeSeconds(0.5f).build())));

    /** A porta arcana, a placa de pressão arcana e o ouvido arcano, para levar na mão. */
    public static final Item ARCANE_DOOR = register("arcane_door", properties ->
            new net.minecraft.world.item.DoubleHighBlockItem(TCBlocks.ARCANE_DOOR, properties.stacksTo(1)));
    public static final Item ARCANE_PRESSURE_PLATE = register("arcane_pressure_plate", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_PRESSURE_PLATE, properties.useBlockDescriptionPrefix()));
    public static final Item ARCANE_EAR = register("arcane_ear", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_EAR, properties.useBlockDescriptionPrefix()));
    public static final Item GOLEM_FETTER = register("golem_fetter", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GOLEM_FETTER, properties.useBlockDescriptionPrefix()));

    public static final Item WARDED_GLASS = register("warded_glass", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.WARDED_GLASS, properties.useBlockDescriptionPrefix()));

    public static final Item MIRROR = register("mirror", properties ->
            new net.thaumcraft.item.MirrorItem((net.thaumcraft.block.MirrorBlock) TCBlocks.MIRROR, properties.useBlockDescriptionPrefix()
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final Item ESSENTIA_MIRROR = register("essentia_mirror", properties ->
            new net.thaumcraft.item.MirrorItem((net.thaumcraft.block.MirrorBlock) TCBlocks.ESSENTIA_MIRROR, properties.useBlockDescriptionPrefix()
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /** O espelho mágico de mão: manda o que se põe nele para o espelho ligado. */
    public static final Item HAND_MIRROR = register("hand_mirror", properties ->
            new net.thaumcraft.item.HandMirrorItem(properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final Item ADVANCED_ALCHEMICAL_CONSTRUCT = register("advanced_alchemical_construct", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT, properties.useBlockDescriptionPrefix()));

    public static final Item ESSENTIA_RESERVOIR = register("essentia_reservoir", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ESSENTIA_RESERVOIR, properties.useBlockDescriptionPrefix()));

    public static final Item MNEMONIC_MATRIX = register("mnemonic_matrix", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.MNEMONIC_MATRIX, properties.useBlockDescriptionPrefix()));

    public static final Item ITEM_GRATE = register("item_grate", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ITEM_GRATE, properties.useBlockDescriptionPrefix()));

    /** As chaves arcanas: a de ferro abre, a de ouro também grava chaves e mexe na placa. */
    public static final Item IRON_KEY = register("iron_key", properties ->
            new net.thaumcraft.item.KeyItem(0, properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final Item GOLD_KEY = register("gold_key", properties ->
            new net.thaumcraft.item.KeyItem(1, properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /** O cogumelo-vis, para levar na mão. */
    public static final Item VISHROOM = register("vishroom", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.VISHROOM, properties.useBlockDescriptionPrefix()));

    /** A essência etérea: o que sobra do fogo-fátuo, com dois pontos do aspecto dele. */
    public static final Item WISP_ESSENCE = register("wisp_essence", net.thaumcraft.item.WispEssenceItem::new);

    /** Os ovos das criaturas, nas cores do ItemSpawnerEgg do original. */
    public static final Item BRAINY_ZOMBIE_SPAWN_EGG = register("brainy_zombie_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.BRAINY_ZOMBIE)));
    public static final Item GIANT_BRAINY_ZOMBIE_SPAWN_EGG = register("giant_brainy_zombie_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.GIANT_BRAINY_ZOMBIE)));
    public static final Item WISP_SPAWN_EGG = register("wisp_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.WISP)));
    public static final Item FIREBAT_SPAWN_EGG = register("firebat_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.FIREBAT)));
    public static final Item PECH_SPAWN_EGG = register("pech_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.PECH)));
    public static final Item THAUMIC_SLIME_SPAWN_EGG = register("thaumic_slime_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.THAUMIC_SLIME)));
    public static final Item TAINT_SPIDER_SPAWN_EGG = register("taint_spider_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_SPIDER)));
    public static final Item TAINTACLE_SPAWN_EGG = register("taintacle_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINTACLE)));
    public static final Item TAINTACLE_SMALL_SPAWN_EGG = register("taintacle_small_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINTACLE_SMALL)));
    public static final Item TAINT_SPORE_SPAWN_EGG = register("taint_spore_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_SPORE)));
    public static final Item TAINT_SPORE_SWARMER_SPAWN_EGG = register("taint_spore_swarmer_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_SPORE_SWARMER)));
    public static final Item TAINT_SWARM_SPAWN_EGG = register("taint_swarm_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_SWARM)));
    public static final Item TAINT_CHICKEN_SPAWN_EGG = register("taint_chicken_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_CHICKEN)));
    public static final Item TAINT_COW_SPAWN_EGG = register("taint_cow_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_COW)));
    public static final Item TAINT_CREEPER_SPAWN_EGG = register("taint_creeper_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_CREEPER)));
    public static final Item TAINT_PIG_SPAWN_EGG = register("taint_pig_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_PIG)));
    public static final Item TAINT_SHEEP_SPAWN_EGG = register("taint_sheep_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_SHEEP)));
    public static final Item TAINT_VILLAGER_SPAWN_EGG = register("taint_villager_spawn_egg", properties ->
            new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(TCEntities.TAINT_VILLAGER)));

    /** A garrafa de mácula, de oito em oito, que se arremessa. */
    public static final Item BOTTLE_TAINT = register("bottle_taint", properties ->
            new net.thaumcraft.item.BottleTaintItem(properties.stacksTo(8)));

    /** O arreio taumostático: voa com Potentia; 400 de durabilidade e conserta com ouro. */
    public static final Item HOVER_HARNESS = register("hover_harness", properties ->
            new net.thaumcraft.item.HoverHarnessItem(properties.humanoidArmor(net.thaumcraft.item.TCMaterials.HARNESS,
                    net.minecraft.world.item.equipment.ArmorType.CHESTPLATE).durability(400).rarity(net.minecraft.world.item.Rarity.EPIC)));

    /** O cinturão taumostático: amortece a queda e ajuda o arreio. */
    public static final Item HOVER_GIRDLE = register("hover_girdle", properties ->
            new net.thaumcraft.item.HoverGirdleItem(properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));

    /** As botas do viajante: correm mais, sobem um bloco e amortecem a queda. */
    public static final Item TRAVELLER_BOOTS = register("traveller_boots", properties ->
            new net.thaumcraft.item.TravellerBootsItem(properties
                    .humanoidArmor(net.thaumcraft.item.TCMaterials.TRAVELLER, net.minecraft.world.item.equipment.ArmorType.BOOTS)
                    .durability(350).rarity(net.minecraft.world.item.Rarity.RARE)
                    .component(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                            net.thaumcraft.item.TCMaterials.TRAVELLER.createAttributes(net.minecraft.world.item.equipment.ArmorType.BOOTS)
                                    .withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(Thaumcraft.id("traveller_step"), 0.4,
                                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                            net.minecraft.world.entity.EquipmentSlotGroup.FEET))));

    /** A armadura de fortaleza de táumio: o elmo, a couraça e as grevas. */
    public static final Item FORTRESS_HELMET = fortress("fortress_helmet", net.minecraft.world.item.equipment.ArmorType.HELMET);
    public static final Item FORTRESS_CHESTPLATE = fortress("fortress_chestplate", net.minecraft.world.item.equipment.ArmorType.CHESTPLATE);
    public static final Item FORTRESS_LEGGINGS = fortress("fortress_leggings", net.minecraft.world.item.equipment.ArmorType.LEGGINGS);

    private static Item fortress(String name, net.minecraft.world.item.equipment.ArmorType type) {
        return register(name, properties -> new net.thaumcraft.item.FortressArmorItem(properties
                .humanoidArmor(net.thaumcraft.item.TCMaterials.FORTRESS, type).rarity(net.minecraft.world.item.Rarity.RARE)));
    }

    private static Item robe(String name, net.minecraft.world.item.equipment.ArmorType type) {
        return register(name, properties -> new net.thaumcraft.item.RobeItem(type, properties
                .humanoidArmor(net.thaumcraft.item.TCMaterials.ROBES, type).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    }

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
        // o de proteção: terra 25, ordo 25 e aqua 10 por bloco protegido
        FOCI.put("warding", register("focus_warding", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "warding",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.EARTH, 25)
                        .add(net.thaumcraft.api.aspects.Aspects.ORDER, 25)
                        .add(net.thaumcraft.api.aspects.Aspects.WATER, 10), false)));
        // o primordial: o custo de verdade é sorteado a cada disparo, de 50 a 250 de cada primário; aqui fica
        // o piso, que é o que se confere antes de atirar
        // o dos Nove Infernos: ignis 2, perditio 1 e aer 1 por morcego, um por segundo
        FOCI.put("hellbat", register("focus_hellbat", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "hellbat",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.FIRE, 200)
                        .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 100)
                        .add(net.thaumcraft.api.aspects.Aspects.AIR, 100), false)));
        // o dos pechs: terra, perditio e aqua 10 por rajada, quatro por segundo
        FOCI.put("pech", register("focus_pech", properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1), "pech",
                new net.thaumcraft.api.aspects.AspectList()
                        .add(net.thaumcraft.api.aspects.Aspects.EARTH, 10)
                        .add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 10)
                        .add(net.thaumcraft.api.aspects.Aspects.WATER, 10), false)));
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
                    new net.thaumcraft.item.GolemPlacerItem(properties.stacksTo(1), material)));
        }
        for (int i = 0; i < net.thaumcraft.api.golems.GolemTypes.CORES.length; i++) {
            final int index = i;
            String core = net.thaumcraft.api.golems.GolemTypes.CORES[i];
            GOLEM_CORES.put(core, register("golem_core_" + core, properties ->
                    new net.thaumcraft.item.GolemCoreItem(properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON), index)));
        }
    }

    /** O núcleo em branco: o disco de barro sem serviço nenhum, de onde saem todos os outros. */
    public static final Item GOLEM_CORE_BLANK = register("golem_core_blank", properties ->
            new net.thaumcraft.item.GolemCoreItem(properties, net.thaumcraft.item.GolemCoreItem.BLANK));

    /** As melhorias de golem, na ordem do original: ar, terra, fogo, água, ordem e entropia. */
    public static final java.util.List<Item> GOLEM_UPGRADES = new java.util.ArrayList<>();

    /** Os acessórios de golem, na ordem do original. */
    public static final java.util.List<Item> GOLEM_DECORATIONS = new java.util.ArrayList<>();

    static {
        for (int i = 0; i < net.thaumcraft.item.GolemUpgradeItem.NAMES.length; i++) {
            final int index = i;
            GOLEM_UPGRADES.add(register("golem_upgrade_" + net.thaumcraft.item.GolemUpgradeItem.NAMES[i], properties ->
                    new net.thaumcraft.item.GolemUpgradeItem(properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON), index)));
        }
        for (int i = 0; i < net.thaumcraft.item.GolemDecorationItem.NAMES.length; i++) {
            final int index = i;
            GOLEM_DECORATIONS.add(register("golem_decoration_" + net.thaumcraft.item.GolemDecorationItem.NAMES[i], properties ->
                    new net.thaumcraft.item.GolemDecorationItem(properties, index)));
        }
    }

    /** O baú itinerante guardado. */
    public static final Item TRUNK_SPAWNER = register("trunk_spawner", properties ->
            new net.thaumcraft.item.TrunkSpawnerItem(properties.stacksTo(1)));

    /** O sino do golem: com ele se diz ao golem para onde levar o que junta. */
    public static final Item GOLEM_BELL = register("golem_bell", properties ->
            new net.thaumcraft.item.GolemBellItem(properties.stacksTo(1)));

    /** O frasco de essência, que guarda um aspecto. */
    public static final Item PHIAL = register("phial", properties ->
            new net.thaumcraft.item.PhialItem(properties.stacksTo(16)));

    /** O jarro lacrado, para levar na mão. */
    public static final Item JAR = register("jar", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.JAR, properties));

    public static final Item BRAIN_JAR = register("brain_jar", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.BRAIN_JAR, properties.useBlockDescriptionPrefix()));
    public static final Item NODE_JAR = registerHidden("node_jar", properties ->
            new net.thaumcraft.item.NodeJarItem(TCBlocks.NODE_JAR, properties.useBlockDescriptionPrefix().stacksTo(1)));

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

    public static final Item TUBE_FILTER = register("tube_filter", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_FILTER, properties));

    public static final Item TUBE_ONEWAY = register("tube_oneway", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_ONEWAY, properties));

    public static final Item TUBE_BUFFER = register("tube_buffer", properties ->
            new net.thaumcraft.item.TubeItem(TCBlocks.TUBE_BUFFER, properties));

    /** A rede de vis: estabilizadores, transdutor, relé e carregador. */
    public static final Item NODE_STABILIZER = register("node_stabilizer", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.NODE_STABILIZER, properties.useBlockDescriptionPrefix()));
    public static final Item NODE_STABILIZER_ADVANCED = register("node_stabilizer_advanced", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.NODE_STABILIZER_ADVANCED, properties.useBlockDescriptionPrefix()));
    public static final Item NODE_CONVERTER = register("node_converter", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.NODE_CONVERTER, properties.useBlockDescriptionPrefix()));
    public static final Item VIS_RELAY = register("vis_relay", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.VIS_RELAY, properties.useBlockDescriptionPrefix()));
    public static final Item WORKBENCH_CHARGER = register("workbench_charger", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.WORKBENCH_CHARGER, properties.useBlockDescriptionPrefix()));

    public static final Item CENTRIFUGE = register("centrifuge", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.CENTRIFUGE, properties.useBlockDescriptionPrefix()));

    public static final Item ESSENTIA_CRYSTALIZER = register("essentia_crystalizer", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ESSENTIA_CRYSTALIZER, properties.useBlockDescriptionPrefix()));

    public static final Item AMBER_BLOCK = register("amber_block", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.AMBER_BLOCK, properties.useBlockDescriptionPrefix()));

    public static final Item AMBER_BRICKS = register("amber_bricks", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.AMBER_BRICKS, properties.useBlockDescriptionPrefix()));

    public static final Item HUNGRY_CHEST = register("hungry_chest", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.HUNGRY_CHEST, properties.useBlockDescriptionPrefix()));

    public static final Item LEVITATOR = register("levitator", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.LEVITATOR, properties.useBlockDescriptionPrefix()));

    public static final Item ARCANE_LAMP = register("arcane_lamp", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_LAMP, properties.useBlockDescriptionPrefix()));

    public static final Item GROWTH_LAMP = register("growth_lamp", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GROWTH_LAMP, properties.useBlockDescriptionPrefix()));

    public static final Item FERTILITY_LAMP = register("fertility_lamp", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.FERTILITY_LAMP, properties.useBlockDescriptionPrefix()));

    public static final Item ALCHEMICAL_CONSTRUCT = register("alchemical_construct", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ALCHEMICAL_CONSTRUCT, properties.useBlockDescriptionPrefix()));

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

    /** A mesa de desconstrução. */
    public static final Item DECONSTRUCTION_TABLE = register("deconstruction_table", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.DECONSTRUCTION_TABLE, properties.useBlockDescriptionPrefix()));

    public static final Item FOCAL_MANIPULATOR = register("focal_manipulator", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.FOCAL_MANIPULATOR, properties.useBlockDescriptionPrefix()));

    public static final Item WAND_PEDESTAL = register("wand_pedestal", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.WAND_PEDESTAL, properties.useBlockDescriptionPrefix()));
    public static final Item RECHARGE_FOCUS = register("recharge_focus", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.RECHARGE_FOCUS, properties.useBlockDescriptionPrefix()));

    public static final Item ARCANE_SPA = register("arcane_spa", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_SPA, properties.useBlockDescriptionPrefix()));
    /** Os sais de banho, que viram fluido purificante na água. */
    public static final Item BATH_SALTS = register("bath_salts", properties ->
            new net.minecraft.world.item.Item(properties));
    /** Os baldes dos dois fluidos. */
    public static final Item BUCKET_PURE = register("bucket_pure", properties ->
            new net.minecraft.world.item.BucketItem(TCFluids.PURIFYING, properties
                    .craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final Item BUCKET_DEATH = register("bucket_death", properties ->
            new net.minecraft.world.item.BucketItem(TCFluids.DEATH, properties
                    .craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));

    /** O fole. */
    public static final Item BELLOWS = register("bellows", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.BELLOWS, properties.useBlockDescriptionPrefix()));

    public static final Item ARCANE_BORE_BASE = register("arcane_bore_base", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_BORE_BASE, properties.useBlockDescriptionPrefix()));
    public static final Item ARCANE_BORE = register("arcane_bore", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_BORE, properties.useBlockDescriptionPrefix()));

    // as escadas e a laje de pedra arcana
    public static final Item ARCANE_STONE_STAIRS = register("arcane_stone_stairs", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_STONE_STAIRS, properties.useBlockDescriptionPrefix()));
    public static final Item ARCANE_STONE_SLAB = register("arcane_stone_slab", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.ARCANE_STONE_SLAB, properties.useBlockDescriptionPrefix()));

    // as árvores mágicas: toras, folhas, mudas, tábuas, escadas e lajes
    public static final Item GREATWOOD_LOG = register("greatwood_log", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_LOG, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_LOG = register("silverwood_log", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_LOG, properties.useBlockDescriptionPrefix()));
    public static final Item GREATWOOD_LEAVES = register("greatwood_leaves", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_LEAVES, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_LEAVES = register("silverwood_leaves", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_LEAVES, properties.useBlockDescriptionPrefix()));
    public static final Item GREATWOOD_SAPLING = register("greatwood_sapling", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_SAPLING, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_SAPLING = register("silverwood_sapling", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_SAPLING, properties.useBlockDescriptionPrefix()));
    public static final Item GREATWOOD_PLANKS = register("greatwood_planks", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_PLANKS, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_PLANKS = register("silverwood_planks", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_PLANKS, properties.useBlockDescriptionPrefix()));
    public static final Item GREATWOOD_STAIRS = register("greatwood_stairs", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_STAIRS, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_STAIRS = register("silverwood_stairs", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_STAIRS, properties.useBlockDescriptionPrefix()));
    public static final Item GREATWOOD_SLAB = register("greatwood_slab", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.GREATWOOD_SLAB, properties.useBlockDescriptionPrefix()));
    public static final Item SILVERWOOD_SLAB = register("silverwood_slab", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SILVERWOOD_SLAB, properties.useBlockDescriptionPrefix()));

    /** A pérola de cinzas, para levar na mão. */
    public static final Item CINDERPEARL = register("cinderpearl", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.CINDERPEARL, properties.useBlockDescriptionPrefix()));

    /** A folha-cintilante, para levar na mão. */
    public static final Item SHIMMERLEAF = register("shimmerleaf", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.SHIMMERLEAF, properties));

    /** A mácula e a flor que a desfaz, para levar na mão. */
    public static final Item TAINT_CRUST = register("taint_crust", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TAINT_CRUST, properties));

    public static final Item TAINT_SOIL = register("taint_soil", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.TAINT_SOIL, properties));

    public static final Item FLESH_BLOCK = register("flesh_block", properties ->
            new net.minecraft.world.item.BlockItem(TCBlocks.FLESH_BLOCK, properties));

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

    /** O carvão da alquimia, que queima muito mais que o comum e, arremessado, explode. */
    public static final Item ALUMENTUM = register("alumentum", net.thaumcraft.item.AlumentumItem::new);

    /** Um ponto de essência preso num cristal: o que sai do cristalizador. */
    public static final Item CRYSTAL_ESSENCE = register("crystal_essence", net.thaumcraft.item.CrystalEssenceItem::new);

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
            "thaumometer", "thaumonomicon", "goggles", "robe_chestplate", "robe_leggings", "robe_boots", "hover_harness", "traveller_boots", "fortress_helmet", "fortress_chestplate", "fortress_leggings",
            "wand", "staff", "focus_fire", "focus_excavation", "focus_frost", "focus_shock", "focus_portable_hole", "focus_trade", "focus_warding", "focus_hellbat", "focus_pech", "focus_primal", "focus_pouch", "mundane_amulet", "mundane_ring", "mundane_belt", "apprentice_ring_air", "apprentice_ring_earth", "apprentice_ring_fire", "apprentice_ring_water", "apprentice_ring_order", "apprentice_ring_entropy", "vis_stone", "vis_amulet", "runic_amulet", "runic_amulet_emergency", "runic_ring_lesser", "runic_ring", "runic_ring_charged", "runic_ring_regen", "runic_girdle", "runic_girdle_kinetic", "hover_girdle",
            // as pontas na ordem da aba do original, cada inerte logo depois da sua
            "wand_cap_iron", "wand_cap_gold", "wand_cap_copper", "wand_cap_silver", "wand_cap_silver_inert",
            "wand_cap_thaumium", "wand_cap_thaumium_inert", "wand_cap_void", "wand_cap_void_inert",
            "wand_rod_greatwood", "wand_rod_obsidian", "wand_rod_silverwood", "wand_rod_ice",
            "wand_rod_quartz", "wand_rod_reed", "wand_rod_blaze", "wand_rod_bone",
            "staff_rod_greatwood", "staff_rod_obsidian", "staff_rod_silverwood", "staff_rod_ice",
            "staff_rod_quartz", "staff_rod_reed", "staff_rod_blaze", "staff_rod_bone", "staff_rod_primal",
            "shard_air", "shard_fire", "shard_water", "shard_earth", "shard_order", "shard_entropy",
            "shard_balanced", "salis_mundus", "phial", "crystal_essence", "wisp_essence", "zombie_brain", "mana_bean",
            "thaumium_ingot", "thaumium_nugget", "void_ingot", "void_nugget", "quicksilver", "magic_tallow", "amber", "enchanted_fabric",
            "vis_filter", "knowledge_fragment", "mirrored_glass", "jar_label", "primal_charm", "gold_coin",
            "alumentum", "nitor", "iron_key", "gold_key", "loot_bag", "loot_bag_uncommon", "loot_bag_rare",
            "thaumium_pickaxe", "thaumium_axe", "thaumium_shovel", "thaumium_hoe", "thaumium_sword",
            "thaumium_helmet", "thaumium_chestplate", "thaumium_leggings", "thaumium_boots",
            "void_pickaxe", "void_axe", "void_shovel", "void_hoe", "void_sword",
            "void_helmet", "void_chestplate", "void_leggings", "void_boots",
            "scribing_tools", "table", "crucible", "arcane_workbench", "deconstruction_table", "alchemical_furnace", "bellows", "alembic", "hungry_chest", "levitator", "arcane_door", "arcane_pressure_plate", "arcane_ear", "warded_glass", "mirror", "essentia_mirror", "hand_mirror", "arcane_lamp", "growth_lamp", "fertility_lamp", "alchemical_construct", "advanced_alchemical_construct", "jar", "jar_void", "tube", "tube_valve", "tube_restrict", "tube_filter", "tube_oneway", "tube_buffer", "centrifuge", "essentia_crystalizer", "essentia_reservoir", "mnemonic_matrix", "item_grate", "node_stabilizer", "node_stabilizer_advanced", "node_converter", "vis_relay", "workbench_charger",
            "infusion_matrix", "pedestal",
            "greatwood_log", "silverwood_log", "greatwood_planks", "silverwood_planks", "greatwood_stairs",
            "silverwood_stairs", "greatwood_slab", "silverwood_slab", "greatwood_leaves", "silverwood_leaves",
            "greatwood_sapling", "silverwood_sapling",
            "shimmerleaf", "cinderpearl", "vishroom", "ethereal_bloom", "taint_crust", "taint_soil", "taint_fibres",
            "golem_bell",
            "golem_straw", "golem_wood", "golem_tallow", "golem_clay",
            "golem_flesh", "golem_stone", "golem_iron", "golem_thaumium",
            "golem_core_blank", "golem_core_fill", "golem_core_empty", "golem_core_gather", "golem_core_harvest",
            "golem_core_guard", "golem_core_decanting", "golem_core_alchemy", "golem_core_chop",
            "golem_core_use", "golem_core_butcher", "golem_core_sorting", "golem_core_fishing",
            "golem_upgrade_air", "golem_upgrade_earth", "golem_upgrade_fire", "golem_upgrade_water",
            "golem_upgrade_order", "golem_upgrade_entropy",
            "golem_decoration_tophat", "golem_decoration_glasses", "golem_decoration_bowtie", "golem_decoration_fez",
            "golem_decoration_dart", "golem_decoration_visor", "golem_decoration_armor", "golem_decoration_mace",
            "golem_fetter", "trunk_spawner",
            "arcane_stone", "arcane_stone_bricks", "arcane_stone_stairs", "arcane_stone_slab", "thaumium_block", "tallow_block",
            "amber_block", "amber_bricks",
            "paving_stone_travel", "paving_stone_warding",
            "infused_stone_air", "infused_stone_fire", "infused_stone_water",
            "infused_stone_earth", "infused_stone_order", "infused_stone_entropy",
            "cinnabar_ore", "amber_ore",
            "crystal_cluster_air", "crystal_cluster_fire", "crystal_cluster_water", "crystal_cluster_earth",
            "crystal_cluster_order", "crystal_cluster_entropy", "crystal_cluster_balanced",
            "white_tallow_candle", "orange_tallow_candle", "magenta_tallow_candle", "light_blue_tallow_candle", "yellow_tallow_candle",
            "lime_tallow_candle", "pink_tallow_candle", "gray_tallow_candle", "light_gray_tallow_candle", "cyan_tallow_candle",
            "purple_tallow_candle", "blue_tallow_candle", "brown_tallow_candle", "green_tallow_candle", "red_tallow_candle", "black_tallow_candle",
            "brainy_zombie_spawn_egg", "giant_brainy_zombie_spawn_egg", "wisp_spawn_egg", "firebat_spawn_egg", "pech_spawn_egg",
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
                .displayItems((parameters, output) -> displayOrder().forEach(item -> {
                    // a essência etérea vem uma de cada aspecto, como no getSubItems do original
                    if (item == WISP_ESSENCE) net.thaumcraft.item.WispEssenceItem.variants().forEach(output::accept);
                    else output.accept(item);
                }))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
