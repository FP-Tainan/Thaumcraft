package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Os oito golens em fila, para ver o corpo e a pele de cada matéria. */
public class GolemClientTest implements FabricClientGameTest {
    private static final String[] MATERIAIS = {
            "straw", "wood", "tallow", "clay", "flesh", "stone", "iron", "thaumium",
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule doMobSpawning false");

            // um chão claro e a vista de frente
            server.runCommand("execute at @p run fill ~-8 ~-1 ~2 ~8 ~-1 ~8 minecraft:smooth_stone");
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 8");

            // um golem de cada matéria, em fila
            for (int i = 0; i < MATERIAIS.length; i++) {
                int x = i - 4;
                server.runCommand("execute at @p run summon thaumcraft:golem ~" + x + " ~ ~5"
                        + " {material:\"" + MATERIAIS[i] + "\",NoAI:1b,Rotation:[180f,0f]}");
            }
            context.waitTicks(40);
            context.takeScreenshot("golens_em_fila");

            // e um de perto, com o núcleo de juntar
            server.runCommand("execute at @p run tp @s ~ ~ ~2 0 5");
            context.waitTicks(20);
            context.takeScreenshot("golem_de_perto");

            context.runOnClient(minecraft -> {
                long quantos = minecraft.level.entitiesForRendering() == null ? 0
                        : java.util.stream.StreamSupport
                                .stream(minecraft.level.entitiesForRendering().spliterator(), false)
                                .filter(e -> e instanceof net.thaumcraft.entity.GolemEntity)
                                .count();
                System.out.println("[GOLEM] o cliente está desenhando " + quantos + " golens");
            });
        }
    }
}
