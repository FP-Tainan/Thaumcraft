package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.ScanManager;

import java.text.DecimalFormat;

/**
 * O thaumômetro na mão, em primeira pessoa: o {@code ItemThaumometerRenderer} da 4.2.3.5.
 *
 * <p>O aparelho fica erguido à frente, seguro pelas duas mãos (as duas com o braço direito do modelo, como no original,
 * uma de cada lado da moldura), e o que está na mira aparece escrito no vidro: o nome, os símbolos do que aquilo é feito
 * — só depois de examinado — e, num nodo, o tipo dele. O vidro tremula de leve. As transformações são as do original,
 * passo a passo: primeiro as que o {@code ItemRenderer} de então aplicava a todo item na mão (o balanço do golpe, o
 * {@code 0.56, -0.52, -0.72}, os 45 graus e a escala de 0,4, e o meio bloco do {@code EQUIPPED_BLOCK}), depois as do
 * próprio desenhista.
 */
public final class ThaumometerFirstPerson {
    private static final Identifier BRASS = Thaumcraft.id("textures/item/scanner.png");
    private static final Identifier LENS = Thaumcraft.id("textures/item/scanscreen.png");
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");

    private ThaumometerFirstPerson() {
    }

    public static void submit(AbstractClientPlayer player, float partial, float attack, float inverseArmHeight,
                              PoseStack pose, SubmitNodeCollector collector, int light) {
        Minecraft mc = Minecraft.getInstance();
        float var7 = 0.8f;
        pose.pushPose();
        // o ItemRenderer.renderItemInFirstPerson de então, para qualquer item
        float f6 = Mth.sin(attack * (float) Math.PI);
        float f7 = Mth.sin(Mth.sqrt(attack) * (float) Math.PI);
        pose.translate(-f7 * 0.4f, Mth.sin(Mth.sqrt(attack) * (float) Math.PI * 2.0f) * 0.2f, -f6 * 0.2f);
        pose.translate(0.7f * var7, -0.65f * var7 - inverseArmHeight * 0.6f, -0.9f * var7);
        pose.mulPose(Axis.YP.rotationDegrees(45.0f));
        float g6 = Mth.sin(attack * attack * (float) Math.PI);
        pose.mulPose(Axis.YP.rotationDegrees(-g6 * 20.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(-f7 * 20.0f));
        pose.mulPose(Axis.XP.rotationDegrees(-f7 * 80.0f));
        pose.scale(0.4f, 0.4f, 0.4f);
        // o ForgeHooksClient.renderEquippedItem com o ajudante de bloco
        pose.translate(-0.5f, -0.5f, -0.5f);

        // o ItemThaumometerRenderer, EQUIPPED_FIRST_PERSON
        pose.translate(1.0f, 0.75f, -1.0f);
        pose.mulPose(Axis.YN.rotationDegrees(135.0f));
        if (player instanceof LocalPlayer local) {
            float f3 = local.xBobO + (local.xBob - local.xBobO) * partial;
            float f4 = local.yBobO + (local.yBob - local.yBobO) * partial;
            pose.mulPose(Axis.XP.rotationDegrees((player.getXRot() - f3) * 0.1f));
            pose.mulPose(Axis.YP.rotationDegrees((player.getYRot() - f4) * 0.1f));
        }
        pose.translate(-0.7f * var7, -(-0.65f * var7) + inverseArmHeight * 1.5f, 0.9f * var7);
        pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        pose.translate(0.0f, 0.0f, -0.9f * var7);
        pose.mulPose(Axis.YP.rotationDegrees(90.0f));

        if (!player.isInvisible()) {
            pose.pushPose();
            pose.scale(5.0f, 5.0f, 5.0f);
            AvatarRenderer<AbstractClientPlayer> renderer = mc.getEntityRenderDispatcher().getPlayerRenderer(player);
            Identifier skin = player.getSkin().body().texturePath();
            for (int var9 = 0; var9 < 2; var9++) {
                int var22 = var9 * 2 - 1;
                pose.pushPose();
                pose.translate(-0.0f, -0.6f, 1.1f * var22);
                pose.mulPose(Axis.XP.rotationDegrees(-45 * var22));
                pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                pose.mulPose(Axis.ZP.rotationDegrees(59.0f));
                pose.mulPose(Axis.YP.rotationDegrees(-65 * var22));
                renderer.renderRightHand(pose, collector, light, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
                pose.popPose();
            }
            pose.popPose();
        }
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(0.4f, -0.4f, 0.0f);
        pose.scale(2.0f, 2.0f, 2.0f);

        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRASS), (matrix, consumer) ->
                MeshDrawer.draw(ScannerMesh.QUADS, matrix, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));

        pose.pushPose();
        pose.translate(0.0f, 0.11f, 0.0f);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        int flicker = (int) (190.0f + Mth.sin(player.tickCount - player.getRandom().nextInt(2)) * 10.0f + 10.0f);
        pose.scale(2.5f, 2.5f, 2.5f);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(LENS), (matrix, consumer) -> {
            corner(matrix, consumer, -0.5f, 0.5f, 0.0f, 1.0f, flicker);
            corner(matrix, consumer, 0.5f, 0.5f, 1.0f, 1.0f, flicker);
            corner(matrix, consumer, 0.5f, -0.5f, 1.0f, 0.0f, flicker);
            corner(matrix, consumer, -0.5f, -0.5f, 0.0f, 0.0f, flicker);
            corner(matrix, consumer, -0.5f, -0.5f, 0.0f, 0.0f, flicker);
            corner(matrix, consumer, 0.5f, -0.5f, 1.0f, 0.0f, flicker);
            corner(matrix, consumer, 0.5f, 0.5f, 1.0f, 1.0f, flicker);
            corner(matrix, consumer, -0.5f, 0.5f, 0.0f, 1.0f, flicker);
        });
        screen(mc, player, pose, collector, flicker);
        pose.popPose();
        pose.popPose();
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float u, float v, int light) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }

    /** O que o vidro mostra: o {@code doScan} na mira, e dele o nome, os aspectos e o tipo do nodo. */
    private static void screen(Minecraft mc, AbstractClientPlayer player, PoseStack pose, SubmitNodeCollector collector, int flicker) {
        if (mc.level == null) return;
        ScanManager.Target scan = ScanManager.target(mc.level, player);
        if (scan == null) return;
        Font font = mc.font;
        boolean scanned = Knowledges.of(player).hasScanned(scan.key());
        pose.translate(0.0f, 0.0f, -0.01f);
        AspectList aspects = scanned ? scan.aspects() : null;
        if (scanned && mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
                && mc.level.getBlockEntity(hit.getBlockPos()) instanceof net.thaumcraft.block.entity.NodeBlockEntity node
                && scan.key().startsWith("NODE")) {
            aspects = node.aspects();
            Component t = node.type().title();
            if (node.modifier() != null) t = t.copy().append(", ").append(node.modifier().title());
            int sw = font.width(t);
            pose.pushPose();
            pose.scale(0.004f, 0.004f, 0.004f);
            collector.submitText(pose, -sw / 2.0f, -40, t.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                    0xF000F0, 0xFF000000 | 15642134, 0, 0);
            pose.popPose();
        }
        if (aspects != null) {
            int posX = 0, posY = 0;
            int aa = aspects.size();
            int baseX = Math.min(5, aa) * 8;
            for (Aspect aspect : aspects.getAspectsSorted()) {
                pose.pushPose();
                pose.scale(0.0075f, 0.0075f, 0.0075f);
                int j = (int) (190.0f + Mth.sin(posX + player.tickCount - player.getRandom().nextInt(2)) * 10.0f + 10.0f);
                tag(pose, collector, font, -baseX + posX * 16, -8 + posY * 16, aspect, aspects.getAmount(aspect), j);
                pose.popPose();
                if (++posX >= 5 - posY) {
                    posX = 0;
                    posY++;
                    aa -= 5 - posY;
                    baseX = Math.min(5 - posY, aa) * 8;
                }
            }
        }
        Component text = scan.name() == null ? Component.literal("?") : scan.name();
        String plain = text.getString();
        if (!plain.isEmpty()) {
            pose.pushPose();
            pose.translate(0.0f, -0.25f, 0.0f);
            int sw = font.width(text);
            float scale = 0.005f;
            if (sw > 90) scale -= 2.5e-5f * (sw - 90);
            pose.scale(scale, scale, scale);
            collector.submitText(pose, -sw / 2.0f, 0, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                    0xF000F0, 0xFFFFFFFF, 0, 0);
            pose.popPose();
        }
    }

    /** O {@code drawTag} com mistura aditiva: o símbolo na cor do aspecto e a quantidade em branco, na metade do tamanho. */
    private static void tag(PoseStack pose, SubmitNodeCollector collector, Font font, int x, int y, Aspect aspect, int amount, int light) {
        int colour = 0xFF000000 | aspect.color();
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(aspect.image()), (matrix, consumer) -> {
            consumer.addVertex(matrix, x, y + 16, 0.01f).setColor(colour).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, x + 16, y + 16, 0.01f).setColor(colour).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, x + 16, y, 0.01f).setColor(colour).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(matrix, 0.0f, 0.0f, 1.0f);
            consumer.addVertex(matrix, x, y, 0.01f).setColor(colour).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(matrix, 0.0f, 0.0f, 1.0f);
        });
        if (amount > 0) {
            String am = FORMAT.format(amount);
            int sw = font.width(am);
            pose.pushPose();
            pose.scale(0.5f, 0.5f, 0.5f);
            collector.submitText(pose, 32 - sw + x * 2, 32 - font.lineHeight + y * 2, Component.literal(am).getVisualOrderText(), false,
                    Font.DisplayMode.NORMAL, 0xF000F0, 0xFFFFFFFF, 0, 0);
            pose.popPose();
        }
    }

    /** Para quem pergunta se aquilo é o aparelho. */
    public static boolean is(ItemStack stack) {
        return stack.is(net.thaumcraft.registry.TCItems.THAUMOMETER);
    }
}
