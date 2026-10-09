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
import net.thaumcraft.occulta.torment.LordOfTormentEntity;

/**
 * O <b>Senhor do Tormento</b> no mundo: o {@code ModelLordOfTorment} e o {@code RenderLordOfTorment}.
 *
 * <p>Ele é o Demônio levado ao fim: o mesmo corpo de oito por catorze, as mesmas pernas, os mesmos braços
 * de vinte — mas <b>quatro braços em vez de dois</b>, <b>dois chifres</b> a sair de dentro da cabeça e uma
 * <b>barba de seis pontas</b> de comprimentos desiguais, que é o que faz a cara dele ser uma cara e não um
 * cubo.
 *
 * <p>E as asas: duas chapas de <b>vinte por quarenta</b>, uma por lado, que são o dobro das de qualquer
 * outro bicho deste mod. Paradas, elas abrem e fecham devagar; <b>andando</b>, ficam abertas num ângulo
 * fixo — o original pergunta se a velocidade em x <b>ou</b> em z é maior que zero, o que quer dizer que ele
 * as abre quando anda para o norte ou para o leste e não quando anda para o sul ou para o oeste. Fica como
 * está: é um engano velho e é dele.
 *
 * <p>Os <b>quatro braços</b> balançam em contratempo dois a dois, e quando ele bate os de cima sobem e
 * abrem num só gesto. É a única coisa do mod com quatro braços depois do Poltergeist.
 */
public final class LordOfTormentRenderer {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("lord_of_torment"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/lord_of_torment.png");

    /** A sombra dele. */
    public static final float SOMBRA = 0.5f;

    /** O balanço dos quatro braços. */
    private static final float ABRE = 0.09f;
    private static final float QUANTO = 0.05f;
    private static final float BALANÇO = 0.067f;

    /** O ângulo fixo em que as asas ficam quando ele anda. */
    private static final float ASA_ABERTA = 0.4f;

    /** E o quanto elas abrem e fecham paradas. */
    private static final float ASA_RESPIRA = 0.3f;

    /** O quanto o braço se levanta ao bater, e em quantas batidas. */
    private static final float GOLPE = 15.0f;

    private LordOfTormentRenderer() {
    }

    /** O que o desenhista precisa de saber dele. */
    public static class Estado extends LivingEntityRenderState {
        public int braço;
        public boolean andando;
    }

    /** A malha, caixa por caixa, na folha de cento e vinte e oito por cento e vinte e oito. */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f)
                        .texOffs(34, 0).addBox(-3.0f, 0.0f, -4.0f, 1.0f, 7.0f, 1.0f)
                        .texOffs(34, 0).addBox(-2.0f, 0.0f, -5.0f, 1.0f, 5.0f, 1.0f)
                        .texOffs(34, 0).addBox(-1.0f, 0.0f, -4.0f, 1.0f, 9.0f, 1.0f)
                        .texOffs(34, 0).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 1.0f)
                        .texOffs(34, 0).addBox(1.0f, 0.0f, -4.0f, 1.0f, 4.0f, 1.0f)
                        .texOffs(34, 0).addBox(2.0f, 0.0f, -5.0f, 1.0f, 8.0f, 1.0f)
                        .texOffs(40, 0).addBox(-3.0f, -4.0f, -5.0f, 6.0f, 4.0f, 1.0f)
                        .texOffs(40, 6).addBox(-2.0f, -6.0f, -5.0f, 4.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, -6.0f, 0.0f));

        /*
         * Os dois chifres nascem <b>dentro</b> da cabeça e saem por cima dela: a caixa deles começa em
         * menos quinze e a peça está em zero. O original os põe duas vezes — em y menos seis e depois em
         * zero — e vale a segunda, como sempre.
         */
        cabeça.addOrReplaceChild("horn_right", CubeListBuilder.create()
                        .texOffs(55, 0).addBox(-2.0f, -15.0f, 0.0f, 1.0f, 9.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.5948578f, 0.0f, -0.1858931f));
        cabeça.addOrReplaceChild("horn_left", CubeListBuilder.create()
                        .texOffs(55, 0).addBox(1.0f, -15.0f, 0.0f, 1.0f, 9.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.5948578f, 0.0f, 0.1858931f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0f, -6.0f, -2.0f, 8.0f, 14.0f, 4.0f),
                PartPose.ZERO);

        raiz.addOrReplaceChild("right_arm_1", braço(-3.0f), PartPose.offset(-5.0f, -4.0f, 0.0f));
        raiz.addOrReplaceChild("right_arm_2", braço(-3.0f), PartPose.offset(-5.0f, -4.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm_1", braço(-1.0f), PartPose.offset(5.0f, -4.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm_2", braço(-1.0f), PartPose.offset(5.0f, -4.0f, 0.0f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 15.0f, 4.0f),
                PartPose.offset(-2.0f, 8.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 15.0f, 4.0f),
                PartPose.offset(2.0f, 8.0f, 0.0f));

        raiz.addOrReplaceChild("wings_left", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-20.0f, -20.0f, 0.0f, 20.0f, 40.0f, 0.0f),
                PartPose.offset(0.0f, 1.0f, 5.0f));
        raiz.addOrReplaceChild("wings_right", CubeListBuilder.create()
                        .texOffs(0, 82).addBox(0.0f, -20.0f, 0.0f, 20.0f, 40.0f, 0.0f),
                PartPose.offset(0.0f, 1.0f, 5.0f));

        return LayerDefinition.create(malha, 128, 128);
    }

    /** Os quatro braços são a mesma caixa; o que muda é de que lado ela começa. */
    private static CubeListBuilder braço(float x) {
        return CubeListBuilder.create().texOffs(40, 16).addBox(x, -2.0f, -2.0f, 4.0f, 20.0f, 4.0f);
    }

    /** O boneco. */
    public static class Modelo extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart braçoDireito1;
        private final ModelPart braçoDireito2;
        private final ModelPart braçoEsquerdo1;
        private final ModelPart braçoEsquerdo2;
        private final ModelPart asaEsquerda;
        private final ModelPart asaDireita;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.braçoDireito1 = raiz.getChild("right_arm_1");
            this.braçoDireito2 = raiz.getChild("right_arm_2");
            this.braçoEsquerdo1 = raiz.getChild("left_arm_1");
            this.braçoEsquerdo2 = raiz.getChild("left_arm_2");
            this.asaEsquerda = raiz.getChild("wings_left");
            this.asaDireita = raiz.getChild("wings_right");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

            /*
             * O balanço dos braços do original é constante: ele multiplica o cosseno de um número fixo —
             * 3,8077927 e 0,6662 — pela velocidade de andar. Quer dizer que os braços não balançam ao
             * passo: eles ficam parados num ângulo que só depende de <b>quão depressa</b> ele anda. Fica
             * como está.
             */
            float quanto = estado.walkAnimationSpeed;
            this.braçoDireito1.xRot = Mth.cos(3.8077927f) * 2.0f * quanto * 0.5f;
            this.braçoDireito2.xRot = Mth.cos(3.8077927f) * 2.0f * quanto * 0.25f;
            this.braçoEsquerdo1.xRot = Mth.cos(0.6662f) * 2.0f * quanto * 0.5f;
            this.braçoEsquerdo2.xRot = Mth.cos(0.6662f) * 2.0f * quanto * 0.25f;

            if (estado.andando) {
                this.asaEsquerda.yRot = ASA_ABERTA;
                this.asaDireita.yRot = -ASA_ABERTA;
            } else {
                float respira = Mth.cos(estado.ageInTicks * ABRE) * ASA_RESPIRA;
                float base = Mth.cos(3.8077927f) * 2.0f * quanto * 0.5f;
                this.asaEsquerda.yRot = base + respira;
                this.asaDireita.yRot = base - respira;
            }

            float abre = Mth.cos(estado.ageInTicks * ABRE) * QUANTO + QUANTO;
            float balanço = Mth.sin(estado.ageInTicks * BALANÇO) * QUANTO;
            this.braçoDireito1.yRot = 0.0f;
            this.braçoDireito2.yRot = 0.0f;
            this.braçoEsquerdo1.yRot = 0.0f;
            this.braçoEsquerdo2.yRot = 0.0f;
            this.braçoDireito1.zRot = abre;
            this.braçoDireito2.zRot = -abre;
            this.braçoEsquerdo1.zRot = -abre;
            this.braçoEsquerdo2.zRot = abre;
            this.braçoDireito1.xRot += balanço;
            this.braçoDireito2.xRot -= balanço;
            this.braçoEsquerdo1.xRot -= balanço;
            this.braçoEsquerdo2.xRot += balanço;

            /* E o golpe, que levanta os de cima e abre os quatro. */
            if (estado.braço <= 0) return;
            float quão = dente(estado.braço, GOLPE);
            this.braçoDireito1.xRot = -1.5f + 0.8f * quão;
            this.braçoEsquerdo1.xRot = -1.5f + 0.8f * quão;
            this.braçoDireito1.zRot = -(-1.5f + 1.5f * quão);
            this.braçoEsquerdo1.zRot = -1.5f + 1.5f * quão;
            this.braçoDireito2.zRot = -(-1.0f + 1.5f * quão);
            this.braçoEsquerdo2.zRot = -1.0f + 1.5f * quão;
        }

        /** A onda de dente de serra do original, de menos um a um. */
        private static float dente(float quanto, float volta) {
            return (Math.abs(quanto % volta - volta * 0.5f) - volta * 0.25f) / (volta * 0.25f);
        }
    }

    /** E o desenhista. */
    public static class Senhor extends MobRenderer<LordOfTormentEntity, Estado, EntityModel<Estado>> {
        public Senhor(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), SOMBRA);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(LordOfTormentEntity bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.braço = bicho.oBraço();
            // o original pergunta se a velocidade em x <b>ou</b> em z é maior que zero, e não o módulo
            estado.andando = bicho.getDeltaMovement().x > 0.0 || bicho.getDeltaMovement().z > 0.0;
        }

        /**
         * <b>Ele se vê no escuro.</b> O {@code getBrightness} do original devolve <b>um</b> — quer dizer
         * que o Senhor do Tormento não é iluminado pelo lugar onde está: ele se desenha sempre com a luz
         * cheia. Num labirinto sem luz nenhuma, é a diferença entre um vulto e um bicho.
         */
        @Override
        protected int getBlockLightLevel(LordOfTormentEntity bicho, net.minecraft.core.BlockPos onde) {
            return 15;
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return FOLHA;
        }
    }
}
