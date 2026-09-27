package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.kettle.KettleBrews;

/**
 * Os frascos do Caldeirão de Pote vistos: os sete no cinto, e o que três deles deixam no mundo — a parede
 * vestida de vinha, os cactos e o galho que brotou.
 */
public class OccultaKettleBrewClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 0 5");

            // os sete frascos no cinto
            String[] frascos = {"brew_of_vines", "brew_of_thorns", "brew_of_ink", "brew_of_sprouting",
                    "brew_of_erosion", "brew_of_love", "brew_of_raising"};
            for (int i = 0; i < frascos.length; i++) {
                server.runCommand("item replace entity @p hotbar." + i + " with thaumcraft:" + frascos[i]);
            }
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:brew_of_vines");
            context.waitTicks(15);
            context.takeScreenshot("ao_frascos_do_pote");

            // e o que eles deixam no mundo: vinha na parede, cactos e um galho
            server.runOnServer(s -> {
                var mundo = s.overworld();
                BlockPos pé = s.getPlayerList().getPlayers().getFirst().blockPosition();
                BlockPos parede = pé.offset(-3, 0, 7);
                for (int dx = 0; dx < 3; dx++) {
                    for (int dy = 0; dy < 5; dy++) {
                        mundo.setBlockAndUpdate(parede.offset(dx, dy, 0), Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
                BlockPos alvo = parede.offset(1, 3, 0);
                KettleBrews.Kind.VINES.impact(mundo,
                        new BlockHitResult(Vec3.atCenterOf(alvo.north()), Direction.NORTH, alvo, false), null);

                BlockPos areia = pé.offset(2, -1, 6);
                mundo.setBlockAndUpdate(areia, Blocks.DIRT.defaultBlockState());
                KettleBrews.Kind.THORNS.impact(mundo,
                        new BlockHitResult(Vec3.atCenterOf(areia.above()), Direction.UP, areia, false), null);

                BlockPos broto = pé.offset(5, -1, 7);
                mundo.setBlockAndUpdate(broto, Blocks.DIRT.defaultBlockState());
                KettleBrews.Kind.SPROUTING.impact(mundo,
                        new BlockHitResult(Vec3.atCenterOf(broto.above()), Direction.UP, broto, false), null);
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_frascos_no_mundo");
        }
    }
}
