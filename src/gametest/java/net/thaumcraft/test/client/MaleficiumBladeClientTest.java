package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As lâminas de fortaleza na mão e no inventário, com a bainha ao lado e as runas da inscrição, e o Medidor Rúnico
 * que aparece por cima da barra.
 */
public class MaleficiumBladeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("give @p thaumcraft:shadowmetal_fortress_blade[thaumcraft:katana_inscription=0]");
            server.runCommand("give @p thaumcraft:thaumium_fortress_blade");
            server.runCommand("give @p thaumcraft:voidmetal_fortress_blade");
            server.runCommand("give @p thaumcraft:gate_key");
            server.runCommand("give @p thaumcraft:thaumic_disassembler");
            server.runCommand("give @p thaumcraft:primal_blade");
            context.waitTicks(20);
            context.takeScreenshot("lamina_na_mao");
            context.runOnClient(minecraft -> {
                if (minecraft.player != null) minecraft.player.getInventory().setSelectedSlot(1);
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
            // e a bainha na cintura, de costas
            // a lâmina na mão, de trás e de frente, que é como quem joga se vê
            context.runOnClient(minecraft -> {
                minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                if (minecraft.player != null) minecraft.player.getInventory().setSelectedSlot(0);
            });
            context.waitTicks(20);
            context.takeScreenshot("lamina_na_mao_de_tras");
            // e uma solta no chão, onde o jeito de desenhar é o mais simples de todos
            server.runCommand("summon item ~ ~1 ~2 {Item:{id:\"thaumcraft:shadowmetal_fortress_blade\",count:1}}");
            context.waitTicks(20);
            context.takeScreenshot("lamina_no_chao");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("lamina_na_mao_de_frente");
            context.runOnClient(minecraft -> {
                if (minecraft.player != null) minecraft.player.getInventory().setSelectedSlot(4);
            });
            context.waitTicks(20);
            context.takeScreenshot("bainha_na_cintura");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.waitTicks(5);
            context.takeScreenshot("laminas_no_inventario");
            // e as páginas do livro das últimas pesquisas, com as receitas que elas mostram
            server.runCommand("thaumcraft pesquisa tudo");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.thaumcraft.client.gui.ResearchPageScreen(new net.thaumcraft.client.gui.ThaumonomiconScreen(),
                            net.thaumcraft.research.Researches.get("THAUMIUMKATANA"), 1)));
            context.waitTicks(20);
            context.takeScreenshot("pagina_lamina_taumio");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.thaumcraft.client.gui.ResearchPageScreen(new net.thaumcraft.client.gui.ThaumonomiconScreen(),
                            net.thaumcraft.research.Researches.get("INSCRIPTIONFIRE"), 1)));
            context.waitTicks(20);
            context.takeScreenshot("pagina_inscricao");
        }
    }
}
