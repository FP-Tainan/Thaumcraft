package net.thaumcraft.mortuorum.client;

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
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A foice na mão e no inventário: o {@code ItemScytheRenderer} do Necromancy.
 *
 * <p>São as sete caixas do {@code ModelScythe} (ou as sete do {@code ModelScytheBone}), com a folha de cada uma.
 * O original tem ainda uma foice de modelo de Blender, que ele só desenha para quem estiver numa lista de nomes
 * que ele ia buscar à rede — a lista morreu com o sítio, e o único nome que ficou no código é o de
 * {@code AtomicStryker}; é esse que aqui a vê, como lá.
 */
public record ScytheItemRenderer(boolean bone) implements SpecialModelRenderer<Unit> {
    private static final Identifier SCYTHE = Thaumcraft.id("textures/models/scythe.png");
    private static final Identifier SCYTHE_BONE = Thaumcraft.id("textures/models/scythe_bone.png");

    /** Quem vê a foice do modelo de Blender: o único nome que sobrou do {@code specialFolk} do original. */
    private static final String SPECIAL = "AtomicStryker";

    // as sete caixas do ModelScythe, todas espelhadas, numa folha de 64 por 32
    private static final float[] HANDLE_MIDDLE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 11, 1, 0, 0, 64, 32));
    private static final float[] HANDLE_BOTTOM = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 12, 1, 0, 0, 64, 32));
    private static final float[] HANDLE_TOP = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 10, 1, 0, 0, 64, 32));
    private static final float[] BLADE_EDGE = BoxMesh.mirror(BoxMesh.box(-0.5f, -0.5f, 0, 1, 1, 10, 4, 0, 64, 32));
    private static final float[] BLADE_BASE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 1, 11, 40, 0, 64, 32));
    private static final float[] JOINT = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 2, 2, 2, 0, 13, 64, 32));

    // e as da foice de osso, que troca o gume e a junta
    private static final float[] BONE_JOINT = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 2, 4, 4, 34, 0, 64, 32));
    private static final float[] BONE_BLADE = BoxMesh.mirror(BoxMesh.box(-0.5f, -0.5f, 0, 1, 1, 15, 0, 15, 64, 32));
    private static final float[] BONE_BLADE_BASE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 1, 1, 15, 0, 15, 64, 32));

    private static final float QUARTER = (float) (Math.PI / 4.0);

    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // o modelo do original é desenhado de cabeça para baixo, a partir do meio da casa
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.scale(0.9f, 0.9f, 0.9f);
        if (special()) {
            pose.scale(1.0f, -1.0f, -1.0f);
            objScythe(pose, collector, light, overlay);
        } else if (this.bone) {
            boneScythe(pose, collector, light, overlay);
        } else {
            plainScythe(pose, collector, light, overlay);
        }
        pose.popPose();
    }

    /** As sete caixas do {@code ModelScythe}. */
    private static void plainScythe(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        part(pose, collector, HANDLE_MIDDLE, SCYTHE, 0, 1.7f, 0, -0.2602503f, 0, 0, light, overlay);
        part(pose, collector, BLADE_EDGE, SCYTHE, 0.5f, -7.0f, 2.0f, 0, 0, QUARTER, light, overlay);
        part(pose, collector, BLADE_BASE, SCYTHE, 0.2f, -8.0f, 1.0f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_BOTTOM, SCYTHE, 0, 12.0f, -2.8f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_TOP, SCYTHE, 0, -8.0f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, JOINT, SCYTHE, -0.5f, -8.1f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, BLADE_BASE, SCYTHE, -0.2f, -8.0f, 1.0f, 0, 0, 0, light, overlay);
    }

    /** E as sete do {@code ModelScytheBone}, com o gume que o {@code render} vira antes de desenhar. */
    private static void boneScythe(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        part(pose, collector, HANDLE_MIDDLE, SCYTHE_BONE, 0, 1.7f, 0, -0.2602503f, 0, 0, light, overlay);
        part(pose, collector, HANDLE_BOTTOM, SCYTHE_BONE, 0, 12.0f, -2.8f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_TOP, SCYTHE_BONE, 0, -8.0f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_JOINT, SCYTHE_BONE, -0.5f, -8.1f, -1.0f, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE, SCYTHE_BONE, 0.5f, -7.0f, 1.0f, -0.1f, 0.06f, 0.7f, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, SCYTHE_BONE, 0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, SCYTHE_BONE, -0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh, Identifier folha,
                             float px, float py, float pz, float rotX, float rotY, float rotZ, int light, int overlay) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rotZ != 0.0f) pose.mulPose(Axis.ZP.rotation(rotZ));
        if (rotY != 0.0f) pose.mulPose(Axis.YP.rotation(rotY));
        if (rotX != 0.0f) pose.mulPose(Axis.XP.rotation(rotX));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, overlay, 0xFFFFFFFF));
        pose.popPose();
    }

    // ------------------------------------------------------------- a foice de segredo

    private static boolean special() {
        var jogador = Minecraft.getInstance().player;
        return jogador != null && SPECIAL.equals(jogador.getGameProfile().name());
    }

    /** O {@code ModelScytheSpecial}: o {@code scythe.obj}, em triângulos. */
    private static void objScythe(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        mesh(pose, collector, ScytheMesh.blade(), ScytheMesh.BLADE_TEXTURE, light, overlay);
        mesh(pose, collector, ScytheMesh.cloth(), ScytheMesh.CLOTH_TEXTURE, light, overlay);
    }

    private static void mesh(PoseStack pose, SubmitNodeCollector collector, float[] malha, Identifier folha,
                             int light, int overlay) {
        if (malha.length == 0) return;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha), (m, v) -> {
            for (int canto = 0; canto + ScytheMesh.STRIDE * 3 <= malha.length; canto += ScytheMesh.STRIDE * 3) {
                for (int i = 0; i < 4; i++) {
                    // o quarto canto é o terceiro outra vez: é o triângulo posto num quadrado
                    vertex(m, v, malha, canto + ScytheMesh.STRIDE * Math.min(i, 2), light, overlay);
                }
            }
        });
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer v, float[] malha, int at, int light, int overlay) {
        v.addVertex(m, malha[at], malha[at + 1], malha[at + 2])
                .setColor(-1)
                .setUv(malha[at + 3], malha[at + 4])
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(m, malha[at + 5], malha[at + 6], malha[at + 7]);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, -0.5f, -0.5f));
        extents.accept(new Vector3f(1.5f, 1.5f, 1.5f));
    }

    @Override
    public Unit extractArgument(ItemStack stack) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked(boolean bone) implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("bone", false).forGetter(Unbaked::bone)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new ScytheItemRenderer(this.bone);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
