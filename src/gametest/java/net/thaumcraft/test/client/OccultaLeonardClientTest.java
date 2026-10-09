package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * O <b>Leonard</b> visto: o bode de pé no roupão, de frente, de lado e de costas.
 *
 * <p>É o chefe do ramo, e o que há para ver é a cara dele: o <b>focinho</b> comprido, a <b>barba</b>, as
 * duas <b>orelhas</b> caídas e os <b>três chifres</b> — dois para fora e um ao meio. De lado vê-se o
 * focinho inteiro, e de costas a <b>saia do roupão</b>, que é a peça que o faz parecer um ídolo e não um
 * bicho.
 *
 * <p>E a <b>barra de chefe</b> aparece na foto de frente, que é como se sabe que ela existe.
 */
public class OccultaLeonardClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            double[] onde = server.computeOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = jogador.blockPosition();

                var ele = OccultaEntities.LEONARD.create(mundo, EntitySpawnReason.COMMAND);
                if (ele != null) {
                    ele.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5, 180.0f, 0.0f);
                    // o corpo e a cabeça também, que o snapTo só vira o rumo de quem anda
                    ele.setYBodyRot(180.0f);
                    ele.setYHeadRot(180.0f);
                    ele.setPersistenceRequired();
                    ele.setNoAi(true);
                    ele.setSilent(true);
                    mundo.addFreshEntity(ele);
                }
                jogador.setDeltaMovement(Vec3.ZERO);
                return new double[]{meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5};
            });

            olha(server, onde[0], onde[1] + 1.2, onde[2] - 4.0, 0.0f, 0.0f);
            context.waitTicks(30);
            context.takeScreenshot("o_leonard_de_frente");

            olha(server, onde[0] + 4.0, onde[1] + 1.2, onde[2], 90.0f, 0.0f);
            context.waitTicks(20);
            context.takeScreenshot("o_leonard_de_lado");

            olha(server, onde[0], onde[1] + 1.2, onde[2] + 4.0, 180.0f, 0.0f);
            context.waitTicks(20);
            context.takeScreenshot("o_leonard_de_costas");
        }
    }

    /** Põe a câmara naquele ponto, olhando para aquele rumo. */
    private static void olha(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             double x, double y, double z, float guinada, float passo) {
        server.runCommand(String.format(java.util.Locale.ROOT, "tp @p %.1f %.1f %.1f %.1f %.1f",
                x, y, z, guinada, passo));
    }
}
