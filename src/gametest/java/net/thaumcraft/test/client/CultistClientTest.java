package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.entity.eldritch.CultistClericEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

import java.util.concurrent.atomic.AtomicReference;

/** O Culto Carmesim: o cavaleiro, o clérigo no ritual do altar, o pretor, o portal; e as armaduras. */
public class CultistClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("gamerule doMobSpawning false");
            server.runCommand("difficulty peaceful");
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
                BlockPos altar = p.offset(0, 0, 9);
                level.setBlockAndUpdate(altar, TCBlocks.ELDRITCH_ALTAR.defaultBlockState());
                int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
                for (int[] c : corners) {
                    CultistClericEntity cleric = TCEntities.CULTIST_CLERIC.create(level, EntitySpawnReason.STRUCTURE);
                    cleric.setPos(altar.getX() + 0.5 + c[0], altar.getY(), altar.getZ() + 0.5 + c[1]);
                    cleric.setHomeTo(altar, 8);
                    cleric.finalizeSpawn(level, level.getCurrentDifficultyAt(altar), EntitySpawnReason.STRUCTURE, null);
                    cleric.setRitualist(true);
                    cleric.setNoAi(true);
                    level.addFreshEntity(cleric);
                }
                Mob knight = TCEntities.CULTIST_KNIGHT.create(level, EntitySpawnReason.STRUCTURE);
                knight.setPos(p.getX() - 2.5, p.getY(), p.getZ() + 4.5);
                knight.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.STRUCTURE, null);
                knight.setNoAi(true);
                knight.setYRot(180);
                knight.setYHeadRot(180);
                knight.yBodyRot = 180;
                level.addFreshEntity(knight);
                Mob leader = TCEntities.CULTIST_LEADER.create(level, EntitySpawnReason.STRUCTURE);
                leader.setPos(p.getX() + 3.5, p.getY(), p.getZ() + 4.5);
                leader.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.STRUCTURE, null);
                leader.setNoAi(true);
                leader.setYRot(180);
                leader.setYHeadRot(180);
                leader.yBodyRot = 180;
                level.addFreshEntity(leader);
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_ROBE_HELMET));
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CULTIST_ROBE_CHESTPLATE));
                player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CULTIST_ROBE_LEGGINGS));
                player.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CULTIST_BOOTS));
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 10");
            context.waitTicks(40);
            context.takeScreenshot("culto-ritual");
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 1.5) + " 0 5");
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.waitTicks(10);
            context.takeScreenshot("culto-robe-no-jogador");
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            server.runOnServer(s -> {
                var level = s.overworld();
                Mob portal = TCEntities.CULTIST_PORTAL.create(level, EntitySpawnReason.STRUCTURE);
                portal.setPos(p.getX() - 5.5, p.getY(), p.getZ() + 0.5);
                level.addFreshEntity(portal);
            });
            server.runCommand("tp @p " + (p.getX() - 5.5) + " " + p.getY() + " " + (p.getZ() - 5.5) + " 0 -12");
            context.waitTicks(60);
            context.takeScreenshot("culto-portal");
        }
    }
}
