package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.FollowerEntity;

/**
 * O desenhista de <b>Elle</b>: o {@code RenderFollower} do Witchery, no tipo zero.
 *
 * <p>O corpo é o de <b>gente</b> — era assim no original, que usa o {@code RenderBiped} tal e qual —, e é de
 * propósito: Elle não parece um monstro. Parece uma mulher parada no meio do campo, e é isso que a torna
 * estranha.
 */
public class FollowerRenderer extends MobRenderer<FollowerEntity,
        HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/follower.png");

    public FollowerRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new HumanoidModel<>(contexto.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState estado) {
        return PELE;
    }
}
