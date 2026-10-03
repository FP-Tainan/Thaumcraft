package net.thaumcraft.occulta.client;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.hunter.BoltEntity;

/**
 * O desenhista do virote: o {@code RenderBolt} do Witchery.
 *
 * <p>São <b>quatro peles para cinco tipos</b>, e é de propósito: a drenagem e a drenagem forte usam a mesma,
 * porque quem as leva de frente não tem como saber qual delas é — e só descobre quando o poder some.
 */
public class BoltRenderer extends ArrowRenderer<BoltEntity, BoltRenderer.Estado> {
    private static final Identifier COMUM = Thaumcraft.id("textures/entity/projectiles/bolt.png");
    private static final Identifier DRENA = Thaumcraft.id("textures/entity/projectiles/bolt_draining.png");
    private static final Identifier SAGRADO = Thaumcraft.id("textures/entity/projectiles/bolt_holy.png");
    private static final Identifier PRATA = Thaumcraft.id("textures/entity/projectiles/bolt_silver.png");

    /** O que o desenhista precisa de saber do virote: só o tipo. */
    public static class Estado extends ArrowRenderState {
        int tipo;
    }

    public BoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Estado createRenderState() {
        return new Estado();
    }

    @Override
    public void extractRenderState(BoltEntity virote, Estado estado, float parcial) {
        super.extractRenderState(virote, estado, parcial);
        estado.tipo = virote.tipo();
    }

    @Override
    protected Identifier getTextureLocation(Estado estado) {
        return switch (estado.tipo) {
            case BoltEntity.SAGRADO -> SAGRADO;
            case BoltEntity.PRATA -> PRATA;
            case BoltEntity.DRENAGEM, BoltEntity.DRENAGEM_FORTE -> DRENA;
            default -> COMUM;
        };
    }
}
