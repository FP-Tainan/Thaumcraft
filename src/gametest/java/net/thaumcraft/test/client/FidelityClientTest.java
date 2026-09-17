package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A conferência do visual: a tela do forno, os jarros rotulados, a essência correndo no tubo, o que os
 * Óculos da Revelação mostram e as marcas do sino.
 *
 * <p>Não é uma prova que passa ou falha sozinha — é a que tira as capturas para se comparar com o mod
 * original lado a lado. É o único jeito honesto de saber se o porte está parecido: olhando.
 */
public class FidelityClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule doMobSpawning false");

            server.runCommand("execute at @p run fill ~-6 ~-1 ~2 ~6 ~-1 ~10 minecraft:smooth_stone");
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 8");

            // a destilaria: forno, alambique em cima, cano descendo até dois jarros rotulados
            server.runCommand("execute at @p run setblock ~1 ~ ~5 thaumcraft:alchemical_furnace[facing=north,lit=true]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 thaumcraft:alembic");
            server.runCommand("execute at @p run setblock ~1 ~2 ~5 thaumcraft:alembic");
            server.runCommand("execute at @p run setblock ~1 ~3 ~5 thaumcraft:alembic");
            server.runCommand("execute at @p run setblock ~1 ~4 ~5 thaumcraft:alembic");
            server.runCommand("execute at @p run setblock ~ ~1 ~5 thaumcraft:tube");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 thaumcraft:tube");
            server.runCommand("execute at @p run setblock ~ ~ ~5 thaumcraft:jar");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 thaumcraft:jar");
            context.waitTicks(30);

            // com essência dentro de tudo, e rótulo nos jarros
            server.runCommand("execute at @p run data merge block ~ ~ ~5 {aspect:\"ignis\",label:\"ignis\",amount:48,facing:3}");
            server.runCommand("execute at @p run data merge block ~-1 ~ ~5 {aspect:\"aqua\",label:\"aqua\",amount:20,facing:3}");
            server.runCommand("execute at @p run data merge block ~1 ~1 ~5 {aspect:\"terra\",amount:22}");
            server.runCommand("execute at @p run data merge block ~1 ~2 ~5 {aspect:\"aer\",amount:14}");
            server.runCommand("execute at @p run data merge block ~1 ~3 ~5 {aspect:\"ordo\",amount:7}");
            server.runCommand("execute at @p run data merge block ~ ~1 ~5 {type:\"ignis\",amount:1}");
            server.runCommand("execute at @p run data merge block ~-1 ~1 ~5 {type:\"aqua\",amount:1}");
            server.runCommand("execute at @p run data merge block ~1 ~ ~5 {aspects:{terra:16,ignis:9}}");
            context.waitTicks(30);
            context.takeScreenshot("fid_destilaria_sem_oculos");

            // agora com os Óculos da Revelação no rosto: cada peça diz o que guarda
            server.runCommand("item replace entity @p armor.head with thaumcraft:goggles");
            context.waitTicks(30);
            context.takeScreenshot("fid_destilaria_com_oculos");

            // a tela do forno
            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(1, 0, 5);
                var hit = new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false);
                minecraft.gameMode.useItemOn(minecraft.player,
                        net.minecraft.world.InteractionHand.MAIN_HAND, hit);
            });
            context.waitTicks(25);
            context.takeScreenshot("fid_forno_tela");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);

            // o Nitor posto no ar, e as marcas do sino sobre um baú
            server.runCommand("execute at @p run setblock ~3 ~1 ~5 thaumcraft:nitor");
            server.runCommand("execute at @p run setblock ~-3 ~ ~5 minecraft:chest");
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:golem_bell");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:golem_straw");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            // agachado, que é como o sino fala antes de o baú abrir
            context.runOnClient(minecraft -> minecraft.options.keyShift.setDown(true));
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(-3, 0, 5);
                var hit = new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                        net.minecraft.core.Direction.UP, pos, false);
                minecraft.gameMode.useItemOn(minecraft.player,
                        net.minecraft.world.InteractionHand.MAIN_HAND, hit);
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.options.keyShift.setDown(false));
            context.waitTicks(25);
            context.takeScreenshot("fid_nitor_e_marca");

            // a varinha na mão, de perto
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 0");
            server.runCommand("clear @p");
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:wand");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(25);
            context.takeScreenshot("fid_varinha_na_mao");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:staff");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(1));
            context.waitTicks(20);
            context.takeScreenshot("fid_bastao_na_mao");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 0, 5);
                var found = minecraft.level.getBlockEntity(pos);
                System.out.println("[FIDELIDADE] jarro em " + pos + ": " + found
                        + (found instanceof net.thaumcraft.block.entity.JarBlockEntity jar
                           ? " aspecto=" + jar.aspect() + " rotulo=" + jar.label()
                             + " frente=" + jar.facing() + " quanto=" + jar.amount()
                           : ""));
            });
        }
    }
}
