package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.EldritchWardenEntity;

/**
 * O guardião-mor: o {@code RenderEldritchGuardian} da 4.2.3.5 no ramo do {@code EntityEldritchWarden} — o modelo do
 * guardião com a {@code eldritch_warden.png}, uma vez e meia maior, inteiro (sem o vulto), subindo do chão enquanto
 * nasce, e o olho do capuz aceso, pulsando, somando luz por cima.
 */
public class EldritchWardenRenderer extends MobRenderer<EldritchWardenEntity, EldritchGuardianRenderer.State, EldritchGuardianRenderer.Model> {
    public static final ModelLayerLocation EYE = new ModelLayerLocation(Thaumcraft.id("eldritch_warden_eye"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/eldritch_warden.png");

    public static class State extends EldritchGuardianRenderer.State {
        float rise;
        int eyeLight;
    }

    private final ModelPart eye;

    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianRenderer.Model(context.bakeLayer(EldritchGuardianRenderer.LAYER)), 0.6f);
        this.eye = context.bakeLayer(EYE).getChild("hoodeye");
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, EldritchGuardianRenderer.State state, float yRot, float xRot) {
                if (!(state instanceof State own)) return;
                ModelPart eye = EldritchWardenRenderer.this.eye;
                pose.pushPose();
                pose.scale(1.01f, 1.01f, 1.01f);
                eye.resetPose();
                eye.yRot = state.yRot * Mth.DEG_TO_RAD;
                eye.xRot = state.xRot * Mth.DEG_TO_RAD;
                collector.submitModelPart(eye, pose, AdditiveGlow.of(SKIN), own.eyeLight, OverlayTexture.NO_OVERLAY, null, -1, null);
                pose.popPose();
            }
        });
    }

    /** Só o olho do capuz, no mesmo lugar do modelo do guardião. */
    public static LayerDefinition createEyeLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("hoodeye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public EldritchGuardianRenderer.State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchWardenEntity warden, EldritchGuardianRenderer.State state, float partial) {
        super.extractRenderState(warden, state, partial);
        state.alpha = 1.0f;
        state.armLiftL = warden.armLiftL;
        state.armLiftR = warden.armLiftR;
        if (state instanceof State own) {
            own.rise = warden.getBbHeight() * (warden.getSpawnTimer() / 150.0f);
            own.eyeLight = (int) (195.0f + Mth.sin(warden.tickCount / 3.0f) * 15.0f + 15.0f);
        }
    }

    @Override
    public Identifier getTextureLocation(EldritchGuardianRenderer.State state) {
        return SKIN;
    }

    @Override
    protected RenderType getRenderType(EldritchGuardianRenderer.State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(SKIN);
    }

    @Override
    protected void scale(EldritchGuardianRenderer.State state, PoseStack pose) {
        pose.scale(1.5f, 1.5f, 1.5f);
    }

    /** Nascendo, sobe do chão ao longo de sete segundos e meio. */
    @Override
    public Vec3 getRenderOffset(EldritchGuardianRenderer.State state) {
        return state instanceof State own ? new Vec3(0.0, -own.rise, 0.0) : Vec3.ZERO;
    }
}
