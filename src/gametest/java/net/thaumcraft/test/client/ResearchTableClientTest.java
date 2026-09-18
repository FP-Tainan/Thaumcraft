package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.ResearchTableBlock;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.Researches;

import java.util.Random;

/** A mesa comum, a mesa de pesquisa no mundo e a tela do tabuleiro de hexágonos. */
public class ResearchTableClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("thaumcraft tudo @p");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 35");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~ ~2 thaumcraft:table");
            singleplayer.getServer().runCommand("execute at @p run setblock ~-1 ~ ~2 thaumcraft:table[axis=z]");
            singleplayer.getServer().runCommand("execute at @p run setblock ~1 ~ ~4 thaumcraft:table");
            singleplayer.getServer().runCommand("execute at @p run setblock ~2 ~ ~4 thaumcraft:table");
            context.waitTicks(30);
            context.takeScreenshot("mesas");

            // as duas mesas do fundo viram a mesa de pesquisa, com tinta e uma nota de uma pesquisa difícil
            singleplayer.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().get(0);
                BlockPos main = player.blockPosition().offset(1, 0, 4);
                ResearchTableBlock.form(player.level(), main, Direction.EAST);
                if (player.level().getBlockEntity(main) instanceof ResearchTableBlockEntity table) {
                    table.setItem(ResearchTableBlockEntity.INK, new ItemStack(TCItems.SCRIBING_TOOLS));
                    Research chosen = null;
                    for (Research research : Researches.ALL.values()) {
                        if (research.complexity() >= 2 && research.tags().size() >= 3) {
                            chosen = research;
                            break;
                        }
                    }
                    table.setItem(ResearchTableBlockEntity.NOTE, ResearchNotes.create(chosen.key(), new Random(3)));
                }
            });
            context.waitTicks(20);
            context.takeScreenshot("mesa_pesquisa_mundo");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(1, 0, 4);
                var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        Direction.UP, pos, false);
                minecraft.player.getInventory().setSelectedSlot(8);
                minecraft.gameMode.useItemOn(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
            });
            context.waitTicks(30);
            context.takeScreenshot("mesa_pesquisa_tela");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
