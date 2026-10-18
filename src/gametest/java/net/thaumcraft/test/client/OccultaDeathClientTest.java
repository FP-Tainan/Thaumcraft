package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>Morte</b> vista, e <b>quem a veste</b>.
 *
 * <p>Duas coisas que só se veem: ela, com o manto que a cobre inteira e a <b>foice ao ombro</b> — que é a
 * peça que a faz —, e as <b>roupas dela num jogador</b>, com o capuz, o manto e as mangas curtas que
 * deixam a mão de fora.
 *
 * <p>É a foto que prova a melhor ideia do ramo: as duas silhuetas têm de ser a <b>mesma</b>, porque quem
 * veste as roupas da Morte é a Morte.
 */
public class OccultaDeathClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            double[] onde = server.computeOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = jogador.blockPosition();

                var ela = OccultaEntities.DEATH.create(mundo, EntitySpawnReason.COMMAND);
                if (ela != null) {
                    ela.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5, 180.0f, 0.0f);
                    ela.setYBodyRot(180.0f);
                    ela.setYHeadRot(180.0f);
                    ela.setPersistenceRequired();
                    ela.setNoAi(true);
                    ela.setSilent(true);
                    mundo.addFreshEntity(ela);
                }
                jogador.setDeltaMovement(Vec3.ZERO);
                return new double[]{meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5};
            });

            olha(server, onde[0], onde[1] + 1.2, onde[2] - 4.0, 0.0f, 0.0f);
            context.waitTicks(30);
            context.takeScreenshot("a_morte_de_frente");

            olha(server, onde[0] + 4.0, onde[1] + 1.2, onde[2], 90.0f, 0.0f);
            context.waitTicks(20);
            context.takeScreenshot("a_morte_de_lado");

            // e as roupas dela num jogador, que é a mesma silhueta
            server.runCommand("gamemode survival");
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.DEATHS_HOOD));
                jogador.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.DEATHS_ROBE));
                jogador.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.DEATHS_BOOTS));
                jogador.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                        new ItemStack(OccultaItems.DEATHS_HAND));
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("quem_veste_as_roupas_da_morte");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Põe a câmara naquele ponto, olhando para aquele rumo. */
    private static void olha(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             double x, double y, double z, float guinada, float passo) {
        server.runCommand(String.format(java.util.Locale.ROOT, "tp @p %.1f %.1f %.1f %.1f %.1f",
                x, y, z, guinada, passo));
    }
}
