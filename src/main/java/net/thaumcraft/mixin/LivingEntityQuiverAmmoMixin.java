package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.goblin.GoblinClothes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * As <b>flechas sem fim</b> da Aljava do Mog: o {@code onArrowNock} e o {@code onArrowLoose} do
 * {@code ItemGoblinClothes}.
 *
 * <p>O original cancela o disparo do jogo e atira uma flecha sua, de graça. Hoje o caminho é mais curto e
 * dá no mesmo: quando o arco procura flecha e não acha nenhuma, a Aljava <b>lhe dá uma</b> que não está na
 * mochila de ninguém. O jogo a atira, tenta gastá-la, e gasta uma pilha que não existe.
 *
 * <p>E essa flecha <b>não se apanha do chão</b> — o {@link GoblinClothes} trata disso quando ela nasce —,
 * porque senão a Aljava não seria flechas sem fim: seria uma fábrica de flechas.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityQuiverAmmoMixin {
    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$aAljavaDáFlecha(ItemStack arma, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) return;
        if (!GoblinClothes.temAljava((LivingEntity) (Object) this)) return;
        cir.setReturnValue(new ItemStack(Items.ARROW));
    }
}
