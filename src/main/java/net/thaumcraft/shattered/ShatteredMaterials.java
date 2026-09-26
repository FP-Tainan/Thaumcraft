package net.thaumcraft.shattered;

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
 * De que são feitos os Óculos do Véu.
 *
 * <p>Aqui já moraram a Lâmina de Fenda e a armadura de Fio do Mundo Tecido, que vinham do original. Saíram: quem
 * manda não quis espada nem armadura neste ramo, que o que ele faz é abrir caminho e não brigar.
 */
public final class ShatteredMaterials {
    private ShatteredMaterials() {
    }

    /** O que conserta o que é do ramo: o Fio do Mundo. */
    public static final TagKey<net.minecraft.world.item.Item> REPAIRS_ARMOR =
            TagKey.create(Registries.ITEM, Thaumcraft.id("repairs_world_thread"));

    public static final ResourceKey<EquipmentAsset> VEIL_GOGGLES_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id("veil_goggles"));

    /**
     * Os Óculos do Véu: o mesmo {@code armorMatSpecial} dos Óculos da Descoberta — vinte e cinco de durabilidade,
     * um de proteção no elmo e vinte e cinco de encantabilidade —, mudado só no que os conserta: o Fio do Mundo,
     * que é o que foi posto neles.
     */
    public static final ArmorMaterial VEIL_GOGGLES = new ArmorMaterial(25,
            Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 2, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1),
            25, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, REPAIRS_ARMOR, VEIL_GOGGLES_ASSET);
}
