package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.wolf.Werewolf;

/**
 * O jogador transformado, visto de fora: gente, lobo e lobisomem.
 *
 * <p>É a prova de que a transformação <b>se vê</b>. O resto da fatia pode estar certo e a licantropia não
 * existir de todo para quem joga: um lobisomem que continuasse a parecer gente seria só um conjunto de
 * números.
 */
public class OccultaWerewolfPlayerClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            // de gente, para se ver o que ele era
            context.runOnClient(minecraft -> minecraft.options.setCameraType(
                    net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("lobisomem_1_de_gente");

            // de lobo, com um lobo de verdade ao lado para o tamanho se conferir
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                player.setDeltaMovement(Vec3.ZERO);
                Werewolf.grau(player, 10);
                /*
                 * O Amuleto da Lua na mochila: sem ele o relógio da lua desfaz a forma em dois segundos, que
                 * é exatamente o que ele faz no jogo. A tela precisa que a forma fique de pé.
                 */
                player.getInventory().add(new net.minecraft.world.item.ItemStack(
                        net.thaumcraft.occulta.OccultaItems.MOON_CHARM));
                Werewolf.forma(player, Werewolf.Forma.LOBO);

                var lobo = net.minecraft.world.entity.EntityTypes.WOLF.create(s.overworld(),
                        net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (lobo != null) {
                    lobo.snapTo(player.getX() + 2.0, player.getY(), player.getZ(), 180.0f, 0.0f);
                    lobo.setYBodyRot(180.0f);
                    lobo.setNoAi(true);
                    lobo.setPersistenceRequired();
                    s.overworld().addFreshEntity(lobo);
                }
            });
            // o estouro da transformação é uma nuvem branca: dar-lhe tempo de passar
            context.waitTicks(60);
            context.takeScreenshot("lobisomem_2_de_lobo");

            // e de lobisomem
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                player.setDeltaMovement(Vec3.ZERO);
                Werewolf.forma(player, Werewolf.Forma.LOBISOMEM);
            });
            context.waitTicks(60);
            context.takeScreenshot("lobisomem_3_de_lobisomem");

            // e de volta a gente, para se ver que a volta também funciona
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                Werewolf.grau(player, 0);
            });
            context.waitTicks(20);
            context.takeScreenshot("lobisomem_4_de_volta");

            context.runOnClient(minecraft -> minecraft.options.setCameraType(
                    net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);
        }
    }
}
