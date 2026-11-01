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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O Conduto de Essência na mão e no inventário: o pedestal e o cristal parado.
 *
 * <p>A mesma malha do bloco, na mesma pose — o que não vem é o giro, porque um item na mão não tem
 * bloco-entidade de onde o tirar.
 */
public class EssenceConduitItemRenderer implements SpecialModelRenderer<net.minecraft.util.Unit> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/essence_conduit.png");

    @Override
    public void submit(@Nullable net.minecraft.util.Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int light, int overlay, boolean foil, int tint) {
        pose.pushPose();
        pose.translate(0.5f, 1.5f, 0.5f);
        pose.pushPose();
        pose.scale(0.0625f, -0.0625f, -0.0625f);
        coletor.submitCustomGeometry(pose, RenderTypes.entityCutout(PELE),
                (m, v) -> MeshDrawer.draw(EssenceConduitRenderer.pedestal(), m, v, light, overlay, 0xFFFFFFFF));
        pose.popPose();

        float[] cristal = ObjModel.part("arcana_crystal", null);
        if (cristal.length > 0) {
            pose.translate(0.0f, -1.0f, 0.0f);
            pose.scale(5.0f, 5.0f, -5.0f);
            coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PELE),
                    (m, v) -> ObjMesh.draw(cristal, m, v, 0xF000F0,
                            overlay, 0xFF00FFFF));
        }
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, -0.1f, -0.5f));
        extents.accept(new Vector3f(0.5f, 0.9f, 0.5f));
    }

    @Override
    public net.minecraft.util.Unit extractArgument(ItemStack pilha) {
        return net.minecraft.util.Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext contexto) {
            return new EssenceConduitItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
