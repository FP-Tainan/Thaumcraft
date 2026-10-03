package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A vassoura, vista.
 *
 * <p>São <b>dez caixas</b> traduzidas do {@code ModelBroom} — um cabo e nove cerdas em leque, cada uma com a
 * sua inclinação — e nenhuma prova de servidor sabe se uma delas ficou do lado errado.
 *
 * <p>E há a <b>tinta</b>: só as cerdas a levam, e a tabela de cores é a da lã de 2014 e não a do jogo. Uma
 * vassoura pintada de roxo ao lado de uma por pintar é a única forma de se ver que essas duas coisas valem.
 */
public class OccultaBroomClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("tp @p ~ ~1 ~-1 0 15");

            // três: por pintar, de roxo e de vermelho, lado a lado
            server.runOnServer(s -> {
                var level = s.overworld();
                var quem = s.getPlayerList().getPlayers().getFirst();
                int[] cores = {-1, net.minecraft.world.item.DyeColor.PURPLE.getId(),
                        net.minecraft.world.item.DyeColor.RED.getId()};
                for (int i = 0; i < cores.length; i++) {
                    var vassoura = net.thaumcraft.occulta.OccultaEntities.BROOM.create(level,
                            net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                    if (vassoura == null) continue;
                    vassoura.snapTo(quem.getX() - 2.0 + i * 2.0, quem.getY() - 1.0, quem.getZ() + 4.0,
                            0.0f, 0.0f);
                    vassoura.pinta(cores[i]);
                    level.addFreshEntity(vassoura);
                }
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_vassouras");
        }
    }
}
