package net.thaumcraft.naturalis.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.TaintBreederEntity;

/**
 * O Criador de Mácula no mundo: o {@code TaintBreederRenderer} do Magia Naturalis 0.5.0 — o modelo dele com a pele
 * {@code taint_breeder.png}.
 */
public class TaintBreederRenderer
        extends MobRenderer<TaintBreederEntity, TaintBreederRenderer.State, TaintBreederModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("taint_breeder"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/taint_breeder.png");

    public static class State extends LivingEntityRenderState {
        float health = 1.0f;
        float maxHealth = 1.0f;
    }

    public TaintBreederRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintBreederModel(context.bakeLayer(LAYER)), 0.8f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TaintBreederEntity breeder, State state, float partial) {
        super.extractRenderState(breeder, state, partial);
        state.health = breeder.getHealth();
        state.maxHealth = breeder.getMaxHealth();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKIN;
    }
}
