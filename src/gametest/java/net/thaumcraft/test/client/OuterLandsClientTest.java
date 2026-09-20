package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.thaumcraft.world.OuterLands;
import net.thaumcraft.world.outer.Cell;
import net.thaumcraft.world.outer.Labyrinth;
import net.thaumcraft.world.outer.MazeFeature;

import java.util.concurrent.atomic.AtomicReference;

/**
 * As Terras de Fora: um labirinto traçado em volta de onde o jogador está, a entrada pelo portal do mundo de cima, a sala
 * do portal por dentro, e um corredor.
 */
public class OuterLandsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("gamerule doMobSpawning false");
            server.runCommand("difficulty peaceful");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            AtomicReference<BlockPos> corridor = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos p = player.blockPosition();
                int cx = p.getX() >> 4, cz = p.getZ() >> 4;
                Labyrinth maze = Labyrinth.get(s);
                maze.build(cx, cz, 15, 15, 12345L);
                base.set(new BlockPos(cx * 16 + 8, MazeFeature.FLOOR + 3, cz * 16 + 8));
                // um corredor comum perto, para a segunda foto
                for (int r = 1; r < 6 && corridor.get() == null; r++) {
                    for (int dx = -r; dx <= r && corridor.get() == null; dx++) {
                        for (int dz = -r; dz <= r; dz++) {
                            Cell c = maze.cell(cx + dx, cz + dz);
                            if (c != null && c.feature == 0 && (c.north || c.south) && (c.east || c.west)) {
                                corridor.set(new BlockPos((cx + dx) * 16 + 8, MazeFeature.FLOOR + 3, (cz + dz) * 16 + 8));
                                break;
                            }
                        }
                    }
                }
                var level = player.level();
                // o portal do mundo de cima, entre o capitel e o obelisco, bem na frente do jogador
                BlockPos gate = p.offset(0, 0, 3);
                level.setBlockAndUpdate(gate.below(), net.thaumcraft.registry.TCBlocks.ELDRITCH_CAPSTONE.defaultBlockState());
                level.setBlockAndUpdate(gate.above(), net.thaumcraft.registry.TCBlocks.ELDRITCH_OBELISK.defaultBlockState());
                level.setBlockAndUpdate(gate, net.thaumcraft.registry.TCBlocks.ELDRITCH_PORTAL.defaultBlockState());
            });
            context.waitTicks(40);
            context.takeScreenshot("terras-de-fora-portal-de-cima");
            BlockPos gatePos = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition().offset(0, 0, 3));
            server.runCommand("tp @p " + (gatePos.getX() + 0.5) + " " + gatePos.getY() + " " + (gatePos.getZ() + 0.5));
            context.waitTicks(100);
            boolean inside = server.computeOnServer(s -> OuterLands.is(s.getPlayerList().getPlayers().getFirst().level()));
            if (!inside) throw new AssertionError("o portal devia levar às Terras de Fora");
            singleplayer.getConnection().waitForChunksRender();
            BlockPos p = base.get();
            server.runCommand("execute in thaumcraft:outer run tp @p " + (p.getX() + 0.5) + " " + (p.getY() - 1) + " " + (p.getZ() - 2.5) + " 0 -10");
            context.waitTicks(60);
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("terras-de-fora-sala-do-portal");
            BlockPos c = corridor.get();
            if (c != null) {
                server.runCommand("execute in thaumcraft:outer run tp @p " + (c.getX() + 0.5) + " " + c.getY() + " " + (c.getZ() + 0.5) + " 0 0");
                context.waitTicks(60);
                singleplayer.getConnection().waitForChunksRender();
                context.takeScreenshot("terras-de-fora-corredor");
            }
            // e a volta: entrando no mesmo portal por dentro, o jogador tem de sair no mundo de cima, ao lado do portal
            server.runCommand("execute in thaumcraft:outer run tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5));
            context.waitTicks(120);
            boolean back = server.computeOnServer(s -> !OuterLands.is(s.getPlayerList().getPlayers().getFirst().level()));
            if (!back) throw new AssertionError("o portal devia trazer de volta ao mundo de cima");
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("terras-de-fora-de-volta");
            // e não pode largar quem volta dentro da pedra
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                var where = player.blockPosition();
                if (level.getBlockState(where).isSuffocating(level, where) || level.getBlockState(where.above()).isSuffocating(level, where.above())) {
                    throw new AssertionError("a volta largou o jogador dentro de bloco em " + where);
                }
                double dist = Math.sqrt(where.distSqr(gatePos));
                if (dist > 8.0) throw new AssertionError("a volta devia ser ao lado do portal, saiu a " + dist + " blocos");
            });
        }
    }
}
