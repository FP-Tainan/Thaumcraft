package net.thaumcraft.naturalis.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * O {@code TaintedTrunkModel} do Magia Naturalis 0.5.0, um dos quatro feitios do Baú Maligno.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/mn-baus.js} a partir do jar original — não se escreve à mão.
 */
public class TaintedTrunkModel extends EvilTrunkModel {
    public TaintedTrunkModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("chest_skull", CubeListBuilder.create()
                .texOffs(0, 19).addBox(0.0f, -10.0f, -14.0f, 14, 10, 14),
                PartPose.offset(1.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_jaw", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0f, 0.0f, -14.0f, 14, 5, 14),
                PartPose.offset(1.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth1", CubeListBuilder.create()
                .texOffs(7, 0).addBox(-1.0f, -1.0f, -14.5f, 2, 3, 1),
                PartPose.offset(13.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth_top1", CubeListBuilder.create()
                .texOffs(9, 0).addBox(0.0f, -2.0f, -14.5f, 1, 1, 1),
                PartPose.offset(13.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0f, -1.0f, -14.5f, 2, 3, 1),
                PartPose.offset(3.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth_top2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0f, -2.0f, -14.5f, 1, 1, 1),
                PartPose.offset(3.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth3", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.7f, -1.0f, -14.5f, 2, 3, 1),
                PartPose.offset(6.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_tooth4", CubeListBuilder.create()
                .texOffs(7, 0).addBox(-1.25f, -1.0f, -14.5f, 2, 3, 1),
                PartPose.offset(10.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_horn1", CubeListBuilder.create()
                .texOffs(0, 5).addBox(6.0f, -12.0f, -13.0f, 2, 2, 2),
                PartPose.offset(1.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_horn2", CubeListBuilder.create()
                .texOffs(0, 5).addBox(6.0f, -14.0f, -13.4f, 2, 2, 2),
                PartPose.offset(1.0f, 11.0f, 15.0f));
        root.addOrReplaceChild("chest_horn_top", CubeListBuilder.create()
                .texOffs(3, 6).addBox(6.5f, -16.0f, -13.2f, 1, 2, 1),
                PartPose.offset(1.0f, 11.0f, 15.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** As peças que a boca leva consigo quando o baú abre. */
    @Override
    protected String[] lid() {
        return new String[]{"chest_skull", "chest_horn_top", "chest_horn2", "chest_horn1"};
    }
}
