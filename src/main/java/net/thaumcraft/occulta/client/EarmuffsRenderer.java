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
 * Os Abafadores na cabeça de quem os veste: o {@code ModelEarmuffs} e o {@code getArmorModel} do
 * {@code ItemEarmuffs} do Witchery.
 *
 * <p>Eles <b>não são armadura de folha</b>: o original não desenha uma peça de armadura por cima do corpo, desenha
 * cinco caixas presas à cabeça — as duas conchas nas orelhas e o arco de três pedaços por cima. A folha do
 * original ({@code textures/entities/earmuffs.png}) é de 64 por 64 e tem tinta só nesses dois cantos; posta como
 * folha de armadura do jogo de hoje, ela pintava o corpo inteiro de vermelho, que foi o que aconteceu antes de
 * isto existir.
 *
 * <p>Por isso vão pelo mesmo caminho da armadura de fortaleza: um modelo próprio, com o corpo todo escondido e só
 * a cabeça a levar as cinco caixas. As caixas são as do original, número por número.
 */
public class EarmuffsRenderer implements ArmorRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("earmuffs"), "main");
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/earmuffs.png");

    private final Model model;

    public EarmuffsRenderer(EntityRendererProvider.Context context) {
        this.model = new Model(context.bakeLayer(LAYER));
    }

    /** As cinco caixas do {@code ModelEarmuffs}, presas à cabeça, na folha de 64 por 64 do original. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        // o corpo fica sem caixa nenhuma: o original esconde tudo o que não é a cabeça
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        head.addOrReplaceChild("band_top",
                CubeListBuilder.create().texOffs(46, 38).addBox(-4.0F, -10.0F, -0.5F, 8.0F, 1.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(33, 32).addBox(-6.0F, -6.0F, -2.0F, 2.0F, 4.0F, 4.0F), PartPose.ZERO);
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(33, 32).addBox(4.0F, -6.0F, -2.0F, 2.0F, 4.0F, 4.0F), PartPose.ZERO);
        head.addOrReplaceChild("band_left",
                CubeListBuilder.create().texOffs(46, 32).addBox(4.0F, -10.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("band_right",
                CubeListBuilder.create().texOffs(46, 32).addBox(-5.0F, -10.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> context) {
        if (slot != EquipmentSlot.HEAD) return;
        collector.submitModel(this.model, state, pose, RenderTypes.armorCutoutNoCull(TEXTURE), light,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    }

    /** O modelo: a cabeça com as conchas e o arco, e o resto do corpo escondido. */
    static class Model extends HumanoidModel<HumanoidRenderState> {
        Model(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            this.hat.visible = false;
            this.body.visible = false;
            this.rightArm.visible = this.leftArm.visible = false;
            this.rightLeg.visible = this.leftLeg.visible = false;
        }
    }
}
