package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.broom.BroomEntity;

/**
 * A vassoura, desenhada: o {@code RenderBroom} do Witchery.
 *
 * <p>Duas coisas dele que importam e que uma prova de servidor não vê:
 *
 * <ol>
 *   <li><b>O cabo e as cerdas desenham-se à parte</b>, porque só as cerdas levam a tinta. O original muda a cor
 *       do OpenGL entre uma e outra; aqui a cor vai no próprio desenho, que é como o jogo de hoje faz o
 *       mesmo.</li>
 *   <li><b>A tabela de cores é a dele</b>, e não a do jogo. São as dezesseis do {@code fleeceColorTable} — as
 *       mesmas da lã de 2014 —, e <b>sem tinta a vassoura é castanha</b>, que é a cor 12.</li>
 * </ol>
 */
public class BroomRenderer extends EntityRenderer<BroomEntity, BroomRenderer.Estado> {
    private static final Identifier PELE = Thaumcraft.id("textures/entity/broom.png");

    /**
     * As dezesseis cores do {@code ModelBroom.fleeceColorTable}, que moram no {@link FleeceColours} desde
     * que o espantalho passou a pintar-se com as mesmas.
     */
    private static final int[] CORES = FleeceColours.ALL;

    /** A cor que ela tem quando ninguém lhe passou tinta: a 12, que é o castanho da madeira. */
    public static final int SEM_TINTA = 12;

    private final ModelPart cabo;
    private final ModelPart cerdas;

    public BroomRenderer(EntityRendererProvider.Context contexto) {
        super(contexto);
        ModelPart raiz = contexto.bakeLayer(BroomModel.BROOM);
        this.cabo = raiz.getChild("handle");
        this.cerdas = raiz.getChild("bristles");
        this.shadowRadius = 0.5f;
    }

    /** O que o desenho precisa saber dela: para onde aponta, e de que cor são as cerdas. */
    public static class Estado extends EntityRenderState {
        public float rumo;
        public int cor = SEM_TINTA;
        public float desdeAPancada;
        public float apanhado;
    }

    @Override
    public Estado createRenderState() {
        return new Estado();
    }

    @Override
    public void extractRenderState(BroomEntity vassoura, Estado estado, float parcial) {
        super.extractRenderState(vassoura, estado, parcial);
        estado.rumo = Mth.rotLerp(parcial, vassoura.yRotO, vassoura.getYRot());
        int qual = vassoura.cor();
        estado.cor = qual < 0 || qual > 15 ? SEM_TINTA : qual;
        estado.desdeAPancada = vassoura.desdeAPancada() - parcial;
        estado.apanhado = Math.max(0.0f, vassoura.apanhado() - parcial);
    }

    @Override
    public void submit(Estado estado, PoseStack pilha, SubmitNodeCollector coletor, CameraRenderState câmara) {
        pilha.pushPose();
        // a ordem é a do original, e a altura também: um bloco para cima ANTES de virar, senão a
        // vassoura fica enterrada — que foi exatamente o que a primeira foto desta fatia mostrou
        pilha.translate(0.0f, 1.0f, 0.0f);
        pilha.mulPose(Axis.YP.rotationDegrees(-estado.rumo + 90.0f));
        // e a sacudidela de quem levou pancada, que o original faz com um seno
        if (estado.desdeAPancada > 0.0f) {
            pilha.mulPose(Axis.XP.rotationDegrees(
                    Mth.sin(estado.desdeAPancada) * estado.desdeAPancada * estado.apanhado / 10.0f));
        }
        // e só então o de cabeça para baixo, que é o glScalef(-1, -1, 1) dele
        pilha.mulPose(Axis.ZP.rotationDegrees(180.0f));

        RenderType tipo = RenderTypes.entityCutout(PELE);
        coletor.submitModelPart(this.cabo, pilha, tipo, estado.lightCoords, OverlayTexture.NO_OVERLAY, null);
        coletor.submitModelPart(this.cerdas, pilha, tipo, estado.lightCoords, OverlayTexture.NO_OVERLAY,
                null, CORES[estado.cor], null);
        pilha.popPose();

        super.submit(estado, pilha, coletor, câmara);
    }
}
