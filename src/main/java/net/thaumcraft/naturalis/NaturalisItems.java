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

    // ------------------------------------------------------------------ os focos

    /** O Foco de Construção: levanta uma forma de blocos a partir da face mirada. */
    public static final Item BUILDER_FOCUS = focus("builder_focus", "build", BuilderFocus.COST);

    // ------------------------------------------------------------------ as pedras alquímicas

    /** A Pedra do Catalisador Fenomorfo, que troca um bloco pelo próximo da família dele. */
    public static final Item MUTATION_STONE = register("mutation_stone", properties ->
            new AlchemicalStoneItem(properties.stacksTo(1).rarity(Rarity.RARE)));

    /** E a Pedra do Alquimista de Mercúrio, que sobe um degrau nos efeitos de quem a usa. */
    public static final Item QUICKSILVER_STONE = register("quicksilver_stone", properties ->
            new QuicksilverStoneItem(properties.stacksTo(1).rarity(Rarity.RARE)));

    // ------------------------------------------------------------------ o diário

    /** O Diário de Pesquisa: anota pontos na mesa de decomposição e despeja-os depois. */
    public static final Item RESEARCH_LOG = register("research_log", properties ->
            new ResearchLogItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));

    // ------------------------------------------------------------------ os óculos

    /** Os Óculos: revelam os nós, descontam seis por cento e dizem o que é o bloco da mira. */
    public static final Item SPECTACLES = register("spectacles", properties ->
            new SpectaclesItem(properties.humanoidArmor(NaturalisMaterials.SPECTACLES,
                    net.minecraft.world.item.equipment.ArmorType.HELMET).rarity(Rarity.EPIC)));

    /** Os Óculos de Cristal Escuro: revelam, descontam mais em Perditio e não deixam cegar. */
    public static final Item DARK_CRYSTAL_GOGGLES = register("dark_crystal_goggles", properties ->
            new DarkCrystalGogglesItem(properties.humanoidArmor(NaturalisMaterials.DARK_CRYSTAL,
                    net.minecraft.world.item.equipment.ArmorType.HELMET).rarity(Rarity.RARE)));

    // ------------------------------------------------------------------ a madeira arcana

    /** Os sete feitios da madeira arcana, na ordem em que o original os listava. */
    public static final Item GREATWOOD_PLANKS_HORIZONTAL = blockItem(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL);
    public static final Item GREATWOOD_ORNAMENT = blockItem(NaturalisBlocks.GREATWOOD_ORNAMENT);
    public static final Item SILVERWOOD_PLANKS_HORIZONTAL = blockItem(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL);
    public static final Item SILVERWOOD_PLANKS_VERTICAL = blockItem(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL);
    public static final Item GREATWOOD_GOLD_ORNAMENT = blockItem(NaturalisBlocks.GREATWOOD_GOLD_ORNAMENT);
    public static final Item GREATWOOD_GOLD_ORNAMENT_2 = blockItem(NaturalisBlocks.GREATWOOD_GOLD_ORNAMENT_2);
    public static final Item GREATWOOD_GOLD_TRIM = blockItem(NaturalisBlocks.GREATWOOD_GOLD_TRIM);

    private NaturalisItems() {
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    /** Um foco do ramo: entra na aba e no mapa de focos do Thaumcraft. */
    private static Item focus(String name, String type, net.thaumcraft.api.aspects.AspectList cost) {
        Item item = register(name, properties ->
                new net.thaumcraft.item.FocusItem(properties.stacksTo(1).rarity(Rarity.RARE), type, cost, false));
        net.thaumcraft.registry.TCItems.FOCI.put(type, item);
        return item;
    }

    /** O item de um bloco do ramo, com o nome do bloco. */
    private static Item blockItem(net.minecraft.world.level.block.Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return register(name, properties -> new net.minecraft.world.item.BlockItem(block, properties.useBlockDescriptionPrefix()));
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
