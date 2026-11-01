package net.thaumcraft.occulta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.DecoyEntity;


/**
 * O desenhista do <b>Sósia</b>: o {@code RenderFollower} do Witchery no tipo cinco.
 *
 * <p>Ele é um bípede do jogo com a <b>pele de quem o fez</b> — e isso aqui é de graça: a pessoa copiada
 * está no mundo, e a pele dela já está carregada no lado de cá. Não se baixa nada de servidor nenhum.
 *
 * <p>Não estando ela por perto, fica a pele de reserva que o jogo dá para aquele número. É a mesma pele
 * que um jogador desconhecido teria, e portanto a resposta certa.
 *
 * <p>A armadura é a de verdade, copiada no instante em que ele nasceu — e é ela, mais do que a cara, que
 * faz um bicho hostil se enganar.
 */
public class DecoyRenderer
        extends HumanoidMobRenderer<DecoyEntity, DecoyRenderer.Estado, DecoyRenderer.Corpo> {

    /** O bípede do jogo, que é o que o original usa. */
    public static class Corpo extends HumanoidModel<Estado> {
        public Corpo(ModelPart root) {
            super(root);
        }
    }

    /** O que o desenhista precisa de saber do sósia: de quem ele é a cara. */
    public static class Estado extends HumanoidRenderState {
        public String dono = "";
    }

    public DecoyRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new Corpo(contexto.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        ArmorModelSet<Corpo> armadura = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,
                contexto.getModelSet(), Corpo::new);
        this.addLayer(new HumanoidArmorLayer<>(this, armadura, contexto.getEquipmentRenderer()));
    }

    @Override
    public Estado createRenderState() {
        return new Estado();
    }

    @Override
    public void extractRenderState(DecoyEntity sósia, Estado estado, float parcial) {
        super.extractRenderState(sósia, estado, parcial);
        estado.dono = sósia.dono();
    }

    @Override
    public Identifier getTextureLocation(Estado estado) {
        if (estado.dono.isEmpty()) return DefaultPlayerSkin.getDefaultTexture();
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level != null) {
            for (Player gente : minecraft.level.players()) {
                if (!estado.dono.equals(gente.getGameProfile().name())) continue;
                if (gente instanceof AbstractClientPlayer quem) {
                    return quem.getSkin().body().texturePath();
                }
            }
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }
}
