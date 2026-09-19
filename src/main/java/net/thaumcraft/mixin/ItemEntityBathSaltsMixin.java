package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Os sais de banho duram só dez segundos soltos no chão (o {@code getEntityLifespan} de 200 do {@code ItemBathSalts}).
 * Quando acabam dentro de uma fonte de água, a água vira fluido purificante (o {@code itemExpire} do
 * {@code EventHandlerEntity}).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityBathSaltsMixin {
    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$bathSalts(CallbackInfo info) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.isRemoved() || self.level().isClientSide() || this.age < 200 || !self.getItem().is(TCItems.BATH_SALTS)) return;
        // no jogo de hoje o item boia: rente à superfície, a fonte pode estar logo abaixo dele
        for (BlockPos pos : new BlockPos[]{self.blockPosition(), self.blockPosition().below()}) {
            var state = self.level().getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                self.level().setBlockAndUpdate(pos, TCBlocks.PURIFYING_FLUID.defaultBlockState());
                break;
            }
        }
        self.discard();
    }
}
