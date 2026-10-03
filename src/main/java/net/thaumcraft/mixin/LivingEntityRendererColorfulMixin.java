package net.thaumcraft.mixin;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.thaumcraft.occulta.OccultaEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * O <b>Colorido</b> visto: o {@code PotionColorful}, que era dois ganchos no desenho de quem o tinha.
 *
 * <p>Quem tem a poção fica <b>da cor do grau</b> — as dezesseis tintas do jogo, pela ordem delas. Não faz mais
 * nada, e é a melhor piada do Witchery: o cozimento mais difícil de acertar sem efeito nenhum.
 *
 * <p>No original são duas chamadas de {@code glColor3f} em volta do desenho inteiro. Hoje o desenho leva uma
 * <b>cor</b> consigo, e por isso basta misturá-la — o que também faz a poção conviver com o piscar de quem
 * levou uma pancada, coisa que no original ela apagava.
 *
 * <p>A cor viaja do bicho para o desenho num <b>apego do estado de desenho</b>, que é o lugar que o jogo de
 * hoje tem para isso: o estado se tira numa batida e se desenha noutra, e o bicho já não está à mão quando a
 * cor é precisa.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererColorfulMixin {
    /** A cor que este bicho leva, se levar alguma. */
    private static final RenderStateDataKey<Integer> THAUMCRAFT$COR = RenderStateDataKey.create();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void thaumcraft$pegaACor(LivingEntity bicho, LivingEntityRenderState estado, float parcial,
                                     CallbackInfo info) {
        Integer grau = bicho.getAttached(OccultaEffects.COR);
        if (grau == null) return;
        var cores = DyeColor.values();
        DyeColor qual = cores[Math.clamp(grau, 0, cores.length - 1)];
        estado.setData(THAUMCRAFT$COR, 0xFF000000 | qual.getTextureDiffuseColor());
    }

    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$pinta(LivingEntityRenderState estado, CallbackInfoReturnable<Integer> info) {
        Integer cor = estado.getData(THAUMCRAFT$COR);
        if (cor == null) return;
        info.setReturnValue(thaumcraft$mistura(info.getReturnValue(), cor));
    }

    /** Duas cores misturadas canal a canal, que é o que o {@code glColor3f} fazia ao desenho. */
    private static int thaumcraft$mistura(int era, int cor) {
        int a = (era >>> 24 & 0xFF) * (cor >>> 24 & 0xFF) / 255;
        int r = (era >>> 16 & 0xFF) * (cor >>> 16 & 0xFF) / 255;
        int g = (era >>> 8 & 0xFF) * (cor >>> 8 & 0xFF) / 255;
        int b = (era & 0xFF) * (cor & 0xFF) / 255;
        return a << 24 | r << 16 | g << 8 | b;
    }
}
