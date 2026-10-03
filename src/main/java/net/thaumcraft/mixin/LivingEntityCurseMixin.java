package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.curse.Curse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A batida em que as maldições mordem: o {@code handleCurseEffects} do {@code Infusion.EventHooks}.
 *
 * <p>É a batida de <b>cada vivo</b>, e não a do mundo, porque é isso que o original faz — e porque uma
 * maldição é de quem a tem, e não do lugar. Quem não tem nenhuma sai na primeira linha, e por isso isto custa
 * uma consulta a um apego vazio por vivo por batida.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCurseMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$curses(CallbackInfo ci) {
        LivingEntity eu = (LivingEntity) (Object) this;
        if (!(eu.level() instanceof ServerLevel mundo)) return;
        Curse.tick(mundo, eu);
        if (eu instanceof net.minecraft.world.entity.player.Player gente) {
            net.thaumcraft.occulta.curse.Grotesque.tick(mundo, gente);
        }
    }
}
