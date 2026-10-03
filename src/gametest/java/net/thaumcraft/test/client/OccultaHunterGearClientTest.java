package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O apetrecho do Caçador visto: as roupas vestidas, os cinco virotes e a besta de mão.
 *
 * <p>As roupas são o que esta fatia tem de visual, e são <b>duas peles</b>: uma para o corpo e outra para as
 * pernas. A prateada e a da aurora se distinguem pela <b>marca por cima do ícone</b>, que é como o original as
 * distingue — na pele vestida elas são iguais, e é de propósito: quem olha um caçador de longe não sabe de que
 * ele anda armado.
 */
public class OccultaHunterGearClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.HUNTER_HAT));
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.HUNTER_COAT));
                player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(OccultaItems.HUNTER_LEGS));
                player.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.HUNTER_BOOTS));

                player.getInventory().setItem(0, new ItemStack(OccultaItems.CROSSBOW_PISTOL));
                player.getInventory().setItem(1, new ItemStack(OccultaItems.STAKE_BOLT, 16));
                player.getInventory().setItem(2, new ItemStack(OccultaItems.ANTI_MAGIC_BOLT, 16));
                player.getInventory().setItem(3, new ItemStack(OccultaItems.HOLY_BOLT, 16));
                player.getInventory().setItem(4, new ItemStack(OccultaItems.SPLITTING_BOLT, 16));
                player.getInventory().setItem(5, new ItemStack(OccultaItems.SILVER_BOLT, 16));
                player.getInventory().setItem(6, new ItemStack(OccultaItems.SILVERED_HUNTER_COAT));
                player.getInventory().setItem(7, new ItemStack(OccultaItems.GARLICKED_HUNTER_COAT));
            });
            context.waitTicks(20);

            // o caçador vestido, visto de fora: é a pele que esta fatia traz
            context.runOnClient(minecraft -> minecraft.options.setCameraType(
                    net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(10);
            context.takeScreenshot("cacador_de_fora");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(
                    net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);

            // e o caçador vestido, visto de frente
            context.runOnClient(minecraft ->
                    minecraft.setScreenAndShow(
                            new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(10);
            context.takeScreenshot("cacador_vestido");

            // e a dica do casaco, com o que o conjunto dá e o que ele tira
            double[] onde = context.computeOnClient(minecraft -> {
                double escala = minecraft.getWindow().getGuiScale();
                int esquerda = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int alto = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                // a sétima casa da barra rápida: o casaco prateado
                return new double[]{(esquerda + 8 + 18 * 6 + 8) * escala, (alto + 142 + 8) * escala};
            });
            context.getInput().setCursorPos(onde[0], onde[1]);
            context.waitTicks(5);
            context.takeScreenshot("casaco_prateado_diz_o_que_faz");
        }
    }
}
