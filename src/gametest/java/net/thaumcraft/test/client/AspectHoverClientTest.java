package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** O gesto do original: agachando sobre um item no inventário, os símbolos do que ele é feito aparecem no cursor. */
public class AspectHoverClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                player.getInventory().setItem(0, new ItemStack(Items.GOLDEN_APPLE));
                // examinado, como o original exige antes de mostrar
                var knowledge = net.thaumcraft.research.Knowledges.of(player);
                knowledge.markScanned(net.thaumcraft.research.ScanManager.keyOf(new ItemStack(Items.GOLDEN_APPLE)));
                for (var aspect : net.thaumcraft.api.aspects.ObjectAspects.of(new ItemStack(Items.GOLDEN_APPLE)).getAspects()) {
                    knowledge.discover(aspect);
                }
                net.thaumcraft.research.Knowledges.save(player, knowledge);
            });
            context.waitTicks(20);
            // abre o inventário e põe o cursor sobre a primeira casa da barra
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(10);
            double[] at = context.computeOnClient(minecraft -> {
                double scale = minecraft.getWindow().getGuiScale();
                int left = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int top = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                // a primeira casa da barra rápida do inventário
                return new double[]{(left + 8 + 8) * scale, (top + 142 + 8) * scale};
            });
            context.getInput().setCursorPos(at[0], at[1]);
            context.waitTicks(5);
            context.takeScreenshot("aspectos_sem_shift");
            context.getInput().holdShift();
            context.waitTicks(5);
            context.takeScreenshot("aspectos_com_shift");
        }
    }
}
