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

            // e o do meio na mão, com a descrição aberta
            context.getInput().pressKey(key -> key.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("frascos_no_inventario");
            context.getInput().pressKey(key -> key.keyInventory);
            context.waitTicks(5);
        }
    }
}
