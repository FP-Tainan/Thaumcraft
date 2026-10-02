package net.thaumcraft.occulta;

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
 * De que são feitas as coisas de vestir do Ars Occulta.
 *
 * <p>Por enquanto são só os <b>Abafadores</b>, e eles não são armadura: no original não protegem de nada, nem se
 * gastam — servem para tapar as orelhas e mais nada. Aqui ficam com zero de proteção e zero de tenacidade, que é
 * o mais perto que o jogo de hoje chega disso.
 *
 * <p>A durabilidade é a do original, {@code Integer.MAX_VALUE}, e não zero: <b>zero não é o mesmo que nunca se
 * gastar</b>. O jogo de hoje olha para a peça que <i>tem</i> durabilidade, seja ela qual for, e a primeira
 * pancada que o dono leva gasta um ponto dela — com zero de conta, a peça se desfaz no primeiro golpe e o grito
 * da mandrágora passava a alcançar quem trazia os abafadores. Com a conta do original, nunca se gastam.
 *
 * <p>A encantabilidade é <b>um</b>, e não zero, porque o jogo de hoje não aceita zero — o mesmo desvio que a
 * Cabeça de Isaac do Ars Mortuorum já tinha.
 */
public final class OccultaMaterials {
    public static final ResourceKey<EquipmentAsset> EARMUFFS_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id("earmuffs"));

    /** A durabilidade do original: tanta que não se gasta. A conta do elmo a multiplica, e por isso não vai ao topo. */
    public static final int NEVER_WEARS = Integer.MAX_VALUE / 16;

    public static final ArmorMaterial EARMUFFS = new ArmorMaterial(NEVER_WEARS,
            Map.of(ArmorType.HELMET, 0), 1, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
            TagKey.create(Registries.ITEM, Thaumcraft.id("repairs_earmuffs")), EARMUFFS_ASSET);

    private OccultaMaterials() {
    }
}
