package net.thaumcraft.maleficium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.client.render.NitorRenderer;
import net.thaumcraft.maleficium.LumosBlockEntity;

/**
 * A chama do Lumos: o mesmo facho do Nitor, branco.
 *
 * <p><b>Desvio</b>, a pedido de quem joga: no original o Lumos é só uma luz invisível com uma faísca de vez em
 * quando, quase impossível de achar. Aqui ele acende também um Nitor branco, e o resto — as faíscas, o som de
 * gelo, o que ele ilumina — continua como era.
 */
public class LumosRenderer implements BlockEntityRenderer<LumosBlockEntity, NitorRenderer.State> {
    public LumosRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public NitorRenderer.State createRenderState() {
        return new NitorRenderer.State();
    }

    @Override
    public void extractRenderState(LumosBlockEntity lumos, NitorRenderer.State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(lumos, state, crumbling);
        state.tick = lumos.getLevel() == null ? 0L : lumos.getLevel().getGameTime();
        state.partial = partial;
        state.seed = lumos.getBlockPos().hashCode();
    }

    @Override
    public void submit(NitorRenderer.State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        NitorRenderer.flame(state, pose, collector, camera, NitorRenderer.WHITE_JETS);
    }
}
