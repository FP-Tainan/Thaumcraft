package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * A pedra sinistra: o {@code ItemCompassStone} da 4.2.3.5. Os nós sombrios se anunciam a quem vê a cada dois segundos e
 * meio ({@code sinisterNodes}); a pedra acende quando um deles está à frente de quem a segura, a até 256 blocos, e apaga
 * dez segundos depois do último aviso.
 */
public class SinisterStoneItem extends Item {
    private record Node(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final Map<Node, Long> NODES = new HashMap<>();

    public SinisterStoneItem(Properties properties) {
        super(properties);
    }

    /** Um nó sombrio se anuncia (do lado de quem vê). */
    public static void mark(Level level, BlockPos pos) {
        NODES.put(new Node(level.dimension(), pos.immutable()), System.currentTimeMillis());
    }

    /** A pedra acende? Algum nó sombrio no cone de visão de quem a segura. */
    public static boolean active(Level level, Entity holder) {
        long now = System.currentTimeMillis();
        NODES.values().removeIf(t -> t < now - 10000L);
        for (Node node : NODES.keySet()) {
            if (node.dimension() != level.dimension()) continue;
            if (visible(holder, Vec3.atCenterOf(node.pos()))) return true;
        }
        return false;
    }

    /** O {@code isVisibleTo(0.66, entidade, ponto, 256)}. */
    private static boolean visible(Entity ent, Vec3 x) {
        Vec3 t = new Vec3(ent.getX(), ent.getBoundingBox().minY + ent.getEyeHeight(), ent.getZ());
        Vec3 b = ent.getLookAngle().scale(256.0).add(t);
        Vec3 apexToX = t.subtract(x), axis = t.subtract(b);
        double dot = apexToX.dot(axis);
        if (dot / apexToX.length() / axis.length() <= Math.cos(0.66f / 2.0f)) return false;
        return dot / axis.length() < axis.length();
    }
}
