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

    /**
     * A lâmina, em pedaços que vão virando.
     *
     * <p><b>Desvio declarado, a pedido de quem manda.</b> No original a lâmina da foice de osso são três varetas
     * de um por um por quinze, e de longe elas somem: o que se vê é um cabo pelado com um toco na ponta. Ele
     * desenhou por cima do retrato o que queria — uma lâmina de verdade, larga na base, afinando e virando até a
     * ponta. É o que está aqui.
     *
     * <p>Cada pedaço é uma chapa fina, e cada um sai virado um tanto em relação ao de trás: enfileirados, eles
     * fazem a curva. A largura vai caindo da base para a ponta. O gume é uma fita mais clara que corre pela
     * beirada de fora, e as duas cores saem das manchas que o {@code scratchpad/Lamina.java} pinta na folha.
     */
    private static final int BLADE_PARTS = 8;
    private static final float BLADE_STEP = 2.9f;
    private static final float BLADE_TURN = 0.20f;
    private static final float BLADE_DROP = -0.70f;
    private static final float BLADE_WIDE = 5.6f;
    private static final float BLADE_TIP = 0.9f;
    private static final float BLADE_THICK = 0.8f;
    private static final float EDGE_THICK = 0.55f;
    private static final float EDGE_WIDE = 1.1f;

    private static final float[][] BLADE = blade(false);
    private static final float[][] EDGE = blade(true);

    /** As chapas da lâmina, ou as fitas do gume, uma por pedaço. */
    private static float[][] blade(boolean gume) {
        float[][] saída = new float[BLADE_PARTS][];
        for (int i = 0; i < BLADE_PARTS; i++) {
            float quanto = i / (float) (BLADE_PARTS - 1);
            float larga = BLADE_WIDE + (BLADE_TIP - BLADE_WIDE) * quanto;
            saída[i] = gume
                    // o gume corre pela beirada de fora da chapa, um fio à frente dela
                    ? BoxMesh.box(larga - EDGE_WIDE, -EDGE_THICK / 2, 0, EDGE_WIDE, EDGE_THICK, BLADE_STEP,
                            55, 17, 64, 32)
                    : BoxMesh.box(0, -BLADE_THICK / 2, 0, larga, BLADE_THICK, BLADE_STEP, 40, 17, 64, 32);
        }
        return saída;
    }

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

    /** O cabo e a junta do {@code ModelScytheBone}, e a lâmina nova saindo da junta. */
    private static void scythe(PoseStack pose, SubmitNodeCollector collector, Identifier folha,
                               int light, int overlay) {
        part(pose, collector, HANDLE_MIDDLE, folha, 0, 1.7f, 0, -0.2602503f, 0, 0, light, overlay);
        part(pose, collector, HANDLE_BOTTOM, folha, 0, 12.0f, -2.8f, 0, 0, 0, light, overlay);
        part(pose, collector, HANDLE_TOP, folha, 0, -8.0f, 0, 0, 0, 0, light, overlay);
        part(pose, collector, BONE_JOINT, folha, -0.5f, -8.1f, -1.0f, 0, 0, 0, light, overlay);
        bladeOut(pose, collector, folha, light, overlay);
    }

    /**
     * A lâmina: os pedaços encaixados um no outro, cada um virado em relação ao de trás.
     *
     * <p>Como cada pedaço é desenhado dentro do quadro do anterior, a curva vai se somando sozinha e não é
     * preciso contar seno nem cosseno de nada.
     */
    private static void bladeOut(PoseStack pose, SubmitNodeCollector collector, Identifier folha,
                                 int light, int overlay) {
        pose.pushPose();
        // da junta para fora
        pose.translate(0.0f, -8.4f / 16.0f, -1.2f / 16.0f);
        // a chapa é larga no X e fina no Y, e a curva vira em torno do Y: assim o plano da lâmina é o mesmo em
        // que a foice é balançada, e de fora se vê a chapa de chapa, e não de perfil.
        //
        // A meia-volta é o que põe a lâmina do lado certo do cabo: sem ela a foice saía com a lâmina caída para
        // trás de quem a segura, e quem manda marcou de vermelho que ela vai para a frente.
        pose.mulPose(Axis.YP.rotation((float) Math.PI / 2.0f - 0.30f));
        // e caída um tanto na direção do cabo, que é como a lâmina de uma foice de verdade fica
        pose.mulPose(Axis.XP.rotation(BLADE_DROP));
        for (int i = 0; i < BLADE_PARTS; i++) {
            flat(pose, collector, BLADE[i], folha, light, overlay);
            flat(pose, collector, EDGE[i], folha, light, overlay);
            pose.translate(0.0f, 0.0f, BLADE_STEP / 16.0f);
            pose.mulPose(Axis.YP.rotation(BLADE_TURN));
        }
        pose.popPose();
    }

    /** Uma chapa no quadro em que o desenho já está, sem mexer em posição nem giro. */
    private static void flat(PoseStack pose, SubmitNodeCollector collector, float[] caixa, Identifier folha,
                             int light, int overlay) {
        pose.pushPose();
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha),
                (m, v) -> MeshDrawer.draw(caixa, m, v, light, overlay, 0xFFFFFFFF));
        pose.popPose();
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
