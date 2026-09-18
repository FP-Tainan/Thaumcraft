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
            release(context);

            // o nó sai da frente, senão a varinha bebe dele em vez de usar o foco
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~3 air");
            String full = "thaumcraft:wand_vis={aer:2500,terra:2500,ignis:2500,aqua:2500,ordo:2500,perditio:2500}";

            // as brasas do foco de fogo
            singleplayer.getServer().runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[" + full
                    + ",thaumcraft:wand_focus=\"fire\"]");
            hold(context, 12);
            context.takeScreenshot("foco_fogo_brasas");
            release(context);

            // o raio, num porco parado à frente
            singleplayer.getServer().runCommand("execute at @p run summon pig ~ ~ ~5 {NoAI:1b}");
            singleplayer.getServer().runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[" + full
                    + ",thaumcraft:wand_focus=\"shock\"]");
            context.runOnClient(minecraft -> minecraft.player.setXRot(10.0f));
            hold(context, 6);
            context.takeScreenshot("foco_raio");
            release(context);
            singleplayer.getServer().runCommand("kill @e[type=pig]");

            // a esfera de gelo, que quica no chão
            singleplayer.getServer().runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[" + full
                    + ",thaumcraft:wand_focus=\"frost\"]");
            context.runOnClient(minecraft -> minecraft.player.setXRot(20.0f));
            hold(context, 5);
            context.takeScreenshot("foco_gelo");
            release(context);

            // o feixe da escavação roendo uma parede de pedra
            singleplayer.getServer().runCommand("execute at @p run fill ~-1 ~ ~4 ~1 ~2 ~4 stone");
            singleplayer.getServer().runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[" + full
                    + ",thaumcraft:wand_focus=\"excavation\"]");
            context.runOnClient(minecraft -> minecraft.player.setXRot(0.0f));
            hold(context, 20);
            context.takeScreenshot("foco_escavacao");
            release(context);

            // o buraco portátil numa parede grossa
            singleplayer.getServer().runCommand("execute at @p run fill ~-2 ~ ~5 ~2 ~3 ~8 stone");
            singleplayer.getServer().runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[" + full
                    + ",thaumcraft:wand_focus=\"portable_hole\"]");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~1");
            context.runOnClient(minecraft -> minecraft.player.setXRot(10.0f));
            hold(context, 3);
            release(context);
            context.waitTicks(15);
            context.takeScreenshot("foco_buraco");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~4.5");
            context.waitTicks(5);
            context.takeScreenshot("foco_buraco_dentro");
        }
    }

    private static void hold(ClientGameTestContext context, int ticks) {
        context.waitTicks(5);
        context.runOnClient(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(0);
            minecraft.options.keyUse.setDown(true);
            net.minecraft.client.KeyMapping.click(minecraft.options.keyUse.getDefaultKey());
        });
        context.waitTicks(ticks);
    }

    private static void release(ClientGameTestContext context) {
        context.runOnClient(minecraft -> {
            minecraft.options.keyUse.setDown(false);
            // solta de verdade, avisando o servidor, como quando o botão sobe
            if (minecraft.player.isUsingItem()) minecraft.gameMode.releaseUsingItem(minecraft.player);
        });
        context.waitTicks(5);
    }
}
