package net.thaumcraft.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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
 * O {@code entity_cutout} do jogo sem o sombreamento das faces: o {@code glDisable(GL_LIGHTING)} com que o original
 * desenha alguns modelos .obj, como as costas do arreio taumostático. A luz do lugar continua valendo.
 */
public final class UnlitCutout {
    public static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Thaumcraft.id("pipeline/unlit_cutout"))
                    .withBindGroupLayout(BindGroupLayouts.LIGHTING)
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .build());

    private static final Function<Identifier, RenderType> BY_TEXTURE = Util.memoize(texture ->
            RenderType.create("thaumcraft_unlit_cutout", RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .createRenderSetup()));

    private UnlitCutout() {
    }

    public static RenderType of(Identifier texture) {
        return BY_TEXTURE.apply(texture);
    }
}
