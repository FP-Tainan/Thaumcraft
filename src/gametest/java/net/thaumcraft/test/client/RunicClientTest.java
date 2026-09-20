package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;

/** A barra do escudo rúnico enchendo com o cinturão vestido, o clarão das runas ao apanhar e as peças no Baubles. */
public class RunicClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var worn = Baubles.container(player);
                worn.setItem(Baubles.AMULET, new ItemStack(TCItems.VIS_AMULET));
                worn.setItem(Baubles.RING_1, new ItemStack(TCItems.RUNIC_RING_CHARGED));
                worn.setItem(Baubles.RING_2, new ItemStack(TCItems.RUNIC_RING_REGEN));
                worn.setItem(Baubles.BELT, new ItemStack(TCItems.RUNIC_GIRDLE_KINETIC));
                ItemStack wand = new ItemStack(TCItems.WAND);
                WandItem.setVis(wand, new AspectList().add(Aspects.AIR, 2500).add(Aspects.EARTH, 2500));
                player.getInventory().setItem(20, wand);
            });
            context.waitTicks(160);
            context.takeScreenshot("escudo_runico_barra");
            server.runCommand("time set midnight");
            context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(5);
            server.runCommand("damage @p 3 minecraft:generic");
            context.waitTicks(2);
            context.takeScreenshot("escudo_runico_clarao");
            context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(20);
            context.takeScreenshot("escudo_runico_baubles");
        }
    }
}
