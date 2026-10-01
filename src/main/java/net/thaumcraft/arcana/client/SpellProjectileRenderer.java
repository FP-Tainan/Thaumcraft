package net.thaumcraft.arcana.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.SpellProjectileEntity;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * O feitiço voando visto: o {@code RenderSpellProjectile} do Ars Magica 2.
 *
 * <p>Ele é um <b>quadrado virado para a câmera</b>, de meio bloco de lado, com a figura de partícula do
 * original pintada da cor da Afinidade, em mistura comum e sem escrever profundidade — o que faz dele uma luz
 * que passa por cima do que está atrás sem recortar nada.
 *
 * <p><b>A figura e a cor saem da Afinidade do feitiço</b>, e é isso que faz um feitiço de fogo parecer um
 * feitiço de fogo sem ninguém ter escolhido: o estouro para o Fogo, a brasa para o Gelo, o brilho para a Vida,
 * a pedra para a Terra, o vento para o Ar. Quem não puxa para lado nenhum sai com a {@code lens_flare} branca,
 * que é o que o original dá à Afinidade nenhuma.
 *
 * <p>As três cores que o original escreve à mão — Ender, Gelo e Vida — estão aqui; as outras sete saem
 * brancas, porque a figura delas já vem colorida.
 *
 * <p><b>Desvio declarado:</b> o original deixa o jogo animar as figuras sozinho, porque elas vivem no atlas
 * dos itens e trazem um {@code .mcmeta} de animação. Aqui cada uma é uma textura de entidade, que não passa
 * pelo atlas, e por isso o desenho <b>anda a tira ele mesmo</b>, um quadro por batida — que é o que o
 * {@code .mcmeta} sem tempo declarado pede, e dá a mesma coisa na tela.
 */
public class SpellProjectileRenderer
        extends EntityRenderer<SpellProjectileEntity, SpellProjectileRenderer.State> {
    /** Metade do quadrado: o {@code glScalef(0.5F, …)} do original sobre um quadrado de um bloco. */
    private static final float HALF = 0.25f;

    /**
     * Quantos quadros tem a tira de cada Afinidade.
     *
     * <p>Sai da altura dividida pela largura de cada figura do original, e varia muito: o estouro do Fogo tem
     * 24, o relâmpago 20, a pedra 16, o vento 10 — e a brasa do Gelo, o brilho da Vida e a bola de Água são
     * quadros soltos, sem animação nenhuma.
     */
    private static final Map<Affinity, Integer> FRAMES = new EnumMap<>(Map.ofEntries(
            Map.entry(Affinity.NONE, 13),
            Map.entry(Affinity.ARCANE, 8),
            Map.entry(Affinity.WATER, 1),
            Map.entry(Affinity.FIRE, 24),
            Map.entry(Affinity.EARTH, 16),
            Map.entry(Affinity.AIR, 10),
            Map.entry(Affinity.LIGHTNING, 20),
            Map.entry(Affinity.ICE, 1),
            Map.entry(Affinity.NATURE, 13),
            Map.entry(Affinity.LIFE, 1),
            Map.entry(Affinity.ENDER, 24)));

    /** As três cores que o original escreve à mão no {@code setEffectStack}; as outras saem brancas. */
    private static final Map<Affinity, Integer> COLORS = new EnumMap<>(Map.of(
            Affinity.ENDER, 0x550055,
            Affinity.ICE, 0x2299FF,
            Affinity.LIFE, 0x22FF44));

    private static final Map<Affinity, Identifier> TEXTURES = new EnumMap<>(Affinity.class);

    static {
        for (Affinity qual : Affinity.values()) {
            TEXTURES.put(qual, Thaumcraft.id("textures/entity/spell/"
                    + qual.name().toLowerCase(Locale.ROOT) + ".png"));
        }
    }

    public static class State extends EntityRenderState {
        Affinity affinity = Affinity.NONE;
        int frame;
        /** A cor que a frase escolheu, ou {@link SpellProjectileEntity#SEM_COR}. */
        int cor = SpellProjectileEntity.SEM_COR;
    }

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpellProjectileEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.affinity = entity.affinity();
        state.frame = entity.tickCount % FRAMES.getOrDefault(state.affinity, 1);
        state.cor = entity.color();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int quadros = FRAMES.getOrDefault(state.affinity, 1);
        float v0 = (float) state.frame / quadros;
        float v1 = (float) (state.frame + 1) / quadros;
        // a Cor, se a frase trouxer uma; senão a da Afinidade, que é o que o original faz
        int cor = 0xFF000000 | (state.cor != SpellProjectileEntity.SEM_COR
                ? state.cor : COLORS.getOrDefault(state.affinity, 0xFFFFFF));
        RenderType porta = AdditiveGlow.blended(TEXTURES.get(state.affinity));

        pose.pushPose();
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, porta, (matrix, buffer) -> {
            corner(matrix, buffer, -HALF, -HALF, 0.0f, v1, cor);
            corner(matrix, buffer, HALF, -HALF, 1.0f, v1, cor);
            corner(matrix, buffer, HALF, HALF, 1.0f, v0, cor);
            corner(matrix, buffer, -HALF, HALF, 0.0f, v0, cor);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer buffer, float x, float y,
                               float u, float v, int cor) {
        buffer.addVertex(matrix, x, y, 0.0f).setColor(cor).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }
}
