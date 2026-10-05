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
 * Os dois <b>deuses goblins</b>, lado a lado, com um goblin comum entre eles para a escala.
 *
 * <p>O que importa nesta tela é a <b>silhueta</b>: os dois têm a mesma cara de goblin — presas, nariz e
 * beiço — e corpos que dizem o que cada um faz. O <b>Mog</b> tem peitoral inclinada e saia, e um arco na
 * mão; o <b>Gulg</b> é um barril de dez por oito por seis com braços de dezesseis. Vê-se qual é qual de
 * longe, e é só isso que o desenho precisa de fazer.
 *
 * <p>E a altura: eles são <b>gente grande</b> — um e oito, como um jogador — ao lado de um goblin comum,
 * que dá pelo peito deles.
 */
public class OccultaGoblinGodsClientTest implements FabricClientGameTest {
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

                var mog = OccultaEntities.MOG.create(level, EntitySpawnReason.COMMAND);
                if (mog != null) {
                    mog.snapTo(meio.getX() - 1.5, meio.getY(), meio.getZ() + 6.0, 180.0f, 0.0f);
                    mog.setYBodyRot(180.0f);
                    mog.setYHeadRot(180.0f);
                    mog.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    mog.setPersistenceRequired();
                    mog.setNoAi(true);
                    level.addFreshEntity(mog);
                }

                var gulg = OccultaEntities.GULG.create(level, EntitySpawnReason.COMMAND);
                if (gulg != null) {
                    gulg.snapTo(meio.getX() + 2.5, meio.getY(), meio.getZ() + 6.0, 180.0f, 0.0f);
                    gulg.setYBodyRot(180.0f);
                    gulg.setYHeadRot(180.0f);
                    gulg.setPersistenceRequired();
                    gulg.setNoAi(true);
                    level.addFreshEntity(gulg);
                }

                // e um goblin comum no meio, para a altura se ver
                var goblin = OccultaEntities.GOBLIN.create(level, EntitySpawnReason.COMMAND);
                if (goblin != null) {
                    goblin.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 6.0, 180.0f, 0.0f);
                    goblin.setYBodyRot(180.0f);
                    goblin.setYHeadRot(180.0f);
                    goblin.setPersistenceRequired();
                    goblin.setNoAi(true);
                    level.addFreshEntity(goblin);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("o_mog_e_o_gulg");
        }
    }
}
