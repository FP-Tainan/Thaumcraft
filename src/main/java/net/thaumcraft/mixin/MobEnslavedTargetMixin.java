package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.enslave.Enslavement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Um bicho escravizado <b>não mira em quem o escravizou</b>: é o {@code onLivingSetAttackTarget} do
 * {@code PotionEnslaved}.
 *
 * <p>No original isto é um evento do Forge, que corre depois de o alvo já estar posto e o desfaz. Aqui se
 * atalha na cabeça do método, o que é o mesmo visto de mais perto — e tem a vantagem de o alvo nunca chegar a
 * existir, nem por uma batida.
 *
 * <p><b>Só vale para gente.</b> O original pergunta {@code event.target instanceof EntityPlayer} antes de mais
 * nada, e é de propósito: o laço é com uma pessoa, e um bicho escravizado continua a poder mirar em tudo o
 * mais, inclusive noutro escravo da mesma pessoa.
 */
@Mixin(Mob.class)
public abstract class MobEnslavedTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$nãoMiraNoDono(LivingEntity alvo, CallbackInfo info) {
        if (!(alvo instanceof Player gente)) return;
        Mob self = (Mob) (Object) this;
        if (Enslavement.escravoDe(self, gente)) info.cancel();
    }
}
