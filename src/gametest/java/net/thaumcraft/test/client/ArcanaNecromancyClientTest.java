package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.AffinityData;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;

import java.util.List;

/**
 * O exército do necromante, visto.
 *
 * <p>Esta foto é o acréscimo inteiro numa imagem: <b>três</b> de pé onde o original deixa <b>um</b>, metade
 * esqueleto e metade zumbi, todos de diamante, e todos <b>a cavalo</b>. Nenhuma prova de servidor mostra isso
 * — ela confere que o elmo é de diamante e que o cavalo está debaixo, mas não que a coisa <i>parece</i> um
 * exército.
 *
 * <p>São duas: a do mago raso, que chama um esqueleto nu como no Ars Magica 2, e a do mago fundo, para se ver
 * lado a lado o que a Afinidade faz.
 */
public class ArcanaNecromancyClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // 1) o mago raso: a Invocação do original, nua
            server.runOnServer(s -> {
                var level = s.overworld();
                var mago = s.getPlayerList().getPlayers().getFirst();
                AffinityData.set(mago, AffinityData.NONE);
                Vec3 onde = mago.position().add(0.0, 0.0, 5.0);
                Essences.SUMMON.onBlock(level, Spell.of(Shapes.TOUCH, Essences.SUMMON), mago,
                        BlockPos.containing(onde), Direction.UP, onde);
            });
            server.runCommand("tp @p ~ ~1 ~-1 0 10");
            context.waitTicks(30);
            context.takeScreenshot("aa_invocacao_nua");

            // e some com ela: se ficasse, ocupava uma das três vagas da foto seguinte — e foi
            // exatamente o que aconteceu da primeira vez que esta prova correu
            server.runOnServer(s -> {
                for (var bicho : s.overworld().getAllEntities()) {
                    if (net.thaumcraft.arcana.Summons.éInvocado(bicho)) bicho.discard();
                }
            });
            context.waitTicks(5);

            // 2) e o mago fundo: três de pé, de diamante e a cavalo
            server.runOnServer(s -> {
                var level = s.overworld();
                var mago = s.getPlayerList().getPlayers().getFirst();
                AffinityData.set(mago, AffinityData.NONE.with(Affinity.ENDER, Affinity.MAX_DEPTH));

                var exército = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                        List.of(Essences.SUMMON), List.of(Modifiers.LEGION, Modifiers.LEGION))));
                var erguer = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                        List.of(Essences.RAISE_DEAD), List.of(Modifiers.LEGION, Modifiers.LEGION))));

                for (int i = 0; i < 3; i++) {
                    Vec3 onde = mago.position().add(-3.0 + i * 3.0, 0.0, 7.0);
                    var qual = i == 1 ? erguer : exército;
                    var peça = i == 1 ? Essences.RAISE_DEAD : Essences.SUMMON;
                    peça.onBlock(level, qual, mago, BlockPos.containing(onde), Direction.UP, onde);
                }
            });
            context.waitTicks(40);

            // e o que a foto tem de mostrar fica conferido aqui, para uma foto ruim falhar em vez
            // de passar calada: três soldados, três montarias, e cada um em cima da sua
            server.runOnServer(s -> {
                int soldados = 0;
                int montarias = 0;
                int acavalo = 0;
                for (var bicho : s.overworld().getAllEntities()) {
                    var dado = bicho.getAttached(net.thaumcraft.arcana.Summons.DATA);
                    if (dado == null) continue;
                    if (dado.montaria()) {
                        montarias++;
                    } else {
                        soldados++;
                        if (bicho.getVehicle() != null) acavalo++;
                    }
                }
                if (soldados != 3) {
                    throw new AssertionError("com duas Legiões são três soldados, e são " + soldados);
                }
                if (montarias != 3) {
                    throw new AssertionError("e três montarias, e são " + montarias);
                }
                if (acavalo != 3) {
                    throw new AssertionError("e os três a cavalo, e estão " + acavalo);
                }
            });
            // e se viram para a câmara: eles nascem virados para onde o mago olha, que é o original,
            // e de costas não se distingue o zumbi do esqueleto
            server.runOnServer(s -> {
                for (var bicho : s.overworld().getAllEntities()) {
                    if (!net.thaumcraft.arcana.Summons.éInvocado(bicho)) continue;
                    bicho.snapTo(bicho.getX(), bicho.getY(), bicho.getZ(), 180.0f, 0.0f);
                    if (bicho instanceof net.minecraft.world.entity.LivingEntity vivo) {
                        vivo.setYHeadRot(180.0f);
                        vivo.yBodyRot = 180.0f;
                    }
                }
            });
            context.waitTicks(2);
            context.takeScreenshot("aa_exercito_do_necromante");
        }
    }
}
