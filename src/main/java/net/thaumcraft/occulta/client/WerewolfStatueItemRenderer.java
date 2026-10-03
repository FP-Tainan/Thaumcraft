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
 * A Estátua do Lobisomem na mão e no inventário: o ramo do {@code RenderStatueWerewolf} em que o mundo é
 * {@code null}.
 *
 * <p>Ela é alta demais para caber numa casa de inventário, e por isso o original a <b>encolhe a seis
 * décimos</b>, vira-a de frente e levanta-a um bloco e meio. São três números do original, e é por eles que a
 * estátua se vê inteira na mão de quem a leva.
 */
public record WerewolfStatueItemRenderer(ModelPart raiz) implements SpecialModelRenderer<Unit> {
    /** Os três números do original para a estátua na mão. */
    public static final float ENCOLHE = 0.6f;
    public static final float LEVANTA = 1.5f;
    public static final float DE_FRENTE = 180.0f;

    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        WerewolfStatueRenderer.desenha(pose, coletor, this.raiz, luz, 0.0f, true);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        cantos.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        cantos.accept(new Vector3f(1.0f, 2.0f, 1.0f));
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
            ModelPart raiz = contexto.entityModelSet().bakeLayer(WerewolfStatueModel.STATUE);
            WerewolfStatueModel.encolhe(raiz);
            return new WerewolfStatueItemRenderer(raiz);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
