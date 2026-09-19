package net.thaumcraft.client.render.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.thaumcraft.Thaumcraft;

/** O tentáculo gigante: o {@code RenderTaintacle} de catorze gomos, com a sombra de um bloco, um terço maior. */
public class TaintacleGiantRenderer extends TaintRenderers.Taintacle {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("taintacle_giant"), "main");

    public TaintacleGiantRenderer(EntityRendererProvider.Context context) {
        super(context, LAYER, 14, 1.0f);
    }

    @Override
    protected void scale(TaintOddModels.State state, PoseStack pose) {
        pose.scale(1.33f, 1.33f, 1.33f);
        super.scale(state, pose);
    }
}
