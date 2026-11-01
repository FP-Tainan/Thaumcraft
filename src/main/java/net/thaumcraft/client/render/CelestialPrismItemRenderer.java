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
 * O Prisma Celeste na mão e no inventário.
 *
 * <p>O original tem um <b>se</b> para isto dentro do desenhista: nas coordenadas {@code 0,0,0}, que é
 * como o jogo de 2014 desenhava um item, ele desce o modelo três décimos e cresce <b>1,75</b> em vez de
 * dois. São essas duas contas.
 */
public class CelestialPrismItemRenderer implements SpecialModelRenderer<net.minecraft.util.Unit> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/celestial_prism.png");
    private static final float ESCALA = 1.75f;
    private static final float DESCE = -0.3f;

    @Override
    public void submit(@Nullable net.minecraft.util.Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int light, int overlay, boolean foil, int tint) {
        float[] malha = ObjModel.part("celestial_prism", null);
        if (malha.length == 0) return;
        pose.pushPose();
        pose.translate(0.5f, DESCE, 0.5f);
        pose.scale(ESCALA, ESCALA, ESCALA);
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PELE),
                (m, v) -> ObjMesh.draw(malha, m, v, 0xF000F0, overlay, 0xFFFFFFFF));
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5f, DESCE, -0.5f));
        extents.accept(new Vector3f(0.5f, DESCE + 1.5f, 0.5f));
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
            return new CelestialPrismItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
