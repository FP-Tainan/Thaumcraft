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

    /** O Fruto Maculado: enche a barriga e cobra caro. */
    public static final Item TAINT_FRUIT = register("taint_fruit", properties ->
            new TaintedFruitItem(properties.food(new FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.8f).alwaysEdible().build())));

    /** O carvão maculado, que sai do tronco na fornalha. */
    public static final Item TAINT_COAL = register("taint_coal", properties -> new Item(properties));

    /** E os itens dos blocos do ramo, na ordem em que os blocos nascem. */
    public static final Item TAINT_LOG = blockItem(ForbiddenBlocks.TAINT_LOG);
    public static final Item TAINT_PLANKS = blockItem(ForbiddenBlocks.TAINT_PLANKS);
    public static final Item TAINT_LEAVES = blockItem(ForbiddenBlocks.TAINT_LEAVES);
    public static final Item TAINT_SAPLING = blockItem(ForbiddenBlocks.TAINT_SAPLING);
    public static final Item TAINT_STONE = blockItem(ForbiddenBlocks.TAINT_STONE);
    public static final Item TAINT_STONE_BRICKS = blockItem(ForbiddenBlocks.TAINT_STONE_BRICKS);

    /**
     * As ferramentas do ramo, com o metal elemental do Thaumcraft, que é o que o original lhes dá.
     *
     * <p>A Pá do Purificador cava mácula e limpa fluxo; a Picareta da Distorção quebra o que houver; o Machado
     * do Tomador de Crânios decepa cabeças mas não serve para madeira; o Garfo do Diabolista é de táumio e é a
     * chave das coisas da Gaiola da Ira; e o Chicote de Montaria apressa quem apanha dele.
     */
    public static final Item PURIFIER_SHOVEL = register("purifier_shovel", properties ->
            new PurifierShovelItem(properties.shovel(net.thaumcraft.item.TCMaterials.ELEMENTAL, 1.5f, -3.0f)
                    .rarity(Rarity.RARE)));
    public static final Item DISTORTION_PICKAXE = register("distortion_pickaxe", properties ->
            new Item(properties.pickaxe(net.thaumcraft.item.TCMaterials.ELEMENTAL, 1.0f, -2.8f)
                    .rarity(Rarity.RARE)));
    public static final Item SKULLTAKER_AXE = register("skulltaker_axe", properties ->
            new Item(properties.sword(net.thaumcraft.item.TCMaterials.ELEMENTAL, 3.0f, -2.4f)
                    .rarity(Rarity.UNCOMMON)));
    public static final Item DIABOLIST_FORK = register("diabolist_fork", properties ->
            new Item(properties.sword(net.thaumcraft.item.TCMaterials.THAUMIUM, 3.0f, -2.4f)));
    public static final Item RIDING_CROP = register("riding_crop", properties ->
            new RidingCropItem(properties.sword(net.minecraft.world.item.ToolMaterial.WOOD, 3.0f, -2.4f)));

    /**
     * As quatro ferramentas camaleão, que guardam três caras cada uma — três jogos de encantamentos e três nomes
     * na mesma ferramenta, trocados com um clique de quem está agachado.
     */
    public static final Item CHAMELEON_PICKAXE = register("chameleon_pickaxe", properties ->
            new MorphToolItem(properties.pickaxe(net.thaumcraft.item.TCMaterials.ELEMENTAL, 1.0f, -2.8f).rarity(Rarity.EPIC)));
    public static final Item CHAMELEON_SWORD = register("chameleon_sword", properties ->
            new MorphToolItem(properties.sword(net.thaumcraft.item.TCMaterials.ELEMENTAL, 3.0f, -2.4f).rarity(Rarity.EPIC)));
    public static final Item CHAMELEON_SHOVEL = register("chameleon_shovel", properties ->
            new MorphToolItem(properties.shovel(net.thaumcraft.item.TCMaterials.ELEMENTAL, 1.5f, -3.0f).rarity(Rarity.EPIC)));
    public static final Item CHAMELEON_AXE = register("chameleon_axe", properties ->
            new MorphToolItem(properties.axe(net.thaumcraft.item.TCMaterials.ELEMENTAL, 6.0f, -3.1f).rarity(Rarity.EPIC)));

    /** O Foco do Piscar, que é o único foco do ramo. */
    public static final Item FOCUS_BLINK = register("focus_blink", properties ->
            new net.thaumcraft.item.FocusItem(properties.stacksTo(1).rarity(Rarity.RARE),
                    "blink", ForbiddenFoci.COST_BLINK, false));

    static {
        net.thaumcraft.registry.TCItems.FOCI.put("blink", FOCUS_BLINK);
    }

    private ForbiddenItems() {
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
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
