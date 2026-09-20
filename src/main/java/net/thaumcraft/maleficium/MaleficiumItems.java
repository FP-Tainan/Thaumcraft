package net.thaumcraft.maleficium;

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

import java.util.ArrayList;
import java.util.List;

/**
 * As coisas do Tainted Magic 8.1.1, de Yulife — o ramo que a lore chama de <i>Maleficium</i>.
 *
 * <p>No original quase tudo morava em dois itens com subtipos: o {@code ItemMaterial}, de doze, e o
 * {@code ItemSalis}, de dois. O Minecraft de hoje não tem subtipo, então cada um virou um item, com o mesmo nome e a
 * mesma figura; a ordem da aba do criativo é a mesma em que o original os listava.
 */
public final class MaleficiumItems {
    /** A ordem em que as coisas entram na aba do criativo. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("maleficium"));

    // ------------------------------------------------------------------ o ItemMaterial, subtipo por subtipo

    /** O lingote de metal das sombras: o que sai do ferro num crisol maculado. */
    public static final Item SHADOWMETAL_INGOT = register("shadowmetal_ingot", Rarity.UNCOMMON);
    /** Pano imbuído de sombra. */
    public static final Item SHADOW_CLOTH = register("shadow_cloth", Rarity.UNCOMMON);
    /** Pano tingido de sangue carmesim, igual ao dos mantos do culto. */
    public static final Item CRIMSON_CLOTH = register("crimson_cloth", Rarity.UNCOMMON);
    /** Fragmento desequilibrado distorcido. */
    public static final Item WARPED_SHARD = register("warped_shard", Rarity.COMMON);
    /** Fragmento desequilibrado maculado. */
    public static final Item TAINTED_SHARD = register("tainted_shard", Rarity.COMMON);
    /** O fragmento da criação: a chave de como tudo veio a ser. */
    public static final Item CREATION_SHARD = register("creation_shard", Rarity.EPIC);
    /** Chapa de liga táumica. */
    public static final Item THAUMIC_PLATING = register("thaumic_plating", Rarity.UNCOMMON);
    /** Chapa carmesim. */
    public static final Item CRIMSON_PLATING = register("crimson_plating", Rarity.UNCOMMON);
    /** A pepita de metal das sombras: nove fazem um lingote. */
    public static final Item SHADOWMETAL_NUGGET = register("shadowmetal_nugget", Rarity.UNCOMMON);
    /** Nódulo primordial. */
    public static final Item PRIMORDIAL_NODULE = register("primordial_nodule", Rarity.EPIC);
    /** Partícula primordial. */
    public static final Item PRIMORDIAL_MOTE = register("primordial_mote", Rarity.EPIC);
    /** Estilhaço da criação. */
    public static final Item CREATION_FRAGMENT = register("creation_fragment", Rarity.EPIC);

    // ------------------------------------------------------------------ os sais

    /** Salis Tempestas: largado no chão, vira e revira o tempo. */
    public static final Item SALIS_TEMPESTAS = register("salis_tempestas", properties ->
            new SalisItem(SalisItem.Kind.TEMPESTAS, properties.rarity(Rarity.EPIC)));
    /** Salis Aevum: largado no chão, empurra o dia para a noite e a noite para o dia. */
    public static final Item SALIS_AEVUM = register("salis_aevum", properties ->
            new SalisItem(SalisItem.Kind.AEVUM, properties.rarity(Rarity.EPIC)));

    private MaleficiumItems() {
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    private static Item register(String name, Rarity rarity) {
        return register(name, properties -> new Item(properties.rarity(rarity)));
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    /** A aba do criativo do ramo: fica à parte da do Thaumcraft, como o original a tinha. */
    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.maleficium"))
                .icon(() -> new ItemStack(SHADOWMETAL_INGOT))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
