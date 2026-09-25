package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * <p>A foice de sangue é o {@code scythe.obj} — o modelo de Blender, mil trezentos e trinta e seis triângulos,
 * com o gume e o pano em folhas próprias. A de osso continua a ser as sete caixas do {@code ModelScytheBone}.
 *
 * <p><b>Desvio declarado:</b> no original o modelo de Blender só aparecia a quem estivesse numa lista de nomes
 * que o mod ia buscar à rede, e todos os outros viam sete caixas do {@code ModelScythe}. A lista morreu com o
 * sítio e o único nome que ficou no código era o de {@code AtomicStryker}. Aqui a foice de Blender é a de toda a
 * gente, a pedido de quem joga; as sete caixas do {@code ModelScythe} saíram, e ficam no histórico.
 *
 * <p><b>E o outro desvio, nos lugares em que a foice aparece:</b> o original tem uma conta de posição, giro e
 * tamanho para cada lugar ({@code ENTITY}, {@code EQUIPPED}, {@code EQUIPPED_FIRST_PERSON} e {@code INVENTORY}),
 * mas aqueles números contam a partir do quadro que o desenhista de itens da 1.7.10 montava, e esse quadro já
 * não existe. Cada foice tem agora o seu arquivo — {@code models/item/scythe.json} para a de Blender, que já vem
 * de pé, e {@code models/item/scythe_bone.json} para a de caixas, que precisa do giro de 180 em Z —, e a posição
 * e o tamanho de ambos foram acertados a olho.
 */
public record ScytheItemRenderer(boolean bone) implements SpecialModelRenderer<Unit> {
    private static final Identifier SCYTHE_BONE = Thaumcraft.id("textures/models/scythe_bone.png");

    // as caixas do ModelScytheBone numa folha de 64 por 32
    private static final float[] HANDLE_MIDDLE = BoxMesh.box(0, 0, 0, 1, 11, 1, 0, 0, 64, 32);
    private static final float[] HANDLE_BOTTOM = BoxMesh.box(0, 0, 0, 1, 12, 1, 0, 0, 64, 32);
    private static final float[] HANDLE_TOP = BoxMesh.box(0, 0, 0, 1, 10, 1, 0, 0, 64, 32);

    // e as da foice de osso, que troca o gume e a junta
    private static final float[] BONE_JOINT = BoxMesh.box(0, 0, 0, 2, 4, 4, 34, 0, 64, 32);
    private static final float[] BONE_BLADE = BoxMesh.box(-0.5f, -0.5f, 0, 1, 1, 15, 0, 15, 64, 32);
    private static final float[] BONE_BLADE_BASE = BoxMesh.box(0, 0, 0, 1, 1, 15, 0, 15, 64, 32);

    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // o modelo desenha-se a partir do meio da casa; o que o põe de pé e no tamanho é o arquivo do item
        pose.translate(0.5f, 0.5f, 0.5f);
        if (this.bone) boneScythe(pose, collector, light, overlay);
        else objScythe(pose, collector, light, overlay);
        pose.popPose();
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
