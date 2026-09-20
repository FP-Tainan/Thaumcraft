package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.registry.TCItems;

/** As quatro casas de bijuteria dentro do inventário de sempre, com amuleto, anel e cinto vestidos. */
public class BaublesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            server.runCommand("give @p thaumcraft:mundane_ring");
            server.runCommand("give @p thaumcraft:apprentice_ring_fire");
            server.runCommand("give @p thaumcraft:goggles");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var worn = Baubles.container(player);
                worn.setItem(Baubles.AMULET, new ItemStack(TCItems.MUNDANE_AMULET));
                worn.setItem(Baubles.RING_1, new ItemStack(TCItems.APPRENTICE_RINGS.get("water")));
                worn.setItem(Baubles.BELT, new ItemStack(TCItems.FOCUS_POUCH));
            });
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(20);
            context.takeScreenshot("inventario_baubles");
        }
    }
}
