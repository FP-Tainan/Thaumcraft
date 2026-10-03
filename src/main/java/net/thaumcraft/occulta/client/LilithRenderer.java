package net.thaumcraft.occulta.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.LilithEntity;

/** O desenhista de Lilith: o {@code RenderLilith} do Witchery, com o modelo dela e a pele dela. */
public class LilithRenderer extends MobRenderer<LilithEntity, LilithRenderer.State, LilithModel> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/lilith.png");

    /** O estado dela leva o braço da pancada, que o modelo lê. */
    public static class State extends LivingEntityRenderState {
        public float braço;
    }

    public LilithRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new LilithModel(contexto.bakeLayer(LilithModel.LILITH)), 0.6f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LilithEntity lilith, State estado, float parcial) {
        super.extractRenderState(lilith, estado, parcial);
        estado.braço = lilith.pancadaNoBraço() - parcial;
    }

    @Override
    public Identifier getTextureLocation(State estado) {
        return PELE;
    }
}
