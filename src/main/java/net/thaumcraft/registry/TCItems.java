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
