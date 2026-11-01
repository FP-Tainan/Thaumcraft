package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.DupGrenadeItem;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Sósia</b> visto, que é a única coisa desta fatia que só existe desenhada.
 *
 * <p>Ele tem de ter a <b>cara de quem o fez</b> — e a armadura dele. Uma foto de um sósia com a pele de
 * reserva não provaria nada; uma com o jogador ao lado da cópia dele prova tudo.
 *
 * <p>E uma segunda do <b>cajado e da granada</b> no inventário, com a dica da granada aberta, porque é
 * nela que está escrito de quem é a cara.
 */
public class OccultaDuplicationClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // o jogador de armadura, e o sósia dele ao lado
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                        new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
                jogador.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
                        new ItemStack(net.minecraft.world.item.Items.GOLDEN_CHESTPLATE));
                var mundo = (net.minecraft.server.level.ServerLevel) jogador.level();
                var sósia = OccultaEntities.DECOY.create(mundo, EntitySpawnReason.TRIGGERED);
                if (sósia == null) return;
                var onde = jogador.position().add(jogador.getLookAngle().scale(4.0));
                sósia.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);
                sósia.veste(jogador);
                sósia.põePrazo(Integer.MAX_VALUE);
                mundo.addFreshEntity(sósia);
            });
            // os avisos de conquista duram cinco segundos, e tapam a foto
            context.waitTicks(130);
            context.takeScreenshot("o_sosia");

            // e as duas coisas no inventário, com a dica da granada
            server.runCommand("gamemode survival @a");
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.getInventory().setItem(0, new ItemStack(OccultaItems.DUP_STAFF));
                jogador.getInventory().setItem(1, DupGrenadeItem.de(jogador));
                jogador.getInventory().setSelectedSlot(0);
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            // o rato em cima da granada, na segunda casa da barra, para a dica abrir
            context.getInput().setCursorPos(318, 374);
            context.waitTicks(20);
            context.takeScreenshot("o_cajado_e_a_granada");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
