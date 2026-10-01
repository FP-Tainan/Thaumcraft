package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellEffectEntity;

/**
 * A Nevasca e a Chuva de Fogo, vistas.
 *
 * <p>Esta foto existe porque as duas <b>não têm desenho nenhum</b>: a entidade delas é invisível, como a da
 * Zona. Tudo o que se vê delas é a coisa que cai de dez blocos acima — vinte flocos por batida numa, dez
 * chamas na outra. Se essa conta estivesse errada, as duas seriam dois círculos de nada, e as 840 provas de
 * servidor continuariam verdes.
 */
public class ArcanaWeatherClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set midnight");
            server.runCommand("tp @p 0 -54 -16 0 8");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();

                // uma de cada, lado a lado, para a foto mostrar as duas de uma vez
                põe(level, jogador, SpellEffectEntity.Kind.BLIZZARD, -6.0);
                põe(level, jogador, SpellEffectEntity.Kind.FIRE_RAIN, 6.0);
            });

            context.waitTicks(30);
            context.takeScreenshot("aa_temporais");
        }
    }

    private static void põe(ServerLevel level, net.minecraft.world.entity.LivingEntity quem,
                            SpellEffectEntity.Kind qual, double desvio) {
        var área = new SpellEffectEntity(level, quem, Spell.of(Shapes.SELF), qual);
        área.setWeather(qual);
        área.setRadius(4.0f);
        área.setLife(2000);
        área.snapTo(desvio, -59.0, 0.0);
        level.addFreshEntity(área);
    }
}
