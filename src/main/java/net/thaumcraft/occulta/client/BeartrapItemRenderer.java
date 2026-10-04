package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * A armadilha na mão e no inventário: o ramo do {@code RenderBeartrap} em que a alma é {@code null}.
 *
 * <p>O original liga o mesmo desenhista ao bloco e ao item, e por isso a armadilha no inventário é a
 * armadilha de verdade e não um desenho dela. Sem alma, o modelo cai no lado de "ainda não disparou": ela
 * aparece <b>deitada e armada</b>, com os dentes para cima e a placa levantada.
 *
 * <p>E sem giro nenhum, porque o giro vinha do bloco. Ela fica olhando para o norte.
 */
public record BeartrapItemRenderer(ModelPart raiz) implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        BeartrapRenderer.desenha(pose, coletor, this.raiz, luz, -1, null, false);
    }

    /**
     * Onde ela cabe, para o jogo a enquadrar na casa do inventário.
     *
     * <p>Dar uma caixa à mão aqui põe a armadilha <b>encostada no fundo da casa</b> e cortada pela borda: ela
     * é rasa, e uma caixa de um bloco de alto centra o vazio por cima dela em vez de centrar a armadilha. O
     * jeito certo é o do próprio jogo — <b>perguntar ao modelo</b> onde ele está, com a mesma pose com que
     * ele vai ser desenhado.
     */
    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        PoseStack pose = new PoseStack();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        this.raiz.getExtentsForGui(pose, cantos);
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
            return new BeartrapItemRenderer(
                    contexto.entityModelSet().bakeLayer(BeartrapModel.ARMADILHA));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
