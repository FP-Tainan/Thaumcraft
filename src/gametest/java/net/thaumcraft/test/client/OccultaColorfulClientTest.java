package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEffects;

/**
 * O <b>Colorido</b> visto: oito ovelhas, cada uma de uma cor, e nenhuma delas fazendo nada.
 *
 * <p>É a única coisa desta fatia que mora do lado de quem joga, e é a melhor piada do Witchery: o cozimento
 * mais difícil de acertar sem efeito nenhum. Sem esta tela, não haveria como saber se ele funciona.
 */
public class OccultaColorfulClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                // oito porcos, cada um com um grau da poção: oito das dezesseis tintas
                for (int qual = 0; qual < 8; qual++) {
                    var porco = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
                    if (porco == null) continue;
                    porco.snapTo(meio.getX() + 0.5 + (qual - 3.5) * 1.8, meio.getY(), meio.getZ() + 6.5,
                            180.0f, 0.0f);
                    porco.setPersistenceRequired();
                    porco.setNoAi(true);
                    // o grau é a tinta: o primeiro é branco, e daí em diante as dezesseis do jogo
                    porco.addEffect(new MobEffectInstance(OccultaEffects.COLORFUL, 6000, qual, false, false));
                    level.addFreshEntity(porco);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            // a cor viaja num apego posto de segundo em segundo: é preciso dar-lhe esse segundo
            context.waitTicks(60);
            context.takeScreenshot("oito_porcos_coloridos");
        }
    }
}
