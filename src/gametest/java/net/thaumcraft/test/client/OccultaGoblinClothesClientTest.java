package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>roupa de goblin</b> vestida: dois manequins, um de frente e um de costas.
 *
 * <p>O de costas é o que importa: é onde se vê a <b>aljava</b> — quatro caixas presas ao tronco, tombadas
 * vinte graus, com três flechas dentro. É ela que faz a silhueta de quem tem a Aljava do Mog, e é a única
 * peça das três que se vê de longe.
 *
 * <p>E a terceira tela é as três peças no inventário, para as folhas delas se verem.
 */
public class OccultaGoblinClothesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("difficulty peaceful");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                for (int x = -3; x <= 3; x++) {
                    for (int z = 0; z <= 8; z++) {
                        level.setBlockAndUpdate(meio.offset(x, -1, z),
                                Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }

                // um de frente e um de costas, para a aljava se ver
                for (int volta = 0; volta < 2; volta++) {
                    var boneco = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
                    if (boneco == null) continue;
                    float giro = volta == 0 ? 180.0f : 0.0f;
                    boneco.snapTo(meio.getX() + 0.5 + (volta == 0 ? -1.5 : 1.5), meio.getY(),
                            meio.getZ() + 5.0, giro, 0.0f);
                    boneco.setYBodyRot(giro);
                    boneco.setYHeadRot(giro);
                    boneco.setItemSlot(EquipmentSlot.HEAD,
                            new ItemStack(OccultaItems.KOBOLDITE_HELM));
                    boneco.setItemSlot(EquipmentSlot.CHEST,
                            new ItemStack(OccultaItems.MOGS_QUIVER));
                    boneco.setItemSlot(EquipmentSlot.LEGS,
                            new ItemStack(OccultaItems.GULGS_GURDLE));
                    level.addFreshEntity(boneco);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("a_roupa_de_goblin_vestida");

            server.runCommand("gamemode creative @p");
            server.runCommand("give @p thaumcraft:koboldite_helm");
            server.runCommand("give @p thaumcraft:mogs_quiver");
            server.runCommand("give @p thaumcraft:gulgs_gurdle");
            context.waitTicks(20);
            context.takeScreenshot("a_roupa_de_goblin_no_cinto");
        }
    }
}
