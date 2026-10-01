package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.ArcanaEntities;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellEffectEntity;
import net.thaumcraft.arcana.SpellProjectileEntity;

import java.util.List;
import java.util.Map;

/**
 * A <b>Cor</b>, vista.
 *
 * <p>Esta peça não muda número nenhum. O que ela muda é o que se <b>vê</b> — e por isso ela é a única do ramo
 * que uma prova de servidor não consegue julgar: lá ela é um número guardado numa etapa, e aqui ela ou pinta o
 * feitiço ou não pinta.
 *
 * <p>A foto são <b>cinco projéteis de fogo</b> lado a lado: o primeiro sem Cor, e os outros quatro com
 * tintas diferentes. Todos têm a mesma figura — a do fogo —, porque a Cor <b>pinta</b> e não troca a cara.
 * E o de tinta branca sai igual ao sem Cor, que é o que tem de ser: o branco do original é 0xF0F0F0, e pintar
 * de branco quase branco é não pintar.
 */
public class ArcanaColourClientTest implements FabricClientGameTest {
    /** As quatro tintas da foto, com os números que o original lhes dá. */
    private static final Map<String, Integer> TINTAS = new java.util.LinkedHashMap<>(Map.of());

    static {
        TINTAS.put("azul", 2437522);
        TINTAS.put("verde-limão", 4312372);
        TINTAS.put("rosa", 14188952);
        TINTAS.put("branco", 15790320);
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set midnight");
            server.runCommand("tp @p 0 -57 -12 0 0");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();

                // uma parede escura atrás, para a cor ter sobre o que brilhar
                for (int y = -60; y <= -52; y++) {
                    for (int x = -9; x <= 9; x++) {
                        level.setBlockAndUpdate(new BlockPos(x, y, 6),
                                Blocks.DEEPSLATE_TILES.defaultBlockState());
                    }
                }

                // o primeiro sem Cor, e os quatro seguintes com tinta
                voa(level, jogador, -6.0, SpellProjectileEntity.SEM_COR);
                double x = -3.0;
                for (int cor : TINTAS.values()) {
                    voa(level, jogador, x, cor);
                    x += 3.0;
                }
            });

            context.waitTicks(5);
            context.takeScreenshot("aa_cor_projeteis");

        }
    }

    private static void voa(ServerLevel level, net.minecraft.world.entity.LivingEntity quem,
                            double x, int cor) {
        var tiro = new SpellProjectileEntity(level, quem,
                Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE), 0.0);
        tiro.snapTo(x, -56.0, 0.0);
        tiro.setDeltaMovement(Vec3.ZERO);
        if (cor != SpellProjectileEntity.SEM_COR) tiro.forceColor(cor);
        level.addFreshEntity(tiro);
    }

    private static void zona(ServerLevel level, net.minecraft.world.entity.LivingEntity quem,
                             double x, Spell feitiço) {
        var área = new SpellEffectEntity(level, quem, feitiço, SpellEffectEntity.Kind.ZONE);
        área.setRadius(3.0f);
        área.setLife(400);
        área.snapTo(x, -58.5, 0.0);
        level.addFreshEntity(área);
    }
}
