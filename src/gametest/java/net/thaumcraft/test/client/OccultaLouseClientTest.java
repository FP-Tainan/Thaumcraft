package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.thaumcraft.occulta.OccultaEntities;

import java.util.Set;

/**
 * O <b>Piolho Parasita</b> e os dois cintos.
 *
 * <p>O piolho é a <b>lacrainha do jogo com outra folha</b> — e é o que há para conferir: que a folha
 * assenta na malha dela sem nada fora do lugar. E os dois cintos, calçados, mostram a <b>perna
 * inchada</b> do boneco das roupas, cada um com a sua cor.
 */
public class OccultaLouseClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            server.runCommand("give @p thaumcraft:louse");
            server.runCommand("give @p thaumcraft:biting_belt");
            server.runCommand("give @p thaumcraft:bark_belt");
            context.waitTicks(20);
            context.takeScreenshot("o_piolho_e_os_cintos_na_barra");

            // o bicho, de perto
            server.runOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos onde = jogador.blockPosition().relative(jogador.getDirection(), 3);
                var bicho = OccultaEntities.LOUSE.create(mundo, EntitySpawnReason.TRIGGERED);
                if (bicho == null) throw new IllegalStateException("o piolho não nasceu");
                bicho.snapTo(onde.getX() + 0.5, jogador.getY(), onde.getZ() + 0.5, 0.0f, 0.0f);
                bicho.poção(new PotionContents(Potions.POISON));
                bicho.setNoAi(true);
                mundo.addFreshEntity(bicho);
                jogador.teleportTo(mundo, onde.getX() + 0.5, jogador.getY(), onde.getZ() - 2.5,
                        Set.of(), 0.0f, 25.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("o_piolho");

            // e os dois cintos, calçados — longe do bicho, que de perto tapa a tela inteira
            server.runOnServer(s -> {
                for (var bicho : s.overworld().getEntitiesOfClass(
                        net.thaumcraft.occulta.louse.LouseEntity.class,
                        new net.minecraft.world.phys.AABB(
                                s.getPlayerList().getPlayers().getFirst().blockPosition())
                                .inflate(16.0))) {
                    bicho.discard();
                }
            });
            context.waitTicks(10);
            context.runOnClient(c -> c.options.setCameraType(
                    net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            for (var cinto : new String[]{"biting_belt", "bark_belt"}) {
                server.runCommand("item replace entity @p armor.legs with thaumcraft:" + cinto);
                context.waitTicks(20);
                context.takeScreenshot("cinto_" + cinto);
            }
        }
    }
}
