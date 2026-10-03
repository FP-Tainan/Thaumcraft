package net.thaumcraft.occulta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.VampireEntity;

/**
 * O desenhista do Vampiro.
 *
 * <p>O original tem um {@code ModelVampire} próprio, que é o corpo de gente com a roupa de vampiro por cima —
 * e a roupa é um conjunto de armadura que este porte ainda não trouxe. Aqui fica o <b>corpo de gente</b> com
 * a pele do original, que é o que dele se vê de qualquer maneira. <b>Declarado no {@code PORTE.md}.</b>
 */
public class VampireRenderer
        extends MobRenderer<VampireEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/vampire.png");

    public VampireRenderer(EntityRendererProvider.Context contexto) {
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
