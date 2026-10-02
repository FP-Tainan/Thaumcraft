package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * O <b>Coven do Pântano</b>, visto — e visto <b>gerado</b>, não posto à mão.
 *
 * <p>A primeira tentativa desta foto plantou o coven com {@code /place template} na altura do mapa de alturas,
 * e ele saiu <b>flutuando sobre a copa das árvores</b>: num pântano fechado, a altura de superfície depois das
 * árvores é o alto delas. A estrutura de verdade usa a altura de <b>antes</b> das árvores e abre o mato com o
 * {@code terrain_adaptation} — mas isso só se vê deixando-a nascer.
 *
 * <p>Por isso aqui se pergunta ao gerador onde está o coven mais perto, vai-se lá e fotografa-se. É a mesma
 * lição da muralha: o que se põe à mão não prova o caminho que o jogo usa.
 */
public class OccultaCovenVillageClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(ajuste -> {
                    var presets = ajuste.getSettings().worldgenLoadContext()
                            .lookupOrThrow(Registries.WORLD_PRESET);
                    ajuste.setWorldType(new net.minecraft.client.gui.screens.worldselection
                            .WorldCreationUiState.WorldTypeEntry(presets.getOrThrow(
                                    net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL)));
                    ajuste.setGenerateStructures(true);
                    ajuste.setSeed("coven");
                })
                .create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            int[] onde = new int[3];
            server.runOnServer(s -> {
                var level = s.overworld();
                var chave = ResourceKey.create(Registries.STRUCTURE, Identifier.parse("thaumcraft:swamp_coven"));
                var estrutura = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                        .getOrThrow(chave);
                var achado = level.getChunkSource().getGenerator().findNearestMapStructure(
                        level, HolderSet.direct(estrutura), BlockPos.ZERO, 200, false);
                if (achado == null) throw new AssertionError("não achei coven nenhum em duzentos trechos");
                onde[0] = achado.getFirst().getX();
                onde[1] = achado.getFirst().getY();
                onde[2] = achado.getFirst().getZ();
            });

            context.runOnClient(minecraft -> minecraft.options.renderDistance().set(12));
            server.runCommand("tp @a " + onde[0] + " 150 " + onde[2]);
            context.waitTicks(200);

            server.runOnServer(s -> {
                onde[1] = s.overworld().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                        onde[0], onde[2]);
            });
            server.runCommand("tp @a " + (onde[0] + 30) + " " + (onde[1] + 25) + " " + (onde[2] + 30)
                    + " facing " + onde[0] + " " + onde[1] + " " + onde[2]);
            context.waitTicks(80);
            context.takeScreenshot("coven_do_pantano");
        }
    }
}
