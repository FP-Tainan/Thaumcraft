package net.thaumcraft.shattered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.thaumcraft.Thaumcraft;

import java.util.Map;

/** De que são feitas as ferramentas e a armadura dos Reinos Fragmentados. */
public final class ShatteredMaterials {
    private ShatteredMaterials() {
    }

    /** O que conserta a Lâmina de Fenda: o Tecido Estável, como no {@code getIsRepairable} do original. */
    public static final TagKey<net.minecraft.world.item.Item> REPAIRS_BLADE =
            TagKey.create(Registries.ITEM, Thaumcraft.id("repairs_stable_fabric"));

    /** E o que conserta a armadura: o Fio do Mundo. */
    public static final TagKey<net.minecraft.world.item.Item> REPAIRS_ARMOR =
            TagKey.create(Registries.ITEM, Thaumcraft.id("repairs_world_thread"));

    /**
     * A Lâmina de Fenda: o original dá-lhe o {@code ToolMaterial.IRON} e troca-lhe só o conserto, então são os
     * números do ferro — duzentos e cinquenta de uso, seis de velocidade, dois de dano, catorze de encantabilidade.
     */
    public static final ToolMaterial RIFT_BLADE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0f, 2.0f, 14, REPAIRS_BLADE);

    public static final ResourceKey<EquipmentAsset> WOVEN_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id("woven_world_thread"));

    /**
     * A armadura de Fio do Mundo Tecido: o {@code WOVEN_WORLD_THREAD} do original — vinte de durabilidade, 2/3/4/5
     * de proteção, vinte de encantabilidade e nada de dureza.
     *
     * <p>Os números do original vêm na ordem dele ({@code botas, calças, peitoral, elmo}); aqui vão pelo nome de
     * cada peça, que é o que o jogo de hoje pede.
     */
    public static final ArmorMaterial WOVEN_WORLD_THREAD = new ArmorMaterial(
            20,
            Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 3, ArmorType.CHESTPLATE, 4, ArmorType.HELMET, 5),
            20, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, REPAIRS_ARMOR, WOVEN_ASSET);

    /** O nome do desenho da armadura, para quem precisar dele. */
    public static Identifier assetId() {
        return WOVEN_ASSET.identifier();
    }
}
