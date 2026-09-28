package net.thaumcraft.arcana;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** As coisas do Ars Arcana. */
public final class ArcanaItems {
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("arcana"));

    /**
     * O <b>Feitiço</b>: a frase escrita, pronta a lançar.
     *
     * <p>Ele não empilha, porque não há dois iguais — o que ele é depende do que lhe escreveram dentro.
     */
    public static final Item SPELL = register("spell", properties ->
            new SpellItem(properties.stacksTo(1)));

    /**
     * <b>As peças de feitiço</b>: uma por palavra da gramática, as {@code ItemSpellPart} do original.
     *
     * <p>Elas não fazem nada na mão. O que elas servem é para ser escritas numa frase, na Mesa de Inscrição,
     * e é lá que viram feitiço. São elas que a árvore de perícias do original dá a quem aprende — e, até a
     * árvore existir, a aba do criativo as dá todas.
     *
     * <p>A ordem é a da gramática, e não a do alfabeto: primeiro as Formas, depois as Essências, depois os
     * Modificadores. É como se lê uma frase.
     */
    public static final java.util.Map<SpellPart, Item> PARTS = new java.util.LinkedHashMap<>();

    /** Uma peça pelo item, ou nada. */
    public static SpellPart partOf(Item qual) {
        return qual instanceof SpellPartItem peça ? peça.part() : null;
    }

    /** E o item de uma peça, ou nada. */
    public static Item itemOf(SpellPart qual) {
        return PARTS.get(qual);
    }

    /** O item da Mesa de Inscrição, para se poder pô-la no mundo. */
    public static final Item INSCRIPTION_TABLE = register("inscription_table", properties ->
            new net.minecraft.world.item.BlockItem(ArcanaBlocks.INSCRIPTION_TABLE, properties));

    private static void registerParts() {
        for (SpellPart.Shape forma : SpellParts.shapes()) part(forma);
        for (SpellPart.Essence essência : SpellParts.essences()) part(essência);
        for (SpellPart.Modifier mod : SpellParts.modifiers()) part(mod);
    }

    private static void part(SpellPart qual) {
        Item item = register("spell_part_" + qual.name(), properties ->
                new SpellPartItem(properties, qual));
        PARTS.put(qual, item);
    }

    private ArcanaItems() {
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    /** O que a aba do ramo mostra, para a prova da aba do mod saber que eles não são dela. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    public static int count() {
        return ORDER.size();
    }

    /** A aba do criativo do ramo, com um feitiço de cada Forma para se experimentar. */
    public static void init() {
        registerParts();

        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.arcana"))
                .icon(() -> new ItemStack(SPELL))
                .displayItems((parameters, output) -> {
                    ORDER.forEach(output::accept);
                    output.accept(SpellItem.write(new ItemStack(SPELL),
                            Spell.of(Shapes.SELF, Essences.HEAL)));
                    output.accept(SpellItem.write(new ItemStack(SPELL),
                            Spell.of(Shapes.TOUCH, Essences.FIRE_DAMAGE)));
                    output.accept(SpellItem.write(new ItemStack(SPELL),
                            Spell.of(Shapes.TOUCH, Essences.DIG)));
                    output.accept(SpellItem.write(new ItemStack(SPELL),
                            Spell.of(Shapes.AOE, Essences.FROST_DAMAGE)));
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
