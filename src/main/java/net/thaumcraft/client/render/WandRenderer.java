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
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.WandItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A varinha como ela é na 4.2.3.5: uma haste de duas por dezoito com uma ponta de metal em cada extremo.
 *
 * <p>As medidas saíram do {@code ModelWand} do próprio mod, que é feito de três caixas — a ponta de cima, a
 * de baixo e a haste entre elas. As texturas também são as dele, uma por madeira e uma por metal, cada uma
 * numa folha de trinta e dois por trinta e dois.
 */
public class WandRenderer implements SpecialModelRenderer<WandRenderer.Parts> {
    /**
     * A haste: dois de grosso por dezoito de alto, desenhada a partir de (0,8) na folha.
     *
     * <p>No original ela é feita de pé com o pé na origem; aqui ela já vem centrada, que é o que a mão
     * espera de um item.
     */
    private static final float[] ROD = BoxMesh.box(-1.0f, -9.0f, -1.0f, 2.0f, 18.0f, 2.0f, 0.0f, 8.0f, 32.0f, 32.0f);
    /** A ponta: um cubinho de dois, desenhado a partir do canto da folha. */
    private static final float[] CAP = BoxMesh.box(-1.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 32.0f, 32.0f);
    /** Do tamanho do modelo para o tamanho de um item na mão. */
    private static final float SCALE = 1.0f / 16.0f;
    /** Onde cada ponta encaixa, uma em cada extremo da haste. */
    private static final float[] CAP_AT = {-8.0f, 8.0f};

    /** De que a varinha da vez é feita. */
    public record Parts(String rod, String cap, boolean staff) {
    }

    @Override
    public void submit(@Nullable Parts parts, PoseStack pose, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int tint) {
        if (parts == null) return;
        Identifier rodTexture = Thaumcraft.id("textures/models/wand_rod_" + parts.rod() + ".png");
        Identifier capTexture = Thaumcraft.id("textures/models/wand_cap_" + parts.cap() + ".png");

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        // o bastão é uma cabeça maior que a varinha
        float grow = parts.staff() ? 1.25f : 1.0f;
        pose.scale(SCALE * grow, SCALE * grow, SCALE * grow);

        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(rodTexture), (matrix, consumer) ->
                MeshDrawer.draw(ROD, matrix, consumer, light, overlay, 0xFFFFFFFF));

        // as duas pontas, uma em cada extremo da haste
        for (float at : CAP_AT) {
            pose.pushPose();
            pose.translate(0.0f, at, 0.0f);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(capTexture), (matrix, consumer) ->
                    MeshDrawer.draw(CAP, matrix, consumer, light, overlay, 0xFFFFFFFF));
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.2f, -0.6f, -0.2f));
        extents.accept(new Vector3f(0.2f, 0.6f, 0.2f));
    }

    @Override
    public Parts extractArgument(ItemStack stack) {
        boolean staff = stack.getItem() instanceof WandItem wand && wand.isStaff();
        return new Parts(WandItem.rodTag(stack), WandItem.capTag(stack), staff);
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Parts> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<Parts> bake(SpecialModelRenderer.BakingContext context) {
            return new WandRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
