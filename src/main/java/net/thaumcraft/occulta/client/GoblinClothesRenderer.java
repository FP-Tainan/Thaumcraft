package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

/**
 * A <b>roupa de goblin</b> no corpo de quem a veste: o {@code ModelGoblinClothes} e o {@code getArmorModel}
 * do {@code ItemGoblinClothes}.
 *
 * <p>É um bípede comum com <b>quatro caixas a mais</b> presas ao tronco — a <b>aljava</b> e as <b>três
 * flechas</b> dentro dela —, tombadas vinte graus para o lado. São elas que fazem a silhueta: de costas,
 * quem tem a Aljava do Mog vê-se de longe.
 *
 * <p>E são <b>três folhas</b>, uma por casa: a da cabeça, a do peito e a das pernas. O original tem uma
 * quarta de cada, para a tinta, e não a usa — estas peças não se pintam.
 *
 * <p><b>As duas folgas do original:</b> o peito com {@code 0,61} e as pernas com zero. O casaco sobra, as
 * calças colam.
 */
public final class GoblinClothesRenderer implements ArmorRenderer {
    public static final ModelLayerLocation PEITO =
            new ModelLayerLocation(Thaumcraft.id("goblin_clothes"), "chest");
    public static final ModelLayerLocation PERNAS =
            new ModelLayerLocation(Thaumcraft.id("goblin_clothes"), "legs");

    private static final Identifier FOLHA_CABEÇA =
            Thaumcraft.id("textures/models/goblin_clothes_head.png");
    private static final Identifier FOLHA_PEITO = Thaumcraft.id("textures/models/goblin_clothes.png");
    private static final Identifier FOLHA_PERNAS =
            Thaumcraft.id("textures/models/goblin_clothes_legs.png");

    /** As folgas do original. */
    public static final float FOLGA_DO_PEITO = 0.61f;
    public static final float FOLGA_DAS_PERNAS = 0.0f;

    /** E o tombo da aljava: os vinte graus do original. */
    private static final float TOMBO = -0.3490659f;

    private final Modelo doPeito;
    private final Modelo dasPernas;

    public GoblinClothesRenderer(EntityRendererProvider.Context contexto) {
        this.doPeito = new Modelo(contexto.bakeLayer(PEITO));
        this.dasPernas = new Modelo(contexto.bakeLayer(PERNAS));
    }

    public static LayerDefinition peito() {
        return constrói(FOLGA_DO_PEITO);
    }

    public static LayerDefinition pernas() {
        return constrói(FOLGA_DAS_PERNAS);
    }

    /** As caixas do {@code ModelGoblinClothes}, número por número, na folha de sessenta e quatro por trinta e dois. */
    private static LayerDefinition constrói(float folga) {
        MeshDefinition malha = HumanoidModel.createMesh(new CubeDeformation(folga), 0.0f);
        PartDefinition corpo = malha.getRoot().getChild("body");

        corpo.addOrReplaceChild("quiver", CubeListBuilder.create().texOffs(33, 0)
                        .addBox(-2.0f, -3.0f, 0.0f, 4.0f, 7.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 7.0f, 2.0f, 0.0f, 0.0f, TOMBO));
        corpo.addOrReplaceChild("arrow_right", CubeListBuilder.create().texOffs(44, 4)
                        .addBox(0.5f, -5.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 7.0f, 2.0f, 0.0f, 0.0f, TOMBO));
        corpo.addOrReplaceChild("arrow_left", CubeListBuilder.create().texOffs(44, 4)
                        .addBox(-1.5f, -5.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 7.0f, 2.0f, 0.0f, 0.0f, TOMBO));
        corpo.addOrReplaceChild("feathers", CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-2.0f, -7.0f, 0.0f, 4.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 7.0f, 2.0f, 0.0f, 0.0f, TOMBO));

        return LayerDefinition.create(malha, 64, 32);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector coletor, ItemStack peça,
                       HumanoidRenderState estado, EquipmentSlot casa, int luz,
                       HumanoidModel<HumanoidRenderState> contexto) {
        Modelo modelo = switch (casa) {
            case HEAD, CHEST -> this.doPeito;
            case LEGS, FEET -> this.dasPernas;
            default -> null;
        };
        if (modelo == null) return;

        modelo.acende(casa);
        Identifier folha = switch (casa) {
            case HEAD -> FOLHA_CABEÇA;
            case LEGS, FEET -> FOLHA_PERNAS;
            default -> FOLHA_PEITO;
        };
        coletor.submitModel(modelo, estado, pose, RenderTypes.armorCutoutNoCull(folha), luz,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    }

    /** O bípede da roupa, que acende só o que a casa pede. */
    static class Modelo extends HumanoidModel<HumanoidRenderState> {
        private EquipmentSlot casa = EquipmentSlot.CHEST;
        private final ModelPart[] aAljava;

        Modelo(ModelPart raiz) {
            super(raiz);
            ModelPart corpo = raiz.getChild("body");
            this.aAljava = new ModelPart[]{
                corpo.getChild("quiver"), corpo.getChild("arrow_right"),
                corpo.getChild("arrow_left"), corpo.getChild("feathers"),
            };
        }

        void acende(EquipmentSlot casa) {
            this.casa = casa;
        }

        @Override
        public void setupAnim(HumanoidRenderState estado) {
            super.setupAnim(estado);
            boolean cabeça = this.casa == EquipmentSlot.HEAD;
            boolean peito = this.casa == EquipmentSlot.CHEST;
            boolean pernas = this.casa == EquipmentSlot.LEGS || this.casa == EquipmentSlot.FEET;

            this.head.visible = cabeça;
            this.hat.visible = cabeça;
            this.body.visible = peito;
            this.rightArm.visible = peito;
            this.leftArm.visible = peito;
            this.rightLeg.visible = pernas;
            this.leftLeg.visible = pernas;

            // e a aljava só aparece no peito, que é onde ela se usa
            for (ModelPart peça : this.aAljava) peça.visible = peito;
        }
    }
}
