package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Os mantos do taumaturgo (na cor de sempre e tingido) e as botas do viajante, vestidos em suportes. */
public class EquipmentClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 10");
            server.runCommand("execute at @p run summon armor_stand ~-1.2 ~ ~3 {Rotation:[180f,0f],equipment:{head:{id:\"thaumcraft:goggles\"},chest:{id:\"thaumcraft:robe_chestplate\"},legs:{id:\"thaumcraft:robe_leggings\"},feet:{id:\"thaumcraft:robe_boots\"}}}");
            server.runCommand("execute at @p run summon armor_stand ~1.2 ~ ~3 {Rotation:[180f,0f],equipment:{chest:{id:\"thaumcraft:robe_chestplate\",components:{\"minecraft:dyed_color\":11546150}},legs:{id:\"thaumcraft:robe_leggings\",components:{\"minecraft:dyed_color\":11546150}},feet:{id:\"thaumcraft:traveller_boots\"}}}");
            server.runCommand("give @p thaumcraft:robe_chestplate");
            server.runCommand("give @p thaumcraft:robe_leggings[minecraft:dyed_color=11546150]");
            server.runCommand("give @p thaumcraft:robe_boots");
            server.runCommand("give @p thaumcraft:traveller_boots");
            context.waitTicks(40);
            context.takeScreenshot("mantos_e_botas");
            server.runCommand("kill @e[type=armor_stand]");
            server.runCommand("execute at @p run summon armor_stand ~-1.2 ~ ~2.5 {Rotation:[160f,0f],equipment:{head:{id:\"thaumcraft:fortress_helmet\",components:{\"thaumcraft:fortress_mask\":1,\"thaumcraft:fortress_goggles\":true}},chest:{id:\"thaumcraft:fortress_chestplate\"},legs:{id:\"thaumcraft:fortress_leggings\"}}}");
            server.runCommand("execute at @p run summon armor_stand ~1.2 ~ ~2.5 {Rotation:[200f,0f],equipment:{chest:{id:\"thaumcraft:fortress_chestplate\"}}}");
            server.runCommand("give @p thaumcraft:fortress_helmet");
            context.waitTicks(40);
            context.takeScreenshot("fortaleza");
        }
    }
}
