package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellEffectEntity;

import java.util.List;

/**
 * O pó que os feitiços deixam no ar: a Zona em roda e a Parede em linha, cada uma na cor da sua Afinidade.
 *
 * <p>São duas áreas lado a lado — uma de <b>fogo</b> e uma de <b>gelo</b> — para a foto mostrar o que importa:
 * que a cor sai da frase e não de uma escolha. Contra uma parede escura, porque o pó é luz.
 */
public class ArcanaParticlesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("tp @p 0 -59 0 -90 0");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();

                // uma parede escura atrás, para o pó ter sobre o que brilhar
                for (int y = -61; y <= -54; y++) {
                    for (int z = -8; z <= 8; z++) {
                        level.setBlockAndUpdate(new BlockPos(12, y, z),
                                Blocks.DEEPSLATE_TILES.defaultBlockState());
                    }
                }

                // uma Zona de fogo à esquerda
                Spell fogo = new Spell(List.of(
                        new Spell.Stage(Shapes.ZONE, List.of(), List.of()),
                        new Spell.Stage(Shapes.AOE, List.of(Essences.FIRE_DAMAGE), List.of())));
                var zona = new SpellEffectEntity(level, jogador, fogo.pop(),
                        SpellEffectEntity.Kind.ZONE);
                zona.setRadius(2.5f);
                zona.setLife(600);
                zona.snapTo(7.0, -59.0, -4.0, 0.0f, 0.0f);
                level.addFreshEntity(zona);

                // e uma Parede de gelo à direita, atravessada
                Spell gelo = new Spell(List.of(
                        new Spell.Stage(Shapes.WALL, List.of(), List.of()),
                        new Spell.Stage(Shapes.AOE, List.of(Essences.FROST_DAMAGE), List.of())));
                var parede = new SpellEffectEntity(level, jogador, gelo.pop(),
                        SpellEffectEntity.Kind.WALL);
                parede.setRadius(3.0f);
                parede.setLife(600);
                parede.snapTo(7.0, -59.0, 4.0, 0.0f, 0.0f);
                parede.setWall(0.0f);
                level.addFreshEntity(parede);
            });

            // tempo para o pó encher o ar
            context.waitTicks(40);
            context.takeScreenshot("aa_po");

            context.waitTicks(20);
            context.takeScreenshot("aa_po_2");
        }
    }
}
