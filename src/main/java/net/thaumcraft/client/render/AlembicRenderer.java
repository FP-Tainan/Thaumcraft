package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.AlembicBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O que os Óculos da Revelação mostram sobre o alambique.
 *
 * <p>O alambique é de metal fechado — não tem vidro por onde se ver o que ele recolheu. No original a
 * única maneira de saber é pôr os óculos, e então o símbolo do aspecto aparece pairando sobre ele. É
 * exatamente isso que este desenhista faz, e nada mais: sem óculos, o alambique é um pote e ponto.
 */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
    /** O que o desenhista precisa saber do alambique neste quadro. */
    public static class State extends BlockEntityRenderState {
        @Nullable
        public Aspect aspect;
        public int amount;
        public boolean labelled;
    }

    private final Font font;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
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
        state.labelled = EssentiaLabel.visible(alembic);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.labelled || state.aspect == null || state.amount <= 0) return;
        EssentiaLabel.submit(pose, collector, camera, this.font,
                state.aspect, state.amount, 1.2f, state.lightCoords);
    }
}
