package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * O <b>Espírito</b>, em fila e em quatro cores.
 *
 * <p>O que importa nesta tela são três coisas, e nenhuma delas é o boneco:
 *
 * <ul>
 *   <li>o <b>tamanho</b> — um quarto de bloco de lado, encolhido a metade pelo próprio desenhista, de modo
 *       que ele é uma coisa pequena pousada junto ao chão, e não um bicho;</li>
 *   <li>a <b>transparência</b> de seis décimos, que o faz parecer uma lanterna de papel;</li>
 *   <li>e o <b>pó</b> que ele larga todo tique, que é o que de verdade se vê dele de longe — dourado por
 *       omissão, e da cor que lhe derem se lhe derem uma.</li>
 * </ul>
 *
 * <p>Por isso há quatro aqui: um sem cor, que sai dourado, e três pintados.
 */
public class OccultaSpiritEntityClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("difficulty peaceful");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                double[] onde = {-1.2, -0.4, 0.4, 1.2};
                int[] cores = {0, 0xFF4444, 0x44FF44, 0x4444FF};

                for (int i = 0; i < onde.length; i++) {
                    var bicho = OccultaEntities.SPIRIT.create(level, EntitySpawnReason.COMMAND);
                    if (bicho == null) continue;
                    bicho.snapTo(meio.getX() + 0.5 + onde[i], meio.getY() + 0.8, meio.getZ() + 2.0,
                            180.0f, 0.0f);
                    bicho.setYBodyRot(180.0f);
                    bicho.setYHeadRot(180.0f);
                    bicho.cor(cores[i]);
                    bicho.setPersistenceRequired();
                    bicho.setNoAi(true);
                    level.addFreshEntity(bicho);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.2, meio.getZ(), 0.0f, 8.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("os_espiritos");
        }
    }
}
