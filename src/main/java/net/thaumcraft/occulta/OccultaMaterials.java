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

    /**
     * A cara das roupas de caçador.
     *
     * <p>É uma só, e <b>nada dela se desenha</b>: quem desenha as roupas é o
     * {@link net.thaumcraft.occulta.client.HunterClothesRenderer}, com o modelo próprio do original. A cara
     * existe só porque o jogo pede uma a toda armadura.
     *
     * <p>E <b>não tem arquivo nenhum</b>, como a dos abafadores: o jogo não aceita uma cara com a lista
     * de camadas vazia, e uma cara que ninguém procura é mais honesta do que uma camada de mentira.
     */
    public static final ResourceKey<EquipmentAsset> HUNTER_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id("hunter_clothes"));

    /** A conta de durabilidade do ferro, que é por quanto o jogo multiplica a base de cada peça. */
    public static final int IRON_WEAR = 15;

    /**
     * De que são feitas as <b>roupas de caçador</b>: o {@code ArmorMaterial.CLOTH} com a durabilidade do ferro.
     *
     * <p>É o que o original faz, letra por letra, e é a melhor piada do mod: a roupa <b>protege como couro</b> —
     * um, três, dois e um — mas <b>dura como ferro</b>. Quem caça o que a espada não mata não se protege com
     * placa: anda de casaco, e o casaco aguenta.
     *
     * <p>Elas se <b>tingem</b>, como o couro, e cada peça tem a sua cor de fábrica, que são as do original.
     */
    public static final ArmorMaterial HUNTER = new ArmorMaterial(IRON_WEAR,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 3,
                    ArmorType.LEGGINGS, 2, ArmorType.BOOTS, 1),
            15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
            TagKey.create(Registries.ITEM, Thaumcraft.id("repairs_hunter_clothes")), HUNTER_ASSET);

    private OccultaMaterials() {
    }
}
