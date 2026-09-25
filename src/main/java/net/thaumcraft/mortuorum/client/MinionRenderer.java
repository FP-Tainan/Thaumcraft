package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.mortuorum.MinionEntity;
import net.thaumcraft.mortuorum.MinionParts;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O lacaio no mundo: o {@code RenderMinion} com o {@code ModelMinion} do Necromancy.
 *
 * <p>Ele é desenhado de baixo para cima, como no original: primeiro as pernas, e delas pende o tronco; do tronco
 * pendem os dois braços e a cabeça, cada um no ponto que o tronco daquele bicho marca. Cada peça sai com a folha
 * do bicho de onde foi tirada, e é por isso que o desenho não cabe num modelo só — vai peça por peça, na camada.
 */
public class MinionRenderer extends LivingEntityRenderer<MinionEntity, MinionRenderState, MinionModel> {
    public MinionRenderer(EntityRendererProvider.Context context) {
        super(context, montar(context), 0.5f);
        this.addLayer(new LimbsLayer(this));
    }

    private static MinionModel montar(EntityRendererProvider.Context context) {
        return MinionModel.build(context::bakeLayer);
    }

    @Override
    public MinionRenderState createRenderState() {
        return new MinionRenderState();
    }

    @Override
    public void extractRenderState(MinionEntity minion, MinionRenderState state, float partial) {
        super.extractRenderState(minion, state, partial);
        state.parts = minion.parts();
        state.attackTimer = minion.attackTimer();
        state.partial = partial;
    }

    /** O corpo do lacaio não é um modelo só; quem o desenha é a camada. */
    @Override
    protected @Nullable RenderType getRenderType(MinionRenderState state, boolean body, boolean transparent, boolean glowing) {
        return null;
    }

    @Override
    public Identifier getTextureLocation(MinionRenderState state) {
        // é só para o jogo ter o que pedir; cada peça leva a sua
        return Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
    }

    /**
     * A camada que desenha as cinco peças, na ordem e nos pontos do {@code ModelMinion}: as pernas na raiz, o
     * tronco no ponto que as pernas marcam, e os braços e a cabeça nos pontos que o tronco marca — sem desfazer o
     * deslocamento do tronco, que é o que o original faz e o que põe cada peça no lugar.
     */
    private static class LimbsLayer extends RenderLayer<MinionRenderState, MinionModel> {
        LimbsLayer(MinionRenderer parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector collector, int light, MinionRenderState state,
                           float yRot, float xRot) {
            draw(this.getParentModel(), pose, collector, light, state);
        }
    }

    static void draw(MinionModel model, PoseStack pose, SubmitNodeCollector collector, int light,
                     MinionRenderState state) {
        MinionParts parts = state.parts;
        String head = MinionParts.mobOf(parts.head());
        String torso = MinionParts.mobOf(parts.torso());
        String armLeft = MinionParts.mobOf(parts.armLeft());
        String armRight = MinionParts.mobOf(parts.armRight());
        String legs = MinionParts.mobOf(parts.legs());

        float[] torsoPos = anchor(legs, "torso");
        float[] armLeftPos = anchor(torso, "armLeft");
        float[] armRightPos = anchor(torso, "armRight");
        float[] headPos = anchor(torso, "head");

        // as pernas, na raiz
        pose.pushPose();
        limb(model, legs, "Legs", pose, collector, light);
        pose.popPose();

        // o tronco, no ponto que as pernas marcam — e este deslocamento fica de pé para o que vem depois
        pose.translate(torsoPos[0] / 16.0f, torsoPos[1] / 16.0f, torsoPos[2] / 16.0f);
        limb(model, torso, "Torso", pose, collector, light);

        pose.pushPose();
        pose.translate(armLeftPos[0] / 16.0f, armLeftPos[1] / 16.0f, armLeftPos[2] / 16.0f);
        limb(model, armLeft, "ArmLeft", pose, collector, light);
        pose.popPose();

        pose.pushPose();
        pose.translate(armRightPos[0] / 16.0f, armRightPos[1] / 16.0f, armRightPos[2] / 16.0f);
        limb(model, armRight, "ArmRight", pose, collector, light);
        pose.popPose();

        pose.pushPose();
        pose.translate(headPos[0] / 16.0f, headPos[1] / 16.0f, headPos[2] / 16.0f);
        limb(model, head, "Head", pose, collector, light);
        pose.popPose();
    }

    private static void limb(MinionModel model, String mob, String place, PoseStack pose,
                             SubmitNodeCollector collector, int light) {
        if (mob.isEmpty()) return;
        Identifier folha = MinionModel.textureOf(mob);
        if (folha == null) return;
        List<ModelPart> partes = model.pieces(mob, place);
        for (ModelPart parte : partes) {
            collector.submitModelPart(parte, pose, RenderTypes.entityCutout(folha), light,
                    OverlayTexture.NO_OVERLAY, null);
        }
    }

    /** O ponto em que um bicho pendura a peça seguinte, ou a origem quando não há bicho nenhum ali. */
    private static float[] anchor(String mob, String which) {
        MinionModels.Mob dados = mob.isEmpty() ? null : MinionModels.of(mob);
        if (dados == null) return new float[]{0.0f, 0.0f, 0.0f};
        return switch (which) {
            case "torso" -> dados.torsoPos();
            case "armLeft" -> dados.armLeftPos();
            case "armRight" -> dados.armRightPos();
            default -> dados.headPos();
        };
    }
}
