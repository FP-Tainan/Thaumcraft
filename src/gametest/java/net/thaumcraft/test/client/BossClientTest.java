package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.registry.TCEntities;

import java.util.concurrent.atomic.AtomicReference;

/** Os chefes das Terras de Fora: o construto (com e sem cabeça), o guardião-mor e o tentáculo gigante. */
public class BossClientTest implements FabricClientGameTest {
    private static <T extends Mob> T place(T mob, double x, double y, double z) {
        mob.setPos(x, y, z);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setYRot(180);
        mob.setYHeadRot(180);
        mob.yBodyRot = 180;
        mob.level().addFreshEntity(mob);
        return mob;
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("gamerule doMobSpawning false");
            server.runCommand("difficulty normal");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -12; x <= 12; x++) for (int z = -2; z <= 16; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState());
                    for (int y = 0; y < 10; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                for (int x = -12; x <= 12; x += 4) level.setBlockAndUpdate(p.offset(x, 0, 6), Blocks.GLOWSTONE.defaultBlockState());
                place(TCEntities.ELDRITCH_GOLEM.create(level, EntitySpawnReason.STRUCTURE), p.getX() + 6.5, p.getY(), p.getZ() + 10.5);
                var headless = TCEntities.ELDRITCH_GOLEM.create(level, EntitySpawnReason.STRUCTURE);
                place(headless, p.getX() + 1.5, p.getY(), p.getZ() + 10.5).setHeadless(true);
                place(TCEntities.ELDRITCH_WARDEN.create(level, EntitySpawnReason.STRUCTURE), p.getX() - 3.5, p.getY(), p.getZ() + 10.5);
                place(TCEntities.TAINTACLE_GIANT.create(level, EntitySpawnReason.STRUCTURE), p.getX() - 8.5, p.getY(), p.getZ() + 10.5);
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 2) + " " + (p.getZ() - 4.5) + " 0 5");
            context.waitTicks(100);
            context.takeScreenshot("chefes");
        }
    }
}
