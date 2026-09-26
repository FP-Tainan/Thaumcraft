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
 * <p>As duas são as sete caixas do {@code ModelScytheBone}. O que as separa é a folha: a de sangue leva a do
 * original, com o cabo de madeira, e a de osso leva a mesma folha passada a osso
 * ({@code scratchpad/Osso.java}), de cabo e tudo.
 *
 * <p><b>Desvios declarados, a pedido de quem joga:</b> as sete caixas do {@code ModelScythe} — a foice de sangue
 * de origem — saíram, e as duas passaram a usar o feitio da de osso; e a foice do modelo de Blender do original
 * ({@code scythe.obj}, que lá só aparecia a quem estivesse numa lista de nomes que o mod ia buscar à rede) saiu
 * também. Fica tudo no histórico.
 *
 * <p>E o outro desvio de sempre: o original tem uma conta de posição, giro e tamanho para cada lugar em que a
 * foice aparece, mas aqueles números contam a partir do quadro que o desenhista de itens da 1.7.10 montava, e
 * esse quadro já não existe. Os de {@code models/item/scythe.json} e {@code scythe_bone.json} foram acertados a
 * olho.
 */
public record ScytheItemRenderer(boolean bone) implements SpecialModelRenderer<Unit> {
    private static final Identifier SCYTHE = Thaumcraft.id("textures/models/scythe.png");
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
        Identifier folha = this.bone ? SCYTHE_BONE : SCYTHE;
        pose.pushPose();
        // o modelo desenha-se a partir do meio da casa; o que o põe de pé e no tamanho é o arquivo do item
        pose.translate(0.5f, 0.5f, 0.5f);
        scythe(pose, collector, folha, light, overlay);
        pose.popPose();
    }

    /** As sete caixas do {@code ModelScytheBone}, com o gume que o {@code render} vira antes de desenhar. */
    private static void scythe(PoseStack pose, SubmitNodeCollector collector, Identifier folha,
                               int light, int overlay) {
        part(pose, collector, HANDLE_MIDDLE, folha, 0, 1.7f, 0, -0.2602503f, 0, 0, light, overlay);
        part(pose, collector, HANDLE_BOTTOM, folha, 0, 12.0f, -2.8f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_TOP, folha, 0, -8.0f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_JOINT, folha, -0.5f, -8.1f, -1.0f, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE, folha, 0.5f, -7.0f, 1.0f, -0.1f, 0.06f, 0.7f, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, folha, 0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
        part(pose, collector, BONE_BLADE_BASE, folha, -0.2f, -8.0f, 1.0f, -0.1115358f, 0, 0, light, overlay);
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] caixa, Identifier folha,
                             float px, float py, float pz, float rotX, float rotY, float rotZ, int light, int overlay) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rotZ != 0.0f) pose.mulPose(Axis.ZP.rotation(rotZ));
        if (rotY != 0.0f) pose.mulPose(Axis.YP.rotation(rotY));
        if (rotX != 0.0f) pose.mulPose(Axis.XP.rotation(rotX));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha),
                (m, v) -> MeshDrawer.draw(caixa, m, v, light, overlay, 0xFFFFFFFF));
        pose.popPose();
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
