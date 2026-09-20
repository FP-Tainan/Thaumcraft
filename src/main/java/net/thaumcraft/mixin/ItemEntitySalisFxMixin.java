package net.thaumcraft.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.maleficium.SalisItem;
import net.thaumcraft.maleficium.client.SalisFx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** As faíscas do sal, do lado de quem joga: o {@code spawnParticles} do {@code ItemSalis}. */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySalisFxMixin {
    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$salisFx(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.level().isClientSide() || !(self.getItem().getItem() instanceof SalisItem salis)) return;
        SalisFx.tick(self, salis, this.age);
    }
}
