package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.StructureTags;

/**
 * A muralha da aldeia, vista.
 *
 * <p><b>Esta prova procura uma aldeia de verdade, e não planta uma.</b> O {@code /place structure} desenha os
 * blocos mas <b>não registra a aldeia</b> no trecho, e o marcador da muralha precisa justamente disso: ele
 * pergunta ao mundo quais são as ruas da aldeia à volta dele. Posta à mão, a aldeia não tem ruas que ele possa
 * achar, e ele desiste — que é o certo, e foi o que a primeira tentativa desta foto mostrou.
 *
 * <p>Por isso aqui se pede ao gerador a aldeia mais perto, vai-se até ela e se espera. O marcador aguarda
 * quarenta tiques depois de o trecho nascer; a espera larga é para a aldeia inteira assentar antes.
 *
 * <p><b>E o mundo é um mundo normal, de semente fixa.</b> O mundo das provas de cliente é <b>superplano</b> por
 * omissão — é o que faz as fotos ficarem iguais entre execuções —, e num superplano não nasce aldeia nenhuma.
 * A semente é escrita à mão para esta foto continuar a ser a mesma de uma vez para a outra.
 */
public class OccultaVillageWallClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(ajuste -> {
                    var presets = ajuste.getSettings().worldgenLoadContext()
                            .lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET);
                    ajuste.setWorldType(new net.minecraft.client.gui.screens.worldselection
                            .WorldCreationUiState.WorldTypeEntry(presets.getOrThrow(
                                    net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL)));
                    ajuste.setGenerateStructures(true);
                    ajuste.setSeed("muralha");
                })
                .create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // onde é a aldeia mais perto
            int[] onde = new int[3];
            server.runOnServer(s -> {
                var level = s.overworld();
                BlockPos aldeia = level.findNearestMapStructure(
                        StructureTags.VILLAGE, BlockPos.ZERO, 100, false);
                if (aldeia == null) throw new AssertionError("não achei aldeia nenhuma em cem trechos");
                onde[0] = aldeia.getX();
                onde[1] = aldeia.getY();
                onde[2] = aldeia.getZ();
            });

            context.runOnClient(minecraft -> minecraft.options.renderDistance().set(16));
            server.runCommand("tp @a " + onde[0] + " 150 " + onde[2]);
            context.waitTicks(300);

            server.runCommand("tp @a " + (onde[0] + 90) + " " + (onde[1] + 100) + " " + (onde[2] + 90)
                    + " facing " + onde[0] + " " + onde[1] + " " + onde[2]);
            context.waitTicks(80);
            context.takeScreenshot("aldeia_murada");

        }
    }
}
