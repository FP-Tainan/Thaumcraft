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
import net.thaumcraft.occulta.goblin.GoblinEntity;

/**
 * O desenhista do goblin: o {@code ModelGoblin} e o {@code RenderGoblin} do Witchery.
 *
 * <p>Ele é um <b>bípede baixo</b> — pernas de seis em vez de doze, e por isso a cabeça fica à altura do peito
 * de um aldeão — com <b>nariz de três caixas</b> e <b>orelhas de duas</b> de cada lado. É o nariz que o faz
 * goblin: três degraus a sair da cara, cada um mais à frente que o anterior.
 *
 * <p>São <b>quatro peles</b>, uma por ofício, sorteadas ao nascer.
 *
 * <p><b>E o braço dele sacode quando trabalha.</b> O original soma ao giro do braço direito um resto do
 * relógio — um décimo por batida, numa volta de vinte —, e o que se vê é um goblin a martelar. É a única coisa
 * que diz, de longe, que ele está a cavar e não só parado ao pé de uma pedra.
 */
public final class GoblinRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("goblin"), "main");

    /** As quatro peles, pela ordem dos ofícios do original. */
    private static final Identifier[] PELES = {
            Thaumcraft.id("textures/entity/goblin2.png"),
            Thaumcraft.id("textures/entity/goblin.png"),
            Thaumcraft.id("textures/entity/goblin4.png"),
            Thaumcraft.id("textures/entity/goblin3.png"),
    };

    /** A volta do martelo, e o quanto ele sobe por batida. */
    private static final int MARTELO = 20;
    private static final float POR_BATIDA = 0.1f;

    private GoblinRenderer() {
    }

    /** As caixas do {@code ModelGoblin}, número por número, na folha de sessenta e quatro por trinta e dois. */
    public static LayerDefinition createLayer() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f)
                        // o nariz, em três degraus
                        .texOffs(34, 3).addBox(-0.5f, -6.0f, -5.0f, 1.0f, 3.0f, 1.0f)
                        .texOffs(34, 0).addBox(-0.5f, -5.0f, -6.0f, 1.0f, 1.0f, 1.0f)
                        .texOffs(33, 9).addBox(-0.5f, -4.0f, -7.0f, 1.0f, 2.0f, 2.0f)
                        // e as orelhas, duas caixas de cada lado
                        .texOffs(46, 0).addBox(6.0f, -7.0f, 0.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(39, 0).addBox(4.0f, -7.0f, 0.0f, 2.0f, 3.0f, 1.0f)
                        .texOffs(39, 0).addBox(-6.0f, -7.0f, 0.0f, 2.0f, 3.0f, 1.0f)
                        .texOffs(46, 0).addBox(-8.0f, -7.0f, 0.0f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 11.0f, 0.0f));

        raiz.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 7.0f, 4.0f),
                PartPose.offset(0.0f, 11.0f, 0.0f));
        raiz.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -3.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -3.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.offset(-2.0f, 18.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.offset(2.0f, 18.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 32);
    }

    /** O que o desenhista precisa de saber dele: a pele, e se ele está a martelar. */
    public static class Estado extends LivingEntityRenderState {
        public int pele;
        public boolean trabalhando;
        public int batidas;
    }

    /** O bípede baixo, com o braço que sacode. */
    public static class Modelo extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.braçoDireito = raiz.getChild("right_arm");
            this.braçoEsquerdo = raiz.getChild("left_arm");
            this.pernaDireita = raiz.getChild("right_leg");
            this.pernaEsquerda = raiz.getChild("left_leg");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

            float passo = estado.walkAnimationPos;
            float quanto = estado.walkAnimationSpeed;
            this.braçoDireito.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 2.0f * quanto * 0.5f;
            this.braçoEsquerdo.xRot = Mth.cos(passo * 0.6662f) * 2.0f * quanto * 0.5f;
            this.pernaDireita.xRot = Mth.cos(passo * 0.6662f) * 1.4f * quanto;
            this.pernaEsquerda.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 1.4f * quanto;

            // e o martelo, que é o resto do relógio somado ao braço direito
            if (estado.trabalhando) {
                this.braçoDireito.xRot -= (estado.batidas % MARTELO) * POR_BATIDA;
            }
        }
    }

    /** E o desenhista, que escolhe a pele pelo ofício. */
    public static class Goblin extends MobRenderer<GoblinEntity, Estado, Modelo> {
        public Goblin(EntityRendererProvider.Context context) {
            super(context, new Modelo(context.bakeLayer(LAYER)), 0.4f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(GoblinEntity goblin, Estado estado, float parcial) {
            super.extractRenderState(goblin, estado, parcial);
            estado.pele = goblin.ofício();
            estado.trabalhando = goblin.trabalhando();
            estado.batidas = goblin.tickCount;
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return PELES[Math.clamp(estado.pele, 0, PELES.length - 1)];
        }
    }
}
