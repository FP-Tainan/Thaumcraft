package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * Os quatro goblins, lado a lado, e um deles com a picareta na mão.
 *
 * <p>O que importa nesta tela é o <b>nariz</b>: três degraus a sair da cara, cada um mais à frente que o
 * anterior. É o que faz um goblin ser um goblin, e é a única coisa que não se podia conferir sem olhar.
 *
 * <p>E a altura: ele é <b>mais baixo que um aldeão</b> — as pernas têm metade do tamanho —, e é isso que o faz
 * parecer o que é mesmo antes de alguém lhe ver a cara.
 */
public class OccultaGoblinClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                // os quatro ofícios, de frente para quem olha
                for (int ofício = 0; ofício < 4; ofício++) {
                    var goblin = OccultaEntities.GOBLIN.create(level, EntitySpawnReason.COMMAND);
                    if (goblin == null) continue;
                    goblin.snapTo(meio.getX() + 0.5 + (ofício - 1.5) * 1.6, meio.getY(), meio.getZ() + 5.5,
                            180.0f, 0.0f);
                    goblin.ofício(ofício);
                    // o corpo tem o giro dele, à parte do rumo: sem isto ele fica de costas
                    goblin.setYBodyRot(180.0f);
                    goblin.setYHeadRot(180.0f);
                    goblin.setPersistenceRequired();
                    goblin.setNoAi(true);
                    // o último leva a picareta e está martelando, para o braço aparecer levantado
                    if (ofício == 3) {
                        goblin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
                        goblin.trabalhando(true);
                    }
                    level.addFreshEntity(goblin);
                }

                // e um aldeão ao lado, para a altura se ver
                var aldeão = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level,
                        EntitySpawnReason.COMMAND);
                if (aldeão != null) {
                    aldeão.snapTo(meio.getX() + 0.5 + 3.6, meio.getY(), meio.getZ() + 5.5, 180.0f, 0.0f);
                    aldeão.setYBodyRot(180.0f);
                    aldeão.setYHeadRot(180.0f);
                    aldeão.setPersistenceRequired();
                    aldeão.setNoAi(true);
                    level.addFreshEntity(aldeão);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("quatro_goblins_e_um_aldeao");
        }
    }
}
