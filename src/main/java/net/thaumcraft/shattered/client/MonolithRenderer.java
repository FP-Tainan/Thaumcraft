package net.thaumcraft.shattered.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.shattered.MonolithEntity;

import java.util.List;

/**
 * O Monólito no mundo: o {@code RenderMonolith} das Portas Dimensionais.
 *
 * <p>Uma lousa de quarenta e oito por cento e oito, com dezenove peles — uma por cara, do olho fechado ao olho
 * aberto — e que se inclina para quem a olha.
 */
public class MonolithRenderer extends MobRenderer<MonolithEntity, MonolithRenderer.State, MonolithRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("monolith"), "main");

    /** As dezenove peles, do {@code monolith0} ao {@code monolith18}. */
    private static final List<Identifier> SKINS = java.util.stream.IntStream.rangeClosed(0, MonolithEntity.FACES)
            .mapToObj(i -> Thaumcraft.id("textures/entity/monolith/monolith" + i + ".png"))
            .toList();

    public static class State extends LivingEntityRenderState {
        int face;
        float pitchLevel;
    }

    /** O {@code ModelMonolith}: uma caixa só, numa folha de 256 por 256. */
    public static class Model extends EntityModel<State> {
        public Model(ModelPart root) {
            super(root, RenderTypes::entityTranslucent);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("monolith", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-24.0f, -83.07693f, -6.0f, 48, 108, 12), PartPose.ZERO);
            return LayerDefinition.create(mesh, 256, 256);
        }
    }

    public MonolithRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.0f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MonolithEntity monólito, State state, float partial) {
        super.extractRenderState(monólito, state, partial);
        state.face = monólito.face();
        state.pitchLevel = monólito.pitchLevel;
    }

    /** A lousa inclina-se para quem a olha, como no original. */
    @Override
    protected void setupRotations(State state, PoseStack pose, float yaw, float scale) {
        super.setupRotations(state, pose, yaw, scale);
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(state.pitchLevel));
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKINS.get(Math.max(0, Math.min(state.face, SKINS.size() - 1)));
    }

    @Override
    protected @org.jetbrains.annotations.Nullable RenderType getRenderType(State state, boolean body,
                                                                           boolean transparent, boolean glowing) {
        return RenderTypes.entityTranslucent(this.getTextureLocation(state));
    }
}
