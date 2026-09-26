package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.thaumcraft.shattered.DungeonRooms;
import net.thaumcraft.shattered.Pockets;

import java.util.List;

/**
 * Umas quantas salas do original, uma a uma, para se ver que a planificação da 1.12 saiu direita.
 *
 * <p>Duas coisas aqui custaram a achar. A primeira: <b>primeiro leva-se quem joga ao lugar, e só depois se cava a
 * sala</b> — um bolso cavado num pedaço de mundo que ninguém está a segurar pode ir-se embora antes de alguém lá
 * chegar. A segunda: quem espera a sala nascer tem de estar de <b>espectador</b>, senão cai no vazio no segundo
 * que passa entre chegar e a sala aparecer, e a foto sai preta.
 *
 * <p>Não há o que conferir por conta própria numa tela: as fotos ficam em
 * {@code build/run/clientGameTest/screenshots} para quem estiver de olho.
 */
public class WildPocketClientTest implements FabricClientGameTest {
    /**
     * As que se retratam: um salão, uma armadilha, um labirinto, um poço e um caldeirão do Nether.
     *
     * <p>Com cada uma vai de onde se olha para ela — o meio da largura, uns palmos acima do chão e encostado a
     * uma parede —, porque as salas do original não são todas do mesmo tamanho.
     */
    private record Retrato(String nome, int x, int y, int z, float giro) {
    }

    private static final List<Retrato> MOSTRA = List.of(
            new Retrato("great_hall", 32, 10, 4, 0.0f),
            new Retrato("hallway_pit_fall_trap", 19, 8, 4, 0.0f),
            new Retrato("omni_maze", 16, 10, 4, 0.0f),
            new Retrato("the_nexus", 29, 12, 4, 0.0f),
            new Retrato("the_cauldron", 36, 10, 4, 0.0f));

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            // espectador: sem gravidade, quem espera a sala nascer não cai no vazio antes de ela chegar. E da
            // consola o comando precisa de dizer a quem se aplica — sem o @p ele queixa-se e não faz nada
            server.runCommand("gamemode spectator @p");
            server.runOnServer(Pockets::level);

            for (int i = 0; i < MOSTRA.size(); i++) {
                Retrato retrato = MOSTRA.get(i);
                String nome = retrato.nome();
                BlockPos canto = new BlockPos(i * Pockets.STRIDE, 32, 0);
                BlockPos olho = canto.offset(retrato.x(), retrato.y(), retrato.z());
                server.runCommand("execute in thaumcraft:public_pockets run tp @p "
                        + (olho.getX() + 0.5) + " " + olho.getY() + " " + (olho.getZ() + 0.5)
                        + " " + retrato.giro() + " 12");
                context.waitTicks(20);

                server.runOnServer(s -> {
                    var bolsos = Pockets.level(s);
                    if (bolsos == null) return;
                    var sala = DungeonRooms.read(s, nome);
                    if (sala == null) return;
                    DungeonRooms.place(bolsos, canto, sala);
                });
                context.waitTicks(80);
                context.takeScreenshot("sala_" + nome);
            }
        }
    }
}
