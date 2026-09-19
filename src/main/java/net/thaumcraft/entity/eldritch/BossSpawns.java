package net.thaumcraft.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.thaumcraft.block.entity.eldritch.AncientLockBlockEntity;
import net.thaumcraft.registry.TCEntities;

/**
 * Os chefes que a fechadura antiga chama (o fim do {@code spawnGolemBossRoom} e do {@code spawnWardenBossRoom} da
 * 4.2.3.5): cada um nasce olhando para a fechadura; o guardião-mor, preso ao meio da sala a trinta e dois blocos. E o
 * tentáculo gigante da sala maculada.
 */
public final class BossSpawns {
    private BossSpawns() {
    }

    public static void init() {
        AncientLockBlockEntity.golem = room -> spawn(room, TCEntities.ELDRITCH_GOLEM.create(room.level(), EntitySpawnReason.EVENT),
                room.x() + 0.5, room.z() + 0.5, false);
        AncientLockBlockEntity.warden = room -> spawn(room, TCEntities.ELDRITCH_WARDEN.create(room.level(), EntitySpawnReason.EVENT),
                room.x2() + 0.5, room.z2() + 0.5, true);
        AncientLockBlockEntity.giantTentacle = level -> TCEntities.TAINTACLE_GIANT.create(level, EntitySpawnReason.EVENT);
    }

    private static void spawn(AncientLockBlockEntity.Room room, Mob boss, double x, double z, boolean home) {
        if (boss == null) return;
        BlockPos lock = room.lock();
        double d0 = lock.getX() - x;
        double d1 = lock.getY() - (room.y() + 3 + boss.getEyeHeight());
        double d2 = lock.getZ() - z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float yaw = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(d1, d3) * 180.0 / Math.PI);
        boss.snapTo(x, room.y() + 3, z, yaw, pitch);
        boss.finalizeSpawn(room.level(), room.level().getCurrentDifficultyAt(boss.blockPosition()), EntitySpawnReason.EVENT, null);
        if (home) boss.setHomeTo(new BlockPos(room.x(), room.y() + 2, room.z()), 32);
        room.level().addFreshEntity(boss);
    }
}
