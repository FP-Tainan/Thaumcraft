package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A bancada arcana no mundo e a tela dela aberta. */
public class WorkbenchClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute at @p run tp @s ~ ~ ~ 0 30");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~ ~2 thaumcraft:arcane_workbench");
            context.waitTicks(30);
            context.takeScreenshot("bancada_no_mundo");

            // e a tela dela, com uma varinha e os fragmentos do amuleto primordial
            singleplayer.getServer().runCommand("give @p thaumcraft:wand");
            singleplayer.getServer().runCommand("give @p thaumcraft:shard_balanced 4");
            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 0, 2);
                minecraft.player.connection.getConnection();
                net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
                minecraft.gameMode.useItemOn(minecraft.player,
                        net.minecraft.world.InteractionHand.MAIN_HAND, hit);
            });
            context.waitTicks(25);
            context.takeScreenshot("bancada_aberta");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // montando uma varinha de ouro e grande-madeira: primeiro com a varinha seca, que não paga
            singleplayer.getServer().runCommand("thaumcraft pesquisa tudo @p");
            singleplayer.getServer().runCommand("execute at @p run item replace block ~ ~ ~2 container.2 with thaumcraft:wand_cap_gold");
            singleplayer.getServer().runCommand("execute at @p run item replace block ~ ~ ~2 container.6 with thaumcraft:wand_cap_gold");
            singleplayer.getServer().runCommand("execute at @p run item replace block ~ ~ ~2 container.4 with thaumcraft:wand_rod_greatwood");
            singleplayer.getServer().runCommand("execute at @p run item replace block ~ ~ ~2 container.9 with thaumcraft:wand");
            open(context);
            context.takeScreenshot("bancada_varinha_seca");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // e com a varinha cheia: os seis círculos acendem e o resultado aparece de verdade
            singleplayer.getServer().runCommand("execute at @p run item replace block ~ ~ ~2 container.9 with "
                    + "thaumcraft:wand[thaumcraft:wand_vis={aer:2500,terra:2500,ignis:2500,aqua:2500,ordo:2500,perditio:2500}]");
            open(context);
            context.takeScreenshot("bancada_varinha_cheia");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }

    private static void open(ClientGameTestContext context) {
        context.runOnClient(minecraft -> {
            var pos = minecraft.player.blockPosition().offset(0, 0, 2);
            net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                    net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
            minecraft.player.getInventory().setSelectedSlot(8);
            minecraft.gameMode.useItemOn(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        });
        context.waitTicks(25);
    }
}
