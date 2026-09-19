package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GiantBrainyZombieEntity;

/**
 * Os zumbis zangado e furioso: o {@code RenderBrainyZombie} da 4.2.3.5 — o zumbi de sempre com a pele
 * {@code bzombie.png}, e o furioso crescido do tanto da raiva dele.
 */
public class BrainyZombieRenderer extends ZombieRenderer {
    private static final Identifier SKIN = Thaumcraft.id("textures/models/bzombie.png");

    public static class State extends ZombieRenderState {
        float anger = 1.0f;
        boolean giant;
    }

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Zombie zombie, ZombieRenderState state, float partial) {
        super.extractRenderState(zombie, state, partial);
        if (state instanceof State own) {
            own.giant = zombie instanceof GiantBrainyZombieEntity;
            own.anger = zombie instanceof GiantBrainyZombieEntity giant ? giant.anger() : 1.0f;
        }
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return SKIN;
    }

    /** O {@code preRenderScale}: o furioso do tamanho da raiva. */
    @Override
    protected void scale(ZombieRenderState state, PoseStack pose) {
        super.scale(state, pose);
        if (state instanceof State own && own.giant) pose.scale(own.anger, own.anger, own.anger);
    }
}
