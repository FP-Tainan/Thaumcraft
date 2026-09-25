package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A aba do Ars Mortuorum no Thaumonomicon e a aba dele no criativo. */
public class MortuorumBookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("thaumcraft pesquisa tudo");
            context.waitTicks(20);

            context.runOnClient(minecraft -> {
                net.thaumcraft.client.gui.ThaumonomiconScreen.select(net.thaumcraft.mortuorum.Mortuorum.CATEGORY);
                minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("livro_mortuorum");

            // e as coisas do ramo na mochila, para se verem as figuras
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
            for (String coisa : new String[]{"necronomicon", "soul_heart", "isaacs_head", "skull_wall",
                    "bucket_blood", "jar_of_blood", "soul_in_a_jar", "bone_needle", "brain_on_a_stick",
                    "scythe", "scythe_bone", "summoning_altar", "sewing_machine", "heart", "brains",
                    "zombie_torso", "cow_head", "spider_legs", "iron_golem_arm"}) {
                server.runCommand("give @p thaumcraft:" + coisa);
            }
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("coisas_do_mortuorum");
        }
    }
}
