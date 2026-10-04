package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.demon.DemonEntity;

/**
 * O Demônio no mundo: o {@code RenderDemon} do Witchery.
 *
 * <p>Ele é do tamanho das caixas dele, como o golem de ferro de que é feito — e <b>oscila com o passo</b>: o
 * {@code rotateDemonCorpse} do original gira o corpo inteiro <b>seis graus e meio</b> para um lado e para o
 * outro, na mesma curva de triângulo das pernas.
 *
 * <p>Esse gingado é a assinatura do golem de ferro, e é o que impede uma coisa de três metros de parecer que
 * desliza: ela <b>cai</b> para um lado a cada passo, como se o peso dela não soubesse ficar quieto.
 */
public class DemonRenderer extends MobRenderer<DemonEntity, DemonRenderer.State, DemonModel> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/demon.png");

    /** O quanto o corpo cai para o lado, e o passo em que ele cai. */
    public static final float GINGA = 6.5f;
    public static final float O_PASSO = 13.0f;

    /** O que o desenhista precisa saber dele: quanto falta do golpe. */
    public static class State extends LivingEntityRenderState {
        public float golpe;
    }

    public DemonRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new DemonModel(contexto.bakeLayer(DemonModel.DEMÔNIO)), 0.7f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DemonEntity demônio, State estado, float parcial) {
        super.extractRenderState(demônio, estado, parcial);
        estado.golpe = demônio.braçoNoAr() > 0 ? demônio.braçoNoAr() - parcial : 0.0f;
    }

    @Override
    public Identifier getTextureLocation(State estado) {
        return PELE;
    }

    /** A ginga: seis graus e meio de lado, na curva de triângulo do passo. */
    @Override
    protected void setupRotations(State estado, PoseStack pose, float rumo, float tamanho) {
        super.setupRotations(estado, pose, rumo, tamanho);
        if (estado.walkAnimationSpeed < 0.01f) return;
        float onda = DemonModel.triângulo(estado.walkAnimationPos + 6.0f, O_PASSO);
        pose.mulPose(Axis.ZP.rotationDegrees(GINGA * onda));
    }
}
