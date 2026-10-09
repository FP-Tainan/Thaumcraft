package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.demon.ImpBlessings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * O <b>Toque de Fundir</b> do Contrato de Fundir: o {@code doHarvest} do {@code IMP_METLING_TOUCH} do
 * Witchery, que lá era um gancho no {@code HarvestDropsEvent} do Forge.
 *
 * <p>Hoje a queda de um bloco sai do {@code Block.getDrops}, e é aqui que ela se apanha: cada coisa que
 * caiu passa pela receita de fornalha, e <b>uma vez em quatro</b> sai uma a mais.
 *
 * <p>Como no original, o que <b>guarda coisas dentro</b> fica de fora — um baú partido não dá um baú
 * fundido —, e o que não tiver receita de fornalha cai como caía.
 */
@Mixin(Block.class)
public abstract class BlockDropsMeltingMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private static void thaumcraft$melt(BlockState qual, ServerLevel level, BlockPos onde,
                                        BlockEntity guarda, Entity quem, ItemInstance ferramenta,
                                        CallbackInfoReturnable<List<ItemStack>> cir) {
        if (guarda != null) return;
        if (!(quem instanceof LivingEntity vivo)) return;
        List<ItemStack> caiu = cir.getReturnValue();
        if (caiu == null || caiu.isEmpty()) return;

        List<ItemStack> fundido = ImpBlessings.funde(level, vivo, caiu);
        if (fundido != caiu) cir.setReturnValue(fundido);
    }
}
