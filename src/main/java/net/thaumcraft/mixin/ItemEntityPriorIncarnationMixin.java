package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.rite.PriorIncarnation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O que acontece a uma coisa <b>carimbada pela morte</b>: o {@code ItemExpireEvent} e o
 * {@code EntityItemPickupEvent} do {@code RitePriorIncarnation} do Witchery.
 *
 * <p><b>Sumindo de velha</b>, ela não se perde: entra na lista do morto, e a <b>Encarnação Anterior</b>
 * pode devolvê-la. <b>Apanhada</b>, o carimbo sai — o que se recuperou com as mãos não se recupera outra
 * vez por rito.
 *
 * <p>A pergunta pela <b>idade</b> é o que separa sumir de morrer: uma coisa que ardeu no fogo ou caiu no
 * vazio some antes dos cinco minutos, e essa o original também perde.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPriorIncarnationMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$oQueSomeFicaGuardado(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.isRemoved() || self.getAge() < PriorIncarnation.SOME_AOS) return;
        if (!(self.level() instanceof ServerLevel level)) return;
        PriorIncarnation.sumiu(level, self);
    }

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void thaumcraft$oQueSeApanhaDeixaDeSerDeUmMorto(Player quem, CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) return;
        PriorIncarnation.apanhada(self.getItem());
    }
}
