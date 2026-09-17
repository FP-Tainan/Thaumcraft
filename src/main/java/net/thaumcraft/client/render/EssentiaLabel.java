package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.item.Revealing;

/**
 * O que os Óculos da Revelação mostram: o aspecto guardado dentro de uma peça, pairando sobre ela.
 *
 * <p>É o traço mais reconhecível do Thaumcraft depois dos nós. Sem os óculos, um jarro é um jarro e um
 * tubo é um cano; com eles no rosto, a sala inteira se enche de símbolos flutuando, um por recipiente,
 * e de relance se lê o que está onde. É desenhado em três dimensões, virado para quem olha, como no
 * original — e não numa caixinha de canto de tela.
 *
 * <p>Quem chama isto é cada desenhista de peça: o do jarro, o do tubo, o do alambique. Assim o rótulo
 * aparece exatamente onde a peça está, sem precisar varrer o mundo atrás delas.
 */
public final class EssentiaLabel {
    /** Até onde os rótulos aparecem, para não encher a tela numa sala grande. */
    private static final double RANGE = 16.0;
    /** O tamanho de um ponto de tela em blocos, o mesmo que as plaquinhas de nome do jogo usam. */
    private static final float PIXEL = 0.025f;
    /** O lado do símbolo, em pontos de tela. */
    private static final float SYMBOL = 13.0f;
    /** O quanto o par símbolo-número se desloca para a esquerda, para ficar centrado no bloco. */
    private static final float SHIFT = 0.0f;

    private EssentiaLabel() {
    }

    /** Vale a pena desenhar rótulo agora? */
    public static boolean visible(net.minecraft.world.level.block.entity.BlockEntity block) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || block.getLevel() == null) return false;
        if (!Revealing.can(minecraft.player)) return false;
        return minecraft.player.blockPosition().distSqr(block.getBlockPos()) <= RANGE * RANGE;
    }

    /**
     * Põe o símbolo do aspecto e o quanto dele pairando sobre a peça.
     *
     * @param height a que altura do bloco o rótulo flutua
     */
    public static void submit(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera,
                              Font font, Aspect aspect, int amount, float height, int light) {
        submit(pose, collector, camera, font, aspect, amount, height, 0.0f, light);
    }

    /**
     * O mesmo, podendo trazer o rótulo para a frente da peça.
     *
     * <p>Peça que se empilha — o alambique — não tem ar livre por cima: o rótulo cairia dentro do corpo da
     * de cima e sumiria. Então ele sai <em>na direção de quem olha</em>, e assim passa na frente do bloco
     * de qualquer ângulo.
     *
     * @param forward o quanto o rótulo avança para fora, em blocos, rumo à câmera
     */
    public static void submit(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera,
                              Font font, Aspect aspect, int amount, float height, float forward, int light) {
        if (aspect == null) return;
        org.joml.Vector3f toward = camera.orientation.transform(new org.joml.Vector3f(0.0f, 0.0f, 1.0f));
        toward.mul(forward);
        Identifier symbol = Thaumcraft.id("textures/aspects/" + aspect.tag() + ".png");
        int color = 0xFF000000 | aspect.color();

        pose.pushPose();
        pose.translate(0.5f + toward.x, height + toward.y, 0.5f + toward.z);
        pose.mulPose(camera.orientation);
        // o mesmo fator das plaquinhas de nome, e o eixo virado porque o texto desce na tela
        pose.scale(-PIXEL, -PIXEL, PIXEL);

        // o símbolo, à esquerda
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(symbol), (matrix, consumer) -> {
            float half = SYMBOL / 2.0f;
            float at = -half - SHIFT;
            consumer.addVertex(matrix, at, half, 0.0f).setColor(color).setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, at + SYMBOL, half, 0.0f).setColor(color).setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, at + SYMBOL, -half, 0.0f).setColor(color).setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, at, -half, 0.0f).setColor(color).setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
        });

        pose.popPose();

        // e o número, pela mesma porta que as plaquinhas de nome do jogo usam: ela já cuida de virar o
        // texto para quem olha e de pôr o fundo atrás, que é o que faz ele se ler contra o céu
        pose.pushPose();
        pose.translate(0.5f + toward.x, height + toward.y, 0.5f + toward.z);
        collector.submitNameTag(pose, new net.minecraft.world.phys.Vec3(0.0, -0.62, 0.0), 0,
                Component.literal(String.valueOf(amount)), false, light, camera);
        pose.popPose();
    }
}
