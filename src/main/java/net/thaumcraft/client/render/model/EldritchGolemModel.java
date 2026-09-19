package net.thaumcraft.client.render.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * golem eldritch, tiradas do {@code ModelEldritchGolem} da 4.2.3.5.
 *
 * <p><strong>Este arquivo é gerado</strong> pelo {@code scratchpad/modelo-entidade.js}, que lê o modelo descompilado do
 * jar: cada caixa com o ponto de giro, o giro e o espelho do original, e os filhos dentro dos pais. Não mexer na mão.
 */
public final class EldritchGolemModel {
    private EldritchGolemModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).mirror(false).addBox(-5.0F, 1.5F, 4.0F, 10.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 37).mirror(false).addBox(-5.0F, 17.5F, -0.8F, 10.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.4465716F, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).mirror(false).addBox(-5.0F, 13.5F, 1.7F, 10.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.3069452F, 0.0F, 0.0F));
        root.addOrReplaceChild("cloakcl", CubeListBuilder.create().texOffs(0, 43).mirror(false).addBox(3.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("cloakcr", CubeListBuilder.create().texOffs(0, 43).mirror(false).addBox(-5.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(47, 12).mirror(false).addBox(-3.5F, -6.0F, -2.5F, 7.0F, 7.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, -0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(26, 16).mirror(false).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("collarl", CubeListBuilder.create().texOffs(75, 50).mirror(false).addBox(3.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("collarr", CubeListBuilder.create().texOffs(67, 50).mirror(false).addBox(-4.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("collarb", CubeListBuilder.create().texOffs(77, 59).mirror(false).addBox(-3.5F, -0.5F, 2.0F, 7.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("collarf", CubeListBuilder.create().texOffs(77, 59).mirror(false).addBox(-3.5F, -0.5F, -7.0F, 7.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("collarblack", CubeListBuilder.create().texOffs(22, 0).mirror(false).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 1.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth0", CubeListBuilder.create().texOffs(114, 52).mirror(false).addBox(-3.0F, 3.2F, -3.5F, 6.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(114, 39).mirror(false).addBox(-1.0F, 1.5F, -3.5F, 6.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(114, 47).mirror(false).addBox(-1.0F, 8.5F, -1.5F, 6.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, -0.3316126F, 0.0F, 0.0F));
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(34, 45).mirror(false).addBox(-5.0F, 2.5F, -3.0F, 10.0F, 10.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F));
        PartDefinition p_armr = root.addOrReplaceChild("armr", CubeListBuilder.create().texOffs(78, 32).mirror(false).addBox(-3.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F),
                PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, 0.1047198F));
        p_armr.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(0, 0).mirror(false).addBox(-4.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
        p_armr.addOrReplaceChild("shoulderr0", CubeListBuilder.create().texOffs(56, 31).mirror(false).addBox(-4.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_armr.addOrReplaceChild("shoulderr1", CubeListBuilder.create().texOffs(0, 23).mirror(false).addBox(-3.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
        p_armr.addOrReplaceChild("shoulderr2", CubeListBuilder.create().texOffs(0, 12).mirror(false).addBox(-2.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F));
        PartDefinition p_arml = root.addOrReplaceChild("arml", CubeListBuilder.create().texOffs(78, 32).mirror().addBox(-0.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F),
                PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -0.1047198F));
        p_arml.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
        p_arml.addOrReplaceChild("shoulderl0", CubeListBuilder.create().texOffs(56, 31).mirror().addBox(-0.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        p_arml.addOrReplaceChild("shoulderl1", CubeListBuilder.create().texOffs(0, 23).mirror().addBox(2.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
        p_arml.addOrReplaceChild("shoulderl2", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(0.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F));
        root.addOrReplaceChild("backpanelr1", CubeListBuilder.create().texOffs(96, 7).mirror(false).addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.addOrReplaceChild("waistr1", CubeListBuilder.create().texOffs(96, 14).mirror(false).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.addOrReplaceChild("waistr2", CubeListBuilder.create().texOffs(116, 13).mirror(false).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.addOrReplaceChild("waistr3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.addOrReplaceChild("legr", CubeListBuilder.create().texOffs(79, 19).mirror(false).addBox(-2.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offsetAndRotation(-2.0F, 12.5F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("waistl1", CubeListBuilder.create().texOffs(96, 14).mirror(false).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        root.addOrReplaceChild("waistl2", CubeListBuilder.create().texOffs(116, 13).mirror(false).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        root.addOrReplaceChild("waistl3", CubeListBuilder.create().texOffs(114, 5).mirror(false).addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        root.addOrReplaceChild("backpanell1", CubeListBuilder.create().texOffs(96, 7).mirror(false).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        root.addOrReplaceChild("legl", CubeListBuilder.create().texOffs(79, 19).mirror(false).addBox(-1.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offsetAndRotation(2.0F, 12.5F, 0.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
