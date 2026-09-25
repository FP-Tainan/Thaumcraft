package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.MortuorumItems;

/**
 * O que quem joga viu torto no Ars Mortuorum: a foice virada para o lado errado, a Máquina de Costura sem pele, o
 * corpo pré-montado que não aparecia na mesa do altar, a cara do Isaac e as peles de quadros esticadas.
 *
 * <p>Não há o que conferir por conta própria numa tela: as fotos ficam em
 * {@code build/run/clientGameTest/screenshots} para quem estiver de olho.
 */
public class MortuorumLookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // ------------------------------------------------------------------ a foice, nos quatro lugares
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                inv.setItem(0, new ItemStack(MortuorumItems.SCYTHE_ITEM));
                inv.setItem(1, new ItemStack(MortuorumItems.SCYTHE_BONE_ITEM));
                inv.setItem(2, new ItemStack(MortuorumItems.SEWING_MACHINE));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(20);
            context.takeScreenshot("am_foice_primeira_pessoa");

            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            context.waitTicks(20);
            context.takeScreenshot("am_foice_terceira_pessoa");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("am_foice_de_frente");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON));

            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(1));
            context.waitTicks(20);
            context.takeScreenshot("am_foice_de_osso");

            // a foice no chão, que é o lugar ENTITY do original
            perto(server, "summon item ~ ~1 ~2 {Item:{id:\"thaumcraft:scythe\",count:1},NoGravity:1b}");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(8));
            perto(server, "tp @p ~ ~ ~ 0 0");
            context.waitTicks(20);
            context.takeScreenshot("am_foice_no_chao");
            perto(server, "kill @e[type=item]");

            // e no inventário, com os cinco itens de quadros ao lado
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                inv.setItem(3, new ItemStack(MortuorumItems.SCYTHE_ITEM));
                inv.setItem(4, pelaId("taint_shard"));
                inv.setItem(5, pelaId("triple_meat_treat"));
                inv.setItem(6, pelaId("wrath_shard"));
                inv.setItem(7, pelaId("pride_shard"));
                inv.setItem(8, pelaId("taint_charcoal"));
                minecraft.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            context.waitTicks(20);
            context.takeScreenshot("am_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));

            // ------------------------------------------------------------------ a Máquina de Costura
            perto(server, "setblock ~ ~ ~3 thaumcraft:sewing_machine");
            perto(server, "tp @p ~ ~ ~ 0 25");
            context.waitTicks(20);
            context.takeScreenshot("am_costura");
            perto(server, "tp @p ~-3 ~ ~3 -90 10");
            context.waitTicks(20);
            context.takeScreenshot("am_costura_de_lado");

            // ------------------------------------------------------------------ o corpo na mesa do altar
            perto(server, "setblock ~-3 ~ ~4 thaumcraft:summoning_altar[facing=south]");
            context.waitTicks(10);
            perto(server, "data merge block ~-3 ~ ~4 {Items:["
                    + "{Slot:2b,id:\"thaumcraft:villager_head\",count:1},"
                    + "{Slot:3b,id:\"thaumcraft:zombie_torso\",count:1},"
                    + "{Slot:4b,id:\"thaumcraft:enderman_legs\",count:1},"
                    + "{Slot:5b,id:\"thaumcraft:zombie_arm\",count:1},"
                    + "{Slot:6b,id:\"thaumcraft:zombie_arm\",count:1}]}");
            context.waitTicks(20);
            perto(server, "tp @p ~-3 ~1 ~ 0 20");
            context.waitTicks(20);
            context.takeScreenshot("am_altar_corpo");

            // ------------------------------------------------------------------ a cara do Isaac
            perto(server, "tp @p ~8 ~ ~-8 0 0");
            context.waitTicks(10);
            for (String qual : new String[]{"isaac_normal", "isaac_blood", "isaac_body", "isaac_head"}) {
                perto(server, "kill @e[type=thaumcraft:" + qual + "]");
                perto(server, "summon thaumcraft:" + qual + " ~ ~ ~3 {NoAI:1b}");
                // o corpo do bicho fica virado para o sul, que é o giro de fábrica; quem se muda é a câmara
                perto(server, "tp @p ~ ~ ~6 180 0");
                context.waitTicks(20);
                context.takeScreenshot("am_" + qual);
                perto(server, "tp @p ~ ~ ~-6 0 0");
                perto(server, "kill @e[type=thaumcraft:" + qual + "]");
            }

            // ------------------------------------------------------------------ as abas do Thaumonomicon
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                inv.setItem(0, pelaId("thaumonomicon"));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(10);
            // o comando pede nível de mestre do jogo; quem o corre é o servidor, no lugar de quem joga
            server.runCommand("execute as @p run thaumcraft pesquisa tudo");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.gameMode.useItem(minecraft.player,
                    net.minecraft.world.InteractionHand.MAIN_HAND));
            context.waitTicks(30);
            context.takeScreenshot("am_thaumonomicon_abas");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }

    /** Um item pelo nome, que é como se pega nos itens dos ramos sem lhes conhecer o campo. */
    private static ItemStack pelaId(String nome) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.thaumcraft.Thaumcraft.id(nome));
        if (item == null) throw new AssertionError("não há item thaumcraft:" + nome);
        return new ItemStack(item);
    }
}
