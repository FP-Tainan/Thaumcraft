package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A varinha na mão, o nó na frente, e o vis subindo enquanto ela bebe dele. */
public class WandClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute at @p run tp @s ~ ~ ~ 0 0");
            singleplayer.getServer().runCommand("give @p thaumcraft:wand");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            context.takeScreenshot("varinha_mao");

            // de fora, para ver a peça inteira
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("varinha_fora");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);

            // um nó bem na frente, e a varinha bebendo dele
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 thaumcraft:node");
            context.waitTicks(30);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(60);
            context.takeScreenshot("varinha_bebendo");
            // e de fora, com os braços erguidos como no arco
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK));
            context.waitTicks(10);
            context.takeScreenshot("varinha_bebendo_fora");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            context.waitTicks(10);
            context.takeScreenshot("varinha_cheia");

            // e com o foco de fogo preso e vis de ignis na varinha, ela cospe chamas
            singleplayer.getServer().runCommand(
                    "item replace entity @p hotbar.0 with thaumcraft:wand[thaumcraft:wand_vis={ignis:2500}]");
            singleplayer.getServer().runCommand("give @p thaumcraft:focus_fire");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(1));
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.getInventory().setSelectedSlot(0);
            });
            context.waitTicks(10);
            context.takeScreenshot("varinha_com_foco");
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(20);
            context.takeScreenshot("foco_de_fogo");
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            context.waitTicks(5);
        }
    }
}
