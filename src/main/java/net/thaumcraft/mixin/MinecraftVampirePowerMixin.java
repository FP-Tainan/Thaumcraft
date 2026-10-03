package net.thaumcraft.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.vampire.VampireHooks;
import net.thaumcraft.occulta.vampire.VampirePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>O botão de usar é o poder</b>: o ramo do {@code GenericEvents.onPlayerInteract} que dispara o poder
 * escolhido de um vampiro.
 *
 * <p>Com um poder escolhido, o clique direito <b>deixa de ser o clique direito</b>. Não abre baús, não põe
 * blocos, não come, não bebe: ele <b>é</b> o poder. E escolhendo <i>nenhum</i>, tudo volta ao normal.
 *
 * <p>Isto é o painel de comando inteiro de um vampiro, e é a decisão de desenho mais forte do mod: ele não
 * tem menu, não tem roda, não tem varinha. Tem uma palavra no canto da tela e o botão que ele já usava.
 *
 * <p>Por isso o clique é <b>engolido</b> mesmo quando o poder escolhido é o de <b>beber</b>, que não se usa
 * no ar: um vampiro com a boca pronta não põe uma tocha na parede por acidente.
 *
 * <p><b>Tocar num bicho não passa por aqui</b> — isso é o {@code onEntityInteract}, e é onde o
 * {@linkplain VampirePowers#prende prender} e o {@linkplain VampirePowers#bebe beber} moram. O clique no
 * nada e o clique num bloco são estes.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftVampirePowerMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$oPoder(CallbackInfo info) {
        Minecraft minecraft = (Minecraft) (Object) this;
        LocalPlayer quem = minecraft.player;
        if (quem == null) return;

        // tocar num bicho é outro caminho, e é lá que o prender e o beber vivem
        HitResult onde = minecraft.hitResult;
        if (onde != null && onde.getType() == HitResult.Type.ENTITY) return;

        VampirePowers.Poder qual = VampirePowers.escolhido(quem);
        if (qual == VampirePowers.Poder.NENHUM) return;

        if (qual != VampirePowers.Poder.BEBER) ClientPlayNetworking.send(new VampireHooks.Usa());
        info.cancel();
    }
}
