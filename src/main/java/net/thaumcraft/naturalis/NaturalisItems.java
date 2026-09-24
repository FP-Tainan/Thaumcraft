package net.thaumcraft.naturalis;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.TCMaterials;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * As coisas do Magia Naturalis 0.5.0, de elenterius — a Thaumaturgia Aplicada da lore.
 *
 * <p>Como o Maleficium, o ramo mora no mesmo jar do Thaumcraft, com as figuras e os textos no espaço de nome
 * {@code thaumcraft}, e tem aba própria no criativo e no Thaumonomicon.
 */
public final class NaturalisItems {
    /** A ordem em que as coisas entram na aba do criativo. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("naturalis"));

    // ------------------------------------------------------------------ as foices

    /** A foice de táumio: corta a planta e mais duas encostadas nela. */
    public static final Item THAUMIUM_SICKLE = register("thaumium_sickle", properties ->
            new SickleItem(properties.sword(TCMaterials.THAUMIUM, 3.0f, -2.4f).rarity(Rarity.UNCOMMON), 2, 0, false));

    /** A foice do vazio: alcança quatro, enfraquece quem ela acerta e se conserta sozinha. */
    public static final Item VOID_SICKLE = register("void_sickle", properties ->
            new VoidSickleItem(properties.sword(TCMaterials.VOID, 3.0f, -2.4f).rarity(Rarity.UNCOMMON)));

    /** A Foice da Abundância: alcança nove, colhe para o inventário e faz cair três vezes. */
    public static final Item ELEMENTAL_SICKLE = register("elemental_sickle", properties ->
            new SickleItem(properties.sword(TCMaterials.ELEMENTAL, 3.0f, -2.4f).rarity(Rarity.RARE), 9, 2, true));

    private NaturalisItems() {
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

    /** A aba do criativo do ramo: fica à parte da do Thaumcraft, como o original a tinha. */
    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.naturalis"))
                .icon(() -> new ItemStack(THAUMIUM_SICKLE))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
