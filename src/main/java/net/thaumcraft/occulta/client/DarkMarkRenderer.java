package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.thaumcraft.occulta.symbol.DarkMarkEntity;

/**
 * A <b>Marca Negra</b> no mundo: o {@code ModelDarkMark} e o {@code RenderDarkMark} do Witchery.
 *
 * <p>Duas caixas e mais nada: a <b>caveira</b>, de dezesseis por doze por dezesseis, e a <b>mandíbula</b>
 * por baixo dela, de dez por quatro por doze. Tudo isso <b>dobrado de tamanho</b> ao desenhar e erguido uma
 * casa, que é o que lhe dá os quatro blocos de altura com que ela se vê de longe.
 *
 * <p>A caveira <b>acompanha quem olha para ela</b> — vira a cara para o jogador mais perto, a dezesseis
 * casas. É o que faz dela uma coisa que olha de volta.
 *
 * <p>O giro vai na peça e não na pilha porque é a cabeça inteira que gira, e o modelo é instanciado por
 * desenhista; mas a pose é reposta a <b>cada quadro</b> a partir do estado, que é o que impede uma marca de
 * herdar o ângulo da outra.
 */
public final class DarkMarkRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("dark_mark"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/dark_mark.png");

    /** O quanto o original a amplia ao desenhar, e o quanto a ergue depois. */
    public static final float DOBRA = 2.0f;
    public static final float ERGUE = -1.0f;

    private DarkMarkRenderer() {
    }

    /** As duas caixas, na folha de sessenta e quatro por sessenta e quatro. */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();
        raiz.addOrReplaceChild("skull", CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 0).addBox(-8.0f, -12.0f, -8.0f, 16.0f, 12.0f, 16.0f)
                        .texOffs(0, 29).addBox(-5.0f, 0.0f, -8.0f, 10.0f, 4.0f, 12.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));
        return LayerDefinition.create(malha, 64, 64);
    }

    public static class Modelo extends EntityModel<LivingEntityRenderState> {
        private final ModelPart caveira;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.caveira = raiz.getChild("skull");
        }

        @Override
        public void setupAnim(LivingEntityRenderState estado) {
            super.setupAnim(estado);
            this.caveira.yRot = estado.yRot * Mth.DEG_TO_RAD;
            this.caveira.xRot = estado.xRot * Mth.DEG_TO_RAD;
        }
    }

    public static class Marca extends MobRenderer<DarkMarkEntity, LivingEntityRenderState,
            EntityModel<LivingEntityRenderState>> {
        public Marca(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState estado) {
            return FOLHA;
        }

        /**
         * O {@code glScalef} e o {@code glTranslatef} do original, nesta ordem.
         *
         * <p>Aqui a pilha já está virada de cabeça para baixo — é por isso que erguê-la é um número
         * <b>negativo</b>, e é por isso que a ordem importa: a mudança de lugar acontece no tamanho novo.
         */
        @Override
        protected void scale(LivingEntityRenderState estado, PoseStack pilha) {
            pilha.scale(DOBRA, DOBRA, DOBRA);
            pilha.translate(0.0f, ERGUE, 0.0f);
        }
    }
}
