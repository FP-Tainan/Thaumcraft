package net.thaumcraft.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GolemEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * O golem desenhado, com a pele da matéria de que ele é feito.
 *
 * <p>As peles são as do próprio mod: {@code golem_straw.png}, {@code golem_wood.png} e as outras seis.
 * O golem do jogo tem noventa e cinco centésimos de bloco de altura, e o corpo do original é desenhado em
 * medidas grandes — por isso ele entra encolhido, no mesmo fator que o original usa.
 */
public class GolemRenderer extends MobRenderer<GolemEntity, GolemRenderer.State, GolemModel> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("golem"), "main");
    /** O corpo do original é grande; entra encolhido para caber num golem de meio bloco de largura. */
    private static final float SCALE = 0.4f;

    private static final Map<String, Identifier> SKINS = new HashMap<>();
    private static final Identifier FALLBACK = Thaumcraft.id("textures/entity/golem_straw.png");

    /** O que o desenhista precisa saber do golem neste quadro. */
    public static class State extends LivingEntityRenderState {
        public String material = "straw";
    }

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(LAYER)), 0.3f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GolemEntity golem, State state, float partial) {
        super.extractRenderState(golem, state, partial);
        state.material = golem.material();
        state.scale = SCALE;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKINS.computeIfAbsent(state.material,
                name -> Thaumcraft.id("textures/entity/golem_" + name + ".png"));
    }
}
