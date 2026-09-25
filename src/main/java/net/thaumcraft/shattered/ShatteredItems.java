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
    }

    /** O Fio do Mundo, que se tira do que o Limbo desfiou. */
    public static final Item WORLD_THREAD = plain("world_thread", new Item.Properties());

    /** E o Tecido Estável, que se tece dele. */
    public static final Item STABLE_FABRIC = plain("stable_fabric", new Item.Properties());

    /** A Assinatura de Fenda, que liga dois lugares. */
    public static final Item RIFT_SIGNATURE = tool("rift_signature",
            properties -> new RiftSignatureItem(properties.stacksTo(1).durability(1), false));

    /** A Estabilizada, que não se gasta nem esquece o lugar marcado. */
    public static final Item STABILIZED_RIFT_SIGNATURE = tool("stabilized_rift_signature",
            properties -> new RiftSignatureItem(properties.stacksTo(1), true));

    /** E o Fecha-Fendas. */
    public static final Item RIFT_REMOVER = tool("rift_remover",
            properties -> new RiftRemoverItem(properties.stacksTo(1).durability(32)));

    private ShatteredItems() {
    }

    /** As portas entram na mochila como item de porta, que se põe de pé em duas metades. */
    private static void door(Block bloco) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(bloco);
        var properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
        ORDER.add(Registry.register(BuiltInRegistries.ITEM, id,
                new net.minecraft.world.item.DoubleHighBlockItem(bloco, properties)));
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
