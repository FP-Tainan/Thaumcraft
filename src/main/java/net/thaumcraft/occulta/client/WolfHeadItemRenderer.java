package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
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
 * A Cabeça de Lobo na mão e no inventário: o mesmo modelo do bloco, assentado e olhando para a frente.
 *
 * <p>No original quem a desenha na mão é o mesmo {@code RenderWolfHead}, chamado pelo caminho do crânio do
 * jogo — e é por isso que ela se vê em três dimensões na mão e não como uma figura chata.
 */
public record WolfHeadItemRenderer(ModelPart cabeça) implements SpecialModelRenderer<Unit> {
    /**
     * O <b>meio-giro</b> que a põe de cara para quem a tem.
     *
     * <p>Na 1.7.10 quem o dava era o caminho do crânio do jogo, por fora do desenhista; hoje não há esse
     * caminho, e sem o meio-giro a cabeça aparece de costas na mão de quem a leva.
     */
    public static final float DE_FRENTE = 180.0f;

    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        WolfHeadRenderer.desenha(pose, coletor, this.cabeça, luz, DE_FRENTE, null);
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
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Unit> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext contexto) {
            return new WolfHeadItemRenderer(contexto.entityModelSet()
                    .bakeLayer(WolfHeadRenderer.WOLF_HEAD).getChild("head"));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
