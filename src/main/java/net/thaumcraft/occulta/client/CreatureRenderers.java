package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.EntEntity;
import net.thaumcraft.occulta.MandrakeEntity;
import net.thaumcraft.occulta.MinedrakeEntity;

/**
 * Os desenhistas dos bichos do Ars Occulta: o {@code RenderMandrake}, o {@code RenderMindrake} e o
 * {@code RenderEnt} do Witchery.
 *
 * <p>Os modelos são os do original, caixa por caixa (ver o {@link CreatureModels}); o que fica aqui é a pele de
 * cada um e o tamanho com que o original os desenha.
 *
 * <p>Nenhum dos três leva escala: o tamanho é o das caixas, e é por isso que o Ent fica do tamanho de uma árvore.
 * O que os três têm em comum é o <b>balanço</b> do {@code rotateCorpse}: andando, o corpo pende de um lado para o
 * outro, que é o andar de uma planta que não tem pernas.
 */
public final class CreatureRenderers {
    public static final ModelLayerLocation MANDRAKE = layer("mandrake");
    public static final ModelLayerLocation MINEDRAKE = layer("minedrake");
    public static final ModelLayerLocation ENT = layer("ent");

    private static final Identifier MANDRAKE_SKIN = Thaumcraft.id("textures/entity/mandrake.png");
    private static final Identifier MINEDRAKE_SKIN = Thaumcraft.id("textures/entity/minedrake.png");
    private static final Identifier ENT_SKIN = Thaumcraft.id("textures/entity/ent.png");

    private CreatureRenderers() {
    }

    private static ModelLayerLocation layer(String nome) {
        return new ModelLayerLocation(Thaumcraft.id(nome), "main");
    }

    // ------------------------------------------------------------- o balanço de quem anda sem pernas

    /** O compasso e o quanto pende: os treze e os seis graus e meio do {@code rotateCorpse} do original. */
    public static final float SWAY_PERIOD = 13.0f;
    public static final float SWAY_DEGREES = 6.5f;

    /**
     * O {@code rotateCorpse} dos três: parada, a planta fica direita; andando, pende de um lado ao outro no
     * compasso do passo. O {@code walkAnimationPos} de hoje já vem contado com a parte da batida, que é o
     * {@code field_70754_ba - field_70721_aZ * (1 - partialTick)} do original.
     */
    public static void sway(LivingEntityRenderState state, PoseStack pose) {
        if (state.walkAnimationSpeed < 0.01f) return;
        float passo = state.walkAnimationPos + 6.0f;
        float pende = (Math.abs(passo % SWAY_PERIOD - SWAY_PERIOD * 0.5f) - SWAY_PERIOD * 0.25f)
                / (SWAY_PERIOD * 0.25f);
        pose.mulPose(Axis.ZP.rotationDegrees(SWAY_DEGREES * pende));
    }

    // ------------------------------------------------------------- os modelos

    public static class PlantModel extends EntityModel<LivingEntityRenderState> {
        public PlantModel(ModelPart root) {
            super(root);
        }
    }

    // ------------------------------------------------------------- os desenhistas

    /** A mandrágora que anda. */
    public static class Mandrake extends MobRenderer<MandrakeEntity, LivingEntityRenderState, PlantModel> {
        public Mandrake(EntityRendererProvider.Context context) {
            super(context, new PlantModel(context.bakeLayer(MANDRAKE)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void setupRotations(LivingEntityRenderState state, PoseStack pose, float corpo, float escala) {
            super.setupRotations(state, pose, corpo, escala);
            sway(state, pose);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return MANDRAKE_SKIN;
        }
    }

    /** A de mina, que o original desenha um pouco menor. */
    public static class Minedrake extends MobRenderer<MinedrakeEntity, LivingEntityRenderState, PlantModel> {
        public Minedrake(EntityRendererProvider.Context context) {
            super(context, new PlantModel(context.bakeLayer(MINEDRAKE)), 0.25f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void setupRotations(LivingEntityRenderState state, PoseStack pose, float corpo, float escala) {
            super.setupRotations(state, pose, corpo, escala);
            sway(state, pose);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return MINEDRAKE_SKIN;
        }
    }

    /** E o Ent, que é do tamanho de uma árvore pequena. */
    public static class Ent extends MobRenderer<EntEntity, LivingEntityRenderState, PlantModel> {
        public Ent(EntityRendererProvider.Context context) {
            super(context, new PlantModel(context.bakeLayer(ENT)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void setupRotations(LivingEntityRenderState state, PoseStack pose, float corpo, float escala) {
            super.setupRotations(state, pose, corpo, escala);
            sway(state, pose);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return ENT_SKIN;
        }
    }
}
