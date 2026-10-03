package net.thaumcraft.occulta.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.WolfmanEntity;

/** O desenhista do Lobisomem: o modelo traduzido e a pele do original. */
public class WolfmanRenderer
        extends MobRenderer<WolfmanEntity, LivingEntityRenderState, WolfmanModel> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/wolfman.png");

    public WolfmanRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new WolfmanModel(contexto.bakeLayer(WolfmanModel.WOLFMAN)), 0.5f);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState estado) {
        return PELE;
    }
}
