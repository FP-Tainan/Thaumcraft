package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** O altar de infusão montado, para ver a matriz, os pedestais e o que eles seguram. */
public class InfusionClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // um chão claro na frente, e a vista de frente
            server.runCommand("execute at @p run fill ~-6 ~-1 ~2 ~6 ~-1 ~11 minecraft:smooth_stone");
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 12");

            // o esqueleto do altar do original: pedestal no meio, tijolos de pedra arcana nos cantos, pedra arcana
            // em cima deles, matriz dois acima do pedestal
            server.runCommand("execute at @p run setblock ~ ~ ~7 thaumcraft:pedestal");
            for (String corner : new String[]{"~-1 %s ~6", "~1 %s ~6", "~-1 %s ~8", "~1 %s ~8"}) {
                server.runCommand("execute at @p run setblock " + corner.formatted("~") + " thaumcraft:arcane_stone_bricks");
                server.runCommand("execute at @p run setblock " + corner.formatted("~1") + " thaumcraft:arcane_stone");
            }
            server.runCommand("execute at @p run setblock ~ ~2 ~7 thaumcraft:infusion_matrix");
            context.waitTicks(10);
            context.takeScreenshot("infusao_esqueleto");
            // a varinha acorda a matriz: os cantos viram pilares
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().get(0);
                var at = player.blockPosition().offset(0, 2, 7);
                var wand = new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.WAND);
                var vis = new net.thaumcraft.api.aspects.AspectList();
                for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) vis.add(primal, 5000);
                wand.set(net.thaumcraft.registry.TCComponents.WAND_VIS, vis);
                if (player.level().getBlockEntity(at) instanceof net.thaumcraft.block.entity.InfusionMatrixBlockEntity matrix) {
                    matrix.poke(player.level(), at, player, wand);
                }
            });

            // pedestais em volta, e jarros de essência ao lado
            server.runCommand("execute at @p run setblock ~-3 ~ ~7 thaumcraft:pedestal");
            server.runCommand("execute at @p run setblock ~3 ~ ~7 thaumcraft:pedestal");
            server.runCommand("execute at @p run setblock ~-4 ~ ~4 thaumcraft:jar");
            server.runCommand("execute at @p run setblock ~4 ~ ~4 thaumcraft:jar");
            context.waitTicks(30);

            server.runCommand("execute at @p run data merge block ~ ~ ~7 {Items:[{id:\"minecraft:obsidian\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~-3 ~ ~7 {Items:[{id:\"thaumcraft:shard_balanced\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~3 ~ ~7 {Items:[{id:\"thaumcraft:shard_earth\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~-4 ~ ~4 {aspect:\"terra\",amount:60}");
            server.runCommand("execute at @p run data merge block ~4 ~ ~4 {aspect:\"tenebrae\",amount:40}");
            context.waitTicks(40);
            context.takeScreenshot("infusao_altar");

            // e de perto, para ver o que o pedestal segura
            server.runCommand("execute at @p run tp @s ~ ~ ~4 0 22");
            context.waitTicks(20);
            context.takeScreenshot("infusao_pedestal_de_perto");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 0, 3);
                var found = minecraft.level.getBlockEntity(pos);
                System.out.println("[INFUSAO] em " + pos + " achei " + found
                        + (found instanceof net.thaumcraft.block.entity.PedestalBlockEntity pedestal
                           ? " segurando " + pedestal.held()
                           : ""));
            });
        }
    }
}
