package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.mirror.MirrorBlock;
import net.thaumcraft.occulta.mirror.MirrorFaceEntity;
import net.thaumcraft.occulta.mirror.MirrorWorld;
import net.thaumcraft.occulta.mirror.ReflectionEntity;

/**
 * Os espelhos vistos: a moldura oval das duas metades, a cara que aparece no vidro, o Reflexo na cela e o Mundo do
 * Espelho por dentro.
 *
 * <p>O modelo é o do Witchery peça por peça (ver o {@code MirrorRenderer}), e a mesma figura serve às duas metades
 * — a de baixo de cabeça para baixo. Estas fotos são para se ver que o oval fecha.
 */
public class OccultaMirrorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 0 0");

            // uma parede de pedra e o espelho pregado nela, olhando para quem joga
            server.runOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = jogador.blockPosition();
                BlockPos alto = pé.offset(0, 2, 5);
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = 0; dy <= 4; dy++) {
                        mundo.setBlockAndUpdate(pé.offset(dx, dy, 6), Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
                var espelho = OccultaBlocks.WITCH_MIRROR.defaultBlockState()
                        .setValue(MirrorBlock.FACING, Direction.NORTH);
                mundo.setBlockAndUpdate(alto, espelho.setValue(MirrorBlock.HALF, DoubleBlockHalf.UPPER));
                mundo.setBlockAndUpdate(alto.below(), espelho.setValue(MirrorBlock.HALF, DoubleBlockHalf.LOWER));
            });
            context.waitTicks(30);
            context.takeScreenshot("espelho_na_parede");

            // a cara do espelho, que é o que ele mostra a quem lhe pergunta
            server.runOnServer(s -> {
                var mundo = s.overworld();
                BlockPos alto = s.getPlayerList().getPlayers().getFirst().blockPosition().offset(0, 2, 5);
                MirrorFaceEntity.show(mundo, alto, Direction.NORTH);
            });
            context.waitTicks(20);
            context.takeScreenshot("espelho_cara");

            // e o vidro troca de figura quando há alguém à frente dele: a segunda folha do original
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos alto = jogador.blockPosition().offset(0, 2, 5);
                for (var cara : s.overworld().getEntitiesOfClass(MirrorFaceEntity.class,
                        new net.minecraft.world.phys.AABB(alto).inflate(4.0))) {
                    cara.discard();
                }
                jogador.teleportTo(s.overworld(), alto.getX() + 0.5, jogador.getY(), alto.getZ() - 2.5,
                        java.util.Set.of(), 0.0f, 10.0f, false);
            });
            context.waitTicks(30);
            context.takeScreenshot("espelho_com_gente_diante");

            // o item, na mão e na barra
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:witch_mirror");
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:witch_mirror");
            context.waitTicks(10);
            context.takeScreenshot("espelho_item");

            // a cela do Mundo do Espelho, de dentro: o forro de superfície de espelho e o espelho selado
            server.runOnServer(s -> {
                var mundo = MirrorWorld.level(s);
                if (mundo == null) throw new IllegalStateException("o Mundo do Espelho não abriu");
                var cela = MirrorWorld.claimCell(s.overworld(), new BlockPos(0, 70, 0));
                if (cela == null) throw new IllegalStateException("a cela não abriu");
                // umas luzes, que a cela é escura como breu
                for (int dx = 2; dx <= 8; dx += 3) {
                    mundo.setBlockAndUpdate(cela.offset(dx, 5, 0), Blocks.GLOWSTONE.defaultBlockState());
                }
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.teleportTo(mundo, cela.getX() + 7.5, cela.getY() - 2.0, cela.getZ() + 0.5,
                        java.util.Set.of(), 90.0f, 0.0f, false);
            });
            context.waitTicks(80);
            context.takeScreenshot("mundo_do_espelho_cela");

            // e o Reflexo que a guarda, que só vive lá: fora do Mundo do Espelho ele some, como no original
            server.runCommand("gamemode spectator");
            server.runOnServer(s -> {
                var mundo = MirrorWorld.level(s);
                var cela = MirrorWorld.cellMirror(s.getPlayerList().getPlayers().getFirst().blockPosition());
                if (mundo != null) ReflectionEntity.wake(mundo, cela);
            });
            context.waitTicks(8);
            context.takeScreenshot("mundo_do_espelho_reflexo");
        }
    }
}
