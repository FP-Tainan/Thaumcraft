package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O comando de teste tem de destrancar de verdade — e o caderno de quem joga tem de saber disso.
 *
 * <p>O que importa aqui é a segunda parte: a pesquisa mora no servidor, e é um anexo sincronizado que a
 * leva até a máquina de quem joga. Se essa sincronia quebrar, o comando parece não funcionar — o livro
 * continua mostrando tudo trancado mesmo com tudo destrancado do lado de lá.
 */
public class CommandClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");

            context.runOnClient(minecraft -> {
                var knowledge = net.thaumcraft.research.Knowledges.of(minecraft.player);
                System.out.println("[COMANDO] antes: " + knowledge.research().size() + " pesquisas, "
                        + knowledge.discovered().size() + " aspectos descobertos");
            });

            server.runCommand("thaumcraft tudo @a");
            context.waitTicks(20);

            context.runOnClient(minecraft -> {
                var knowledge = net.thaumcraft.research.Knowledges.of(minecraft.player);
                int pesquisas = knowledge.research().size();
                int aspectos = knowledge.discovered().size();
                System.out.println("[COMANDO] depois: " + pesquisas + " pesquisas, "
                        + aspectos + " aspectos descobertos");
                if (pesquisas < net.thaumcraft.research.Researches.ALL.size()) {
                    throw new AssertionError("o comando não destrancou tudo: só " + pesquisas + " de "
                            + net.thaumcraft.research.Researches.ALL.size());
                }
                if (aspectos < net.thaumcraft.api.aspects.Aspects.count()) {
                    throw new AssertionError("faltaram aspectos: só " + aspectos + " de "
                            + net.thaumcraft.api.aspects.Aspects.count());
                }
            });

            // e o livro aberto, com tudo à vista
            server.runCommand("give @p thaumcraft:thaumonomicon");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(10);
            context.takeScreenshot("comando_tudo_destrancado");
        }
    }
}
