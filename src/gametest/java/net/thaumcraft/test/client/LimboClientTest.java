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
                jogador.teleportTo(limbo, 14.5, alto + 3.0, 14.5, java.util.Set.of(), 135.0f, -5.0f, false);
                // e um monólito a olhar, uns blocos à frente
                var lousa = net.thaumcraft.shattered.ShatteredEntities.MONOLITH.create(limbo,
                        net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (lousa != null) {
                    lousa.snapTo(2.5, alto + 1.0, 2.5, 0.0f, 0.0f);
                    limbo.addFreshEntity(lousa);
                    // o Limbo é escuro e a lousa é preta: umas luzes para se a ver
                    for (int dx = -4; dx <= 4; dx += 4) {
                        for (int dz = -4; dz <= 4; dz += 4) {
                            limbo.setBlockAndUpdate(new net.minecraft.core.BlockPos(2 + dx, alto + 1, 2 + dz),
                                    net.minecraft.world.level.block.Blocks.GLOWSTONE.defaultBlockState());
                        }
                    }
                }
            });
            context.waitTicks(80);
            context.takeScreenshot("limbo");
        }
    }
}
