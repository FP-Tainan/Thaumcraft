package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.leonard.LeonardEntity;

/**
 * O <b>Leonard</b> no mundo: o {@code ModelLeonard} e o {@code RenderLeonard} do Witchery.
 *
 * <p>É um <b>bode de pé</b> num roupão: um pescoço curto inclinado, uma cabeça com <b>focinho</b> comprido,
 * <b>barba</b>, duas <b>orelhas</b> caídas e <b>três chifres</b> — dois para fora e um ao meio, que é o que
 * lhe dá a cara de ídolo.
 *
 * <p>O corpo é o de um boneco comum, e as pernas trazem <b>a saia do roupão</b> presa a elas: duas caixas de
 * cinco por onze por cinco, uma por perna, postas de volta no meio do corpo. É um truque do original e é o
 * que faz a saia <b>abrir ao andar</b> sem haver peça nenhuma para isso.
 *
 * <p><b>O que se mexe:</b> o pescoço segue o olhar, os braços e as pernas andam em compasso oposto, e os
 * braços <b>balançam de leve</b> o tempo todo — um cosseno lento que o original põe em cima de tudo o resto,
 * e que é o que o faz parecer vivo mesmo parado. E quando ele bate, o braço direito <b>sobe e desce</b> numa
 * onda de dez batidas.
 */
public final class LeonardRenderer {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("leonard"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/leonard.png");

    /** A inclinação de dez graus com que o pescoço, a cabeça e o focinho nascem. */
    public static final float INCLINA = 0.1745329f;

    /** O compasso do andar, e o quanto braços e pernas abrem. */
    public static final float COMPASSO = 0.6662f;
    public static final float BRAÇO_ABRE = 2.0f;
    public static final float PERNA_ABRE = 1.4f;

    /** O balanço de sempre: um cosseno lento nos ombros e um seno mais lento nos braços. */
    public static final float BALANÇO_OMBRO = 0.09f;
    public static final float BALANÇO_BRAÇO = 0.067f;
    public static final float BALANÇO = 0.05f;

    /** E a onda do murro: dez batidas de braço erguido. */
    public static final float MURRO = 10.0f;

    private LeonardRenderer() {
    }

    /**
     * A malha, caixa por caixa.
     *
     * <p>A árvore é a do original e não é a de um boneco do jogo: o <b>pescoço</b> é a raiz de tudo o que é
     * cabeça, e a <b>saia</b> pendura-se das pernas. O corpo, os braços e as pernas são irmãos do pescoço, e
     * não filhos dele.
     */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition pescoço = raiz.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-2.0f, -1.0f, -2.0f, 4.0f, 2.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, INCLINA, 0.0f, 0.0f));

        PartDefinition cabeça = pescoço.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -5.0f, -1.0f, 6.0f, 4.0f, 4.0f),
                PartPose.rotation(INCLINA, 0.0f, 0.0f));

        cabeça.addOrReplaceChild("snout", CubeListBuilder.create()
                        .texOffs(16, 2).addBox(-2.0f, -5.0f, -7.0f, 4.0f, 4.0f, 7.0f),
                PartPose.rotation(INCLINA, 0.0f, 0.0f));
        cabeça.addOrReplaceChild("beard", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-2.0f, -0.2f, -7.0f, 4.0f, 2.0f, 2.0f),
                PartPose.rotation(-0.0113601f, 0.0f, 0.0f));
        cabeça.addOrReplaceChild("earLeft", CubeListBuilder.create()
                        .texOffs(38, 0).addBox(3.5f, 1.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.rotation(-0.5129616f, -Mth.PI / 12.0f, -1.180008f));
        cabeça.addOrReplaceChild("earRight", CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-4.5f, 1.0f, 0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.rotation(-0.3346075f, 0.0371786f, 1.226894f));
        cabeça.addOrReplaceChild("hornLeft", CubeListBuilder.create()
                        .texOffs(43, 0).addBox(-0.5f, -12.0f, -0.5f, 1.0f, 8.0f, 1.0f),
                PartPose.rotation(-0.2268928f, 0.0f, 0.3665191f));
        cabeça.addOrReplaceChild("hornMiddle", CubeListBuilder.create()
                        .texOffs(43, 0).addBox(-0.5f, -10.0f, -0.5f, 1.0f, 6.0f, 1.0f),
                PartPose.rotation(-0.2974289f, 0.0f, 0.0f));
        cabeça.addOrReplaceChild("hornRight", CubeListBuilder.create()
                        .texOffs(43, 0).addBox(-0.5f, -12.0f, -0.5f, 1.0f, 8.0f, 1.0f),
                PartPose.rotation(-0.2268928f, 0.0f, -0.3665191f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.ZERO);
        raiz.addOrReplaceChild("rightarm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("leftarm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 2.0f, 0.0f));

        /*
         * As duas pernas, e a saia presa a cada uma. A saia desenha-se a partir do meio do corpo — é por
         * isso que ela leva o recuo de doze para cima e de dois para o lado: ela está pendurada da perna,
         * mas <b>mora</b> onde o corpo está.
         */
        PartDefinition pernaDireita = raiz.addOrReplaceChild("rightleg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-2.0f, 12.0f, 0.0f));
        pernaDireita.addOrReplaceChild("gownLowerRight", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-5.0f, 12.0f, -2.5f, 5.0f, 11.0f, 5.0f),
                PartPose.offset(2.0f, -12.0f, 0.0f));

        PartDefinition pernaEsquerda = raiz.addOrReplaceChild("leftleg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(2.0f, 12.0f, 0.0f));
        pernaEsquerda.addOrReplaceChild("gownLowerLeft", CubeListBuilder.create()
                        .texOffs(21, 33).addBox(0.0f, 12.0f, -2.5f, 5.0f, 11.0f, 5.0f),
                PartPose.offset(-2.0f, -12.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** O que o desenhista precisa de saber dele: há quantas batidas o braço está erguido. */
    public static class Estado extends LivingEntityRenderState {
        public int braço;
    }

    /** O boneco. */
    public static class Modelo extends EntityModel<Estado> {
        private final ModelPart pescoço;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.pescoço = raiz.getChild("neck");
            this.braçoDireito = raiz.getChild("rightarm");
            this.braçoEsquerdo = raiz.getChild("leftarm");
            this.pernaDireita = raiz.getChild("rightleg");
            this.pernaEsquerda = raiz.getChild("leftleg");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.pescoço.yRot = estado.yRot * Mth.DEG_TO_RAD;
            this.pescoço.xRot = estado.xRot * Mth.DEG_TO_RAD;

            float passo = estado.walkAnimationPos * COMPASSO;
            float quanto = estado.walkAnimationSpeed;
            this.braçoDireito.xRot = Mth.cos(passo + Mth.PI) * BRAÇO_ABRE * quanto * 0.5f;
            this.braçoEsquerdo.xRot = Mth.cos(passo) * BRAÇO_ABRE * quanto * 0.5f;
            this.pernaDireita.xRot = Mth.cos(passo) * PERNA_ABRE * quanto;
            this.pernaEsquerda.xRot = Mth.cos(passo + Mth.PI) * PERNA_ABRE * quanto;
            this.braçoDireito.yRot = 0.0f;
            this.braçoEsquerdo.yRot = 0.0f;
            this.pernaDireita.yRot = 0.0f;
            this.pernaEsquerda.yRot = 0.0f;

            // o balanço de sempre, que o original soma por cima de tudo
            float idade = estado.ageInTicks;
            this.braçoDireito.zRot = Mth.cos(idade * BALANÇO_OMBRO) * BALANÇO + BALANÇO;
            this.braçoEsquerdo.zRot = -(Mth.cos(idade * BALANÇO_OMBRO) * BALANÇO + BALANÇO);
            this.braçoDireito.xRot += Mth.sin(idade * BALANÇO_BRAÇO) * BALANÇO;
            this.braçoEsquerdo.xRot -= Mth.sin(idade * BALANÇO_BRAÇO) * BALANÇO;

            // e a onda do murro, que é a do golem de ferro
            if (estado.braço > 0) {
                this.braçoDireito.xRot = -2.0f + 1.5f
                        * (Math.abs(estado.braço % MURRO - MURRO * 0.5f) - MURRO * 0.25f) / (MURRO * 0.25f);
            }
        }
    }

    /** E o desenhista, que é o de um bicho comum — a barra de chefe é do servidor. */
    public static class Leonard extends MobRenderer<LeonardEntity, Estado, Modelo> {
        public Leonard(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.5f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(LeonardEntity bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.braço = bicho.braço();
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return FOLHA;
        }
    }
}
