package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.SeerStoneItem;
import net.thaumcraft.occulta.coven.Coven;
import net.thaumcraft.occulta.coven.CovenSummon;

import java.util.UUID;

/**
 * A <b>Pedra do Vidente</b> vista: o coven chegando, e a leitura no bate-papo.
 *
 * <p>São duas coisas que só existem desenhadas. O <b>coven</b> é a única vez em que se veem cinco bruxas
 * do ofício juntas, cada uma com uma cara diferente — e é esse o ponto delas: um coven de cinco tem de
 * parecer cinco pessoas e não cinco cópias.
 *
 * <p>E a <b>leitura</b> é texto, mas é texto que ninguém mais escreve: a ficha oculta de uma pessoa, em
 * azul, numa linha só.
 */
public class OccultaSeerStoneClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // cinco bruxas na lista, e as cinco chamadas de uma vez para a foto
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.getInventory().setSelectedSlot(0);
                jogador.getInventory().setItem(0, new ItemStack(OccultaItems.SEER_STONE));
                String[] nomes = {"Agnes Nutter", "Esme Weatherwax", "Gytha Ogg",
                        "Magrat Garlick", "Tiffany Aching"};
                for (int qual = 0; qual < nomes.length; qual++) {
                    Coven.junta(jogador, new Coven.Membro(UUID.randomUUID(), nomes[qual], qual, -1));
                }
                /*
                 * Elas vão em fila para a foto. Chamadas todas para o mesmo ponto, ficariam umas dentro
                 * das outras — o que é o que acontece no jogo e não serve para mostrar as cinco caras.
                 */
                var meio = jogador.position().add(jogador.getLookAngle().scale(8.0));
                var lado = new net.minecraft.world.phys.Vec3(-jogador.getLookAngle().z, 0.0,
                        jogador.getLookAngle().x).normalize();
                var mundo = (net.minecraft.server.level.ServerLevel) jogador.level();
                for (int qual = 0; qual < nomes.length; qual++) {
                    CovenSummon.chama(mundo, jogador, meio.add(lado.scale(qual * 2.0 - 4.0).add(0.0, 0.0, 0.0)), qual);
                }
            });
            context.waitTicks(30);
            context.takeScreenshot("o_coven_chamado");

            // e a leitura, que é a ficha oculta de quem se olha
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                SeerStoneItem.lê(jogador, jogador);
            });
            context.waitTicks(20);
            context.takeScreenshot("a_leitura_da_pedra");
        }
    }
}
