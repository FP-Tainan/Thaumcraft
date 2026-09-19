package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.CultistClericEntity;
import net.thaumcraft.entity.eldritch.CultistLeaderEntity;

/**
 * Os cultistas: o {@code RenderCultist} da 4.2.3.5 — o corpo de gente com a pele {@code cultist.png} e as armaduras por
 * cima. O pretor é um quarto maior. O clérigo no ritual boia devagar e fica preso ao altar por um fio vermelho que corre.
 */
public class CultistRenderer<T extends Mob> extends HumanoidMobRenderer<T, CultistRenderer.State, HumanoidModel<CultistRenderer.State>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("cultist"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/cultist.png");

    public static class State extends HumanoidRenderState {
        boolean leader;
        boolean ritual;
        float bob;
        Vec3 from = Vec3.ZERO;
        Vec3 altar = Vec3.ZERO;
        float grow;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 64);
    }

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.leader = entity instanceof CultistLeaderEntity;
        state.ritual = entity instanceof CultistClericEntity cleric && cleric.isRitualist() && cleric.altar().isPresent();
        state.bob = 0.0f;
        if (state.ritual) {
            CultistClericEntity cleric = (CultistClericEntity) entity;
            int val = new java.util.Random(entity.getId()).nextInt(1000);
            float c = entity.tickCount + partial + val;
            state.bob = Mth.sin(c / 9.0f) * 0.1f + 0.21f;
            var home = cleric.altar().get();
            Vec3 pos = entity.getPosition(partial);
            state.from = new Vec3(pos.x, pos.y + entity.getEyeHeight() * 1.2f, pos.z);
            state.altar = new Vec3(home.getX() + 0.5, home.getY() + 1.5 - state.bob, home.getZ() + 0.5);
            state.grow = Math.min(entity.tickCount, 10) / 10.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKIN;
    }

    /** O pretor, um quarto maior. */
    @Override
    protected void scale(State state, PoseStack pose) {
        super.scale(state, pose);
        if (state.leader) pose.scale(1.25f, 1.25f, 1.25f);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        if (state.ritual) pose.translate(0.0f, state.bob, 0.0f);
        super.submit(state, pose, collector, camera);
        pose.popPose();
        if (state.ritual) {
            // o fio do ritual, desenhado a partir do altar, como no original
            pose.pushPose();
            Vec3 offset = state.altar.subtract(state.x, state.y, state.z);
            pose.translate(offset.x, offset.y, offset.z);
            FloatyLine.submit(pose, collector, state.from, state.altar, 0x110011, state.grow, -0.03f, 0.25f,
                    RenderTypes.entityTranslucent(FloatyLine.WISPY), 0.8f);
            pose.popPose();
        }
    }
}
