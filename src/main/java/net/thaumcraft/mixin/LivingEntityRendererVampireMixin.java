package net.thaumcraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.client.VampireBatModels;
import net.thaumcraft.occulta.vampire.VampirePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>Um vampiro em forma de morcego não se desenha como gente</b>: o {@code TransformBat} do Witchery.
 *
 * <p>É a mesma costura do {@linkplain LivingEntityRendererWerewolfMixin lobo} e pela mesma razão — atalha-se
 * o desenho do jogador e põe-se no lugar dele o modelo do bicho —, mas com uma diferença que vale a pena:
 * aqui são <b>três</b> bichos e não um.
 *
 * <p>O original desenha o morcego de mentira três vezes: um no lugar do jogador e dois atrás, mais baixos,
 * menores e com as asas fora de compasso. O que se vê não é um morcego — é uma <b>nuvenzinha</b> deles, e
 * quem vira morcego descobre que virou <b>vários</b>. A conta de onde cada um fica, e o erro de unidade do
 * autor que a torna simétrica por acaso, estão no {@link VampireBatModels}.
 *
 * <p>E atalhar o desenho inteiro é o certo e não um atalho: um morcego <b>não veste nada</b>, e as camadas
 * que o jogo desenharia por cima — armadura, capa, elitro — não têm onde se pôr num bicho de meio bloco.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererVampireMixin {
    /** Se este corpo é um vampiro em forma de morcego. */
    private static final RenderStateDataKey<Boolean> THAUMCRAFT$MORCEGO = RenderStateDataKey.create();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void thaumcraft$pegaOMorcego(LivingEntity quem, LivingEntityRenderState estado, float parcial,
                                         CallbackInfo info) {
        if (!(quem instanceof Player gente)) return;
        if (!VampirePowers.emMorcego(gente)) return;
        estado.setData(THAUMCRAFT$MORCEGO, true);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$desenhaOsMorcegos(LivingEntityRenderState estado, PoseStack pose,
                                              SubmitNodeCollector coletor, CameraRenderState câmara,
                                              CallbackInfo info) {
        if (estado.getData(THAUMCRAFT$MORCEGO) == null) return;

        var modelo = VampireBatModels.modelo();
        for (int qual = 0; qual < VampireBatModels.QUANTOS; qual++) {
            var onde = VampireBatModels.onde(estado, qual);
            pose.pushPose();

            /*
             * Os dois de trás encolhem antes de se afastarem, e é de propósito: no original o encolher é uma
             * escala que já está em vigor quando o bicho se põe no lugar dele, e por isso ela encolhe também
             * o afastamento. Três quartos de bloco a oito décimos dão seis décimos, e é o que se vê.
             */
            if (qual != 0) {
                pose.scale(VampireBatModels.TAMANHO, VampireBatModels.TAMANHO, VampireBatModels.TAMANHO);
                pose.translate(onde.x, onde.y, onde.z);
            }

            // e então a mesma volta que o desenhista do jogo dá antes de desenhar qualquer vivo
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f - estado.bodyRot));
            pose.scale(-1.0f, -1.0f, 1.0f);
            pose.translate(0.0f, -1.501f, 0.0f);

            var doMorcego = VampireBatModels.como(estado, qual);
            modelo.setupAnim(doMorcego);
            coletor.submitModel(modelo, doMorcego, pose,
                    RenderTypes.entityCutout(VampireBatModels.PELE),
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, estado.outlineColor, null);

            pose.popPose();
        }
        info.cancel();
    }
}
