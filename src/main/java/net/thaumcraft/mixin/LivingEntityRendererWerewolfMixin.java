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
import net.thaumcraft.occulta.client.WerewolfPlayerModels;
import net.thaumcraft.occulta.wolf.Werewolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>Um jogador transformado não se desenha como gente</b>: o {@code TransformWolf} e o
 * {@code TransformWolfman} do Witchery.
 *
 * <p>No original isto é um <b>bicho de mentira</b> guardado à parte, com a posição e o passo copiados do
 * jogador a cada quadro, desenhado no lugar dele. Aqui o desenho já vem separado do que existe: o que se faz
 * é <b>atalhar o desenho do jogador</b> e pôr no lugar dele o modelo do lobo ou do lobisomem, com o mesmo
 * estado.
 *
 * <p>E atalhar o desenho inteiro é o certo, não um atalho: um lobo <b>não veste nada</b>, e as camadas que o
 * jogo desenharia por cima — armadura, capa, elitro, o que ele traz às costas — não têm onde se pôr num
 * bicho. O jogo já lhe tirou tudo isso das mãos quando ele mudou de forma.
 *
 * <p>A forma viaja num <b>apego sincronizado</b>, e por isso o cliente a sabe: é a mesma razão da cor do
 * Colorido, e o mesmo caminho.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererWerewolfMixin {
    /** A forma em que este corpo anda, se for um jogador transformado. */
    private static final RenderStateDataKey<Werewolf.Forma> THAUMCRAFT$FORMA = RenderStateDataKey.create();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void thaumcraft$pegaAForma(LivingEntity quem, LivingEntityRenderState estado, float parcial,
                                       CallbackInfo info) {
        if (!(quem instanceof Player)) return;
        Werewolf.Forma qual = Werewolf.formaDe(quem);
        if (!qual.éBicho()) return;
        estado.setData(THAUMCRAFT$FORMA, qual);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$desenhaOBicho(LivingEntityRenderState estado, PoseStack pose,
                                          SubmitNodeCollector coletor, CameraRenderState câmara,
                                          CallbackInfo info) {
        Werewolf.Forma qual = estado.getData(THAUMCRAFT$FORMA);
        if (qual == null || !qual.éBicho()) return;

        pose.pushPose();
        /*
         * A mesma volta que o desenhista do jogo dá antes de desenhar qualquer vivo, e pela mesma ordem:
         * virar o corpo para onde ele olha, virar o modelo de pernas para o ar (que é como todo modelo de
         * bicho nasce), e só então descer um bloco e meio. Trocar a ordem põe o bicho de pé.
         */
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f - estado.bodyRot));
        pose.scale(-1.0f, -1.0f, 1.0f);

        if (qual == Werewolf.Forma.LOBO) {
            pose.scale(WerewolfPlayerModels.TAMANHO_DO_LOBO, WerewolfPlayerModels.TAMANHO_DO_LOBO,
                    WerewolfPlayerModels.TAMANHO_DO_LOBO);
            pose.translate(0.0f, -1.501f, 0.0f);
            var modelo = WerewolfPlayerModels.lobo();
            var doLobo = WerewolfPlayerModels.comoLobo(estado);
            modelo.setupAnim(doLobo);
            coletor.submitModel(modelo, doLobo, pose,
                    RenderTypes.entityCutout(WerewolfPlayerModels.PELE_LOBO),
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, estado.outlineColor, null);
        } else {
            pose.translate(0.0f, -1.501f, 0.0f);
            var modelo = WerewolfPlayerModels.lobisomem();
            modelo.setupAnim(estado);
            coletor.submitModel(modelo, estado, pose,
                    RenderTypes.entityCutout(WerewolfPlayerModels.PELE_LOBISOMEM),
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, estado.outlineColor, null);
        }

        pose.popPose();
        info.cancel();
    }
}
