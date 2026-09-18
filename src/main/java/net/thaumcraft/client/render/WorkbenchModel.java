package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O {@code ModelArcaneWorkbench} da 4.2.3.5: o tampo, a base e os quatro pés, com a textura de cada mesa — a
 * bancada arcana ({@code worktable.png}) e a mesa de desconstrução ({@code decontable.png}).
 *
 * <p>Os desenhistas do original põem o modelo de ponta-cabeça (cento e oitenta graus em X) com a origem no topo
 * do bloco; aqui é igual.
 */
public final class WorkbenchModel {
    private static final float UNIT = 1.0f / 16.0f;
    // as caixas, já com o ponto de giro de cada uma somado
    private static final float[] MESH = BoxMesh.join(
            BoxMesh.box(-8, 0, -8, 16, 8, 16, 0, 0, 128, 64),
            BoxMesh.box(-8, 12, -8, 16, 4, 16, 0, 32, 128, 64),
            BoxMesh.box(3, 8, -7, 4, 4, 4, 72, 0, 128, 64),
            BoxMesh.box(-7, 8, 3, 4, 4, 4, 72, 0, 128, 64),
            BoxMesh.box(3, 8, 3, 4, 4, 4, 72, 0, 128, 64),
            BoxMesh.box(-7, 8, -7, 4, 4, 4, 72, 0, 128, 64));

    private WorkbenchModel() {
    }

    /** A mesa no bloco: o {@code renderAll} depois do deslocamento e do giro do original. */
    public static void draw(PoseStack pose, SubmitNodeCollector collector, Identifier texture, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(texture),
                (matrix, consumer) -> MeshDrawer.draw(MESH, matrix, consumer, light, overlay, 0xFFFFFFFF));
        pose.popPose();
    }

    /** A mesa na mão e no inventário, com a mesma malha do bloco. */
    public record Item(Identifier texture) implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            draw(pose, collector, this.texture, light, overlay);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    /** O que o arquivo do item declara: qual textura de mesa usar. */
    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new Item(this.texture);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
