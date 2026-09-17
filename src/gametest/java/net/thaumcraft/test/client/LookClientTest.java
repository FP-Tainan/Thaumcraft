package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Sobe um mundo de verdade só para olhar: o thaumômetro na mão e o exame acontecendo. Os outros testes
 * provam a conta; este prova que dá para ver.
 */
public class LookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("gamerule doDaylightCycle false");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("give @p thaumcraft:thaumometer");
            singleplayer.getServer().runCommand("execute at @p run tp @s ~ ~ ~ 0 0");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(30);
            context.takeScreenshot("thaumometro_mao");
            // uma tocha na mão, só para conferir se o jogo desenha o braço em item comum
            singleplayer.getServer().runCommand("item replace entity @p hotbar.1 with minecraft:torch");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(1));
            context.waitTicks(20);
            context.takeScreenshot("tocha");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(10);

            // de fora, para ver a peça de três dimensões
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("thaumometro_fora");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);

            // um bloco conhecido na frente do nariz, e o exame acontecendo nele
            // pedra primeiro, que é só terra e se lê desde o começo
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 minecraft:stone");
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(15);
            context.takeScreenshot("examinando");
            context.waitTicks(25);
            context.takeScreenshot("examinado");
            // um item caído no chão, que também se examina
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 minecraft:air");
            singleplayer.getServer().runCommand("execute at @p run summon item ~ ~1.5 ~2 {Item:{id:\"minecraft:apple\",count:1},NoGravity:1b}");
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(45);
            context.takeScreenshot("item_no_chao");
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            singleplayer.getServer().runCommand("kill @e[type=item]");
            context.waitTicks(5);

            // e a água, que também se examina
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 minecraft:water");
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(20);
            context.takeScreenshot("agua");

            // agora o minério, que ele ainda não entende
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 minecraft:iron_ore");
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(true);
                net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
            });
            context.waitTicks(20);
            context.takeScreenshot("recusado");
            context.runOnClient(minecraft -> {
                minecraft.options.keyUse.setDown(false);
                minecraft.player.stopUsingItem();
            });
            context.waitTicks(5);
        }
    }
}
