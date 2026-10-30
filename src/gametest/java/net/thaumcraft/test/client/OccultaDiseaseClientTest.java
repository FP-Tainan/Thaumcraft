package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>poça de doença</b>: o rasto de quem está doente.
 *
 * <p>A primeira tela é a poça em si, de rente ao chão — um castanho de ferrugem, parado, que não se parece com água
 * nenhuma do jogo. A segunda é o que ela faz quando cai: ela <b>escorre para baixo</b> e não se abre para os
 * lados, e por isso uma fileira delas largada no ar vira uma <b>coluna</b> e não uma mancha.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaDiseaseClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("execute at @p run fill ~-8 ~-1 ~3 ~8 ~-1 ~16 minecraft:stone");
            server.runCommand("execute at @p run fill ~-8 ~ ~3 ~8 ~6 ~16 minecraft:air");

            // uma poça de três por três no chão, ao lado de uma de água, para se ver que não é água
            server.runCommand("execute at @p run fill ~1 ~ ~7 ~3 ~ ~9 thaumcraft:disease");
            server.runCommand("execute at @p run fill ~-3 ~-1 ~7 ~-1 ~-1 ~9 minecraft:water");
            server.runCommand("execute at @p run tp @p ~ ~ ~3 0 20");
            context.waitTicks(20);
            context.takeScreenshot("doenca_1_a_poca_e_a_agua");

            // e uma largada no alto, que cai em coluna e não se abre
            server.runCommand("execute at @p run setblock ~ ~5 ~8 thaumcraft:disease");
            context.waitTicks(10);
            context.takeScreenshot("doenca_2_caindo");
            context.waitTicks(90);
            context.takeScreenshot("doenca_3_ja_no_chao");
        }
    }
}
