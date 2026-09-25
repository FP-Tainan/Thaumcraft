package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

/** O altar antigo do Crimson Warfare, montado no chão, e a aba do ramo no livro. */
public class CrimsonClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("thaumcraft pesquisa tudo");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(12);
                net.thaumcraft.crimson.AncientAltarFeature.build(level, onde.below());
            });
            server.runCommand("tp @p ~ ~4 ~ 180 25");
            context.waitTicks(40);
            context.takeScreenshot("crimson_altar");

            context.runOnClient(minecraft -> {
                net.thaumcraft.client.gui.ThaumonomiconScreen.select(net.thaumcraft.crimson.Crimson.CATEGORY);
                minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("livro_crimson");
        }
    }
}
