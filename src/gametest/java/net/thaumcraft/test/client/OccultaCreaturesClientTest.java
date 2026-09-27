package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os três bichos do ofício desenhados no mundo: a Mandrágora, a Mandrágora-de-Mina e o Ent.
 *
 * <p>Os modelos são os do Witchery caixa por caixa (ver o {@code CreatureModels}); esta prova é para se ver que os
 * três têm desenhista registrado — sem ele o jogo cai ao olhar para a criatura — e que a pele de cada um está no
 * lugar. Os abafadores vão na cabeça do jogador na mesma foto, porque é com eles que se anda perto delas.
 */
public class OccultaCreaturesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            // o jogador de frente para o sul, e os três em fila diante dele
            server.runCommand("tp @p ~ ~ ~ 0 0");
            server.runCommand("summon thaumcraft:mandrake ~-2 ~ ~6");
            server.runCommand("summon thaumcraft:minedrake ~ ~ ~6");
            server.runCommand("summon thaumcraft:ent ~3 ~ ~7");
            context.waitTicks(20);
            context.takeScreenshot("bichos_do_oficio");

            // e os abafadores na cabeça, que é com o que se anda perto da mandrágora
            server.runCommand("item replace entity @p armor.head with thaumcraft:earmuffs");
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:ent_branch");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(10);
            context.takeScreenshot("abafadores_na_cabeca");
            context.runOnClient(minecraft ->
                    minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
        }
    }
}
