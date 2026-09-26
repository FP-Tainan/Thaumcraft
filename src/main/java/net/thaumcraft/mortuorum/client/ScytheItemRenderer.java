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
 * A foice na mão e no inventário: o {@code ItemScytheRenderer} e o {@code ItemScytheBoneRenderer} do Necromancy.
 *
 * <p>Cada foice tem o modelo dela, como no original: a de sangue é o {@code scythe.obj} — o modelo de Blender,
 * com o gume, o pano das juntas e o cabo em folhas próprias — e a de osso são as sete caixas do
 * {@code ModelScytheBone}.
 *
 * <p>Uma armadilha do original, que custou a achar: o {@code ModelScytheSpecial} <b>não segue o {@code .mtl}</b>.
 * O arquivo de materiais manda o cabo usar a {@code cloth.jpg}, mas o desenhista liga-o à {@code guntex.jpg}
 * antes de o desenhar. É a folha do desenhista que vale.
 *
 * <p><b>Desvios declarados, todos a pedido de quem joga:</b>
 * <ul>
 *   <li>No original o modelo de Blender só aparecia a quem estivesse numa lista de nomes que o mod ia buscar à
 *       rede; a lista morreu com o sítio e o único nome que ficou no código era o de {@code AtomicStryker}.
 *       Aqui é o de toda a gente, e as sete caixas do {@code ModelScythe} — a foice de sangue de origem —
 *       saíram.</li>
 *   <li>As folhas das duas passaram a tom de osso ({@code scratchpad/Osso.java}), e por isso a de sangue já não
 *       tem o gume escuro que tinha na origem.</li>
 *   <li>O original tem uma conta de posição, giro e tamanho para cada lugar em que a foice aparece, mas aqueles
 *       números contam a partir do quadro que o desenhista de itens da 1.7.10 montava, e esse quadro já não
 *       existe. Os de {@code models/item/scythe.json} e {@code scythe_bone.json} foram acertados a olho.</li>
 * </ul>
 */
public record ScytheItemRenderer(boolean bone) implements SpecialModelRenderer<Unit> {
    private static final Identifier SCYTHE_BONE = Thaumcraft.id("textures/models/scythe_bone.png");

    // as sete caixas do ModelScytheBone, numa folha de 64 por 32
    private static final float[] HANDLE_MIDDLE = BoxMesh.box(0, 0, 0, 1, 11, 1, 0, 0, 64, 32);
    private static final float[] HANDLE_BOTTOM = BoxMesh.box(0, 0, 0, 1, 12, 1, 0, 0, 64, 32);
    private static final float[] HANDLE_TOP = BoxMesh.box(0, 0, 0, 1, 10, 1, 0, 0, 64, 32);
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

    /** O {@code scythe.obj}: o gume, o pano das duas juntas e o cabo, cada um com a folha que o original lhe dá. */
    private static void objScythe(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        mesh(pose, collector, ScytheMesh.blade(), ScytheMesh.BLADE_TEXTURE, light, overlay);
        mesh(pose, collector, ScytheMesh.cloth(), ScytheMesh.CLOTH_TEXTURE, light, overlay);
        mesh(pose, collector, ScytheMesh.handle(), ScytheMesh.HANDLE_TEXTURE, light, overlay);
    }

    /** As sete caixas do {@code ModelScytheBone}, com o gume que o {@code render} vira antes de desenhar. */
    private static void boneScythe(PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        part(pose, collector, HANDLE_MIDDLE, 0, 1.7f, 0, -0.2602503f, 0, 0, light, overlay);
        part(pose, collector, HANDLE_BOTTOM, 0, 12.0f, -2.8f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_TOP, 0, -8.0f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_JOINT, -0.5f, -8.1f, -1.0f, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE, 0.5f, -7.0f, 1.0f, -0.1f, 0.06f, 0.7f, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, 0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, -0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] caixa,
                             float px, float py, float pz, float rotX, float rotY, float rotZ, int light, int overlay) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rotZ != 0.0f) pose.mulPose(Axis.ZP.rotation(rotZ));
        if (rotY != 0.0f) pose.mulPose(Axis.YP.rotation(rotY));
        if (rotX != 0.0f) pose.mulPose(Axis.XP.rotation(rotX));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(SCYTHE_BONE),
                (m, v) -> MeshDrawer.draw(caixa, m, v, light, overlay, 0xFFFFFFFF));
        pose.popPose();
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
