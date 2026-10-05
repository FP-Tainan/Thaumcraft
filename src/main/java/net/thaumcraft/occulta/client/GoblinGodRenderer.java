package net.thaumcraft.occulta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.goblin.GulgEntity;
import net.thaumcraft.occulta.goblin.MogEntity;

/**
 * Os desenhistas dos dois <b>deuses goblins</b>: os {@code ModelGoblinMog} e {@code ModelGoblinGulg} do
 * Witchery.
 *
 * <p>Os dois são bípedes com a <b>cara de goblin</b> — presas, nariz e beiço a sair dela —, e a diferença
 * entre eles é a forma do corpo, que diz o que cada um faz:
 *
 * <ul>
 *   <li>o <b>Mog</b> tem o corpo de sempre, uma <b>peitoral inclinada a quarenta e cinco graus</b> e uma
 *       <b>saia</b> de nove por onze. Um arqueiro de capa;</li>
 *   <li>o <b>Gulg</b> tem um tronco curto e um <b>peito de dez por oito por seis</b> por cima dele, com
 *       braços de <b>dezesseis</b> em vez de catorze. Um barril com punhos.</li>
 * </ul>
 *
 * <p>Vê-se qual é qual de longe, e é só isso que o desenho precisa de fazer.
 */
public final class GoblinGodRenderer {
    public static final ModelLayerLocation MOG = new ModelLayerLocation(Thaumcraft.id("mog"), "main");
    public static final ModelLayerLocation GULG = new ModelLayerLocation(Thaumcraft.id("gulg"), "main");

    public static final Identifier FOLHA_MOG = Thaumcraft.id("textures/entity/mog.png");
    public static final Identifier FOLHA_GULG = Thaumcraft.id("textures/entity/gulg.png");

    private GoblinGodRenderer() {
    }

    /** A cara de goblin, que os dois partilham: presas, nariz e beiço. */
    private static CubeListBuilder cara() {
        return CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f)
                .texOffs(0, 4).addBox(-2.0f, -4.0f, -5.0f, 1.0f, 2.0f, 1.0f)
                .texOffs(0, 4).addBox(1.0f, -4.0f, -5.0f, 1.0f, 2.0f, 1.0f)
                .texOffs(25, 0).addBox(-1.0f, -6.0f, -6.0f, 2.0f, 3.0f, 2.0f)
                .texOffs(34, 0).addBox(-2.0f, -2.0f, -6.0f, 4.0f, 1.0f, 2.0f);
    }

    /** O Mog: o corpo de sempre, com peitoral inclinada e saia. */
    public static LayerDefinition mog() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head", cara(), PartPose.ZERO);
        /*
         * O boneco do jogo exige uma peça chamada <b>hat</b> — o chapéu, que é a segunda camada da
         * cabeça —, e nenhum dos dois a tem: o original desenha-lhes a cara e mais nada. Ela entra vazia,
         * e nos <b>dois lugares</b> em que o jogo já a procurou ao longo das versões.
         */
        raiz.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        cabeça.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        raiz.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.ZERO);
        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 14)
                .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 14.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 14)
                .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 14.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(2.0f, 12.0f, 0.0f));

        /*
         * A peitoral e a saia são <b>peças soltas</b>, e não filhas do corpo: no original elas são
         * desenhadas à parte e não seguem o giro dele quando ele soca. É assim que ficam.
         */
        raiz.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(35, 5)
                        .addBox(-4.0f, -2.0f, -5.0f, 8.0f, 4.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 2.0f, 0.0f, (float) (Math.PI / 4), 0.0f, 0.0f));
        raiz.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(14, 34)
                        .addBox(-4.5f, 0.0f, -2.5f, 9.0f, 11.0f, 5.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** E o Gulg: tronco curto, peito enorme e braços de dezesseis. */
    public static LayerDefinition gulg() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head", cara(), PartPose.ZERO);
        /*
         * O boneco do jogo exige uma peça chamada <b>hat</b> — o chapéu, que é a segunda camada da
         * cabeça —, e nenhum dos dois a tem: o original desenha-lhes a cara e mais nada. Ela entra vazia,
         * e nos <b>dois lugares</b> em que o jogo já a procurou ao longo das versões.
         */
        raiz.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        cabeça.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        raiz.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 4.0f, 4.0f), PartPose.offset(0.0f, 8.0f, 0.0f));
        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 14)
                .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 16.0f, 4.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 14)
                .addBox(0.0f, -2.0f, -2.0f, 4.0f, 16.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(12, 35)
                .addBox(-5.0f, 0.0f, -3.0f, 10.0f, 8.0f, 6.0f), PartPose.ZERO);

        return LayerDefinition.create(malha, 64, 64);
    }

    /** O desenhista do Mog. */
    public static class Mog extends HumanoidMobRenderer<MogEntity, HumanoidRenderState,
            HumanoidModel<HumanoidRenderState>> {
        public Mog(EntityRendererProvider.Context contexto) {
            super(contexto, new HumanoidModel<>(contexto.bakeLayer(MOG)), 0.5f);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState estado) {
            return FOLHA_MOG;
        }
    }

    /** E o do Gulg. */
    public static class Gulg extends HumanoidMobRenderer<GulgEntity, HumanoidRenderState,
            HumanoidModel<HumanoidRenderState>> {
        public Gulg(EntityRendererProvider.Context contexto) {
            super(contexto, new HumanoidModel<>(contexto.bakeLayer(GULG)), 0.5f);
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState estado) {
            return FOLHA_GULG;
        }
    }

    /** Sem uso fora do porte: as peças soltas dos dois, para quem precisar de as nomear. */
    public static final String[] SOLTAS = {"chest", "skirt"};

    /** E a raiz delas, que o {@link HumanoidModel} não conhece mas desenha na mesma. */
    public static ModelPart solta(ModelPart raiz, String qual) {
        return raiz.getChild(qual);
    }
}
