package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.registry.TCItems;

/** As botas do viajante sobem um bloco inteiro sem pular, como o {@code stepHeight = 1.0F} do original. */
public class TravellerBootsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            server.runCommand("gamerule doMobSpawning false");
            // um degrau de um bloco à frente, e chão limpo em volta
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                for (int x = -3; x <= 3; x++) for (int z = -1; z <= 6; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                for (int x = -3; x <= 3; x++) level.setBlockAndUpdate(p.offset(x, 0, 3), Blocks.STONE.defaultBlockState());
            });
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 0");
            context.waitTicks(20);

            // sem as botas, o degrau barra
            double semBotas = andaEDiz(context, server);
            if (semBotas > 3.5) throw new AssertionError("sem as botas o degrau tinha de barrar; andou " + semBotas);
            server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst()
                    .setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.TRAVELLER_BOOTS)));
            context.waitTicks(20);
            double comBotas = andaEDiz(context, server);
            System.out.println("[BOTAS] sem=" + semBotas + " com=" + comBotas);
            if (comBotas < semBotas + 2.0) {
                throw new AssertionError("com as botas o degrau de um bloco tem de ser subido andando: sem=" + semBotas + " com=" + comBotas);
            }
            // e o pulo alto pedido por quem joga: um pulo tem de passar dos três blocos
            double alturaDoPulo = pulaEDiz(context, server);
            System.out.println("[BOTAS] pulo=" + alturaDoPulo);
            if (alturaDoPulo < 2.9) throw new AssertionError("com as botas o pulo tem de chegar a três blocos; chegou a " + alturaDoPulo);
            context.takeScreenshot("botas_do_viajante");
        }
    }

    /** Volta para a marca, pula uma vez e diz até onde subiu. */
    private static double pulaEDiz(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 0");
        context.waitTicks(10);
        double chao = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getY());
        context.getInput().holdKey(options -> options.keyJump);
        context.waitTicks(2);
        context.getInput().releaseKey(options -> options.keyJump);
        double alto = 0.0;
        for (int tique = 0; tique < 30; tique++) {
            context.waitTicks(1);
            alto = Math.max(alto, server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getY()) - chao);
        }
        return alto;
    }

    /** Volta para a marca, anda para a frente um pouco e diz a que altura parou. */
    private static double andaEDiz(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 0");
        context.waitTicks(10);
        double antes = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getY());
        double antesZ = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getZ());
        context.getInput().holdKey(options -> options.keyUp);
        context.waitTicks(40);
        context.getInput().releaseKey(options -> options.keyUp);
        context.waitTicks(10);
        double depois = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getY());
        double depoisZ = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getZ());
        System.out.println("[BOTAS] andou dz=" + (depoisZ - antesZ) + " dy=" + (depois - antes));
        return depoisZ - antesZ;
    }
}
