package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A esfera do foco Primordial parada no ar, de dia e de noite, com a varinha do foco na mão. */
public class PrimalOrbClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("gamerule mob_griefing false");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 0");
            singleplayer.getServer().runCommand("give @p thaumcraft:wand[thaumcraft:wand_focus=\"primal\"]");
            singleplayer.getServer().runCommand("execute at @p run summon thaumcraft:primal_orb ~ ~1.6 ~2.5 {Motion:[0.0,0.0,0.0],NoGravity:1b}");
            context.waitTicks(12);
            context.takeScreenshot("esfera_primordial_dia");
            singleplayer.getServer().runCommand("time set midnight");
            singleplayer.getServer().runCommand("kill @e[type=thaumcraft:primal_orb]");
            singleplayer.getServer().runCommand("execute at @p run summon thaumcraft:primal_orb ~ ~1.6 ~2.5 {Motion:[0.0,0.0,0.0],NoGravity:1b}");
            context.waitTicks(15);
            context.takeScreenshot("esfera_primordial_noite");
        }
    }
}
