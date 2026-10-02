package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.spirit.CorpseEntity;
import net.thaumcraft.occulta.spirit.NightmareEntity;

/**
 * Os desenhistas do que anda no sonho: o {@code RenderCorpse} e o {@code RenderNightmare} do Witchery.
 */
public final class SpiritRenderers {
    private static final Identifier CORPSE_SKIN = Thaumcraft.id("textures/entity/reflection.png");
    private static final Identifier NIGHTMARE_SKIN = Thaumcraft.id("textures/entity/nightmare.png");

    /** A volta com que o jogo deita um morto, e com que o original deita o corpo: noventa graus. */
    private static final float DEATH_ROTATION = 90.0f;

    private SpiritRenderers() {
    }

    /** O bípede do jogo, que serve aos dois. */
    public static class SpiritModel extends HumanoidModel<HumanoidRenderState> {
        public SpiritModel(ModelPart root) {
            super(root);
        }
    }

    /**
     * O corpo deitado: o bípede do jogo, <b>virado de costas no chão</b>, que é o que o original faz para o
     * mostrar caído.
     *
     * <p><b>Desvio declarado:</b> no original o corpo usa a <b>pele de quem o deixou</b>, baixada do servidor de
     * peles; aqui, como no Reflexo, ele leva a pele de reserva do próprio Witchery. Baixar a pele de alguém de um
     * servidor de fora é coisa que este porte não faz.
     */
    public static class Corpse extends HumanoidMobRenderer<CorpseEntity, HumanoidRenderState, SpiritModel> {
        public Corpse(EntityRendererProvider.Context context) {
            super(context, new SpiritModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        /**
         * Deitado: a mesma volta com que o {@code RenderCorpse} o deita — noventa graus em Z.
         *
         * <p><b>Desvio declarado:</b> o original põe antes dela um {@code glTranslatef(0.9, 0.25, 0)} e uma
         * segunda volta em Y, que eram o que os números de 2014 pediam para o corpo assentar. Com as contas de
         * hoje eles atiram-no para o ar e para o lado; a volta em Z sozinha é a que o jogo de agora usa para
         * deitar um morto, e é a que o põe no chão. O que se vê é o que o original mostra.
         */
        @Override
        protected void setupRotations(HumanoidRenderState state, PoseStack pose, float giro, float escala) {
            super.setupRotations(state, pose, giro, escala);
            pose.mulPose(Axis.ZP.rotationDegrees(DEATH_ROTATION));
        }

        /**
         * E o corpo fica <b>duro</b>: o original passa ao bípede um {@code setRotationAngles} vazio, e o que isso
         * faz é tirar-lhe a animação toda. Aqui se zera o que a alimenta.
         */
        @Override
        public void extractRenderState(CorpseEntity corpo, HumanoidRenderState state, float partial) {
            super.extractRenderState(corpo, state, partial);
            state.walkAnimationPos = 0.0f;
            state.walkAnimationSpeed = 0.0f;
            state.attackTime = 0.0f;
            state.isCrouching = false;
            state.xRot = 0.0f;
            state.yRot = 0.0f;
            state.deathTime = 0.0f;
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return CORPSE_SKIN;
        }
    }

    /** E o Pesadelo, que anda de pé e vem vestido do que apanhou. */
    public static class Nightmare extends HumanoidMobRenderer<NightmareEntity, HumanoidRenderState, SpiritModel> {
        public Nightmare(EntityRendererProvider.Context context) {
            super(context, new SpiritModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
            ArmorModelSet<SpiritModel> armadura = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,
                    context.getModelSet(), SpiritModel::new);
            this.addLayer(new HumanoidArmorLayer<>(this, armadura, context.getEquipmentRenderer()));
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return NIGHTMARE_SKIN;
        }
    }
}
