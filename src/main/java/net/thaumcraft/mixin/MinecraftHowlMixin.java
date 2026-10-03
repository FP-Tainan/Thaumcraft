package net.thaumcraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.thaumcraft.occulta.wolf.WerewolfHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>O gesto do uivo</b>: o pedaço do {@code GenericEvents.onPlayerInteract} que manda o {@code PacketHowl}.
 *
 * <p>Atira-se a cabeça <b>direito para cima</b>, agacha-se, e aperta-se o botão de usar. Não há tecla, não há
 * item, não há menu — e não está escrito em lugar nenhum do jogo. Quem descobre o uivo descobre-o por ter
 * olhado para a lua.
 *
 * <p>O {@code -90} é exato de propósito: o jogo trava o olhar nos noventa graus, e por isso "direito para
 * cima" é um número e não um intervalo. Quem olha a oitenta e nove não uiva.
 *
 * <p>Por que um mixin e não o {@code UseItemCallback}: com a <b>mão vazia</b> e o ar à frente, o jogo de hoje
 * nem fala com o servidor — não há evento nenhum para se ouvir. O Forge de então chamava o
 * {@code PlayerInteractEvent} de qualquer jeito, e é esse jeito que se repõe aqui.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftHowlMixin {
    /** Direito para cima: o travão do olhar. */
    private static final float THAUMCRAFT$PARA_CIMA = -90.0f;

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void thaumcraft$uiva(CallbackInfo info) {
        LocalPlayer quem = ((Minecraft) (Object) this).player;
        if (quem == null) return;
        if (quem.getXRot() != THAUMCRAFT$PARA_CIMA || !quem.isShiftKeyDown()) return;
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new WerewolfHooks.Uivo());
    }
}
