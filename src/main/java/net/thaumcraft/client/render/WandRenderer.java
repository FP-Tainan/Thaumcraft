package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A varinha e o bastão da 4.2.3.5: o {@code ModelWand} e o {@code ItemWandRenderer} descompilados.
 *
 * <p>O desenho repete as operações do {@code ModelWand.render} na mesma ordem, com o {@code PoseStack} no
 * lugar das chamadas de GL — em vez de recalcular posições, que foi como eu tinha errado da primeira vez
 * (as pontas saíam por dentro das pontas do cabo, quando no original elas ficam além dele, e mais
 * largas).
 *
 * <ul>
 * <li>o cabo: dois por dezoito, de 1 a 19; o de blaze acende e pulsa;</li>
 * <li>as pontas: um cubinho de dois em cada extremo, de −1 a 1 e de 19 a 21, com 1,2 da largura do cabo
 * (1,3 no bastão);</li>
 * <li>o bastão: o cabo com o dobro do comprimento e uma terceira ponta, achatada, logo abaixo da de
 * cima;</li>
 * <li>as runas: no bastão primordial, uma fileira de runas acesas em cada um dos quatro lados;</li>
 * <li>o foco: um cubo translúcido de seis na ponta, na cor do foco, pulsando de brilho.</li>
 * </ul>
 *
 * <p>E segurando o uso, a varinha inclina sessenta graus para a frente em três tiques e depois balança
 * (drenando um nó, ou com um foco que balança) ou treme (com um foco que carrega). Isso só na mão: no
 * inventário e no chão ela fica parada, como no original.
 */
public class WandRenderer implements SpecialModelRenderer<WandRenderer.Parts> {
    /** A folha do cubo do foco, a {@code models/wand.png} do original. */
    private static final Identifier FOCUS_TEXTURE = Thaumcraft.id("textures/models/wand.png");
    /** As runas do bastão primordial, uma tira de dezesseis. */
    private static final Identifier SCRIPT = Thaumcraft.id("textures/misc/script.png");

    /** As caixas do {@code ModelWand}, numa folha de sessenta e quatro por trinta e dois. */
    private static final float[] ROD = BoxMesh.box(-1.0f, 1.0f, -1.0f, 2.0f, 18.0f, 2.0f, 0.0f, 8.0f, 64.0f, 32.0f);
    private static final float[] CAP = BoxMesh.box(-1.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 64.0f, 32.0f);
    private static final float[] CAP_BOTTOM = BoxMesh.box(-1.0f, 19.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 64.0f, 32.0f);
    private static final float[] FOCUS = BoxMesh.box(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f, 0.0f, 0.0f, 64.0f, 32.0f);
    /** O {@code render(0.0625F)} de cada peça do modelo. */
    private static final float UNIT = 1.0f / 16.0f;

    /** Como a varinha está sendo segurada: na primeira pessoa, na terceira, ou parada. */
    public enum Pose {
        FIRST, FIRST_LEFT, THIRD, THIRD_LEFT, STILL;

        static final Codec<Pose> CODEC = Codec.STRING.xmap(
                name -> switch (name) {
                    case "first" -> FIRST;
                    case "first_left" -> FIRST_LEFT;
                    case "third" -> THIRD;
                    case "third_left" -> THIRD_LEFT;
                    default -> STILL;
                },
                pose -> pose.name().toLowerCase(java.util.Locale.ROOT));

        boolean firstPerson() {
            return this == FIRST || this == FIRST_LEFT;
        }

        boolean left() {
            return this == FIRST_LEFT || this == THIRD_LEFT;
        }
    }

    /** Os três jeitos de a varinha se mexer no uso, o {@code WandFocusAnimation} do original. */
    public enum Motion { NONE, WAVE, CHARGE }

    /**
     * De que a varinha da vez é feita e o que ela está fazendo.
     *
     * @param focusColour a cor do foco preso, ou −1 sem foco
     * @param using       há quantos tiques ela está em uso, ou −1 fora de uso
     * @param ticks       o relógio, para o que pulsa
     */
    public record Parts(String rod, String cap, boolean staff, boolean glowing, boolean runes,
                        int focusColour, float using, Motion motion, float ticks, boolean sceptre) {
    }

    private final Pose pose;

    public WandRenderer(Pose pose) {
        this.pose = pose;
    }

    @Override
    public void submit(@Nullable Parts parts, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        if (parts == null) return;
        boolean staff = parts.staff();

        pose.pushPose();
        // desfaz o meio bloco que o jogo desconta de todo item
        pose.translate(0.5, 0.5, 0.5);
        if (this.pose == Pose.STILL) {
            // o original desenha a varinha de ponta-cabeça: a ponta de cima fica na origem e o resto desce
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            // e aqui ela vem centrada, que é o que o inventário e o chão esperam de um item
            pose.translate(0.0f, staff ? -1.37f : -0.625f, 0.0f);
            this.submitModel(pose, collector, parts, light, overlay);
            pose.popPose();
            return;
        }

        // na mão, a conta é a do 1.7.10 inteira: o que o jogo novo já fez até aqui é trocado pelo que o
        // antigo fazia, e daí em diante vem o ItemWandRenderer tal e qual. A mão esquerda, que o original
        // não tinha, é o espelho da direita.
        boolean first = this.pose.firstPerson();
        if (this.pose.left()) pose.scale(-1.0f, 1.0f, 1.0f);
        pose.mulPose(first ? firstPersonCorrection(parts.using()) : THIRD_PERSON_CORRECTION);
        // o translate(-0.5) do render helper do Forge, o do bastão, e o translate(0.5, 1.5, 0.5) do renderer
        pose.translate(0.0f, staff ? 1.5f : 1.0f, 0.0f);
        if (first) pose.scale(1.0f, 1.1f, 1.0f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (parts.using() >= 0.0f) this.swing(pose, parts, first);
        if (this.pose.left()) pose.scale(-1.0f, 1.0f, 1.0f);

        this.submitModel(pose, collector, parts, light, overlay);
        pose.popPose();
    }

    /**
     * Da mão da terceira pessoa do jogo novo para a do 1.7.10.
     *
     * <p>O novo, depois do braço, faz {@code XP(-90) YP(180) translate(1/16, 2/16, -10/16)}. O antigo, no
     * {@code RenderPlayer} para item de pé ({@code isFull3D}), fazia {@code translate(-1/16, 7/16, 1/16)},
     * {@code translate(0, 3/16, 0)}, {@code scale(0.625, -0.625, 0.625)}, {@code XP(-100)} e {@code YP(45)}.
     */
    private static final org.joml.Matrix4f THIRD_PERSON_CORRECTION = new org.joml.Matrix4f()
            .rotateX((float) Math.toRadians(-90.0)).rotateY((float) Math.toRadians(180.0))
            .translate(1.0f / 16.0f, 2.0f / 16.0f, -10.0f / 16.0f)
            .invert()
            .translate(-1.0f / 16.0f, 7.0f / 16.0f, 1.0f / 16.0f)
            .translate(0.0f, 3.0f / 16.0f, 0.0f)
            .scale(0.625f, -0.625f, 0.625f)
            .rotateX((float) Math.toRadians(-100.0))
            .rotateY((float) Math.toRadians(45.0));

    /**
     * Da mão da primeira pessoa do jogo novo para a do 1.7.10.
     *
     * <p>Os dois começam no mesmo {@code translate(0.56, -0.52, -0.72)} do braço. Parada, o antigo girava
     * quarenta e cinco graus em Y e encolhia para 0,4 — o novo desfaz esse giro no fim do balanço, então
     * basta repô-lo. Em uso, os dois aplicam a pose do arco, cada um com os seus números: o novo é
     * desfeito e o do 1.7.10 ({@code ItemRenderer.renderItemInFirstPerson}, {@code EnumAction.bow}) entra
     * no lugar.
     */
    private static org.joml.Matrix4f firstPersonCorrection(float using) {
        org.joml.Matrix4f m = new org.joml.Matrix4f();
        if (using < 0.0f) {
            return m.rotateY((float) Math.toRadians(45.0)).scale(0.4f);
        }
        float held = Math.max(0.0f, using - 1.0f);
        float power = held / 20.0f;
        power = Math.min(1.0f, (power * power + power * 2.0f) / 3.0f);
        float shake = power > 0.1f ? (float) Math.sin((held - 0.1f) * 1.3f) * (power - 0.1f) : 0.0f;

        // o arco do jogo novo, para desfazer
        org.joml.Matrix4f modern = new org.joml.Matrix4f()
                .translate(-0.2785682f, 0.18344387f, 0.15731531f)
                .rotateX((float) Math.toRadians(-13.935))
                .rotateY((float) Math.toRadians(35.3))
                .rotateZ((float) Math.toRadians(-9.785))
                .translate(0.0f, shake * 0.004f, 0.0f)
                .translate(0.0f, 0.0f, power * 0.04f)
                .scale(1.0f, 1.0f, 1.0f + power * 0.2f)
                .rotateY((float) Math.toRadians(-45.0));
        m.set(modern).invert();
        // e o do 1.7.10
        m.rotateY((float) Math.toRadians(45.0)).scale(0.4f)
                .rotateZ((float) Math.toRadians(-18.0))
                .rotateY((float) Math.toRadians(-12.0))
                .rotateX((float) Math.toRadians(-8.0))
                .translate(-0.9f, 0.2f, 0.0f)
                .translate(0.0f, shake * 0.01f, 0.0f)
                .translate(0.0f, 0.0f, power * 0.1f)
                .rotateZ((float) Math.toRadians(-335.0))
                .rotateY((float) Math.toRadians(-50.0))
                .translate(0.0f, 0.5f, 0.0f)
                .scale(1.0f, 1.0f, 1.0f + power * 0.2f)
                .translate(0.0f, -0.5f, 0.0f)
                .rotateY((float) Math.toRadians(50.0))
                .rotateZ((float) Math.toRadians(335.0));
        return m;
    }

    /** O movimento do uso, o trecho com {@code getItemInUseDuration} do {@code ItemWandRenderer}. */
    private void swing(PoseStack pose, Parts parts, boolean first) {
        float t = Math.min(3.0f, parts.using());
        // o pivô do original fica a um bloco da ponta de cima, na altura da mão
        pose.translate(0.0f, 1.0f, 0.0f);
        if (first) {
            pose.mulPose(Axis.XP.rotationDegrees(10.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(10.0f));
        } else {
            pose.mulPose(Axis.ZP.rotationDegrees(33.0f));
        }
        // a inclinação para a frente, rumo ao que se está mirando: o glRotatef(60 * t/3, -1, 0, 0) do original
        pose.mulPose(Axis.XN.rotationDegrees(60.0f * (t / 3.0f)));
        if (parts.motion() == Motion.WAVE) {
            pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(parts.using() / 10.0f) * 10.0f));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.sin(parts.using() / 15.0f) * 10.0f));
        } else if (parts.motion() == Motion.CHARGE) {
            pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(parts.using() / 0.8f)));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.sin(parts.using() / 0.7f)));
        }
        pose.translate(0.0f, -1.0f, 0.0f);
    }

    /** O {@code ModelWand.render}, operação por operação. */
    private void submitModel(PoseStack pose, SubmitNodeCollector collector, Parts parts, int light, int overlay) {
        boolean staff = parts.staff();
        Identifier rodTexture = Thaumcraft.id("textures/models/wand_rod_" + parts.rod() + ".png");
        Identifier capTexture = Thaumcraft.id("textures/models/wand_cap_" + parts.cap() + ".png");

        pose.pushPose();
        if (staff) pose.translate(0.0, 0.2, 0.0);

        // o cabo; o de blaze acende e pulsa de leve
        pose.pushPose();
        int rodLight = parts.glowing() ? (int) (200.0f + Math.sin(parts.ticks()) * 5.0f + 5.0f) : light;
        if (staff) {
            pose.translate(0.0, -0.1, 0.0);
            pose.scale(1.2f, 2.0f, 1.2f);
        }
        box(pose, collector, rodTexture, ROD, rodLight, overlay);
        pose.popPose();

        // as pontas
        pose.pushPose();
        if (staff) pose.scale(1.3f, 1.1f, 1.3f);
        else pose.scale(1.2f, 1.0f, 1.2f);
        if (parts.sceptre()) {
            // o cetro: a ponta de cima maior, e outra achatada logo abaixo dela
            pose.pushPose();
            pose.scale(1.3f, 1.3f, 1.3f);
            box(pose, collector, capTexture, CAP, light, overlay);
            pose.popPose();
            pose.pushPose();
            pose.translate(0.0, 0.3, 0.0);
            pose.scale(1.0f, 0.66f, 1.0f);
            box(pose, collector, capTexture, CAP, light, overlay);
            pose.popPose();
        } else {
            box(pose, collector, capTexture, CAP, light, overlay);
        }
        if (staff) {
            // a terceira ponta do bastão, achatada, logo abaixo da de cima
            pose.translate(0.0, 0.225, 0.0);
            pose.pushPose();
            pose.scale(1.0f, 0.66f, 1.0f);
            box(pose, collector, capTexture, CAP, light, overlay);
            pose.popPose();
            pose.translate(0.0, 0.65, 0.0);
        }
        box(pose, collector, capTexture, CAP_BOTTOM, light, overlay);
        pose.popPose();

        // o foco: um cubo translúcido na cor dele, pulsando de brilho
        if (parts.focusColour() >= 0) {
            pose.pushPose();
            if (staff) {
                pose.translate(0.0f, -0.0475f, 0.0f);
                pose.scale(0.525f, 0.5525f, 0.525f);
            } else {
                pose.scale(0.5f, 0.5f, 0.5f);
            }
            int glow = (int) (195.0f + Math.sin(parts.ticks() / 3.0f) * 10.0f + 10.0f);
            int colour = 0xF2000000 | (parts.focusColour() & 0xFFFFFF);
            pose.scale(UNIT, UNIT, UNIT);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(FOCUS_TEXTURE), (matrix, consumer) ->
                    MeshDrawer.draw(FOCUS, matrix, consumer, glow, overlay, colour));
            pose.popPose();
        }

        // as dez runas que giram em volta da ponta do cetro
        if (parts.sceptre()) {
            for (int rot = 0; rot < 10; rot++) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(36 * rot + parts.ticks()));
                this.rune(pose, collector, 0.16, -0.01, -0.125, rot, parts.ticks());
                pose.popPose();
            }
        }

        // as runas do bastão primordial: quatro fileiras de catorze, uma em cada lado, acesas
        if (parts.runes()) {
            pose.pushPose();
            for (int side = 0; side < 4; side++) {
                pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                for (int a = 0; a < 14; a++) {
                    this.rune(pose, collector, 0.36 + a * 0.14, -0.01, -0.08, (a + side * 3) % 16, parts.ticks());
                }
            }
            pose.popPose();
        }
        pose.popPose();
    }

    /** Uma runa da tira {@code misc/script.png}, somando luz, com a cor e o tamanho respirando. */
    private void rune(PoseStack pose, SubmitNodeCollector collector, double x, double y, double z, int rune,
                      float ticks) {
        float r = (float) Math.sin((ticks + rune * 5) / 5.0f) * 0.1f + 0.88f;
        float g = (float) Math.sin((ticks + rune * 5) / 7.0f) * 0.1f + 0.63f;
        float alpha = (float) Math.sin((ticks + rune * 5) / 10.0f) * 0.2f;
        int colour = ((int) ((alpha + 0.6f) * 255.0f) << 24) | ((int) (r * 255.0f) << 16)
                | ((int) (g * 255.0f) << 8) | (int) (0.2f * 255.0f);
        float half = 0.06f + alpha / 40.0f;
        float u0 = 0.0625f * rune, u1 = u0 + 0.0625f;

        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(x, y, z);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(SCRIPT), (matrix, consumer) -> {
            vertex(matrix, consumer, -half, half, u1, 1.0f, colour);
            vertex(matrix, consumer, half, half, u1, 0.0f, colour);
            vertex(matrix, consumer, half, -half, u0, 0.0f, colour);
            vertex(matrix, consumer, -half, -half, u0, 1.0f, colour);
        });
        pose.popPose();
    }

    private static void box(PoseStack pose, SubmitNodeCollector collector, Identifier texture, float[] mesh,
                            int light, int overlay) {
        pose.pushPose();
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(texture), (matrix, consumer) ->
                MeshDrawer.draw(mesh, matrix, consumer, light, overlay, 0xFFFFFFFF));
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float u, float v,
                               int colour) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(colour).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.2f, -1.4f, -0.2f));
        extents.accept(new Vector3f(0.2f, 1.4f, 0.2f));
    }

    @Override
    public Parts extractArgument(ItemStack stack) {
        boolean staff = stack.getItem() instanceof WandItem wand && wand.isStaff();
        WandParts.Rod rod = WandParts.rod(WandItem.rodTag(stack));
        FocusItem focus = Focuses.on(stack);

        Minecraft minecraft = Minecraft.getInstance();
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float ticks = minecraft.level == null ? 0.0f : minecraft.level.getGameTime() + partial;

        // o uso só se sabe da própria mão: é a varinha que o jogador daqui está usando agora
        float using = -1.0f;
        Motion motion = Motion.NONE;
        Player player = minecraft.player;
        if (player != null && player.isUsingItem() && player.getUseItem() == stack) {
            using = player.getTicksUsingItem() + partial;
            if (WandItem.nodeInSight(player.level(), player) != null) motion = Motion.WAVE;
            else if (focus != null) motion = motion(focus);
        }
        return new Parts(WandItem.rodTag(stack), WandItem.capTag(stack), staff,
                rod != null && rod.glowing(), rod != null && rod.runes(),
                focus == null ? -1 : colour(focus), using, motion, ticks, WandItem.isSceptre(stack));
    }

    /** A cor de cada foco, o {@code getFocusColor} de cada um no original. */
    private static int colour(FocusItem focus) {
        return switch (focus.type()) {
            case "fire" -> 0xE55204;
            case "frost" -> 0x4F69CC;
            case "shock" -> 0x9FB3BF;
            case "excavation" -> 0x064006;
            case "portable_hole" -> 0x091429;
            case "trade" -> 0x857B93;
            case "primal" -> 0xA5A2C1;
            case "hellbat" -> 0xDC3502;
            case "pech" -> 0x229944;
            case "warding" -> 0xFFE9CF;
            default -> 0xFFFFFF;
        };
    }

    /** O {@code getAnimation} de cada foco: o de gelo balança, os outros três tremem. */
    private static Motion motion(FocusItem focus) {
        return focus.type().equals("frost") ? Motion.WAVE : Motion.CHARGE;
    }

    /** O que o arquivo do item declara para pedir este desenhista, com a pose em que ele está. */
    public record Unbaked(Pose pose) implements SpecialModelRenderer.Unbaked<Parts> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Pose.CODEC.optionalFieldOf("pose", Pose.STILL).forGetter(Unbaked::pose)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Parts> bake(SpecialModelRenderer.BakingContext context) {
            return new WandRenderer(this.pose);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
