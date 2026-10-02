package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.coven.CovenWitchEntity;

/**
 * O desenhista da Bruxa do Coven: o {@code RenderCovenWitch} do Witchery.
 *
 * <p>O corpo é o da <b>bruxa do próprio jogo</b> — era assim no original, que usa o {@code ModelWitch} tal e
 * qual. O que muda é a <b>cara</b>: há cinco, e cada bruxa nasce com uma. É o que faz um coven de seis parecer
 * seis pessoas e não seis cópias.
 */
public class CovenWitchRenderer extends MobRenderer<CovenWitchEntity,
        CovenWitchRenderer.State, WitchModel> {
    /** As cinco peles do original. */
    private static final Identifier[] CARAS = new Identifier[CovenWitchEntity.CARAS];

    static {
        for (int i = 0; i < CARAS.length; i++) {
            CARAS[i] = Thaumcraft.id("textures/entity/coven/witch" + (i + 1) + ".png");
        }
    }

    /** O estado da bruxa do jogo, mais a cara que esta tem. */
    public static class State extends WitchRenderState {
        public int cara;
    }

    public CovenWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CovenWitchEntity bruxa, State estado, float parcial) {
        super.extractRenderState(bruxa, estado, parcial);
        estado.entityId = bruxa.getId();
        estado.isHoldingItem = false;
        estado.isHoldingPotion = false;
        estado.cara = Math.floorMod(bruxa.cara(), CARAS.length);
    }

    @Override
    public Identifier getTextureLocation(State estado) {
        return CARAS[estado.cara];
    }
}
