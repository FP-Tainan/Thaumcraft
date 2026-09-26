package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.shattered.PocketThemes;
import net.thaumcraft.shattered.Pockets;

/**
 * As quatro salas com tema, uma a uma: a biblioteca, o deserto, o Nether e o reino antigo.
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
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            // espectador: sem gravidade, quem espera a sala nascer não cai no vazio antes de ela chegar. E da
            // consola o comando precisa de dizer a quem se aplica — sem o @p ele queixa-se e não faz nada
            server.runCommand("gamemode spectator @p");
            server.runOnServer(Pockets::level);

            for (PocketThemes tema : PocketThemes.values()) {
                BlockPos olho = new BlockPos(tema.ordinal() * Pockets.STRIDE + 2, 34, 2);
                server.runCommand("execute in thaumcraft:public_pockets run tp @p "
                        + (olho.getX() + 0.5) + " " + olho.getY() + " " + (olho.getZ() + 0.5) + " -45 12");
                context.waitTicks(20);

                server.runOnServer(s -> {
                    var bolsos = Pockets.level(s);
                    if (bolsos == null) return;
                    BlockPos canto = new BlockPos(tema.ordinal() * Pockets.STRIDE, 32, 0);
                    Pockets.carve(bolsos, canto, tema);
                    tema.fill(bolsos, canto, bolsos.getRandom());
                    // onde quem olha está tem de ficar livre: as dunas e as colunas enchem-no
                    for (int y = -1; y < 3; y++) {
                        bolsos.setBlock(olho.above(y), Blocks.AIR.defaultBlockState(), 2);
                    }
                });
                context.waitTicks(60);
                context.takeScreenshot("bolso_" + tema.name().toLowerCase(java.util.Locale.ROOT));
            }
        }
    }
}
