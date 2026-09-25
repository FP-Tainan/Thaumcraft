package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O tecido desfiado: o {@code BlockFabricUnravelled} das Portas Dimensionais.
 *
 * <p>É o que o Limbo é feito, e é dele que o desfiar se espalha: de vez em quando ele pega nos seis vizinhos e
 * desce cada um um degrau. Fora do Limbo, fica quieto.
 */
public class UnravelledFabricBlock extends Block {
    public static final MapCodec<UnravelledFabricBlock> CODEC = simpleCodec(UnravelledFabricBlock::new);

    public UnravelledFabricBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.dimension() != ShatteredRealms.LIMBO) return;
        LimboDecay.spread(level, pos, random);
    }
}
