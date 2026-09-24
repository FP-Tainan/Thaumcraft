package net.thaumcraft.naturalis;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.thaumcraft.Thaumcraft;

import java.util.Map;

/**
 * De que são feitas as roupas do Magia Naturalis: o material especial do Thaumcraft, que é o que o original usa,
 * com os trezentos e cinquenta usos que ele dá aos dois óculos. Consertam-se com ouro.
 */
public final class NaturalisMaterials {
    public static final ResourceKey<EquipmentAsset> SPECTACLES_ASSET = asset("spectacles");
    public static final ResourceKey<EquipmentAsset> DARK_CRYSTAL_ASSET = asset("dark_crystal_goggles");

    /** Os Óculos: trezentos e cinquenta usos, um de proteção na cabeça. */
    public static final ArmorMaterial SPECTACLES = goggles(SPECTACLES_ASSET);
    /** E os de cristal escuro, iguais nos números. */
    public static final ArmorMaterial DARK_CRYSTAL = goggles(DARK_CRYSTAL_ASSET);

    private NaturalisMaterials() {
    }

    private static ArmorMaterial goggles(ResourceKey<EquipmentAsset> asset) {
        return new ArmorMaterial(350 / 11, Map.of(ArmorType.HELMET, 1), 25,
                SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
                TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.withDefaultNamespace("gold_ingots")),
                asset);
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(name));
    }
}
