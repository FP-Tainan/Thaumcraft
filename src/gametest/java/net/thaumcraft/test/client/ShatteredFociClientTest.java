package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Os raios dos três focos de fenda: roxos para rasgar e cerzir, brancos para firmar.
 *
 * <p>Quem manda pediu o mesmo raio do foco de choque, pintado de outra cor. Não há o que conferir por conta
 * própria numa tela: as fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class ShatteredFociClientTest implements FabricClientGameTest {
    private static final List<String> FOCOS = List.of("rift_open", "rift_hold", "rift_close");

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @p");
            server.runCommand("time set midnight");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                // uma parede à frente, para o raio ter onde bater
                var base = player.blockPosition().north(8);
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dy = 0; dy < 6; dy++) {
                        level.setBlockAndUpdate(base.east(dx).above(dy),
                                net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
            });
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(20);

            for (String qual : FOCOS) {
                context.runOnClient(minecraft -> {
                    ItemStack varinha = new ItemStack(TCItems.WAND);
                    varinha.set(TCComponents.WAND_FOCUS, qual);
                    var melhorias = new ArrayList<Short>();
                    while (melhorias.size() < 5) melhorias.add((short) -1);
                    varinha.set(TCComponents.FOCUS_UPGRADES, melhorias);
                    var vis = new AspectList();
                    for (var primal : Aspects.primals()) vis.add(primal, 2500);
                    varinha.set(TCComponents.WAND_VIS, vis);
                    minecraft.player.getInventory().setItem(0, varinha);
                    minecraft.player.getInventory().setSelectedSlot(0);
                    // o raio dura uns tiques; solta-se uns quantos para a foto pegar o feixe cheio
                    for (int i = 0; i < 4; i++) {
                        Focuses.tick(minecraft.level, minecraft.player, varinha, Focuses.on(varinha));
                    }
                });
                context.waitTicks(2);
                context.takeScreenshot("raio_" + qual);
            }
        }
    }
}
