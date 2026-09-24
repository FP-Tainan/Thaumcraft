package net.thaumcraft.naturalis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.naturalis.PrisonJarBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O bicho dentro do jarro: o {@code TileJarPrisonRenderer} do Magia Naturalis 0.5.0. Ele flutua no meio do vidro,
 * encolhido para caber, e gira devagar virando-se para quem olha.
 */
public class PrisonJarRenderer implements BlockEntityRenderer<PrisonJarBlockEntity, PrisonJarRenderer.State> {
    public static class State extends BlockEntityRenderState {
        @Nullable Entity entity;
        float spin;
    }

    public PrisonJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PrisonJarBlockEntity jar, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(jar, state, crumbling);
        state.entity = cached(jar);
        state.spin = (System.currentTimeMillis() % 7200L) / 20.0f;
    }

    /** A criatura desenhada, feita uma vez só a partir do que o jarro guarda. */
    private static @Nullable Entity cached(PrisonJarBlockEntity jar) {
        if (jar.clientEntity != null || !jar.hasStored()) return jar.clientEntity;
        CompoundTag guardado = jar.stored();
        var level = Minecraft.getInstance().level;
        if (guardado == null || level == null) return null;
        jar.clientEntity = EntityType.loadEntityRecursive(guardado, level,
                new EntitySpawnRequest(EntitySpawnReason.TRIGGERED, true), entity -> entity);
        return jar.clientEntity;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Entity entity = state.entity;
        if (entity == null) return;
        // o bicho cabe no jarro encolhendo conforme o tamanho dele
        float maior = Math.max(entity.getBbHeight(), entity.getBbWidth());
        float escala = Math.min(0.5f, 0.5f / maior);
        pose.pushPose();
        pose.translate(0.5f, 0.15f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(state.spin * 8.0f));
        pose.scale(escala, escala, escala);
        @SuppressWarnings("unchecked")
        EntityRenderer<Entity, EntityRenderState> renderer =
                (EntityRenderer<Entity, EntityRenderState>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState estado = renderer.createRenderState(entity, 0.0f);
        estado.lightCoords = state.lightCoords;
        renderer.submit(estado, pose, collector, camera);
        pose.popPose();
    }
}
