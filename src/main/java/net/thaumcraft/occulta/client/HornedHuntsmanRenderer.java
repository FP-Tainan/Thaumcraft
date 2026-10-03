package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.HornedHuntsmanEntity;

/**
 * O desenhista do Caçador Cornudo: o {@code RenderHornedAvatar} do Witchery.
 *
 * <p>Ele traz uma coisa que nenhum outro bicho deste porte tem: <b>o corpo balança</b>. Andando, o Caçador
 * se inclina de um lado para o outro cinco graus e meio, no compasso do passo — e é isso que faz dele uma
 * coisa <b>pesada</b> em vez de um boneco grande.
 *
 * <p>O original ainda lhe punha um <b>brilho verde correndo</b> por cima do corpo, com a folha do
 * encantamento. Esse brilho era <b>opcional no próprio original</b> — o {@code renderHuntsmanGlintEffect} do
 * ajuste dele —, e aqui não está: o jeito de hoje de pintar um brilho por cima de um modelo é outro, e um
 * verde por cima de um bicho que já é escuro não é o que faz dele assustador. O que faz é o tamanho e o
 * balanço, e esses estão.
 */
public class HornedHuntsmanRenderer
        extends MobRenderer<HornedHuntsmanEntity, HornedHuntsmanRenderer.State, HornedHuntsmanModel> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/horned_huntsman.png");

    /** Quanto o corpo dele balança, e o compasso do balanço. */
    public static final float BALANÇO = 5.5f;
    public static final float COMPASSO = 13.0f;

    /** A partir de que passo o balanço começa. */
    public static final float ANDANDO = 0.01f;

    /** O que o original soma ao passo antes de medir a onda. */
    public static final float ADIANTA = 6.0f;

    /** O estado dele leva o braço da pancada, que o modelo lê. */
    public static class State extends LivingEntityRenderState {
        public float braço;
    }

    public HornedHuntsmanRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new HornedHuntsmanModel(contexto.bakeLayer(HornedHuntsmanModel.HUNTSMAN)), 1.0f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HornedHuntsmanEntity caçador, State estado, float parcial) {
        super.extractRenderState(caçador, estado, parcial);
        estado.braço = caçador.pancadaNoBraço() - parcial;
    }

    /** E o balanço, que é o {@code rotateHornedAvatarCorpse} do original. */
    @Override
    protected void setupRotations(State estado, PoseStack pose, float giro, float escala) {
        super.setupRotations(estado, pose, giro, escala);
        if (estado.walkAnimationSpeed < ANDANDO) return;
        float onda = HornedHuntsmanModel.onda(estado.walkAnimationPos + ADIANTA, COMPASSO);
        pose.mulPose(Axis.ZP.rotationDegrees(BALANÇO * onda));
    }

    @Override
    public Identifier getTextureLocation(State estado) {
        return PELE;
    }
}
