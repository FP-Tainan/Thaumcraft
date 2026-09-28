package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.thaumcraft.arcana.AffinityEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

/**
 * O enderman não encara quem chegou ao fim do Ender: o começo do {@code onEntityLivingBase} do Ars Magica 2.
 *
 * <p>Lá o original limpa o alvo do enderman a cada batida; aqui a recusa entra na hora em que ele escolhe o
 * alvo, que é mais limpo e dá no mesmo. É a recompensa mais silenciosa da roda — e a mais útil no Fim.
 */
@Mixin(EnderMan.class)
public abstract class EnderManAffinityMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$affinityIgnores(@Nullable LivingEntity alvo, CallbackInfo ci) {
        if (alvo != null && AffinityEffects.endermanIgnores((EnderMan) (Object) this, alvo)) ci.cancel();
    }
}
