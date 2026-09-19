package net.thaumcraft.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.FluxBlock;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;
import java.util.function.Predicate;

/**
 * A gosma e o gás de fluxo no mundo: o {@code RenderBlockFluid} do Forge (a gosma) e o {@code BlockGasRenderer} da
 * 4.2.3.5 (o gás). A camada tem a altura dos quanta (7/8 do bloco quando cheia, o bloco inteiro com mais do mesmo
 * fluxo do lado de onde ele vem); o gás sem teto firme em cima é um cubo inteiro, e com teto pende dele como a gosma
 * assenta no chão. Faces coladas no mesmo fluxo ou em bloco firme não se desenham.
 */
public class FluxModel implements BlockStateModel {
    private final BlockStateModel parent;

    public FluxModel(BlockStateModel parent) {
        this.parent = parent;
    }

    public static void init() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE,
                (model, modelContext) -> modelContext.state().getBlock() instanceof FluxBlock ? new FluxModel(model) : model));
    }

    /** O {@code getFluidHeightForRender}. */
    private static float height(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction from) {
        BlockState behind = level.getBlockState(pos.relative(from));
        if (behind.is(state.getBlock()) || !behind.getFluidState().isEmpty()) return 1.0f;
        int md = state.getValue(FluxBlock.LEVEL);
        if (md == 7) return 0.875f;
        return FluxBlock.fullness(state) * 0.875f;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<Direction> cullTest) {
        if (!(state.getBlock() instanceof FluxBlock)) return;
        boolean gas = state.is(TCBlocks.FLUX_GAS);
        // de onde o fluxo "vem": a gosma pesa para baixo e se enche de cima; o gás sobe e se enche de baixo
        Direction from = gas ? Direction.DOWN : Direction.UP;
        Direction base = from.getOpposite();
        float h;
        if (gas && !level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)) {
            h = 1.0f;
        } else {
            h = height(level, pos, state, from);
        }
        Material.Baked sprite = this.parent.particleMaterial();
        for (Direction dir : Direction.values()) {
            BlockPos at = pos.relative(dir);
            BlockState there = level.getBlockState(at);
            if (there.is(state.getBlock())) continue;
            boolean flush = dir == base || h >= 1.0f;
            if (flush && there.isFaceSturdy(level, at, dir.getOpposite())) continue;
            if (dir == base) {
                emitter.square(dir, 0, 0, 1, 1, 0);
            } else if (dir == from) {
                emitter.square(dir, 0, 0, 1, 1, 1.0f - h);
            } else if (base == Direction.DOWN) {
                emitter.square(dir, 0, 0, 1, h, 0);
            } else {
                emitter.square(dir, 0, 1.0f - h, 1, 1, 0);
            }
            emitter.materialBake(sprite, MutableQuadView.BAKE_LOCK_UV);
            emitter.color(-1, -1, -1, -1);
            emitter.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
            emitter.ambientOcclusion(TriState.FALSE);
            emitter.emit();
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.parent.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.parent.materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }
}
