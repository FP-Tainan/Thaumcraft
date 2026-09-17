package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A aba do criativo cheia, a armadura de táumio vestida e o livro em português. */
public class GearClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");

            // a armadura de táumio vestida, vista de fora
            singleplayer.getServer().runCommand("item replace entity @p armor.head with thaumcraft:thaumium_helmet");
            singleplayer.getServer().runCommand("item replace entity @p armor.chest with thaumcraft:thaumium_chestplate");
            singleplayer.getServer().runCommand("item replace entity @p armor.legs with thaumcraft:thaumium_leggings");
            singleplayer.getServer().runCommand("item replace entity @p armor.feet with thaumcraft:thaumium_boots");
            singleplayer.getServer().runCommand("give @p thaumcraft:thaumium_pickaxe");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(30);
            context.takeScreenshot("taumio_vestido");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);

            // a aba do criativo, com tudo o que o mod tem
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(
                            minecraft.player, minecraft.player.connection.enabledFeatures(), true)));
            context.waitTicks(20);
            context.takeScreenshot("criativo");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // o livro em português: o teste troca a língua do jogo para conferir a tradução
            context.runOnClient(minecraft -> {
                minecraft.getLanguageManager().setSelected("pt_br");
                minecraft.reloadResourcePacks();
            });
            context.waitTicks(60);

            // e o livro em português
            context.runOnClient(minecraft ->
                    minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen()));
            context.waitTicks(20);
            context.takeScreenshot("livro_portugues");
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof net.thaumcraft.client.gui.ThaumonomiconScreen book) {
                    minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ResearchPageScreen(book,
                            net.thaumcraft.research.Researches.get("WARP")));
                }
            });
            context.waitTicks(15);
            context.takeScreenshot("pagina_portugues");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
