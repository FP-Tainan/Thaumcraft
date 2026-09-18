package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.registry.TCComponents;

/**
 * O arreio taumostático no corpo: o {@code ModelHoverHarness} da 4.2.3.5.
 *
 * <p>Uma caixa sobre o tronco (a {@code hoverharness.png}, meio pixel e um décimo mais larga que o corpo), o aparelho
 * das costas ({@code hoverharness.obj} com a {@code hoverharness2.png}, sem sombreamento) e, pairando, dois anéis de
 * raios girando atrás das costas — o grande branco, o pequeno rosado virado para o outro lado. As faíscas que saltam
 * do aparelho para os blocos em volta ficam no {@code HoverClient}.
 */
public class HoverHarnessRenderer implements ArmorRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("hover_harness"), "main");
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/hoverharness.png");
    private static final Identifier BACK = Thaumcraft.id("textures/models/hoverharness2.png");
    private static final Identifier RING = Thaumcraft.id("textures/misc/lightningring.png");
    /** O {@code rotateAngleX} de quem se agacha no 1.7.10, em graus. */
    private static final float SNEAK = 28.64789f;

    private final Model model;

    public HoverHarnessRenderer(EntityRendererProvider.Context context) {
        this.model = new Model(context.bakeLayer(LAYER));
    }

    /** O tronco do {@code ModelBiped} com a caixa do arreio; o resto do corpo vazio. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.6f)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> context) {
        if (slot != EquipmentSlot.CHEST) return;
        collector.submitModel(this.model, state, pose, RenderTypes.armorCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        if (stack.hasFoil()) {
            collector.submitModel(this.model, state, pose, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        }
        // o aparelho das costas segue o tronco, que no 26.2 desce um pouco ao agachar
        float down = state.isCrouching ? 3.2f / 16.0f : 0.0f;
        pose.pushPose();
        pose.translate(0.0f, down, 0.0f);
        pose.scale(0.1f, 0.1f, 0.1f);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        if (state.isCrouching) pose.mulPose(Axis.XP.rotationDegrees(SNEAK));
        pose.translate(0.0f, 0.33f, -3.7f);
        float[] mesh = ObjModel.part("hoverharness", null);
        collector.submitCustomGeometry(pose, UnlitCutout.of(BACK), (m, c) -> ObjMesh.draw(mesh, m, c, light, OverlayTexture.NO_OVERLAY, -1));
        pose.popPose();

        if (!Boolean.TRUE.equals(stack.get(TCComponents.HOVER))) return;
        // os anéis de raio, somando luz: o quadro do ícone animado de 16 quadros sai do relógio
        int frame = (int) (Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime() % 16);
        pose.pushPose();
        pose.translate(0.0f, down, 0.0f);
        if (state.isCrouching) {
            pose.mulPose(Axis.XP.rotationDegrees(SNEAK));
            pose.translate(0.0f, 0.075f, -0.05f);
        }
        pose.translate(0.0f, 0.2f, 0.55f);
        ring(pose, collector, frame, 2.5f, 0xFFFFFFFF);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.translate(0.0f, 0.0f, 0.03f);
        ring(pose, collector, frame, 1.5f, 0xFFFF80FF);
        pose.popPose();
        pose.popPose();
    }

    /** O {@code renderQuadCenteredFromIcon}: um quadrado de lado {@code scale} centrado, com o brilho 230. */
    private static void ring(PoseStack pose, SubmitNodeCollector collector, int frame, float scale, int colour) {
        float v0 = frame / 16.0f, v1 = (frame + 1) / 16.0f;
        pose.pushPose();
        pose.scale(scale, scale, scale);
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(RING), (m, c) -> {
            vertex(m, c, -0.5f, 0.5f, 0.0f, v1, colour);
            vertex(m, c, 0.5f, 0.5f, 1.0f, v1, colour);
            vertex(m, c, 0.5f, -0.5f, 1.0f, v0, colour);
            vertex(m, c, -0.5f, -0.5f, 0.0f, v0, colour);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(230).setNormal(m, 0.0f, 0.0f, 1.0f);
    }

    /** O modelo só com o tronco à mostra. */
    static class Model extends HumanoidModel<HumanoidRenderState> {
        Model(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            this.head.visible = this.hat.visible = false;
            this.rightArm.visible = this.leftArm.visible = false;
            this.rightLeg.visible = this.leftLeg.visible = false;
            this.body.visible = true;
        }
    }
}
