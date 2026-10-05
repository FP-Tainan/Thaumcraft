package net.thaumcraft.occulta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.hunter.WitchHunterEntity;

/**
 * O desenhista do Caçador de Bruxas: o {@code RenderWitchHunter} e o {@code ModelWitchHunter} do Witchery.
 *
 * <p>Ele é um <b>bípede com três caixas a mais</b>: a <b>aba do chapéu</b>, de catorze por um por catorze; o
 * <b>topo</b>, de seis por dois por seis; e a <b>saia do casaco</b>, de dez por onze por cinco, presa ao
 * tronco e descendo até o joelho.
 *
 * <p><b>As roupas dele estão pintadas na pele</b>, e não vestidas. São <b>três peles</b>, sorteadas ao
 * nascer, e é a única coisa que distingue um caçador de outro — porque o que eles fazem é todos o mesmo.
 *
 * <p>Repare que a aba dele é <b>maior</b> que a das roupas que se podem vestir: catorze contra treze. Um
 * caçador de verdade tem o chapéu que ninguém mais tem.
 */
public final class WitchHunterRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("witch_hunter"), "main");

    /** As três peles do original. */
    private static final Identifier[] PELES = {
            Thaumcraft.id("textures/entity/witch_hunter1.png"),
            Thaumcraft.id("textures/entity/witch_hunter2.png"),
            Thaumcraft.id("textures/entity/witch_hunter3.png"),
    };

    private WitchHunterRenderer() {
    }

    /** As três caixas a mais, nos números do original, na folha de sessenta e quatro por sessenta e quatro. */
    public static LayerDefinition createLayer() {
        MeshDefinition malha = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.getChild("head");
        cabeça.addOrReplaceChild("hat_brim",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-7.0f, 0.0f, -7.0f, 14.0f, 1.0f, 14.0f),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        cabeça.addOrReplaceChild("hat_top",
                CubeListBuilder.create().texOffs(33, 48)
                        .addBox(-3.0f, 0.0f, -3.0f, 6.0f, 2.0f, 6.0f),
                PartPose.offset(0.0f, -10.0f, 0.0f));

        raiz.getChild("body").addOrReplaceChild("coat_bottom",
                CubeListBuilder.create().texOffs(1, 47)
                        .addBox(-5.0f, 0.0f, -2.5f, 10.0f, 11.0f, 5.0f),
                PartPose.offset(0.0f, 11.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** O que o desenhista precisa de saber dele: só qual das três peles. */
    public static class Estado extends HumanoidRenderState {
        public int pele;
    }

    /** O bípede do caçador, com o chapéu de dois andares e a saia do casaco. */
    public static class Modelo extends HumanoidModel<Estado> {
        public Modelo(ModelPart raiz) {
            super(raiz);
        }
    }

    /** E o desenhista, que escolhe a pele. */
    public static class Caçador extends HumanoidMobRenderer<WitchHunterEntity, Estado, Modelo> {
        public Caçador(EntityRendererProvider.Context context) {
            super(context, new Modelo(context.bakeLayer(LAYER)), 0.5f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(WitchHunterEntity caçador, Estado estado, float parcial) {
            super.extractRenderState(caçador, estado, parcial);
            estado.pele = caçador.pele();
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return PELES[Math.clamp(estado.pele, 0, PELES.length - 1)];
        }
    }
}
