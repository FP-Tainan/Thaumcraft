package net.thaumcraft.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.resources.ResourceKey;
import net.thaumcraft.Thaumcraft;

import java.util.Map;

/**
 * De que são feitas as ferramentas e as armaduras do mod, com os números da 4.2.3.5.
 *
 * <p>Os valores saíram do {@code ThaumcraftApi} do original. O táumio é um degrau acima do ferro: aguenta
 * quatrocentos golpes, cava como diamante e aceita encantamento com uma facilidade que nem o ouro tem. O
 * metal do vazio corta mais fundo e dura pouco, mas se conserta sozinho — coisa que chega noutra fatia.
 */
public final class TCMaterials {
    /** Táumio: nível de diamante, 400 de uso, 7 de velocidade, 2 de dano, 22 de encantabilidade. */
    public static final ToolMaterial THAUMIUM = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 400, 7.0f, 2.0f, 22,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/thaumium")));

    /** Metal do vazio: corta mais e dura menos, como no original. */
    public static final ToolMaterial VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0f, 3.0f, 10,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/void")));

    /** O {@code toolMatElemental}: nível de diamante, 1500 de uso, 10 de velocidade, 3 de dano, 18 de encantabilidade. */
    public static final ToolMaterial ELEMENTAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 10.0f, 3.0f, 18,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/thaumium")));

    /** O {@code toolMatCrimsonVoid} da espada carmesim: nível 4, 200 de uso, 8 de velocidade, 3,5 de dano, 20; conserta com o amuleto primordial. */
    public static final ToolMaterial CRIMSON_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0f, 3.5f, 20,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Thaumcraft.id("repairs_primal")));

    /** O {@code PRIMALVOID} do triturador primordial: nível 5, 500 de uso, 8 de velocidade, 4 de dano, 20. */
    public static final ToolMaterial PRIMAL_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0f, 4.0f, 20,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Thaumcraft.id("repairs_primal")));

    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> THAUMIUM_ARMOR_ASSET = assetKey("thaumium");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> VOID_ARMOR_ASSET = assetKey("void");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> GOGGLES_ASSET = assetKey("goggles");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> ROBES_ASSET = assetKey("robes");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> TRAVELLER_ASSET = assetKey("traveller");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> FORTRESS_ASSET = assetKey("fortress");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> HARNESS_ASSET = assetKey("hover_harness");

    /** Armadura de táumio: 2/5/6/2 de proteção e 25 de encantabilidade, os números do original. */
    public static final ArmorMaterial THAUMIUM_ARMOR = new ArmorMaterial(
            25,
            Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2),
            25, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/thaumium")),
            THAUMIUM_ARMOR_ASSET);

    /** Armadura do vazio: protege como diamante e dura pouco, também do original. */
    public static final ArmorMaterial VOID_ARMOR = new ArmorMaterial(
            10,
            Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 7, ArmorType.HELMET, 3),
            10, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE, 0.0f, 0.0f,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/void")),
            VOID_ARMOR_ASSET);

    /**
     * O {@code armorMatSpecial} do original: 1/3/2/1 de proteção, 25 de durabilidade e de encantabilidade. É o
     * material dos óculos, dos mantos do taumaturgo e das botas do viajante; cada um com o seu desenho.
     */
    public static final ArmorMaterial GOGGLES = special(GOGGLES_ASSET, "repairs_special");
    public static final ArmorMaterial ROBES = special(ROBES_ASSET, "repairs_robes");
    public static final ArmorMaterial TRAVELLER = special(TRAVELLER_ASSET, "repairs_special");
    /** O arreio taumostático usa o mesmo material; o getIsRepairable dele aceita ouro. */
    public static final ArmorMaterial HARNESS = special(HARNESS_ASSET, "repairs_harness");

    /** O {@code armorMatThaumiumFortress}: 3/7/6/3, durabilidade 40, encantabilidade 25; conserta com táumio. */
    public static final ArmorMaterial FORTRESS = new ArmorMaterial(40,
            Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 7, ArmorType.HELMET, 3),
            25, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/thaumium")), FORTRESS_ASSET);

    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> CULTIST_ASSET = assetKey("cultist");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> CULTIST_BOOTS_ASSET = assetKey("cultist_boots");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> CULTIST_LEADER_ASSET = assetKey("cultist_leader");

    /** As armaduras do Culto Carmesim: o {@code ArmorMaterial.IRON} de então (2/6/5/2, 15, 9), consertadas com ferro. */
    public static final ArmorMaterial CULTIST = iron(CULTIST_ASSET);
    public static final ArmorMaterial CULTIST_BOOTS = iron(CULTIST_BOOTS_ASSET);
    /** A armadura do pretor: a mesma conta da de fortaleza, mas consertada com ferro. */
    public static final ArmorMaterial CULTIST_LEADER = new ArmorMaterial(40,
            Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 7, ArmorType.HELMET, 3),
            25, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, CULTIST_LEADER_ASSET);

    private static ArmorMaterial iron(ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset) {
        return new ArmorMaterial(15, Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2),
                9, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR, asset);
    }

    private static ArmorMaterial special(ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset, String repair) {
        return new ArmorMaterial(25,
                Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 2, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1),
                25, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Thaumcraft.id(repair)), asset);
    }

    private TCMaterials() {
    }

    private static ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> assetKey(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(name));
    }

}
