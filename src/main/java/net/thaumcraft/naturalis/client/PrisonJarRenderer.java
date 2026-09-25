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
    /** O {@code ENTITY_SCALE} do original: o bicho cabe no vidro encolhido a pouco mais de um quinto. */
    public static final float ESCALA = 0.21875f;

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
        // de perto o bicho vira-se para quem olha; de longe ele roda devagar, como no original
        var pos = jar.getBlockPos();
        if (camera.distanceTo(Vec3.atLowerCornerOf(pos)) < 4.5) {
            double dx = camera.x - (pos.getX() + 0.5);
            double dz = camera.z - (pos.getZ() + 0.5);
            state.spin = -((float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f);
        } else {
            state.spin = (System.currentTimeMillis() % 7200L) / 20.0f;
        }
    }

    /** A criatura desenhada, feita uma vez só a partir do que o jarro guarda. */
    private static @Nullable Entity cached(PrisonJarBlockEntity jar) {
        if (jar.clientEntity != null || !jar.hasStored()) return jar.clientEntity;
        CompoundTag guardado = jar.stored();
        var level = Minecraft.getInstance().level;
        if (guardado == null || level == null) return null;
        // o id de mentira do mostruário do gerador de monstros: sem ele o desenhista estoura ao pedir o id de um
        // bicho que nunca entrou no mundo
        jar.clientEntity = EntityType.loadEntityRecursive(guardado, level,
                new EntitySpawnRequest(EntitySpawnReason.TRIGGERED, true), entity -> {
                    entity.setId(-1);
                    return entity;
                });
        return jar.clientEntity;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Entity entity = state.entity;
        if (entity == null) return;
        pose.pushPose();
        pose.translate(0.5f, 0.1f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(state.spin));
        pose.scale(ESCALA, ESCALA, ESCALA);
        @SuppressWarnings("unchecked")
        EntityRenderer<Entity, EntityRenderState> renderer =
                (EntityRenderer<Entity, EntityRenderState>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState estado = renderer.createRenderState(entity, 0.0f);
        estado.lightCoords = state.lightCoords;
        renderer.submit(estado, pose, collector, camera);
        pose.popPose();
    }
}
