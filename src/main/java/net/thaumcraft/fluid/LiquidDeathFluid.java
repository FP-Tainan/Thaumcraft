package net.thaumcraft.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCDamageTypes;
import net.thaumcraft.registry.TCFluids;
import net.thaumcraft.registry.TCItems;

/**
 * A morte líquida: o {@code BlockFluidDeath} da 4.2.3.5 (um {@code BlockFluidFinite} de quatro níveis, luz 8). Dissolve
 * o que vive dentro dela — um de dano por nível, até quatro no cheio — e borbulha roxo. O fluido finito do Forge não
 * existe hoje: aqui ele corre como os outros, perdendo dois níveis por bloco (quatro de alcance), e não forma fonte.
 */
public abstract class LiquidDeathFluid extends ThaumFluid {
    @Override
    protected Fluid source() {
        return TCFluids.DEATH;
    }

    @Override
    protected Fluid flowing() {
        return TCFluids.DEATH_FLOWING;
    }

    @Override
    protected Item bucket() {
        return TCItems.BUCKET_DEATH;
    }

    @Override
    protected Block block() {
        return TCBlocks.LIQUID_DEATH;
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 2;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 2;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 7;
    }

    /** O nível do fluido finito: de 0 (raso) a 3 (cheio). */
    public static int quanta(FluidState state) {
        return Math.max(0, (state.getAmount() + 1) / 2 - 1);
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity) {
            entity.hurtServer(server, TCDamageTypes.dissolve(level), quanta(level.getFluidState(pos)) + 1);
        }
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        clientEffects.death(level, pos, quanta(state), random);
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 1.0, pos.getZ() + random.nextFloat(), SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS, 0.1f + random.nextFloat() * 0.1f, 0.9f + random.nextFloat() * 0.15f, false);
        }
    }

    public static class Source extends LiquidDeathFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends LiquidDeathFluid {
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
