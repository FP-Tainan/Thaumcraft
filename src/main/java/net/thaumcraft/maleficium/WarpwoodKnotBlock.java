package net.thaumcraft.maleficium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O nó da madeira distorcida: o {@code BlockWarpwoodLog} de número dois, do Tainted Magic.
 *
 * <p>É um tronco que a árvore cria de vez em quando, duro de quebrar como obsidiana e cheio de sementes do vazio
 * — de uma a cinco. Quem o quebra ouve o estalo do trabalho malfeito e vê fogos-fátuos saindo dele.
 */
public class WarpwoodKnotBlock extends RotatedPillarBlock {
    public static final MapCodec<WarpwoodKnotBlock> CODEC = simpleCodec(WarpwoodKnotBlock::new);

    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void burst(Level level, BlockPos pos);
    }

    public static ClientEffects clientEffects = (level, pos) -> {
    };

    public WarpwoodKnotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WarpwoodKnotBlock> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) {
            clientEffects.burst(level, pos);
        } else {
            level.playSound(null, pos, net.thaumcraft.registry.TCSounds.CRAFT_FAIL.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
