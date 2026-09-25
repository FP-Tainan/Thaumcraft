package net.thaumcraft.forbidden;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * As coisas do Forbidden Magic 0.575, de SpitefulFox.
 *
 * <p>Como os outros ramos, ele mora no mesmo jar do Thaumcraft, com as figuras e os textos no espaço de nome
 * {@code thaumcraft} e aba própria no criativo.
 */
public final class ForbiddenItems {
    /** A ordem em que as coisas entram na aba do criativo. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("forbidden"));

    /**
     * Os sete vícios do {@code ItemDeadlyShard}, na ordem dos números do original — e o nome de cada um é o do
     * pecado, menos o da mácula, que não é pecado nenhum.
     */
    public static final List<String> VICES = List.of("wrath", "envy", "taint", "pride", "lust", "sloth", "greed");

    /** Um fragmento por vício, pelo nome. */
    public static final Map<String, Item> SHARDS = new LinkedHashMap<>();

    static {
        for (String vice : VICES) {
            SHARDS.put(vice, register(vice + "_shard", properties -> new Item(properties.rarity(Rarity.UNCOMMON))));
        }
    }

    /**
     * O Fragmento da Gula é comida: dois de fome e um décimo de saturação, como o {@code ItemGluttonyShard}. Por
     * isso ele fica de fora do {@link #SHARDS}, que é de pedra.
     */
    public static final Item GLUTTONY_SHARD = register("gluttony_shard", properties ->
            new Item(properties.rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder()
                    .nutrition(2).saturationModifier(0.1f).alwaysEdible().build())));

    private ForbiddenItems() {
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    /** A aba do criativo do ramo, à parte da do Thaumcraft, como o {@code ForbiddenTab} do original. */
    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.forbidden"))
                .icon(() -> new ItemStack(SHARDS.get("wrath")))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
