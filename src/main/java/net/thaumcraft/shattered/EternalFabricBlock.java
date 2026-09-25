package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O tecido eterno: o {@code BlockFabricEternal} das Portas Dimensionais — o chão do Limbo.
 *
 * <p>Não se quebra e não se atravessa. Quem o pisa é devolvido ao mundo de onde veio, que é a única saída que o
 * Limbo tem: o {@code EscapeTarget} do original.
 */
public class EternalFabricBlock extends Block {
    public static final MapCodec<EternalFabricBlock> CODEC = simpleCodec(EternalFabricBlock::new);

    public EternalFabricBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean past) {
        if (!(level instanceof ServerLevel server)) return;
        ShatteredRealms.escape(server, entity);
    }
}
