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
 * O Baú de Sanguessugas na mão e no inventário: o ramo do {@code RenderLeechChest} em que a alma é vazia.
 *
 * <p>O original liga o mesmo desenhista ao bloco e ao item, e por isso o baú no inventário é o baú de
 * verdade — a tampa de quatro quartos e tudo — e não um desenho dele. <b>Sem alma</b>, ele cai no lado de
 * "ninguém o abriu ainda": fechado, sem saco nenhum, e sem giro.
 *
 * <p>Até esta fatia, o item era uma <b>folha achatada</b>, o que era um remendo não declarado. Agora é o
 * modelo.
 */
public record LeechChestItemRenderer(ModelPart raiz) implements SpecialModelRenderer<Unit> {
    @Override
    public void submit(@Nullable Unit nada, PoseStack pose, SubmitNodeCollector coletor,
                       int luz, int porCima, boolean brilho, int cor) {
        LeechChestRenderer.desenha(pose, coletor, this.raiz, luz, null, 0.0f, 0);
    }

    /** Onde ele cabe, perguntado ao próprio modelo com a pose com que vai ser desenhado. */
    @Override
    public void getExtents(Consumer<Vector3fc> cantos) {
        PoseStack pose = new PoseStack();
        pose.translate(0.0f, 1.0f, 1.0f);
        pose.scale(1.0f, -1.0f, -1.0f);
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
            return new LeechChestItemRenderer(
                    contexto.entityModelSet().bakeLayer(LeechChestModel.BAÚ));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
