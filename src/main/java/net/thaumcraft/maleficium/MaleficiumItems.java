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

    // ------------------------------------------------------------------ o sangue e o cogumelo

    /** O frasco de sangue infundido com o vazio: na mesa, ele toca uma peça de armadura. */
    public static final Item VOID_BLOOD = register("void_blood", properties ->
            new Item(properties.rarity(Rarity.RARE).craftRemainder(net.thaumcraft.registry.TCItems.PHIAL)));

    /** O cogumelo mágico: come-se depressa e ensina um ponto de um primário. */
    public static final Item MAGIC_FUNGUAR = register("magic_funguar", properties ->
            new MagicFunguarItem(properties.rarity(Rarity.UNCOMMON)
                    .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(0.2f).build())));

    // ------------------------------------------------------------------ os focos

    /** Os seis focos do ramo; eles também entram no mapa de focos do Thaumcraft, pelo nome do tipo. */
    public static final Item FOCUS_TAINT_SWARM = focus("focus_taint_swarm", "taint_swarm", MaleficiumFoci.COST_TAINT_SWARM);
    public static final Item FOCUS_DARK_MATTER = focus("focus_dark_matter", "dark_matter", MaleficiumFoci.COST_DARK_MATTER);
    public static final Item FOCUS_SHOCKWAVE = focus("focus_shockwave", "shockwave", MaleficiumFoci.COST_SHOCKWAVE);
    public static final Item FOCUS_VIS_SHARD = focus("focus_vis_shard", "vis_shard", MaleficiumFoci.COST_VIS_SHARD);
    public static final Item FOCUS_LUMOS = focus("focus_lumos", "lumos", MaleficiumFoci.COST_LUMOS);
    public static final Item FOCUS_MAGE_MACE = focus("focus_mage_mace", "mage_mace", MaleficiumFoci.COST_MAGE_MACE);

    // ------------------------------------------------------------------ as roupas e as bijuterias

    /** Os óculos distorcidos: revelam, distorcem um, e consertam-se com metal das sombras. */
    public static final Item WARPED_GOGGLES = register("warped_goggles", properties ->
            new MaleficiumGear(0, 1, false, properties
                    .humanoidArmor(MaleficiumArmor.WARPED, net.minecraft.world.item.equipment.ArmorType.HELMET)
                    .rarity(Rarity.RARE)));

    /** Os óculos de metal do vazio: revelam, descontam doze por cento, distorcem cinco e consertam-se sozinhos. */
    public static final Item VOIDMETAL_GOGGLES = register("voidmetal_goggles", properties ->
            new MaleficiumGear(12, 5, true, properties
                    .humanoidArmor(MaleficiumArmor.VOIDMETAL_GOGGLES, net.minecraft.world.item.equipment.ArmorType.HELMET)
                    .rarity(Rarity.RARE)));

    /** As botas do caminhante do vazio. */
    public static final Item VOIDWALKER_BOOTS = register("voidwalker_boots", properties ->
            new VoidwalkerBootsItem(properties
                    .humanoidArmor(MaleficiumArmor.VOIDWALKER, net.minecraft.world.item.equipment.ArmorType.BOOTS)
                    .rarity(Rarity.EPIC)
                    .component(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                            MaleficiumArmor.VOIDWALKER.createAttributes(net.minecraft.world.item.equipment.ArmorType.BOOTS)
                                    // o degrau de um bloco e o pulo um quarto mais alto do original
                                    .withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                    net.thaumcraft.Thaumcraft.id("voidwalker_step"), 0.4,
                                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                            net.minecraft.world.entity.EquipmentSlotGroup.FEET)
                                    .withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,
                                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                    net.thaumcraft.Thaumcraft.id("voidwalker_jump"), 0.25,
                                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                            net.minecraft.world.entity.EquipmentSlotGroup.FEET))));

    /** A faixa do caminhante do vazio, que vai na casa do cinto. */
    public static final Item VOIDWALKER_SASH = register("voidwalker_sash", properties ->
            new MaleficiumBaubles.VoidwalkerSashItem(properties.stacksTo(1).rarity(Rarity.EPIC)));

    /** A armadura de fortaleza do vazio. */
    public static final Item VOID_FORTRESS_HELMET = fortress("void_fortress_helmet", MaleficiumArmor.VOID_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.HELMET, 5, 3);
    public static final Item VOID_FORTRESS_CHESTPLATE = fortress("void_fortress_chestplate", MaleficiumArmor.VOID_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, 5, 3);
    public static final Item VOID_FORTRESS_LEGGINGS = fortress("void_fortress_leggings", MaleficiumArmor.VOID_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.LEGGINGS, 5, 3);

    /** E a das sombras, que aguenta dez vezes mais pancada. */
    public static final Item SHADOW_FORTRESS_HELMET = fortress("shadow_fortress_helmet", MaleficiumArmor.SHADOW_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.HELMET, 5, 5);
    public static final Item SHADOW_FORTRESS_CHESTPLATE = fortress("shadow_fortress_chestplate", MaleficiumArmor.SHADOW_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, 5, 5);
    public static final Item SHADOW_FORTRESS_LEGGINGS = fortress("shadow_fortress_leggings", MaleficiumArmor.SHADOW_FORTRESS,
            net.minecraft.world.item.equipment.ArmorType.LEGGINGS, 5, 5);

    /** O anel de Lumos: vestido, enxerga-se no escuro. */
    public static final Item LUMOS_RING = register("lumos_ring", properties ->
            new MaleficiumBaubles.LumosRingItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** O amuleto de voo, que troca vis por altura. */
    public static final Item FLYTE_CHARM = register("flyte_charm", properties ->
            new FlyteCharmItem(properties.stacksTo(1).rarity(Rarity.EPIC)));

    /** A Chave do Portão Celeste: prende-se a um lugar e leva de volta a ele. */
    public static final Item GATE_KEY = register("gate_key", properties ->
            new GateKeyItem(properties.stacksTo(1).rarity(Rarity.EPIC)));

    // ------------------------------------------------------------------ as peças de varinha

    /** A haste de madeira distorcida e o núcleo de bastão dela. */
    public static final Item WAND_ROD_WARPWOOD = register("wand_rod_warpwood", Rarity.UNCOMMON);
    public static final Item STAFF_ROD_WARPWOOD = register("staff_rod_warpwood", Rarity.RARE);

    /** As quatro pontas: metal das sombras e os três panos. */
    public static final Item WAND_CAP_SHADOWMETAL = register("wand_cap_shadowmetal", Rarity.RARE);
    public static final Item WAND_CAP_CLOTH = register("wand_cap_cloth", Rarity.UNCOMMON);
    public static final Item WAND_CAP_CRIMSONCLOTH = register("wand_cap_crimsoncloth", Rarity.UNCOMMON);
    public static final Item WAND_CAP_SHADOWCLOTH = register("wand_cap_shadowcloth", Rarity.UNCOMMON);

    // ------------------------------------------------------------------ as lâminas e as ferramentas

    /** O frasco de sangue carmesim, que o punhal oco tira de quem apanha. */
    public static final Item CRIMSON_BLOOD = register("crimson_blood", Rarity.UNCOMMON);

    /** O punhal oco: quase não fere, mas tira sangue. */
    public static final Item HOLLOW_DAGGER = register("hollow_dagger", properties ->
            new HollowDaggerItem(properties.sword(MaleficiumMaterials.HOLLOW, 3.0f, -2.4f)
                    .rarity(Rarity.UNCOMMON)));

    /** As cinco ferramentas de metal das sombras. */
    public static final Item SHADOWMETAL_PICKAXE = register("shadowmetal_pickaxe", properties ->
            new Item(properties.pickaxe(MaleficiumMaterials.SHADOW, 1.0f, -2.8f).rarity(Rarity.UNCOMMON)));
    public static final Item SHADOWMETAL_AXE = register("shadowmetal_axe", properties ->
            new Item(properties.axe(MaleficiumMaterials.SHADOW, 5.0f, -3.0f).rarity(Rarity.UNCOMMON)));
    public static final Item SHADOWMETAL_SHOVEL = register("shadowmetal_shovel", properties ->
            new Item(properties.shovel(MaleficiumMaterials.SHADOW, 1.5f, -3.0f).rarity(Rarity.UNCOMMON)));
    public static final Item SHADOWMETAL_HOE = register("shadowmetal_hoe", properties ->
            new ShadowmetalHoeItem(properties.hoe(MaleficiumMaterials.SHADOW, -3.0f, 0.0f).rarity(Rarity.UNCOMMON)));
    public static final Item SHADOWMETAL_SWORD = register("shadowmetal_sword", properties ->
            new Item(properties.sword(MaleficiumMaterials.SHADOW, 3.0f, -2.4f).rarity(Rarity.UNCOMMON)));

    /** O Desmontador Táumico: bebe entropia e cava com ela. */
    public static final Item THAUMIC_DISASSEMBLER = register("thaumic_disassembler", properties ->
            new ThaumicDisassemblerItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** A Lâmina Primordial. */
    public static final Item PRIMAL_BLADE = register("primal_blade", properties ->
            new PrimalBladeItem(properties.sword(MaleficiumMaterials.PRIMAL, 0.0f, -2.4f).rarity(Rarity.EPIC)));

    /** As três Lâminas de Fortaleza, com os danos do original. */
    public static final Item THAUMIUM_FORTRESS_BLADE = blade("thaumium_fortress_blade", 0, 14.25f, Rarity.UNCOMMON);
    public static final Item VOIDMETAL_FORTRESS_BLADE = blade("voidmetal_fortress_blade", 1, 17.5f, Rarity.RARE);
    public static final Item SHADOWMETAL_FORTRESS_BLADE = blade("shadowmetal_fortress_blade", 2, 20.75f, Rarity.EPIC);

    // ------------------------------------------------------------------ o que vem dos blocos

    /** As bagas da beladona: comem-se e matam. */
    public static final Item NIGHTSHADE_BERRIES = register("nightshade_berries", properties ->
            new NightshadeBerriesItem(properties.rarity(Rarity.UNCOMMON)
                    .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(0).saturationModifier(0.0f)
                            .alwaysEdible().build())));

    /** O adubo que torce a muda de madeira-prata em muda distorcida. */
    public static final Item WARP_FERTILIZER = register("warp_fertilizer", properties ->
            new WarpFertilizerItem(properties.rarity(Rarity.UNCOMMON)));

    public static final Item WARPWOOD_LOG = blockItem(MaleficiumBlocks.WARPWOOD_LOG);
    public static final Item WARPWOOD_KNOT = blockItem(MaleficiumBlocks.WARPWOOD_KNOT);
    public static final Item WARPWOOD_PLANKS = blockItem(MaleficiumBlocks.WARPWOOD_PLANKS);
    public static final Item WARPWOOD_LEAVES = blockItem(MaleficiumBlocks.WARPWOOD_LEAVES);
    public static final Item WARPWOOD_SAPLING = blockItem(MaleficiumBlocks.WARPWOOD_SAPLING);
    public static final Item NIGHTSHADE_BUSH = blockItem(MaleficiumBlocks.NIGHTSHADE_BUSH);

    private MaleficiumItems() {
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

    /** Uma lâmina de fortaleza: sem uso que gaste, com o dano do subtipo dela no braço. */
    private static Item blade(String name, int tier, float damage, Rarity rarity) {
        return register(name, properties -> new FortressBladeItem(properties.stacksTo(1).rarity(rarity)
                .component(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                        net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                                Item.BASE_ATTACK_DAMAGE_ID, damage,
                                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                                .build()),
                tier, damage));
    }

    /** Uma peça de armadura de fortaleza, com o desconto de vis e a distorção dela. */
    private static Item fortress(String name, net.minecraft.world.item.equipment.ArmorMaterial material,
                                 net.minecraft.world.item.equipment.ArmorType type, int discount, int warp) {
        return register(name, properties -> new MaleficiumFortressGear(discount, warp,
                properties.humanoidArmor(material, type).rarity(Rarity.EPIC)));
    }

    /** Um foco do ramo: entra na aba e no mapa de focos do Thaumcraft. */
    private static Item focus(String name, String type, net.thaumcraft.api.aspects.AspectList cost) {
        Item item = register(name, properties ->
                new net.thaumcraft.item.FocusItem(properties.stacksTo(1).rarity(Rarity.RARE), type, cost, false));
        net.thaumcraft.registry.TCItems.FOCI.put(type, item);
        return item;
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
