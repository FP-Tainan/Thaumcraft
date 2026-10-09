package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.fetish.FetishBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O <b>Espantalho</b> e o <b>Ídolo de Treant</b> na mão e no baú, com a mesma malha do bloco.
 *
 * <p>É o que o original faz: o desenhista da alma serve também ao item, de modo que um espantalho na mão é
 * o mesmo que está no chão — <b>e já com a tinta dele</b>. Dezesseis espantalhos de dezesseis cores num
 * baú se veem uns aos outros sem se lhes ler o nome.
 */
public class FetishItemRenderer implements SpecialModelRenderer<Integer> {
    /** O quanto o modelo desce para caber centrado na caixa do item. */
    public static final float DESCE = -0.1f;

    private final ModelPart malha;
    private final boolean ídolo;

    public FetishItemRenderer(ModelPart malha, boolean ídolo) {
        this.malha = malha;
        this.ídolo = ídolo;
    }

    @Override
    public void submit(@Nullable Integer tinta, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int overlay, boolean brilho, int cor) {
        pose.pushPose();
        pose.translate(0.0f, DESCE, 0.0f);
        FetishRenderer.desenha(pose, coletor, this.malha, this.ídolo, luz, Direction.NORTH,
                tinta == null ? FetishBlockEntity.DEFAULT_COLOR : tinta, false);
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        cantos.accept(new Vector3f(0.2f, DESCE, 0.2f));
        cantos.accept(new Vector3f(0.8f, DESCE + 0.95f, 0.8f));
    }

    @Override
    public @Nullable Integer extractArgument(ItemStack oquê) {
        var guardado = oquê.get(OccultaComponents.FETISH_DATA);
        return guardado == null ? FetishBlockEntity.DEFAULT_COLOR : guardado.color();
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked(boolean ídolo) implements SpecialModelRenderer.Unbaked<Integer> {
        public static final MapCodec<Unbaked> ESPANTALHO =
                RecordCodecBuilder.mapCodec(campo -> campo.point(new Unbaked(false)));
        public static final MapCodec<Unbaked> IDOLO =
                RecordCodecBuilder.mapCodec(campo -> campo.point(new Unbaked(true)));

        @Override
        public SpecialModelRenderer<Integer> bake(SpecialModelRenderer.BakingContext onde) {
            return new FetishItemRenderer(onde.entityModelSet().bakeLayer(
                    this.ídolo ? FetishRenderer.IDOLO : FetishRenderer.ESPANTALHO), this.ídolo);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return this.ídolo ? IDOLO : ESPANTALHO;
        }
    }
}
