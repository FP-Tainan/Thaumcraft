package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Diabrete</b>, de frente e de costas, solto e ligado.
 *
 * <p>O que importa nesta tela:
 *
 * <ul>
 *   <li>a <b>silhueta</b>: cabeça grande com dois chifres tortos e um nariz comprido virado para baixo,
 *       corpo baixo, braços que quase tocam o chão e pernas curtas;</li>
 *   <li>as <b>asas</b> chapadas de trás, que são a peça de que se desconfia — no original elas declaram
 *       outro tamanho de folha, e a conta dele não fecha;</li>
 *   <li>e o que o <b>Coração de Demônio</b> faz: ele <b>engorda</b> uma vez e meia de lado, e não cresce
 *       para cima.</li>
 * </ul>
 *
 * <p>Por isso são quatro: dois de frente — um solto e um ligado — e dois de costas, para as asas se
 * verem.
 */
public class OccultaImpClientTest implements FabricClientGameTest {
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

                for (int dx = -5; dx <= 5; dx++) {
                    for (int dz = 2; dz <= 10; dz++) {
                        level.setBlockAndUpdate(meio.offset(dx, -1, dz),
                                Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                }

                // os dois da frente olham para cá; os dois de trás, para longe
                põe(level, meio.offset(-3, 0, 8), 180.0f, false);
                põe(level, meio.offset(-1, 0, 8), 180.0f, true);
                põe(level, meio.offset(1, 0, 8), 0.0f, false);
                põe(level, meio.offset(3, 0, 8), 0.0f, true);

                player.snapTo(meio.getX() + 0.5, meio.getY() + 2.0, meio.getZ(), 0.0f, 6.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("os_diabretes");
        }
    }

    private static void põe(net.minecraft.server.level.ServerLevel level, BlockPos onde, float rumo,
                            boolean ligado) {
        var bicho = OccultaEntities.IMP.create(level, EntitySpawnReason.COMMAND);
        if (bicho == null) return;
        bicho.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, rumo, 0.0f);
        bicho.setYBodyRot(rumo);
        bicho.setYHeadRot(rumo);
        bicho.setPersistenceRequired();
        bicho.setNoAi(true);
        level.addFreshEntity(bicho);
        if (!ligado) return;
        // o Coração de Demônio, sem a mão de ninguém
        var dono = level.getServer().getPlayerList().getPlayers().getFirst();
        bicho.tame(dono);
        bicho.mobInteract(dono, net.minecraft.world.InteractionHand.OFF_HAND);
        dono.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,
                new ItemStack(OccultaItems.DEMON_HEART));
        bicho.mobInteract(dono, net.minecraft.world.InteractionHand.OFF_HAND);
        dono.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }
}
