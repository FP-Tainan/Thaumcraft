package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.thaumcraft.world.DynamicDimensions;

/**
 * A prova que faltava antes das Portas Dimensionais: abrir um mundo com o jogo andando e mandar alguém para lá.
 *
 * <p>O lado do servidor já se sabia que dava ({@code DynamicDimensionGameTest}); o que se põe à prova aqui é o
 * lado de quem vê — se o cliente aceita um mundo que não estava na lista que ele recebeu ao entrar.
 */
public class DynamicDimensionClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var chave = DynamicDimensions.key("test_pocket");
                var gerador = new net.minecraft.world.level.levelgen.FlatLevelSource(
                        net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                                s.registryAccess().lookupOrThrow(Registries.BIOME),
                                s.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                                s.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)));
                var bolso = DynamicDimensions.getOrCreate(s, chave, BuiltinDimensionTypes.OVERWORLD, gerador);
                if (bolso == null) throw new IllegalStateException("o bolso não abriu");

                // uma marca para se ver que se chegou mesmo lá
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        bolso.setBlockAndUpdate(new BlockPos(dx, 64, dz), Blocks.AMETHYST_BLOCK.defaultBlockState());
                    }
                }
                bolso.setBlockAndUpdate(new BlockPos(0, 65, -3), Blocks.SEA_LANTERN.defaultBlockState());

                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.teleportTo(bolso, 0.5, 65.0, 3.5, java.util.Set.of(), 180.0f, 20.0f, false);
            });
            context.waitTicks(60);
            context.takeScreenshot("bolso_dinamico");

            // e de volta para casa, que é o que a porta faz ao sair
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.teleportTo(s.overworld(), jogador.getX(), 80.0, jogador.getZ(),
                        java.util.Set.of(), 180.0f, 0.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("bolso_de_volta");
        }
    }
}
