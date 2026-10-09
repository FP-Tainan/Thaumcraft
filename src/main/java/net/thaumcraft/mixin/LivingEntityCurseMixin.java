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
 *
 * <p><b>E cada coisa tem o seu compasso</b>, que é o do original e não o de cada batida: as maldições mordem
 * <b>de segundo em segundo</b> — o {@code handleCurseEffects} está, nos dois lugares de onde é chamado,
 * dentro de um {@code counter % 20 == 0} — e o <b>Grotesco</b> empurra <b>de quatro em quatro batidas</b>,
 * que é onde o {@code handleBrewGrotesqueEffect} fica.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCurseMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$curses(CallbackInfo ci) {
        LivingEntity eu = (LivingEntity) (Object) this;
        if (!(eu.level() instanceof ServerLevel mundo)) return;
        long conta = mundo.getGameTime();
        if (conta % 20L == 0L) Curse.tick(mundo, eu);
        // e a cor do Colorido, que precisa de viajar até quem desenha
        net.thaumcraft.occulta.OccultaEffects.tickColour(mundo, eu);
        if (conta % 4L == 0L && eu instanceof net.minecraft.world.entity.player.Player gente) {
            net.thaumcraft.occulta.curse.Grotesque.tick(mundo, gente);
        }
    }
}
