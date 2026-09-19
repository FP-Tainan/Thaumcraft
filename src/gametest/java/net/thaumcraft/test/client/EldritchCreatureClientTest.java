package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.entity.eldritch.CrabSpawnerBlockEntity;
import net.thaumcraft.entity.eldritch.EldritchCrabEntity;
import net.thaumcraft.entity.eldritch.EldritchOrbEntity;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;

import java.util.concurrent.atomic.AtomicReference;

/** Os de dentro das Terras de Fora: os caranguejos, o zumbi habitado, o guardião e o orbe, e a abertura incrustada. */
public class EldritchCreatureClientTest implements FabricClientGameTest {
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
            server.runCommand("time set noon");
            server.runCommand("gamerule doMobSpawning false");
            server.runCommand("difficulty normal");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -8; x <= 8; x++) for (int z = -2; z <= 14; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState());
                    for (int y = 0; y < 8; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                // o muro de pedra incrustada com as aberturas viradas para quem vê
                for (int x = -3; x <= 3; x++) for (int y = 0; y < 4; y++) {
                    level.setBlockAndUpdate(p.offset(x, y, 10), TCBlocks.CRUSTED_STONE.defaultBlockState());
                }
                for (int x : new int[]{-2, 0, 2}) {
                    BlockPos vent = p.offset(x, 1 + (x == 0 ? 1 : 0), 10);
                    level.setBlockAndUpdate(vent, TCBlocks.CRUSTED_OPENING.defaultBlockState());
                    ((CrabSpawnerBlockEntity) level.getBlockEntity(vent)).setFacing(Direction.NORTH);
                }
                EldritchCrabEntity helmed = TCEntities.ELDRITCH_CRAB.create(level, EntitySpawnReason.STRUCTURE);
                place(helmed, p.getX() - 1.5, p.getY(), p.getZ() + 4.5).setHelm(true);
                EldritchCrabEntity bare = TCEntities.ELDRITCH_CRAB.create(level, EntitySpawnReason.STRUCTURE);
                place(bare, p.getX() + 1.5, p.getY(), p.getZ() + 4.5).setHelm(false);
                var zombie = TCEntities.INHABITED_ZOMBIE.create(level, EntitySpawnReason.STRUCTURE);
                zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.STRUCTURE, null);
                place(zombie, p.getX() - 4.5, p.getY(), p.getZ() + 6.5);
                var guardian = TCEntities.ELDRITCH_GUARDIAN.create(level, EntitySpawnReason.STRUCTURE);
                place(guardian, p.getX() + 4.5, p.getY(), p.getZ() + 6.5);
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 1) + " " + (p.getZ() - 0.5) + " 0 15");
            context.waitTicks(60);
            context.takeScreenshot("eldritch-criaturas");
            server.runOnServer(s -> {
                var level = s.overworld();
                var guardian = level.getEntitiesOfClass(net.thaumcraft.entity.eldritch.EldritchGuardianEntity.class,
                        new net.minecraft.world.phys.AABB(p).inflate(10)).getFirst();
                var orb = new EldritchOrbEntity(level, guardian);
                orb.setPos(p.getX() + 1.5, p.getY() + 1.6, p.getZ() + 3.0);
                orb.setDeltaMovement(0, 0, 0);
                level.addFreshEntity(orb);
                TCNetwork.sonic(level, guardian);
            });
            server.runCommand("tp @p " + (p.getX() + 2.5) + " " + (p.getY() + 1) + " " + (p.getZ() + 1.0) + " 20 10");
            context.waitTicks(4);
            context.takeScreenshot("eldritch-orbe-e-grito");
        }
    }
}
