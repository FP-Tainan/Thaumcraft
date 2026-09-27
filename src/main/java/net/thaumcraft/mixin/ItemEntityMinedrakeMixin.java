package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.thaumcraft.occulta.MinedrakeEntity;
import net.thaumcraft.occulta.OccultaItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O Bulbo de Mandrágora-de-Mina largado no chão <b>vira bicho</b> ao fim de três segundos.
 *
 * <p>É o {@code getEntityLifespan} de três segundos do {@code BlockWitchCrop} com o {@code onItemExpireEvent} do
 * {@code GenericEvents}: quando o bulbo acaba, nasce dali uma Mandrágora-de-Mina por cada bulbo do monte — e, se
 * quem o largou foi alguém, ela nasce <b>dona dele</b>.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMinedrakeMixin {
    /** Três segundos no chão, que é o que o original dá ao bulbo. */
    private static final int THAUMCRAFT$BULB_LIFE = 60;

    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$minedrake(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.isRemoved() || !(self.level() instanceof ServerLevel level)) return;
        if (this.age < THAUMCRAFT$BULB_LIFE || !self.getItem().is(OccultaItems.MINDRAKE_BULB)) return;

        int quantos = self.getItem().getCount();
        for (int volta = 0; volta < quantos; volta++) {
            MinedrakeEntity bicho = MinedrakeEntity.sprout(level, self);
            if (bicho == null) break;
        }
        self.discard();
    }
}
