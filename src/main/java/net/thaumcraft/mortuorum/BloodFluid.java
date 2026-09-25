package net.thaumcraft.mortuorum;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * O sangue que corre: o {@code BlockBlood} do Necromancy — um {@code BlockFluidClassic} com a figura da água e a
 * cor {@code 0xD90000}, que pinga de onde está pendurado.
 */
public abstract class BloodFluid extends net.thaumcraft.fluid.ThaumFluid {
    /** O {@code 14221312} que o original devolve na cor do bloco, com o opaco que o jogo de hoje pede. */
    public static final int COLOUR = 0xFFD90000;

    @Override
    protected Fluid source() {
        return MortuorumFluids.BLOOD;
    }

    @Override
    protected Fluid flowing() {
        return MortuorumFluids.BLOOD_FLOWING;
    }

    @Override
    protected Item bucket() {
        return MortuorumItems.BUCKET_BLOOD;
    }

    @Override
    protected Block block() {
        return MortuorumBlocks.BLOOD;
    }

    /** O {@code BlockFluidClassic} anda como a água: quatro de alcance e um nível por bloco. */
    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    /** O pingo de onde está pendurado, que no original é a gota de lava. */
    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        if (random.nextInt(10) != 0) return;
        if (!level.getBlockState(pos.below()).isAir()) return;
        if (!level.getBlockState(pos.below(2)).isAir()) return;
        clientDrip.drip(level, pos.getX() + random.nextFloat(), pos.getY() - 1.05, pos.getZ() + random.nextFloat());
    }

    /** O que o cliente faz com o pingo; no servidor não há nada para fazer. */
    public interface Drip {
        void drip(Level level, double x, double y, double z);
    }

    public static Drip clientDrip = (level, x, y, z) -> {
    };

    public static class Source extends BloodFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends BloodFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
