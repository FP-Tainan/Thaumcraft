package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.wolf.WerewolfPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * <b>O osso que sai da terra</b>: o {@code processDigging} do Witchery, que era um gancho no
 * {@code HarvestDropsEvent} do Forge.
 *
 * <p>É o mesmo lugar onde a <b>Fortuna</b> mexe — o {@link BlockDropsFortuneMixin} —, e os dois não se
 * pisam: a Fortuna entra na <b>cabeça</b>, mexendo na ferramenta antes de a queda se calcular, e o osso entra
 * no <b>fim</b>, somando-se ao que já caiu. É a ordem do original.
 */
@Mixin(Block.class)
public abstract class BlockDropsWolfBoneMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private static void thaumcraft$ossoDeLobo(BlockState qual, ServerLevel level, BlockPos onde,
                                              BlockEntity guarda, Entity quem, ItemInstance ferramenta,
                                              CallbackInfoReturnable<List<ItemStack>> info) {
        List<ItemStack> mais = WerewolfPowers.osso(level, quem, info.getReturnValue());
        if (mais != null) info.setReturnValue(mais);
    }
}
