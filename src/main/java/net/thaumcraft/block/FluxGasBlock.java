package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.registry.TCEffects;

/**
 * O gás de fluxo: o {@code BlockFluxGas} da 4.2.3.5. Sobe e se espalha; quem respira (uma vez em dez, se não é maculado
 * nem morto-vivo e ainda não está exausto nem enjoado) pega exaustão de vis ou náusea, e o gás gasta um quantum.
 */
public class FluxGasBlock extends FluxBlock {
    public static final MapCodec<FluxGasBlock> CODEC = simpleCodec(FluxGasBlock::new);

    public FluxGasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Direction flow() {
        return Direction.UP;
    }

    @Override
    protected int tickRate() {
        return 12;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (level.isClientSide() || level.getRandom().nextInt(10) != 0 || !(entity instanceof LivingEntity living)
                || entity instanceof TaintedMob || living.isInvertedHealAndHarm()
                || living.hasEffect(TCEffects.VIS_EXHAUST) || living.hasEffect(MobEffects.NAUSEA)) {
            return;
        }
        int md = state.getValue(LEVEL);
        if (level.getRandom().nextBoolean()) living.addEffect(new MobEffectInstance(TCEffects.VIS_EXHAUST, 1200, md / 3, true, true));
        else living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80 + md * 20, 0));
        if (md > 0) level.setBlockAndUpdate(pos, state.setValue(LEVEL, md - 1));
        else level.removeBlock(pos, false);
    }
}
