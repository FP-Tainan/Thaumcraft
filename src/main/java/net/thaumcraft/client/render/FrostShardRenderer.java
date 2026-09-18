package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.model.OrbModel;
import net.thaumcraft.entity.FrostShardEntity;

import java.util.Random;

/**
 * A esfera de gelo: o {@code RenderFrostShard} da 4.2.3.5, descompilado.
 *
 * <p>É o {@code orb.obj} do mod com a textura de gelo, virado para onde a esfera voa, e esticado um pouco
 * diferente em cada eixo — um sorteio com a identidade da esfera como semente —, para que nenhuma saia igual a
 * outra. O tamanho vem do dano: um décimo dele, mais o sorteio.
 */
public class FrostShardRenderer extends EntityRenderer<FrostShardEntity, FrostShardRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/entity/frostshard.png");

    public static class State extends EntityRenderState {
        float yaw;
        float pitch;
        float damage;
        int id;
    }

    public FrostShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FrostShardEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.yaw = entity.getYRot(partial);
        state.pitch = entity.getXRot(partial);
        state.damage = entity.getDamage();
        state.id = entity.getId();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Random random = new Random(state.id);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(state.yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(state.pitch));
        float base = state.damage * 0.1f;
        pose.scale(base + random.nextFloat() * 0.1f, base + random.nextFloat() * 0.1f, base + random.nextFloat() * 0.1f);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE), (matrix, consumer) ->
                ObjMesh.draw(OrbModel.ORB, matrix, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
