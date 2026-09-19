package net.thaumcraft.client.render.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * As peças do guardião eldritch e do guardião-mor, tiradas do {@code ModelEldritchGuardian} da 4.2.3.5.
 *
 * <p><strong>Este arquivo é gerado</strong> pelo {@code scratchpad/modelo-entidade.js}, que lê o modelo descompilado do
 * jar: cada caixa com o ponto de giro, o giro e o espelho do original, e os filhos dentro dos pais. Não mexer na mão.
 */
public final class EldritchGuardianModel {
    private EldritchGuardianModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("beltr", CubeListBuilder.create().texOffs(76, 44).mirror(false).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).mirror(false).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mbeltl", CubeListBuilder.create().texOffs(76, 44).mirror(false).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mbeltr", CubeListBuilder.create().texOffs(76, 44).mirror(false).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("beltl", CubeListBuilder.create().texOffs(76, 44).mirror(false).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).mirror(false).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("hoodeye", CubeListBuilder.create().texOffs(0, 0).mirror(false).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_hood1 = root.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(40, 12).mirror(false).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_hood1.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(36, 28).mirror(false).addBox(-3.5F, -8.7F, 2.0F, 7.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F));
        p_hood1.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(22, 19).mirror(false).addBox(-3.0F, -9.0F, 2.5F, 6.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F));
        p_hood1.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(40, 4).mirror(false).addBox(-2.5F, -9.7F, 3.5F, 5.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F));
        root.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).mirror(false).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shoulderplatetopr", CubeListBuilder.create().texOffs(110, 37).mirror(false).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
        root.addOrReplaceChild("shoulderplater1", CubeListBuilder.create().texOffs(110, 45).mirror(false).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
        root.addOrReplaceChild("shoulderplater2", CubeListBuilder.create().texOffs(94, 45).mirror(false).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
        root.addOrReplaceChild("shoulderplater3", CubeListBuilder.create().texOffs(94, 45).mirror(false).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
        root.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(56, 35).mirror(false).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.122173F, 0.0349066F));
        PartDefinition p_arml1 = root.addOrReplaceChild("arml1", CubeListBuilder.create().texOffs(72, 8).mirror(false).addBox(-1.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.9599311F, -0.1047198F, -0.1919862F));
        p_arml1.addOrReplaceChild("arml2", CubeListBuilder.create().texOffs(76, 28).mirror(false).addBox(-1.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_arml1.addOrReplaceChild("arml3", CubeListBuilder.create().texOffs(76, 23).mirror(false).addBox(-1.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_armr1 = root.addOrReplaceChild("armr1", CubeListBuilder.create().texOffs(72, 8).mirror(false).addBox(-3.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.9599311F, 0.1047198F, 0.1919862F));
        p_armr1.addOrReplaceChild("armr2", CubeListBuilder.create().texOffs(76, 28).mirror(false).addBox(-3.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_armr1.addOrReplaceChild("armr3", CubeListBuilder.create().texOffs(76, 23).mirror(false).addBox(-3.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.122173F, -0.0349066F));
        root.addOrReplaceChild("shoulderplateltop", CubeListBuilder.create().texOffs(110, 37).mirror(false).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
        root.addOrReplaceChild("shoulderplatel1", CubeListBuilder.create().texOffs(110, 45).mirror(false).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F));
        root.addOrReplaceChild("shoulderplatel2", CubeListBuilder.create().texOffs(94, 45).mirror(false).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
        root.addOrReplaceChild("shoulderplatel3", CubeListBuilder.create().texOffs(94, 45).mirror(false).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F));
        root.addOrReplaceChild("legpanelr4", CubeListBuilder.create().texOffs(0, 43).mirror(false).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanelr5", CubeListBuilder.create().texOffs(0, 47).mirror(false).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanelr6", CubeListBuilder.create().texOffs(6, 43).mirror(false).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanelr1", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanelr2", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanelr3", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanell3", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanell4", CubeListBuilder.create().texOffs(0, 43).mirror(false).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanell5", CubeListBuilder.create().texOffs(0, 47).mirror(false).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanell6", CubeListBuilder.create().texOffs(6, 43).mirror(false).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanell1", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanell2", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        root.addOrReplaceChild("sidepanell1", CubeListBuilder.create().texOffs(0, 22).mirror(false).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        root.addOrReplaceChild("sidepanelr1", CubeListBuilder.create().texOffs(0, 22).mirror(false).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        PartDefinition p_sidepanelr2 = root.addOrReplaceChild("sidepanelr2", CubeListBuilder.create().texOffs(0, 54).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(-4.5F, 9.5F, -2.0F, 0.0F, 0.0F, 0.122173F));
        PartDefinition p_sidepanelr3 = p_sidepanelr2.addOrReplaceChild("sidepanelr3", CubeListBuilder.create().texOffs(0, 35).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.296706F));
        p_sidepanelr3.addOrReplaceChild("sidepanelr4", CubeListBuilder.create().texOffs(24, 35).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
        PartDefinition p_sidepanell2 = root.addOrReplaceChild("sidepanell2", CubeListBuilder.create().texOffs(0, 54).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(4.5F, 9.5F, -2.0F, 0.0F, 0.0F, -0.122173F));
        PartDefinition p_sidepanell3 = p_sidepanell2.addOrReplaceChild("sidepanell3", CubeListBuilder.create().texOffs(0, 35).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.296706F));
        p_sidepanell3.addOrReplaceChild("sidepanell4", CubeListBuilder.create().texOffs(24, 35).mirror(false).addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
        PartDefinition p_legpanelc1 = root.addOrReplaceChild("legpanelc1", CubeListBuilder.create().texOffs(16, 45).mirror(false).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, -3.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_legpanelc2 = p_legpanelc1.addOrReplaceChild("legpanelc2", CubeListBuilder.create().texOffs(16, 54).mirror(false).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_legpanelc2.addOrReplaceChild("legpanelc3", CubeListBuilder.create().texOffs(32, 59).mirror(false).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_cloak1 = root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(106, 0).mirror(false).addBox(0.0F, 0.0F, -0.5F, 10.0F, 18.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, -6.0F, 4.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition p_cloak2 = p_cloak1.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(106, 19).mirror(false).addBox(0.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_cloak2.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(106, 24).mirror(false).addBox(0.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
