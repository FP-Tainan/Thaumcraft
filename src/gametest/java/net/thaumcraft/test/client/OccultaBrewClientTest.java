package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os frascos de cozimento na mão e no inventário: o nome que se monta e a cor que sai do que está dentro.
 *
 * <p>Nenhum deles tem figura própria — são todos a mesma garrafa, pintada da cor que a receita deu. Se dois
 * frascos diferentes saírem da mesma cor, é na foto que se vê.
 */
public class OccultaBrewClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            // três cozimentos diferentes, cada um com a sua cor e o seu nome
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:brew["
                    + "thaumcraft:brew=[\"minecraft:nether_wart\",\"minecraft:spider_eye\"],"
                    + "dyed_color=3381606]");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:brew["
                    + "thaumcraft:brew=[\"minecraft:nether_wart\",\"minecraft:glowstone_dust\","
                    + "\"minecraft:sugar\"],dyed_color=16733525]");
            server.runCommand("item replace entity @p hotbar.2 with thaumcraft:brew["
                    + "thaumcraft:brew=[\"minecraft:nether_wart\",\"minecraft:diamond\","
                    + "\"minecraft:fermented_spider_eye\",\"minecraft:golden_carrot\","
                    + "\"minecraft:redstone\",\"minecraft:ghast_tear\"],dyed_color=5046016]");
            // e um de atirar, que leva pólvora dentro
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:brew["
                    + "thaumcraft:brew=[\"minecraft:nether_wart\",\"minecraft:spider_eye\","
                    + "\"minecraft:gunpowder\"],dyed_color=7864145]");
            context.waitTicks(10);
            context.takeScreenshot("frascos_de_cozimento");

            // o frasco atirado a voar, que é o item em pessoa
            server.runCommand("summon thaumcraft:brew ~ ~1 ~4 {Item:{id:\"thaumcraft:brew\",count:1,"
                    + "components:{\"minecraft:dyed_color\":7864145}}}");
            context.waitTicks(3);
            context.takeScreenshot("frasco_a_voar");

            // a destilaria, com dois potes de barro dentro para as garrafas aparecerem
            server.runCommand("setblock ~ ~ ~3 thaumcraft:distillery");
            server.runCommand("item replace block ~ ~ ~3 container.2 with thaumcraft:clay_jar 2");
            server.runCommand("tp @p ~ ~ ~ 0 20");
            context.waitTicks(10);
            context.takeScreenshot("destilaria");

            // um círculo de giz desenhado no chão, com os três anéis
            server.runCommand("fill ~-9 ~-1 ~-9 ~9 ~-1 ~9 minecraft:stone");
            server.runCommand("setblock ~ ~ ~ thaumcraft:circle_heart");
            for (int[] anel : new int[][]{{2, 0}, {4, 1}, {8, 2}}) {
                String giz = anel[1] == 0 ? "ritual_glyph" : (anel[1] == 1 ? "otherwhere_glyph" : "infernal_glyph");
                for (int i = -anel[0]; i <= anel[0]; i++) {
                    for (int j = -anel[0]; j <= anel[0]; j++) {
                        if (Math.abs(i) != anel[0] && Math.abs(j) != anel[0]) continue;
                        server.runCommand(String.format("setblock ~%d ~ ~%d thaumcraft:%s", i, j, giz));
                    }
                }
            }
            server.runCommand("tp @p ~ ~6 ~-6 0 45");
            context.waitTicks(10);
            context.takeScreenshot("circulo_de_giz");
            server.runCommand("tp @p ~ ~-6 ~6 0 20");

            // a névoa no chão, da cor do que se cozeu
            server.runCommand("setblock ~2 ~ ~5 thaumcraft:brew_gas");
            server.runCommand("setblock ~3 ~ ~5 thaumcraft:brew_gas");
            server.runCommand("setblock ~2 ~1 ~5 thaumcraft:brew_gas");
            context.waitTicks(5);
            context.takeScreenshot("nevoa_de_cozimento");

            // e o do meio na mão, com a descrição aberta
            context.getInput().pressKey(key -> key.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("frascos_no_inventario");
            context.getInput().pressKey(key -> key.keyInventory);
            context.waitTicks(5);
        }
    }
}
