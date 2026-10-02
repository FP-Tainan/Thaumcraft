package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.familiar.OwlEntity;
import net.thaumcraft.occulta.familiar.ToadEntity;

/**
 * Os desenhistas dos dois familiares.
 *
 * <p>Nenhum dos dois tem animação do original traduzida: lá elas eram contas sobre os campos do bicho de 2014,
 * e aqui ficam as do jogo de hoje — a cabeça que acompanha quem olha e o passo. <b>Declarado</b>, porque é
 * menos do que o original fazia: a coruja dele abre as asas quando voa, e esta não.
 */
public final class FamiliarRenderers {
    private static final Identifier PELE_SAPO = Thaumcraft.id("textures/entity/familiar/toad.png");
    private static final Identifier PELE_CORUJA = Thaumcraft.id("textures/entity/familiar/owl.png");

    private FamiliarRenderers() {
    }

    /** O corpo de um familiar: as partes nomeadas do modelo, e a cabeça que olha. */
    public static class Corpo extends EntityModel<LivingEntityRenderState> {
        private final ModelPart cabeça;

        public Corpo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
        }

        @Override
        public void setupAnim(LivingEntityRenderState estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * (Mth.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * (Mth.PI / 180.0f);
        }
    }

    public static class Sapo extends MobRenderer<ToadEntity, LivingEntityRenderState, Corpo> {
        public Sapo(EntityRendererProvider.Context contexto) {
            super(contexto, new Corpo(contexto.bakeLayer(FamiliarModels.TOAD)), 0.4f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState estado) {
            return PELE_SAPO;
        }
    }

    public static class Coruja extends MobRenderer<OwlEntity, LivingEntityRenderState, Corpo> {
        public Coruja(EntityRendererProvider.Context contexto) {
            super(contexto, new Corpo(contexto.bakeLayer(FamiliarModels.OWL)), 0.4f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState estado) {
            return PELE_CORUJA;
        }
    }
}
