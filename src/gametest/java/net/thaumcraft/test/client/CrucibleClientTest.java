package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** O crisol montado, cheio, fervendo, e a cor da água mudando conforme o que se joga dentro. */
public class CrucibleClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute at @p run tp @s ~ ~ ~ 0 35");

            // um caldeirão na frente, e a varinha para benzê-lo
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~-1 ~2 minecraft:magma_block");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~ ~2 thaumcraft:crucible");
            singleplayer.getServer().runCommand("give @p thaumcraft:wand");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(30);
            context.takeScreenshot("crisol_vazio");

            // com água dentro
            singleplayer.getServer().runCommand("execute at @p run data merge block ~ ~ ~2 {water:1b}");
            context.waitTicks(20);
            context.takeScreenshot("crisol_com_agua");

            // e com alguma coisa dissolvida, para ver a cor mudar
            singleplayer.getServer().runCommand(
                    "execute at @p run data merge block ~ ~ ~2 {heat:200,water:1b,aspects:{ignis:12,terra:4}}");
            context.waitTicks(30);
            context.takeScreenshot("crisol_fervendo");
            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 1, 3);
                var be = minecraft.level.getBlockEntity(pos);
                System.out.println("[CRISOL] em " + pos + " achei " + be
                        + (be instanceof net.thaumcraft.block.entity.CrucibleBlockEntity c
                           ? " agua=" + c.hasWater() + " fervendo=" + c.boiling() + " cor=" + Integer.toHexString(c.brew())
                           : ""));
            });
        }
    }
}
