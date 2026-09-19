package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.entity.TravelingTrunkEntity;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

import java.util.concurrent.atomic.AtomicReference;

/** O baú itinerante na tela: fechado ao lado de um baú do jogo, com a plaquinha da melhoria, a tela dele e o item. */
public class TrunkClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            AtomicReference<TravelingTrunkEntity> made = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -3; x <= 3; x++) for (int z = 1; z <= 5; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 3; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                level.setBlockAndUpdate(p.offset(-1, 0, 3), Blocks.CHEST.defaultBlockState());
                TravelingTrunkEntity t = TCEntities.TRAVELING_TRUNK.create(level, EntitySpawnReason.COMMAND);
                t.snapTo(p.getX() + 1.5, p.getY(), p.getZ() + 3.5, 180.0f, 0.0f);
                t.setYBodyRot(180.0f);
                t.setYHeadRot(180.0f);
                t.setOwner(player.getName().getString());
                t.setUpgrade(5);
                t.setStay(true);
                t.setInvSize();
                t.setNoAi(true);
                level.addFreshEntity(t);
                made.set(t);
                player.getInventory().setItem(0, new ItemStack(TCItems.TRUNK_SPAWNER));
                player.getInventory().setSelectedSlot(0);
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 25");
            context.waitTicks(30);
            context.takeScreenshot("bau_itinerante");
            server.runOnServer(s -> net.thaumcraft.inventory.TrunkMenu.open(s.getPlayerList().getPlayers().getFirst(), made.get()));
            context.waitTicks(20);
            context.takeScreenshot("bau_itinerante_tela");
        }
    }
}
