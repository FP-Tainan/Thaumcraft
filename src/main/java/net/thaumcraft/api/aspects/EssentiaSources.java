package net.thaumcraft.api.aspects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * De onde a magia bebe essência quando precisa.
 *
 * <p>É o {@code EssentiaHandler} do Thaumcraft 4.2.3.5, reduzido ao que a infusão usa. A matriz não tem
 * cano nenhum ligado a ela: ela simplesmente <em>chama</em> a essência dos jarros que estiverem por
 * perto, e a essência vem pelo ar. O original guarda uma lista dos jarros achados para não vasculhar o
 * mundo toda vez; aqui a vasculhada é feita na hora, porque o alcance é curto e ela só acontece a cada
 * dez tiques.
 */
public final class EssentiaSources {
    private EssentiaSources() {
    }

    /**
     * Tira uma unidade deste aspecto de algum recipiente ao alcance.
     *
     * @param range o alcance em blocos; a infusão do original usa doze
     * @return de onde ela saiu, para desenhar o fio de luz — ou nulo se não havia nenhuma
     */
    @Nullable
    public static BlockPos drain(Level level, BlockPos from, Aspect aspect, int range) {
        if (aspect == null) return null;
        BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -range; y <= range; y++) {
                    scan.set(from.getX() + x, from.getY() + y, from.getZ() + z);
                    if (!level.isLoaded(scan)) continue;
                    if (!(level.getBlockEntity(scan) instanceof AspectContainer source)) continue;
                    if (!source.takeFromContainer(aspect, 1)) continue;
                    return scan.immutable();
                }
            }
        }
        return null;
    }

    /** Há ao menos esta quantidade deste aspecto ao alcance? */
    public static boolean available(Level level, BlockPos from, Aspect aspect, int amount, int range) {
        if (aspect == null) return amount <= 0;
        int found = 0;
        BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -range; y <= range; y++) {
                    scan.set(from.getX() + x, from.getY() + y, from.getZ() + z);
                    if (!level.isLoaded(scan)) continue;
                    if (!(level.getBlockEntity(scan) instanceof AspectContainer source)) continue;
                    found += source.containerContains(aspect);
                    if (found >= amount) return true;
                }
            }
        }
        return false;
    }
}
