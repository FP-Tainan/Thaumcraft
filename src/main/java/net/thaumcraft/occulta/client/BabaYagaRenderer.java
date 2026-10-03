package net.thaumcraft.occulta.client;

import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.baba.BabaYagaEntity;

/**
 * O desenhista da Baba Yaga: o corpo da bruxa do jogo, com a pele do original.
 *
 * <p>O {@code ModelBabaYaga} do Witchery é o modelo da bruxa com o chapéu trocado e a verruga tirada; o que
 * dela se vê, de perto e de longe, é a <b>pele</b>. Fica o corpo do jogo com a pele dele. <b>Declarado no
 * {@code PORTE.md}.</b>
 */
public class BabaYagaRenderer extends MobRenderer<BabaYagaEntity, WitchRenderState, WitchModel> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/baba_yaga.png");

    public BabaYagaRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new WitchModel(contexto.bakeLayer(ModelLayers.WITCH)), 0.6f);
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(BabaYagaEntity baba, WitchRenderState estado, float parcial) {
        super.extractRenderState(baba, estado, parcial);
        estado.isHoldingItem = !baba.getMainHandItem().isEmpty();
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState estado) {
        return PELE;
    }
}
