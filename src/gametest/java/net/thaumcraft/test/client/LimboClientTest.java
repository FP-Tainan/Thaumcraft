package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.shattered.ShatteredRealms;

/** O Limbo: a terra de tecido desfiado sobre o chão de tecido eterno. */
public class LimboClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runOnServer(s -> {
                var limbo = ShatteredRealms.limbo(s);
                if (limbo == null) throw new IllegalStateException("o Limbo não abriu");
                var jogador = s.getPlayerList().getPlayers().getFirst();
                // o pedaço tem de estar feito antes de se lhe perguntar a altura
                limbo.getChunk(0, 0);
                int alto = limbo.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 8, 8);
                jogador.teleportTo(limbo, 8.5, alto + 1.0, 8.5, java.util.Set.of(), 135.0f, 5.0f, false);
            });
            context.waitTicks(80);
            context.takeScreenshot("limbo");
        }
    }
}
