package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.spirit.SpiritEntity;

/**
 * O <b>Espírito</b> no mundo: o {@code ModelSpirit} e o {@code RenderSpirit} do Witchery.
 *
 * <p>Ele não tem cabeça, nem braços, nem pernas. São <b>cinco caixas empilhadas</b> numa peça só — uma
 * lanterna de papel, larga no meio e estreita nas pontas — e nada nelas se mexe: o boneco inteiro é
 * estático, e o que lhe dá vida é a <b>deriva</b> do bicho e o <b>pó</b> que ele larga a cada batida.
 *
 * <p>Sai a <b>seis décimos de transparência</b> e tingido pela cor que lhe deram, que por omissão é o
 * branco — o dourado do original está no pó, não no boneco.
 */
public final class SpiritRenderer {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("spirit"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/spirit.png");

    /** A transparência do original. */
    public static final int COR = 0x99FFFFFF;

    /** E a escala e a descida que o boneco leva antes de se desenhar. */
    public static final float ESCALA = 0.5f;
    public static final float DESCE = 0.65f;

    /** A descida, em unidades de modelo, que é como a peça a leva. */
    public static final float DESCE_EM_UNIDADES = DESCE * 16.0f;

    private SpiritRenderer() {
    }

    /**
     * A malha das cinco caixas, numa peça só em (0, 21, 0) — e essa peça dentro de outra, que é onde
     * moram a <b>metade</b> e a <b>descida de dois terços de bloco</b> que o original faz à mão na pilha
     * de matrizes antes de desenhar.
     *
     * <p>A conta é esta: a peça de fora desce {@code 0,65 × 16 = 10,4} unidades <b>sem escala</b> e
     * encolhe tudo o que está dentro dela à metade, que é exatamente o que as duas linhas de OpenGL do
     * original fazem, por essa ordem.
     */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        var fora = malha.getRoot().addOrReplaceChild("spirit", CubeListBuilder.create(),
                PartPose.offset(0.0f, DESCE_EM_UNIDADES, 0.0f).withScale(ESCALA));
        fora.addOrReplaceChild("piece", CubeListBuilder.create()
                        .texOffs(2, 5).addBox(-2.5f, -2.0f, -2.5f, 5.0f, 1.0f, 5.0f)
                        .texOffs(2, 21).addBox(-2.5f, 1.0f, -2.5f, 5.0f, 1.0f, 5.0f)
                        .texOffs(0, 12).addBox(-3.0f, -1.0f, -3.0f, 6.0f, 2.0f, 6.0f)
                        .texOffs(6, 0).addBox(-1.5f, -3.0f, -1.5f, 3.0f, 1.0f, 3.0f)
                        .texOffs(6, 28).addBox(-1.5f, 2.0f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(0.0f, 21.0f, 0.0f));
        return LayerDefinition.create(malha, 32, 32);
    }

    /** O que o desenhista precisa de saber dele: a cor, e mais nada. */
    public static class Estado extends LivingEntityRenderState {
        public int cor;
    }

    /**
     * O boneco, que <b>não se mexe</b>.
     *
     * <p>Nenhuma das cinco caixas gira, sobe ou desce com coisa nenhuma — não há cabeça para virar nem
     * perna para andar. O que lhe dá vida é a deriva do bicho e o pó que ele larga.
     */
    public static class Modelo extends EntityModel<Estado> {
        public Modelo(ModelPart raiz) {
            super(raiz);
        }
    }

    /** E o desenhista, translúcido e tingido. */
    public static class Espírito extends MobRenderer<SpiritEntity, Estado, EntityModel<Estado>> {
        public Espírito(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.0f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(SpiritEntity bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.cor = bicho.cor();
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return FOLHA;
        }

        @Override
        protected int getModelTint(Estado estado) {
            return estado.cor == 0 ? COR : (COR & 0xFF000000) | (estado.cor & 0xFFFFFF);
        }

        @Override
        protected @org.jetbrains.annotations.Nullable RenderType getRenderType(Estado estado,
                                                                               boolean corpo,
                                                                               boolean translúcido,
                                                                               boolean brilha) {
            return RenderTypes.entityTranslucent(FOLHA);
        }
    }
}
