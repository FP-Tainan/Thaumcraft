package net.thaumcraft.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.thaumcraft.occulta.hunter.HunterClothes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * As <b>roupas de caçador</b> contra quem elas foram feitas: o {@code getProperties} e o {@code damageArmor} do
 * {@code ItemHunterClothes}, que em 2014 eram o {@code ISpecialArmor} do Forge.
 *
 * <p>Uma peça <b>prateada</b> contra a pancada de um <b>lobisomem</b> — ou uma <b>com alho</b> contra a de um
 * <b>vampiro</b> — vale <b>duas vezes e meia</b> a armadura dela, <b>não se gasta</b> nessa pancada, e
 * <b>queima quem bateu</b>.
 *
 * <p><b>Como a conta se traduz.</b> No Forge de então cada peça dizia quanto absorvia: o comum era
 * {@code armadura / 25}, e a peça certa dizia {@code armadura * 2,5 / 25}. A armadura comum já é aplicada pelo
 * jogo de hoje antes de isto correr, e por isso o que se tira aqui é <b>só o que falta</b> —
 * {@code armadura * 1,5 / 25} por peça certa. Com o conjunto prateado inteiro, que dá sete de armadura, são
 * quarenta e dois por cento a menos do que já sobrou. <b>Declarado no {@code PORTE.md}.</b>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHunterArmorMixin {
    /** O que falta tirar por ponto de armadura da peça certa: o dois e meio menos o um do original. */
    private static final double THAUMCRAFT$VALE_MAIS = 1.5;

    /** E por quanto o Forge de então dividia a armadura. */
    private static final double THAUMCRAFT$SOBRE = 25.0;

    private static final EquipmentSlot[] THAUMCRAFT$CASAS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
    };

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$roupasDeCaçador(DamageSource fonte, float dano,
                                            CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        double tira = 0.0;
        for (EquipmentSlot casa : THAUMCRAFT$CASAS) {
            ItemStack peça = self.getItemBySlot(casa);
            if (!HunterClothes.peçaCerta(peça, fonte)) continue;
            tira += thaumcraft$armaduraDe(peça) * THAUMCRAFT$VALE_MAIS / THAUMCRAFT$SOBRE;
        }
        if (tira <= 0.0) return;

        // quem bateu arde: o um de fogo que o original devolve
        Entity quemBateu = fonte.getEntity();
        if (quemBateu instanceof LivingEntity vivo && self.level() instanceof ServerLevel level) {
            vivo.hurtServer(level, self.damageSources().onFire(), HunterClothes.QUEIMA);
        }

        cir.setReturnValue(cir.getReturnValue() * (float) (1.0 - Math.min(1.0, tira)));
    }

    /**
     * E a peça certa <b>não se gasta</b> nessa pancada: o {@code damageArmor} do original.
     *
     * <p><b>Desvio declarado:</b> o original poupa <b>peça a peça</b>, e aqui se poupa o conjunto inteiro
     * quando alguma peça é a certa. Com o conjunto todo do mesmo feitio — que é o único caso em que as
     * proteções valem — dá exatamente o mesmo; a diferença só aparece num conjunto misturado, e é a favor de
     * quem o veste.
     */
    @Inject(method = "hurtArmor", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$aPeçaCertaNãoSeGasta(DamageSource fonte, float dano, CallbackInfo info) {
        if (HunterClothes.algumaPeçaCerta((LivingEntity) (Object) this, fonte)) info.cancel();
    }

    /** A armadura que a peça dá, lida dos modificadores dela. */
    private static double thaumcraft$armaduraDe(ItemStack peça) {
        ItemAttributeModifiers modificadores =
                peça.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double soma = 0.0;
        for (ItemAttributeModifiers.Entry entrada : modificadores.modifiers()) {
            if (entrada.attribute().equals(Attributes.ARMOR)) soma += entrada.modifier().amount();
        }
        return soma;
    }
}
