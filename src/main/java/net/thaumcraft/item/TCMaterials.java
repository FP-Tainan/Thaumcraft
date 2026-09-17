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

    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> THAUMIUM_ARMOR_ASSET = assetKey("thaumium");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> VOID_ARMOR_ASSET = assetKey("void");
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> GOGGLES_ASSET = assetKey("goggles");

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

    /** Os óculos protegem como a armadura de táumio e usam o desenho próprio deles. */
    public static final ArmorMaterial GOGGLES = new ArmorMaterial(
            25,
            Map.of(ArmorType.HELMET, 2),
            25, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f,
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/thaumium")),
            GOGGLES_ASSET);

    private TCMaterials() {
    }

    private static ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> assetKey(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(name));
    }

}
