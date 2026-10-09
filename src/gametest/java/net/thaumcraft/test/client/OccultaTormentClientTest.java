package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.occulta.torment.Torment;
import net.thaumcraft.occulta.torment.TormentMaze;

import java.util.Set;

/**
 * O Tormento, visto de dentro.
 *
 * <p>É a fatia em que o que se vê é <b>a ausência</b> do que lá está: as paredes do labirinto não se
 * desenham, e as fotos têm de mostrar exatamente isso — um chão de micélio que se perde no vermelho, sem
 * nenhuma parede à vista, e um corpo que no entanto não passa.
 *
 * <p>Quatro fotos: o <b>chão do labirinto</b> da altura dos olhos, o <b>portal</b> na moldura dele, um
 * <b>baú enterrado</b> no chão e o labirinto <b>de cima</b>, de onde se vê que as paredes continuam a não
 * aparecer mesmo quando se olha para elas de fora.
 */
public class OccultaTormentClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");

            // o labirinto do andar de baixo, da porta de entrada, olhando para o sul
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                var jogador = s.getPlayerList().getPlayers().getFirst();
                int chão = TormentMaze.floorOf(0);
                jogador.teleportTo(mundo, TormentMaze.DOOR_X + 0.5, chão + 1.0,
                        TormentMaze.DOOR_Z + 0.5, Set.of(), 0.0f, 0.0f, false);
            });
            context.waitTicks(80);
            context.takeScreenshot("o_labirinto_de_dentro");

            // o portal, do corredor que vai a ele
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                var jogador = s.getPlayerList().getPlayers().getFirst();
                int chão = TormentMaze.floorOf(0);
                jogador.teleportTo(mundo, TormentMaze.ORIGIN_X + TormentMaze.SIZE + 0.5, chão + 1.0,
                        TormentMaze.ORIGIN_Z + 2 * TormentMaze.SIZE - 4.5, Set.of(), 0.0f, 0.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("o_portal_do_tormento");

            // o baú do meio, que está enterrado no chão
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                var jogador = s.getPlayerList().getPlayers().getFirst();
                int chão = TormentMaze.floorOf(0);
                jogador.teleportTo(mundo, TormentMaze.ORIGIN_X + TormentMaze.SIZE + 0.5, chão + 1.0,
                        TormentMaze.ORIGIN_Z + TormentMaze.SIZE - 2.5, Set.of(), 0.0f, 25.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("o_bau_enterrado");

            // e de cima, de onde se vê a laje inteira do andar e nenhuma parede
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                var jogador = s.getPlayerList().getPlayers().getFirst();
                int chão = TormentMaze.floorOf(0);
                jogador.teleportTo(mundo, TormentMaze.ORIGIN_X + TormentMaze.SIZE + 0.5, chão + 9.0,
                        TormentMaze.ORIGIN_Z + 10.5, Set.of(), 0.0f, 35.0f, false);
            });
            context.waitTicks(60);
            context.takeScreenshot("o_labirinto_de_cima");

            /*
             * E o <b>Senhor do Tormento</b>, na sala do meio, que é onde ele espera: de frente, para se
             * verem os quatro braços, a barba de seis pontas e os dois chifres; e de trás, para se verem
             * as duas chapas de vinte por quarenta que são as asas dele.
             */
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                int chão = TormentMaze.floorOf(0);
                for (var tinha : mundo.getEntitiesOfClass(
                        net.thaumcraft.occulta.torment.LordOfTormentEntity.class,
                        new net.minecraft.world.phys.AABB(
                                TormentMaze.ORIGIN_X, chão - 4, TormentMaze.ORIGIN_Z,
                                TormentMaze.ORIGIN_X + TormentMaze.SPAN_X, chão + 8,
                                TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z))) {
                    tinha.discard();
                }
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.teleportTo(mundo, TormentMaze.LORD_X + 0.5, chão + 1.0,
                        TormentMaze.LORD_Z - 4.5, Set.of(), 0.0f, 0.0f, false);
            });
            context.waitTicks(20);
            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                var ele = Torment.waitFor(mundo, 0);
                if (ele == null) throw new IllegalStateException("o Senhor não nasceu");
                ele.setNoAi(true);
            });
            context.waitTicks(40);
            context.takeScreenshot("o_senhor_do_tormento");

            server.runOnServer(s -> {
                var mundo = Torment.level(s);
                if (mundo == null) throw new IllegalStateException("o Tormento não abriu");
                int chão = TormentMaze.floorOf(0);
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.teleportTo(mundo, TormentMaze.LORD_X + 0.5, chão + 1.0,
                        TormentMaze.LORD_Z + 4.5, Set.of(), 180.0f, 0.0f, false);
            });
            context.waitTicks(30);
            context.takeScreenshot("o_senhor_de_costas");
        }
    }
}
