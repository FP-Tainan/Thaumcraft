package net.thaumcraft.client.render.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * As peças do caranguejo eldritch, tiradas do {@code ModelEldritchCrab} da 4.2.3.5.
 *
 * <p><strong>Este arquivo é gerado</strong> pelo {@code scratchpad/modelo-entidade.js}, que lê o modelo descompilado do
 * jar: cada caixa com o ponto de giro, o giro e o espelho do original, e os filhos dentro dos pais. Não mexer na mão.
 */
public final class EldritchCrabModel {
    private EldritchCrabModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tailhelm", CubeListBuilder.create().texOffs(0, 0).mirror(false).addBox(-4.5F, -4.5F, -0.4F, 9.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("tailbare", CubeListBuilder.create().texOffs(64, 0).mirror(false).addBox(-4.0F, -4.0F, -0.4F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("rclaw1", CubeListBuilder.create().texOffs(0, 47).mirror(false).addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-6.0F, 15.5F, -10.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(0, 38).mirror(false).addBox(-2.0F, -1.5F, -9.066667F, 4.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rclaw0", CubeListBuilder.create().texOffs(0, 55).mirror(false).addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(-6.0F, 17.0F, -7.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rclaw2", CubeListBuilder.create().texOffs(14, 54).mirror(false).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        root.addOrReplaceChild("rarm", CubeListBuilder.create().texOffs(44, 4).mirror(false).addBox(-1.0F, -1.0F, -5.066667F, 2.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F));
        root.addOrReplaceChild("lclaw2", CubeListBuilder.create().texOffs(14, 54).mirror(false).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        root.addOrReplaceChild("lclaw1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(6.0F, 15.5F, -10.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lclaw0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(6.0F, 17.0F, -7.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("larm", CubeListBuilder.create().texOffs(44, 4).mirror(false).addBox(-1.0F, -1.0F, -4.066667F, 2.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F));
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 18).mirror(false).addBox(-3.5F, -3.5F, -6.066667F, 7.0F, 7.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("head0", CubeListBuilder.create().texOffs(0, 31).mirror(false).addBox(-2.5F, -2.0F, -8.066667F, 5.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rrleg1", CubeListBuilder.create().texOffs(36, 4).mirror(false).addBox(-4.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rfleg1", CubeListBuilder.create().texOffs(36, 4).mirror(false).addBox(-5.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lrleg1", CubeListBuilder.create().texOffs(36, 4).mirror(false).addBox(2.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lfleg1", CubeListBuilder.create().texOffs(36, 4).mirror(false).addBox(3.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rrleg0", CubeListBuilder.create().texOffs(36, 0).mirror(false).addBox(-4.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rfleg0", CubeListBuilder.create().texOffs(36, 0).mirror(false).addBox(-5.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lfleg0", CubeListBuilder.create().texOffs(36, 0).mirror(false).addBox(-1.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 20.0F, -3.5F, 0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lrleg0", CubeListBuilder.create().texOffs(36, 0).mirror(false).addBox(-1.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 20.0F, -1.5F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
