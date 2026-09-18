package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.block.entity.NodeBlockEntity;

/**
 * O nó de aura como ele aparece na 4.2.3.5: uma nuvem de bolhas coloridas que gira sobre si mesma.
 *
 * <p>Cada aspecto que o nó guarda vira uma bolha da cor dele, e todas saem da mesma folha de desenhos —
 * {@code misc/nodes.png}, que tem trinta e dois quadros lado a lado e troca de quadro a cada quarenta
 * milésimos. O tamanho de cada bolha respira num compasso próprio e cresce com o quanto o nó tem daquele
 * aspecto; o feitio do nó manda no brilho, e o esmaecido pisca como quem está para se apagar.
 */
public class NodeRenderer implements BlockEntityRenderer<NodeBlockEntity, NodeRenderState> {
    private static final Identifier NODES = Thaumcraft.id("textures/misc/nodes.png");
    /** A folha tem trinta e dois quadros de largura e trinta e duas fileiras. */
    private static final int FRAMES = 32;
    /** A fileira de cima é a bolha dos aspectos. */
    private static final int STRIP = 0;

    public NodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public NodeRenderState createRenderState() {
        return new NodeRenderState();
    }

    @Override
    public void extractRenderState(NodeBlockEntity node, NodeRenderState state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(node, state, crumbling);
        state.wisps.clear();
        for (Aspect aspect : node.aspects().getAspects()) {
            int amount = node.aspects().getAmount(aspect);
            if (amount <= 0) continue;
            // no original o aspecto escuro é desenhado por cima em vez de somado à luz
            state.wisps.add(new NodeRenderState.Wisp(aspect.color(), amount, aspect.blend() != 1));
        }
        // sem thaumômetro na mão nem óculos no rosto, um nó é só ar — como no original
        if (!net.thaumcraft.item.Revealing.can(Minecraft.getInstance().player)) state.wisps.clear();
        state.type = node.type();
        state.modifier = node.modifier();
        state.seed = Math.abs(node.getBlockPos().hashCode()) % FRAMES;
        Minecraft minecraft = Minecraft.getInstance();
        state.ticks = minecraft.player == null ? partial : minecraft.player.tickCount + partial;
        this.extractDrains(node, state, partial);
    }

    /**
     * Quem está bebendo deste nó agora. É o {@code drainEntity} do {@code TileNodeRenderer} original.
     *
     * <p>A linha sai de um ponto logo à frente e um pouco abaixo dos olhos de quem segura a varinha —
     * (−0,1; −0,1; 0,5) girado pelo olhar —, e balança junto com o braço, no mesmo compasso do balanço
     * da varinha. Vale para qualquer jogador por perto: o uso e o olhar de todos chegam a este lado.
     */
    private void extractDrains(NodeBlockEntity node, NodeRenderState state, float partial) {
        state.drains.clear();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        net.minecraft.world.phys.Vec3 centre = net.minecraft.world.phys.Vec3.atCenterOf(node.getBlockPos());
        int colour = -1;
        for (net.minecraft.world.entity.player.Player player : minecraft.level.players()) {
            if (player.distanceToSqr(centre) > 64.0 * 64.0) continue;
            if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof net.thaumcraft.item.WandItem)) {
                continue;
            }
            NodeBlockEntity seen = net.thaumcraft.item.WandItem.nodeInSight(minecraft.level, player);
            if (seen == null || !seen.getBlockPos().equals(node.getBlockPos())) continue;

            float using = player.getTicksUsingItem() + partial;
            float sway = (float) Math.sin(using / 10.0f) * 10.0f;
            net.minecraft.world.phys.Vec3 tip = new net.minecraft.world.phys.Vec3(-0.1, -0.1, 0.5)
                    .xRot((float) Math.toRadians(-player.getViewXRot(partial)))
                    .yRot((float) Math.toRadians(-player.getViewYRot(partial)))
                    .yRot(-sway * 0.01f)
                    .xRot(-sway * 0.015f);
            if (colour < 0) colour = node.shownColour(minecraft.level.getGameTime());
            state.drains.add(new NodeRenderState.Drain(player.getEyePosition(partial).add(tip), centre,
                    Math.min(using, 10.0f) / 10.0f, colour));
        }
    }

    @Override
    public void submit(NodeRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // a linha da varinha bebendo daqui aparece mesmo sem óculos: no original ela fica fora da conta
        // de quem enxerga o nó
        for (NodeRenderState.Drain drain : state.drains) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            FloatyLine.submit(pose, collector, drain.from(), drain.to(), drain.colour(), drain.grow(), -0.02f, 0.15f);
            pose.popPose();
        }
        if (state.wisps.isEmpty()) return;

        float alpha = 1.0f;
        if (state.modifier == NodeModifier.BRIGHT) {
            alpha = 1.0f;
        } else if (state.modifier == NodeModifier.PALE) {
            alpha = 0.66f;
        } else if (state.modifier == NodeModifier.FADING) {
            // o esmaecido pisca como quem está para se apagar
            alpha = (float) Math.sin(state.ticks / 3.0f) * 0.25f + 0.33f;
        }
        // cada nó anda no seu próprio compasso, senão dois vizinhos piscam iguais
        long now = System.nanoTime();
        int frame = (int) ((now / 40000000L + state.seed) % FRAMES);
        long time = now / 5000000L;
        float share = alpha / Math.max(1.0f, state.wisps.size() / 2.0f);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(camera.orientation);

        int index = 0;
        for (NodeRenderState.Wisp wisp : state.wisps) {
            // o tamanho respira num compasso próprio de cada bolha, e cresce com o quanto o nó tem
            float breath = (float) Math.sin(state.ticks / (14.0f - index)) * 0.25f + 0.5f;
            float size = 0.2f + breath * (wisp.amount() / 50.0f);
            float angle = (float) (time % (5000L + 500L * index)) / (5000.0f + 500.0f * index) * 360.0f;
            float shade = wisp.dark() ? share * 1.5f : share;
            quad(pose, collector, frame, size, angle, wisp.color(), shade);
            index++;
        }
        // o miolo branco, que dá o brilho de dentro
        quad(pose, collector, frame, 0.30f, 0.0f, 0xFFFFFF, alpha * 0.5f);
        pose.popPose();
    }

    /** Uma bolha: um quadrado sempre virado para quem olha, com o quadro da vez da folha de desenhos. */
    private static void quad(PoseStack pose, SubmitNodeCollector collector, int frame, float size, float angle,
                             int color, float alpha) {
        float u0 = frame / (float) FRAMES;
        float u1 = (frame + 1) / (float) FRAMES;
        float v0 = STRIP / (float) FRAMES;
        float v1 = (STRIP + 1) / (float) FRAMES;
        int argb = (int) (Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | color & 0xFFFFFF;

        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(NODES), (matrix, consumer) -> {
            vertex(matrix, consumer, -size, -size, u0, v1, argb);
            vertex(matrix, consumer, size, -size, u1, v1, argb);
            vertex(matrix, consumer, size, size, u1, v0, argb);
            vertex(matrix, consumer, -size, size, u0, v0, argb);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y,
                               float u, float v, int argb) {
        consumer.addVertex(matrix, x, y, 0.0f)
                .setColor(argb)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
