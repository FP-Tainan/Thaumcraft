package net.thaumcraft.occulta.client;

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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.thaumcraft.Thaumcraft;

/**
 * As <b>roupas da Morte</b> vestidas: o {@code ModelClothesDeath} do Witchery.
 *
 * <p>São dez caixas numa folha de <b>cento e vinte e oito por sessenta e quatro</b> — o dobro da largura
 * de uma armadura comum —, e três delas são o que faz a silhueta: o <b>capuz</b>, que é uma caixa de nove
 * por dez por nove à volta da cabeça; o <b>manto</b>, de nove por vinte e dois; e as <b>mangas</b>, de dez
 * de comprido em vez de doze, que deixam a mão de fora.
 *
 * <p>As outras sete são a roupa colada ao corpo, com as folgas miúdas do original: meio na cabeça, cinco
 * centésimos nos braços e nas pernas, nada no peito.
 *
 * <p>Cada casa acende o que é dela: o capuz e a cabeça no <b>elmo</b>; o peito, os braços, o manto e as
 * mangas no <b>peito</b>; e as pernas nas <b>calças ou botas</b>. É o que o {@code getArmorModel} do
 * original faz com os sinalizadores de visibilidade.
 */
public final class DeathClothesRenderer implements ArmorRenderer {
    public static final ModelLayerLocation CAMADA =
            new ModelLayerLocation(Thaumcraft.id("deaths_clothes"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/deaths_clothes.png");

    private final Modelo modelo;

    public DeathClothesRenderer(EntityRendererProvider.Context contexto) {
        this.modelo = new Modelo(contexto.bakeLayer(CAMADA));
    }

    /** A malha, com as folgas do original. */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition raiz = malha.getRoot();
        PartDefinition cabeça = raiz.getChild("head");
        PartDefinition corpo = raiz.getChild("body");
        PartDefinition braçoDireito = raiz.getChild("right_arm");
        PartDefinition braçoEsquerdo = raiz.getChild("left_arm");
        PartDefinition pernaDireita = raiz.getChild("right_leg");
        PartDefinition pernaEsquerda = raiz.getChild("left_leg");

        cabeça.addOrReplaceChild("cloth_head", CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.5f)),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("cowl", CubeListBuilder.create().texOffs(0, 45)
                        .addBox(-4.5f, -8.5f, -4.5f, 9.0f, 10.0f, 9.0f, new CubeDeformation(0.4f)),
                PartPose.ZERO);

        corpo.addOrReplaceChild("cloth_body", CubeListBuilder.create().texOffs(72, 16)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.ZERO);
        corpo.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(36, 37)
                        .addBox(-4.5f, 0.0f, -2.5f, 9.0f, 22.0f, 5.0f, new CubeDeformation(0.1f)),
                PartPose.ZERO);

        braçoDireito.addOrReplaceChild("cloth_right_arm", CubeListBuilder.create().texOffs(96, 16)
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.05f)),
                PartPose.ZERO);
        braçoDireito.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(64, 50)
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 10.0f, 4.0f, new CubeDeformation(0.1f)),
                PartPose.ZERO);
        braçoEsquerdo.addOrReplaceChild("cloth_left_arm", CubeListBuilder.create().texOffs(96, 16)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.05f)),
                PartPose.ZERO);
        braçoEsquerdo.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(64, 50)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 10.0f, 4.0f, new CubeDeformation(0.1f)),
                PartPose.ZERO);

        pernaDireita.addOrReplaceChild("cloth_right_leg", CubeListBuilder.create().texOffs(56, 16)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.05f)),
                PartPose.ZERO);
        pernaEsquerda.addOrReplaceChild("cloth_left_leg", CubeListBuilder.create().texOffs(56, 16)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.05f)),
                PartPose.ZERO);

        return LayerDefinition.create(malha, 128, 64);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector coletor, ItemStack peça,
                       HumanoidRenderState estado, EquipmentSlot casa, int luz,
                       HumanoidModel<HumanoidRenderState> contexto) {
        this.modelo.acende(casa);
        coletor.submitModel(this.modelo, estado, pose, RenderTypes.armorCutoutNoCull(FOLHA), luz,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    }

    /** O bípede da roupa, que acende só o que a casa pede. */
    static class Modelo extends HumanoidModel<HumanoidRenderState> {
        Modelo(ModelPart raiz) {
            super(raiz);
        }

        void acende(EquipmentSlot casa) {
            boolean cabeça = casa == EquipmentSlot.HEAD;
            boolean peito = casa == EquipmentSlot.CHEST;
            boolean pernas = casa == EquipmentSlot.LEGS || casa == EquipmentSlot.FEET;
            this.head.visible = cabeça;
            this.hat.visible = false;
            this.body.visible = peito;
            this.rightArm.visible = peito;
            this.leftArm.visible = peito;
            this.rightLeg.visible = pernas;
            this.leftLeg.visible = pernas;
        }
    }
}
