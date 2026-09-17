package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.model.AlembicModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O alambique na mao e no inventario, com a mesma malha do bloco.
 *
 * <p>Sem isto o item seria desenhado por um arquivo de modelo comum e sairia diferente do bloco posto no
 * chao -- que e como estava enquanto o corpo era feito de caixinhas. O original resolve igual: quando o
 * {@code TileAlembicRenderer} nao tem mundo por perto, ele desce o modelo um pouco e desenha as mesmas
 * pecas.
 *
 * <p>As pecas sao as de um alambique solto: pes, corpo e painel. Bico e encaixe so aparecem quando ha
 * algo embaixo para ligar.
 */
public class AlembicItemRenderer implements SpecialModelRenderer<net.minecraft.util.Unit> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/alembic.png");
    /** O quanto o modelo desce para caber centrado na caixa do item, como no original. */
    private static final float DROP = -0.4f;

    @Override
    public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f + DROP, 0.5f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (matrix, consumer) -> {
            ObjMesh.draw(AlembicModel.LEGS, matrix, consumer, light, overlay, 0xFFFFFFFF);
            ObjMesh.draw(AlembicModel.POT, matrix, consumer, light, overlay, 0xFFFFFFFF);
            ObjMesh.draw(AlembicModel.PANEL, matrix, consumer, light, overlay, 0xFFFFFFFF);
        });
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.45f, DROP, -0.5f));
        extents.accept(new Vector3f(0.45f, DROP + 0.95f, 0.5f));
    }

    @Override
    public net.minecraft.util.Unit extractArgument(ItemStack stack) {
        return net.minecraft.util.Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new AlembicItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
