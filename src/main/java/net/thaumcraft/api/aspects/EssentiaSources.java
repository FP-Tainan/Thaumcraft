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

    /**
     * O {@code getSources} com direção, que o espelho de essência usa do lado do par: a caixa de
     * {@code (2 * range + 1)²} blocos de largura e {@code range} de fundo à frente do vidro (a direção é para onde ele
     * olha). Os outros espelhos de essência ficam de fora, como no {@code ignoreMirror} do original — senão um par
     * beberia do outro sem fim.
     *
     * @return de onde a unidade saiu, ou nulo
     */
    @Nullable
    public static BlockPos drainFacing(Level level, BlockPos from, Aspect aspect, net.minecraft.core.Direction direction, int range) {
        if (aspect == null) return null;
        BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
        for (int aa = -range; aa <= range; aa++) {
            for (int bb = -range; bb <= range; bb++) {
                for (int cc = 0; cc < range; cc++) {
                    if (aa == 0 && bb == 0 && cc == 0) continue;
                    int xx = from.getX(), yy = from.getY(), zz = from.getZ();
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
                    scan.set(xx, yy, zz);
                    if (!level.isLoaded(scan)) continue;
                    var te = level.getBlockEntity(scan);
                    if (!(te instanceof AspectContainer source) || te instanceof net.thaumcraft.block.entity.EssentiaMirrorBlockEntity) continue;
                    if (source.takeFromContainer(aspect, 1)) return scan.immutable();
                }
            }
        }
        return null;
    }

    /** O fio de luz da essência indo de {@code from} até {@code to}, na cor do aspecto. */
    public static void thread(Level level, BlockPos to, BlockPos from, int colour) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        net.minecraft.world.phys.Vec3 here = net.minecraft.world.phys.Vec3.atCenterOf(to);
        net.minecraft.world.phys.Vec3 there = net.minecraft.world.phys.Vec3.atCenterOf(from);
        int steps = (int) Math.max(4, here.distanceTo(there) * 3);
        for (int step = 0; step <= steps; step++) {
            net.minecraft.world.phys.Vec3 at = there.lerp(here, step / (double) steps);
            server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(0xFF000000 | colour, 0.8f),
                    at.x, at.y, at.z, 1, 0.04, 0.04, 0.04, 0.0);
        }
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
