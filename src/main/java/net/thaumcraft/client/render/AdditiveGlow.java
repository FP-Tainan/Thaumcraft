package net.thaumcraft.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.thaumcraft.Thaumcraft;

import java.util.function.Function;

/**
 * A luz que <strong>soma</strong>: a mistura com que o Thaumcraft desenha fachos, brilhos e faíscas.
 *
 * <p>O original desenha as partículas dele com {@code glBlendFunc(SRC_ALPHA, ONE)}: a cor do facho é
 * somada à do que está atrás, pesada pelo alfa. Dez fachos vermelhos um sobre o outro não dão um vermelho
 * mais forte — dão branco, porque o verde e o azul de cada um vão se acumulando até estourar. É isso que
 * faz o miolo do Nitor ser branco-amarelo e a borda ficar vermelha.
 *
 * <p>Eu vinha usando a porta {@code eyes} do jogo achando que ela somava, e no 26.2 ela não soma mais:
 * mistura como vidro ({@code TRANSLUCENT}). Com ela, empilhar fachos nunca passa da cor de um facho só,
 * e o Nitor saía um disco vermelho apagado por mais fachos que eu pusesse. Este pipeline é o {@code eyes}
 * do jogo, peça por peça, com a única troca que importa: a mistura do raio, {@code SRC_ALPHA, ONE}.
 */
public final class AdditiveGlow {
    /** O {@code eyes} do jogo com a mistura aditiva do original, e sem escrever profundidade. */
    public static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Thaumcraft.id("pipeline/additive_glow"))
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build());

    /** O mesmo, desenhando as duas faces: o {@code glDisable(GL_CULL_FACE)} de efeitos como o clarão do escudo rúnico. */
    public static final RenderPipeline TWO_SIDED_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Thaumcraft.id("pipeline/additive_glow_two_sided"))
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build());

    private static final Function<Identifier, RenderType> BY_TEXTURE = Util.memoize(texture ->
            RenderType.create("thaumcraft_additive_glow", RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", texture)
                    .sortOnUpload()
                    .createRenderSetup()));

    private static final Function<Identifier, RenderType> TWO_SIDED = Util.memoize(texture ->
            RenderType.create("thaumcraft_additive_glow_two_sided", RenderSetup.builder(TWO_SIDED_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .sortOnUpload()
                    .createRenderSetup()));

    private AdditiveGlow() {
    }

    /** A porta aditiva com esta textura. */
    public static RenderType of(Identifier texture) {
        return BY_TEXTURE.apply(texture);
    }

    /** A porta aditiva sem descartar as faces de trás. */
    public static RenderType twoSided(Identifier texture) {
        return TWO_SIDED.apply(texture);
    }
}
