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

    /** O item do Óculus. */
    public static final Item OCCULUS = register("occulus", properties ->
            new net.minecraft.world.item.BlockItem(ArcanaBlocks.OCCULUS, properties));

    /**
     * As <b>ferramentas vinculadas</b>: um feitiço que virou ferramenta.
     *
     * <p>Elas não se fabricam — quem as faz é a Forma do Vínculo, e quem as desfaz é a mana acabar. Não
     * empilham, porque cada uma leva dentro de si o feitiço que era.
     */
    public static final java.util.Map<BoundToolItem.Kind, Item> BOUND =
            new java.util.EnumMap<>(BoundToolItem.Kind.class);

    private static void registerBound() {
        for (BoundToolItem.Kind qual : BoundToolItem.Kind.values()) {
            Item item = register(qual.id(), properties -> new BoundToolItem(feitio(qual, properties), qual));
            BOUND.put(qual, item);
        }
    }

    /**
     * O feitio de cada ferramenta vinculada: o metal, o que ela quebra e o quanto ela bate.
     *
     * <p>Os metais são os do original — diamante para a picareta, o machado e a espada, ferro para a pá, e
     * pedra para a enxada —, e é o metal que decide quanto ela custa de manter.
     *
     * <p>Quem monta isto é o {@code ToolMaterial} do jogo, e não uma peça escrita à mão: ele sabe esperar as
     * etiquetas de bloco carregarem, que na hora de registrar um item ainda não existem.
     */
    private static Item.Properties feitio(BoundToolItem.Kind qual, Item.Properties properties) {
        // ela não se gasta: o que a mantém é a mana, e por isso não tem durabilidade nenhuma
        Item.Properties nua = properties.stacksTo(1);

        return switch (qual) {
            case PICKAXE -> net.minecraft.world.item.ToolMaterial.DIAMOND.applyToolProperties(
                    nua, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, 1.0f, -2.8f, 0.0f);
            case AXE -> net.minecraft.world.item.ToolMaterial.DIAMOND.applyToolProperties(
                    nua, net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE, 5.0f, -3.0f, 0.0f);
            case SWORD -> net.minecraft.world.item.ToolMaterial.DIAMOND.applySwordProperties(
                    nua, 3.0f, -2.4f);
            case SHOVEL -> net.minecraft.world.item.ToolMaterial.IRON.applyToolProperties(
                    nua, net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL, 1.5f, -3.0f, 0.0f);
            case HOE -> net.minecraft.world.item.ToolMaterial.STONE.applyToolProperties(
                    nua, net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE, -1.0f, -2.0f, 0.0f);
        };
    }
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
        registerBound();
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
