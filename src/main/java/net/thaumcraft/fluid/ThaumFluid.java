package net.thaumcraft.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Os fluidos do Thaumcraft: os {@code BlockFluidClassic}/{@code BlockFluidFinite} do Forge que o original usa, feitos
 * com o fluido que corre do jogo de hoje. Cada um diz de quanto em quanto cai de nível por bloco, até onde procura
 * descida e de quantos em quantos tiques anda; nenhum forma fonte nova.
 */
public abstract class ThaumFluid extends FlowingFluid {
    protected abstract Fluid source();

    protected abstract Fluid flowing();

    protected abstract Item bucket();

    protected abstract Block block();

    @Override
    public Fluid getFlowing() {
        return this.flowing();
    }

    @Override
    public Fluid getSource() {
        return this.source();
    }

    @Override
    public Item getBucket() {
        return this.bucket();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, entity);
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return this.block().defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this.source() || fluid == this.flowing();
    }

    /** O metadado do jogo antigo: zero cheio, crescendo conforme baixa. */
    protected static int meta(FluidState state) {
        return state.isSource() ? 0 : 8 - state.getAmount();
    }

    /** O bloco do fluido: o {@code LiquidBlock} com o construtor aberto. */
    public static class LiquidBlock extends net.minecraft.world.level.block.LiquidBlock {
        public LiquidBlock(FlowingFluid fluid, Properties properties) {
            super(fluid, properties);
        }
    }

    /** As bolhas dos dois, do lado de quem vê. */
    public interface ClientEffects {
        void purifying(Level level, BlockPos pos, int meta, RandomSource random);

        void death(Level level, BlockPos pos, int meta, RandomSource random);
    }

    public static ClientEffects clientEffects = new ClientEffects() {
        @Override
        public void purifying(Level level, BlockPos pos, int meta, RandomSource random) {
        }

        @Override
        public void death(Level level, BlockPos pos, int meta, RandomSource random) {
        }
    };
}
