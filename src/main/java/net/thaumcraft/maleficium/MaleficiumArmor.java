package net.thaumcraft.maleficium;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.thaumcraft.Thaumcraft;

import java.util.Map;

/**
 * De que são feitas as roupas do Maleficium: o {@code TMMaterials} do Tainted Magic 8.1.1, com os números dele.
 *
 * <p>Os óculos distorcidos e os de metal do vazio só cobrem a cabeça; as botas do caminhante do vazio, só os pés; e
 * as duas armaduras de fortaleza — a do vazio e a das sombras — cobrem elmo, peitoral e grevas, como no original.
 */
public final class MaleficiumArmor {
    public static final ResourceKey<EquipmentAsset> WARPED_GOGGLES_ASSET = asset("warped_goggles");
    public static final ResourceKey<EquipmentAsset> VOIDMETAL_GOGGLES_ASSET = asset("voidmetal_goggles");
    public static final ResourceKey<EquipmentAsset> VOIDWALKER_ASSET = asset("voidwalker");
    public static final ResourceKey<EquipmentAsset> VOID_FORTRESS_ASSET = asset("void_fortress");
    public static final ResourceKey<EquipmentAsset> SHADOW_FORTRESS_ASSET = asset("shadow_fortress");

    /** Consertam-se com o lingote de metal das sombras. */
    private static final TagKey<Item> SHADOWMETAL = MaleficiumMaterials.SHADOWMETAL_INGOT;
    /** E as do vazio, com o lingote de metal do vazio do próprio Thaumcraft. */
    private static final TagKey<Item> VOID_INGOT = TagKey.create(Registries.ITEM,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/void"));

    /** Os óculos distorcidos: quinhentos usos, quatro de proteção na cabeça, vinte de encantabilidade. */
    public static final ArmorMaterial WARPED = new ArmorMaterial(500 / 11,
            Map.of(ArmorType.HELMET, 4), 20, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
            SHADOWMETAL, WARPED_GOGGLES_ASSET);

    /** Os óculos de metal do vazio, que no original usam a matéria especial do Thaumcraft. */
    public static final ArmorMaterial VOIDMETAL_GOGGLES = new ArmorMaterial(500 / 11,
            Map.of(ArmorType.HELMET, 3), 25, SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f,
            VOID_INGOT, VOIDMETAL_GOGGLES_ASSET);

    /** As botas do caminhante do vazio: trezentos usos, quatro de proteção nos pés, trinta de encantabilidade. */
    public static final ArmorMaterial VOIDWALKER = new ArmorMaterial(300 / 13,
            Map.of(ArmorType.BOOTS, 4), 30, SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f,
            VOID_INGOT, VOIDWALKER_ASSET);

    /** A armadura de fortaleza do vazio: quatro, oito e seis. */
    public static final ArmorMaterial VOID_FORTRESS = new ArmorMaterial(300 / 11,
            Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6), 30,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f, VOID_INGOT, VOID_FORTRESS_ASSET);

    /** A das sombras: três mil usos, quatro, dez e seis. */
    public static final ArmorMaterial SHADOW_FORTRESS = new ArmorMaterial(3000 / 11,
            Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 10, ArmorType.LEGGINGS, 6), 30,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f, SHADOWMETAL, SHADOW_FORTRESS_ASSET);

    private MaleficiumArmor() {
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(name));
    }
}
