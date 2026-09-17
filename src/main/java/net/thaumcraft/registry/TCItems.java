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

    /** O crisol, para levar na mão. */
    public static final Item CRUCIBLE = register("crucible", properties ->
            new net.minecraft.world.item.BlockItem(net.thaumcraft.registry.TCBlocks.CRUCIBLE, properties));

    /** O fragmento equilibrado: o que o crisol faz de seis primários. */
    public static final Item SHARD_BALANCED = register("shard_balanced", Item::new);

    /** O carvão da alquimia, que queima muito mais que o comum. */
    public static final Item ALUMENTUM = register("alumentum", Item::new);

    /** A chama fria que não queima nada. */
    public static final Item NITOR = register("nitor", Item::new);

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

    static {
        for (String tag : WandParts.RODS.keySet()) {
            // a haste de madeira é o graveto do próprio jogo, como no original
            if (tag.equals("wood")) continue;
            WAND_RODS.put(tag, register("wand_rod_" + tag, Item::new));
        }
        for (String tag : WandParts.CAPS.keySet()) {
            WAND_CAPS.put(tag, register("wand_cap_" + tag, Item::new));
        }
    }

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("thaumcraft"));

    private TCItems() {
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft"))
                .icon(() -> new ItemStack(THAUMOMETER))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
