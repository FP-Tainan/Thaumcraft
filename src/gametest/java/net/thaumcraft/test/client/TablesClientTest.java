package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** As mesas do ModelArcaneWorkbench: a bancada arcana com a varinha e a de desconstrução trabalhando. */
public class TablesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 35");
            server.runCommand("execute at @p run setblock ~-1 ~ ~2 thaumcraft:arcane_workbench");
            server.runCommand("execute at @p run setblock ~1 ~ ~2 thaumcraft:deconstruction_table");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().get(0);
                var level = player.level();
                var base = player.blockPosition();
                if (level.getBlockEntity(base.offset(-1, 0, 2)) instanceof net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity bench) {
                    bench.setItem(net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity.WAND_SLOT,
                            new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.WAND));
                }
                if (level.getBlockEntity(base.offset(1, 0, 2)) instanceof net.thaumcraft.block.entity.DeconstructionTableBlockEntity table) {
                    table.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BLOCK, 2));
                }
            });
            server.runCommand("give @p thaumcraft:deconstruction_table");
            server.runCommand("give @p thaumcraft:arcane_workbench");
            context.waitTicks(55);
            context.takeScreenshot("mesas_bancada_e_desconstrucao");
        }
    }
}
