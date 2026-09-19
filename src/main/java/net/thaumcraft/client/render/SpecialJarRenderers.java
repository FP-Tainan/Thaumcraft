package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.BrainJarBlockEntity;
import net.thaumcraft.item.JarredNode;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O que vai dentro dos jarros especiais: o {@code renderBrain} do {@code TileJarRenderer} (o {@code ModelBrain} na
 * salmoura, virando devagar para quem chega e subindo e descendo) e o jarro com o nó na mão (o
 * {@code ItemJarNodeRenderer}, o nó em três planos fixos).
 */
public final class SpecialJarRenderers {
    private static final Identifier BRAIN = Thaumcraft.id("textures/models/brain2.png");
    private static final Identifier BRINE = Thaumcraft.id("textures/models/jarbrine.png");
    // o ModelBrain (folha de 128 por 64): o cérebro, o tronco embaixo e o nervinho inclinado
    private static final float[] BRAIN_BOXES = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(-6, 8, -8, 12, 10, 16, 0, 0, 128, 64)),
            BoxMesh.mirror(BoxMesh.box(-4, 18, 0, 8, 3, 7, 64, 0, 128, 64)));
    private static final float[] BRAIN_NERVE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 2, 6, 2, 0, 32, 128, 64));
    // a salmoura do ModelJar (folha de 64 por 32)
    private static final float[] BRINE_BOX = BoxMesh.mirror(BoxMesh.box(-4, -11, -4, 8, 10, 8, 0, 0, 64, 32));

    private SpecialJarRenderers() {
    }

    /** O cérebro e a salmoura, com o giro dado (em radianos) e o sobe-e-desce. */
    static void drawBrain(PoseStack pose, SubmitNodeCollector collector, float rot, float bob, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.01f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        pose.pushPose();
        pose.translate(0.0f, -0.8f + bob, 0.0f);
        pose.mulPose(Axis.YP.rotation(rot));
        pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
        pose.scale(0.4f / 16.0f, 0.4f / 16.0f, 0.4f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRAIN),
                (m, v) -> MeshDrawer.draw(BRAIN_BOXES, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.translate(-1.0f, 18.0f, -2.0f);
        pose.mulPose(Axis.XP.rotation(0.4089647f));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BRAIN),
                (m, v) -> MeshDrawer.draw(BRAIN_NERVE, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(BRINE),
                (m, v) -> MeshDrawer.draw(BRINE_BOX, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    public static class BrainState extends BlockEntityRenderState {
        float rot, bob;
    }

    /** O jarro de cérebro no mundo. */
    public static class Brain implements BlockEntityRenderer<BrainJarBlockEntity, BrainState> {
        public Brain(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public BrainState createRenderState() {
            return new BrainState();
        }

        @Override
        public void extractRenderState(BrainJarBlockEntity jar, BrainState state, float partial, Vec3 camera,
                                       ModelFeatureRenderer.CrumblingOverlay crumbling) {
            BlockEntityRenderState.extractBase(jar, state, crumbling);
            float f2 = Mth.wrapDegrees((jar.rota - jar.rotb) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            state.rot = jar.rotb + f2 * partial;
            var player = Minecraft.getInstance().player;
            state.bob = Mth.sin((player == null ? 0 : player.tickCount) / 14.0f) * 0.03f + 0.03f;
        }

        @Override
        public void submit(BrainState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            drawBrain(pose, collector, state.rot, state.bob, state.lightCoords);
        }
    }

    /** O jarro de cérebro e o nó no jarro na mão. */
    public record ItemRenderer(boolean brain) implements SpecialModelRenderer<JarredNode> {
        @Override
        public void submit(@Nullable JarredNode node, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int tint) {
            if (this.brain) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                pose.translate(-1.0f, 0.0f, 0.0f);
                drawBrain(pose, collector, 0.0f, 0.03f, light);
                pose.popPose();
                return;
            }
            if (node == null) return;
            NodeRenderState state = new NodeRenderState();
            for (Aspect aspect : node.aspects().getAspects()) {
                int amount = node.aspects().getAmount(aspect);
                if (amount > 0) state.wisps.add(new NodeRenderState.Wisp(aspect.color(), amount, aspect.blend() != 1));
            }
            state.type = node.nodeType();
            state.modifier = node.nodeModifier();
            var player = Minecraft.getInstance().player;
            state.ticks = player == null ? 0 : player.tickCount;
            state.yOffset = -0.1f;
            NodeRenderer.drawWisps(state, pose, collector, new Quaternionf());
            NodeRenderer.drawWisps(state, pose, collector, new Quaternionf().rotateY((float) Math.PI / 2.0f));
            NodeRenderer.drawWisps(state, pose, collector, new Quaternionf().rotateY((float) Math.PI / 2.0f).rotateX((float) Math.PI / 2.0f));
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public @Nullable JarredNode extractArgument(ItemStack stack) {
            return stack.get(TCComponents.JARRED_NODE);
        }
    }

    public record Unbaked(boolean brain) implements SpecialModelRenderer.Unbaked<JarredNode> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("brain").forGetter(Unbaked::brain)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<JarredNode> bake(SpecialModelRenderer.BakingContext context) {
            return new ItemRenderer(this.brain);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
