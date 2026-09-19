package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;

/** A distorção na tela: os quatro filtros das poções, a vinheta do susto, a névoa e as aranhas da mente. */
public class WarpClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 10");
            context.waitTicks(20);
            context.takeScreenshot("dobra_normal");
            String[] names = {"olhar_mortal", "vista_embacada", "fome_estranha", "desprezo_do_sol"};
            var effects = java.util.List.of(TCEffects.DEATH_GAZE, TCEffects.BLURRED_VISION, TCEffects.UNNATURAL_HUNGER, TCEffects.SUN_SCORNED);
            for (int i = 0; i < names.length; i++) {
                var effect = effects.get(i);
                server.runOnServer(s -> {
                    var p = s.getPlayerList().getPlayers().getFirst();
                    p.removeAllEffects();
                    p.addEffect(new MobEffectInstance(effect, 2000, 0));
                });
                context.waitTicks(10);
                context.takeScreenshot("dobra_" + names[i]);
            }
            server.runOnServer(s -> {
                var p = s.getPlayerList().getPlayers().getFirst();
                p.removeAllEffects();
                net.thaumcraft.net.TCNetwork.miscEvent(p, 0);
                net.thaumcraft.net.TCNetwork.miscEvent(p, 1);
                for (int a = 0; a < 6; a++) {
                    var spider = TCEntities.MIND_SPIDER.create(p.level(), EntitySpawnReason.EVENT);
                    spider.snapTo(p.getX() - 1.5 + a * 0.6, p.getY(), p.getZ() + 3.0, 180.0f, 0.0f);
                    spider.setNoAi(true);
                    if (a % 2 == 0) {
                        spider.setViewer(p.getName().getString());
                        spider.setHarmless(true);
                    }
                    p.level().addFreshEntity(spider);
                }
            });
            context.waitTicks(40);
            context.takeScreenshot("dobra_susto");
            context.waitTicks(110);
            context.takeScreenshot("dobra_nevoa_aranhas");
        }
    }
}
