package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;

/**
 * O resto do Magia Naturalis na tela: os dois baús arcanos, a mesa de transcrição, o geo-pilone sobre os totens, o
 * baú maligno nos quatro feitios, a criadora de mácula e o revenante.
 *
 * <p>Os comandos correm no console do servidor, onde o {@code ~} é a origem do mundo; por isso tudo o que precisa
 * cair em volta de quem olha passa pelo {@code execute at @p}.
 */
public class NaturalisWorldClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // os dois baús, a mesa e o pilone em cima do totem
            perto(server, "setblock ~3 ~ ~5 thaumcraft:arcane_chest_greatwood");
            perto(server, "setblock ~1 ~ ~5 thaumcraft:arcane_chest_silverwood");
            perto(server, "setblock ~-1 ~ ~5 thaumcraft:transcribing_table");
            perto(server, "setblock ~-4 ~-1 ~5 thaumcraft:obsidian_totem");
            perto(server, "setblock ~-4 ~ ~5 thaumcraft:obsidian_totem");
            perto(server, "setblock ~-4 ~1 ~5 thaumcraft:obsidian_totem");
            perto(server, "setblock ~-4 ~3 ~5 thaumcraft:geo_pylon");
            context.waitTicks(20);
            context.takeScreenshot("naturalis_blocos");

            // o pilone de perto, com a cara dele
            perto(server, "tp @p ~-4 ~1 ~ 0 -20");
            context.waitTicks(20);
            context.takeScreenshot("naturalis_pilone");

            // as coisas novas na mão
            server.runCommand("give @p thaumcraft:key_of_unraveling");
            server.runCommand("give @p thaumcraft:key_of_endorsing");
            server.runCommand("give @p thaumcraft:biome_sampler");
            server.runCommand("give @p thaumcraft:revenant_focus");
            server.runCommand("give @p thaumcraft:arcane_chest_greatwood");
            server.runCommand("give @p thaumcraft:geo_pylon");
            server.runCommand("give @p thaumcraft:transcribing_table");
            server.runCommand("give @p thaumcraft:trunk_spawner_corrupted");
            server.runCommand("give @p thaumcraft:trunk_spawner_sinister");
            server.runCommand("give @p thaumcraft:trunk_spawner_demonic");
            server.runCommand("give @p thaumcraft:trunk_spawner_tainted");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("naturalis_coisas_novas");

            // os quatro baús malignos, de frente
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
            // a mão vazia, para o item não tapar a vista
            server.runCommand("item replace entity @p hotbar.0 with air");
            perto(server, "tp @p ~ ~ ~ 0 0");
            perto(server, "summon thaumcraft:evil_trunk ~3 ~ ~8 {TrunkType:0,Waiting:1b,NoAI:1b}");
            perto(server, "summon thaumcraft:evil_trunk ~1 ~ ~8 {TrunkType:1,Waiting:1b,NoAI:1b}");
            perto(server, "summon thaumcraft:evil_trunk ~-1 ~ ~8 {TrunkType:2,Waiting:1b,NoAI:1b}");
            perto(server, "summon thaumcraft:evil_trunk ~-3 ~ ~8 {TrunkType:3,Waiting:1b,NoAI:1b}");
            context.waitTicks(30);
            context.takeScreenshot("naturalis_baus_malignos");

            // e a criadora de mácula com o revenante ao lado
            server.runCommand("kill @e[type=thaumcraft:evil_trunk]");
            perto(server, "summon thaumcraft:taint_breeder ~-2 ~ ~8 {NoAI:1b}");
            perto(server, "summon thaumcraft:revenant ~2 ~ ~8 {NoAI:1b}");
            context.waitTicks(30);
            context.takeScreenshot("naturalis_criadora");
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
