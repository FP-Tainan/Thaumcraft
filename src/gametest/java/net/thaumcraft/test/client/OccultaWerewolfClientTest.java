package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O Lobisomem, visto.
 *
 * <p>São <b>dez caixas</b> traduzidas do {@code ModelWolfman}, e a graça delas é que ele não é um homem com
 * cabeça de lobo: o tronco inclina-se para a frente, as pernas são de bicho — coxa e canela dobradas ao
 * contrário —, os braços caem até o chão e há uma cauda. Nenhuma prova de servidor sabe se uma dessas caixas
 * ficou do lado errado.
 *
 * <p>Ele entra na foto <b>ao lado de um aldeão</b>, que é o que ele era, e de um lobo do jogo, que é o que ele
 * não é.
 */
public class OccultaWerewolfClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("tp @p ~ ~-0.4 ~ 0 5");

            server.runCommand("summon thaumcraft:wolfman ~ ~ ~3 {NoAI:1b,Rotation:[180f,0f]}");
            server.runCommand("summon minecraft:villager ~-2 ~ ~3 {NoAI:1b,Rotation:[180f,0f]}");
            server.runCommand("summon minecraft:wolf ~2 ~ ~3 {NoAI:1b,Rotation:[180f,0f]}");
            context.waitTicks(30);
            context.takeScreenshot("ao_lobisomem");

            // e de lado, que é onde se vê o que ele tem de bicho: o tronco inclinado, a perna
            // dobrada ao contrário e a cauda. De frente, a perna de trás some atrás da da frente.
            // e de lado, só ele. Vira-se a CÂMARA, e não o bicho: o corpo de um bicho tem conta
            // própria e não respeita um giro à força, mas a câmara vai para onde se mandar.
            server.runCommand("kill @e[type=minecraft:wolf]");
            server.runCommand("kill @e[type=minecraft:villager]");
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                for (var bicho : s.overworld().getAllEntities()) {
                    if (!(bicho instanceof net.thaumcraft.occulta.wolf.WolfmanEntity lobo)) continue;
                    quem.teleportTo(s.overworld(), lobo.getX() + 3.5, lobo.getY() + 0.6, lobo.getZ(),
                            java.util.Set.of(), 90.0f, 8.0f, false);
                    break;
                }
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_lobisomem_de_lado");
        }
    }
}
