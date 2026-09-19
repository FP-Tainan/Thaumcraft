package net.thaumcraft.api.aspects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thaumcraft.block.entity.EssentiaMirrorBlockEntity;
import net.thaumcraft.net.TCNetwork;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O {@code EssentiaHandler} da 4.2.3.5: de onde a magia bebe essência quando precisa.
 *
 * <p>A matriz de infusão (e o espelho de essência, do lado do par) não tem cano nenhum: ela chama a essência dos
 * jarros por perto, e a essência vem pelo ar — o fio colorido que o {@link TCNetwork.EssentiaSource} desenha. Como no
 * original, a lista de fontes achadas fica guardada para cada bloco que bebe; quando nenhuma delas dá o que se pediu,
 * a lista se apaga e só se procura de novo cinco segundos depois.
 */
public final class EssentiaSources {
    private record Key(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final Map<Key, List<BlockPos>> SOURCES = new HashMap<>();
    private static final Map<Key, Long> DELAY = new HashMap<>();

    private EssentiaSources() {
    }

    /** O {@code drainEssentia} sem olhar para os espelhos de fora ({@code ignoreMirror} falso). */
    public static boolean drain(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range) {
        return drain(tile, aspect, direction, range, false);
    }

    /**
     * Tira uma unidade deste aspecto de uma fonte ao alcance e manda o fio de essência a quem estiver a até trinta e
     * dois blocos.
     *
     * @param direction nula é o {@code UNKNOWN} do original: a caixa inteira em volta; com direção, só à frente
     */
    public static boolean drain(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, boolean ignoreMirror) {
        if (aspect == null || !(tile.getLevel() instanceof ServerLevel level)) return false;
        Key key = new Key(level.dimension(), tile.getBlockPos());
        if (!SOURCES.containsKey(key)) {
            findSources(level, key, direction, range);
            // o original chama de novo sem o ignoreMirror; fica assim
            return SOURCES.containsKey(key) && drain(tile, aspect, direction, range);
        }
        for (BlockPos source : SOURCES.get(key)) {
            BlockEntity te = level.getBlockEntity(source);
            if (!(te instanceof AspectSource container)) break;
            if (ignoreMirror && te instanceof EssentiaMirrorBlockEntity) continue;
            if (container.takeFromContainer(aspect, 1)) {
                TCNetwork.essentiaSource(level, tile.getBlockPos(), source, aspect.color());
                return true;
            }
        }
        SOURCES.remove(key);
        DELAY.put(key, System.currentTimeMillis() + 5000L);
        return false;
    }

    /** O {@code findEssentia}: há ao menos uma unidade deste aspecto numa fonte ao alcance? */
    public static boolean find(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range) {
        if (aspect == null || !(tile.getLevel() instanceof ServerLevel level)) return false;
        Key key = new Key(level.dimension(), tile.getBlockPos());
        if (!SOURCES.containsKey(key)) {
            findSources(level, key, direction, range);
            return SOURCES.containsKey(key) && find(tile, aspect, direction, range);
        }
        for (BlockPos source : SOURCES.get(key)) {
            if (!(level.getBlockEntity(source) instanceof AspectSource container)) break;
            if (container.doesContainerContainAmount(aspect, 1)) return true;
        }
        SOURCES.remove(key);
        DELAY.put(key, System.currentTimeMillis() + 5000L);
        return false;
    }

    /**
     * O {@code getSources}: sem direção, a caixa de {@code range} para todo lado (em altura, de {@code -range} a
     * {@code range - 1}); com direção, só a metade à frente. O espelho de essência que é o par de quem bebe fica de fora.
     */
    private static void findSources(ServerLevel level, Key key, @Nullable Direction direction, int range) {
        Long delay = DELAY.get(key);
        if (delay != null) {
            if (delay > System.currentTimeMillis()) return;
            DELAY.remove(key);
        }
        BlockEntity sourceTile = level.getBlockEntity(key.pos());
        List<BlockPos> found = new ArrayList<>();
        int start = 0;
        if (direction == null) {
            start = -range;
            direction = Direction.UP;
        }
        BlockPos pos = key.pos();
        for (int aa = -range; aa <= range; aa++) {
            for (int bb = -range; bb <= range; bb++) {
                for (int cc = start; cc < range; cc++) {
                    if (aa == 0 && bb == 0 && cc == 0) continue;
                    int xx = pos.getX(), yy = pos.getY(), zz = pos.getZ();
                    if (direction.getStepY() != 0) {
                        xx += aa;
                        yy += cc * direction.getStepY();
                        zz += bb;
                    } else if (direction.getStepX() == 0) {
                        xx += aa;
                        yy += bb;
                        zz += cc * direction.getStepZ();
                    } else {
                        xx += cc * direction.getStepX();
                        yy += aa;
                        zz += bb;
                    }
                    BlockPos at = new BlockPos(xx, yy, zz);
                    if (!level.isLoaded(at)) continue;
                    BlockEntity te = level.getBlockEntity(at);
                    if (!(te instanceof AspectSource)) continue;
                    if (sourceTile instanceof EssentiaMirrorBlockEntity && te instanceof EssentiaMirrorBlockEntity mirror
                            && mirror.linkPos().equals(pos) && mirror.linkDim == level.dimension()) continue;
                    found.add(at);
                }
            }
        }
        if (!found.isEmpty()) SOURCES.put(key, found);
        else DELAY.put(key, System.currentTimeMillis() + 5000L);
    }
}
