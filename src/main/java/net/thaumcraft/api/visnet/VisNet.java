package net.thaumcraft.api.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.thaumcraft.api.aspects.Aspect;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A rede de vis: o {@code VisNetHandler} da 4.2.3.5.
 *
 * <p>Guarda as fontes de cada mundo, pendura os pontos novos no mais perto à vista (e da mesma cor) e atende quem pede
 * vis: para cada fonte, o ponto dela mais perto de quem pede, desde que quem pede esteja no alcance daquele ponto.
 */
public final class VisNet {
    private static final Map<ResourceKey<Level>, Map<BlockPos, WeakReference<VisNodeBlockEntity>>> SOURCES = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, List<WeakReference<VisNodeBlockEntity>>>> NEARBY = new HashMap<>();

    private VisNet() {
    }

    @Nullable
    public static VisNodeBlockEntity valid(@Nullable WeakReference<VisNodeBlockEntity> ref) {
        VisNodeBlockEntity node = ref == null ? null : ref.get();
        return node != null && !node.isRemoved() ? node : null;
    }

    private static Map<BlockPos, WeakReference<VisNodeBlockEntity>> sources(Level level) {
        return SOURCES.computeIfAbsent(level.dimension(), key -> new HashMap<>());
    }

    public static void addSource(Level level, VisNodeBlockEntity source) {
        sources(level).put(source.getBlockPos(), new WeakReference<>(source));
        NEARBY.clear();
    }

    public static void removeSource(Level level, VisNodeBlockEntity source) {
        Map<BlockPos, WeakReference<VisNodeBlockEntity>> map = SOURCES.get(level.dimension());
        if (map != null) map.remove(source.getBlockPos());
        NEARBY.clear();
    }

    /**
     * O {@code drainVis}: quem está em {@code pos} pede {@code amount} (em centésimos) de um aspecto. Cada fonte manda
     * pelo ponto dela mais perto, até completar.
     */
    public static int drainVis(Level level, BlockPos pos, Aspect aspect, int amount) {
        int drained = 0;
        List<WeakReference<VisNodeBlockEntity>> nodes = NEARBY.computeIfAbsent(level.dimension(), key -> new HashMap<>())
                .computeIfAbsent(pos.immutable(), key -> calculateNearbyNodes(level, key));
        for (WeakReference<VisNodeBlockEntity> ref : nodes) {
            VisNodeBlockEntity node = ref.get();
            if (node == null) continue;
            int got = node.consumeVis(aspect, amount);
            drained += got;
            amount -= got;
            if (amount <= 0) break;
        }
        return drained;
    }

    /** O {@code addNode}: o ponto mais perto (fonte ou já pendurado) que alcança este, à vista e da mesma cor. */
    @Nullable
    public static VisNodeBlockEntity addNode(Level level, VisNodeBlockEntity node) {
        Map<BlockPos, WeakReference<VisNodeBlockEntity>> map = SOURCES.get(level.dimension());
        if (map == null) return null;
        List<Object[]> nearby = new ArrayList<>();
        for (WeakReference<VisNodeBlockEntity> root : map.values()) {
            VisNodeBlockEntity source = valid(root);
            if (source == null) continue;
            float r = inRange(node.getBlockPos(), source.getBlockPos(), node.getRange());
            if (r > 0.0f) nearby.add(new Object[]{source, r - node.getRange() * 2});
            findClosestNodes(node, source, nearby, new HashSet<>());
        }
        float best = Float.MAX_VALUE;
        VisNodeBlockEntity closest = null;
        for (Object[] o : nearby) {
            VisNodeBlockEntity candidate = (VisNodeBlockEntity) o[0];
            float d = (Float) o[1];
            if (d < best && (node.getAttunement() == -1 || candidate.getAttunement() == -1 || node.getAttunement() == candidate.getAttunement())
                    && canNodeBeSeen(node, candidate)) {
                best = d;
                closest = candidate;
            }
        }
        if (closest == null) return null;
        closest.children().add(new WeakReference<>(node));
        NEARBY.clear();
        return closest;
    }

    private static void findClosestNodes(VisNodeBlockEntity target, VisNodeBlockEntity parent, List<Object[]> in, Set<BlockPos> seen) {
        if (seen.size() > 512 || !seen.add(parent.getBlockPos())) return;
        for (WeakReference<VisNodeBlockEntity> ref : new ArrayList<>(parent.children())) {
            VisNodeBlockEntity child = ref.get();
            if (child == null || child == target || child == parent) continue;
            float r = inRange(child.getBlockPos(), target.getBlockPos(), target.getRange());
            if (r > 0.0f) in.add(new Object[]{child, r});
            findClosestNodes(target, child, in, seen);
        }
    }

    /** A distância ao quadrado, ou -1 se passa do alcance. */
    private static float inRange(BlockPos a, BlockPos b, int range) {
        float distance = (float) a.distSqr(b);
        return distance > range * range ? -1.0f : distance;
    }

    private static List<WeakReference<VisNodeBlockEntity>> calculateNearbyNodes(Level level, BlockPos drainer) {
        List<WeakReference<VisNodeBlockEntity>> out = new ArrayList<>();
        Map<BlockPos, WeakReference<VisNodeBlockEntity>> map = SOURCES.get(level.dimension());
        if (map == null) return out;
        for (WeakReference<VisNodeBlockEntity> root : map.values()) {
            VisNodeBlockEntity source = valid(root);
            if (source == null) continue;
            VisNodeBlockEntity closest = null;
            float range = Float.MAX_VALUE;
            float r = inRange(drainer, source.getBlockPos(), source.getRange());
            if (r > 0.0f) {
                range = r;
                closest = source;
            }
            for (WeakReference<VisNodeBlockEntity> ref : allChildren(level, source, new ArrayList<>())) {
                VisNodeBlockEntity n = ref.get();
                if (n == null || n == source) continue;
                float r2 = inRange(n.getBlockPos(), drainer, n.getRange());
                if (r2 > 0.0f && r2 < range) {
                    range = r2;
                    closest = n;
                }
            }
            if (closest != null) out.add(new WeakReference<>(closest));
        }
        return out;
    }

    private static List<WeakReference<VisNodeBlockEntity>> allChildren(Level level, VisNodeBlockEntity source, List<WeakReference<VisNodeBlockEntity>> list) {
        for (WeakReference<VisNodeBlockEntity> ref : new ArrayList<>(source.children())) {
            VisNodeBlockEntity n = ref.get();
            if (n != null && n.getLevel() != null && level.isLoaded(n.getBlockPos())) {
                list.add(ref);
                allChildren(level, n, list);
            }
        }
        return list;
    }

    /** O {@code canNodeBeSeen}: nada no caminho entre os dois centros, a não ser o próprio alvo. */
    public static boolean canNodeBeSeen(VisNodeBlockEntity source, VisNodeBlockEntity target) {
        Level level = source.getLevel();
        if (level == null) return false;
        Vec3 from = Vec3.atCenterOf(source.getBlockPos());
        Vec3 to = Vec3.atCenterOf(target.getBlockPos());
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (hit.getType() == HitResult.Type.MISS) return true;
        BlockPos at = hit.getBlockPos();
        return at.equals(target.getBlockPos()) || at.equals(source.getBlockPos());
    }
}
