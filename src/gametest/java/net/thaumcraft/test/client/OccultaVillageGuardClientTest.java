package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Guarda da Aldeia</b>, visto.
 *
 * <p>O corpo dele é uma mistura de propósito — tronco e braços de gente, para a armadura assentar; cabeça alta de
 * aldeão, com nariz; e uma túnica meio ponto mais larga por cima do tronco. É o {@code ModelVillageGuard} do
 * Witchery, e é o que o faz ler como <b>aldeão armado</b> e não como pessoa nem como aldeão.
 *
 * <p>Duas fotos. Na primeira, <b>três guardas comuns em fila</b>: a armadura de cada um sai sorteada, e uma em
 * cinco vezes o peito e a cabeça vêm de malha em vez de couro — com três lado a lado costuma-se ver a diferença,
 * que é justamente o que faz uma aldeia guardada não parecer uniformizada.
 *
 * <p>Na segunda, um guarda <b>de frente e de perto</b>, que é onde a mistura se lê: a cabeça alta com o nariz, a
 * túnica por cima do tronco e a armadura por cima dela.
 *
 * <p><b>E o infernal não aparece em foto nenhuma, de propósito.</b> Ele é maior só na <b>caixa</b> — 0,72 por
 * 2,34 contra 0,6 por 1,8 —, e não no desenho: o {@code RenderVillageGuard} do original é apenas
 * {@code super(new ModelVillageGuard(), 0.5F)}, sem escala, e por isso lá ele também se desenha do tamanho de
 * gente. Fotografá-lo ao lado do comum mostraria dois guardas iguais e faria parecer defeito o que é fidelidade.
 */
public class OccultaVillageGuardClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 0 0");

            // três comuns em fila, para se ver o sorteio da armadura
            server.runCommand("summon thaumcraft:village_guard ~-2 ~ ~6");
            server.runCommand("summon thaumcraft:village_guard ~ ~ ~6");
            server.runCommand("summon thaumcraft:village_guard ~2 ~ ~6");
            context.waitTicks(20);
            context.takeScreenshot("guarda_da_aldeia");

            // e um de frente e de perto, que é onde a cabeça, o nariz e a túnica se leem
            server.runCommand("kill @e[type=thaumcraft:village_guard]");
            server.runCommand("summon thaumcraft:village_guard ~ ~ ~3 {Rotation:[180f,0f]}");
            context.waitTicks(20);
            context.takeScreenshot("guarda_de_perto");
        }
    }
}
