package net.thaumcraft.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

/** O zumbi habitado: o {@code RenderInhabitedZombie} da 4.2.3.5 — o zumbi de sempre com a pele {@code czombie.png}. */
public class InhabitedZombieRenderer extends ZombieRenderer {
    private static final Identifier SKIN = Thaumcraft.id("textures/models/czombie.png");

    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return SKIN;
    }
}
