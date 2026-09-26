package net.thaumcraft.shattered;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;

/** As coisas dos Reinos Fragmentados, e a aba própria deles no criativo. */
public final class ShatteredItems {
    /** A ordem em que as coisas entram na aba. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("shattered"));

    static {
        for (Block bloco : FabricBlocks.FABRIC.values()) item(bloco);
        for (Block bloco : FabricBlocks.ANCIENT.values()) item(bloco);
        item(FabricBlocks.ETERNAL);
        item(FabricBlocks.UNRAVELLED);
        for (Block porta : ShatteredBlocks.doors()) door(porta);
        item(ShatteredBlocks.MARKING_PLATE);
    }

    /** O Fio do Mundo, que se tira do que o Limbo desfiou. */
    public static final Item WORLD_THREAD = plain("world_thread", new Item.Properties());

    /** E o Tecido Estável, que se tece dele. */
    public static final Item STABLE_FABRIC = plain("stable_fabric", new Item.Properties());


    /**
     * Os três focos de fenda: abrir, firmar e fechar.
     *
     * <p>Tomaram o lugar do Firma-Fendas e do Fecha-Fendas de mão, que saíram — num mod de Thaumcraft isto é
     * trabalho de varinha, e não de ferro no cinto.
     */
    public static final Item FOCUS_RIFT_OPEN = foco("focus_rift_open", "rift_open", ShatteredFoci.COST_OPEN);
    public static final Item FOCUS_RIFT_HOLD = foco("focus_rift_hold", "rift_hold", ShatteredFoci.COST_HOLD);
    public static final Item FOCUS_RIFT_CLOSE = foco("focus_rift_close", "rift_close", ShatteredFoci.COST_CLOSE);

    /**
     * Os Óculos do Véu: os da Descoberta com Fio do Mundo, e com eles no rosto as fendas do mundo aparecem.
     *
     * <p>Vão raros, como os outros óculos do mod.
     */
    public static final Item VEIL_GOGGLES = tool("veil_goggles", properties ->
            new VeilGogglesItem(properties.humanoidArmor(ShatteredMaterials.VEIL_GOGGLES,
                    net.minecraft.world.item.equipment.ArmorType.HELMET)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));

    private ShatteredItems() {
    }

    /** As portas entram na mochila como item de porta, que se põe de pé em duas metades. */
    private static void door(Block bloco) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(bloco);
        var properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
        // as dimensionais sabem onde se podem assentar; as de enfeite são portas como as outras
        ORDER.add(Registry.register(BuiltInRegistries.ITEM, id,
                bloco instanceof DimensionalDoorBlock
                        ? new DimensionalDoorItem(bloco, properties)
                        : new net.minecraft.world.item.DoubleHighBlockItem(bloco, properties)));
    }


    /** Um foco de varinha do ramo, que entra também na lista de focos do mod. */
    private static Item foco(String nome, String tipo, net.thaumcraft.api.aspects.AspectList custo) {
        Item feito = tool(nome, properties -> new net.thaumcraft.item.FocusItem(
                properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), tipo, custo, false));
        net.thaumcraft.registry.TCItems.FOCI.put(tipo, feito);
        return feito;
    }

    /** Uma coisa que não é bloco. */
    private static Item plain(String nome, Item.Properties ignorado) {
        return tool(nome, Item::new);
    }

    private static Item tool(String nome, java.util.function.Function<Item.Properties, Item> fábrica) {
        Identifier id = Thaumcraft.id(nome);
        Item feito = fábrica.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        ORDER.add(Registry.register(BuiltInRegistries.ITEM, id, feito));
        return feito;
    }

    private static void item(Block bloco) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(bloco);
        var properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
        ORDER.add(Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(bloco, properties)));
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    public static int count() {
        return ORDER.size();
    }

    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.shattered"))
                .icon(() -> new ItemStack(FabricBlocks.ETERNAL))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
