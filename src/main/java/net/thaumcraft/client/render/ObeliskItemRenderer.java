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
 * O Obelisco na mão e no inventário, com a mesma malha do bloco.
 *
 * <p>O original faz isto com um <b>se</b> dentro do desenhista do bloco: quando as coordenadas são
 * {@code 0,0,0} — que é como o jogo de 2014 desenhava um item — ele encolhe o modelo a três quartos e
 * baixa-o meio bloco, para a agulha de dois blocos caber na caixa do item. São essas duas contas que
 * estão aqui.
 */
public class ObeliskItemRenderer implements SpecialModelRenderer<net.minecraft.util.Unit> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/obelisk.png");
    /** O {@code glScalef(0.75)} do original. */
    private static final float ESCALA = 0.75f;
    /** E o {@code glTranslatef(0, -0.5, 0)}. */
    private static final float DESCE = -0.5f;

    @Override
    public void submit(@Nullable net.minecraft.util.Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int light, int overlay, boolean foil, int tint) {
        float[] malha = ObjModel.part("obelisk_am", null);
        if (malha.length == 0) return;
        pose.pushPose();
        pose.scale(ESCALA, ESCALA, ESCALA);
        pose.translate(0.5f, DESCE, 0.5f);
        coletor.submitCustomGeometry(pose, RenderTypes.entityCutout(PELE),
                (m, v) -> ObjMesh.draw(malha, m, v, light, overlay, 0xFFFFFFFF));
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
            return new ObeliskItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
