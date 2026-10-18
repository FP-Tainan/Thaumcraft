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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.familiar.WingedMonkeyEntity;

/**
 * O <b>Macaco de Asas</b> no mundo: o {@code ModelMonkey} e o {@code RenderWingedMonkey} do Witchery.
 *
 * <p>Catorze caixas: a cabeça com a <b>cara</b>, o <b>nariz</b> e as <b>duas orelhas</b> presas a ela, o
 * tronco em duas peças — o ombro deitado de lado e o corpo inclinado —, dois braços compridos, duas
 * pernas curtas, o <b>rabo</b> e as <b>duas asas chatas</b> de doze blocos de envergadura.
 *
 * <h2>A tinta não troca a pele: ela mistura-se com o castanho</h2>
 *
 * <p>O original <b>não pinta o macaco da cor da tinta</b>. Ele pega na cor da lã, tira-lhe <b>quinze e
 * meio por cento</b> e soma-lhe um castanho fixo — de modo que um macaco pintado de azul é um macaco
 * castanho com um sopro de azul. É pouco, e é de propósito: a folha dele já é castanha.
 *
 * <p>E o <b>branco</b>, que é o índice zero, não leva tinta nenhuma: ele sai como a folha o desenha.
 */
public final class WingedMonkeyRenderer {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("winged_monkey"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/winged_monkey.png");

    /** Quanto da cor da lã passa, e o castanho que leva o resto. */
    public static final float MISTURA = 0.15686274f;
    public static final float CASTANHO_R = 0.41568628f;
    public static final float CASTANHO_G = 0.3137255f;
    public static final float CASTANHO_B = 0.24313726f;

    /** O compasso do bater de asas, e o do balanço de braços deste mod. */
    private static final float ASAS = 0.5f;
    private static final float ABRE = 0.09f;
    private static final float BALANÇO = 0.067f;
    private static final float QUANTO = 0.05f;

    /** As poses que o original repõe à mão a cada quadro. */
    private static final float ASA_X = -0.68294734f;
    private static final float ASA_Y = 0.3642502f;
    private static final float ASA_Z = 0.5462881f;
    private static final float BRAÇO_PARADO = -0.18203785f;
    private static final float CORPO_DE_PÉ = 0.59184116f;
    private static final float RABO_DE_PÉ = -0.63739425f;
    private static final float RABO_SENTADO = -0.9f;

    private WingedMonkeyRenderer() {
    }

    /** O que o desenhista precisa saber dele. */
    public static class Estado extends LivingEntityRenderState {
        public boolean pousado;
        public boolean sentado;
        public int cor;
        public int braço;
    }

    /** A malha, caixa por caixa, na folha de sessenta e quatro por trinta e dois. */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(18, 23).addBox(-0.5f, -7.8f, -0.5f, 1.0f, 8.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 18.5f, 5.3f, RABO_DE_PÉ, 0.0f, 0.0f));

        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-2.0f, -1.1f, -1.0f, 2.0f, 11.0f, 2.0f),
                PartPose.offsetAndRotation(-3.5f, 14.0f, 0.0f, BRAÇO_PARADO, 0.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 19).addBox(-0.5f, -1.0f, -1.0f, 2.0f, 11.0f, 2.0f),
                PartPose.offsetAndRotation(4.0f, 14.0f, 0.0f, BRAÇO_PARADO, 0.0f, 0.0f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(9, 25).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offset(-1.4f, 19.0f, 4.7f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(9, 25).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offset(1.4f, 19.0f, 4.7f));

        raiz.addOrReplaceChild("shoulder", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-3.5f, -3.0f, -3.0f, 7.0f, 5.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, 16.0f, 1.0f, Mth.HALF_PI, 0.0f, 0.0f));
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(36, 12).addBox(-2.5f, -2.0f, -3.0f, 5.0f, 7.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 15.8f, 2.0f, CORPO_DE_PÉ, 0.0f, 0.0f));

        raiz.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(28, 25).addBox(-12.0f, -0.5f, -3.0f, 12.0f, 1.0f, 6.0f),
                PartPose.offsetAndRotation(-1.0f, 14.0f, 2.5f, ASA_X, ASA_Y, ASA_Z));
        raiz.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(28, 25).addBox(0.0f, -0.5f, -3.0f, 12.0f, 1.0f, 6.0f),
                PartPose.offsetAndRotation(1.0f, 14.0f, 2.5f, ASA_X, -ASA_Y, -ASA_Z));

        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -4.0f, -3.0f, 6.0f, 6.0f, 5.0f),
                PartPose.offset(0.0f, 12.0f, -1.5f));
        cabeça.addOrReplaceChild("right_ear", CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-4.5f, -2.5f, -1.5f, 2.0f, 3.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("left_ear", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(18, 14).addBox(2.5f, -2.5f, -1.5f, 2.0f, 3.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("face", CubeListBuilder.create()
                        .texOffs(5, 12).addBox(-2.5f, -3.5f, -3.5f, 5.0f, 5.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("nose", CubeListBuilder.create()
                        .texOffs(9, 19).addBox(-2.0f, -1.5f, -4.5f, 4.0f, 3.0f, 1.0f),
                PartPose.ZERO);

        return LayerDefinition.create(malha, 64, 32);
    }

    /** O boneco, com as três poses do original: no ar, pousado e sentado. */
    public static class Modelo extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart corpo;
        private final ModelPart rabo;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;
        private final ModelPart asaDireita;
        private final ModelPart asaEsquerda;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.corpo = raiz.getChild("body");
            this.rabo = raiz.getChild("tail");
            this.braçoDireito = raiz.getChild("right_arm");
            this.braçoEsquerdo = raiz.getChild("left_arm");
            this.pernaDireita = raiz.getChild("right_leg");
            this.pernaEsquerda = raiz.getChild("left_leg");
            this.asaDireita = raiz.getChild("right_wing");
            this.asaEsquerda = raiz.getChild("left_wing");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            float idade = estado.ageInTicks;
            float abre = Mth.cos(idade * ABRE) * QUANTO + QUANTO;
            float balanço = Mth.sin(idade * BALANÇO) * QUANTO;

            this.braçoDireito.zRot = 0.0f;
            this.braçoEsquerdo.zRot = 0.0f;
            this.pernaDireita.zRot = 0.0f;
            this.pernaEsquerda.zRot = 0.0f;

            if (estado.pousado) {
                this.asaEsquerda.y = 14.0f;
                this.asaDireita.y = 14.0f;
                this.braçoEsquerdo.xRot = BRAÇO_PARADO;
                this.braçoDireito.xRot = BRAÇO_PARADO;
                this.pernaEsquerda.xRot = 0.0f;
                this.pernaDireita.xRot = 0.0f;
            } else {
                // as asas batem, e sobem dois pixels enquanto batem
                this.asaDireita.zRot = Mth.cos(idade * ASAS) * Mth.PI * 0.2f * 2.0f + 0.2f;
                this.asaDireita.xRot = 0.0f;
                this.asaDireita.y = 12.0f;
                this.asaEsquerda.zRot = -this.asaDireita.zRot;
                this.asaEsquerda.xRot = 0.0f;
                this.asaEsquerda.y = 12.0f;

                this.braçoEsquerdo.xRot = 0.2f;
                this.braçoDireito.xRot = 0.2f;
                this.pernaEsquerda.xRot = 0.1f;
                this.pernaDireita.xRot = 0.1f;
                this.braçoDireito.zRot += abre;
                this.braçoEsquerdo.zRot -= abre;
                this.braçoDireito.xRot += balanço;
                this.braçoEsquerdo.xRot -= balanço;
                this.pernaDireita.xRot += balanço;
                this.pernaEsquerda.xRot -= balanço;
            }

            if (estado.sentado) {
                this.pernaDireita.xRot = -1.3f;
                this.pernaDireita.z = 2.0f;
                this.pernaDireita.y = 21.0f;
                this.pernaEsquerda.xRot = -1.3f;
                this.pernaEsquerda.z = 2.0f;
                this.pernaEsquerda.y = 21.0f;
                this.corpo.xRot = 0.1f;
                this.corpo.y = 17.0f;
                this.rabo.z = 4.0f;
                this.rabo.y = 20.0f;
                this.rabo.xRot = RABO_SENTADO;
                this.rabo.yRot = 0.0f;
                this.rabo.zRot = 0.0f;
                this.braçoDireito.zRot += abre;
                this.braçoEsquerdo.zRot -= abre;
                this.braçoDireito.xRot += balanço;
                this.braçoEsquerdo.xRot -= balanço;
                if (!estado.pousado) {
                    this.pernaDireita.xRot = 0.0f;
                    this.pernaEsquerda.xRot = 0.0f;
                }
            }

            // o rabo abana sempre, e abana mais depressa quando ele anda
            if (estado.walkAnimationSpeed > 0.1f) {
                this.rabo.xRot += -estado.walkAnimationSpeed - 0.1f;
                this.rabo.zRot += 5.0f * Mth.cos(idade * ABRE) * QUANTO + QUANTO;
            } else {
                this.rabo.zRot += 3.0f * Mth.cos(idade * ABRE) * QUANTO + QUANTO;
            }

            this.cabeça.yRot = estado.yRot * (Mth.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * (Mth.PI / 180.0f);

            // e o braço direito sobe por dez batidas quando ele dá um murro
            if (estado.braço > 0) {
                float dez = 10.0f;
                this.braçoDireito.xRot = -2.0f + 1.5f
                        * (Math.abs((estado.braço - estado.yRot) % dez - dez * 0.5f) - dez * 0.25f)
                        / (dez * 0.25f);
            }
        }
    }

    /** E o desenhista, que lhe mistura a tinta no castanho. */
    public static class Macaco extends MobRenderer<WingedMonkeyEntity, Estado, EntityModel<Estado>> {
        public Macaco(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.5f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(WingedMonkeyEntity bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.cor = bicho.cor();
            estado.braço = bicho.braço();
            estado.sentado = bicho.isInSittingPose();
            estado.pousado = bicho.getDeltaMovement().equals(net.minecraft.world.phys.Vec3.ZERO)
                    && pousado(bicho);
        }

        /**
         * <b>Pousado</b> é o {@code ModelOwl.isLanded}: o bloco um centésimo abaixo dos pés é
         * <b>folhagem</b> ou é <b>maciço</b>. Com isso, um macaco parado numa árvore fecha as asas.
         */
        private static boolean pousado(WingedMonkeyEntity bicho) {
            BlockPos pé = BlockPos.containing(bicho.getX(), bicho.getY() - 0.01, bicho.getZ());
            var estado = bicho.level().getBlockState(pé);
            return estado.is(net.minecraft.tags.BlockTags.LEAVES)
                    || estado.isSolidRender();
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return FOLHA;
        }

        /**
         * A mistura do original: <b>um sexto da cor da lã</b> somado a um castanho fixo. A branca, que é
         * a do índice zero, não entra — ela sai como a folha a desenha.
         */
        @Override
        protected int getModelTint(Estado estado) {
            if (estado.cor == 0) return super.getModelTint(estado);
            int lã = FleeceColours.of(estado.cor);
            return ARGB.colorFromFloat(1.0f,
                    ARGB.red(lã) / 255.0f * MISTURA + CASTANHO_R,
                    ARGB.green(lã) / 255.0f * MISTURA + CASTANHO_G,
                    ARGB.blue(lã) / 255.0f * MISTURA + CASTANHO_B);
        }
    }
}
