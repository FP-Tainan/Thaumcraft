package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * As folhas que um gerador de árvore mágica pôs, para acertar a distância de cada uma até o tronco.
 *
 * <p>No jogo antigo a folha media essa distância na hora de apodrecer; no de hoje ela a guarda no próprio
 * bloco, e uma folha posta sem essa conta acharia que está solta e cairia. Então, ao fim de cada árvore,
 * a distância é medida a partir das toras, pelo caminho das folhas, como o próprio jogo faz com as dele. A
 * folha que ficar longe demais fica para sempre, como ficaria no original.
 */
final class TreeLeaves {
    private final Set<BlockPos> placed = new LinkedHashSet<>();

    void place(LevelAccessor level, BlockPos pos, BlockState state, int flags) {
        level.setBlock(pos, state, flags);
        this.placed.add(pos.immutable());
    }

    void forget(BlockPos pos) {
        this.placed.remove(pos);
    }

    void settle(LevelAccessor level, int flags) {
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pos : this.placed) {
            if (!(level.getBlockState(pos).getBlock() instanceof LeavesBlock)) continue;
            for (Direction dir : Direction.values()) {
                if (level.getBlockState(pos.relative(dir)).is(BlockTags.LOGS)) {
                    distance.put(pos, 1);
                    queue.add(pos);
                    break;
                }
            }
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int next = distance.get(pos) + 1;
            if (next >= LeavesBlock.DECAY_DISTANCE) continue;
            for (Direction dir : Direction.values()) {
                BlockPos side = pos.relative(dir);
                if (!this.placed.contains(side) || distance.containsKey(side)) continue;
                distance.put(side, next);
                queue.add(side);
            }
        }
        for (BlockPos pos : this.placed) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LeavesBlock)) continue;
            int d = distance.getOrDefault(pos, LeavesBlock.DECAY_DISTANCE);
            BlockState settled = state.setValue(LeavesBlock.DISTANCE, d);
            // longe demais do tronco para as contas de hoje: no original a folha nascida da árvore só apodrecia
            // quando algo mudava a quatro blocos dela, e estas ficariam; aqui ficam também
            if (d >= LeavesBlock.DECAY_DISTANCE) settled = settled.setValue(LeavesBlock.PERSISTENT, true);
            if (settled != state) level.setBlock(pos, settled, flags);
        }
    }

    /** O chão em que uma muda pega: terra, grama e o resto que a muda de carvalho de hoje aceita. */
    static boolean isSoil(BlockState state) {
        return state.is(BlockTags.SUPPORTS_VEGETATION);
    }
}
