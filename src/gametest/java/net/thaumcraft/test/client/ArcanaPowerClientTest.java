package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A maquinaria da rede de Etherium vista de perto: o Obelisco aceso e apagado, o Conduto com o cristal, a
 * Bateria, o anel de giz e as duas flores — e a tela do Obelisco.
 *
 * <p>Metade destas peças desenha-se à mão, com malhas lidas de arquivos {@code .obj} do original. Uma
 * malha que não se lê não rebenta nada: ela simplesmente não aparece, e nenhuma prova de servidor dá por
 * isso.
 */
public class ArcanaPowerClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("gamerule doDaylightCycle false");
            server.runCommand("weather clear");

            // o obelisco inteiro: anel de giz e quatro pilares de tijolo com o topo cinzelado
            server.runCommand("setblock ~ ~ ~-6 thaumcraft:obelisk");
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    server.runCommand("setblock ~" + i + " ~ ~" + (-6 + j) + " thaumcraft:wizard_chalk");
                }
            }
            for (int x = -2; x <= 2; x += 4) {
                for (int z = -2; z <= 2; z += 4) {
                    server.runCommand("setblock ~" + x + " ~ ~" + (-6 + z) + " minecraft:stone_bricks");
                    server.runCommand("setblock ~" + x + " ~1 ~" + (-6 + z) + " minecraft:stone_bricks");
                    server.runCommand("setblock ~" + x + " ~2 ~" + (-6 + z) + " minecraft:chiseled_stone_bricks");
                }
            }
            // e, ao lado, um conduto e uma bateria
            server.runCommand("setblock ~4 ~ ~-6 thaumcraft:essence_conduit[facing=up]");
            server.runCommand("setblock ~6 ~ ~-6 thaumcraft:mana_battery");
            server.runCommand("tp @p ~ ~2 ~ 180 10");
            context.waitTicks(40);
            context.takeScreenshot("arcana_obelisco");

            // o mesmo obelisco a arder, que muda de pele e ganha as runas por cima
            server.runCommand("data merge block ~ ~ ~-6 {Items:[{Slot:0b,id:\"thaumcraft:vinteum_dust\",count:8}]}");
            context.waitTicks(40);
            context.takeScreenshot("arcana_obelisco_aceso");

            // o conduto num pedestal, à altura dos olhos e a três casas: é a peça mais miudinha
            server.runCommand("setblock ~ ~ ~-3 minecraft:stone");
            server.runCommand("setblock ~ ~1 ~-3 thaumcraft:essence_conduit[facing=up]");
            server.runCommand("setblock ~2 ~ ~-3 minecraft:stone");
            server.runCommand("setblock ~2 ~1 ~-3 thaumcraft:mana_battery");
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(20);
            context.takeScreenshot("arcana_conduto");

            // as duas flores, de perto
            server.runCommand("setblock ~-1 ~ ~-3 minecraft:sand");
            server.runCommand("setblock ~-1 ~1 ~-3 thaumcraft:desert_nova");
            server.runCommand("setblock ~1 ~ ~-3 thaumcraft:cerublossom");
            server.runCommand("tp @p ~ ~1 ~ 180 20");
            context.waitTicks(20);
            context.takeScreenshot("arcana_flores");

            // a barra com a chave, o giz e os três blocos na mão
            String[] mão = {"crystal_wrench", "wizard_chalk", "obelisk", "essence_conduit", "mana_battery"};
            for (int i = 0; i < mão.length; i++) {
                server.runCommand("item replace entity @p hotbar." + i + " with thaumcraft:" + mão[i]);
            }
            context.waitTicks(20);
            context.takeScreenshot("arcana_rede_itens");

            // e a tela do Obelisco, com o mostrador a meio da queima
            server.runOnServer(s2 -> {
                var jogador = s2.getPlayerList().getPlayers().getFirst();
                var nível = (net.minecraft.server.level.ServerLevel) jogador.level();
                var onde = jogador.blockPosition().north(6);
                if (nível.getBlockEntity(onde) instanceof net.thaumcraft.arcana.machine.ObeliskBlockEntity obelisco) {
                    jogador.openMenu(obelisco);
                }
            });
            context.waitTicks(20);
            context.takeScreenshot("arcana_obelisco_tela");
        }
    }
}
