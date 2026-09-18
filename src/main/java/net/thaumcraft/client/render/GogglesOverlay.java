package net.thaumcraft.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.item.Revealing;
import net.thaumcraft.research.Knowledges;

import java.util.function.Function;

/**
 * O que os Óculos da Revelação mostram. É o {@code drawTagsOnContainer} do {@code RenderEventHandler} da
 * 4.2.3.5, descompilado.
 *
 * <p>Aparece só no recipiente <strong>na mira</strong>, e o lugar depende do que há em cima dele: com ar
 * em cima, os símbolos flutuam acima; com alguma coisa em cima — e o jarro quase sempre tem o cano —, eles
 * saem na <em>face que se está olhando</em>, um pouco à frente dela. É por isso que, no original, o que se
 * vê num jarro encanado é o aspecto estampado no vidro e não pairando sobre a rolha.
 *
 * <p>Os símbolos viram para o jogador só no giro, de pé; são desenhados por cima de tudo, como o original
 * faz desligando a profundidade; e crescem de leve quando se mira, até três décimos de bloco. Mais de
 * cinco aspectos quebram em outra linha. Aspecto que o jogador ainda não descobriu aparece como o
 * símbolo desconhecido.
 */
public final class GogglesOverlay {
    /**
     * O desenho do símbolo por cima de tudo: o {@code eyes} do jogo, com mistura comum, passando sempre no
     * teste de profundidade — aparece através das paredes, como no original — mas <em>gravando</em> a
     * profundidade dele. Sem gravar, o vidro do jarro, que o jogo pinta depois, passava por cima do
     * rótulo e o manchava.
     */
    private static final RenderPipeline SEE_THROUGH = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Thaumcraft.id("pipeline/goggles_tag"))
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    // o fundo vazado do símbolo é jogado fora, senão ele gravaria profundidade num quadrado
                    // inteiro e abriria um buraco no vidro atrás
                    .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(new com.mojang.blaze3d.pipeline.DepthStencilState(
                            com.mojang.blaze3d.platform.CompareOp.ALWAYS_PASS, true))
                    .build());
    private static final Function<Identifier, RenderType> TAG = Util.memoize(texture ->
            RenderType.create("thaumcraft_goggles_tag", RenderSetup.builder(SEE_THROUGH)
                    .withTexture("Sampler0", texture)
                    .sortOnUpload()
                    .createRenderSetup()));

    /** O brilho com que o original acende o símbolo, sem depender da luz do lugar. */
    private static final int BRIGHT = 220;
    /** Até onde os símbolos crescem. */
    private static final float FULL = 0.3f;
    /** Quantos símbolos cabem por linha. */
    private static final int ROW = 5;

    /** O tamanho de agora; cresce enquanto se mira e volta a zero quando se larga. */
    private static float tagscale;
    private static BlockPos target;
    private static Direction face = Direction.UP;
    private static boolean spaceAbove;

    private GogglesOverlay() {
    }

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(GogglesOverlay::update);
    }

    /**
     * A cada quadro: quem está na mira, o quanto o rótulo já cresceu, e o desenho dele.
     *
     * <p>Vale para qualquer recipiente de aspecto, como no original — jarro, alambique, forno, tubo —,
     * sem cada peça precisar saber dos óculos.
     */
    private static void update(LevelRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || !Revealing.can(player)) {
            forget();
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
            forget();
            return;
        }
        BlockPos pos = block.getBlockPos();
        AspectList tags = contents(minecraft.level.getBlockEntity(pos));
        if (tags == null || tags.size() == 0) {
            forget();
            return;
        }
        if (!pos.equals(target)) {
            target = pos.immutable();
            tagscale = 0.0f;
        }
        if (tagscale < FULL) tagscale += 0.031f - tagscale / 10.0f;
        spaceAbove = minecraft.level.isEmptyBlock(pos.above());
        face = spaceAbove ? Direction.UP : block.getDirection();

        Snapshot tag = snapshot(minecraft.level.getBlockEntity(pos));
        if (tag == null) return;
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack pose = context.poseStack();
        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        submit(tag, pose, context.submitNodeCollector(), minecraft.font);
        pose.popPose();
    }

    /** O retrato do rótulo neste quadro, ou nada se aquele bloco não está na mira. */
    private record Snapshot(AspectList tags, Direction face, boolean spaceAbove, float scale, float yaw) {
    }

    @org.jetbrains.annotations.Nullable
    private static Snapshot snapshot(BlockEntity block) {
        if (block == null) return null;
        if (target == null || tagscale <= 0.0f || !block.getBlockPos().equals(target)) return null;
        AspectList tags = contents(block);
        if (tags == null || tags.size() == 0) return null;
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 feet = player.getPosition(partial);
        BlockPos pos = block.getBlockPos();
        float yaw = (float) Math.toDegrees(Math.atan2(feet.x - (pos.getX() + 0.5), feet.z - (pos.getZ() + 0.5)));
        return new Snapshot(tags, face, spaceAbove, tagscale, yaw);
    }

    /** Desenha o rótulo, com a pose na origem do bloco. */
    private static void submit(@org.jetbrains.annotations.Nullable Snapshot tag, PoseStack pose,
                              SubmitNodeCollector collector, Font font) {
        if (tag == null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        var knowledge = Knowledges.of(player);
        float scale = tag.scale();
        Direction dir = tag.face();

        int current = 0;
        float shiftY = 0.0f;
        int left = tag.tags().size();
        for (Aspect aspect : tag.tags().getAspects()) {
            int across = Math.min(left, ROW);
            if (current >= ROW) {
                current = 0;
                shiftY -= scale * 1.05f;
                left -= ROW;
                if (left < ROW) across = left % ROW;
            }
            float shift = (current - across / 2.0f + 0.5f) * scale * 4.0f * scale;

            pose.pushPose();
            pose.translate(0.5f + scale * 2.0f * dir.getStepX(),
                    (tag.spaceAbove() ? 0.4f : 0.0f) - shiftY + 0.5f + scale * 2.0f * dir.getStepY(),
                    0.5f + scale * 2.0f * dir.getStepZ());
            // o original gira para o jogador mais meia volta e depois vira o plano de cabeça para baixo,
            // o que deixa a figura certa mas de costas. No 1.7.10 isso não importava; no 26.2 o texto de
            // costas é descartado. Girando só para o jogador e espelhando o eixo de cima, a figura sai
            // idêntica — mesmo lado para a direita, mesmo lado para cima — e de frente
            pose.mulPose(Axis.YP.rotationDegrees(tag.yaw()));
            pose.translate(-shift, 0.0f, 0.0f);
            pose.scale(scale, -scale, scale);

            boolean known = knowledge.hasDiscovered(aspect);
            symbol(pose, collector, known ? aspect.tag() : "_unknown", aspect.color());
            amount(pose, collector, font, tag.tags().getAmount(aspect));
            pose.popPose();
            current++;
        }
    }

    private static void forget() {
        tagscale = 0.0f;
        target = null;
    }

    /** O que o recipiente guarda: a lista de um jarro ou alambique, ou o que corre dentro de um tubo. */
    private static AspectList contents(BlockEntity block) {
        if (block instanceof AspectContainer container) return container.getAspects();
        if (block instanceof EssentiaTransport transport && transport.getEssentiaAmount(null) > 0
                && transport.getEssentiaType(null) != null) {
            return new AspectList().add(transport.getEssentiaType(null), transport.getEssentiaAmount(null));
        }
        return null;
    }

    /** O símbolo, um quadrado de um de lado centrado, na cor do aspecto e com três quartos de opacidade. */
    private static void symbol(PoseStack pose, SubmitNodeCollector collector, String tag, int rgb) {
        Identifier texture = Thaumcraft.id("textures/aspects/" + tag + ".png");
        int colour = 0xBF000000 | (rgb & 0xFFFFFF);
        collector.submitCustomGeometry(pose, TAG.apply(texture), (matrix, consumer) -> {
            consumer.addVertex(matrix, -0.5f, 0.5f, 0.0f).setColor(colour).setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(BRIGHT).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, 0.5f, 0.5f, 0.0f).setColor(colour).setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(BRIGHT).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, 0.5f, -0.5f, 0.0f).setColor(colour).setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(BRIGHT).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, -0.5f, -0.5f, 0.0f).setColor(colour).setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(BRIGHT).setNormal(matrix, 0.0f, 0.0f, 1.0f);
        });
    }

    /** O número no canto de baixo à direita do símbolo, branco com a sombra escura por trás. */
    private static void amount(PoseStack pose, SubmitNodeCollector collector, Font font, int amount) {
        if (amount < 0) return;
        String text = Integer.toString(amount);
        int width = font.width(text);
        pose.pushPose();
        pose.scale(0.04f, 0.04f, 0.04f);
        pose.translate(0.0f, 6.0f, 0.1f);
        // a ordem dos números do submitText é luz, cor, fundo e contorno — é assim que as placas do jogo
        // chamam. Com a cor no lugar da luz o texto saía preto e transparente, e parecia não desenhar
        collector.submitText(pose, 14 - width, 1, Component.literal(text).getVisualOrderText(), false,
                Font.DisplayMode.SEE_THROUGH, BRIGHT, 0xFF111111, 0, 0);
        pose.translate(0.0f, 0.0f, 0.1f);
        collector.submitText(pose, 13 - width, 0, Component.literal(text).getVisualOrderText(), false,
                Font.DisplayMode.SEE_THROUGH, BRIGHT, 0xFFFFFFFF, 0, 0);
        // e de novo pelo desenho comum, que grava profundidade: é o que as plaquinhas de nome do jogo
        // fazem, e é o que impede o vidro pintado depois de passar por cima do número
        pose.translate(0.0f, 0.0f, 0.01f);
        collector.submitText(pose, 14 - width, 1, Component.literal(text).getVisualOrderText(), false,
                Font.DisplayMode.NORMAL, BRIGHT, 0xFF111111, 0, 0);
        pose.translate(0.0f, 0.0f, 0.01f);
        collector.submitText(pose, 13 - width, 0, Component.literal(text).getVisualOrderText(), false,
                Font.DisplayMode.NORMAL, BRIGHT, 0xFFFFFFFF, 0, 0);
        pose.popPose();
    }
}
