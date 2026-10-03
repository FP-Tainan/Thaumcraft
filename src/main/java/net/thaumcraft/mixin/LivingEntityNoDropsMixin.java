package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.NoDrops;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O {@code LivingDropsEvent} cancelado do {@code GenericEvents}: o que o ofício fez nascer morre com as mãos
 * vazias.
 *
 * <p>No original são os <b>itens</b> que se cancelam, e a experiência não — e é o mesmo aqui: o
 * {@code dropAllDeathLoot} é o que larga coisa, e a experiência cai por outro caminho.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityNoDropsMixin {
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$nadaCai(ServerLevel level, DamageSource fonte, CallbackInfo info) {
        if (NoDrops.marcado((LivingEntity) (Object) this)) info.cancel();
    }
}
