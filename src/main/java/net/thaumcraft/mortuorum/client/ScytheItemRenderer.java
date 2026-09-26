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
 * <p>As duas foices são o {@code scythe.obj} — o modelo de Blender, com o gume, o pano das juntas e o cabo em
 * folhas próprias. O que as separa é o cabo: o da foice de sangue é o pano escuro do original, e o da de osso é
 * o mesmo pano puxado para o claro.
 *
 * <p><b>Desvios declarados, todos a pedido de quem joga:</b>
 * <ul>
 *   <li>No original o modelo de Blender só aparecia a quem estivesse numa lista de nomes que o mod ia buscar à
 *       rede; a lista morreu com o sítio e o único nome que ficou no código era o de {@code AtomicStryker}.
 *       Aqui é o de toda a gente, e as sete caixas do {@code ModelScythe} e do {@code ModelScytheBone} saíram.</li>
 *   <li>As folhas passaram todas a tom de osso ({@code scratchpad/Osso.java}), e por isso a foice de sangue já
 *       não tem o gume escuro que tinha na origem.</li>
 *   <li>O original tem uma conta de posição, giro e tamanho para cada lugar em que a foice aparece, mas aqueles
 *       números contam a partir do quadro que o desenhista de itens da 1.7.10 montava, e esse quadro já não
 *       existe. Os de {@code models/item/scythe.json} e {@code scythe_bone.json} foram acertados a olho.</li>
 * </ul>
 */
public record ScytheItemRenderer(boolean bone) implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        // o modelo desenha-se a partir do meio da casa; o que o põe de pé e no tamanho é o arquivo do item
        pose.translate(0.5f, 0.5f, 0.5f);
        mesh(pose, collector, ScytheMesh.blade(), ScytheMesh.BLADE_TEXTURE, light, overlay);
        mesh(pose, collector, ScytheMesh.cloth(), ScytheMesh.CLOTH_TEXTURE, light, overlay);
        mesh(pose, collector, ScytheMesh.handle(),
                this.bone ? ScytheMesh.HANDLE_BONE_TEXTURE : ScytheMesh.HANDLE_TEXTURE, light, overlay);
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
