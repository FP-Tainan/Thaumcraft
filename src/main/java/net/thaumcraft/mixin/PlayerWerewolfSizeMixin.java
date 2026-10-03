package net.thaumcraft.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.wolf.Werewolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <b>Um lobo cabe onde uma pessoa não cabe</b>: o {@code EntitySizeInfo} do Witchery.
 *
 * <p>Um jogador em forma de <b>lobo</b> mede <b>oito décimos</b> de altura em vez de um e oito — menos de um
 * bloco. Passa por baixo de um alçapão, entra numa toca de um bloco, e enfia-se onde nada de pé entra.
 *
 * <p>É a melhor razão para virar lobo que o mod tem, e é a única que não é um número de combate.
 *
 * <p>O <b>lobisomem</b> fica do tamanho de gente, e o que ele ganha é a <b>passada</b> — um bloco inteiro de
 * degrau, que vai pelo atributo e está em {@link net.thaumcraft.occulta.wolf.WerewolfStats}.
 */
@Mixin(net.minecraft.world.entity.Avatar.class)
public abstract class PlayerWerewolfSizeMixin {
    /** O tamanho de um lobo: o seis e o oito décimos do original. */
    private static final float THAUMCRAFT$LARGURA = 0.6f;
    private static final float THAUMCRAFT$ALTURA = 0.8f;

    /** E onde ficam os olhos dele: noventa e dois centésimos da altura. */
    private static final float THAUMCRAFT$OLHOS = 0.92f;

    @Inject(method = "getDefaultDimensions", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$tamanhoDeLobo(Pose pose, CallbackInfoReturnable<EntityDimensions> info) {
        if (!(((Object) this) instanceof Player self)) return;
        if (Werewolf.formaDe(self) != Werewolf.Forma.LOBO) return;
        info.setReturnValue(EntityDimensions.scalable(THAUMCRAFT$LARGURA, THAUMCRAFT$ALTURA)
                .withEyeHeight(THAUMCRAFT$ALTURA * THAUMCRAFT$OLHOS));
    }
}
