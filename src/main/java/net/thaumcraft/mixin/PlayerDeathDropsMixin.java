package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.rite.PriorIncarnation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * O <b>carimbo da morte</b>: o {@code PlayerDropsEvent} do {@code RitePriorIncarnation} do Witchery.
 *
 * <p>Tudo o que cai de quem morre fica marcado com o número dele, e o lugar da morte fica guardado. É esse
 * carimbo que, muito depois, deixa a <b>Encarnação Anterior</b> saber de quem era a coisa que sumiu.
 *
 * <p>São dois ganchos porque o jogo não tem um só: um diz <b>quando</b> a morte está largando coisas, o
 * outro carimba <b>cada</b> coisa largada enquanto isso dura. Fora da morte, largar uma coisa não carimba
 * nada.
 */
@Mixin(Player.class)
public abstract class PlayerDeathDropsMixin {
    @Unique
    private boolean thaumcraft$morrendo;

    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void thaumcraft$começaAMorrer(ServerLevel level, CallbackInfo info) {
        this.thaumcraft$morrendo = PriorIncarnation.morreu((Player) (Object) this);
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void thaumcraft$acabouDeMorrer(ServerLevel level, CallbackInfo info) {
        this.thaumcraft$morrendo = false;
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"))
    private void thaumcraft$carimbaOQueCai(ItemStack oquê, boolean àFrente,
                                           CallbackInfoReturnable<ItemEntity> saída) {
        if (!this.thaumcraft$morrendo) return;
        PriorIncarnation.carimba(oquê, (Player) (Object) this);
    }
}
