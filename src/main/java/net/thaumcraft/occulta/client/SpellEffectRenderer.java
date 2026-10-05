package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.occulta.symbol.ProjectileSymbol;
import net.thaumcraft.occulta.symbol.SpellEffectEntity;
import net.thaumcraft.occulta.symbol.Symbols;

/**
 * A <b>bola de feitiço</b> desenhada: o {@code RenderSpellEffect} do Witchery.
 *
 * <p>Um <b>quadrado sempre virado para quem olha</b>, com a textura do original, pintado da <b>cor do
 * símbolo</b> e a pouco mais de metade de opaco.
 *
 * <p>A cor e o tamanho não estão na entidade: estão no <b>símbolo</b>, e a entidade só leva o número dele.
 * É por isso que o número atravessa a rede — para o lado de cá poder perguntar de que cor é a bola que está
 * vendo.
 */
public class SpellEffectRenderer extends EntityRenderer<SpellEffectEntity,
        SpellEffectRenderer.State> {
    /**
     * <b>A folha é a da bola de neve do jogo</b>, e não uma do mod.
     *
     * <p>Parece engano e não é: o original <b>tem</b> uma folha própria para isto — a
     * {@code spelleffect.png} — e <b>não a usa</b>. O desenhista dele liga o atlas dos itens e pede o ícone
     * da bola de neve, que é redondo e branco, para o poder pintar de qualquer cor. A folha própria ficou no
     * mod sem ninguém lhe chamar.
     *
     * <p>Fica como está, porque é o que se vê no jogo dele.
     */
    public static final Identifier FOLHA =
            Identifier.withDefaultNamespace("textures/item/snowball.png");

    /** O quanto a bola é opaca, e o quanto o tamanho do símbolo a encolhe. */
    public static final int OPACA = 140;
    public static final float ENCOLHE = 0.65f;

    /** Metade do quadrado, antes de o tamanho do símbolo lhe mexer. */
    private static final float METADE = 0.25f;

    public static class State extends EntityRenderState {
        int cor = 0xFF0000;
        float tamanho = 1.0f;
    }

    public SpellEffectRenderer(EntityRendererProvider.Context contexto) {
        super(contexto);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpellEffectEntity bola, State estado, float parcial) {
        super.extractRenderState(bola, estado, parcial);
        var qual = Symbols.daquele(bola.símbolo());
        if (qual instanceof ProjectileSymbol atirado) {
            estado.cor = atirado.cor();
            estado.tamanho = atirado.tamanho();
        }
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        float meio = METADE * estado.tamanho * ENCOLHE;
        int cor = OPACA << 24 | (estado.cor & 0xFFFFFF);
        RenderType tipo = RenderTypes.entityTranslucent(FOLHA);

        pose.pushPose();
        pose.mulPose(câmara.orientation);
        coletor.submitCustomGeometry(pose, tipo, (matriz, buffer) -> {
            quina(matriz, buffer, -meio, -meio, 0.0f, 1.0f, cor);
            quina(matriz, buffer, meio, -meio, 1.0f, 1.0f, cor);
            quina(matriz, buffer, meio, meio, 1.0f, 0.0f, cor);
            quina(matriz, buffer, -meio, meio, 0.0f, 0.0f, cor);
        });
        pose.popPose();
        super.submit(estado, pose, coletor, câmara);
    }

    private static void quina(PoseStack.Pose matriz, VertexConsumer buffer, float x, float y,
                              float u, float v, int cor) {
        buffer.addVertex(matriz, x, y, 0.0f)
                .setColor(cor)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matriz, 0.0f, 1.0f, 0.0f);
    }
}
