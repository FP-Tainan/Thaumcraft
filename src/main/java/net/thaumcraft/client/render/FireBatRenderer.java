package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.FireBatEntity;

/**
 * O {@code RenderFireBat} da 4.2.3.5: o morcego a 35% do tamanho (60% o diabo e o vampiro), subindo e descendo no
 * voo, a pele {@code firebat.png} (ou {@code vampirebat.png}) e sempre aceso.
 */
public class FireBatRenderer extends MobRenderer<FireBatEntity, FireBatRenderer.State, FireBatModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("firebat"), "main");
    private static final Identifier FIREBAT = Thaumcraft.id("textures/models/firebat.png");
    private static final Identifier VAMPIRE = Thaumcraft.id("textures/models/vampirebat.png");

    public static class State extends LivingEntityRenderState {
        boolean hanging;
        boolean big;
        boolean vampire;
    }

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(LAYER)), 0.25f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FireBatEntity bat, State state, float partial) {
        super.extractRenderState(bat, state, partial);
        state.hanging = bat.isBatHanging();
        state.vampire = bat.isVampire();
        state.big = bat.isDevil() || bat.isVampire();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.vampire ? VAMPIRE : FIREBAT;
    }

    @Override
    protected int getBlockLightLevel(FireBatEntity bat, BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(State state, PoseStack pose) {
        float s = state.big ? 0.6f : 0.35f;
        pose.scale(s, s, s);
    }

    /** O {@code rotateCorpse}: o balanço do voo, ou o dedo de altura de quem está pendurado. */
    @Override
    protected void setupRotations(State state, PoseStack pose, float bodyRot, float scale) {
        pose.translate(0.0f, state.hanging ? -0.1f : Mth.cos(state.ageInTicks * 0.3f) * 0.1f, 0.0f);
        super.setupRotations(state, pose, bodyRot, scale);
    }
}
