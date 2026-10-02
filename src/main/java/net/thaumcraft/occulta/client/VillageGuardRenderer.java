package net.thaumcraft.occulta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.village.VillageGuardEntity;

/**
 * O desenhista do Guarda da Aldeia: o {@code RenderVillageGuard} e o {@code ModelVillageGuard} do Witchery.
 *
 * <p>O guarda é um <b>bípede com cara de aldeão</b>: o corpo e os braços são os de gente, para a armadura lhe
 * assentar, mas a cabeça é a alta do aldeão, com o nariz, e por cima do tronco vai uma <b>túnica</b> um pouco mais
 * larga que ele. É essa mistura que o faz ler como aldeão armado e não como pessoa nem como aldeão.
 *
 * <p><b>E as pernas dele andam pela metade.</b> O original corta a amplitude do passo ao meio e tira-lhes a volta
 * para os lados; o que se vê é um andar pesado, de quem está de guarda, e não o trote do aldeão.
 */
public final class VillageGuardRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("village_guard"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/entity/village_guard.png");

    /** O quanto o chapéu sobe, e o quanto o passo encurta: os números do original. */
    private static final float HAT_LIFT = 0.3f;
    private static final float STEP = 0.5f;

    private VillageGuardRenderer() {
    }

    /**
     * O corpo: o bípede do jogo com a cabeça trocada, o nariz pendurado nela e a túnica por cima do tronco.
     *
     * <p>A túnica entra <b>na raiz</b> e não no tronco, que é onde o original a desenha: lá ela é desenhada à
     * parte, depois de tudo, sem receber volta nenhuma.
     */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();

        // a cabeça do aldeão: dois a mais de altura que a de gente
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(28, 46)
                        .addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(52, 46)
                        .addBox(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offset(0.0f, -2.0f, 0.0f));

        root.addOrReplaceChild("robe",
                CubeListBuilder.create().texOffs(0, 38)
                        .addBox(-4.0f, 0.0f, -3.0f, 8.0f, 18.0f, 6.0f, new CubeDeformation(0.5f)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    /** O bípede do guarda, com o andar curto do original. */
    public static class GuardModel extends HumanoidModel<HumanoidRenderState> {
        public GuardModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            this.hat.y += HAT_LIFT;
            float passo = state.walkAnimationPos;
            float quanto = state.walkAnimationSpeed;
            this.rightLeg.xRot = Mth.cos(passo * 0.6662f) * 1.4f * quanto * STEP;
            this.leftLeg.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 1.4f * quanto * STEP;
            this.rightLeg.yRot = 0.0f;
            this.leftLeg.yRot = 0.0f;
        }
    }

    /** E o desenhista, com as camadas de armadura por cima, porque o guarda anda sempre vestido. */
    public static class Guard extends HumanoidMobRenderer<VillageGuardEntity, HumanoidRenderState, GuardModel> {
        public Guard(EntityRendererProvider.Context context) {
            super(context, new GuardModel(context.bakeLayer(LAYER)), 0.5f);
            this.addLayer(new HumanoidArmorLayer<>(this,
                    ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                    context.getEquipmentRenderer()));
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return SKIN;
        }
    }
}
