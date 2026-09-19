package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.TCBiomes;

/**
 * A gosma de fluxo: o {@code BlockFluxGoo} da 4.2.3.5. Escorre para baixo, prende quem anda nela (quanto mais cheia,
 * mais) e dá exaustão de vis; parada, às vezes vira um slime taumático, às vezes (cheia, com ar em cima) macula o bioma
 * e vira fibra, e às vezes evapora um quantum — que pode subir como gás.
 */
public class FluxGooBlock extends FluxBlock {
    public static final MapCodec<FluxGooBlock> CODEC = simpleCodec(FluxGooBlock::new);

    public FluxGooBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Direction flow() {
        return Direction.DOWN;
    }

    @Override
    protected int tickRate() {
        return 30;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** O {@code onEntityCollidedWithBlock}: o slime cresce comendo a gosma; o resto fica preso e exausto. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        int md = state.getValue(LEVEL);
        if (slimeGrowth.test(entity, md)) {
            if (level.getRandom().nextBoolean() && !level.isClientSide()) {
                if (md > 1) level.setBlockAndUpdate(pos, state.setValue(LEVEL, md - 1));
                else level.removeBlock(pos, false);
            }
            return;
        }
        float slow = 1.0f - fullness(state);
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(slow, 1.0, slow));
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(TCEffects.VIS_EXHAUST, 600, md / 3, true, true));
        }
    }

    /** O slime taumático crescendo na gosma (as criaturas da mácula ligam isto): devolve se comeu. */
    public static java.util.function.BiPredicate<Entity, Integer> slimeGrowth = (entity, md) -> false;

    /** O slime taumático que nasce da gosma, do tamanho dado. */
    public static java.util.function.BiConsumer<ServerLevel, BlockPos> spawnSlimeSmall = (level, pos) -> {
    };
    public static java.util.function.BiConsumer<ServerLevel, BlockPos> spawnSlimeBig = (level, pos) -> {
    };

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        int meta = state.getValue(LEVEL);
        boolean airAbove = level.isEmptyBlock(pos.above());
        if (meta >= 2 && meta < 6 && airAbove && rand.nextInt(25) == 0) {
            level.removeBlock(pos, false);
            spawnSlimeSmall.accept(level, pos);
            level.playSound(null, pos, TCSounds.GORE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (meta >= 6 && airAbove) {
            if (rand.nextInt(25) == 0) {
                level.removeBlock(pos, false);
                spawnSlimeBig.accept(level, pos);
                level.playSound(null, pos, TCSounds.GORE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else if (rand.nextInt(50) == 0) {
                // o biome_taint_from_flux do original, ligado por padrão
                BiomePainter.paint(level, pos, TCBiomes.TAINTED_LAND);
                level.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRES.defaultBlockState());
                level.blockEvent(pos, TCBlocks.TAINT_FIBRES, 1, 0);
            }
        } else if (rand.nextInt(30) == 0) {
            if (meta == 0) {
                level.removeBlock(pos, false);
            } else {
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, meta - 1));
                if (rand.nextBoolean() && airAbove) level.setBlockAndUpdate(pos.above(), TCBlocks.FLUX_GAS.defaultBlockState().setValue(LEVEL, 0));
            }
        }
    }

    /** O {@code randomDisplayTick}: bolhas subindo, tanto mais quanto mais cheia. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        int meta = state.getValue(LEVEL);
        if (rand.nextInt(30) <= meta) clientEffects.accept(level, pos, meta);
    }

    public interface ClientEffects {
        void accept(Level level, BlockPos pos, int meta);
    }

    public static ClientEffects clientEffects = (level, pos, meta) -> {
    };
}
