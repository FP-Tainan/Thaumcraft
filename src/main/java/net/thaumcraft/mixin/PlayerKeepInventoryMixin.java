package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.OccultaEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>Guardar o Que Se Tem</b>: o {@code PotionKeepInventory}, que era um gancho no {@code PlayerDropsEvent}.
 *
 * <p>Quem morre com ela <b>não larga nada</b>. É a poção mais cara do mod a fazer, e é a única razão de
 * alguém entrar numa dimensão que não conhece.
 *
 * <p>O gancho é o <b>largar o que se veste</b> do jogador, que é por onde a mochila inteira cai quando a
 * regra do mundo não a guarda. Cancelado ele, nada cai — e a mochila segue com quem acorda, porque o jogo
 * novo só a esvazia aqui.
 */
@Mixin(Player.class)
public abstract class PlayerKeepInventoryMixin {
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$guardaTudo(ServerLevel level, CallbackInfo info) {
        Player self = (Player) (Object) this;
        if (self.hasEffect(OccultaEffects.KEEP_INVENTORY)) info.cancel();
    }
}
