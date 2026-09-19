package net.thaumcraft.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCFluids;
import net.thaumcraft.registry.TCItems;

import java.util.function.ToIntFunction;

/**
 * O fluido purificante: o {@code BlockFluidPure} da 4.2.3.5 (um {@code BlockFluidClassic} de oito níveis, luz 10, que
 * anda a cada cinco tiques). Um jogador sem a proteção contra a dobra que entra numa fonte dele ganha a proteção — tanto
 * menos tempo quanto mais dobra permanente tem — e a fonte some. Borbulha branco e estala de vez em quando.
 */
public abstract class PurifyingFluid extends ThaumFluid {
    /** A dobra permanente do jogador (a fatia da dobra liga isto ao conhecimento dele). */
    public static ToIntFunction<Player> permanentWarp = player -> 0;

    @Override
    protected Fluid source() {
        return TCFluids.PURIFYING;
    }

    @Override
    protected Fluid flowing() {
        return TCFluids.PURIFYING_FLOWING;
    }

    @Override
    protected Item bucket() {
        return TCItems.BUCKET_PURE;
    }

    @Override
    protected Block block() {
        return TCBlocks.PURIFYING_FLUID;
    }

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

    /** O {@code onEntityCollidedWithBlock}: a proteção contra a dobra, e a fonte vai embora. */
    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (level.isClientSide() || !(entity instanceof Player player) || !level.getFluidState(pos).isSource()) return;
        if (player.hasEffect(TCEffects.WARP_WARD)) return;
        int warp = permanentWarp.applyAsInt(player);
        int div = 1;
        if (warp > 0) div = Math.max(1, (int) Math.sqrt(warp));
        player.addEffect(new MobEffectInstance(TCEffects.WARP_WARD, Math.min(32000, 200000 / div), 0, true, true));
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        clientEffects.purifying(level, pos, meta(state), random);
        if (random.nextInt(25) == 0) {
            level.playLocalSound(pos.getX() + random.nextFloat(), pos.getY() + 1.0, pos.getZ() + random.nextFloat(), SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS, 0.1f + random.nextFloat() * 0.1f, 0.9f + random.nextFloat() * 0.15f, false);
        }
    }

    public static class Source extends PurifyingFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends PurifyingFluid {
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
