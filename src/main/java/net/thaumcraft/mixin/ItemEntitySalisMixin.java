package net.thaumcraft.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.maleficium.SalisItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O sal largado no chão se gasta em cem tiques: é o {@code onEntityItemUpdate} do {@code ItemSalis} do Tainted
 * Magic, que o Minecraft de hoje não tem mais — a coisa solta no mundo não avisa mais o item a cada tique.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySalisMixin {
    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$salis(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.isRemoved() || self.level().isClientSide() || this.age < SalisItem.LIFE) return;
        if (self.getItem().getItem() instanceof SalisItem salis) salis.spend(self);
    }
}
