package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A escada dos dez graus na tela: o <b>Altar do Lobo</b>, a <b>Cabeça de Lobo</b> nas duas posições, o
 * <b>Caçador Cornudo</b> e a <b>Lança do Caçador</b> na mão.
 *
 * <p>É a prova que importa mais nesta fatia, porque o altar é <b>trinta e oito peças</b> montadas de cabeça
 * para baixo: um número trocado não falha prova nenhuma, só faz um lobo sair do lugar errado. Só se vê.
 *
 * <p>Tudo se mede a partir de um <b>marco</b> posto no princípio, e não do lugar de quem joga — senão cada
 * salto da máquina arrasta o seguinte e as fotos saem a olhar para o nada.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaWerewolfLadderClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // o marco, que é de onde tudo se mede
            server.runCommand("execute at @p run summon minecraft:marker ~ ~ ~ {Tags:[\"escada\"]}");

            // o Altar do Lobo, de frente: o senhor, a lança e os dois lobos aos pés
            põe(server, "~ ~ ~5 thaumcraft:werewolf_statue[facing=south]");
            olha(server, "~ ~ ~ 0 10");
            context.waitTicks(20);
            context.takeScreenshot("escada_1_altar_de_frente");

            // e de perto e de baixo, para se ver a cabeça de lobo dele
            olha(server, "~ ~ ~2.5 0 -10");
            context.waitTicks(20);
            context.takeScreenshot("escada_2_altar_de_perto");

            // de lado, que é onde os dois lobos se vêem separados
            olha(server, "~4 ~ ~5 90 5");
            context.waitTicks(20);
            context.takeScreenshot("escada_3_altar_de_lado");

            // a Cabeça de Lobo: uma no chão, numa pedra, e uma pregada numa parede
            põe(server, "~-4 ~ ~5 minecraft:stone");
            põe(server, "~-4 ~1 ~5 thaumcraft:mounted_wolf_head[rotation=0]");
            põe(server, "~-7 ~ ~5 minecraft:stone");
            põe(server, "~-7 ~1 ~5 minecraft:stone");
            põe(server, "~-7 ~1 ~4 thaumcraft:mounted_wolf_head_wall[facing=north]");
            olha(server, "~-5.5 ~ ~1.5 0 5");
            context.waitTicks(20);
            context.takeScreenshot("escada_4_cabecas");

            // o Caçador Cornudo, que é um homem com as proporções erradas
            server.runCommand(
                    "execute at @e[tag=escada,limit=1] run summon thaumcraft:horned_huntsman ~5 ~ ~5"
                            + " {NoAI:1b,Invul:0,Rotation:[180f,0f]}");
            olha(server, "~5 ~ ~-1 0 0");
            context.waitTicks(30);
            context.takeScreenshot("escada_5_cacador");

            // e de perto, para a galhada e a lança dele
            olha(server, "~5 ~ ~2 0 -5");
            context.waitTicks(20);
            context.takeScreenshot("escada_6_cacador_de_perto");

            // a Lança do Caçador na mão, com o Caçador fora do caminho
            server.runCommand("kill @e[type=thaumcraft:horned_huntsman]");
            olha(server, "~ ~ ~ 0 0");
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:huntsmans_spear");
            context.waitTicks(20);
            context.takeScreenshot("escada_7_lanca_na_mao");

            // e as cinco coisas no inventário, que é onde o altar e a cabeça se vêem inteiros
            server.runCommand("give @p thaumcraft:werewolf_statue");
            server.runCommand("give @p thaumcraft:mounted_wolf_head");
            server.runCommand("give @p thaumcraft:horn_of_the_hunt");
            server.runCommand("give @p thaumcraft:infernal_blood");
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("escada_8_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }

    /** Põe um bloco medido do marco. */
    private static void põe(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=escada,limit=1] run setblock " + resto);
    }

    /** E leva quem joga para um lugar medido do marco, a olhar para onde se diz. */
    private static void olha(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=escada,limit=1] run tp @p " + resto);
    }
}
