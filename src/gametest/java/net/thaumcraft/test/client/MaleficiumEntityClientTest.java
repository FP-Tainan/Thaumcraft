package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As três criaturas do Maleficium desenhadas no mundo.
 *
 * <p>Sem desenhista registrado o jogo cai ao ver a criatura ({@code EntityRenderDispatcher.shouldRender} com o
 * desenhista nulo), que foi o que aconteceu ao usar um foco do ramo pela primeira vez.
 */
public class MaleficiumEntityClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("summon thaumcraft:dark_matter ~ ~1 ~3");
            server.runCommand("summon thaumcraft:homing_shard ~2 ~1 ~3");
            server.runCommand("summon thaumcraft:diffusion ~-2 ~1 ~3");
            context.waitTicks(10);
            context.takeScreenshot("criaturas_do_maleficium");
        }
    }
}
