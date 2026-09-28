package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellProjectileEntity;

/**
 * As dez Afinidades vistas: um projétil de cada, lado a lado contra uma parede.
 *
 * <p>É a foto que prova o que a Afinidade faz de verdade na tela — o estouro do Fogo, a brasa do Gelo, o
 * brilho verde da Vida, a pedra da Terra, o vento do Ar. Ninguém escolheu nenhuma delas: elas saem das
 * Essências que a frase tem.
 *
 * <p>Os projéteis ficam <b>parados</b> de propósito, com velocidade zero, e a Afinidade é posta à mão em cada
 * um — no jogo ela vem de {@code Spell.mainAffinity()}, mas ainda não há uma Essência para cada uma das dez.
 */
public class ArcanaAffinityClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p 0 -59 0 -90 0");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();

                // uma parede escura atrás, para a luz de cada uma ter sobre o que somar
                for (int y = -61; y <= -54; y++) {
                    for (int z = -10; z <= 10; z++) {
                        level.setBlockAndUpdate(new BlockPos(9, y, z),
                                Blocks.DEEPSLATE_TILES.defaultBlockState());
                    }
                }

                // uma de cada, em fila da esquerda para a direita, na ordem do enum: a nenhuma primeiro
                Affinity[] todas = Affinity.values();
                for (int i = 0; i < todas.length; i++) {
                    var voa = new SpellProjectileEntity(level, jogador,
                            Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE), 1.0);
                    voa.forceAffinity(todas[i]);
                    voa.snapTo(5.0, -58.0, -5.5 + i * 1.1, 90.0f, 0.0f);
                    voa.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(voa);
                }
            });
            context.waitTicks(30);
            context.takeScreenshot("aa_afinidades");

            // e outra algumas batidas depois, porque quase todas são tiras animadas
            context.waitTicks(11);
            context.takeScreenshot("aa_afinidades_2");
        }
    }
}
