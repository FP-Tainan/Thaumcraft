package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Os dois marcadores na mão e no inventário: o ramo dos dois desenhistas em que o mundo é {@code null}.
 *
 * <p>São os mesmos três números das outras estátuas — encolher a seis décimos, virar de frente e levantar
 * um bloco e meio —, <b>por cima</b> da metade que a estatueta já encolheu no mundo.
 *
 * @param raiz  a malha assada
 * @param éLobo qual dos dois, que decide a folha e as peças
 */
public record AreaMarkerItemRenderer(ModelPart raiz, boolean éLobo) implements SpecialModelRenderer<Unit> {
    /** Os três números do original para a estátua na mão. */
    public static final float ENCOLHE = 0.6f;
    public static final float LEVANTA = 1.5f;
    public static final float DE_FRENTE = 180.0f;

    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        AreaMarkerRenderer.desenha(pose, coletor, this.raiz, luz, 0.0f, this.éLobo, true);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        cantos.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        cantos.accept(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public Unit extractArgument(ItemStack qual) {
        return Unit.INSTANCE;
    }

    /** O que o arquivo do item declara para pedir este desenhista. */
    public record Unbaked(boolean wolf) implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                com.mojang.serialization.Codec.BOOL.fieldOf("wolf").forGetter(Unbaked::wolf)
        ).apply(i, Unbaked::new));

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext contexto) {
            var qual = this.wolf ? AreaMarkerModels.WOLF : AreaMarkerModels.MANDRAKE;
            return new AreaMarkerItemRenderer(contexto.entityModelSet().bakeLayer(qual), this.wolf);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
