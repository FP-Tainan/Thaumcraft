package net.thaumcraft.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.thaumcraft.item.FortressArmorItem;
import net.thaumcraft.registry.TCComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A proteção especial da armadura de fortaleza: o {@code getProperties} do {@code ItemFortressArmor} com o
 * {@code ArmorProperties.ApplyArmor} do Forge 1.7.10, que o jogo de hoje não tem.
 *
 * <p>Só entra em cena para jogadores (o {@code EntityPlayer.damageEntity} do 1.7.10 é quem chama o {@code ApplyArmor})
 * com alguma peça de fortaleza vestida. Cada peça tira, nesta ordem: magia dano/35, fogo e explosão dano/20 (esses
 * com prioridade, descontados antes), nada do que atravessa armadura, e o resto dano/25 — por isso ela protege de
 * magia e de fogo, que no jogo de hoje (e no de 2014) atravessam armadura;
 * tudo multiplicado pelo conjunto — 0,875, mais 0,125 por peça de fortaleza e 0,05 por máscara. As outras peças
 * vestidas entram como no Forge, com a armadura delas sobre 25.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin {
    @Shadow
    protected abstract void hurtArmor(DamageSource source, float damage);

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$fortress(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof net.minecraft.world.entity.player.Player)) return;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        boolean any = false;
        double set = 0.875;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            ItemStack piece = self.getItemBySlot(slot);
            if (piece.getItem() instanceof FortressArmorItem) {
                any = true;
                set += 0.125;
                if (piece.has(TCComponents.FORTRESS_MASK)) set += 0.05;
            }
        }
        if (!any) return;
        boolean unblockable = source.is(DamageTypeTags.BYPASSES_ARMOR);

        boolean magic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
        boolean fireOrBlast = source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        double priority = 0.0, normal = 0.0;
        for (EquipmentSlot slot : slots) {
            ItemStack piece = self.getItemBySlot(slot);
            if (piece.isEmpty()) continue;
            double armor = armorOf(piece);
            if (piece.getItem() instanceof FortressArmorItem) {
                if (magic) priority += armor / 35.0 * set;
                else if (fireOrBlast) priority += armor / 20.0 * set;
                else if (!unblockable) normal += armor / 25.0 * set;
            } else if (!unblockable) {
                normal += armor / 25.0;
            }
        }
        if (!unblockable) this.hurtArmor(source, damage);
        float left = damage * (float) (1.0 - Math.min(1.0, priority));
        left *= (float) (1.0 - Math.min(1.0, normal));
        cir.setReturnValue(left);
    }

    /** A armadura que a peça dá: os modificadores de armadura do próprio item. */
    private static double armorOf(ItemStack piece) {
        ItemAttributeModifiers modifiers = piece.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double total = 0.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR)) total += entry.modifier().amount();
        }
        return total;
    }
}
