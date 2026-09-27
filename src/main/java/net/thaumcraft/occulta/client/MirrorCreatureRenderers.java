package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.mirror.MirrorFaceEntity;
import net.thaumcraft.occulta.mirror.ReflectionEntity;

/**
 * Os desenhistas do que mora nos espelhos: o {@code RenderMirrorFace} e o {@code RenderReflection} do Witchery.
 */
public final class MirrorCreatureRenderers {
    public static final ModelLayerLocation MIRROR_FACE = layer("mirror_face");

    private static final Identifier FACE_SKIN = Thaumcraft.id("textures/entity/mirror_face.png");
    private static final Identifier REFLECTION_SKIN = Thaumcraft.id("textures/entity/reflection.png");

    private MirrorCreatureRenderers() {
    }

    private static ModelLayerLocation layer(String nome) {
        return new ModelLayerLocation(Thaumcraft.id(nome), "main");
    }

    // ------------------------------------------------------------------ a cara do espelho

    /** O {@code ModelMirrorFace}: uma cabeça de bípede, e mais nada. */
    public static LayerDefinition mirrorFace() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8), PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** A cabeça vira-se para quem olha, e é o que o modelo do original faz de mais. */
    public static class MirrorFaceModel extends EntityModel<LivingEntityRenderState> {
        private final ModelPart head;

        public MirrorFaceModel(ModelPart root) {
            // meio transparente, que é o que o original faz com o glColor4f(1, 1, 1, 0.8)
            super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityTranslucent);
            this.head = root.getChild("head");
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            super.setupAnim(state);
            this.head.yRot = state.yRot * ((float) Math.PI / 180.0f);
            this.head.xRot = state.xRot * ((float) Math.PI / 180.0f);
        }
    }

    /**
     * O desenhista dela.
     *
     * <p>O original desenha-a a <b>três quartos</b> do tamanho, um pouco levantada e <b>meio transparente</b> —
     * que é o que lhe dá o ar de estar dentro do vidro, e não à frente dele.
     */
    public static class MirrorFace extends MobRenderer<MirrorFaceEntity, LivingEntityRenderState, MirrorFaceModel> {
        public MirrorFace(EntityRendererProvider.Context context) {
            super(context, new MirrorFaceModel(context.bakeLayer(MIRROR_FACE)), 0.0f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        /**
         * Os {@code glScalef(0.75)} e {@code glTranslatef(0, 0.4, 0)} do modelo do original.
         *
         * <p>Lá eles vinham <b>depois</b> do recuo de um bloco e meio que todo desenhista de bicho faz; aqui este
         * gancho vem <b>antes</b> dele, e por isso o recuo também encolheria. Os números são os mesmos, postos na
         * conta desta ordem: a cabeça acaba no mesmo lugar.
         */
        @Override
        protected void scale(LivingEntityRenderState state, PoseStack pose) {
            pose.translate(0.0f, -1.501f + 0.75f * (1.501f + 0.4f), 0.0f);
            pose.scale(0.75f, 0.75f, 0.75f);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return FACE_SKIN;
        }
    }

    // ------------------------------------------------------------------ o Reflexo

    /** O bípede do jogo, que é o que o original usa para o Reflexo. */
    public static class ReflectionModel extends HumanoidModel<HumanoidRenderState> {
        public ReflectionModel(ModelPart root) {
            super(root);
        }
    }

    /**
     * O desenhista do Reflexo.
     *
     * <p>Ele veste a armadura de quem copiou, como no original.
     *
     * <p><b>Desvio declarado:</b> no original a pele dele é <b>a pele de quem entrou</b>, baixada do servidor de
     * peles do jogo. Aqui é sempre a pele de reserva do próprio Witchery — a {@code reflection.png} —, porque
     * baixar a pele de alguém de um servidor de fora é coisa que este porte não faz. A armadura, a arma e os
     * efeitos continuam a ser os de quem entrou, que é o que se vê primeiro.
     */
    public static class Reflection
            extends HumanoidMobRenderer<ReflectionEntity, HumanoidRenderState, ReflectionModel> {
        public Reflection(EntityRendererProvider.Context context) {
            super(context, new ReflectionModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
            ArmorModelSet<ReflectionModel> armadura = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,
                    context.getModelSet(), ReflectionModel::new);
            this.addLayer(new HumanoidArmorLayer<>(this, armadura, context.getEquipmentRenderer()));
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return REFLECTION_SKIN;
        }
    }

    /** A camada de armadura do Reflexo, que o bípede do jogo já sabe desenhar. */
    public static LayerDefinition reflection() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 64);
    }
}
