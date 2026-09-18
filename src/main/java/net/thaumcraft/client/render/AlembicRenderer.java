package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.AlchemicalFurnaceBlock;
import net.thaumcraft.block.AlembicBlock;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import net.thaumcraft.client.render.model.AlembicModel;
import org.jetbrains.annotations.Nullable;

/**
 * O alambique arcano, com a malha do modelo original.
 *
 * <p>Eu passei tres tentativas empilhando caixinhas atras da silhueta deste troco, e as tres sairam
 * erradas -- a ultima o dono do mod chamou, com razao, de casa de abelhas. O erro nao era de medida: era
 * de ferramenta. Um arquivo de modelo do Minecraft so sabe caixa alinhada ao eixo, e o alambique nao e
 * feito de caixas; ele foi modelado num programa de modelagem e veio no mod como {@code alembic.obj}.
 * Com renderizador proprio da para desenhar triangulo solto, e entao da para usar a geometria do autor
 * em vez de imita-la. E o que o {@link AlembicModel} guarda.
 *
 * <p>As pecas entram conforme o que esta embaixo, exatamente como no {@code TileAlembicRenderer}
 * original: sobre o forno ele ganha o bico e os pes; sobre outro alambique, o bico e o encaixe que desce
 * no de baixo; solto no chao, so os pes. O corpo e o painel vao sempre.
 */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/alembic.png");

    /** O que o desenhista precisa saber do alambique neste quadro. */
    public static class State extends BlockEntityRenderState {
        @Nullable
        public Aspect aspect;
        public int amount;
        public boolean aboveFurnace;
        public boolean aboveAlembic;
        public float turn;
    }


    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AlembicBlockEntity alembic, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(alembic, state, crumbling);
        state.aspect = alembic.aspect();
        state.amount = alembic.amount();

        state.turn = turnFor(alembic.getBlockState());

        Level level = alembic.getLevel();
        var below = level == null ? null : level.getBlockState(alembic.getBlockPos().below()).getBlock();
        state.aboveFurnace = below instanceof AlchemicalFurnaceBlock;
        state.aboveAlembic = below instanceof AlembicBlock;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;

        pose.pushPose();
        // o original poe a origem do modelo no meio do bloco, mas rente ao chao dele
        pose.translate(0.5f, 0.0f, 0.5f);
        if (state.turn != 0.0f) pose.mulPose(Axis.YP.rotationDegrees(state.turn));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (matrix, consumer) -> {
            if (state.aboveFurnace) {
                draw(AlembicModel.TUBEMAIN, matrix, consumer, light);
                draw(AlembicModel.LEGS, matrix, consumer, light);
            } else if (state.aboveAlembic) {
                draw(AlembicModel.TUBEMAIN, matrix, consumer, light);
                draw(AlembicModel.TUBESMALL, matrix, consumer, light);
            } else {
                draw(AlembicModel.LEGS, matrix, consumer, light);
            }
            draw(AlembicModel.POT, matrix, consumer, light);
            draw(AlembicModel.PANEL, matrix, consumer, light);
        });
        pose.popPose();

    }

    /**
     * Quanto girar para o painel apontar para onde o bloco esta virado.
     *
     * <p>No modelo do autor o painel nasce virado para o oeste; esta conta leva o oeste ate a face
     * pedida.
     */
    public static float turnFor(net.minecraft.world.level.block.state.BlockState state) {
        if (!state.hasProperty(AlembicBlock.FACING)) return 0.0f;
        return 90.0f - state.getValue(AlembicBlock.FACING).toYRot();
    }

    private static void draw(float[] mesh, PoseStack.Pose matrix, VertexConsumer consumer, int light) {
        ObjMesh.draw(mesh, matrix, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
