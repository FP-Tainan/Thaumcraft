package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.FollowerEntity;

/**
 * O desenhista de <b>Elle</b>: o {@code RenderFollower} do Witchery, no tipo zero.
 *
 * <p>O corpo é o de <b>gente</b> — era assim no original, que usa o {@code RenderBiped} tal e qual —, e é de
 * propósito: Elle não parece um monstro. Parece uma mulher parada no meio do campo, e é isso que a torna
 * estranha.
 *
 * <p>E <b>as quatro Mais Belas</b> têm pele própria, uma para cada tipo. São as mesmas quatro do original,
 * e a diferença entre elas é só a roupa: nenhuma parece coisa nenhuma. Parecem gente que passava por ali —
 * que é exatamente o que o espelho quer que se pense.
 */
public class FollowerRenderer extends MobRenderer<FollowerEntity,
        FollowerRenderer.Estado, HumanoidModel<FollowerRenderer.Estado>> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/follower.png");

    /** As quatro da Mais Bela, pela ordem dos tipos. */
    private static final Identifier[] BELAS = {
            Thaumcraft.id("textures/entity/fairest1.png"),
            Thaumcraft.id("textures/entity/fairest2.png"),
            Thaumcraft.id("textures/entity/fairest3.png"),
            Thaumcraft.id("textures/entity/fairest4.png")};

    /** O que o desenhista precisa de saber dela: qual delas é. */
    public static class Estado extends HumanoidRenderState {
        public int tipo = FollowerEntity.ELLE;
    }

    public FollowerRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new HumanoidModel<>(contexto.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Estado createRenderState() {
        return new Estado();
    }

    @Override
    public void extractRenderState(FollowerEntity ela, Estado estado, float parcial) {
        super.extractRenderState(ela, estado, parcial);
        estado.tipo = ela.tipo();
    }

    @Override
    public Identifier getTextureLocation(Estado estado) {
        if (estado.tipo < 1 || estado.tipo > BELAS.length) return PELE;
        return BELAS[estado.tipo - 1];
    }
}
