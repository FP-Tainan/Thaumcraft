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
     * A <b>Chave de Cristal</b>: a única maneira de desenhar a rede de Etherium.
     *
     * <p>Toca-se na fonte e depois na máquina. Não empilha, porque leva um lugar guardado dentro.
     */
    public static final Item CRYSTAL_WRENCH = register("crystal_wrench", properties ->
            new net.thaumcraft.arcana.machine.CrystalWrenchItem(properties.stacksTo(1)));

    /** O <b>Giz de Mago</b>: cinquenta riscos, e cada um deles sorteado entre dezesseis. */
    public static final Item WIZARD_CHALK = register("wizard_chalk", properties ->
            new net.thaumcraft.arcana.machine.ChalkItem(properties
                    .stacksTo(1)
                    .durability(net.thaumcraft.arcana.machine.ChalkItem.USES)));

    /**
     * Os <b>nove materiais</b> do ramo: o {@code ItemOre} da 1.4.0.009, que eram nove metadados de um item
     * só, na ordem em que ele os numera.
     *
     * <p>Quatro saem do chão — a <b>quimerita</b>, o <b>topázio azul</b>, a <b>pedra-da-lua</b> e a
     * <b>pedra-do-sol</b>. Dois saem do forno — o <b>pó de vinteum</b>, que é o minério fundido, e a
     * <b>cinza arcana</b>, duas de cada <b>composto arcano</b>, que por sua vez se fabrica. A <b>gordura
     * animal</b> cai de porco. E o <b>vinteum purificado</b>, veja-se o {@link #PURIFIED_VINTEUM}.
     */
    public static final java.util.Map<String, Item> MATERIALS = new java.util.LinkedHashMap<>();

    /**
     * O <b>Vinteum Purificado</b>, e o buraco que ele tem no original.
     *
     * <p>Ele é ingrediente de quatro coisas — o embutido de ouro, os óculos magitécnicos, a Poção de Mana
     * Lendária e o Catalisador de Vínculo — e <b>nada no 1.4.0.009 o produz</b>: não há receita, não há
     * fundição, não há queda, não há baú. Quem joga o Ars Magica 2 daquela versão só o tem em criativo ou
     * vindo de outro mod.
     *
     * <p>Fica como está. Inventar-lhe uma receita seria escrever um mod em vez de portar um, e o lugar de
     * dizer isto é aqui e no {@code PORTE.md} — não numa receita que o original não tem.
     */
    public static Item PURIFIED_VINTEUM;

    private static void registerMaterials() {
        for (String qual : new String[]{"vinteum_dust", "arcane_compound", "arcane_ash", "purified_vinteum",
                "chimerite", "blue_topaz", "sunstone", "moonstone", "animal_fat"}) {
            MATERIALS.put(qual, register(qual, Item::new));
        }
        PURIFIED_VINTEUM = MATERIALS.get("purified_vinteum");

        register("cerublossom", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.CERUBLOSSOM, properties.useBlockDescriptionPrefix()));
        register("desert_nova", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.DESERT_NOVA, properties.useBlockDescriptionPrefix()));
        register("black_aurem", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.BLACK_AUREM, properties.useBlockDescriptionPrefix()));
        register("celestial_prism", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.CELESTIAL_PRISM, properties.useBlockDescriptionPrefix()));
        register("essence_conduit", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.ESSENCE_CONDUIT, properties.useBlockDescriptionPrefix()));
        register("mana_battery", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.MANA_BATTERY, properties.useBlockDescriptionPrefix()));
        register("obelisk", properties -> new net.minecraft.world.item.BlockItem(
                ArcanaBlocks.OBELISK, properties.useBlockDescriptionPrefix()));

        for (var entrada : ArcanaBlocks.ORES.entrySet()) {
            register(entrada.getKey() + "_ore", properties -> new net.minecraft.world.item.BlockItem(
                    entrada.getValue(), properties.useBlockDescriptionPrefix()));
        }
        for (var entrada : ArcanaBlocks.MATERIAL_BLOCKS.entrySet()) {
            register(entrada.getKey() + "_block", properties -> new net.minecraft.world.item.BlockItem(
                    entrada.getValue(), properties.useBlockDescriptionPrefix()));
        }
    }

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
            // o arco é de outra classe, porque um arco do jogo tem de herdar o BowItem
            Item item = qual == BoundToolItem.Kind.BOW
                    ? register(qual.id(), properties -> new BoundBowItem(properties.stacksTo(1)))
                    : register(qual.id(), properties -> new BoundToolItem(feitio(qual, properties), qual));
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
            // o arco não passa por aqui: ele é do BoundBowItem, e um arco não tem feitio de ferramenta
            case BOW -> nua;
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
        registerMaterials();

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
